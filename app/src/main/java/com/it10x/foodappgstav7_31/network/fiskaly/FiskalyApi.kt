package com.it10x.foodappgstav7_31.network.fiskaly


import com.it10x.foodappgstav7_31.network.model.ActivateTssRequest
import com.it10x.foodappgstav7_31.network.model.AuthRequest
import com.it10x.foodappgstav7_31.network.model.AuthResponse
import com.it10x.foodappgstav7_31.network.model.TseResponse
import com.it10x.foodappgstav7_31.network.model.ClientRequest
import com.it10x.foodappgstav7_31.network.model.ClientResponse
import com.it10x.foodappgstav7_31.network.model.FinishTransactionRequest
import com.it10x.foodappgstav7_31.network.model.StartTransactionRequest
import com.it10x.foodappgstav7_31.network.model.TransactionResponse
import com.it10x.foodappgstav7_31.network.model.ExportResponse
import com.it10x.foodappgstav7_31.network.model.TssRequest
import retrofit2.Response

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FiskalyApi {



    @POST("auth")
    suspend fun authenticate(
        @Body request: AuthRequest
    ): AuthResponse


    // CREATE TSS (requires GUID)
    @PUT("tss/{tssId}")
    suspend fun createTss(
        @Path("tssId") tssId: String,
        @Body request: TssRequest
    ): TseResponse



    // ACTIVATE TSS
//    @PATCH("tss/{tssId}")
//    suspend fun activateTss(
//        @Path("tssId") tssId: String,
//        @Body request: ActivateTssRequest
//    ): TseResponse
    @PATCH("tss/{tssId}")
    suspend fun updateTss(
        @Path("tssId") tssId: String,
        @Body request: UpdateTssRequest
    ): TseResponse


    // CHECK TSS STATUS
    @GET("tss/{tssId}")
    suspend fun getTssStatus(
        @Path("tssId") tssId: String
    ): TseResponse



    @PUT("tss/{tssId}/client/{clientId}")
    suspend fun createClient(
        @Path("tssId") tssId: String,
        @Path("clientId") clientId: String,
        @Body request: ClientRequest
    ): ClientResponse


    // ✅ START TRANSACTION (CREATE → revision = 1)
    @PUT("tss/{tssId}/tx/{txId}")
    suspend fun startTransaction(
        @Path("tssId") tssId: String,
        @Path("txId") txId: String,
        @Query("tx_revision") txRevision: Int, // 🔥 REQUIRED
        @Body request: StartTransactionRequest
    ): TransactionResponse


    // ✅ FINISH TRANSACTION (UPDATE → revision 2+)
    @PUT("tss/{tssId}/tx/{txId}")
    suspend fun finishTransaction(
        @Path("tssId") tssId: String,
        @Path("txId") txId: String,
        @Query("tx_revision") txRevision: Int,
        @Body request: FinishTransactionRequest
    ): TransactionResponse




    // EXPORT DATA
    @POST("tse/v2/exports")
    suspend fun createExport(): ExportResponse







    @PATCH("tss/{tssId}/admin")
    suspend fun setAdminPin(
        @Path("tssId") tssId: String,
        @Body body: AdminPinRequest
    ): Response<Unit>

    @POST("tss/{tssId}/admin/auth")
    suspend fun adminAuth(
        @Path("tssId") tssId: String,
        @Body body: AdminAuthRequest
    ): Response<Unit>




}

data class UpdateTssRequest(
    val state: String,
    val admin_pin: String? = null
)

data class AdminPinRequest(
    val admin_puk: String,
    val new_admin_pin: String
)

data class AdminAuthRequest(
    val admin_pin: String
)