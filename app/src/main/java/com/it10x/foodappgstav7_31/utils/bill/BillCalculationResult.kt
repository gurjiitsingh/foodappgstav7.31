package com.it10x.foodappgstav7_31.utils.bill

data class BillCalculationResult(

    val itemSubtotalPaise: Long,

    val exclusiveTaxPaise: Long,

    val inclusiveTaxPaise: Long,

    val totalTaxPaise: Long,

    val discountPaise: Long,
    val loyaltyDiscountPaise: Long,
    val deliveryFeePaise: Long,

    val deliveryTaxPaise: Long,
    val taxableAmountPaise: Long,
    val roundOffPaise: Long,
    val grandTotalPaise: Long
)