package com.it10x.foodappgstav7_31.utils.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_31.data.pos.entities.config.OutletEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import com.it10x.foodappgstav7_31.printer.utils.QrUtils

object ReceiptPdfGenerator {

    fun generatePdf(
        context: Context,
        order: PosOrderMasterEntity,
        items: List<PosOrderItemEntity>,
        outlet: OutletEntity?
    ): Uri {

        // =========================
// OUTLET VALUES
// =========================

        // =========================
// OUTLET VALUES
// =========================

        val outletName =
            outlet?.outletName ?: "My Restaurant"

        val outletPhone =
            outlet?.phone ?: ""

        val outletAddress = buildString {

            append(outlet?.addressLine1 ?: "")

            if (!outlet?.addressLine2.isNullOrBlank()) {
                append(", ${outlet?.addressLine2}")
            }

            if (!outlet?.city.isNullOrBlank()) {
                append(", ${outlet?.city}")
            }

            if (!outlet?.state.isNullOrBlank()) {
                append(", ${outlet?.state}")
            }

            if (!outlet?.zipcode.isNullOrBlank()) {
                append(" - ${outlet?.zipcode}")
            }
        }

        val outletGstin =
            outlet?.gstVatNumber ?: ""

        val gstText =
            if (outletGstin.isNotBlank()) {
                "GSTIN: $outletGstin"
            } else {
                ""
            }

        val footerNote =
            outlet?.footerNote ?: "Thank you for visiting!"

        val pageWidth = 576
        val baseHeight = 650

        val itemsHeight = items.sumOf {

            var h = 80

            if (it.modifierSummary.isNotEmpty()) {
                h += 40
            }

            h
        }

        // =========================
// EXTRA HEIGHT
// =========================

        val hasQr =
            outlet?.qrEnabled == true &&
                    (
                            order.paymentMode.equals("UPI", true) ||
                                    QrUtils.loadSavedQr(context) != null
                            )

        val qrHeight =
            if (hasQr) 280 else 0

        val footerHeight = 120

        val pageHeight =
            baseHeight +
                    itemsHeight +
                    qrHeight +
                    footerHeight

        val pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(
            pageWidth,
            pageHeight,
            1
        ).create()

        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas

        // =========================
        // PAINTS
        // =========================

        val titlePaint = Paint().apply {
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val boldPaint = Paint().apply {
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // ✅ Item name paint (normal / semi-bold look)
        val itemNamePaint = Paint().apply {
            textSize = 19f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val normalPaint = Paint().apply {
            textSize = 18f
            isAntiAlias = true
        }

        val smallPaint = Paint().apply {
            textSize = 16f
            isAntiAlias = true
        }

        // =========================
        // HEADER
        // =========================

        var y = 60

        canvas.drawText(
            outletName,
            40f,
            y.toFloat(),
            titlePaint
        )

        y += 35

        if (outletPhone.isNotEmpty()) {

            canvas.drawText(
                "Phone: $outletPhone",
                40f,
                y.toFloat(),
                normalPaint
            )

            y += 30
        }

        if (outletAddress.isNotEmpty()) {

            canvas.drawText(
                outletAddress,
                40f,
                y.toFloat(),
                smallPaint
            )

            y += 30
        }

        if (gstText.isNotEmpty()) {

            canvas.drawText(
                gstText,
                40f,
                y.toFloat(),
                normalPaint
            )

            y += 30
        }

        // =========================
        // DIVIDER
        // =========================

        y += 10

        canvas.drawLine(
            40f,
            y.toFloat(),
            536f,
            y.toFloat(),
            normalPaint
        )

        // =========================
        // ORDER INFO
        // =========================

        y += 40

        val sdf = SimpleDateFormat(
            "dd MMM yyyy hh:mm a",
            Locale.getDefault()
        )

        canvas.drawText(
            "ORDER RECEIPT",
            180f,
            y.toFloat(),
            boldPaint
        )

        y += 40

        canvas.drawText(
            "Order #: ${order.srno}",
            40f,
            y.toFloat(),
            normalPaint
        )

        y += 30

        canvas.drawText(
            "Date: ${sdf.format(Date(order.createdAt))}",
            40f,
            y.toFloat(),
            normalPaint
        )

        y += 30

        canvas.drawText(
            "Type: ${order.orderType}",
            40f,
            y.toFloat(),
            normalPaint
        )

        y += 30

        if (!order.tableNo.isNullOrEmpty()) {

            canvas.drawText(
                "Table: ${order.tableNo}",
                40f,
                y.toFloat(),
                normalPaint
            )

            y += 30
        }

        canvas.drawText(
            "Payment: ${order.paymentMode}",
            40f,
            y.toFloat(),
            normalPaint
        )

        // =========================
        // ITEMS HEADER
        // =========================



        y += 40

        canvas.drawLine(
            40f,
            y.toFloat(),
            560f,
            y.toFloat(),
            normalPaint
        )

        y += 35

// Header columns
        canvas.drawText(
            "ITEM",
            40f,
            y.toFloat(),
            boldPaint
        )

        canvas.drawText(
            "QTY",
            320f,
            y.toFloat(),
            boldPaint
        )

        canvas.drawText(
            "PRICE",
            390f,
            y.toFloat(),
            boldPaint
        )

        val totalHeader = "TOTAL"

        canvas.drawText(
            totalHeader,
            pageWidth - 40f - boldPaint.measureText(totalHeader),
            y.toFloat(),
            boldPaint
        )

        y += 20

        canvas.drawLine(
            40f,
            y.toFloat(),
            560f,
            y.toFloat(),
            normalPaint
        )

        y += 20
        // =========================
        // ITEMS
        // =========================



        // =========================
// ITEMS
// =========================

        items.forEach { item ->

            y += 34

            // Item Name
            canvas.drawText(
                item.name,
                40f,
                y.toFloat(),
                itemNamePaint
            )

            // Qty
            val qtyText = "${item.quantity}"

            canvas.drawText(
                qtyText,
                330f,
                y.toFloat(),
                normalPaint
            )

            // Price
// Price (moved more left under PRICE header)
            val priceText =
                "%.2f".format(item.finalPricePerItem)

            val priceX =
                430f - smallPaint.measureText(priceText)

            canvas.drawText(
                priceText,
                priceX,
                y.toFloat(),
                smallPaint
            )

// Total (right aligned with more gap)
            val totalText =
                "%.2f".format(item.finalTotal)

            val totalX =
                pageWidth - 40f - boldPaint.measureText(totalText)

            canvas.drawText(
                totalText,
                totalX,
                y.toFloat(),
                boldPaint
            )

            // Modifier
            if (item.modifierSummary.isNotEmpty()) {

                y += 22

                canvas.drawText(
                    "+ ${item.modifierSummary}",
                    60f,
                    y.toFloat(),
                    smallPaint
                )
            }

            y += 20
        }

        // =========================
        // TOTALS
        // =========================

        y += 20

        canvas.drawLine(
            40f,
            y.toFloat(),
            560f,
            y.toFloat(),
            normalPaint
        )

        y += 40

        fun drawTotalRow(
            label: String,
            value: String
        ) {

            canvas.drawText(
                label,
                40f,
                y.toFloat(),
                normalPaint
            )

            val valueX =
                pageWidth - 40f - boldPaint.measureText(value)

            canvas.drawText(
                value,
                valueX,
                y.toFloat(),
                boldPaint
            )

            y += 35
        }

        drawTotalRow(
            "Subtotal",
            "${"%.2f".format(order.itemTotal)}"
        )

        drawTotalRow(
            "Tax",
            "${"%.2f".format(order.itemTax)}"
        )

        if (order.deliveryFee > 0) {

            drawTotalRow(
                "Delivery Fee",
                "${"%.2f".format(order.deliveryFee)}"
            )
        }

        if (order.discountTotal > 0) {

            drawTotalRow(
                "Discount",
                "${"%.2f".format(order.discountTotal)}"
            )
        }

        y += 10

        canvas.drawLine(
            40f,
            y.toFloat(),
            560f,
            y.toFloat(),
            normalPaint
        )

        y += 45

        canvas.drawText(
            "GRAND TOTAL",
            40f,
            y.toFloat(),
            boldPaint
        )

        val grandText =
            "${"%.2f".format(order.grandTotal)}"

        val grandX =
            pageWidth - 40f - titlePaint.measureText(grandText)

        canvas.drawText(
            grandText,
            grandX,
            y.toFloat(),
            titlePaint
        )


        // =========================
// QR CODE
// =========================

        val qrBitmap = try {

            if (
                order.paymentMode.equals("UPI", true) &&
                outlet?.qrEnabled == true &&
                !outlet.upiId.isNullOrBlank()
            ) {

                val upiId = outlet.upiId

                val name =
                    outlet.upiName ?: outlet.outletName

                val encodedName =
                    java.net.URLEncoder.encode(name, "UTF-8")

                val amount =
                    String.format("%.2f", order.grandTotal)

                val upiLink =
                    "upi://pay?" +
                            "pa=$upiId" +
                            "&pn=$encodedName" +
                            "&am=$amount" +
                            "&cu=INR"

                QrUtils.generateQr(upiLink)

            } else {

                QrUtils.loadSavedQr(context)
            }

        } catch (e: Exception) {
            null
        }

        if (qrBitmap != null) {

            y += 50

            val qrSize = 180f

            // ✅ PERFECT CENTER
            val qrX =
                (pageWidth / 2f) - (qrSize / 2f)

            // ✅ DRAW QR
            canvas.drawBitmap(
                qrBitmap.scale(
                    qrSize.toInt(),
                    qrSize.toInt(),
                    true
                ),
                qrX,
                y.toFloat(),
                null
            )

            // ✅ MOVE BELOW QR
            y += qrSize.toInt() + 45

            val qrTitle =
                if (order.paymentMode.equals("UPI", true)) {

                    outlet?.upiTitle
                        ?: "Scan & Pay"

                } else {

                    outlet?.qrTitle
                        ?: ""
                }

            // ✅ CENTER TEXT
            val qrTitleWidth =
                boldPaint.measureText(qrTitle)

            val qrTitleX =
                (pageWidth - qrTitleWidth) / 2f

            if (qrTitle.isNotBlank()) {

                canvas.drawText(
                    qrTitle,
                    qrTitleX,
                    y.toFloat(),
                    boldPaint
                )

                y += 35
            }
        }
        // =========================
// FOOTER
// =========================

        y += 70

        val footerX =
            (pageWidth - boldPaint.measureText(footerNote)) / 2

        canvas.drawText(
            footerNote,
            footerX,
            y.toFloat(),
            boldPaint
        )

        y += 35

       // val poweredText = "Powered by IT10X POS"
        val poweredText = ""
        val poweredX =
            (pageWidth - smallPaint.measureText(poweredText)) / 2

        canvas.drawText(
            poweredText,
            poweredX,
            y.toFloat(),
            smallPaint
        )

        // =========================
        // FINISH
        // =========================

        pdfDocument.finishPage(page)

        val date =
            SimpleDateFormat(
                "ddMMyyyy",
                Locale.getDefault()
            ).format(Date(order.createdAt))

        val file = File(
            context.cacheDir,
            "sale_${order.srno}_$date.pdf"
        )

        FileOutputStream(file).use {
            pdfDocument.writeTo(it)
        }

        pdfDocument.close()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
}