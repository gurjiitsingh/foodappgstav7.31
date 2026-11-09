package com.it10x.foodappgstav7_31.data.pos.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cart",
    indices = [
        // Allow same product multiple times IF note or modifiers differ
        Index(
            value = ["productId", "tableId", "note", "modifiersJson"],
            unique = true
        )
    ]
)
data class PosCartEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val productId: String,
    val productMode: String,
    val currentStock: Double = 0.0,
    val name: String,
    val categoryId: String,
    val categoryName: String,
    val parentId: String?,
    val isVariant: Boolean,


    val basePrice: Double,
    val discountEligible: Boolean = true,
    val finalPrice: Double = 0.0,
    val modifierTotal: Double,
    val quantity: Int,

    val taxCategory: String,
    val taxRate: Double,
    val taxType: String,          // inclusive | exclusive
    val taxRate1: Double? = null,
    val taxType1: String? = null,

    // 🔑 POS SESSION
    val sessionId: String,

    // 🪑 Only for DINE_IN
    val tableId: String?,
    val tableName: String?,
    val createdById : String = "",
    val createdByName: String = "",
    // 📝 Free text kitchen instruction
    val note: String = "",

    // ⭐ Structured modifiers (future support: size, toppings, addons)
    // Store as JSON string
    val modifiersJson: String = "",

    // 🚀 Kitchen workflow
    val sentToKitchen: Boolean = false,
    var kitchenPrintReq: Boolean = false,
    var printStatus: String = "PENDING",

    val createdAt: Long = System.currentTimeMillis()
)
