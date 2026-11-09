package com.it10x.foodappgstav7_31.printer.billimage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import com.it10x.foodappgstav7_31.data.PrinterRole
import com.it10x.foodappgstav7_31.data.print.OutletInfo
import com.it10x.foodappgstav7_31.printer.PrintOrder
import com.it10x.foodappgstav7_31.printer.utils.QrUtils
import com.it10x.foodappgstav7_31.utils.tax.TaxMode

object ReceiptBitmapGenerator {

    //================================================
    // Printer Constants
    //================================================

    // 80mm printer width
    private const val RECEIPT_WIDTH = 576

    // Original receipt layout was designed for 384px
    private const val BASE_WIDTH = 384f

    // Scale factor
    private const val SCALE = RECEIPT_WIDTH / BASE_WIDTH

    // Receipt margins
    private const val LEFT = 12f * SCALE
    private const val RIGHT = 372f * SCALE

    private const val START_Y = 25f




    //================================================
    // Public API
    //================================================

    fun billing48Image(
        context: Context,
        order: PrintOrder,
        outletInfo: OutletInfo,
        logo: Bitmap?,
        kotNumberText: String,
        stewardName: String,
    ): Bitmap {

        val bitmap = Bitmap.createBitmap(
            RECEIPT_WIDTH,
            3200,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.WHITE)

        val drawer = ReceiptDrawer(canvas)

        //==============================
// LOGO
//==============================
        drawer.drawLogo(logo)
        // Header
        drawer.drawHeader(order, outletInfo)

        // Order Info
        drawer.drawOrderInfo(order, kotNumberText, stewardName)

        // Table Header
        drawer.drawItemsHeader()

        // Items
        drawer.drawItems(order)

        // ✅ Totals + GST + RoundOff (ALL INSIDE)
        drawer.drawAmountSummay(order,outlet= outletInfo)

        drawer.drawDiscountSummary(order )

        drawer.drawGrandTotalBox(order)

        // ✅ QR Bitmap (ADD THIS BLOCK HERE)
        val qrBitmap = QrUtils.loadSavedQr(context)
           // if (role != PrinterRole.KITCHEN && outletInfo.qrEnabled == true) {
              //  QrUtils.loadSavedQr(context)
//            } else {
//                null
//            }



        drawer.drawQrCode(qrBitmap)

        val finalHeight = drawer.y.toInt() + 20

        val finalBitmap = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            finalHeight
        )

        bitmap.recycle()

        return finalBitmap

//        return Bitmap.createBitmap(
//            bitmap,
//            0,
//            0,
//            bitmap.width,
//            finalHeight
//        )
    }

    //================================================
    // Receipt Drawer
    //================================================
    private class ReceiptDrawer(
        private val canvas: Canvas
    )
    {

        var y = START_Y

//================================================
// Layout
//================================================

        private val printableWidth = RECEIPT_WIDTH.toFloat()

        // Margins
        private val leftMargin = 16f
        private val rightMargin = printableWidth - 16f

        // Column separators
        private val line1 = 255f    // End of Item Name
        private val line2 = 363f    // End of Rate
        private val line3 = 430f   // End of Qty

        // Item column width (used for wrapping)
        private val itemRight = line1

        // Text positions
        private val xItem = leftMargin + 8f

        // Right-aligned numeric columns
        private val xRate = line2 - 12f
        private val xQty = line3 - 12f
        private val xAmount = rightMargin - 12f

//================================================
// Paints
//================================================

//        private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//            color = Color.BLACK
//            textSize = 34f
//            textAlign = Paint.Align.CENTER
//            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
//            isFakeBoldText = true
//        }

        private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 38f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isFakeBoldText = true
        }

        private val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
        }

        private val boldPaintLarge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 33f
            typeface = Typeface.DEFAULT_BOLD
        }

        private val boldPaintItem = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
        }

        private val normalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 26f
            typeface = Typeface.DEFAULT
        }

        private val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 25f
            textAlign = Paint.Align.CENTER
        }

        private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 2f
        }

        private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }




//================================================
// Header
//================================================

        fun drawHeader(
            order: PrintOrder,
            outletInfo: OutletInfo
        ) {

            // Top padding
            y += 17f

            // Restaurant Name
            drawCenter(
                outletInfo.outletName.uppercase(),
                titlePaint
            )

            y += 5f

            // ---------- Address ----------

            val fullAddress = listOf(
                outletInfo.addressLine1,
                outletInfo.addressLine2,
                outletInfo.addressLine3
            )
                .filter { !it.isNullOrBlank() }
                .joinToString(", ")

            if (fullAddress.isNotBlank()) {

                val maxWidth = RIGHT - LEFT - 20f

                val lines = wrapText(
                    fullAddress,
                    maxWidth,
                    smallPaint
                )

                lines.forEach { line ->
                    drawCenter(
                        line,
                        smallPaint
                    )
                    y += 2f
                }
            }

            // Phone
            // ---------- Phones ----------

            val phoneText = listOf(
                outletInfo.phone,
                outletInfo.phone2
            )
                .filter { !it.isNullOrBlank() }
                .joinToString("   |   ") { "☎️ $it" }
//                .joinToString("   |   ") { "☎ $it" }
                //.joinToString("   |   ") { "📱 $it" }

            if (phoneText.isNotBlank()) {

                val maxWidth = RIGHT - LEFT - 20f

                val lines = wrapText(
                    phoneText,
                    maxWidth,
                    smallPaint
                )

                lines.forEach { line ->
                    drawCenter(
                        line,
                        smallPaint
                    )
                    y += 2f
                }
            }

            // GST
            if (!outletInfo.gstVatNumber.isNullOrBlank()) {
                drawCenter(
                    "GSTIN : ${outletInfo.gstVatNumber}",
                    smallPaint
                )
                y += 1f
            }
            // FSSAI
//            if (!outletInfo.fssaiNumber.isNullOrBlank()) {
//                drawCenter(
//                    "FSSAI : ${outletInfo.fssaiNumber}",
//                    smallPaint
//                )
//                y += 1f
//            }
              y-=5f
            // Top divider
            canvas.drawLine(
                LEFT,
                y,
                RIGHT,
                y,
                linePaint
            )

            // Padding above title
            y += 35f

            // TAX INVOICE
            drawCenter(
                "TAX INVOICE",
                Paint(boldPaint).apply {
                    textAlign = Paint.Align.CENTER
                }
            )

            y += 10f

            // TAX INVOICE
            val cleanOrderType = order.orderType?.replace("_", " ")
            drawCenter(
                "${cleanOrderType}",
                Paint(boldPaintLarge).apply {
                    textAlign = Paint.Align.CENTER
                }
            )

            // Padding below title
            y -= 2f

            // Bottom divider
            canvas.drawLine(
                LEFT,
                y-20f,
                RIGHT,
                y-20f,
                linePaint
            )

            // Space before next section
            y += 25f
        }
        //================================================
// Order Details
//================================================

        fun drawOrderInfo(
            order: PrintOrder, kotNumberText: String,stewardName:String,
        ) {

            drawLeft(
                "Invoice No : ${order.orderNo}",
                leftMargin,
                normalPaint
            )

            drawRight(
                order.dateTime,
                rightMargin,
                normalPaint
            )

            y += 40f

            drawLeft(
                "Table : ${order.tableNo ?: "-"}",
                leftMargin,
                normalPaint
            )

            drawRight(
                "Steward : $stewardName",
                rightMargin - 50f,
                normalPaint
            )
            y += 40f

            drawLeft(
                "KOT(s) : ${kotNumberText}",
                leftMargin,
                normalPaint
            )

            y += 25f
        }

//================================================
// Item Header
//================================================

        fun drawItemsHeader() {

            //====================================
            // Header Box
            //====================================

            val headerPaint = Paint(boldPaint).apply {
                textSize = 22f          // Smaller text
                textAlign = Paint.Align.CENTER
            }

            val top = y
            val boxHeight = 52f         // Taller box for more padding
            val bottom = top + boxHeight

            // Outer Rectangle
            canvas.drawRect(
                leftMargin,
                top,
                rightMargin,
                bottom,
                boxPaint
            )

            // Vertical Separators
            canvas.drawLine(
                line1,
                top,
                line1,
                bottom,
                linePaint
            )

            canvas.drawLine(
                line2,
                top,
                line2,
                bottom,
                linePaint
            )

            canvas.drawLine(
                line3,
                top,
                line3,
                bottom,
                linePaint
            )

            //------------------------------------
            // Text Position (center vertically)
            //------------------------------------

            val textY =
                top + (boxHeight / 2f) + (headerPaint.textSize / 3f)

            //------------------------------------
// COLUMN CENTERS (DYNAMIC)
//------------------------------------

            val itemCenter = (leftMargin + line1) / 2f
            val rateCenter = (line1 + line2) / 2f
            val qtyCenter = (line2 + line3) / 2f
            val amountCenter = (line3 + rightMargin) / 2f

//------------------------------------
// DRAW TEXT (NO MANUAL OFFSETS)
//------------------------------------

            canvas.drawText(
                "Item Name",
                itemCenter,
                textY,
                headerPaint
            )

            canvas.drawText(
                "Rate",
                rateCenter,
                textY,
                headerPaint
            )

            canvas.drawText(
                "Qty",
                qtyCenter,
                textY,
                headerPaint
            )

            canvas.drawText(
                "Amt",
                amountCenter,
                textY,
                headerPaint
            )

            // Space below the box
            y = bottom + 39f

        }
        //================================================
// Draw All Items
//================================================



        fun drawItems(
            order: PrintOrder
        ) {

            order.items.forEach { item ->

                drawSingleItem(
                    itemName = item.name,          // Normal case
                    rate = item.price,
                    qty = item.quantity.toDouble(),
                    amount = item.subtotal
                )


                //---------------- Modifiers ----------------

                Log.d(
                    "PRINT_TOTALS",
                    "Show Item Tax = ${order.itemTax > 0}"
                )

                if (!item.modifiersJson.isNullOrBlank()) {

                    val modifiers =
                        ModifierJsonHelper.fromJson(item.modifiersJson)

                    modifiers.forEach { group ->

                        group.items.forEach { modifierItem ->

                            if (modifierItem.name.isNotBlank()) {

                                drawModifier(
                                    modifierItem.name
                                )
                            }
                        }
                    }
                }


                //---------------- Notes ----------------

                if (!item.note.isNullOrBlank()) {

                    drawNote(item.note)

                }

                // Space between products
                y += 12f
            }

            // Divider before totals only
            y-=10f
            canvas.drawLine(
                leftMargin,
                y,
                rightMargin,
                y,
                linePaint
            )

            y += 5f
        }

        //================================================
// Draw Single Item
//================================================

        private fun drawSingleItem(
            itemName: String,
            rate: Double,
            qty: Double,
            amount: Double
        ) {

            val maxWidth = itemRight - xItem - 10f

            val lines = wrapText(
                itemName.uppercase(),
                maxWidth,
                boldPaintItem
            )

            // ---------- First Line ----------

            canvas.drawText(
                lines.first(),
                xItem,
                y,
                boldPaintItem
            )

            drawRight(
                String.format("%.2f", rate),
                xRate,
                boldPaintItem
            )

            drawRight(
                qty.toInt().toString(),
                xQty,
                boldPaint
            )

            drawRight(
                String.format("%.2f", amount),
                xAmount,
                boldPaintItem
            )

            // ---------- Remaining Lines ----------

            if (lines.size > 1) {

                lines.drop(1).forEach { line ->

                    y += 24f          // spacing between wrapped lines

                    canvas.drawText(
                        line,
                        xItem,
                        y,
                        boldPaintItem
                    )
                }
            }

            // Gap after item
            y += 28f
        }

        private fun drawSingleItem1(
            itemName: String,
            rate: Double,
            qty: Double,
            amount: Double
        ) {

            val maxWidth = itemRight - xItem - 10f

            val lines = wrapText(
                itemName.uppercase(),
                maxWidth,
                boldPaintItem
            )

            // First line

            canvas.drawText(
                lines.first(),
                xItem,
                y,
                boldPaintItem, //normalPaint,//
            )


            drawRight(
                String.format("%.2f", rate),
                xRate,
                boldPaintItem
            )

            drawRight(
                qty.toInt().toString(),
                xQty,
                boldPaint
            )

            drawRight(
                String.format("%.2f", amount),
                xAmount,
                boldPaintItem
            )

            // Remaining wrapped lines

//            if (lines.size > 1) {
//
//                lines.drop(1).forEach { line ->
//
//                    y += 30f
//
//                    canvas.drawText(
//                        line,
//                        xItem,
//                        y,
//                        boldPaint
//                    )
//
//                }
//
//            }

            y += 36f

            // Row separator

//            canvas.drawLine(
//                leftMargin,
//                y,
//                rightMargin,
//                y,
//                linePaint
//            )

            y += 1f
        }

        // ============================
        //  TOTALS
        // ============================

        fun drawAmountSummay_old(order: PrintOrder) {

            y += 30f
//            drawDivider()

            val  itemTotal = order.itemTotal

            //----------------------------------
            // ITEM TOTAL
            //----------------------------------
            drawLabelValue("Item Total", itemTotal)

            //----------------------------------
            // DISCOUNT
            //----------------------------------
            if (order.discount > 0) {
                drawLabelValue(
                    "Discount",
                    -order.discount
                )
            }

            //----------------------------------
            // DELIVERY
            //----------------------------------
          //  val deliveryTotal = order.deliveryFee //+ order.deliveryTax
            val deliveryTotal = order.deliveryFee
            if (deliveryTotal > 0) {
                drawLabelValue(
                    "Delivery Charge",
                    deliveryTotal
                )
            }


            val taxableAmount = order.itemTotal - order.discount
            //----------------------------------
            // GST CALCULATION (AUTO SPLIT)
            //----------------------------------
            val cgst = order.tax / 2
            val sgst = order.tax / 2

            //----------------------------------
            // GST CALCULATION (AUTO %)
            //----------------------------------


//            val totalGstPercent =
//                if (taxableAmount > 0)
//                    (order.tax / taxableAmount) * 100
//                else 0.0

//            val cgstPercent = 2.5// totalGstPercent / 2
//            val sgstPercent = 2.5//totalGstPercent / 2
//            val itemTax = order.itemTax
             val deliveryTax = order.deliveryTax

//            val cgst = itemTax / 2
//            val sgst = itemTax / 2
            val cgstPercent = order.tax / 2
            val sgstPercent = order.tax / 2


          //  drawLabelValue("Taxable Amount", taxableAmount)
            //----------------------------------
            // CGST
            //----------------------------------
            drawGSTLine(
                "Central GST ${"%.1f".format(cgstPercent)}% on",
                taxableAmount,
                cgst
            )

            //----------------------------------
            // SGST
            //----------------------------------
            drawGSTLine(
                "State GST ${"%.1f".format(sgstPercent)}% on",
                taxableAmount,
                sgst
            )

            //----------------------------------
            // ROUND OFF
            //----------------------------------
//            val calculatedTotal =
//                itemTotal +
//                        order.tax +
//                        deliveryTotal -
//                        order.discount
            val calculatedTotal = order.grandTotal

            val roundedTotal = kotlin.math.round(calculatedTotal)
            val roundOff = roundedTotal - calculatedTotal

            if (roundOff != 0.0) {
                drawLabelValue("Round Off", roundOff)
            }




            y -= 10f



        }
        fun drawAmountSummay(order: PrintOrder, outlet: OutletInfo) {
            Log.d("PRINT_TOTALS", "----------------------------")
            Log.d("PRINT_TOTALS", "itemTotal      = ${order.itemTotal}")
            Log.d("PRINT_TOTALS", "discount       = ${order.discount}")
            Log.d("PRINT_TOTALS", "itemTax        = ${order.itemTax}")
            Log.d("PRINT_TOTALS", "deliveryFee    = ${order.deliveryFee}")
            Log.d("PRINT_TOTALS", "deliveryTax    = ${order.deliveryTax}")
            Log.d("PRINT_TOTALS", "taxTotal       = ${order.tax}")
            Log.d("PRINT_TOTALS", "grandTotal     = ${order.grandTotal}")
            Log.d("PRINT_TOTALS", "taxType        = ${outlet.taxType}")
            y += 30f

            //----------------------------------
            // ITEM TOTAL
            //----------------------------------
            drawLabelValue(
                "Item Total",
                order.itemTotal
            )

            //----------------------------------
            // DISCOUNT
            //----------------------------------
            if (order.discount > 0) {
                drawLabelValue(
                    "Discount",
                    -order.discount
                )
            }

            //----------------------------------
            // TAXABLE AMOUNT
            //----------------------------------
            val taxableAmount = when (outlet.taxMode) {
                TaxMode.FORCE_INCLUSIVE ->
                    order.itemTotal - order.discount - order.itemTax

                else ->
                    order.itemTotal - order.discount
            }

            drawLabelValue(
                "Taxable Amount",
                taxableAmount
            )


//----------------------------------
// TAX
//----------------------------------
            if (order.itemTax > 0) {

                if (outlet.countryCode == "IN") {

                    val cgst = order.itemTax / 2
                    val sgst = order.itemTax / 2

                    // Default display only
                    val cgstPercent = 2.5
                    val sgstPercent = 2.5

                    drawGSTLine(
                        "Central GST ${"%.1f".format(cgstPercent)}% on",
                        taxableAmount,
                        cgst
                    )

                    drawGSTLine(
                        "State GST ${"%.1f".format(sgstPercent)}% on",
                        taxableAmount,
                        sgst
                    )

                } else {

                    drawLabelValue(
                        "${outlet.taxType ?: "Tax"}",
                        order.itemTax
                    )
                }
            }

            //----------------------------------
            // DELIVERY
            //----------------------------------
            if (order.deliveryFee > 0) {
                drawLabelValue(
                    "Delivery Charge",
                    order.deliveryFee
                )
            }

            //----------------------------------
            // DELIVERY GST
            //----------------------------------
            if (order.deliveryTax > 0) {
                drawLabelValue(
                    "Delivery Tax",
                    order.deliveryTax
                )
            }

            //----------------------------------
            // ROUND OFF
            //----------------------------------
            val roundedTotal = kotlin.math.round(order.grandTotal)
            val roundOff = roundedTotal - order.grandTotal

            if (kotlin.math.abs(roundOff) >= 0.01) {
                drawLabelValue(
                    "Round Off",
                    roundOff
                )
            }

            y -= 10f
        }

        fun drawGrandTotalBox(order: PrintOrder) {

            val grandPaint = Paint(boldPaint).apply {
                textSize = 30f
            }

            val labelPaint = Paint(boldPaint).apply {
                textSize = 26f
                textAlign = Paint.Align.CENTER
            }

            val top = y + 20f
            val boxHeight = 70f
            val bottom = top + boxHeight

            //==============================
            // OUTER BOX
            //==============================
            canvas.drawRect(leftMargin, top, rightMargin, bottom, boxPaint)

            //==============================
            // 🔥 DIVIDER POSITION (CONTROLS RIGHT BOX WIDTH)
            //==============================
            // 👉 Move MORE LEFT → bigger right box
            // 👉 Move RIGHT → smaller right box
            val midX = line3 - 60f   // 🔥 increased from -40f → MORE WIDTH

            canvas.drawLine(midX, top, midX, bottom, linePaint)

            //==============================
            // TEXT CENTER (VERTICAL)
            //==============================
            val textY = top + (boxHeight / 2f) + (grandPaint.textSize / 3f)

            //==============================
            // 🔥 LEFT TEXT POSITION
            //==============================
            // 👉 THIS LINE controls horizontal position of "PLEASE PAY"
            val leftCenter = (leftMargin + midX+20) / 2f   // 👈 adjust this if needed

            canvas.drawText(
                "PLEASE PAY",
                leftCenter,
                textY,
                labelPaint
            )

            //==============================
            // 🔥 RIGHT TEXT (AMOUNT POSITION)
            //==============================
            drawRightY(
                String.format("%.2f", order.grandTotal),
                rightMargin - 45f,   //
                textY,
                grandPaint
            )

            //==============================
// AMOUNT IN WORDS
//==============================
            val wordsPaint = Paint(normalPaint).apply {
                textSize = 22f
                textAlign = Paint.Align.CENTER
            }

            val amountWords = numberToWords(order.grandTotal)

// 👉 center below box
            canvas.drawText(
                amountWords,
                printableWidth / 2f,
                bottom + 30f,
                wordsPaint
            )





            y = bottom + 40f   // 👈 increase spacing
        }


        fun drawDiscountSummary(order: PrintOrder) {

            val discountAmount = order.discount ?: 0.0
            if (discountAmount <= 0.0) return

            y += 40f

            val paint = Paint().apply {
                color = Color.BLACK
                textSize = 54f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC) // 🔥 stylish
                isAntiAlias = true
                letterSpacing = 0.02f // bring letters closer
            }

            // 🔥 Keep text tight (no big gaps)
            val text = "Discount ₹${discountAmount.toInt()}"

            canvas.drawText(text, 20f, y, paint)

            y += 20f
        }

        fun drawQrCode(qr: Bitmap?) {

            if (qr == null) return

            y -= 10f

            val qrSize = 280  // 🔥 best for 48mm

            val scaledQr = Bitmap.createScaledBitmap(qr, qrSize, qrSize, true)

            val x = (printableWidth - qrSize) / 2f

            // Draw QR
            canvas.drawBitmap(scaledQr, x, y, null)

            y += qrSize + 1f

            // Label
            val paint = Paint().apply {
                textAlign = Paint.Align.CENTER
                textSize = 26f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }

            canvas.drawText(
                "SCAN & PAY",
                printableWidth / 2f,
                y,
                paint
            )

            y += 30f
        }
        private fun drawLabelValue(
            label: String,
            value: Double
        ) {

            canvas.drawText(
                label,
                xItem,
                y,
                normalPaint
            )

            drawRight(
                String.format("%.2f", value),
                xAmount,
                boldPaint
            )

            y += 34f
        }


//================================================
// GST BREAKDOWN
//================================================
private fun drawGSTLine(
    label: String,
    baseAmount: Double,
    value: Double
) {

    canvas.drawText(
        "$label [${String.format("%.2f", baseAmount)}]:",
        xItem,
        y,
        normalPaint
    )

    drawRight(
        String.format("%.2f", value),
        xAmount,
        boldPaint
    )

    y += 32f
}




        private fun drawGSTBreakdown(
            taxableAmount: Double,
            cgst: Double,
            sgst: Double
        ) {

            val labelPaint = normalPaint
            val valuePaint = boldPaint

            val leftX = leftMargin
            val rightX = xAmount   // right aligned values

            //----------------------------------
            // CGST
            //----------------------------------

            canvas.drawText(
                "Central GST on [${String.format("%.2f", taxableAmount)}]:",
                leftX,
                y,
                labelPaint
            )

            drawRight(
                String.format("%.2f", cgst),
                rightX,
                valuePaint
            )

            y += 32f

            //----------------------------------
            // SGST
            //----------------------------------

            canvas.drawText(
                "State GST on [${String.format("%.2f", taxableAmount)}]:",
                leftX,
                y,
                labelPaint
            )

            drawRight(
                String.format("%.2f", sgst),
                rightX,
                valuePaint
            )

            y += 36f
        }

//================================================
// Wrap Text
//================================================

        private fun wrapText(
            text: String,
            maxWidth: Float,
            paint: Paint
        ): List<String> {

            val words = text.split(" ")

            val lines = mutableListOf<String>()

            var current = ""

            words.forEach { word ->

                val test =
                    if (current.isEmpty())
                        word
                    else
                        "$current $word"

                if (paint.measureText(test) <= maxWidth) {

                    current = test

                } else {

                    if (current.isNotEmpty()) {
                        lines.add(current)
                    }

                    current = word

                }

            }

            if (current.isNotEmpty()) {
                lines.add(current)
            }

            return lines
        }
//================================================
// Draw Modifier
//================================================

        private fun drawModifier(
            modifier: String
        ) {

            canvas.drawText(
                "+ $modifier",
                xItem + 24f,
                y,
                normalPaint
            )

            y += 30f
        }

//================================================
// Draw Note
//================================================

        private fun drawNote(
            note: String
        ) {

            val notePaint = Paint(normalPaint).apply {
                textSize = 22f
                typeface = Typeface.create(
                    Typeface.DEFAULT,
                    Typeface.ITALIC
                )
            }

            canvas.drawText(
                "* $note",
                xItem + 24f,
                y,
                notePaint
            )

            y += 30f
        }

//================================================
// Helpers
//================================================

        private fun drawCenter(
            text: String,
            paint: Paint
        ) {

            canvas.drawText(
                text,
                printableWidth / 2f,
                y,
                paint
            )

            y += paint.textSize + 10f
        }

        private fun drawLeft(
            text: String,
            x: Float,
            paint: Paint
        ) {

            canvas.drawText(
                text,
                x,
                y,
                paint
            )
        }

        private fun drawRight(
            text: String,
            x: Float,
            paint: Paint
        ) {

            val width = paint.measureText(text)

            canvas.drawText(
                text,
                x - width,
                y,
                paint
            )
        }

        private fun drawRightY(
            text: String,
            x: Float,
            yPos: Float,
            paint: Paint
        ) {
            val width = paint.measureText(text)

            canvas.drawText(
                text,
                x - width,
                yPos,
                paint
            )
        }

        private fun drawDivider() {

            canvas.drawLine(
                leftMargin,
                y,
                rightMargin,
                y,
                linePaint
            )

            y += 18f
        }


        fun drawLogo(logo: Bitmap?) {

            if (logo == null) return

            //----------------------------------
            // Available space
            //----------------------------------
            val maxWidth = (rightMargin - leftMargin).toInt()
            val maxHeight = 150   // 👈 your limit

            //----------------------------------
            // ORIGINAL SIZE
            //----------------------------------
            val originalWidth = logo.width.toFloat()
            val originalHeight = logo.height.toFloat()

            //----------------------------------
            // SCALE FACTOR (keep aspect ratio)
            //----------------------------------
            val widthRatio = maxWidth / originalWidth
            val heightRatio = maxHeight / originalHeight

            // 👉 choose smaller ratio to fit inside both limits
            val scaleFactor = minOf(widthRatio, heightRatio)

            //----------------------------------
            // FINAL SIZE (PROPORTIONAL)
            //----------------------------------
            val finalWidth = (originalWidth * scaleFactor).toInt()
            val finalHeight = (originalHeight * scaleFactor).toInt()

            //----------------------------------
            // CREATE SCALED BITMAP
            //----------------------------------
            val scaledLogo = Bitmap.createScaledBitmap(
                logo,
                finalWidth,
                finalHeight,
                true
            )

            //----------------------------------
            // Center horizontally
            //----------------------------------
            val x = (printableWidth - finalWidth) / 2f

            //----------------------------------
            // Draw
            //----------------------------------
            canvas.drawBitmap(
                scaledLogo,
                x,
                y,
                null
            )

            //----------------------------------
            // Move Y down
            //----------------------------------
            y += finalHeight + 20f
        }

        fun numberToWords(number: Double): String {

            val units = arrayOf(
                "", "One", "Two", "Three", "Four", "Five", "Six",
                "Seven", "Eight", "Nine", "Ten", "Eleven",
                "Twelve", "Thirteen", "Fourteen", "Fifteen",
                "Sixteen", "Seventeen", "Eighteen", "Nineteen"
            )

            val tens = arrayOf(
                "", "", "Twenty", "Thirty", "Forty",
                "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
            )

            fun convert(n: Int): String {
                return when {
                    n < 20 -> units[n]
                    n < 100 -> tens[n / 10] + " " + units[n % 10]
                    n < 1000 -> units[n / 100] + " Hundred " + convert(n % 100)
                    n < 100000 -> convert(n / 1000) + " Thousand " + convert(n % 1000)
                    n < 10000000 -> convert(n / 100000) + " Lakh " + convert(n % 100000)
                    else -> convert(n / 10000000) + " Crore " + convert(n % 10000000)
                }
            }

            val rupees = number.toInt()

            return convert(rupees).trim() + " Only"
        }

    }
}