package com.it10x.foodappgstav7_31.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pos_business_day")
data class PosBusinessDayEntity(

    // Always "CURRENT"
    @PrimaryKey
    val id: String = "CURRENT",

    // Example: "2026-07-22"
    val businessDate: String,

    // Business day opened
    val openedAt: Long,

    val openedById: String,

    val openedByName: String,

    // Cash in drawer when day opened
    val openingCash: Double = 0.0,

    // Closing status
    val isClosed: Boolean = false,

    // Closing information
    val closedAt: Long? = null,

    val closedById: String? = null,

    val closedByName: String? = null,

    // OPEN / CLOSED
    val status: String = "OPEN",

    val updatedAt: Long = System.currentTimeMillis()
)