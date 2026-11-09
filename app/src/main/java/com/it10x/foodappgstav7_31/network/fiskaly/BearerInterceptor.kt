package com.it10x.foodappgstav7_31.network.fiskaly

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID

class BearerInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val original = chain.request()

        val token = TokenManager.getToken()
        Log.d("FISKALY", "Token in bearer = ${TokenManager.getToken()}")
        val requestBuilder = original.newBuilder()

        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()

        return chain.proceed(request)
    }
}