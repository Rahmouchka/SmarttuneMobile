# 📱 SmartTune - Authentification & Inscription Complète

## 🎯 Vue d'ensemble du projet

SmartTune est une application mobile Android (Kotlin) de streaming musical. L'application suit l'architecture **MVVM** (Model-View-ViewModel) avec une séparation claire entre les couches :
- **Vue (UI)** : Activities et Fragments qui affichent les données
- **ViewModel** : Gère l'état et la logique métier
- **Repository** : Accède aux données (API et Base de Données)
- **API Retrofit** : Communication avec le serveur backend
- **Room Database** : Stockage local de la session utilisateur

---

## 🔐 Système d'Authentification

### Objectif Général
Permettre aux utilisateurs de :
1. **S'inscrire** (User ou Artist) via l'API
2. **Se connecter** via l'API
3. **Rester connectés** grâce à la session locale
4. **Se déconnecter** en supprimant la session

### Règles Clés (Conformes au PDF)
- ✅ **Authentification via API uniquement** : Les identifiants sont validés par le serveur backend
- ✅ **Session créée UNIQUEMENT lors du login** : Pas lors de l'inscription !
- ✅ **Flux inscription** : 1️⃣ Créer compte (server) → 2️⃣ Rediriger vers login → 3️⃣ Se connecter (créer session)
- ✅ **Pas de token JWT** : Le backend ne fournit pas de token, on utilise juste l'userId
- ✅ **État observable** : MainActivity observe la session pour contrôler la navigation

---

## 📂 Architecture Technique Détaillée

### 1️⃣ COUCHE DONNÉES (Data Layer)

#### A. Entité Room - SessionEntity.kt
```
SessionEntity (Table Room)
├── userId: Long (clé primaire)
├── userRole: String ("USER", "ARTIST", "ADMIN")
└── Représente : La session de l'utilisateur connecté
```

**Pourquoi ?** 
- Room stocke les données de manière persistante sur le téléphone
- Permet à l'app de savoir qui est connecté après un redémarrage

#### B. DAO - SessionDao.kt
```
Interface SessionDao
├── saveSession(session: SessionEntity) → INSERT/UPDATE
├── getSession() → SELECT (observable en LiveData)
└── clearSession() → DELETE
```

**Pourquoi ?**
- Le DAO est le pont entre l'app et la base de données Room
- Chaque opération (lire, écrire, supprimer) passe par ici

#### C. Database - SmartTuneDatabase.kt
```
Abstract class SmartTuneDatabase : RoomDatabase()
├── sessionDao(): SessionDao
└── Version = 2 (migré depuis v1)
```

**Pourquoi ?**
- C'est le point d'accès unique à la base de données
- Centralise toutes les entités Room de l'app

#### D. API - AuthApi.kt
```
Interface AuthApi (Retrofit)
├── login(request: LoginRequest) → Response<User>
├── registerUser(request: UserRegistrationRequest) → Response<User>
└── registerArtist(...) → Response<ArtistRequest>
```

**Pourquoi ?**
- Définit les endpoints API que l'app utilise
- Retrofit utilise cette interface pour générer les appels HTTP

#### E. DTOs (Data Transfer Objects)
```
LoginRequest
├── email: String
└── password: String

UserRegistrationRequest
├── username: String
├── nom: String
├── prenom: String
├── email: String
├── numTel: String?
├── dateNaissance: String (format YYYY-MM-DD)
├── genre: String ("H" ou "F")
└── password: String

ArtistRegistrationRequest
├── Tous les champs de UserRegistrationRequest
├── bio: String
└── (+ upload PDF)

User (réponse du serveur)
├── id: Long
├── username: String
├── email: String
├── nom: String?
├── prenom: String?
├── role: String ("USER" ou "ARTIST")
└── isActive: Boolean
```

**Pourquoi ?**
- Sérialisent/désérialisent les données JSON en objets Kotlin
- Gson convertit automatiquement JSON ↔ Kotlin objects

---

### 2️⃣ COUCHE REPOSITORY (Business Logic)

#### AuthRepository.kt
```
AuthRepository
├── login(email, password)
│   ├── 1. Appelle api.login(LoginRequest)
│   ├── 2. Si succès → Sauvegarde la session en Room
│   ├── 3. Retourne Result<User>
│   └── Gère les erreurs de connexion réseau
│
├── registerUser(username, nom, prenom, email, numTel, dateNaissance, genre, password)
│   ├── 1. Crée UserRegistrationRequest
│   ├── 2. Appelle api.registerUser()
│   ├── 3. Si succès → Retourne User créé
│   ├── 4. ❌ NE SAUVEGARDE PAS la session (voir login() pour ça)
│   ├── 5. Utilisateur sera redirigé vers LoginActivity
│   └── Format de date : "YYYY-MM-DD" depuis le formulaire
│
├── registerArtist(...)
│   ├── Similaire à registerUser
│   ├── Convertit le PDF en MultipartBody.Part
│   ├── Envoie les données en multipart/form-data
│   └── Retourne Result<ArtistRequest>
│
└── logout()
    ├── Appelle sessionDao.clearSession()
    └── Supprime la session locale
```

**Logique Clé :**
```
login() {
    try {
        // 1. Appel API - Le serveur valide l'email/password
        val response = api.login(LoginRequest(email, password))
        
        if (response.isSuccessful && response.body() != null) {
            // 2. Récupérer l'utilisateur retourné
            val user = response.body()!!
            
            // 3. Créer une session locale
            val session = SessionEntity(
                userId = user.id,
                userRole = user.role
            )
            
            // 4. Sauvegarder en Room (persistance locale)
            sessionDao.saveSession(session)
            
            // 5. Retourner le succès
            return Result.success(user)
        } else {
            // Erreur serveur
            return Result.failure(Exception("Connexion échouée"))
        }
    } catch (e: Exception) {
        // Erreur réseau, timeout, etc.
        return Result.failure(e)
    }
}
```

**Pourquoi cette approche ?**
- ✅ Authentification centralisée : un seul endroit gère login/register
- ✅ API appelée en premier : le serveur valide les credentials
- ✅ Session sauvegardée après : on stocke le userId pour plus tard
- ✅ Gestion d'erreurs robuste : try-catch + Result pattern

---

### 3️⃣ COUCHE VIEWMODEL (Présentation Logic)

#### AuthViewModel.kt
```
AuthViewModel : ViewModel()
├── uiState: LiveData<AuthUiState>
│   ├── Émet Loading → affiche ProgressBar
│   ├── Émet SuccessUser → affiche "Compte créé"
│   ├── Émet SuccessArtist → affiche "Demande envoyée"
│   └── Émet Error(message) → affiche le message d'erreur
│
├── authenticatedUser: LiveData<SessionEntity?>
│   ├── Observé par MainActivity
│   ├── Si null → Utilisateur NOT connecté
│   └── Si != null → Utilisateur connecté
│
├── login(email, password)
│   ├── Émet Loading
│   ├── Appelle repository.login()
│   └── Émet Success ou Error
│
├── registerUser(...)
│   ├── Même logique que login()
│   └── Émet SuccessUser après inscription
│
├── registerArtist(...)
│   ├── Même logique
│   └── Émet SuccessArtist après inscription
│
└── logout()
    ├── Appelle repository.logout()
    ├── authenticatedUser devient null
    └── MainActivity se redessine automatiquement
```

**Logique Clé :**
```
fun login(email: String, password: String) {
    _uiState.value = AuthUiState.Loading
    
    viewModelScope.launch {
        val result = repository.login(email, password)
        
        result.fold(
            onSuccess = { user ->
                // Succès ! Émettre l'état Success
                _uiState.value = AuthUiState.SuccessUser(user)
                // authenticatedUser est observé depuis la SessionEntity
            },
            onFailure = { error ->
                // Erreur ! Émettre l'état Error
                _uiState.value = AuthUiState.Error(error.message ?: "Erreur")
            }
        )
    }
}
```

**Pourquoi ViewModel ?**
- ✅ Gère les coroutines de manière safe (viewModelScope)
- ✅ Survit aux rotation d'écran (config changes)
- ✅ LiveData notifie automatiquement les observateurs
- ✅ Sépare la logique métier de l'UI

---

### 4️⃣ COUCHE UI (Presentation)

#### A. LoginActivity.kt
```
LoginActivity : AppCompatActivity()
├── Observe viewModel.uiState
│   ├── Si Loading → affiche ProgressBar
│   ├── Si Success → navigue vers MainActivity
│   └── Si Error → affiche Toast
│
└── Bouton "Se connecter"
    ├── Valide email et password
    └── Appelle viewModel.login(email, password)
```

**Flux :**
```
Utilisateur tape email + password
         ↓
Clique "Se connecter"
         ↓
Valider les champs
         ↓
viewModel.login(email, password)
         ↓
ViewModel observe le repository
         ↓
Repository appelle API
         ↓
Serveur valide et retourne User
         ↓
ViewModel émet Success
         ↓
LoginActivity observer reçoit Success
         ↓
Navigue vers MainActivity
```

#### B. SignupUserActivity.kt & SignupArtistActivity.kt
```
SignupUserActivity : AppCompatActivity()
├── Formulaire avec champs : username, nom, prenom, email, dateNaissance, genre, password
├── Valide chaque champ
├── Observe viewModel.uiState
└── Bouton "Créer mon compte"
    ├── Valide tous les champs
    ├── Appelle viewModel.registerUser(...)
    └── Redirige vers LoginActivity si succès

SignupArtistActivity : AppCompatActivity()
├── Formulaire + upload PDF
├── Bouton "Envoyer ma demande"
├── Observe viewModel.uiState
└── Redirige vers LoginActivity si succès
```

#### C. MainActivity.kt
```
MainActivity : AppCompatActivity()
├── Observe viewModel.authenticatedUser
│   ├── Si null (pas connecté)
│   │   ├── Affiche écran "Votre musique, Partout, Toujours"
│   │   ├── Bouton "Commencer maintenant" → SignupTypeActivity
│   │   └── Bouton "En savoir plus" → Affiche section "Pourquoi SmartTune"
│   │
│   └── Si != null (connecté)
│       ├── Affiche "Bienvenue, Utilisateur !"
│       ├── Bouton "Découvrir la musique" → Feed (à implémenter)
│       └── Bouton "Se déconnecter" → Appelle viewModel.logout()
│
└── Section "Pourquoi SmartTune ?"
    ├── 4 cartes : Musique illimitée, Découvrez, Artistes, Pour les artistes
    ├── Bouton "Créer un compte gratuitement"
    └── Bouton "← Retour" pour revenir
```

---

## 🔄 Flux Complet d'Authentification

### Cas 1 : INSCRIPTION UTILISATEUR

```
┌─────────────────────────────────────────────────────────┐
│ 1. Utilisateur arrive sur MainActivity (pas connecté)   │
│    → Affiche écran d'accueil                            │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 2. Clique "Commencer maintenant"                        │
│    → Navigue vers SignupTypeActivity                    │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 3. Choisit "Utilisateur"                               │
│    → Navigue vers SignupUserActivity                    │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 4. Remplit le formulaire :                              │
│    - Username : "john_doe"                              │
│    - Nom : "Dupont"                                     │
│    - Prénom : "Jean"                                    │
│    - Email : "john@example.com"                         │
│    - Tél : "+21620123456"                               │
│    - Date naissance : "1995-05-20"                      │
│    - Genre : "Homme"                                    │
│    - Mot de passe : "SecurePass123!"                    │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 5. Clique "Créer mon compte"                            │
│    → Validation locale des champs                       │
│    → Appelle viewModel.registerUser(...)               │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 6. ViewModel émet Loading (ProgressBar visible)         │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 7. Repository.registerUser() est appelé                 │
│    ↓                                                    │
│    Crée UserRegistrationRequest avec tous les champs   │
│    ↓                                                    │
│    Appelle api.registerUser(request)                   │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 8. SERVEUR BACKEND (Spring Boot)                        │
│    ├─ Valide tous les champs (email, password, etc.)   │
│    ├─ Vérifie les patterns (email format, pwd strength)│
│    ├─ Crée un nouvel utilisateur en base de données    │
│    └─ Retourne User{id: 123, username: "john_doe", ... │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 9. Repository reçoit la réponse                         │
│    ├─ ❌ NE CRÉE PAS la session (inscription ≠ login)  │
│    ├─ Retourne Result.success(user)                    │
│    └─ authenticatedUser reste null                     │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 10. ViewModel émet SuccessUser                          │
│     ├─ L'utilisateur est créé sur le serveur           │
│     └─ Mais la session locale est VIDE (null)          │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 11. SignupUserActivity observer reçoit SuccessUser      │
│     ├─ Affiche Toast "Compte créé avec succès !"       │
│     └─ Navigue vers LoginActivity                      │
│        (L'utilisateur doit se connecter maintenant)    │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 12. LoginActivity                                       │
│     ├─ L'utilisateur rentre ses identifiants           │
│     ├─ Clique "Se connecter"                           │
│     └─ C'est LÀ que la session est créée en Room !     │
└─────────────────────────────────────────────────────────┘
```

### Cas 2 : CONNEXION UTILISATEUR

```
┌─────────────────────────────────────────────────────────┐
│ 1. LoginActivity                                        │
│    Utilisateur tape : email et password                 │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 2. Clique "Se connecter"                                │
│    → viewModel.login(email, password)                  │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 3. ViewModel émet Loading                               │
│    (Affiche ProgressBar, désactive le bouton)          │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 4. Repository.login(email, password)                    │
│    ├─ Crée LoginRequest                                │
│    ├─ Appelle api.login(request)                       │
│    └─ Lance un appel HTTP POST au serveur              │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 5. SERVEUR valide l'email et le password               │
│    ├─ Email existe-t-il ? ✓                            │
│    ├─ Password correct ? ✓ (hash bcrypt)               │
│    └─ Retourne User{id: 123, role: "USER", ...}        │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 6. Repository reçoit User                               │
│    ├─ Crée SessionEntity(123, "USER")                  │
│    ├─ Sauvegarde en Room                               │
│    └─ Retourne Result.success(user)                    │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 7. ViewModel.authenticatedUser notifie ses observateurs │
│    (Room a détecté la sauvegarde)                       │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 8. LoginActivity observer reçoit SuccessUser            │
│    ├─ Affiche Toast "Connexion réussie"                │
│    └─ Navigue vers MainActivity                        │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 9. MainActivity                                         │
│    ├─ Observe authenticatedUser                        │
│    ├─ Reçoit la SessionEntity (userId != null)         │
│    └─ Affiche "Bienvenue, Utilisateur !"               │
│        avec boutons "Découvrir" et "Se déconnecter"    │
└─────────────────────────────────────────────────────────┘
```

### Cas 3 : DÉCONNEXION

```
┌─────────────────────────────────────────────────────────┐
│ 1. MainActivity (utilisateur connecté)                  │
│    Clique bouton "Se déconnecter"                       │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 2. Appelle viewModel.logout()                           │
│    ↓                                                    │
│    Repository.logout()                                 │
│    ↓                                                    │
│    sessionDao.clearSession() (DELETE from session)     │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 3. Room notifie les observateurs                        │
│    authenticatedUser = null                            │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 4. MainActivity observer reçoit null                     │
│    ├─ isShowingAbout = false (retour à l'accueil)      │
│    └─ Affiche écran d'accueil avec "Commencer" button  │
└─────────────────────────────────────────────────────────┘
```

---

## 🛡️ Sécurité & Validations

### Validations Client (dans l'Activity)
```
Email
  ├─ Vérifie que ce n'est pas vide
  └─ Utilise Patterns.EMAIL_ADDRESS

Mot de passe
  ├─ Minimum 8 caractères
  ├─ Au moins 1 majuscule (regex: .*[A-Z].*)
  ├─ Au moins 1 minuscule (regex: .*[a-z].*)
  ├─ Au moins 1 chiffre (regex: .*[0-9].*)
  └─ Au moins 1 caractère spécial (regex: .*[^A-Za-z0-9].*)

Username
  ├─ Minimum 3 caractères
  ├─ Caractères autorisés : lettres, chiffres, _, -
  └─ Pattern : ^[a-zA-Z0-9_-]+$

Date de naissance
  ├─ Format : YYYY-MM-DD (ISO 8601)
  ├─ Doit être dans le passé
  └─ Le formulaire Android utilise inputType="date"

Téléphone (optionnel)
  ├─ Si rempli : doit être un numéro tunisien
  └─ Pattern : ^(\+216|00216)?[0-9]{8}$
```

### Validations Serveur (Spring Boot - CONFIANCE)
```
Email
  ├─ Format Email valide (@Email)
  ├─ Email unique en BDD
  └─ Longueur 5-255 caractères

Mot de passe
  ├─ Minimum 8 caractères
  ├─ Complexité (maj, min, chiffre, spécial)
  └─ Hashé en bcrypt avant stockage (jamais en clair !)

Username
  ├─ Unique en BDD
  ├─ Format pattern
  └─ Longueur 3-30 caractères
```

---

## 🗂️ Fichiers Créés/Modifiés

### ✅ CRÉÉS
```
data/database/
  ├─ SessionEntity.kt (Table Room)
  ├─ SessionDao.kt (DAO)
  └─ SmartTuneDatabase.kt (Database - version 2)

dto/
  ├─ UserRegistrationRequest.kt
  ├─ ArtistRegistrationRequest.kt
  ├─ ArtistRequest.kt
  └─ LoginRequest.kt

ui/auth/
  ├─ LoginActivity.kt
  ├─ SignupUserActivity.kt
  ├─ SignupArtistActivity.kt
  └─ SignupTypeActivity.kt

ui/
  └─ MainActivity.kt (refactorisé)

viewModel/
  └─ AuthViewModel.kt (refactorisé)

layout/
  ├─ activity_login.xml
  ├─ activity_signup_user.xml
  ├─ activity_signup_artist.xml
  ├─ activity_main.xml
  └─ activity_main_about.xml
```

### ✏️ MODIFIÉS
```
data/
  ├─ AuthRepository.kt (refactorisé pour Room + API)
  ├─ AuthApi.kt (endpoints CRUD)
  └─ RetrofitClient.kt (configuration Gson avec LocalDate)

viewModel/
  └─ AuthViewModel.kt (LiveData observable)
```

---

## 🔑 Concepts Clés à Maîtriser

### 1. MVVM Architecture
- **M** = Model (SessionEntity, User)
- **V** = View (Activity, Layout XML)
- **VM** = ViewModel (AuthViewModel)

**Flux de données :**
```
UI (observe) ← ViewModel (appelle) ← Repository (utilise) ← API/Room
```

### 2. LiveData & Observers
```
ViewModel émet une valeur
  ↓
Tous les observateurs reçoivent une notification
  ↓
Observateur met à jour l'UI (automatiquement)

Avantage : Pas de memory leak (LiveData est lifecycle-aware)
```

### 3. Room Database
```
SessionEntity (objet Kotlin)
  ↓ (Serialization)
Room database (SQLite local)
  ↓ (sur le téléphone)
Données persistantes même après redémarrage
```

### 4. Retrofit API
```
Interface AuthApi (définit les endpoints)
  ↓
RetrofitClient (crée une instance Retrofit)
  ↓
api.login() → HTTP POST → Serveur Spring Boot
  ↓
Response<User> ou erreur
```

### 5. Coroutines avec viewModelScope
```
viewModel.registerUser() {
    viewModelScope.launch {  ← Coroutine lifecycle-safe
        val result = repository.login()  ← Appel réseau (non-blocking)
        // Résultat reçu
        _uiState.value = Success
    }
}
```

---

## 🎓 Ce que vous pouvez expliquer à votre professeur

### Question : "Pourquoi avoir séparé UI, ViewModel et Repository ?"
**Réponse :**
- **UI n'appelle jamais l'API directement** : C'est le Repository qui le fait
- **ViewModel gère l'état** : Les observers sont notifiés automatiquement
- **Repository est réutilisable** : Changer l'UI n'affecte pas la logique
- **Facile à tester** : On peut mocker Repository et API

### Question : "Comment fonctionne la session ?"
**Réponse :**
1. Après login réussi, on crée une SessionEntity avec userId et userRole
2. On la sauvegarde en Room (base de données locale)
3. Room notifie automatiquement les observateurs
4. MainActivity observe cette session et change l'UI
5. À la déconnexion, on efface la session
6. MainActivity se redessine automatiquement

### Question : "Pourquoi utiliser LiveData et pas juste les callbacks ?"
**Réponse :**
- LiveData est **lifecycle-aware** : pas de memory leak quand l'Activity est détruite
- Les observateurs sont **notifiés automatiquement** sans code supplémentaire
- LiveData **survit aux rotations d'écran**
- C'est la pratique recommandée par Google

### Question : "Comment les erreurs sont gérées ?"
**Réponse :**
1. Try-catch autour de tous les appels API
2. Result<T> pattern : success() ou failure()
3. ViewModel émet un état Error avec le message
4. Activity affiche le message à l'utilisateur

---

## 📊 Résumé Visuel

```
ARCHITECTURE COMPLÈTE
═══════════════════════════════════════════════════════════

                   ACTIVITIES (UI)
                   ─────────────
        LoginActivity    SignupUserActivity
              │                │
              └────────┬───────┘
                       │
              observe LiveData
                       ↓
                 ┌──────────────┐
                 │ AuthViewModel │
      (gère l'état + logique)
                 └──────────────┘
                       │
              appelle les fonctions
                       ↓
            ┌─────────────────────┐
            │  AuthRepository    │
      (pont entre UI et données)
            └────────┬──────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
       API        Room DB    RetrofitClient
   (serveur)   (localStorage) (réseau)
        │            │            │
        └────────────┼────────────┘
                     │
            DONNÉES PERSISTANTES
```

---

## ✅ Checklist Finale

- [x] Login via API ✓
- [x] Register (User + Artist) via API ✓
- [x] Session sauvegardée en Room ✓
- [x] MainActivity observe la session ✓
- [x] Navigation basée sur l'authentification ✓
- [x] Déconnexion efface la session ✓
- [x] Validations client et serveur ✓
- [x] Gestion d'erreurs robuste ✓
- [x] MVVM strictement respectée ✓
- [x] Section "Pourquoi SmartTune ?" ✓
- [x] Formulaires avec RadioButtons pour genre ✓

---

## 🎯 Conclusion

L'application SmartTune implémente une authentification **complète et sécurisée** :
- ✅ Les credentials sont validés par le serveur (pas de validation locale seule)
- ✅ La session est persistante en Room
- ✅ L'interface s'adapte automatiquement (connecté/non-connecté)
- ✅ L'architecture MVVM permet une maintenance facile
- ✅ Les erreurs sont gérées proprement

**Tout est prêt pour le test avec le backend Spring Boot !** 🚀

