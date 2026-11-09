package com.it10x.foodappgstav7_31.data.model.report

data class DailyTotals(
    var sales: Double = 0.0,
    var discount: Double = 0.0,
    var tax: Double = 0.0,

    var cash: Double = 0.0,
    var upi: Double = 0.0,
    var card: Double = 0.0,
    var wallet: Double = 0.0,

    var credit: Double = 0.0
)