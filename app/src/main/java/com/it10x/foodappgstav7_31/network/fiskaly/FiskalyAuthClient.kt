package com.it10x.foodappgstav7_31.network.fiskaly

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FiskalyAuthClient {

    private const val BASE_URL = "https://auth.fiskaly.com/v2/"

    val api: FiskalyApi by lazy {

        val client = OkHttpClient.Builder().build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FiskalyApi::class.java)
    }
}