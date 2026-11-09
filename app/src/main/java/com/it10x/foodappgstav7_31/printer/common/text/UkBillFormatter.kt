package com.it10x.foodappgstav7_31.printer.common.text

import com.it10x.foodappgstav7_31.data.print.OutletInfo
import com.it10x.foodappgstav7_31.printer.PrintOrder

object UkBillFormatter {

    private const val LINE_WIDTH = 48

    // ESC/POS alignment
    private const val ALIGN_LEFT = "\u001B\u0061\u0000"
    private const val ALIGN_CENTER = "\u001B\u0061\u0001"

    fun billing48(
        order: PrintOrder,
        outletInfo: OutletInfo
    ): String {

        val outletHeader = buildOutletHeader(
            info = outletInfo,
            width = LINE_WIDTH
        )

        val headerBlock = buildHeaderBlock(order)

        val itemsBlock = buildItemsBlock(order)

        val totalsBlock = buildTotalsBlock(order)

        val qrTitleToPrint =
            if (outletInfo.qrEnabled == true) {

                when {
                    order.paymentMode == "UPI" &&
                            !outletInfo.upiId.isNullOrBlank() ->
                        outletInfo.upiTitle ?: "SCAN & PAY"

                    else ->
                        outletInfo.qrTitle
                }

            } else {
                null
            }

        return buildString {

            // -------------------------------------------------
            // QR TITLE
            // -------------------------------------------------
            if (!qrTitleToPrint.isNullOrBlank()) {

                append(ALIGN_CENTER)

                append(
                    qrTitleToPrint
                        .uppercase()
                        .take(LINE_WIDTH)
                )

                append("\n\n")
            }

            // -------------------------------------------------
            // MAIN BILL
            // -------------------------------------------------
            append(ALIGN_CENTER)

            append(
                outletHeader
            )

            append("\n")

            append(ALIGN_LEFT)

            append(
                "-".repeat(LINE_WIDTH)
            )

            append("\n")

            // -------------------------------------------------
            // ORDER / CUSTOMER DETAILS
            // -------------------------------------------------
            append(headerBlock)

            append("\n")

            append(
                "-".repeat(LINE_WIDTH)
            )

            append("\n")

            // -------------------------------------------------
            // ITEMS
            // -------------------------------------------------
            append(itemsBlock)

            append("\n")

            append(
                "-".repeat(LINE_WIDTH)
            )

            append("\n")

            // -------------------------------------------------
            // TOTALS
            // -------------------------------------------------
            append(totalsBlock)

            append("\n")

            append(
                "-".repeat(LINE_WIDTH)
            )

            append("\n")

            // -------------------------------------------------
            // GRAND TOTAL
            // -------------------------------------------------
            append(
                totalLine48(
                    label = "TOTAL",
                    amount = order.grandTotal,
                    currency = true
                )
            )

            append("\n")

            append(
                "-".repeat(LINE_WIDTH)
            )

            append("\n")

            // -------------------------------------------------
            // PAYMENT
            // -------------------------------------------------
            order.paymentMode
                ?.takeIf { it.isNotBlank() }
                ?.let {

                    append(
                        totalTextLine48(
                            "Payment",
                            it
                        )
                    )

                    append("\n")
                }

            // -------------------------------------------------
            // FOOTER
            // -------------------------------------------------
            val footer = buildOutletFooter(
                info = outletInfo,
                width = LINE_WIDTH
            )

            if (footer.isNotBlank()) {

                append("\n")
                append(footer)
            }

            append("\n")

            append(ALIGN_CENTER)

            append("Thank You")

            append("\n")

            append("Please Visit Again")

            append("\n")
        }
    }


    // =========================================================
    // OUTLET HEADER
    // =========================================================

    private fun buildOutletHeader(
        info: OutletInfo,
        width: Int
    ): String {

        val lines = mutableListOf<String>()

        fun addCentered(text: String?) {
            if (!text.isNullOrBlank()) {
                lines += centerText(
                    text.trim(),
                    width
                )
            }
        }

        // -----------------------------------------------------
        // RESTAURANT NAME
        // -----------------------------------------------------

        addCentered(
            info.outletName
                .takeIf { it.isNotBlank() }
                ?.uppercase()
        )

        // -----------------------------------------------------
        // ADDRESS
        // -----------------------------------------------------

        addCentered(info.addressLine1)
        addCentered(info.addressLine2)
        addCentered(info.addressLine3)
        addCentered(info.city)

        // -----------------------------------------------------
        // PHONE
        // -----------------------------------------------------

        val phone1 =
            info.phone?.takeIf { it.isNotBlank() }

        val phone2 =
            info.phone2?.takeIf { it.isNotBlank() }

        when {

            phone1 != null && phone2 != null ->
                addCentered(
                    "Tel: $phone1 / $phone2"
                )

            phone1 != null ->
                addCentered(
                    "Tel: $phone1"
                )

            phone2 != null ->
                addCentered(
                    "Tel: $phone2"
                )
        }

        // -----------------------------------------------------
        // EMAIL
        // -----------------------------------------------------

        addCentered(info.email)

        // -----------------------------------------------------
        // WEBSITE
        // -----------------------------------------------------

        addCentered(info.web)

        // -----------------------------------------------------
        // VAT NUMBER
        // -----------------------------------------------------

        info.gstVatNumber
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { vatNumber ->
                addCentered("VAT No: $vatNumber")
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

        // -----------------------------------------------------
        // INVOICE
        // -----------------------------------------------------

        lines += labelValueLine(
            "Invoice No",
            order.orderNo
        )

        // -----------------------------------------------------
        // DATE
        // -----------------------------------------------------

        lines += labelValueLine(
            "Date",
            order.dateTime
        )

        // -----------------------------------------------------
        // CUSTOMER
        // -----------------------------------------------------

        lines += labelValueLine(
            "Customer",
            order.customerName.ifBlank {
                "Walk-in"
            }
        )

        // -----------------------------------------------------
        // ORDER TYPE
        // -----------------------------------------------------

        val orderTypeText =
            when (order.orderType) {

                "DINE_IN" -> "Dine In"

                "TAKEAWAY" -> "Takeaway"

                "DELIVERY" -> "Delivery"

                "ONLINE" -> "Online"

                null -> "Unknown"

                else ->
                    order.orderType
                        .replace("_", " ")
                        .lowercase()
                        .replaceFirstChar {
                            it.uppercase()
                        }
            }

        lines += labelValueLine(
            "Order Type",
            orderTypeText
        )

        // -----------------------------------------------------
        // DINE IN
        // -----------------------------------------------------

        if (order.orderType == "DINE_IN") {

            order.tableNo
                ?.takeIf { it.isNotBlank() }
                ?.let {

                    lines += labelValueLine(
                        "Table",
                        it
                    )
                }
        }

        // -----------------------------------------------------
        // TAKEAWAY
        // -----------------------------------------------------

        if (order.orderType == "TAKEAWAY") {

            order.customerPhone
                ?.takeIf { it.isNotBlank() }
                ?.let {

                    lines += labelValueLine(
                        "Phone",
                        it
                    )
                }
        }

        // -----------------------------------------------------
        // DELIVERY / ONLINE
        // -----------------------------------------------------

        if (
            order.orderType == "DELIVERY" ||
            order.orderType == "ONLINE"
        ) {

            val addressLines =
                mutableListOf<String>()

            order.dAddressLine1
                ?.takeIf { it.isNotBlank() }
                ?.let {

                    addressLines += it
                }

            order.dAddressLine2
                ?.takeIf { it.isNotBlank() }
                ?.let {

                    addressLines += it
                }

            order.dLandmark
                ?.takeIf { it.isNotBlank() }
                ?.let {

                    addressLines += "Landmark: $it"
                }

            val cityZip =
                listOfNotNull(
                    order.dCity
                        ?.takeIf { it.isNotBlank() },

                    order.dZipcode
                        ?.takeIf { it.isNotBlank() }
                ).joinToString(" ")

            if (cityZip.isNotBlank()) {

                addressLines += cityZip
            }

            if (addressLines.isNotEmpty()) {

//                lines += "Address:"

                addressLines.forEach { address ->

                    lines += wrapText(
                        address,
                        LINE_WIDTH
                    ).map {
                        "  $it"
                    }
                }
            }

            order.customerPhone
                ?.takeIf { it.isNotBlank() }
                ?.let {

                    lines += labelValueLine(
                        "Phone",
                        it
                    )
                }
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

            return "No items found"
        }

        return buildString {

            // -------------------------------------------------
            // COLUMN HEADER
            // -------------------------------------------------

            append(
                buildItemHeader()
            )

            append("\n")

            append(
                "-".repeat(LINE_WIDTH)
            )

            append("\n")

            // -------------------------------------------------
            // ITEMS
            // -------------------------------------------------

            order.items.forEach { item ->

                val quantity =
                    formatQuantity(item.quantity)

                val name =
                    item.name
                        .replace("\n", " ")
                        .trim()

                val price =
                    money(item.price)

                val total =
                    money(item.subtotal)

                append(
                    buildItemLine(
                        quantity = quantity,
                        name = name,
                        price = price,
                        total = total
                    )
                )

                append("\n")

                // -------------------------------------------------
                // MODIFIERS
                // -------------------------------------------------

                if (!item.modifiersJson.isNullOrBlank()) {

                    parseModifiers(
                        item.modifiersJson
                    ).forEach { modifier ->

                        wrapText(
                            modifier,
                            44
                        ).forEach { modLine ->

                            append("    + $modLine")
                            append("\n")
                        }
                    }
                }

                // -------------------------------------------------
                // NOTE
                // -------------------------------------------------

                if (!item.note.isNullOrBlank()) {

                    wrapText(
                        item.note.trim(),
                        44
                    ).forEach { noteLine ->

                        append("    Note: $noteLine")
                        append("\n")
                    }
                }
            }
        }.trimEnd()
    }


    private fun buildItemHeader(): String {

        val qty =
            "QTY".padEnd(5)

        val item =
            "ITEM".padEnd(23)

        val price =
            "PRICE".padStart(9)

        val total =
            "TOTAL".padStart(11)

        return qty + item + price + total
    }


    private fun buildItemLine(
        quantity: String,
        name: String,
        price: String,
        total: String
    ): String {

        val qty =
            quantity
                .take(5)
                .padEnd(5)

        val item =
            name
                .take(23)
                .padEnd(23)

        val priceText =
            price
                .padStart(9)

        val totalText =
            total
                .padStart(11)

        return qty +
                item +
                priceText +
                totalText
    }


    // =========================================================
    // TOTALS
    // =========================================================

    private fun buildTotalsBlock(
        order: PrintOrder
    ): String {

        return buildString {

            // -------------------------------------------------
            // SUBTOTAL
            // -------------------------------------------------

            append(
                totalLine48(
                    label = "Subtotal",
                    amount = order.itemTotal
                )
            )

            append("\n")

            // -------------------------------------------------
            // DISCOUNT
            // -------------------------------------------------

            if ((order.discount ?: 0.0) > 0.0) {

                append(
                    totalLine48(
                        label = "Discount",
                        amount = order.discount ?: 0.0,
                        negative = true
                    )
                )

                append("\n")
            }

            // -------------------------------------------------
            // DELIVERY
            // -------------------------------------------------

            if ((order.deliveryFee ?: 0.0) > 0.0) {

                append(
                    totalLine48(
                        label = "Delivery",
                        amount = order.deliveryFee ?: 0.0
                    )
                )

                append("\n")
            }

            // -------------------------------------------------
            // VAT
            // -------------------------------------------------

            if ((order.tax ?: 0.0) > 0.0) {

                append(
                    totalLine48(
                        label = "VAT",
                        amount = order.tax ?: 0.0
                    )
                )
            }
        }.trimEnd()
    }


    // =========================================================
    // TOTAL LINE
    // =========================================================

    private fun totalLine48(
        label: String,
        amount: Double,
        negative: Boolean = false,
        currency: Boolean = true
    ): String {

        val amountText =
            if (negative) {

                "-${formatMoney(amount)}"

            } else {

                formatMoney(amount)
            }

        val finalAmount =
            if (currency) {

                "$amountText"

            } else {

                amountText
            }

        val spaces =
            LINE_WIDTH -
                    label.length -
                    finalAmount.length

        return label +
                " ".repeat(
                    maxOf(spaces, 1)
                ) +
                finalAmount
    }


    // =========================================================
    // LABEL + VALUE
    // =========================================================

    private fun labelValueLine(
        label: String,
        value: String
    ): String {

        val left =
            "$label:"

        val available =
            LINE_WIDTH -
                    left.length -
                    1

        val right =
            value
                .replace("\n", " ")
                .take(maxOf(available, 1))

        return left +
                " ".repeat(
                    maxOf(
                        LINE_WIDTH -
                                left.length -
                                right.length,
                        1
                    )
                ) +
                right
    }


    // =========================================================
    // PAYMENT TEXT LINE
    // =========================================================

    private fun totalTextLine48(
        label: String,
        value: String
    ): String {

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
    // MONEY
    // =========================================================

    private fun money(
        value: Double
    ): String {

        return "${formatMoney(value)}"
    }


    private fun formatMoney(
        value: Double
    ): String {

        return "%.2f".format(value)
    }


    // =========================================================
    // QUANTITY
    // =========================================================

    private fun formatQuantity(quantity: Int): String {
        return quantity.toString()
    }


    // =========================================================
    // MODIFIERS
    // =========================================================

    private fun parseModifiers(
        json: String
    ): List<String> {

        return try {

            json
                .removePrefix("[")
                .removeSuffix("]")
                .split(",")
                .map {

                    it.trim()
                        .replace("\"", "")
                        .trim()
                }
                .filter {
                    it.isNotBlank()
                }

        } catch (_: Exception) {

            listOf(json)
        }
    }


    // =========================================================
    // CENTER TEXT
    // =========================================================

    private fun centerText(
        text: String,
        width: Int
    ): String {
        val clean =
            text
                .replace("\n", " ")
                .trim()
                .take(width)

        val pad =
            ((width - clean.length) / 2 - 2)
                .coerceAtLeast(0)

        return " ".repeat(pad) + clean
    }


    // =========================================================
    // FOOTER
    // =========================================================

    private fun buildOutletFooter(
        info: OutletInfo,
        width: Int
    ): String {

        val note =
            info.footerNote
                ?.trim()

        if (note.isNullOrBlank()) {

            return ""
        }

        val wrappedLines =
            wrapText(
                note,
                width
            )

        return buildString {

            wrappedLines.forEach { line ->

                append(
                    centerText(
                        line,
                        width
                    )
                )

                append("\n")
            }
        }.trimEnd()
    }


    // =========================================================
    // TEXT WRAPPING
    // =========================================================

    private fun wrapText(
        text: String,
        width: Int
    ): List<String> {

        if (text.isBlank()) {
            return emptyList()
        }

        val words =
            text
                .split("\\s+".toRegex())
                .filter {
                    it.isNotBlank()
                }

        val lines =
            mutableListOf<String>()

        var currentLine = ""

        for (word in words) {

            // Long word
            if (word.length > width) {

                if (currentLine.isNotBlank()) {

                    lines += currentLine.trim()
                    currentLine = ""
                }

                word.chunked(width).forEach {

                    lines += it
                }

                continue
            }

            val candidate =
                if (currentLine.isBlank()) {

                    word

                } else {

                    "$currentLine $word"
                }

            if (candidate.length > width) {

                if (currentLine.isNotBlank()) {

                    lines += currentLine.trim()
                }

                currentLine = word

            } else {

                currentLine = candidate
            }
        }

        if (currentLine.isNotBlank()) {

            lines += currentLine.trim()
        }

        return lines
    }
}