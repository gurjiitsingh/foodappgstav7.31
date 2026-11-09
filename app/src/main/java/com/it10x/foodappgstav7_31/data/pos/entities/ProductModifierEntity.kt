package com.it10x.foodappgstav7_31.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_modifiers")
data class ProductModifierEntity(

    @PrimaryKey
    val id: String,

    val productId: String,      // pizza id
    val groupId: String,        // modifier group id

    val sortOrder: Int = 0
)