package com.it10x.foodappgstav7_31.fiskaly

import android.content.Context
import android.util.Log
import com.it10x.foodappgstav7_31.network.fiskaly.FiskalyApi
import com.it10x.foodappgstav7_31.network.fiskaly.FiskalyAuthService
import com.it10x.foodappgstav7_31.network.fiskaly.TokenManager
import com.it10x.foodappgstav7_31.network.model.*
import com.it10x.foodappgstav7_31.storage.TssStorage
import kotlinx.coroutines.delay
import java.util.UUID

// ✅ NO-OP SERVICE (for India etc.)
class NoOpFiskalyService : FiskalyService {

    override fun isEnabled() = false

    override suspend fun startTransaction(sessionId: String): Pair<String, String>? {
        return null
    }

    override suspend fun finishTransaction(
        txId: String,
        clientId: String,
        vatList: List<VatAmount>,
        paymentList: List<PaymentAmount>
    ) {
        // do nothing
    }

    override suspend fun cancelTransaction(
        txId: String,
        clientId: String
    ) {
        // do nothing
    }
}

// ✅ MAIN REPOSITORY
class FiskalyRepository(
    private val context: Context,
    private val api: FiskalyApi   // ✅ USE THIS ONLY
) {

    // ==========================
    // AUTH
    // ==========================
    private suspend fun ensureAuthenticated() {

        val token = TokenManager.getToken()

        if (token.isNullOrEmpty()) {
            Log.d("FISKALY", "🔑 Token missing → authenticating")

            val newToken = FiskalyAuthService.authenticate(api)

            TokenManager.saveToken(newToken)

            Log.d("FISKALY", "✅ Token saved")
        }
    }

    // ==========================
    // START TRANSACTION
    // ==========================
    suspend fun startTransaction(): Pair<String, String> {

        ensureAuthenticated()

        val tssId = TssStorage.getTssId(context)
            ?: throw Exception("TSS not initialized")

        val txId = UUID.randomUUID().toString()

        var clientId = TssStorage.getClientId(context)

        if (clientId == null) {

            clientId = UUID.randomUUID().toString()

            api.createClient(
                tssId,
                clientId,
                ClientRequest(serial_number = clientId)
            )

            TssStorage.saveClientId(context, clientId)

            Log.d("FISKALY", "✅ NEW CLIENT CREATED: $clientId")

            delay(300)

        } else {
            Log.d("FISKALY", "♻️ REUSING CLIENT: $clientId")
        }

        // ✅ START TX
        api.startTransaction(
            tssId = tssId,
            txId = txId,
            txRevision = 1,
            request = StartTransactionRequest(client_id = clientId)
        )

        Log.d("FISKALY", "TX STARTED → $txId")

        return Pair(txId, clientId)
    }

    // ==========================
    // FINISH TRANSACTION
    // ==========================
    suspend fun finishTransaction(
        txId: String,
        clientId: String,
        vatList: List<VatAmount>,
        paymentList: List<PaymentAmount>
    ) {

        ensureAuthenticated()   // ✅ IMPORTANT

        val tssId = TssStorage.getTssId(context)
            ?: throw Exception("TSS not initialized")

        val finishRequest = FinishTransactionRequest(
            client_id = clientId,
            schema = Schema(
                standard_v1 = StandardV1(
                    receipt = Receipt(
                        amounts_per_vat_rate = vatList,
                        amounts_per_payment_type = paymentList
                    )
                )
            )
        )

        val response = api.finishTransaction(
            tssId = tssId,
            txId = txId,
            txRevision = 2,
            request = finishRequest
        )

        Log.d("FISKALY", "TX FINISHED → $response")
    }

    // ==========================
    // CANCEL TRANSACTION
    // ==========================
    suspend fun cancelTransaction(
        txId: String,
        clientId: String
    ) {

        ensureAuthenticated()   // ✅ IMPORTANT

        val tssId = TssStorage.getTssId(context)
            ?: throw Exception("TSS not initialized")

        val request = FinishTransactionRequest(
            client_id = clientId,
            state = "CANCELLED",
            schema = Schema(
                standard_v1 = StandardV1(
                    receipt = Receipt(
                        amounts_per_vat_rate = listOf(
                            VatAmount("NORMAL", "0.00")
                        ),
                        amounts_per_payment_type = listOf(
                            PaymentAmount("CASH", "0.00")
                        )
                    )
                )
            )
        )

        api.finishTransaction(
            tssId = tssId,
            txId = txId,
            txRevision = 2,
            request = request
        )

        Log.d("FISKALY", "TX CANCELLED → $txId")
    }
}