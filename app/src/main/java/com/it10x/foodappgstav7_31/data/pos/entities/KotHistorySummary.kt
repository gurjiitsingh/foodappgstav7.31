package com.it10x.foodappgstav7_31.data.pos.entities

import androidx.room.ColumnInfo

/**
 * Projection used for the KOT History list.
 *
 * This is NOT a Room Entity.
 * It represents one KOT (one batch), not one item.
 */
data class KotHistorySummary(

    @ColumnInfo(name = "batchId")
    val batchId: String,

    @ColumnInfo(name = "kotNumber")
    val kotNumber: String,

    @ColumnInfo(name = "createdAt")
    val createdAt: Long,

    @ColumnInfo(name = "tableNo")
    val tableNo: String,

    @ColumnInfo(name = "tableName")
    val tableName: String,

    @ColumnInfo(name = "createdByName")
    val createdByName: String,

    @ColumnInfo(name = "orderType")
    val orderType: String,

    @ColumnInfo(name = "source")
    val source: String,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "totalLines")
    val totalLines: Int,

    @ColumnInfo(name = "totalQty")
    val totalQty: Int,

    @ColumnInfo(name = "activeCount")
    val activeCount: Int,

    @ColumnInfo(name = "paidCount")
    val paidCount: Int,

    @ColumnInfo(name = "deletedCount")
    val deletedCount: Int
)