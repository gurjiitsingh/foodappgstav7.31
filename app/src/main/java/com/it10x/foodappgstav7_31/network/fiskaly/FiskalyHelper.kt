package com.it10x.foodappgstav7_31.network.fiskaly

import com.it10x.foodappgstav7_31.network.model.*

object FiskalyHelper {

    fun buildFinishRequest(
        amount: Double,
        clientId: String
    ): FinishTransactionRequest {

        return FinishTransactionRequest(
            client_id = clientId,
            schema = Schema(
                standard_v1 = StandardV1(
                    receipt = Receipt(
                        receipt_type = "RECEIPT",
                        amounts_per_vat_rate = listOf(
                            VatAmount(
                                vat_rate = "NORMAL",
                                amount = String.format("%.2f", amount)
                            )
                        ),
                        amounts_per_payment_type = listOf(
                            PaymentAmount(
                                payment_type = "CASH",
                                amount = String.format("%.2f", amount)
                            )
                        )
                    )
                )
            )
        )
    }
}