package com.it10x.foodappgstav7_31.fiskaly

import com.it10x.foodappgstav7_31.network.model.PaymentAmount
import com.it10x.foodappgstav7_31.network.model.VatAmount

interface FiskalyService {

    fun isEnabled(): Boolean

    suspend fun startTransaction(
        sessionId: String
    ): Pair<String, String>?   // txId + clientId

    suspend fun finishTransaction(
        txId: String,
        clientId: String,
        vatList: List<VatAmount>,
        paymentList: List<PaymentAmount>
    )


//    override suspend fun finish(
//        context: FiscalContext,
//        payments: List<PaymentInput>,
//        items: List<PosKotItemEntity>
//    ) {
//
//        val vatList = buildVat(items)
//        val paymentList = buildPayment(payments)
//
//        try {
//
//            repository.finishTransaction(
//                context.txId,
//                context.clientId,
//                vatList,
//                paymentList
//            )
//
//        } catch (e: Exception) {
//
//            // 🔥 SAVE FOR RETRY
//            val pending = PosFiscalPendingEntity(
//                id = UUID.randomUUID().toString(),
//                txId = context.txId,
//                clientId = context.clientId,
//                vatJson = Gson().toJson(vatList),
//                paymentJson = Gson().toJson(paymentList),
//                createdAt = System.currentTimeMillis()
//            )
//
//            fiscalPendingDao.insert(pending)
//
//            throw e   // keep original behavior
//        }
//    }

    suspend fun cancelTransaction(
        txId: String,
        clientId: String
    )
}