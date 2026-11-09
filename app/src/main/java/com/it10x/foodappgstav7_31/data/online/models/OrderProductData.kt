package com.it10x.foodappgstav7_31.data.online.models

data class OrderProductData(

    val id: String = "",
    val productId: String = "",
    val orderMasterId: String = "",

    val categoryName: String = "",
    val name: String = "",
    val quantity: Int = 0,

    val basePrice: Any? = null,
    val itemSubtotal: Any? = null,
    val taxRate: Any? = null,
    val taxType: String = "",
    val taxAmount: Any? = null,
    val taxTotal: Any? = null,

    val finalPrice: Any? = null,
    val finalTotal: Any? = null,

    val productCat: String = "",
    val note: String? = null,

    // Existing field - keep it
    val modifiersJson: String? = null,

    // Firestore stores modifiers as an ARRAY
    val modifiers: List<FirestoreModifierData> = emptyList()

) {
    fun priceDouble() = basePrice.toDoubleSafe()
    fun finalPriceDouble() = finalPrice.toDoubleSafe()
    fun finalTotalDouble() = finalTotal.toDoubleSafe()
}

data class FirestoreModifierData(
    val createdAt: String = "",
    val groupId: String = "",
    val id: String = "",
    val isDefault: Boolean = false,
    val name: String = "",
    val price: Any? = null,
    val priceMap: Map<String, Any?> = emptyMap(),
    val sortOrder: Int = 0,
    val status: String = "",
    val updatedAt: String = ""
)

fun Any?.toDoubleSafe(): Double {
    return when (this) {
        is Number -> this.toDouble()
        is String -> this.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
}