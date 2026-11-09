package com.it10x.foodappgstav7_31.printer.common.text

import android.util.Log
import com.it10x.foodappgstav7_31.data.print.OutletInfo
import com.it10x.foodappgstav7_31.printer.PrintOrder
import java.text.SimpleDateFormat
import java.util.Locale

object AustriaBillFormatter {

    // =========================================================
    // CONSTANTS
    // =========================================================

    private const val LINE_WIDTH = 43

    private const val ALIGN_LEFT = "\u001B\u0061\u0000"
    private const val ALIGN_CENTER = "\u001B\u0061\u0001"


    // =========================================================
    // MAIN AUSTRIA BILL
    // =========================================================

    fun billing48(
        order: PrintOrder,
        outletInfo: OutletInfo
    ): String {




        val outletHeader =
            buildOutletHeader(
                outletInfo = outletInfo,
                width = LINE_WIDTH
            )

        val orderHeader =
            buildHeaderBlock(order)

        val itemsBlock =
            buildItemsBlock(order)

        val paymentBlock =
            buildPaymentBlock(order)

        val taxBlock =
            buildTaxBlock(order)

        val legalBlock =
            buildLegalBlock(
                order = order,
                outletInfo = outletInfo
            )

        val testQrData =
            "_R1-AT1_4.1GASTROPOS01_2026-TEST-0019275_" +
                    "2026-05-08T12:24:35_4,50_25,30_0,00_0,00_0,00_" +
                    "TEST-QR-FISCAL-DATA"

        return buildString {

            append(ALIGN_LEFT)

            // =================================================
            // OUTLET HEADER
            // =================================================

            append(outletHeader)
            append("\n\n")

            // =================================================
            // ORDER HEADER
            // =================================================

            append(orderHeader)
            append("\n")

            append("-".repeat(LINE_WIDTH))
            append("\n")

            // =================================================
            // ITEMS
            // =================================================

            append(itemsBlock)
            append("\n")

            append("-".repeat(LINE_WIDTH))
            append("\n")

            // =================================================
            // PAYMENT
            // =================================================

            append(paymentBlock)
            append("\n")

            // =================================================
            // TAX BREAKDOWN
            // =================================================

            append("=".repeat(LINE_WIDTH))
            append("\n")

            append(taxBlock)

            append("\n")
            append("=".repeat(LINE_WIDTH))
            append("\n")

            // =================================================
            // FINAL TOTAL
            // =================================================

            append(
                totalLine(
                    "RECHNUNGSBETRAG",
                    order.grandTotal
                )
            )

            append("\n\n")

            // =================================================
            // LEGAL / FISCAL DATA
            // =================================================

            append(legalBlock)

            append("\n\n")

            // =================================================
            // FOOTER
            // =================================================

            append(
                outletInfo.footerNote
                    ?.takeIf { it.isNotBlank() }
                    ?: "WIR DANKEN FÜR IHREN BESUCH!!"
            )

            append("\n")

            //END TEST QR
        }
    }


    // =========================================================
    // OUTLET HEADER
    // =========================================================

    private fun buildOutletHeader(
        outletInfo: OutletInfo,
        width: Int
    ): String {

        val lines = mutableListOf<String>()

        // Company / outlet name
//        if (outletInfo.outletName.isNotBlank()) {
//
//            lines += centerText(
//                outletInfo.outletName,
//                width
//            )
//        }
        if (outletInfo.outletName.isNotBlank()) lines += outletInfo.outletName
        // Address
        outletInfo.addressLine1
            ?.takeIf { it.isNotBlank() }
            ?.let {
                lines += it.take(width)
            }

        outletInfo.addressLine2
            ?.takeIf { it.isNotBlank() }
            ?.let {
                lines += it.take(width)
            }

        outletInfo.addressLine3
            ?.takeIf { it.isNotBlank() }
            ?.let {
                lines += it.take(width)
            }

        outletInfo.city
            ?.takeIf { it.isNotBlank() }
            ?.let {
                lines += it.take(width)
            }

        // Phone
        outletInfo.phone
            ?.takeIf { it.isNotBlank() }
            ?.let {
                lines += "Tel.: $it"
            }

        outletInfo.phone2
            ?.takeIf { it.isNotBlank() }
            ?.let {
                lines += "Tel.: $it"
            }

        // Email
        outletInfo.email
            ?.takeIf { it.isNotBlank() }
            ?.let {
                lines += "E-Mail: $it"
            }

        // Austrian UID
        outletInfo.gstVatNumber
            ?.takeIf { it.isNotBlank() }
            ?.let {
                lines += "UID Nr. $it"
            }

        return lines.joinToString("\n")
    }


    // =========================================================
    // ORDER HEADER
    // =========================================================

    private fun buildHeaderBlock(
        order: PrintOrder
    ): String {

        val lines = mutableListOf<String>()

        // Table
        if (order.tableNo?.isNotBlank() == true) {

            lines += order.tableNo
        }

        // Invoice
        lines +=
            "RECHNUNG NR. ${order.orderNo}"

        // Date
        val date =
            extractDate(order.dateTime)

        if (date.isNotBlank()) {

            lines +=
                "vom $date"
        }

        // Time
        val time =
            extractTime(order.dateTime)

        if (time.isNotBlank()) {

            lines +=
                time.padStart(LINE_WIDTH)
        }

        return lines.joinToString("\n")
    }


    // =========================================================
    // ITEMS
    // =========================================================

    private fun buildItemsBlock(
        order: PrintOrder
    ): String {

        if (order.items.isEmpty()) {

            return "Keine Artikel"
        }

        return buildString {

            order.items.forEach { item ->

                // Quantity
                val qty =
                    item.quantity
                        .toString()
                        .padStart(2)

                // Name
                val name =
                    item.name
                        .take(20)
                        .padEnd(20)

                // Price
                val price =
                    formatEuro(item.price)
                        .padStart(8)

                // Total
                val total =
                    formatEuro(item.subtotal)
                        .padStart(10)

                append(
                    "$qty   $name$price$total"
                )

                append("\n")

                // -------------------------------------------------
                // MODIFIERS
                // -------------------------------------------------

                if (!item.modifiersJson.isNullOrBlank()) {

                    try {

                        val modifiers =
                            item.modifiersJson
                                .removePrefix("[")
                                .removeSuffix("]")
                                .split(",")
                                .map {
                                    it.trim()
                                        .replace("\"", "")
                                }
                                .filter {
                                    it.isNotBlank()
                                }

                        modifiers.forEach { modifier ->

                            append(
                                "     + ${modifier.trim()}"
                            )

                            append("\n")
                        }

                    } catch (_: Exception) {

                        append(
                            "     + ${item.modifiersJson}"
                        )

                        append("\n")
                    }
                }

                // -------------------------------------------------
                // NOTE
                // -------------------------------------------------

                if (!item.note.isNullOrBlank()) {

                    append(
                        "     ${item.note}"
                    )

                    append("\n")
                }
            }

        }.trimEnd()
    }


    // =========================================================
    // PAYMENT
    // =========================================================

    private fun buildPaymentBlock(
        order: PrintOrder
    ): String {

        val paymentLabel =
            when (
                order.paymentMode?.uppercase()
            ) {

                "CASH",
                "NONE" ->
                    "Bar"

                "CARD" ->
                    "Karte"

                "UPI" ->
                    "Online"

                "BANK_TRANSFER" ->
                    "Überweisung"

                else ->
                    order.paymentMode
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: "Bar"
            }

        return totalLine(
            paymentLabel,
            order.grandTotal
        )
    }


    // =========================================================
    // AUSTRIA VAT BREAKDOWN
    // =========================================================
    //
    // Example:
    //
    // NETTO                                12,64
    // MwSt.                 10,00%          1,26
    // NETTO                                 7,08
    // MwSt.                 20,00%          1,42
    //
    // =========================================================

    private fun buildTaxBlock(
        order: PrintOrder
    ): String {

        if (order.items.isEmpty()) {
            return ""
        }

        // Group item totals by VAT rate
        val taxGroups =
            order.items
                .groupBy { item ->
                    item.taxRate ?: 0.0
                }
                .filterKeys { rate ->
                    rate > 0.0
                }

        return buildString {

            taxGroups
                .toSortedMap()
                .forEach { (taxRate, grossTotal) ->

                    val gross =
                        grossTotal.sumOf { item ->
                            item.subtotal
                        }

                    // Austrian sample indicates item prices include VAT.
                    // Therefore calculate NETTO from the gross amount.
                    val net =
                        gross / (1.0 + taxRate / 100.0)

                    val vat =
                        gross - net

                    append(
                        totalLine(
                            "NETTO",
                            net
                        )
                    )

                    append("\n")

                    append(
                        taxLine(
                            label = "MwSt.",
                            rate = String.format(
                                Locale.GERMANY,
                                "%.2f%%",
                                taxRate
                            ),
                            amount = vat
                        )
                    )

                    append("\n")
                }
        }.trimEnd()
    }


    // =========================================================
    // TAX RATE
    // =========================================================

    private fun normalizeTaxRate(
        rate: Double
    ): Double {

        return when {

            rate <= 0.0 ->
                0.0

            else ->
                rate
        }
    }


    // =========================================================
    // FORMAT VAT RATE
    // =========================================================

    private fun formatRate(
        rate: Double
    ): String {

        return String.format(
            Locale.GERMANY,
            "%.2f%%",
            rate
        )
    }


    // =========================================================
    // LEGAL / FISCAL INFORMATION
    // =========================================================
// =========================================================
// LEGAL / FISCAL INFORMATION
// =========================================================

    private fun buildLegalBlock(
        order: PrintOrder,
        outletInfo: OutletInfo
    ): String {

        Log.d(
            "AUSTRIA_DATE",
            "RAW dateTime = '${order.dateTime}'"
        )

        return buildString {

            if (order.orderNo.isNotBlank()) {

                // Kassen-ID
                append("Kassen-ID:     1")
                append("\n")

                // Beleg-Nr
                append(
                    "Beleg-Nr:      ${order.orderNo}"
                )

                append("\n")
            }

            if (order.dateTime.isNotBlank()) {

                val legalDateTime =
                    formatLegalDateTime(
                        order.dateTime
                    )

                append(
                    "Beleg-Datum:   $legalDateTime"
                )

                append("\n")
            }

        }.trimEnd()
    }







    // =========================================================
// LEGAL DATE/TIME — 24 HOUR FORMAT
// =========================================================

    private fun formatLegalDateTime(
        dateTime: String
    ): String {

        if (dateTime.isBlank()) {
            return ""
        }

        return try {

            val parser = SimpleDateFormat(
                "dd/MM/yyyy hh:mm a",
                Locale.US
            )

            parser.isLenient = false

            val date = parser.parse(dateTime.trim())
                ?: return dateTime

            val output = SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.US
            )

            val result = output.format(date)

            Log.d(
                "AUSTRIA_DATE",
                "LEGAL converted '$dateTime' -> '$result'"
            )

            result

        } catch (e: Exception) {

            Log.e(
                "AUSTRIA_DATE",
                "LEGAL parse failed='$dateTime'",
                e
            )

            dateTime
        }
    }

    // =========================================================
    // TOTAL LINE
    // =========================================================

    private fun totalLine(
        label: String,
        amount: Double
    ): String {

        val value =
            formatEuro(amount)

        val spaces =
            LINE_WIDTH -
                    label.length -
                    value.length

        return label +
                " ".repeat(
                    maxOf(spaces, 1)
                ) +
                value
    }


    // =========================================================
    // TAX LINE
    // =========================================================

    private fun taxLine(
        label: String,
        rate: String,
        amount: Double
    ): String {

        /*
         * Desired:
         *
         * MwSt.                 10,00%          1,26
         */

        val rateColumnWidth = 12

        val value =
            formatEuro(amount)

        val rateColumn =
            rate.padStart(rateColumnWidth)

        val left =
            label +
                    " ".repeat(17) +
                    rateColumn

        val spaces =
            LINE_WIDTH -
                    left.length -
                    value.length

        return left +
                " ".repeat(
                    maxOf(spaces, 1)
                ) +
                value
    }


    // =========================================================
    // EURO FORMAT
    // =========================================================

    private fun formatEuro(
        value: Double
    ): String {

        return String.format(
            Locale.GERMANY,
            "%.2f",
            value
        )
    }


    // =========================================================
    // DATE
    // =========================================================

    private fun extractDate(
        dateTime: String
    ): String {

        if (dateTime.isBlank()) {
            return ""
        }

        return dateTime
            .substringBefore(" ")
            .substringBefore("T")
    }




    // =========================================================
    // TIME
    // =========================================================
// =========================================================
// TIME — 24 HOUR FORMAT
// =========================================================

// =========================================================
// TIME — 24 HOUR FORMAT
// =========================================================

    private fun extractTime(
        dateTime: String
    ): String {

        if (dateTime.isBlank()) {
            return ""
        }

        return try {

            val parser = SimpleDateFormat(
                "dd/MM/yyyy hh:mm a",
                Locale.US
            )

            parser.isLenient = false

            val date = parser.parse(dateTime.trim())
                ?: return ""

            val output = SimpleDateFormat(
                "HH:mm",
                Locale.US
            )

            val result = output.format(date)

//            Log.d(
//                "AUSTRIA_DATE",
//                "HEADER converted '$dateTime' -> '$result'"
//            )

            result

        } catch (e: Exception) {

            Log.e(
                "AUSTRIA_DATE",
                "HEADER parse failed='$dateTime'",
                e
            )

            ""
        }
    }




    // =========================================================
    // CENTER
    // =========================================================

    private fun centerText(
        text: String,
        width: Int
    ): String {

        val pad =
            (width - text.length) / 2

        return " ".repeat(
            maxOf(pad, 0)
        ) + text
    }
}