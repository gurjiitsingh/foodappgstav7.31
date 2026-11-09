package com.it10x.foodappgstav7_31.data.pos.entities


import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity(tableName = "order_counter")
data class OrderCounterEntity(

    @PrimaryKey
    val id: Int = 1,

    val invoiceSerialNo: Long = 0L,

    val updatedAt: Long = System.currentTimeMillis()
)

//@Entity(tableName = "order_counter")
//data class OrderCounterEntity(
//
//    @PrimaryKey
//    val id: String = "main",
//
//    // Customer bill number
//    val invoiceSerialNo: Long = 0L,
//
//    // Kitchen order ticket number
//    val kotSerialNo: Long = 0L,
//
//    // Takeaway token number (future)
//    val tokenSerialNo: Long = 0L,
//
//    val updatedAt: Long = System.currentTimeMillis()
//)

//@Entity(tableName = "order_counter")
//data class OrderCounterEntity(
//
//    @PrimaryKey
//    val id: Int = 1,
//
//    val orderSerialNo: Long = 0L,
//
//    val updatedAt: Long = 0L
//)
//
//@Entity(tableName = "order_counter")
//data class OrderCounterEntity(
//
//    @PrimaryKey
//    val id: String = "main",
//
//    val orderSerialNo: Long = 0,
//    val invoiceSerialNo: Long = 0,
//    val kotSerialNo: Long = 0,
//    val tokenSerialNo: Long = 0,
//
//    val updatedAt: Long = System.currentTimeMillis()
//)