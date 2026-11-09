package com.it10x.foodappgstav7_31.network.model

//data class Signature(
//    val value: String?,
//    val algorithm: String?,
//    val public_key: String?
//)
//data class TransactionResponse(
//    val transaction_id: String?,
//    val transaction_number: Long?,
//    val signature_counter: Long?,
//    val signature: Signature?,   // ✅ FIXED
//    val log_time_start: String?,
//    val log_time_end: String?
//)

data class TransactionResponse(
    val tx_id: String?,   // ✅ correct field name
    val state: String?,
    val signature: Signature?,
    val log_time_start: String?,
    val log_time_end: String?
)

data class Signature(
    val value: String?,
    val counter: Long?,   // ✅ this was your signature_counter
    val algorithm: String?,
    val public_key: String?
)