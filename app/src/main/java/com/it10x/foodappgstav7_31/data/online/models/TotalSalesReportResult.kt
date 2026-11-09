package com.it10x.foodappgstav7_31.data.online.models

data class TotalSalesReportResult(
    val totalSales: Double = 0.0,      // after discount
    val totalDiscount: Double = 0.0,
    val totalTax: Double = 0.0
)