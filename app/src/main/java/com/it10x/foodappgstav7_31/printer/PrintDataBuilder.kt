package com.it10x.foodappgstav7_31.printer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.it10x.foodappgstav7_31.printer.bluetooth.BluetoothPrinter
import java.io.File



object PrintDataBuilder {

    fun loadLogo(context: Context): Bitmap? {
        return try {
            val file = File(context.filesDir, "logo.png")
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun resizeLogo(
        bitmap: Bitmap,
        printerSize: String
    ): Bitmap {
        val targetWidth = if (printerSize == "80mm") 200 else 100
        return BluetoothPrinter.resizeBitmap(bitmap, targetWidth)
    }
}