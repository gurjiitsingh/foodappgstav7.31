package com.it10x.foodappgstav7_31.printer

import com.it10x.foodappgstav7_31.data.print.OutletInfo



object Receipt48Formatter {

    private const val LINE_WIDTH = 48


    fun billing48(
        order: PrintOrder,
        outletInfo: OutletInfo
    ): String {

        val header = buildReceiptHeader48(
            order,
            outletInfo
        )

        val items = buildItemsBlock(order)

        return buildString {

            append(EscPosCommands.RESET)

            append(EscPosCommands.FONT_A)

            append(EscPosCommands.ALIGN_CENTER)

            append(header)

            append(EscPosCommands.ALIGN_LEFT)

            append(items)

            // Totals will come here

            append("\n")

            append(EscPosCommands.ALIGN_CENTER)

            append(EscPosCommands.BOLD_ON)

            append("THANK YOU\n")

            append(EscPosCommands.BOLD_OFF)

            append(EscPosCommands.ALIGN_LEFT)
        }
    }
private fun divider(char: Char = '-'): String =
    char.toString().repeat(LINE_WIDTH)

private fun center(text: String): String {
    if (text.length >= LINE_WIDTH) return text
    val padding = (LINE_WIDTH - text.length) / 2
    return " ".repeat(padding) + text
}

private fun format(value: Double?): String {
    return String.format("%.2f", value ?: 0.0)
}

private fun totalLine48(label: String, value: Double?): String {

    val amount = format(value)

    return label.padEnd(LINE_WIDTH - amount.length) + amount + "\n"
}

private fun totalLine48(label: String, value: String): String {

    return label.padEnd(LINE_WIDTH - value.length) + value + "\n"
}



private fun wrapText_NEW(
    text: String,
    width: Int
): List<String> {

    if (text.length <= width)
        return listOf(text)

    val result = mutableListOf<String>()

    var remaining = text.trim()

    while (remaining.length > width) {

        var breakIndex = remaining.lastIndexOf(' ', width)

        if (breakIndex == -1)
            breakIndex = width

        result.add(
            remaining.substring(0, breakIndex)
        )

        remaining =
            remaining.substring(breakIndex).trimStart()
    }

    if (remaining.isNotEmpty())
        result.add(remaining)

    return result
}


private fun twoColumn48(
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String
): String {

    val left = "$leftLabel $leftValue"

    if (rightLabel.isBlank()) {
        return left
    }

    val right = "$rightLabel $rightValue"

    return left.padEnd(24) + right
}


private fun itemHeader48(): String
{

    val name = "ITEM".padEnd(22)

    val rate = "RATE".padStart(8)

    val qty = "QTY".padStart(5)

    val total = "AMOUNT".padStart(13)

    return name + rate + qty + total
}


private fun itemLine48(

    name: String,

    rate: Double,

    qty: Double,

    total: Double

): String {

    return name.padEnd(22)
        .take(22) +
            ReceiptFormatter.formatAmount(rate).padStart(8) +
            qty.toString().padStart(5) +
            ReceiptFormatter.formatAmount(total).padStart(13)
}



private fun buildItemLines(

    name: String,

    rate: Double,

    qty: Double,

    total: Double

): String {

    val wrapped = wrapText_NEW(name, 22)

    val builder = StringBuilder()

    builder.append(

        itemLine48(
            wrapped.first(),
            rate,
            qty,
            total
        )

    )

    builder.append("\n")

    if (wrapped.size > 1) {

        wrapped.drop(1).forEach {

            builder.append(it)

            builder.append("\n")
        }
    }

    return builder.toString()
}


private fun grandTotal48(amount: Double): String {

    val total = ReceiptFormatter.formatAmount(amount)

    return buildString {

        append(divider('='))

        append("\n")



        append(EscPosCommands.BOLD_ON)
        append(EscPosCommands.DOUBLE_SIZE)

        append(
            "GRAND TOTAL"
                .padEnd(LINE_WIDTH - total.length)
        )


        append(EscPosCommands.NORMAL_SIZE)
        append(EscPosCommands.BOLD_OFF)

        append(total)

        append("\n")

        append(divider('='))

        append("\n")
    }
}


private fun buildReceiptHeader48(
    order: PrintOrder,
    outletInfo: OutletInfo
): String {

    val builder = StringBuilder()

    //==============================
    // Outlet Name
    //==============================

    builder.append(EscPosCommands.BOLD_ON)
    builder.append(EscPosCommands.DOUBLE_SIZE)

    builder.append(center(outletInfo.outletName.uppercase()))

    builder.append(EscPosCommands.NORMAL_SIZE)
    builder.append(EscPosCommands.BOLD_OFF)

    builder.append("\n")

    if (!outletInfo.addressLine1.isNullOrBlank()) {
        builder.append(center(outletInfo.addressLine1))
        builder.append("\n")
    }

    if (!outletInfo.phone.isNullOrBlank()) {
        builder.append(center("☎ ${outletInfo.phone}"))
        builder.append("\n")
    }

    if (!outletInfo.gstVatNumber.isNullOrBlank()) {
        builder.append(center("GSTIN : ${outletInfo.gstVatNumber}"))
        builder.append("\n")
    }

    builder.append(divider('='))
    builder.append("\n")

    //==============================
    // Invoice Title
    //==============================

    builder.append(EscPosCommands.BOLD_ON)

    builder.append(center("TAX INVOICE"))

    builder.append(EscPosCommands.BOLD_OFF)

    builder.append("\n")

    if (!order.orderType.isNullOrBlank()) {
        builder.append(center(order.orderType.uppercase()))
        builder.append("\n")
    }

    builder.append(divider('='))
    builder.append("\n")

    //==============================
    // Order Details
    //==============================

    builder.append(
        twoColumn48(
            "Invoice No :",
            order.orderNo,
            "Date :",
            order.dateTime
        )
    )
    builder.append("\n")

    builder.append(
        twoColumn48(
            "Time :",
            order.dateTime,
            "Token :",
            order.tableNo ?: "-"
        )
    )
    builder.append("\n")

    builder.append(
        twoColumn48(
            "Table :",
            order.tableNo ?: "-",
            "KOT :",
            order.tableNo ?: "-"
        )
    )
    builder.append("\n")

    builder.append(
        twoColumn48(
            "Steward :",
            order.tableNo ?: "-",
            "",
            ""
        )
    )
    builder.append("\n")

    builder.append(divider('='))

    return builder.toString()
}




private fun buildItemsBlock(
    order: PrintOrder
): String {

    return buildString {

        append(EscPosCommands.BOLD_ON)
        append(itemHeader48())
        append(EscPosCommands.BOLD_OFF)
        append("\n")
        append(divider('='))
        append("\n")
        // Header


        if (order.items.isEmpty()) {

            append(center("No Items"))
            append("\n")

        } else {

            order.items.forEach { item ->

                //----------------------------------
                // Main Item
                //----------------------------------

                append(EscPosCommands.BOLD_ON)

                append(
                    buildItemLines(
                        name = item.name.uppercase(),
                        rate = item.price,
                        qty = item.quantity.toDouble(),
                        total = item.subtotal
                    )
                )

                append(EscPosCommands.BOLD_OFF)

                //----------------------------------
                // Modifiers
                //----------------------------------

                if (!item.modifiersJson.isNullOrBlank()) {

                    try {

                        val modifiers = item.modifiersJson
                            .removePrefix("[")
                            .removeSuffix("]")
                            .split(",")
                            .map {
                                it.trim().replace("\"", "")
                            }
                            .filter {
                                it.isNotBlank()
                            }

                        modifiers.forEach { modifier ->

                            append("   + ")
                            append(modifier)
                            append("\n")

                        }

                    } catch (_: Exception) {

                        append("   + ")
                        append(item.modifiersJson)
                        append("\n")

                    }

                }

                //----------------------------------
                // Note
                //----------------------------------

                if (!item.note.isNullOrBlank()) {

                    append("   * ")
                    append(item.note)
                    append("\n")

                }

                //----------------------------------
                // Space After Each Item
                //----------------------------------

                append("\n")
            }
        }

        append(divider('='))
        append("\n")
    }
}


}