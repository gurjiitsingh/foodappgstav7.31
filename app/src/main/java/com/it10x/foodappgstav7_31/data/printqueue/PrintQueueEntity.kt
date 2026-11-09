package com.it10x.foodappgstav7_31.data.printqueue

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "print_queue",
    indices = [
        Index(
            value = ["referenceId"],
            unique = true
        )
    ]
)
data class PrintQueueEntity(

    @PrimaryKey
    val id: String,
    val referenceId: String?,
    val earnedPoints: Int?,
    val role: String,

    val logoImagePath: String? = null,
    val qrData: String? = null,     // generated per bill
    // ----------------------------
    // JOB TYPE
    // ----------------------------
    val jobType: String = "TEXT",   // TEXT or IMAGE

    // ----------------------------
    // TEXT JOB
    // ----------------------------
    val text: String? = null,

    // ----------------------------
    // IMAGE JOB
    // ----------------------------
    val imagePath: String? = null,

    // ----------------------------
    // PAYMENT INFO
    // ----------------------------
    val paymentMode: String? = null,
    val grandTotal: Double? = null,

    // ----------------------------
    // STATUS
    // ----------------------------
    val status: String, // PENDING, PRINTING, FAILED

    val retryCount: Int,

    val createdAt: Long
)

//@Entity(tableName = "print_queue")
//data class PrintQueueEntity(
//
//    @PrimaryKey
//    val id: String,
//
//    val role: String,
//    val text: String,
//
//    val paymentMode: String? = null,
//    val grandTotal: Double? = null,
//
//    val status: String, // PENDING, PRINTING, FAILED
//    val retryCount: Int,
//
//    val createdAt: Long
//)