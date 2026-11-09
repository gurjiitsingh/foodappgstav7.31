package com.it10x.foodappgstav7_31.data.pos.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.it10x.foodappgstav7_31.data.pos.dao.OrderCounterDao
import com.it10x.foodappgstav7_31.data.pos.entities.OrderCounterEntity

class InvoiceCounterRepository(
    private val firestore: FirebaseFirestore,
    private val orderCounterDao: OrderCounterDao
) {


    private val deviceCode = "P1"


    /**
     * Restore local counter after reinstall
     */
    suspend fun syncCounterFromFirestore() {

        val fy = getFinancialYearCode()


        val snapshot = firestore
            .collection("invoiceCounter")
            .document(deviceCode)
            .get()
            .await()


        if (!snapshot.exists()) {

            // First time setup
            firestore
                .collection("invoiceCounter")
                .document(deviceCode)
                .set(
                    mapOf(
                        "lastSequence" to 0L,
                        "financialYear" to fy,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()


            orderCounterDao.save(
                OrderCounterEntity(
                    invoiceSerialNo = 0L,
                    updatedAt = System.currentTimeMillis()
                )
            )


            return
        }



        val firestoreFY =
            snapshot.getString("financialYear") ?: fy


        val firestoreSequence =
            snapshot.getLong("lastSequence") ?: 0L



        if (firestoreFY == fy) {

            val localCounter =
                orderCounterDao.getCounter()?.invoiceSerialNo ?: 0L

            val finalCounter = maxOf(
                localCounter,
                firestoreSequence
            )

            orderCounterDao.save(
                OrderCounterEntity(
                    invoiceSerialNo = finalCounter,
                    updatedAt = System.currentTimeMillis()
                )
            )

        }

    }




    private fun getFinancialYearCode(): String {

        val calendar = java.util.Calendar.getInstance()

        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1


        return if (month >= 4) {

            "${year % 100}${(year + 1) % 100}"

        } else {

            "${(year - 1) % 100}${year % 100}"

        }
    }

}