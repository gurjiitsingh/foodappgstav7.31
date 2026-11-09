package com.it10x.foodappgstav7_31.network.model


data class PaymentAmount(
    val payment_type: String,
    val amount: String
)
data class VatAmount(
    val vat_rate: String,
    val amount: String
)

data class Receipt(
    val receipt_type: String = "RECEIPT",
    val amounts_per_vat_rate: List<VatAmount>,
    val amounts_per_payment_type: List<PaymentAmount>
)
data class StandardV1(
    val receipt: Receipt
)
data class Schema(
    val standard_v1: StandardV1
)
data class FinishTransactionRequest(
    val state: String = "FINISHED",
    val client_id: String,
    val schema: Schema
)

