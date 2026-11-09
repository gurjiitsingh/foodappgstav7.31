package com.it10x.foodappgstav7_31.data.online.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local Room entity for products belonging to received online orders.
 *
 * Numeric Firestore values are stored as Double.
 * Modifiers are stored as JSON text in modifiersJson.
 */
@Entity(
    tableName = "online_order_products",
    indices = [
        Index(value = ["orderMasterId"])
    ]
)
data class OnlineReceivedOrderProductEntity(

    // =====================================================
    // CORE IDENTIFIERS
    // =====================================================

    @PrimaryKey
    val id: String,

    val productId: String = "",
    val orderMasterId: String,

    // =====================================================
    // PRODUCT DETAILS
    // =====================================================

    val categoryName: String = "",
    val name: String = "",
    val quantity: Int = 0,

    // =====================================================
    // PRICING / TAX
    // =====================================================

    val basePrice: Double? = null,
    val itemSubtotal: Double? = null,
    val taxRate: Double? = null,
    val taxType: String = "",
    val taxAmount: Double? = null,
    val taxTotal: Double? = null,

    val finalPrice: Double? = null,
    val finalTotal: Double? = null,

    // =====================================================
    // CATEGORY / NOTES
    // =====================================================

    val productCat: String = "",
    val note: String? = null,

    // =====================================================
    // MODIFIERS
    // =====================================================

    // JSON representation of the Firestore modifiers array.
    val modifiersJson: String? = null
)
