package com.isetr.smarttune.data

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        return if (sessionManager.accessToken != null) {
            val requestBuilder = original.newBuilder()
                .addHeader("Authorization", "Bearer ${sessionManager.accessToken}")
            chain.proceed(requestBuilder.build())
        } else {
            chain.proceed(original)
        }
    }
}