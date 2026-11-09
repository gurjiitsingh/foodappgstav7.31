package com.it10x.foodappgstav7_31.printer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File

object QrUtils {

    fun generateQr(
        text: String,
        size: Int = 256
    ): Bitmap {

        val bits = QRCodeWriter().encode(
            text,
            BarcodeFormat.QR_CODE,
            size,
            size
        )

        val bmp = Bitmap.createBitmap(
            size,
            size,
            Bitmap.Config.RGB_565
        )

        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(
                    x,
                    y,
                    if (bits[x, y]) Color.BLACK else Color.WHITE
                )
            }
        }

        return bmp
    }

    // ✅ LOAD SAVED QR
    fun loadSavedQr(
        context: Context
    ): Bitmap? {

        return try {

            val file = File(
                context.filesDir,
                "qr.png"
            )

            if (!file.exists()) {
                return null
            }

            BitmapFactory.decodeFile(
                file.absolutePath
            )

        } catch (e: Exception) {
            null
        }
    }
}