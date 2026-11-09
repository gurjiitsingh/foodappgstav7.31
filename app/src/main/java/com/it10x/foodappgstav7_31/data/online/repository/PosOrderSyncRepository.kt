package com.it10x.foodappgstav7_31.data.online.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.it10x.foodappgstav7_31.data.pos.dao.OrderMasterDao
import com.it10x.foodappgstav7_31.data.pos.dao.OrderProductDao
import com.it10x.foodappgstav7_31.data.pos.dao.OutletDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
import com.it10x.foodappgstav7_31.data.model.report.DailyTotals
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_31.data.pos.repository.POSPaymentRepository



class PosOrderSyncRepository(
    private val orderMasterDao: OrderMasterDao,
    private val orderProductDao: OrderProductDao,
    private val outletDao: OutletDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val paymentRepository: POSPaymentRepository
) {

    suspend fun syncPendingOrders() = withContext(Dispatchers.IO) {
      //  val dailyMap = mutableMapOf<String, Triple<Double, Double, Double>>()
        val dailyMap = mutableMapOf<String, DailyTotals>()
        //val monthlyMap = mutableMapOf<String, Triple<Double, Double, Double>>()
        val monthlyMap = mutableMapOf<String, DailyTotals>()
        val outlet = outletDao.getOutlet()
            ?: throw IllegalStateException("Outlet not configured")

        val ownerId = outlet.ownerId
        val outletId = outlet.outletId

        val pendingOrders = orderMasterDao.getPendingSyncOrders()

        if (pendingOrders.isEmpty()) {
            Log.d("ORDER_SYNC", "No pending orders to sync")
            return@withContext
        }

        val batch = firestore.batch()

        pendingOrders.forEach { order ->

            val orderRef = firestore.collection("orderMaster").document(order.id)
            val orderItems = orderProductDao.getByOrderIdSync(order.id)

            val now = order.createdAt
            val createdTimestamp = com.google.firebase.Timestamp(
                now / 1000,
                ((now % 1000) * 1_000_000).toInt()
            )

//           val date = Date(now)
//            val orderDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
//            val orderMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(date)
//            val orderYear = Calendar.getInstance().apply { time = date }.get(Calendar.YEAR)


            // Already saved when order was created
            val orderDate = order.orderDate

            val orderMonth = orderDate.substring(0, 7)

            val orderYear = orderDate.substring(0, 4).toInt()

            //Agrigate sale

            val sales = order.grandTotal ?: 0.0
            val discount = order.discountTotal ?: 0.0
            val tax = order.taxTotal ?: 0.0

            Log.d(
                "ORDER_TOTAL_DEBUG",
                """
    Order ID       = ${order.id}
    SrNo           = ${order.srno}
    itemTotal      = ${order.itemTotal}
    taxableAmount  = ${order.taxableAmount}
    taxTotal       = ${order.taxTotal}
    discountTotal  = ${order.discountTotal}
    grandTotal     = ${order.grandTotal}
    roundOff       = ${order.roundOff}
    dueAmount      = ${order.dueAmount}
    """.trimIndent()
            )

            val payments = paymentRepository.getPaymentsByOrderId(order.id)

            var cash = 0.0
            var upi = 0.0
            var card = 0.0
            var wallet = 0.0

            payments
                .filter { it.status == "SUCCESS" && !it.isVoided }
                .forEach { payment ->
                    when (payment.mode) {
                        "CASH" -> cash += payment.amount
                        "UPI" -> upi += payment.amount
                        "CARD" -> card += payment.amount
                        "WALLET" -> wallet += payment.amount
                    }
                }



            val credit = order.dueAmount ?: 0.0

            Log.d(
                "ORDER_TOTAL_DEBUG",
                """
    FINAL CALCULATION
    Order ID = ${order.id}
    Sales    = $sales
    Cash     = $cash
    UPI      = $upi
    Card     = $card
    Wallet   = $wallet
    Credit   = $credit
    Tax      = $tax
    """.trimIndent()
            )

            val totals = dailyMap.getOrPut(orderDate) { DailyTotals() }

            totals.sales += sales.round2()
            totals.discount += kotlin.math.abs(discount).round2()
            totals.tax += tax.round2()

            totals.cash += cash.round2()
            totals.upi += upi.round2()
            totals.card += card.round2()
            totals.wallet += wallet.round2()

            totals.credit += credit.round2()

// ✅ MONTHLY (FIXED POSITION)
            val monthTotals = monthlyMap.getOrPut(orderMonth) { DailyTotals() }

            monthTotals.sales += sales.round2()
            monthTotals.discount += kotlin.math.abs(discount).round2()
            monthTotals.tax += tax.round2()

            monthTotals.cash += cash.round2()
            monthTotals.upi += upi.round2()
            monthTotals.card += card.round2()
            monthTotals.wallet += wallet.round2()

            monthTotals.credit += credit.round2()

            val paymentType = when {
                cash > 0 && upi == 0.0 && card == 0.0 && wallet == 0.0 -> "CASH"
                upi > 0 && cash == 0.0 && card == 0.0 && wallet == 0.0 -> "UPI"
                card > 0 && cash == 0.0 && upi == 0.0 && wallet == 0.0 -> "CARD"
                wallet > 0 && cash == 0.0 && upi == 0.0 && card == 0.0 -> "WALLET"
                else -> "MIXED"
            }
            val totalQty = orderItems.sumOf { it.quantity }
            // -------- ORDER MASTER --------
            batch.set(orderRef, mapOf(
                "id" to order.id,
                "srno" to order.srno,
                "ownerId" to ownerId,
                "outletId" to outletId,
                "orderType" to order.orderType,
                "tableNo" to order.tableNo,
                "itemTotal" to order.itemTotal,
                "itemTax" to order.itemTax,
                "deliveryFee" to order.deliveryFee,
                "deliveryTax" to order.deliveryTax,
                "taxableAmount" to order.taxableAmount,
                "taxTotal" to order.taxTotal,
                "discountTotal" to order.discountTotal,
                "grandTotal" to order.grandTotal,
                "roundOff" to order.roundOff,
                "dueAmount" to credit,
                "totalItems" to totalQty,
                "paymentType" to paymentType,
                "cashAmount" to cash,
                "upiAmount" to upi,
                "cardAmount" to card,
                "walletAmount" to wallet,
                "paymentStatus" to order.paymentStatus,
                "orderStatus" to order.orderStatus,
                "source" to "POS",
                "createdAt" to createdTimestamp,
                "createdAtMillis" to now,
                "orderDate" to order.orderDate,
                "orderMonth" to orderMonth,
                "orderYear" to orderYear,
                "syncStatus" to "SYNCED"
            ))

            // -------- ORDER ITEMS --------
            orderItems.forEach { item ->
                val itemRef = firestore.collection("orderProducts").document(item.id)
                val itemDate = Date(item.createdAt)
                val itemOrderDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(itemDate)
                val itemOrderMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(itemDate)
                Log.d("FINAL_TRACE", "ADDING productId=${item}, name=${item.name}")
                batch.set(itemRef, mapOf(
                    "id" to item.id,
                    "orderMasterId" to order.id,
                    "productId" to item.productId,
                    "name" to item.name,
                    "categoryId" to item.categoryId,
                    "categoryName" to item.categoryName,
                    "quantity" to item.quantity,
                    "basePrice" to item.basePrice,
                    "itemSubtotal" to item.itemSubtotal,
                    "taxRate" to item.taxRate,
                    "taxType" to item.taxType,
                    "taxAmount" to item.taxAmountPerItem,
                    "taxTotal" to item.taxTotal,
                    "finalPrice" to item.finalPricePerItem,
                    "finalTotal" to item.finalTotal,
                    "createdAt" to com.google.firebase.Timestamp(
                        item.createdAt / 1000,
                        ((item.createdAt % 1000) * 1_000_000).toInt()
                    ),
                    "orderDate" to itemOrderDate,
                    "orderMonth" to itemOrderMonth
                ))
            }


            orderItems.forEach { item ->
                val itemRef = firestore.collection("orderItems").document(item.id)
                val itemDate = Date(item.createdAt)
                val itemOrderDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(itemDate)
                val itemOrderMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(itemDate)
                batch.set(itemRef, mapOf(
                    "id" to item.id,
                    "orderMasterId" to order.id,
                    "itemId" to item.productId,
                    "name" to item.name,
                    "categoryId" to item.categoryId,
                    "categoryName" to item.categoryName,
                    "quantity" to item.quantity,
                    "basePrice" to item.basePrice,
                    "itemSubtotal" to item.itemSubtotal,
                    "taxRate" to item.taxRate,
                    "taxType" to item.taxType,
                    "taxAmount" to item.taxAmountPerItem,
                    "taxTotal" to item.taxTotal,
                    "finalPrice" to item.finalPricePerItem,
                    "finalTotal" to item.finalTotal,
                    "createdAt" to com.google.firebase.Timestamp(
                        item.createdAt / 1000,
                        ((item.createdAt % 1000) * 1_000_000).toInt()
                    ),
                    "orderDate" to itemOrderDate,
                    "orderMonth" to itemOrderMonth
                ))
            }
        }

        dailyMap.forEach { (date, totals) ->

            val docRef = firestore
                .collection("dailyReports")
//                .document(ownerId)
//                .collection("main")
//                .document(outletId)
                //.collection("reports")
                .document(date)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate = sdf.parse(date)!!

            val reportTimestamp = com.google.firebase.Timestamp(
                parsedDate.time / 1000,
                ((parsedDate.time % 1000) * 1_000_000).toInt()
            )

            batch.set(
                docRef,
                mapOf(
                    "date" to date, // "2026-04-02" (for easy UI)
                    "dateTimestamp" to reportTimestamp, // 🔥 for queries
                    "totalSales" to FieldValue.increment(totals.sales),
                    "totalDiscount" to FieldValue.increment(totals.discount),
                    "totalTax" to FieldValue.increment(totals.tax),

                    "cashCollection" to FieldValue.increment(totals.cash),
                    "upiCollection" to FieldValue.increment(totals.upi),
                    "cardCollection" to FieldValue.increment(totals.card),
                    "walletCollection" to FieldValue.increment(totals.wallet),

                    "totalCredit" to FieldValue.increment(totals.credit),
                ),
                SetOptions.merge()
            )
        }

     //   ✅ ADD THIS (after dailyMap loop, before commit)


        monthlyMap.forEach { (month, totals) ->

            val docRef = firestore
                .collection("monthlyReports")
//                .document(ownerId)
//                .collection("outlets")
//                .document(outletId)
//                .collection("reports")
                .document(month)

            batch.set(
                docRef,
                mapOf(
                    "month" to month,
                    "totalSales" to FieldValue.increment(totals.sales),
                    "totalDiscount" to FieldValue.increment(totals.discount),
                    "totalTax" to FieldValue.increment(totals.tax),

                    "cashCollection" to FieldValue.increment(totals.cash),
                    "upiCollection" to FieldValue.increment(totals.upi),
                    "cardCollection" to FieldValue.increment(totals.card),
                    "walletCollection" to FieldValue.increment(totals.wallet),

                    "totalCredit" to FieldValue.increment(totals.credit),
                    "lastUpdated" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
        }


        try {
            batch.commit().await()
            Log.d("ORDER_SYNC", "Batch sync success (${pendingOrders.size} orders)")

            orderMasterDao.markOrdersSynced(
                ids = pendingOrders.map { it.id },
                time = System.currentTimeMillis()
            )



        } catch (e: Exception) {
            Log.e("ORDER_SYNC", "Batch sync failed: ${e.message}", e)
            throw e
        }

        updateOrderCounter(pendingOrders)
    }
    fun Double.round2(): Double {
        return String.format("%.2f", this).toDouble()
    }

    private suspend fun updateOrderCounter(
        pendingOrders: List<PosOrderMasterEntity>
    ) {

        // =====================================================
        // ANDROID DEVICE CODE
        // =====================================================

        val deviceCode = "P1"


        // =====================================================
        // FINANCIAL YEAR
        // =====================================================

        val financialYear = getFinancialYearCode()


        // =====================================================
        // FIRESTORE DOCUMENT ID
        //
        // Example:
        // P1_2627
        // =====================================================

        val docId =
            "${deviceCode}_${financialYear}"


        val ref = firestore
            .collection("orderCounters")
            .document(docId)


        // =====================================================
        // FIND HIGHEST LOCAL SERIAL
        //
        // Supports:
        // P1-2627-501
        // P1-2627-502
        // 501
        // =====================================================

        val localSrNo = pendingOrders
            .maxOfOrNull { order ->

                order.srno
                    .substringAfterLast("-")
                    .toLongOrNull() ?: 0L

            } ?: 0L


        // =====================================================
        // NOTHING TO UPLOAD
        // =====================================================

        if (localSrNo <= 0L) {

            Log.d(
                "ORDER_COUNTER",
                "No local serial found"
            )

            return
        }


        // =====================================================
        // READ FIRESTORE COUNTER
        // =====================================================

        val snapshot =
            ref.get().await()


        val firestoreSrNo =
            if (snapshot.exists()) {

                snapshot
                    .getLong("invoiceSerialNo")
                    ?: 0L

            } else {
                0L
            }


        Log.d(
            "ORDER_COUNTER",
            """
        Device       = $deviceCode
        FinancialYear= $financialYear
        Document     = $docId
        Firestore    = $firestoreSrNo
        Local        = $localSrNo
        """.trimIndent()
        )


        // =====================================================
        // ONLY MOVE FIRESTORE FORWARD
        // =====================================================

        if (localSrNo > firestoreSrNo) {

            ref.set(
                mapOf(
                    "invoiceSerialNo" to localSrNo,
                    "deviceCode" to deviceCode,
                    "financialYear" to financialYear,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).await()


            Log.d(
                "ORDER_COUNTER",
                "Updated Firestore $docId = $localSrNo"
            )

        } else {

            Log.d(
                "ORDER_COUNTER",
                "No update required"
            )
        }
    }


    private fun getFinancialYearCode(): String {

        val calendar = java.util.Calendar.getInstance()

        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1

        val startYear =
            if (month >= 4) {
                year
            } else {
                year - 1
            }

        val endYear = startYear + 1

        return "${startYear.toString().takeLast(2)}" +
                endYear.toString().takeLast(2)
    }


    }


