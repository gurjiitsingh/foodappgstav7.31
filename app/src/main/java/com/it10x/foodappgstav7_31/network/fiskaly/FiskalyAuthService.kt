package com.it10x.foodappgstav7_31.network.fiskaly

import com.it10x.foodappgstav7_31.network.model.AuthRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FiskalyAuthService {

    suspend fun authenticate(api: FiskalyApi): String {

        val response = api.authenticate(
            AuthRequest(
                api_key = "test_ba6mebcylsqgci25j6nx3pcqs_german-2",
                api_secret = "z1HdFZGPAhlvCWt0avK0zbyUvKoqwNu1h9S1UnCJTK8"
            )
        )



        TokenManager.saveToken(response.access_token)

        return response.access_token
    }
}