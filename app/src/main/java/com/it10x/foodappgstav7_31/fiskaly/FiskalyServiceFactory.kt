package com.it10x.foodappgstav7_31.fiskaly

import android.content.Context
import com.it10x.foodappgstav7_31.network.fiskaly.FiskalyApi
import com.it10x.foodappgstav7_31.network.fiskaly.FiskalyClient

object FiskalyServiceFactory {

    fun create(context: Context, countryCode: String): FiskalyRepository {
        return when (countryCode) {

            "DE" -> {
                val api: FiskalyApi = FiskalyClient.api
                FiskalyRepository(context, api)
            }

            else -> {
                // 👉 For India / others → return dummy repo OR reuse same
                val api: FiskalyApi = FiskalyClient.api
                FiskalyRepository(context, api)
            }
        }
    }
}