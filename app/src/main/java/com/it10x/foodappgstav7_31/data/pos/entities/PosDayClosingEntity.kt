package com.it10x.foodappgstav7_31.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "pos_day_closing")
data class PosDayClosingEntity(

    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val businessDate: String,

    val openedAt: Long,
    val closedAt: Long,

    val openedById: String,
    val openedByName: String,

    val closedById: String,
    val closedByName: String,

    val openingCash: Double,

    val expectedCash: Double,
    val actualCash: Double,
    val cashHandedOver: Double,
    val cashDifference: Double,

    val totalSales: Double,
    val totalRefund: Double,

    val totalDiscount: Double,
    val totalTax: Double,

    val cashSales: Double,
    val cardSales: Double,
    val upiSales: Double,
    val walletSales: Double,

    val creditSales: Double,

    val complimentarySales: Double,

    val totalOrders: Int,

    val syncStatus: String,

    val createdAt: Long
)