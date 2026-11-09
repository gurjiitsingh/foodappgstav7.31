package com.it10x.foodappgstav7_31.network

import com.it10x.foodappgstav7_31.data.models.ClientResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ClientApi {

    @GET("api/client/{clientId}")
    suspend fun getClientConfig(
        @Path("clientId") clientId: String,
        @Header("Authorization") token: String = "Bearer my_secret_key"
    ): ClientResponse
}