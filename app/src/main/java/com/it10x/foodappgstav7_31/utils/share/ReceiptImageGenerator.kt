package com.it10x.foodappgstav7_31.printer.billimage



import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_31.data.pos.entities.config.OutletEntity
import com.it10x.foodappgstav7_31.printer.utils.QrUtils
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReceiptImageGenerator {

    fun generateReceiptImage(
        context: Context,
        order: PosOrderMasterEntity,
        items: List<PosOrderItemEntity>,
        outlet: OutletEntity?
    ): Uri {

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

        // =========================
        // HEIGHT CALCULATION
        // =========================

        val baseHeight = 900

        val itemsHeight = items.sumOf {

            var h = 80

            if (it.modifierSummary.isNotEmpty()) {
                h += 40
            }

            h
        }

        val hasQr =
            outlet?.qrEnabled == true &&
                    (
                            order.paymentMode.equals("UPI", true) ||
                                    QrUtils.loadSavedQr(context) != null
                            )

        val qrHeight =
            if (hasQr) 320 else 0

        val bitmapHeight =
            baseHeight + itemsHeight + qrHeight

        val bitmap = Bitmap.createBitmap(
            600,
            bitmapHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.WHITE)

        // =========================
        // PAINTS
        // =========================

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val normalPaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            isAntiAlias = true
        }

        val smallPaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            isAntiAlias = true
        }

        val itemPaint = Paint().apply {
            color = Color.BLACK
            textSize = 21f
            isAntiAlias = true
        }

        // =========================
// HEADER
// =========================

        var y = 80f   // More top padding

// ---------- Restaurant Name ----------

        val titleX = (600f - titlePaint.measureText(outletName)) / 2f

        canvas.drawText(
            outletName.uppercase(),
            titleX,
            y,
            titlePaint
        )

        y += 45f

// ---------- Phone ----------

        if (outletPhone.isNotEmpty()) {

            val phone = "Phone : $outletPhone"

            val phoneX =
                (600f - normalPaint.measureText(phone)) / 2f

            canvas.drawText(
                phone,
                phoneX,
                y,
                normalPaint
            )

            y += 32f
        }

// ---------- Address ----------

        if (outletAddress.isNotEmpty()) {

            val addressX =
                (600f - smallPaint.measureText(outletAddress)) / 2f

            canvas.drawText(
                outletAddress,
                addressX,
                y,
                smallPaint
            )

            y += 30f
        }

// ---------- GST ----------

        if (gstText.isNotEmpty()) {

            val gstX =
                (600f - normalPaint.measureText(gstText)) / 2f

            canvas.drawText(
                gstText,
                gstX,
                y,
                normalPaint
            )

            y += 35f
        }

// ---------- Divider ----------

        y += 12f

        canvas.drawLine(
            40f,
            y,
            560f,
            y,
            normalPaint
        )

        y += 38f

// ---------- TAX INVOICE ----------

// Top padding before title
        y += 20f

// Top divider
        canvas.drawLine(
            40f,
            y,
            560f,
            y,
            normalPaint
        )

// Space between line and title
        y += 40f

        val invoicePaint = Paint(boldPaint).apply {
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText(
            "TEST CENTER",
            300f,
            y,
            invoicePaint
        )

// Space below title
        y += 35f

// Bottom divider
        canvas.drawLine(
            40f,
            y,
            560f,
            y,
            normalPaint
        )

// Space after divider
        y += 35f
        // =========================
        // ORDER INFO
        // =========================

        y += 50f

        val sdf = SimpleDateFormat(
            "dd MMM yyyy hh:mm a",
            Locale.getDefault()
        )

        canvas.drawText(
            "ORDER RECEIPT",
            180f,
            y,
            boldPaint
        )

        y += 50f

        canvas.drawText(
            "Order #: ${order.srno}",
            40f,
            y,
            normalPaint
        )

        y += 35f

        canvas.drawText(
            "Date: ${sdf.format(Date(order.createdAt))}",
            40f,
            y,
            normalPaint
        )

        y += 35f

        canvas.drawText(
            "Type: ${order.orderType}",
            40f,
            y,
            normalPaint
        )

        y += 35f

        canvas.drawText(
            "Payment: ${order.paymentMode}",
            40f,
            y,
            normalPaint
        )

        // =========================
        // ITEMS HEADER
        // =========================

        y += 45f

        canvas.drawLine(
            40f,
            y,
            560f,
            y,
            normalPaint
        )

        y += 35f

        canvas.drawText(
            "ITEM",
            40f,
            y,
            boldPaint
        )

        canvas.drawText(
            "QTY",
            320f,
            y,
            boldPaint
        )

        canvas.drawText(
            "PRICE",
            390f,
            y,
            boldPaint
        )

        val totalHeader = "TOTAL"

        canvas.drawText(
            totalHeader,
            600f - 40f - boldPaint.measureText(totalHeader),
            y,
            boldPaint
        )

        y += 20f

        canvas.drawLine(
            40f,
            y,
            560f,
            y,
            normalPaint
        )

        // =========================
        // ITEMS
        // =========================

        items.forEach { item ->

            y += 40f

            canvas.drawText(
                item.name,
                40f,
                y,
                itemPaint
            )

            canvas.drawText(
                "${item.quantity}",
                330f,
                y,
                normalPaint
            )

            val priceText =
                "%.2f".format(item.finalPricePerItem)

            canvas.drawText(
                priceText,
                430f - smallPaint.measureText(priceText),
                y,
                smallPaint
            )

            val totalText =
                "%.2f".format(item.finalTotal)

            canvas.drawText(
                totalText,
                600f - 40f - boldPaint.measureText(totalText),
                y,
                boldPaint
            )

            if (item.modifierSummary.isNotEmpty()) {

                y += 25f

                canvas.drawText(
                    "+ ${item.modifierSummary}",
                    60f,
                    y,
                    smallPaint
                )
            }

            y += 20f
        }

        // =========================
        // TOTALS
        // =========================

        y += 20f

        canvas.drawLine(
            40f,
            y,
            560f,
            y,
            normalPaint
        )

        y += 45f

        fun drawTotalRow(
            label: String,
            value: String
        ) {

            canvas.drawText(
                label,
                40f,
                y,
                normalPaint
            )

            val valueX =
                600f - 40f - boldPaint.measureText(value)

            canvas.drawText(
                value,
                valueX,
                y,
                boldPaint
            )

            y += 40f
        }

        drawTotalRow(
            "Subtotal",
            "%.2f".format(order.itemTotal)
        )

        drawTotalRow(
            "Tax",
            "%.2f".format(order.itemTax)
        )

        if (order.deliveryFee > 0) {

            drawTotalRow(
                "Delivery Fee",
                "%.2f".format(order.deliveryFee)
            )
        }

        if (order.discountTotal > 0) {

            drawTotalRow(
                "Discount",
                "%.2f".format(order.discountTotal)
            )
        }

        y += 10f

        canvas.drawLine(
            40f,
            y,
            560f,
            y,
            normalPaint
        )

        y += 50f

        canvas.drawText(
            "GRAND TOTAL",
            40f,
            y,
            boldPaint
        )

        val grandText =
            "%.2f".format(order.grandTotal)

        canvas.drawText(
            grandText,
            600f - 40f - titlePaint.measureText(grandText),
            y,
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

            y += 60f

            val qrSize = 180f

            // ✅ PERFECT CENTER
            val qrX =
                (600f / 2f) - (qrSize / 2f)

            // ✅ DRAW QR
            canvas.drawBitmap(
                qrBitmap.scale(
                    qrSize.toInt(),
                    qrSize.toInt(),
                    true
                ),
                qrX,
                y,
                null
            )

            // ✅ MOVE BELOW QR
            y += qrSize + 45f

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
                (600f - qrTitleWidth) / 2f

            if (qrTitle.isNotBlank()) {

                canvas.drawText(
                    qrTitle,
                    qrTitleX,
                    y,
                    boldPaint
                )

                y += 35f
            }
        }

        // =========================
        // FOOTER
        // =========================

        y += 80f

        val footerX =
            (600f - boldPaint.measureText(footerNote)) / 2f

        canvas.drawText(
            footerNote,
            footerX,
            y,
            boldPaint
        )

        y += 40f

       // val poweredText = "Powered by IT10X POS"
        val poweredText = ""
        val poweredX =
            (600f - smallPaint.measureText(poweredText)) / 2f

        canvas.drawText(
            poweredText,
            poweredX,
            y,
            smallPaint
        )

        // =========================
        // SAVE IMAGE
        // =========================

        val date =
            SimpleDateFormat(
                "ddMMyyyy",
                Locale.getDefault()
            ).format(Date(order.createdAt))

        val file = File(
            context.cacheDir,
            "sale_${order.srno}_$date.jpg"
        )

        FileOutputStream(file).use {
            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                100,
                it
            )
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
}