package com.it10x.foodappgstav7_31.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(

    @PrimaryKey
    val id: String,

    val name: String,

    val purchaseUnit: String,

    val consumptionUnit: String,

    val conversionFactor: Double,

    val currentStock: Double
)