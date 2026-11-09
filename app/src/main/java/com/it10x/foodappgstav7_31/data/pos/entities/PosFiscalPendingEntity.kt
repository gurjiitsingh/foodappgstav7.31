package com.it10x.foodappgstav7_31.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pos_fiscal_pending")
data class PosFiscalPendingEntity(

    @PrimaryKey
    val id: String,

    val txId: String,
    val clientId: String,

    val vatJson: String,
    val paymentJson: String,

    val createdAt: Long,
    val retryCount: Int = 0
)