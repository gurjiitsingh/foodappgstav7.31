package com.it10x.foodappgstav7_31.data.pos.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pos_kot_items",
    indices = [
        Index(value = ["kotBatchId"]),
        Index(value = ["sessionId"]),   // ✅ MOST IMPORTANT
        Index(value = ["tableNo"]),
        Index(value = ["productId"]),
        Index(value = ["syncedToCloud"]), // ✅ fast sync lookup
        Index(value = ["source"])         // ✅ filter customer/waiter
    ]
)
data class PosKotItemEntity(

    @PrimaryKey
    val id: String,
    val kotNumber: String,
    val categoryName: String,
    val productMode: String,
    val currentStock: Double = 0.0,
    val sessionId: String?,             // groups table visit
    val kotBatchId: String,

    val tableNo: String?,               // UI / print only
    val tableName: String?,

    val productId: String,
    val name: String,
    val categoryId: String,

    val createdById: String = "",
    val createdByName: String = "",

    val parentId: String?,
    val isVariant: Boolean,
    val discountEligible: Boolean = true,
    val basePrice: Double,
    val finalPrice: Double = 0.0,
    val modifierTotal: Double,
    val quantity: Int,

    val taxCategory: String,
    val taxRate: Double,
    val taxType: String,          // inclusive | exclusive
    val taxRate1: Double? = null,
    val taxType1: String? = null,


    val status: String,                 // PENDING / DONE /DELETED
    val note: String = "",
    val modifiersJson: String = "",

    val kitchenPrintReq: Boolean = true,
    val kitchenPrinted: Boolean = false,
    val createdAt: Long,

    // ================= NEW FIELDS =================

    val source: String = "POS",         // POS / CUSTOMER / WAITER

    val syncedToCloud: Boolean = false, // POS → Firestore mirror
    val syncedFromCloud: Boolean = false // Firestore → POS imported
)
