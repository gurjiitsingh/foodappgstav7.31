package com.it10x.foodappgstav7_31.data.online.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.data.pos.AppDatabase
import com.it10x.foodappgstav7_31.data.pos.entities.OrderCounterEntity
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class OrderCounterSyncRepository(
    private val db: AppDatabase,
    private val firestore: FirebaseFirestore
) {

    // =====================================================
    // ANDROID POS DEVICE CODE
    // =====================================================

    private val deviceCode = "P1"


    // =====================================================
    // FINANCIAL YEAR
    // April -> March
    //
    // April 2026 -> 2627
    // March 2027 -> 2627
    // April 2027 -> 2728
    // =====================================================

    private fun getFinancialYearCode(): String {

        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)

        val month =
            calendar.get(Calendar.MONTH) + 1

        val startYear =
            if (month >= 4) {
                year
            } else {
                year - 1
            }

        val endYear =
            startYear + 1

        return "${startYear.toString().takeLast(2)}" +
                endYear.toString().takeLast(2)
    }


    // =====================================================
    // DOWNLOAD / SYNC ORDER COUNTER
    // =====================================================

    suspend fun syncLastOrderSerialNo() {

        // =================================================
        // FINANCIAL YEAR
        // =================================================

        val financialYear =
            getFinancialYearCode()


        // =================================================
        // FIRESTORE DOCUMENT
        //
        // Example:
        // orderCounters/P1_2627
        // =================================================

        val docId =
            "${deviceCode}_${financialYear}"


        val ref = firestore
            .collection("orderCounters")
            .document(docId)


        // =================================================
        // FETCH FIRESTORE COUNTER
        // =================================================

        val snapshot =
            ref.get().await()


        // =================================================
        // DOCUMENT DOES NOT EXIST
        // =================================================

        if (!snapshot.exists()) {

            Log.d(
                "ORDER_COUNTER",
                "No counter found in Firestore: $docId"
            )

            return
        }


        // =================================================
        // FIRESTORE SERIAL
        // =================================================

        val firestoreSerial =
            snapshot.getLong("invoiceSerialNo")
                ?: 0L


        // =================================================
        // LOCAL SERIAL
        // =================================================

        val localCounter =
            db.orderCounterDao().getCounter()


        val localSerial =
            localCounter?.invoiceSerialNo
                ?: 0L


        Log.d(
            "ORDER_COUNTER",
            """
            Device        = $deviceCode
            FinancialYear = $financialYear
            Document      = $docId
            Local         = $localSerial
            Firestore     = $firestoreSerial
            """.trimIndent()
        )


        // =================================================
        // ONLY MOVE LOCAL COUNTER FORWARD
        // =================================================

        if (firestoreSerial > localSerial) {

            val counter =
                OrderCounterEntity(
                    invoiceSerialNo = firestoreSerial,
                    updatedAt = System.currentTimeMillis()
                )


            db.orderCounterDao().save(counter)


            Log.d(
                "ORDER_COUNTER",
                "LOCAL COUNTER UPDATED = $firestoreSerial"
            )

        } else {

            Log.d(
                "ORDER_COUNTER",
                "LOCAL COUNTER ALREADY >= FIRESTORE"
            )
        }
    }
}