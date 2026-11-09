package com.it10x.foodappgstav7_31.data.pos.model



data class KotHistoryBatch(

    val batchId: String,

    val sessionId: String,

    val tableNo: String,

    val orderType: String,

    val createdAt: Long,

    val source: String,

    val status: String,

    val itemCount: Int,

    val orderId: String?,

    val paidAt: Long?
)