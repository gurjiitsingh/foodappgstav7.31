package com.it10x.foodappgstav7_31.data.online.sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(

    @PrimaryKey
    val id: String,

    val type: String, // TABLE_UPDATE, TABLE_CLEAR
    val tableId: String,

    val status: String, // PENDING, DONE
    val createdAt: Long
)