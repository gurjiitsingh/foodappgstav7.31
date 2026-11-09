package com.it10x.foodappgstav7_31.printer

import com.it10x.foodappgstav7_31.data.print.OutletInfo

/**
 * Master print model
 * Used by BOTH:
 *  - POS Local Orders
 *  - Firestore Online Orders
 *
 * ⚠️ DELIVERY FIELDS USE d* EVERYWHERE (STANDARD)
 */
data class PrintOrder(

    // ---------- CORE ----------
    val orderNo: String,
    val customerName: String,
    val dateTime: String,

    // ---------- ORDER TYPE ----------
    val orderType: String? = null,   // DINE_IN | TAKEAWAY | DELIVERY | ONLINE
    val tableNo: String? = null,
    val paymentMode: String,
    // ---------- DELIVERY SNAPSHOT (d*) ----------
    val customerPhone: String? = null,
    val dAddressLine1: String? = null,
    val dAddressLine2: String? = null,
    val dCity: String? = null,
    val dState: String? = null,
    val dZipcode: String? = null,
    val dLandmark: String? = null,

    // ---------- ITEMS ----------
    val items: List<PrintItem>,

// ---------- TOTALS ----------
    val itemTotal: Double = 0.0,

    val itemTax: Double = 0.0,
    val deliveryTax: Double = 0.0,
    val tax: Double = 0.0,          // itemTax + deliveryTax

    val deliveryFee: Double = 0.0,
    val loyaltyDiscountPaise: Double = 0.0,
    val discount: Double = 0.0,

// GST information
    val gstRate: Double = 0.0,
    val taxMode: String = "PER_ITEM",
    val taxableAmount: Double = 0.0,
    val roundOff: Double = 0.0,
    val grandTotal: Double,

)

/**
 * Line item printed on receipt
 */
data class PrintItem(
    val name: String,
    val quantity: Int,
    val price: Double = 0.0,
    val subtotal: Double = 0.0,
    val note: String?,
    val modifiersJson: String?,
    val taxRate: Double? = null,
    val taxType: String,

)
