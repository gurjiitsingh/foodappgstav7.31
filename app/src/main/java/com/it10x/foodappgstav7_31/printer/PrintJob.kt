package com.it10x.foodappgstav7_31.printer

import com.it10x.foodappgstav7_31.ui.sales.SalesUiState

sealed class PrintJob {


//    data class Billing(
//        val data: PrintOrder,
//        val outlet: OutletInfo,
//        val printMillis: Long = System.currentTimeMillis()
//    ) : PrintJob()
    data class SalesReport(
        val state: SalesUiState,
        val printMillis: Long = System.currentTimeMillis()
    ) : PrintJob()

    data class CategoryWiseSalesReport(
        val categorySales: Map<String, Pair<Int, Double>>,
        val fromMillis: Long,
        val toMillis: Long,
        val printMillis: Long = System.currentTimeMillis()
    ) : PrintJob()
    data class ProductSummary(
        val product: String,
        val qty: Int,
        val amount: Double,

        val fromMillis: Long,
        val toMillis: Long,
        val printMillis: Long = System.currentTimeMillis()
    ) : PrintJob()

    data class SingleCategoryDetail(
        val category: String,
        val items: Map<String, Pair<Int, Double>>,
        val fromMillis: Long,
        val toMillis: Long,
        val printMillis: Long = System.currentTimeMillis()
    ) : PrintJob()


//    data class SingleCategorySummary(
//        val category: String,
//        val qty: Int,
//        val amount: Double,
//        val fromMillis: Long,
//        val toMillis: Long,
//        val printMillis: Long = System.currentTimeMillis()
//    ) : PrintJob()


    data class CategoryProductReport(
        val category: String,
        val items: List<com.it10x.foodappgstav7_31.ui.reports.model.ProductReportItem>,
        val fromMillis: Long,
        val toMillis: Long,
        val printMillis: Long = System.currentTimeMillis()
    ) : PrintJob()


    data class CategorySummary(
        val category: String,
        val qty: Int,
        val amount: Double,
        val fromMillis: Long,
        val toMillis: Long,
        val printMillis: Long = System.currentTimeMillis()
    ) : PrintJob()


    data class TotalSalesReport(
        val beforeDiscount: Double,
        val discount: Double,
        val afterDiscount: Double,
        val tax: Double,
        val fromMillis: Long,
        val toMillis: Long,
        val printMillis: Long = System.currentTimeMillis()
    ) : PrintJob()

}