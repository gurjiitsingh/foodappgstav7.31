package com.it10x.foodappgstav7_31.ui.dayclosing

data class DayClosingUiState(

    val businessDate: String = "",

    val openedBy: String = "",

    val openedAt: Long = 0L,


    val openingCash: Double = 0.0,

    val expectedCash: Double = 0.0,

    val actualCash: String = "",

    val cashHandedOver: String = "",

    val notes: String = "",


    val totalOrders: Int = 0,

    val totalSales: Double = 0.0,

    val totalDiscount: Double = 0.0,

    val totalTax: Double = 0.0,

    val totalRefund: Double = 0.0,

    val complimentarySales: Double = 0.0,


    val cashSales: Double = 0.0,

    val cardSales: Double = 0.0,

    val upiSales: Double = 0.0,

    val walletSales: Double = 0.0,

    val creditSales: Double = 0.0,


    // Add this
    val errorMessage: String? = null
)