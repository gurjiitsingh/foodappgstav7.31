package com.it10x.foodappgstav7_31.network.fiskaly

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

object FiskalyClient {

    private const val BASE_URL = "https://kassensichv-middleware.fiskaly.com/api/v2/"

    val api: FiskalyApi by lazy {

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        // 🔥 COOKIE STORAGE
        val cookieJar = object : CookieJar {

            private val cookieStore = HashMap<String, MutableList<Cookie>>()

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {

                val storedCookies = cookieStore.getOrPut(url.host) { mutableListOf() }

                // remove old cookies with same name
                cookies.forEach { newCookie ->
                    storedCookies.removeAll { it.name == newCookie.name }
                    storedCookies.add(newCookie)
                }

                Log.d("COOKIE", "Saved: $storedCookies")
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val cookies = cookieStore[url.host] ?: emptyList()
            //    Log.d("FISKALY", "Sending cookies: $cookies") // 🔥 ADD THIS
                return cookies
            }
        }

        val client = OkHttpClient.Builder()
            .cookieJar(cookieJar) // 🔥 IMPORTANT
//            .addInterceptor(logging)
//            .addInterceptor(BearerInterceptor())

            .addInterceptor(BearerInterceptor())  // FIRST
            .addInterceptor(logging)              // SECOND

            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FiskalyApi::class.java)
    }
}