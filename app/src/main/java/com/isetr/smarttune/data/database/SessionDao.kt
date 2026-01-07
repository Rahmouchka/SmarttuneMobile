package com.isetr.smarttune.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SessionDao {

    @Query("SELECT * FROM session WHERE id = 1")
    fun getCurrentSession(): LiveData<SessionEntity?>

    @Query("SELECT * FROM session WHERE id = 1")
    suspend fun getCurrentSessionAsync(): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("DELETE FROM session")
    suspend fun clearSession()
}

