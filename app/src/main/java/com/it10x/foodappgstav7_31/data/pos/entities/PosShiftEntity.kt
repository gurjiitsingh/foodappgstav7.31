package com.it10x.foodappgstav7_31.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pos_shift")
data class PosShiftEntity(

    @PrimaryKey
    val id: String,

    val businessDate: String,

    val shiftNumber: Int,

    val openedAt: Long,
    val closedAt: Long?,

    val openedById: String,
    val openedByName: String,

    val closedById: String?,
    val closedByName: String?,

    val openingCash: Double = 0.0,

    val expectedCash: Double = 0.0,

    val actualCash: Double?,

    val cashHandedOver: Double?,

    val cashDifference: Double?,

    val status: String = "OPEN",

    val notes: String = "",

    val createdAt: Long
)