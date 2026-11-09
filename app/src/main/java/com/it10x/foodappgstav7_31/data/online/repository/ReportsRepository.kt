package com.it10x.foodappgstav7_31.data.online.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.data.online.models.CategorySaleData
import com.it10x.foodappgstav7_31.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_31.data.online.models.OrderProductData
import com.it10x.foodappgstav7_31.data.online.models.TotalSalesReportResult
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportsRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getCategorySalesByDate(
        categoryId: String,
        startMillis: Long,
        endMillis: Long
    ): CategorySaleData? {

        return try {

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val startDate = sdf.format(Date(startMillis))
            val endDate = sdf.format(Date(endMillis))

            val snapshot = db.collection("orderProducts")
                .whereEqualTo("categoryId", categoryId) // ✅ USE ID
                .whereGreaterThanOrEqualTo("orderDate", startDate)
                .whereLessThanOrEqualTo("orderDate", endDate)
                .get()
                .await()

            val items = snapshot.documents.mapNotNull {
                it.toObject(OrderProductData::class.java)
            }

            val qty = items.sumOf { it.quantity }
            val sales = items.sumOf { it.finalTotalDouble() }

            CategorySaleData(
                categoryName = items.firstOrNull()?.categoryName ?: "", // ✅ get name safely
                totalQty = qty,
                totalSales = sales
            )

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }





    suspend fun getTotalSalesReport(
        startMillis: Long,
        endMillis: Long
    ): TotalSalesReportResult {


        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val startDate = sdf.format(Date(startMillis))
        val endDate = sdf.format(Date(endMillis))

        android.util.Log.d("REPORT_DEBUG", "START = $startDate | END = $endDate")

        val snapshot = db.collection("orderMaster")
            .whereGreaterThanOrEqualTo("orderDate", startDate)
            .whereLessThanOrEqualTo("orderDate", endDate)
            .get()
            .await()

        val orders = snapshot.documents.mapNotNull {
            it.toObject(OrderMasterData::class.java)
        }

        android.util.Log.d("REPORT_DEBUG", "FILTERED SIZE = ${orders.size}")

        val totalSales = orders.sumOf { it.grandTotal ?: 0.0 }
        val totalDiscount = orders.sumOf { it.discountTotal ?: 0.0 }
        val totalTax = orders.sumOf { it.taxTotal ?: 0.0 }

        return TotalSalesReportResult(
            totalSales = totalSales,
            totalDiscount = totalDiscount,
            totalTax = totalTax
        )
    }



    suspend fun getProductSalesByDate(
        productId: String,
        startMillis: Long,
        endMillis: Long
    ): CategorySaleData? {

        return try {

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val startDate = sdf.format(Date(startMillis))
            val endDate = sdf.format(Date(endMillis))


            val snapshot = db.collection("orderProducts")
                .whereEqualTo("productId", productId)   // ✅ CORRECT NOW
                .whereGreaterThanOrEqualTo("orderDate", startDate)
                .whereLessThanOrEqualTo("orderDate", endDate)
                .get()
                .await()

            val items = snapshot.documents.mapNotNull {
                it.toObject(OrderProductData::class.java)
            }



            val qty = items.sumOf { it.quantity }
            val sales = items.sumOf { it.finalTotalDouble() }

            CategorySaleData(
                categoryName = productId, // you can map name separately
                totalQty = qty,
                totalSales = sales
            )

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    suspend fun getCategoryProductReport(
        categoryId: String,
        startMillis: Long,
        endMillis: Long
    ): List<ProductSaleDataInCat> {

        return try {

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val startDate = sdf.format(Date(startMillis))
            val endDate = sdf.format(Date(endMillis))

            val snapshot = db.collection("orderProducts")
                .whereEqualTo("categoryId", categoryId)
                .whereGreaterThanOrEqualTo("orderDate", startDate)
                .whereLessThanOrEqualTo("orderDate", endDate)
                .get()
                .await()

            val items = snapshot.documents.mapNotNull {
                it.toObject(OrderProductData::class.java)
            }

            // 🔥 GROUPING
            val grouped = items.groupBy { it.productId }

            grouped.map { (_, list) ->

                val name = list.first().name

                val qty = list.sumOf { it.quantity }
                val total = list.sumOf { it.finalTotalDouble() }

                ProductSaleDataInCat(
                    productId = list.first().id,   // ✅ FIXED
                    productName = name,
                    totalQty = qty,
                    totalSales = total
                )
            }.sortedByDescending { it.totalSales }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }



}

data class ProductSaleData(
    val productName: String,
    val totalQty: Int,
    val totalSales: Double
)


data class ProductSaleDataInCat(
    val productId: String,
    val productName: String,
    val totalQty: Int,
    val totalSales: Double
)