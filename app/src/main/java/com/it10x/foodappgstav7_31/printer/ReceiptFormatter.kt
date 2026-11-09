package com.it10x.foodappgstav7_31.printer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.util.Log
import com.it10x.foodappgstav7_31.data.PrinterRole
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_31.data.print.OutletInfo
import com.it10x.foodappgstav7_31.printer.billimage.ReceiptBitmapGenerator
import com.it10x.foodappgstav7_31.printer.common.CountryBillFormatter
import com.it10x.foodappgstav7_31.printer.kotImage.KitchenBitmapGenerator
import com.it10x.foodappgstav7_31.ui.sales.SalesUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// -----------------------------
// PRINT MODELS (ONE TRUTH)
// -----------------------------


// -----------------------------
// RECEIPT FORMATTER
// -----------------------------

object ReceiptFormatter {

    private const val LINE_WIDTH = 32
    private const val ALIGN_LEFT = "\u001B\u0061\u0000"
    private const val ALIGN_CENTER = "\u001B\u0061\u0001"



    private const val ESC = "\u001B"

    // Normal size
    private const val TXT_NORMAL = ESC + "!" + "\u0000"

    // Double Height
    private const val TXT_DOUBLE_HEIGHT = ESC + "!" + "\u0010"

    // Double Width
    private const val TXT_DOUBLE_WIDTH = ESC + "!" + "\u0020"

    private const val TXT_FONT_A = ESC + "M" + "\u0000"
    private const val TXT_FONT_B = ESC + "M" + "\u0001"

    // Double Width + Double Height
    private const val TXT_BIG = ESC + "!" + "\u0030"

    // Bold
    private const val TXT_BOLD_ON = ESC + "E" + "\u0001"
    private const val TXT_BOLD_OFF = ESC + "E" + "\u0000"
    // -----------------------------
    // BILLING RECEIPT
    // -----------------------------

    fun billing32(order: PrintOrder, outletInfo: OutletInfo): String {

        val LINE_WIDTH = 32
        Log.d("RECEIPT_FORMATTER", "billing48() called for orderNo=${order.orderNo}")
        val outletHeader = buildOutletHeader(outletInfo, LINE_WIDTH)

        val headerBlock = buildHeaderBlock(order)

        val totalsBlock = buildString {

            append(totalLine("Item Total", order.itemTotal))

            if ((order.deliveryFee ?: 0.0) > 0.0) {
                append(totalLine("Delivery", order.deliveryFee))
            }

            if ((order.discount ?: 0.0) > 0.0) {
                append(totalLine("Discount", order.discount))
            }

            if ((order.tax ?: 0.0) > 0.0) {
                append(totalLine("Tax", order.tax))
            }
        }


        val qrTitleToPrint = if (
           outletInfo?.qrEnabled == true
        ) {
            when {
                order.paymentMode == "UPI" && !outletInfo.upiId.isNullOrBlank() ->
                    outletInfo.upiTitle ?: "Scan & Pay"
                else ->
                    outletInfo.qrTitle
            }
        } else null





        val itemsBlock = if (order.items.isEmpty()) {
            "No items found"
        } else {
            val header =
                "QTY".padEnd(4) +
                        "ITEM".padEnd(16) +
                        "PRICE".padStart(6) +
                        "TOTAL".padStart(6)

            val divider = "-".repeat(LINE_WIDTH)



            val lines = buildString {

                order.items.forEach { item ->

                    val qty = item.quantity.toString().padEnd(4)
                    val name = item.name.take(16).padEnd(16)
                    val price = format(item.price).padStart(6)
                    val total = format(item.subtotal).padStart(6)

                    // 🔹 Main item line (32 chars total)
                    append(qty + name + price + total + "\n")


                    // 🔹 Modifiers (indented, max width safe)
                    if (!item.modifiersJson.isNullOrBlank()) {
                        try {
                            val modifiers = item.modifiersJson
                                .removePrefix("[")
                                .removeSuffix("]")
                                .split(",")
                                .map { it.trim().replace("\"", "") }
                                .filter { it.isNotBlank() }

                            modifiers.forEach { mod ->
                                append("    + ${mod.take(26)}\n")
                            }
                        } catch (_: Exception) {
                            append("    + ${item.modifiersJson.take(26)}\n")
                        }
                    }

                    // 🔹 Note (indented)
                    if (!item.note.isNullOrBlank()) {
                        append("    • ${item.note.take(26)}\n")
                    }
                }
            }


            "$header\n$divider\n$lines"
        }

        return buildString {
            append(ALIGN_CENTER)

            // ✅ PRINT TITLE JUST AFTER QR
            if (!qrTitleToPrint.isNullOrBlank()) {
                append(qrTitleToPrint.uppercase())
                append("\n\n")
            }
            append(ALIGN_LEFT)
            append(
                """
------------------------------
$outletHeader
------------------------------
$headerBlock
----------------------------
$itemsBlock
------------------------------
$totalsBlock
------------------------------
${totalLine("GRAND TOTAL", order.grandTotal)}
------------------------------
${buildOutletFooter(outletInfo, 32)}
Thank You!
""".trimIndent()
            )
        }
    }


    fun billing48(
        order: PrintOrder,
        kotNumberText: String="",
        stewardName: String = "",
        outletInfo: OutletInfo,
        earnedPoints: Int,
    ): String {

        return CountryBillFormatter.billing48(
            order = order,
            outletInfo = outletInfo,
            kotNumberText = kotNumberText,
            stewardName = stewardName,
            earnedPoints = earnedPoints,
        )
    }


    fun billing48IMAGE(
        order: PrintOrder,
        outletInfo: OutletInfo,
        context: Context,
        logo: Bitmap?,
        kotNumberText: String="",
        stewardName: String = "",

    ): Bitmap {

        return ReceiptBitmapGenerator.billing48Image(
            context,
            order,
            outletInfo,
            logo,   // ✅ IMPORTANT: pass here
            kotNumberText,
            stewardName,
        )
    }

    fun billing32IMAGE(
        order: PrintOrder,
        outletInfo: OutletInfo,
        context: Context,
        logo: Bitmap?,
        kotNumberText: String = "",
        stewardName: String = "",
    ): Bitmap {

        // Temporary: use 48-char / 80mm image layout
        return billing48IMAGE(
            order = order,
            outletInfo = outletInfo,
            context = context,
            logo = logo,
            kotNumberText = kotNumberText,
            stewardName = stewardName,
        )
    }


    // -----------------------------
    // KITCHEN RECEIPT
    // -----------------------------

    fun kitchen(items: List<PosKotItemEntity>, title: String = "KITCHEN"): String {

        val itemsBlock = if (items.isEmpty()) {
            "No items"
        } else {
            items.joinToString("\n") {
                "${it.quantity.toString().padEnd(3)} ${it.name}"
            }
        }

        return buildString {
            append(ALIGN_LEFT)
            append(
                """
******** $title ********

------------------------
$itemsBlock
------------------------


""".trimIndent()
            )
        }
    }


    // -----------------------------
    // HEADER LOGIC (IMPORTANT)
    // -----------------------------
    private fun buildHeaderBlock(order: PrintOrder): String {

        val base = mutableListOf<String>()

        // --- Common fields ---
        base.add("Invoice No : ${order.orderNo}")
        base.add("Customer : ${order.customerName.ifBlank { "Walk-in" }}")
        base.add("Date     : ${order.dateTime}")

        when (order.orderType) {

            // -----------------------------
            // DINE-IN ORDERS
            // -----------------------------
            "DINE_IN" -> {
                order.tableNo?.takeIf { it.isNotBlank() }?.let {
                    base.add("Table    : $it")
                }
            }

            // -----------------------------
            // TAKEAWAY ORDERS
            // -----------------------------
            "TAKEAWAY" -> {
                // Only show phone if filled
                order.customerPhone?.takeIf { it.isNotBlank() }?.let {
                    base.add("Phone    : $it")
                }
            }

            // -----------------------------
            // DELIVERY / ONLINE ORDERS
            // -----------------------------
            "DELIVERY", "ONLINE" -> {
                val addressLines = mutableListOf<String>()

                order.dAddressLine1?.takeIf { it.isNotBlank() }?.let { addressLines.add(it) }
                order.dAddressLine2?.takeIf { it.isNotBlank() }?.let { addressLines.add(it) }
                order.dLandmark?.takeIf { it.isNotBlank() }?.let { addressLines.add("Landmark: $it") }

                // City + Zip
                listOfNotNull(order.dCity, order.dZipcode)
                    .joinToString(" ")
                    .takeIf { it.isNotBlank() }
                    ?.let { addressLines.add(it) }

                if (addressLines.isNotEmpty()) {
                    base.add("Address  :")
                    base.addAll(addressLines)
                }

                order.customerPhone?.takeIf { it.isNotBlank() }?.let {
                    base.add("Phone    : $it")
                }
            }
        }

        return base.joinToString("\n")
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



    fun posKitchen(
        sessionKey: String,
        tableName: String,
        orderType: String,
        items: List<PosKotItemEntity>,
        kotNumber: String,
        title: String = "KITCHEN",
    ): String {

        val time = java.text.SimpleDateFormat(
            "HH:mm",
            java.util.Locale.getDefault()
        ).format(java.util.Date())
        val stewardName = items.firstOrNull()?.createdByName ?: ""
        val header = buildString {

            append(TXT_NORMAL)
            append(TXT_BOLD_ON)
            appendLine("******** $title ********")

            appendLine("KOT   : $kotNumber")
            appendLine("Type  : $orderType")
            appendLine("Ref   : $tableName")
            appendLine("Time  : $time")
            appendLine("Stew  : $stewardName")
            appendLine("------------------------")
        }

        val itemsBlock =
            if (items.isEmpty()) {

                "No items\n"

            } else {

                buildString {

                    items.forEach { item ->

                        // =========================================
                        // MAIN ITEM
                        // =========================================

                        append(TXT_NORMAL)
                        append(TXT_BOLD_ON)

                        appendLine(
                            "${item.quantity.toString().padEnd(3)} ${item.name}"
                        )


                        // =========================================
                        // MODIFIERS
                        // =========================================

                        if (item.modifiersJson.isNotBlank()) {

                            try {

                                val modifiers =
                                    ModifierJsonHelper.fromJson(
                                        item.modifiersJson
                                    )

                                modifiers.forEach { group ->

                                    // Modifier group name
                                    append(TXT_NORMAL)
                                    append(TXT_BOLD_ON)

//                                    appendLine(
//                                        "   ${group.groupName}:"
//                                    )

                                    // Modifier items
                                    group.items.forEach { modifierItem ->

                                        append(TXT_NORMAL)
                                        append(TXT_BOLD_OFF)

                                        appendLine("+ ${modifierItem.name}")
                                    }
                                }

                            } catch (e: Exception) {

                                Log.e(
                                    "KOT_PRINT",
                                    "Failed to parse modifiersJson: ${item.modifiersJson}",
                                    e
                                )

                                // Fallback
                                appendLine(
                                    "      + ${item.modifiersJson}"
                                )
                            }
                        }


                        // =========================================
                        // NOTE
                        // =========================================

                        if (item.note.isNotBlank()) {

                            append(TXT_NORMAL)
                            append(TXT_BOLD_OFF)

                            appendLine(
                                "      NOTE: ${item.note}"
                            )
                        }

                        appendLine()
                    }
                }
            }

        return buildString {

            append(ALIGN_LEFT)
            append(header)
            append(itemsBlock)

            appendLine("------------------------")
        }
    }



    fun posKitchen_old_without_MODIFIER(
        sessionKey: String,
        tableName:String,
        orderType: String,
        items: List<PosKotItemEntity>,
        kotNumber: String,
        title: String = "KITCHEN",
    ): String {

        val time = java.text.SimpleDateFormat(
            "HH:mm",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        val header = buildString {

            append(TXT_NORMAL)
            append(TXT_BOLD_ON)
            appendLine("******** $title ********")



            appendLine("KOT   : $kotNumber")
            appendLine("Type  : $orderType")
            appendLine("Ref   : $tableName")
            appendLine("Time  : $time")
            appendLine("------------------------")
        }

        val itemsBlock =
            if (items.isEmpty()) {

                "No items\n"

            } else {

                buildString {

                    items.forEach { item ->

                        // Main Item
                        append(TXT_NORMAL)
                        append(TXT_BOLD_ON)
                        appendLine("${item.quantity.toString().padEnd(3)} ${item.name}")


                        // Modifiers
                        if (item.modifiersJson.isNotBlank()) {

                            try {

                                val modifiers = item.modifiersJson
                                    .removePrefix("[")
                                    .removeSuffix("]")
                                    .split(",")
                                    .map { it.trim().replace("\"", "") }
                                    .filter { it.isNotBlank() }

                                modifiers.forEach { modifier ->
                                    appendLine("      + $modifier")
                                }

                            } catch (_: Exception) {

                                appendLine("      + ${item.modifiersJson}")

                            }
                        }

                        // Note
                        if (item.note.isNotBlank()) {
                            appendLine("      NOTE: ${item.note}")
                        }

                        appendLine()
                    }
                }
            }

        return buildString {
            append(ALIGN_LEFT)
            append(header)
            append(itemsBlock)
            appendLine("------------------------")
        }
    }





    private fun totalLine48(label: String, amount: Double): String {
        val formatted = format(amount)
        // label left-aligned, amount right-aligned to total 48 characters
        val space = 48 - label.length - formatted.length
        return label + " ".repeat(if (space > 0) space else 1) + formatted
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
            info.gstVatNumber?.let { lines += "GST: $it" }
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
            info.gstVatNumber?.let { lines += "GST: $it" }
            // info.footerNote?.let { lines += it.take(width) }
        }

        return lines.joinToString("\n")
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

    fun salesReport(
        state: SalesUiState,
        outletInfo: OutletInfo,
        width: Int
    ): String {

        val divider = "-".repeat(width)

        fun line(label: String, value: Double): String {
            val formatted = "%.2f".format(value)
            val space = width - label.length - formatted.length
            return label + " ".repeat(if (space > 0) space else 1) + formatted
        }

        val header = buildOutletHeader(outletInfo, width)

        return buildString {
            append("\u001B\u0061\u0000") // align left
            append(header + "\n")
            append(divider + "\n")
            append("SALES REPORT\n")
            append(divider + "\n")

            append("From : ${java.util.Date(state.from)}\n")
            append("To   : ${java.util.Date(state.to)}\n")
            append(divider + "\n")

            append(line("Total Sales", state.totalSales) + "\n")
            append(line("Tax", state.taxTotal) + "\n")
            append(line("Discount", state.discountTotal) + "\n")

            append(divider + "\n")

            state.paymentBreakup.forEach { (type, amount) ->
                append(line(type, amount) + "\n")
            }

            append(divider + "\n")
            append(line("Food", state.foodTotal) + "\n")
            append(line("Beverages", state.beveragesTotal) + "\n")
            append(line("Wine", state.wineTotal) + "\n")

            append(divider + "\n\n")
        }
    }


    fun salesCategorySummary(
        category: String,
        totalQty: Int,
        totalAmount: Double,
        outletInfo: OutletInfo,
        width: Int
    ): String {

        val divider = "-".repeat(width)

        fun line(label: String, value: String): String {
            val safeLabel = label.take(width - value.length - 1)
            val space = width - safeLabel.length - value.length
            return safeLabel + " ".repeat(if (space > 0) space else 1) + value
        }



        val header = buildOutletHeader(outletInfo, width)

        return buildString {

            append("\u001B\u0061\u0000")
            append(header + "\n")
            append(divider + "\n")

            append("CATEGORY SUMMARY\n")
            append(category.uppercase() + "\n")
            append(divider + "\n")

            append(line("Total Qty", totalQty.toString()) + "\n")
            append(line("Total Amount", "₹ %.2f".format(totalAmount)) + "\n")

            append(divider + "\n\n")
        }
    }

    fun salesBySingleCategory(
        category: String,
        items: Map<String, Pair<Int, Double>>,
        outletInfo: OutletInfo,
        width: Int,
        fromMillis: Long,
        toMillis: Long,
        printMillis: Long = System.currentTimeMillis()
    ): String {

        val divider = "-".repeat(width)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun line(label: String, value: String): String {
            val safeLabel = label.take(width - value.length - 1)
            val space = width - safeLabel.length - value.length
            return safeLabel + " ".repeat(if (space > 0) space else 1) + value
        }

        val header = buildOutletHeader(outletInfo, width)

        return buildString {

            var totalQty = 0
            var totalAmount = 0.0

            append("\u001B\u0061\u0001") // center
            append(header + "\n")
            append(divider + "\n")

            append("CATEGORY REPORT\n")
            append(category.uppercase() + "\n")

            append(divider + "\n")

            append("From : ${dateFormat.format(Date(fromMillis))}\n")
            append("To   : ${dateFormat.format(Date(toMillis))}\n")
            append("Printed : ${dateFormat.format(Date(printMillis))}\n")

            append(divider + "\n")

            append("\u001B\u0061\u0000") // left

            if (items.isEmpty()) {
                append("No sales data\n")
            } else {

                items.forEach { (itemName, data) ->

                    val qty = data.first
                    val total = data.second
                    val rate = if (qty > 0) total / qty else 0.0

                    totalQty += qty
                    totalAmount += total

                    append(itemName.take(width) + "\n")

                    val qtyRate = "$qty x %.2f".format(rate)
                    val totalStr = "%.2f".format(total)

                    append(line(qtyRate, totalStr) + "\n")
                    append("\n")
                }
            }

            append(divider + "\n")

            // ✅ TOTAL SECTION (NEW)
            append(line("TOTAL QTY", totalQty.toString()) + "\n")
            append(line("TOTAL AMOUNT", "%.2f".format(totalAmount)) + "\n")

            append(divider + "\n\n")
        }
    }


    fun salesCategorySummary(
        category: String,
        totalQty: Int,
        totalAmount: Double,
        info: OutletInfo,
        width: Int,
        fromMillis: Long,
        toMillis: Long,
        printMillis: Long = System.currentTimeMillis()
    ): String {

        val divider = "-".repeat(width)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun line(label: String, value: String): String {
            val safeLabel = label.take(width - value.length - 1)
            val space = width - safeLabel.length - value.length
            return safeLabel + " ".repeat(if (space > 0) space else 1) + value
        }

        return buildString {

            append("\u001B\u0061\u0001") // center

            append(info.outletName + "\n")
            append(divider + "\n")

            append("CATEGORY SUMMARY\n")
            append(category.uppercase() + "\n")

            append(divider + "\n")

            append("From : ${dateFormat.format(Date(fromMillis))}\n")
            append("To   : ${dateFormat.format(Date(toMillis))}\n")
            append("Printed : ${dateFormat.format(Date(printMillis))}\n")

            append(divider + "\n")

            append("\u001B\u0061\u0000") // left

            append(line("Total Qty", totalQty.toString()) + "\n")
            append(line("Total Amount", "%.2f".format(totalAmount)) + "\n")

            append(divider + "\n\n")
        }
    }

    fun salesProductSummary(
        product: String,
        qty: Int,
        amount: Double,
        info: OutletInfo,
        width: Int,
        fromMillis: Long,
        toMillis: Long,
        printMillis: Long
    ): String {

        val sdfDate = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
        val sdfDateTime = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", java.util.Locale.getDefault())

        val fromDate = sdfDate.format(java.util.Date(fromMillis))
        val toDate = sdfDate.format(java.util.Date(toMillis))
        val printTime = sdfDateTime.format(java.util.Date(printMillis))

        val divider = "-".repeat(width)

        return buildString {

            append("\u001B\u0061\u0000")

            // ✅ Outlet name
            append(centerText(info.outletName, width) + "\n")
            append(divider + "\n")

            append("PRODUCT REPORT\n")
            append(divider + "\n")

            // ✅ Date range
            append("From : $fromDate\n")
            append("To   : $toDate\n")
            append("Print: $printTime\n")

            append(divider + "\n")

            append("Product : $product\n")
            append("Qty     : $qty\n")
            append("Amount  : ₹${"%.2f".format(amount)}\n")

            append(divider + "\n")
            append(centerText("Thank You", width))
            append("\n\n")
        }
    }


    fun salesCategoryProductList(
        category: String,
        items: List<com.it10x.foodappgstav7_31.ui.reports.model.ProductReportItem>,
        outletInfo: OutletInfo,
        width: Int,
        fromMillis: Long,
        toMillis: Long,
        printMillis: Long = System.currentTimeMillis()   // ✅ ADD THIS
    ): String {

        val divider = "-".repeat(width)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun line(label: String, value: String): String {
            val safeLabel = label.take(width - value.length - 1)
            val space = width - safeLabel.length - value.length
            return safeLabel + " ".repeat(if (space > 0) space else 1) + value
        }

        val totalAmount = items.sumOf { it.total }

        return buildString {

            append("\u001B\u0061\u0001") // center align

            // ✅ OUTLET NAME
            append(outletInfo.outletName + "\n")

            append(divider + "\n")

            append("CATEGORY REPORT\n")
            append(category.uppercase() + "\n")

            append(divider + "\n")

            // ✅ DATE RANGE
            append("From : ${dateFormat.format(Date(fromMillis))}\n")
            append("To   : ${dateFormat.format(Date(toMillis))}\n")

            // ✅ PRINT TIME
            append("Printed : ${dateFormat.format(Date(printMillis))}\n")

            append(divider + "\n")

            append("\u001B\u0061\u0000") // left align

            if (items.isEmpty()) {
                append("No data\n")
            } else {

                items.forEach { item ->

                    append(item.name.take(width) + "\n")

                    val rate = if (item.qty > 0) item.total / item.qty else 0.0
                    val qtyRate = "${item.qty} x %.2f".format(rate)
                    val totalStr = "%.2f".format(item.total)

                    append(line(qtyRate, totalStr) + "\n")
                    append("\n")
                }
            }

            append(divider + "\n")
            append(line("TOTAL", "%.2f".format(totalAmount)) + "\n")
            append(divider + "\n\n")
        }
    }

    fun totalSalesReport(
        beforeDiscount: Double,
        discount: Double,
        afterDiscount: Double,
        tax: Double,
        info: OutletInfo,
        width: Int,
        fromMillis: Long,
        toMillis: Long,
        printMillis: Long
    ): String {

        val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        return buildString {

            appendLine(center(info.outletName, width))
            appendLine("-".repeat(width))

            appendLine(center("TOTAL SALES REPORT", width))
            appendLine("-".repeat(width))

            appendLine("From : ${df.format(Date(fromMillis))}")
            appendLine("To   : ${df.format(Date(toMillis))}")
            appendLine("Printed : ${df.format(Date(printMillis))}")

            appendLine("-".repeat(width))

            appendLine(leftRight("Before Discount", formatAmount(beforeDiscount), width))
            appendLine(leftRight("Discount", formatAmount(discount), width))
            appendLine(leftRight("After Discount", formatAmount(afterDiscount), width))
            appendLine(leftRight("Tax", formatAmount(tax), width))

            appendLine("-".repeat(width))
            appendLine("\n\n")
        }
    }

    fun center(text: String, width: Int): String {
        val padding = (width - text.length) / 2
        return if (padding > 0) {
            " ".repeat(padding) + text
        } else text
    }
    fun formatAmount(value: Double): String {
        return "%.2f".format(value)
    }
    fun leftRight(left: String, right: String, width: Int): String {
        val space = width - left.length - right.length
        return if (space > 0) {
            left + " ".repeat(space) + right
        } else {
            "$left $right" // fallback if overflow
        }
    }

    fun totalSalesSummary(
        before: Double,
        discount: Double,
        after: Double,
        tax: Double,
        info: OutletInfo,
        width: Int,
        fromMillis: Long,
        toMillis: Long,
        printMillis: Long
    ): String {

        val divider = "-".repeat(width)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun line(label: String, value: String): String {
            val labelWidth = width / 2
            return label.padEnd(labelWidth, ' ') + value
        }

        return buildString {

            append(centerText(info.outletName, width) + "\n")
            append(divider + "\n")

            append(centerText("TOTAL SALES REPORT", width) + "\n")

            append(divider + "\n")

            append("From : ${dateFormat.format(Date(fromMillis))}\n")
            append("To   : ${dateFormat.format(Date(toMillis))}\n")
            append("Printed : ${dateFormat.format(Date(printMillis))}\n")

            append(divider + "\n")

            append(line("Before Discount", "%.2f".format(before)) + "\n")
            append(line("Discount", "%.2f".format(discount)) + "\n")
            append(line("After Discount", "%.2f".format(after)) + "\n")
            append(line("Tax", "%.2f".format(tax)) + "\n")

            append(divider + "\n\n")
        }
    }


    fun salesFullReport(
        state: SalesUiState,
        info: OutletInfo,
        width: Int,
        printMillis: Long
    ): String {

        val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        return buildString {

            appendLine(centerText(info.outletName, width))
            appendLine("-".repeat(width))

            appendLine(centerText("SALES REPORT", width))
            appendLine("-".repeat(width))

            appendLine("From : ${df.format(Date(state.from))}")
            appendLine("To   : ${df.format(Date(state.to))}")
            appendLine("Printed : ${df.format(Date(printMillis))}")

            appendLine("-".repeat(width))

            // ---------------- SUMMARY ----------------
            appendLine(centerText("SUMMARY", width))
            appendLine("-".repeat(width))

            appendLine(leftRight("Before Discount", formatAmount(state.totalBeforeDiscount), width))
            appendLine(leftRight("Discount", formatAmount(state.discountTotal), width))
            appendLine(leftRight("Total Sales", formatAmount(state.totalSales), width))
            appendLine(leftRight("Tax", formatAmount(state.taxTotal), width))

            appendLine("-".repeat(width))

            appendLine(leftRight("Received", formatAmount(state.receivedTotal), width))
            appendLine(leftRight("Credit", formatAmount(state.creditTotal), width))

            appendLine("-".repeat(width))

            // ---------------- PAYMENT ----------------
            if (state.paymentBreakup.isNotEmpty()) {
                appendLine(centerText("PAYMENT", width))
                appendLine("-".repeat(width))

                state.paymentBreakup.forEach { (type, amount) ->
                    appendLine(leftRight(type, formatAmount(amount), width))
                }

                appendLine("-".repeat(width))
            }

            // ---------------- GROUPED ----------------
            appendLine(centerText("GROUPED SALES", width))
            appendLine("-".repeat(width))

            appendLine(leftRight("Food", formatAmount(state.foodTotal), width))
            appendLine(leftRight("Beverages", formatAmount(state.beveragesTotal), width))
            appendLine(leftRight("Wine", formatAmount(state.wineTotal), width))

            appendLine("-".repeat(width))

            // ---------------- CATEGORY ----------------
            if (state.categorySales.isNotEmpty()) {

                appendLine(centerText("CATEGORY", width))
                appendLine("-".repeat(width))

                state.categorySales.forEach { (category, data) ->

                    val qty = data.first
                    val amount = data.second

                    appendLine(category)
                    appendLine(leftRight("Qty", qty.toString(), width))
                    appendLine(leftRight("Amt", formatAmount(amount), width))
                    appendLine("-".repeat(width))
                }
            }

            append("\n\n")
        }
    }


    fun categoryWiseSalesReport(
        categorySales: Map<String, Pair<Int, Double>>,
        info: OutletInfo,
        width: Int,
        fromMillis: Long,
        toMillis: Long,
        printMillis: Long
    ): String {

        val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        return buildString {

            appendLine(centerText(info.outletName, width))
            appendLine("-".repeat(width))

            appendLine(centerText("CATEGORY SALES REPORT", width))
            appendLine("-".repeat(width))

            appendLine("From : ${df.format(Date(fromMillis))}")
            appendLine("To   : ${df.format(Date(toMillis))}")
            appendLine("Printed : ${df.format(Date(printMillis))}")

            appendLine("-".repeat(width))

            categorySales.forEach { (category, data) ->
                val qty = data.first
                val amount = data.second

                appendLine(category)
                appendLine(leftRight("Qty", qty.toString(), width))
                appendLine(leftRight("Amount", formatAmount(amount), width))
                appendLine("-".repeat(width))
            }

            appendLine("\n\n")
        }
    }




    fun posKitchen_IMAGE(
        context: Context,
        sessionKey: String,
        orderType: String,
        items: List<PosKotItemEntity>,
        kotNumber: String,
    ): Bitmap {

        val width = 384
        val padding = 16
        val lineHeight = 36

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isAntiAlias = true
        }

        val boldPaint = Paint(paint).apply {
            typeface = Typeface.DEFAULT_BOLD
        }

        val boxPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        val bitmap = Bitmap.createBitmap(width, 1200, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.WHITE)

        var y = padding + 20

        // =========================
        // HEADER BOX (K.No + Date)
        // =========================
        val boxHeight = 60

        canvas.drawRect(
            padding.toFloat(),
            y.toFloat(),
            (width - padding).toFloat(),
            (y + boxHeight).toFloat(),
            boxPaint
        )

        canvas.drawText("K.No.", padding + 10f, y + 35f, boldPaint)
        canvas.drawText(kotNumber, padding + 120f, y + 35f, paint)

        val time = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Date: $time", width - 220f, y + 35f, paint)

        y += boxHeight + 10

        // =========================
        // TABLE INFO
        // =========================
        canvas.drawText("Table No : $orderType", padding.toFloat(), y.toFloat(), boldPaint)
        y += lineHeight

        canvas.drawText("Steward : NONAME", padding.toFloat(), y.toFloat(), boldPaint)
        y += lineHeight

        // =========================
        // HEADER ROW
        // =========================
        canvas.drawLine(padding.toFloat(), y.toFloat(), (width - padding).toFloat(), y.toFloat(), boxPaint)
        y += 10

        canvas.drawText("Item Name", padding.toFloat(), y.toFloat(), boldPaint)
        canvas.drawText("Qty", width - 70f, y.toFloat(), boldPaint)

        y += 10
        canvas.drawLine(padding.toFloat(), y.toFloat(), (width - padding).toFloat(), y.toFloat(), boxPaint)
        y += lineHeight

        // =========================
        // ITEMS
        // =========================
        items.forEach { item ->

            val words = item.name.split(" ")
            var line = ""

            words.forEach { word ->
                val testLine = "$line $word"
                if (paint.measureText(testLine) > width - 100) {
                    canvas.drawText(line.trim(), padding.toFloat(), y.toFloat(), paint)
                    y += lineHeight
                    line = word
                } else {
                    line += " $word"
                }
            }

            // last line
            canvas.drawText(line.trim(), padding.toFloat(), y.toFloat(), paint)

            // qty (only on first line visually aligned)
            canvas.drawText(
                String.format("%.3f", item.quantity),
                width - 70f,
                y.toFloat(),
                boldPaint
            )

            y += lineHeight

            // note (if any)
            if (!item.note.isNullOrEmpty()) {
                canvas.drawText(
                    "• ${item.note}",
                    padding + 20f,
                    y.toFloat(),
                    paint
                )
                y += lineHeight
            }

            canvas.drawLine(
                padding.toFloat(),
                y.toFloat(),
                (width - padding).toFloat(),
                y.toFloat(),
                boxPaint
            )

            y += 10
        }

        return Bitmap.createBitmap(bitmap, 0, 0, width, y + 20)
    }



}//END OF FILE
