package com.it10x.foodappgstav7_31.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "modifier_items")
data class ModifierItemEntity(

    @PrimaryKey
    val id: String,

    val name: String,

    val groupId: String,        // FK → modifier_groups

    val price: Double,

    val isDefault: Boolean = false,

    val sortOrder: Int = 0,

    val status: String
)