


package com.it10x.foodappgstav7_31.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_serial_map")
data class OrderSerialMapEntity(

    @PrimaryKey
    val mapKey: String,          // tableId

    val orderId: String?,        // null until payment

    val orderSerialNo: Long,     // 101

    val srno: String,            // P1-2627-101

    val createdAt: Long = System.currentTimeMillis()
)
//data class OrderSerialMapEntity(
//
//    @PrimaryKey
//    val mapKey: String,
//
//    val orderSerialNo: Long,
//
//    val createdAt: Long = System.currentTimeMillis()
//)
