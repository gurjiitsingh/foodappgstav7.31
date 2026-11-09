package com.it10x.foodappgstav7_31.network.fiskaly

import android.util.Log
import com.it10x.foodappgstav7_31.network.model.StartTransactionRequest
import com.it10x.foodappgstav7_31.network.model.TransactionResponse



//class FiskalyTransactionManager {
//
//    suspend fun startTransaction(
//        api: FiskalyApi,
//        clientId: String,
//        amount: Double = 0.0
//    ): TransactionResponse {
//
//        val processData = api.buildProcessData(amount)
//
//        val response = api.startTransaction(
//            StartTransactionRequest(
//                client_id = clientId,
//                process_data = processData
//            )
//        )
//
//        Log.d("FISKALY", "Transaction started: ${response.transaction_number}")
//
//        return response
//    }
//}