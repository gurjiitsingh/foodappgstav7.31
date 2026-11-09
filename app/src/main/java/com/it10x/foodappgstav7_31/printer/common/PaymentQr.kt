package com.it10x.foodappgstav7_31.printer.common

import com.it10x.foodappgstav7_31.data.print.OutletInfo
import com.it10x.foodappgstav7_31.printer.PrintOrder

import java.util.Locale

object PaymentQr {



    fun createPaymentQrData(
        order: PrintOrder,
        outletInfo: OutletInfo
    ): String {

        val amount = order.grandTotal



      val  UPI_ID =
            outletInfo.upiId!!

        val PAYEE_NAME =
            outletInfo.upiName
                ?.takeIf { it.isNotBlank() }
                ?: "Shop"

        require(amount > 0.0) {
            "Payment amount must be greater than zero"
        }

        val formattedAmount =
            String.format(
                Locale.US,
                "%.2f",
                amount
            )

        return buildString {

            append("upi://pay")

            append("?pa=")
            append(UPI_ID)

            append("&pn=")
            append(PAYEE_NAME)

            append("&am=")
            append(formattedAmount)

            append("&cu=INR")
        }
    }
}