package com.it10x.foodappgstav7_31.data.pos.models

data class CartModifier(
    val groupId: String,
    val groupName: String,
    val items: List<CartModifierItem>
)

data class CartModifierItem(
    val itemId: String,
    val name: String,
    val price: Double
)