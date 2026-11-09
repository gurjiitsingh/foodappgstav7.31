package com.it10x.foodappgstav7_31.data.pos.repository

import android.util.Log
import com.it10x.foodappgstav7_31.data.pos.dao.PosOrderPaymentDao
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderPaymentEntity

class POSPaymentRepository(
    private val paymentDao: PosOrderPaymentDao
) {

    companion object {
        private const val TAG = "POS_PAYMENT_DEBUG"
    }

    // -----------------------------------------------------
    // INSERT PAYMENTS
    // -----------------------------------------------------
    suspend fun insertPayments(
        payments: List<PosOrderPaymentEntity>
    ) {
        Log.d(TAG, "========== INSERT PAYMENTS START ==========")
        Log.d(TAG, "Payment records received: ${payments.size}")

        payments.forEachIndexed { index, payment ->
            Log.d(
                TAG,
                """
                PAYMENT[$index]
                id=${payment.id}
                orderId=${payment.orderId}
                ownerId=${payment.ownerId}
                outletId=${payment.outletId}
                amount=${payment.amount}
                mode=${payment.mode}
                provider=${payment.provider}
                method=${payment.method}
                status=${payment.status}
                deviceId=${payment.deviceId}
                createdAt=${payment.createdAt}
                businessDate=${payment.businessDate}
                syncStatus=${payment.syncStatus}
                isVoided=${payment.isVoided}
                """.trimIndent()
            )
        }

        paymentDao.insertAll(payments)

        Log.d(TAG, "Insert completed")

        payments.map { it.orderId }.distinct().forEach { orderId ->
            val savedPayments = paymentDao.getPaymentsByOrderId(orderId)

            Log.d(TAG, "---------- SAVED PAYMENTS ----------")
            Log.d(TAG, "orderId=$orderId")
            Log.d(TAG, "Saved record count=${savedPayments.size}")

            savedPayments.forEachIndexed { index, payment ->
                Log.d(
                    TAG,
                    "SAVED[$index]: id=${payment.id}, " +
                            "orderId=${payment.orderId}, " +
                            "mode=${payment.mode}, " +
                            "amount=${payment.amount}, " +
                            "status=${payment.status}, " +
                            "isVoided=${payment.isVoided}, " +
                            "syncStatus=${payment.syncStatus}"
                )
            }

            val totalPaid = paymentDao.getTotalPaidForOrder(orderId)

            Log.d(TAG, "SUCCESS total for orderId=$orderId: $totalPaid")
            Log.d(TAG, "========== INSERT PAYMENTS END ==========")
        }
    }

    // -----------------------------------------------------
    // CALCULATIONS
    // -----------------------------------------------------
    suspend fun getTotalPaid(orderId: String): Double {
        val total = paymentDao.getTotalPaidForOrder(orderId)
        Log.d(TAG, "getTotalPaid: orderId=$orderId total=$total")
        return total
    }

    // -----------------------------------------------------
    // VOID / REVERSAL
    // -----------------------------------------------------
    suspend fun voidPayment(paymentId: String) {
        Log.d(TAG, "Voiding payment: paymentId=$paymentId")
        paymentDao.voidPayment(paymentId)
        Log.d(TAG, "Void request completed: paymentId=$paymentId")
    }

    // -----------------------------------------------------
    // SYNC
    // -----------------------------------------------------
    suspend fun getPendingSync(): List<PosOrderPaymentEntity> {
        val pending = paymentDao.getPendingSync()
        Log.d(TAG, "Pending payment sync count=${pending.size}")
        return pending
    }

    suspend fun markSynced(paymentId: String, time: Long) {
        paymentDao.markSynced(paymentId, time)
        Log.d(TAG, "Payment marked SYNCED: id=$paymentId time=$time")
    }

    suspend fun getPaymentsByOrderId(
        orderId: String
    ): List<PosOrderPaymentEntity> {
        val payments = paymentDao.getPaymentsByOrderId(orderId)
        Log.d(
            TAG,
            "getPaymentsByOrderId: orderId=$orderId count=${payments.size}"
        )
        return payments
    }
}
