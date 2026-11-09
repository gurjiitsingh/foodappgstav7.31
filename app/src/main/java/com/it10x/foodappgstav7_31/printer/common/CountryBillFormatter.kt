package com.it10x.foodappgstav7_31.printer.common

import com.it10x.foodappgstav7_31.data.print.OutletInfo
import com.it10x.foodappgstav7_31.printer.PrintOrder
import com.it10x.foodappgstav7_31.printer.common.text.AustriaBillFormatter
import com.it10x.foodappgstav7_31.printer.common.text.IndiaBillFormatter
import com.it10x.foodappgstav7_31.printer.common.text.UkBillFormatter
import com.it10x.foodappgstav7_31.printer.common.text.spainBillFormatter



object CountryBillFormatter {

    /**
     * Country-specific CUSTOMER BILL formatter.
     *
     * Printer type does NOT matter here.
     *
     * The same returned text can be printed through:
     * - Bluetooth
     * - LAN
     * - USB
     */
    fun billing48(
        order: PrintOrder,
        kotNumberText: String,
        stewardName: String,
        outletInfo: OutletInfo,
        earnedPoints: Int,
    ): String {

        return when (outletInfo.countryCode?.uppercase()) {

            // 🇮🇳 India
            "IN" -> {
                IndiaBillFormatter.billing48(
                    order = order,
                    outletInfo = outletInfo,
                    kotNumberText,
                    stewardName,
                    earnedPoints = earnedPoints,
                )
            }

            // 🇦🇹 Austria
            "AT" -> {
                AustriaBillFormatter.billing48(
                    order = order,
                    outletInfo = outletInfo
                )
            }

            // 🇬🇧 United Kingdom
            "GB" -> {
                // Until UK formatter is created,
                // temporarily use India format.
                UkBillFormatter.billing48(
                    order = order,
                    outletInfo = outletInfo
                )
            }

            // 🇬🇧 United Kingdom
            "ES" -> {
                spainBillFormatter.billing48(
                    order = order,
                    outletInfo = outletInfo,
                    kotNumberText,
                    stewardName,

                    )
            }

            // Default
            else -> {
                IndiaBillFormatter.billing48(
                    order = order,
                    outletInfo = outletInfo,
                    kotNumberText,
                    stewardName,
                    earnedPoints = earnedPoints,

                )
            }
        }
    }
}