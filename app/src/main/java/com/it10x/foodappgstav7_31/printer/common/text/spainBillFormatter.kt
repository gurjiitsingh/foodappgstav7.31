package com.it10x.foodappgstav7_31.printer.common.text

import com.it10x.foodappgstav7_31.data.print.OutletInfo
import com.it10x.foodappgstav7_31.printer.PrintOrder


import com.it10x.foodappgstav7_31.printer.common.*


object spainBillFormatter {

    private const val ALIGN_LEFT = "\u001B\u0061\u0000"
    private const val ALIGN_CENTER = "\u001B\u0061\u0001"
    fun billing48(
        order: PrintOrder,
        outletInfo: OutletInfo,
        kotNumberText: String,
        stewardName: String,
    ): String {

        val LINE_WIDTH = 48

        val outletHeader =
            buildOutletHeader(outletInfo, LINE_WIDTH)

        val headerBlock =
            buildHeaderBlock(
                order = order,
                stewardName = stewardName
            )



        val totalsBlock = buildString {

            append(
                totalLine48(
                    "Item Total",
                    order.itemTotal
                )
            )

            if ((order.deliveryFee ?: 0.0) > 0.0) {
                append(
                    totalLine48(
                        "Delivery",
                        order.deliveryFee
                    )
                )
            }

            if ((order.discount ?: 0.0) > 0.0) {
                append(
                    totalLine48(
                        "Discount",
                        order.discount
                    )
                )
            }

            // Taxable Amount
//            append(
//                totalLine48(
//                    "Taxable Amount",
//                    order.taxableAmount
//                )
//            )

            // CGST 2.5%
//            append(
//                totalLine48(
//                    "CGST 2.5% on [${format(order.itemTotal)}]",
//                    order.itemTotal * 0.025
//                )
//            )
//
//            // SGST 2.5%
//            append(
//                totalLine48(
//                    "SGST 2.5% on [${format(order.itemTotal)}]",
//                    order.itemTotal * 0.025
//                )
//            )

            // Roundof
            append(
                totalLine48(
                    "RoundOff",
                    order.roundOff
                )
            )
        }

        val qrTitleToPrint =
            if (outletInfo.qrEnabled == true) {

                when {
                    order.paymentMode == "UPI" &&
                            !outletInfo.upiId.isNullOrBlank() ->
                        outletInfo.upiTitle ?: "Scan & Pay"

                    else ->
                        outletInfo.qrTitle
                }

            } else {
                null
            }

        val itemsBlock =
            if (order.items.isEmpty()) {

                "No items found"

            } else {

                val header =
                    "QTY".padEnd(4) +
                            "ITEM".padEnd(26) +
                            "PRICE".padStart(8) +
                            "TOTAL".padStart(10)

                val divider =
                    "-".repeat(LINE_WIDTH)

                val lines = buildString {

                    order.items.forEach { item ->

                        val qty =
                            item.quantity
                                .toString()
                                .padEnd(4)

                        val name =
                            item.name
                                .take(26)
                                .padEnd(26)

                        val price =
                            format(item.price)
                                .padStart(8)

                        val total =
                            format(item.subtotal)
                                .padStart(10)

                        append(
                            qty +
                                    name +
                                    price +
                                    total +
                                    "\n"
                        )

                        // Modifiers
                        // Modifiers
                        if (!item.modifiersJson.isNullOrBlank()) {

                            val modifiers =
                                ModifierJsonHelper.fromJson(item.modifiersJson)

                            modifiers.forEach { group ->

                                group.items.forEach { modifierItem ->

                                    if (modifierItem.name.isNotBlank()) {
                                        append(
                                            "    + ${modifierItem.name}\n"
                                        )
                                    }
                                }
                            }
                        }

                        // Note
                        if (!item.note.isNullOrBlank()) {

                            append(
                                "• ${item.note}\n"
                            )
                        }
                    }
                }

                "$header\n$divider\n$lines"
            }

        return buildString {

            append(ALIGN_CENTER)

            if (!qrTitleToPrint.isNullOrBlank()) {

                append(
                    qrTitleToPrint.uppercase()
                )

                append("\n\n")
            }

            append(ALIGN_LEFT)

            append(
                """
------------------------------------------------
$outletHeader
------------------------------------------------
${buildInvoiceTitle(order)}
------------------------------------------------
$headerBlock
------------------------------------------------
$itemsBlock
------------------------------------------------
$totalsBlock
------------------------------------------------
${grandTotalLine48("TOTAL", order.grandTotal)}
------------------------------------------------
${buildOutletFooter(outletInfo, 48)}
Thank You!

""".trimIndent()
            )
        }
    }



    private fun buildOutletHeader(info: OutletInfo, width: Int): String {
        val lines = mutableListOf<String>()
        if (info.outletName.isNotBlank()) lines += centerText(info.outletName, width)
        info.addressLine1
            ?.takeIf { it.isNotBlank() }
            ?.let { address ->
                lines += address.take(width)
            }
        if(width==32){
            info.addressLine2
                ?.takeIf { it.isNotBlank() }
                ?.let { lines += it.take(width) }
            info.addressLine3
                ?.takeIf { it.isNotBlank() }
                ?.let { lines += it.take(width) }
            info.city
                ?.takeIf { it.isNotBlank() }
                ?.let { lines += it.take(width) }
            val phone1 = info.phone?.takeIf { it.isNotBlank() }
            val phone2 = info.phone2?.takeIf { it.isNotBlank() }

            if (phone1 != null && phone2 != null) {
                lines += "Phone: $phone1, $phone2".take(width)
            } else if (phone1 != null) {
                lines += "Phone: $phone1".take(width)
            } else if (phone2 != null) {
                lines += "Phone: $phone2".take(width)
            }
            info.email
                ?.takeIf { it.isNotBlank() }
                ?.let { lines += "Email: $it" }
            info.web
                ?.takeIf { it.isNotBlank() }
                ?.let { lines += "$it" }
            info.gstVatNumber?.let { lines += "VAT: $it" }
            //info.footerNote?.let { lines += it.take(width) }
        }

        if(width==48){
            info.addressLine2
                ?.takeIf { it.isNotBlank() }
                ?.let { lines += it.take(width) }
            info.addressLine3
                ?.takeIf { it.isNotBlank() }
                ?.let { lines += it.take(width) }
            info.city
                ?.takeIf { it.isNotBlank() }
                ?.let { lines += it.take(width) }
            val phone1 = info.phone?.takeIf { it.isNotBlank() }
            val phone2 = info.phone2?.takeIf { it.isNotBlank() }

            if (phone1 != null && phone2 != null) {
                // Both phones available
                lines += "Phone: $phone1, $phone2".take(width)
            } else if (phone1 != null) {
                // Only first phone
                lines += "Phone: $phone1".take(width)
            } else if (phone2 != null) {
                // Only second phone
                lines += "Phone: $phone2".take(width)
            }
            info.email
                ?.takeIf { it.isNotBlank() }
                ?.let { lines += "Email: $it" }
            info.web
                ?.takeIf { it.isNotBlank() }
                ?.let { lines += "$it" }
            info.gstVatNumber?.let { lines += "VAT No: $it" }
            // info.footerNote?.let { lines += it.take(width) }
        }

        return lines.joinToString("\n")
    }


    private fun buildInvoiceTitle(order: PrintOrder): String {

        val orderTypeText = when (order.orderType) {
            "DINE_IN" -> "DINE IN"
            "TAKEAWAY" -> "TAKEAWAY"
            "DELIVERY" -> "DELIVERY"
            "ONLINE" -> "ONLINE"
            else -> order.orderType
        }

        val space =
            48 - "TAX INVOICE".length - 7

        return "\u001B\u0045\u0001" +   // Bold ON
                "TAX INVOICE" +
                " ".repeat(if (space > 0) space else 1) +
                orderTypeText +
                "\u001B\u0045\u0000"   // Bold OFF

    }

    //    private fun buildInvoiceTitle(order: PrintOrder): String {
//
//        val orderTypeText = when (order.orderType) {
//            "DINE_IN" -> "DINE IN"
//            "TAKEAWAY" -> "TAKEAWAY"
//            "DELIVERY" -> "DELIVERY"
//            "ONLINE" -> "ONLINE"
//            else -> order.orderType
//        }
//
//        return ALIGN_CENTER +
//                "\u001B\u0045\u0001" +   // Bold ON
//                "TAX INVOICE" +
//                "\n" +
//                orderTypeText +
//                "\u001B\u0045\u0000" +   // Bold OFF
//                ALIGN_LEFT
//    }
    // -----------------------------
    // HEADER LOGIC (IMPORTANT)
    // -----------------------------
// -----------------------------
// HEADER LOGIC
// -----------------------------
    private fun buildHeaderBlock(
        order: PrintOrder,
        stewardName: String
    ): String {

        val base = mutableListOf<String>()

        // ---------------------------------------------------------
        // COMMON FIELDS
        // ---------------------------------------------------------

        base.add("Invoice No : ${order.orderNo}")

        base.add(
            "Customer   : ${
                order.customerName.ifBlank {
                    "Walk-in"
                }
            }"
        )

        base.add("Date       : ${order.dateTime}")


        // ---------------------------------------------------------
        // ORDER TYPE
        // ---------------------------------------------------------

        when (order.orderType) {

            // =====================================================
            // DINE-IN
            // =====================================================

            "DINE_IN" -> {

                val table =
                    order.tableNo
                        ?.takeIf {
                            it.isNotBlank()
                        }

                val steward =
                    stewardName
                        .trim()
                        .takeIf {
                            it.isNotBlank()
                        }


                // -------------------------------------------------
                // TABLE + STEWARD ON SAME LINE
                // -------------------------------------------------

                if (table != null && steward != null) {

                    val left =
                        "Table : $table"

                    val right =
                        "Steward : $steward"

                    val space =
                        48 -
                                left.length -
                                right.length


                    if (space > 0) {

                        base.add(
                            left +
                                    " ".repeat(space) +
                                    right
                        )

                    } else {

                        // Too long for one 48-column line
                        base.add(
                            "Table : $table"
                        )

                        base.add(
                            "Steward : $steward"
                        )
                    }


                } else if (table != null) {

                    base.add(
                        "Table : $table"
                    )


                } else if (steward != null) {

                    base.add(
                        "Steward : $steward"
                    )
                }
            }


            // =====================================================
            // TAKEAWAY
            // =====================================================

            "TAKEAWAY" -> {

                order.customerPhone
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {

                        base.add(
                            "Phone      : $it"
                        )
                    }
            }


            // =====================================================
            // DELIVERY / ONLINE
            // =====================================================

            "DELIVERY",
            "ONLINE" -> {

                val addressLines =
                    mutableListOf<String>()


                order.dAddressLine1
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        addressLines.add(it)
                    }


                order.dAddressLine2
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        addressLines.add(it)
                    }


                order.dLandmark
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {

                        addressLines.add(
                            "Landmark: $it"
                        )
                    }


                // -------------------------------------------------
                // CITY + ZIP
                // -------------------------------------------------

                listOfNotNull(
                    order.dCity,
                    order.dZipcode
                )
                    .joinToString(" ")
                    .takeIf {
                        it.isNotBlank()
                    }
                    ?.let {

                        addressLines.add(it)
                    }


                // -------------------------------------------------
                // ADDRESS
                // -------------------------------------------------

                if (addressLines.isNotEmpty()) {

                    base.add(
                        "Address    :"
                    )

                    base.addAll(
                        addressLines
                    )
                }


                // -------------------------------------------------
                // PHONE
                // -------------------------------------------------

                order.customerPhone
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {

                        base.add(
                            "Phone      : $it"
                        )
                    }
            }
        }


        return base.joinToString("\n")
    }
    private fun centerText(text: String, width: Int): String {
        val pad = (width - text.length) / 2
        return " ".repeat(maxOf(pad, 0)) + text
    }


    private fun buildOutletFooter(info: OutletInfo, width: Int): String {

        val note = info.footerNote?.trim()

        // 🔹 If null or blank → return empty
        if (note.isNullOrBlank()) return ""

        val wrappedLines = wrapText(note, width)

        return buildString {

            wrappedLines.forEach { line ->
                append(line)
                append("\n")
            }

            append("-".repeat(width))
            append("\n")
        }
    }
    private fun wrapText(text: String, width: Int): List<String> {
        val words = text.split("\\s+".toRegex())
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {

            // If single word itself is longer than width
            if (word.length >= width) {
                if (currentLine.isNotBlank()) {
                    lines.add(currentLine.trim())
                    currentLine = ""
                }

                // break long word safely
                word.chunked(width).forEach {
                    lines.add(it)
                }

                continue
            }

            if ((currentLine + word).length + 1 > width) {
                lines.add(currentLine.trim())
                currentLine = "$word "
            } else {
                currentLine += "$word "
            }
        }

        if (currentLine.isNotBlank()) {
            lines.add(currentLine.trim())
        }

        return lines
    }





    private fun totalLine48(label: String, amount: Double): String {
        val formatted = format(amount)
        // label left-aligned, amount right-aligned to total 48 characters
        val space = 48 - label.length - formatted.length
        return label + " ".repeat(if (space > 0) space else 1) + formatted
    }

    private fun grandTotalLine48(label: String, amount: Double): String {
        val formatted = format(amount)

        val space = 48 - label.length - formatted.length

        return "\u001B\u0045\u0001" +   // Bold ON
                label +
                " ".repeat(if (space > 0) space else 1) +
                formatted +
                "\u001B\u0045\u0000" +   // Bold OFF
                "\n"
    }



    // -----------------------------
    // HELPERS
    // -----------------------------
    private fun totalLine(label: String, value: Double): String {
        if (value == 0.0) return ""
        val left = label.padEnd(14)
        val right = format(value).padStart(18)
        return left + right
    }

    private fun format(value: Double): String = "%.2f".format(value)

}