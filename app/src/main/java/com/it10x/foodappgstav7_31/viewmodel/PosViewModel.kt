package com.it10x.foodappgstav7_31.viewmodel

import android.app.Application
import retrofit2.HttpException
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_31.network.fiskaly.*
import com.it10x.foodappgstav7_31.network.model.ClientRequest
import com.it10x.foodappgstav7_31.network.model.FinishTransactionRequest
import com.it10x.foodappgstav7_31.network.model.PaymentAmount
import com.it10x.foodappgstav7_31.network.model.Receipt
import com.it10x.foodappgstav7_31.network.model.Schema
import com.it10x.foodappgstav7_31.network.model.StandardV1
import com.it10x.foodappgstav7_31.network.model.StartTransactionRequest
import com.it10x.foodappgstav7_31.network.model.TssRequest
import com.it10x.foodappgstav7_31.network.model.VatAmount
import com.it10x.foodappgstav7_31.storage.TssStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class PosViewModel(application: Application) : AndroidViewModel(application) {

    init {
        Log.d("FISKALY", "PosViewModel initialized")
        startFiskalyFlow()
    }

    private fun startFiskalyFlow() {
        viewModelScope.launch {
            try {
                val api = FiskalyClient.api

                // 🔥 STEP 1 - AUTH
                Log.d("FISKALY", "Step 1: Authentication...")
                val token = FiskalyAuthService.authenticate(api)
                TokenManager.saveToken(token)
                delay(200)

                val context = getApplication<Application>().applicationContext

                var tssId = TssStorage.getTssId(context)
                var puk = TssStorage.getPuk(context)

                var tssValid = false


                if (tssId != null && puk != null) {

                    try {
                        val tss = api.getTssStatus(tssId)

                        Log.d("FISKALY", "Existing TSS state: ${tss.state}")

                        // ✅ CHECK IF DELETED
                        if (tss.state == "DELETED") {
                            Log.d("FISKALY", "TSS deleted → recreating")
                            tssValid = false
                        } else {
                            tssValid = true
                        }

                    } catch (e: Exception) {
                        Log.d("FISKALY", "Saved TSS invalid → will recreate")
                        tssValid = false
                    }
                }



                if (tssId == null || puk == null || !tssValid) {

                    // 🔥 FIRST-TIME OR INVALID → CREATE NEW
                    tssId = UUID.randomUUID().toString()
                    Log.d("FISKALY", "Creating NEW TSS → $tssId")

                    val tss = api.createTss(
                        tssId,
                        TssRequest(metadata = mapOf("created_by" to "POS_APP"))
                    )

                    puk = tss.admin_puk ?: throw Exception("PUK missing from Fiskaly")
                    Log.d("FISKALY", "PUK: $puk")

                    // ✅ SAVE BOTH (PAIR)
                    TssStorage.saveTssId(context, tssId)
                    TssStorage.savePuk(context, puk)

                    // 🔥 MOVE → UNINITIALIZED
                    api.updateTss(tssId, UpdateTssRequest(state = "UNINITIALIZED"))

                    // 🔥 SET ADMIN PIN
                    // 🔥 SET ADMIN PIN
                    api.setAdminPin(
                        tssId,
                        AdminPinRequest(
                            admin_puk = puk,
                            new_admin_pin = "1234567890"
                        )
                    )

// ✅ VERY IMPORTANT DELAY
                    delay(1000) // minimum 800–1500ms

// 🔥 ADMIN AUTH
                    val authResponse = api.adminAuth(
                        tssId,
                        AdminAuthRequest("1234567890")
                    )



                    if (!authResponse.isSuccessful) {
                        throw Exception("Admin auth failed")
                    }

                    delay(500)

                    // 🔥 INITIALIZE
                    api.updateTss(
                        tssId,
                        UpdateTssRequest(state = "INITIALIZED")
                    )

                    Log.d("FISKALY", "TSS Initialized Successfully")

                } else {

                    // ✅ EXISTING VALID TSS
                    // 🔹 EXISTING TSS FLOW
                    Log.d("FISKALY", "Using EXISTING TSS → $tssId")

                    val tss = api.getTssStatus(tssId)
                    Log.d("FISKALY", "Current State: ${tss.state}")

                    if (tss.state == "UNINITIALIZED") {

                        Log.d("FISKALY", "TSS not initialized → fixing setup")

                        // 🔥 MUST set PIN again
                        api.setAdminPin(
                            tssId,
                            AdminPinRequest(
                                admin_puk = puk!!,
                                new_admin_pin = "1234567890"
                            )
                        )

                        // 🔥 VERY IMPORTANT DELAY
                        delay(1000)

                        // 🔥 ADMIN AUTH
                        val authResponse = api.adminAuth(
                            tssId,
                            AdminAuthRequest("1234567890")
                        )

                        if (!authResponse.isSuccessful) {
                            throw Exception("Admin auth failed")
                        }

                        delay(500)

                        // 🔥 INITIALIZE
                        api.updateTss(
                            tssId,
                            UpdateTssRequest(state = "INITIALIZED")
                        )

                        Log.d("FISKALY", "TSS initialized from existing flow")




                    } else {

                        Log.d("FISKALY", "TSS already initialized")

                        val authResponse = api.adminAuth(
                            tssId,
                            AdminAuthRequest("1234567890")
                        )

                        if (!authResponse.isSuccessful) {
                            throw Exception("Admin auth failed")
                        }

                       }


                    // ✅ Start transaction always, after TSS is ready
                    val processData = """
{
  "items": [
    { "name": "Pizza", "price": 500, "quantity": 2 },
    { "name": "Coke", "price": 100, "quantity": 1 }
  ],
  "total": 1100
}
""".trimIndent()

                    // 🔥 CREATE CLIENT
                    val clientId = UUID.randomUUID().toString()

                    api.createClient(
                        tssId,
                        clientId,
                        ClientRequest(serial_number = clientId)
                    )

                    Log.d("FISKALY", "Client created: $clientId")

                    delay(500) // 🔥 MUST

                    // 🔥 START TRANSACTION
                    val txId = UUID.randomUUID().toString()

                    val startResponse = api.startTransaction(
                        tssId = tssId,
                        txId = txId,
                        txRevision = 1,
                        request = StartTransactionRequest(
                            client_id = clientId
                        )
                    )

                    Log.d("FISKALY", "Transaction started: $startResponse")

// 🔥 SMALL DELAY (important for stability)
                    delay(300)

// 🔥 FINISH REQUEST
                    val finishRequest = FinishTransactionRequest(
                        client_id = clientId,
                        schema = Schema(
                            standard_v1 = StandardV1(
                                receipt = Receipt(
                                    amounts_per_vat_rate = listOf(
                                        VatAmount(
                                            vat_rate = "NORMAL",
                                            amount = "10.00"
                                        )
                                    ),
                                    amounts_per_payment_type = listOf(
                                        PaymentAmount(
                                            payment_type = "CASH",
                                            amount = "10.00"
                                        )
                                    )
                                )
                            )
                        )
                    )

// 🔥 FINISH TRANSACTION
                    val finishResponse = api.finishTransaction(
                        tssId = tssId,
                        txId = txId, // ✅ SAME TX ID
                        txRevision = 2,
                        request = finishRequest
                    )

                    Log.d("FISKALY", "Transaction finished: $finishResponse")


                }

            } catch (e: HttpException) {

                val errorBody = e.response()?.errorBody()?.string()
                Log.e("FISKALY", "HTTP ERROR ${e.code()}")
                Log.e("FISKALY", "Error body: $errorBody")


                Log.e("FISKALY", "Error: ${e.message}", e)
            }
        }
    }
}