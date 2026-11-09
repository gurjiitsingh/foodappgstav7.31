package com.it10x.foodappgstav7_31.printer.kotImage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotItemEntity

object KitchenBitmapGenerator {

    fun generate(
        context: Context,
        sessionKey: String,
        tableName: String,
        orderType: String,
        items: List<PosKotItemEntity>,
        kotNumber: String,
    ): Bitmap {

        return try {

            val width = 576

            // 🔥 Dynamic height (better than fixed 2000)
            val estimatedHeight = 700 + (items.size * 80)
            val height = estimatedHeight.coerceAtLeast(600)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            Log.d("KOT_DEBUG", "Generating NEW bitmap: ${System.currentTimeMillis()}")

            canvas.drawColor(Color.WHITE)

            val drawer = KitchenDrawer(canvas)

            drawer.drawTopBox(orderType)
            drawer.drawHeader(kotNumber,orderType,tableNo=sessionKey,tableName, items )

            Log.d("KOT_DEBUG", "Drawing items...")
            drawer.drawItems(items)

            Log.d("KOT_DEBUG", "Drawing footer...")
            drawer.drawFooter()

            Log.d("KOT_DEBUG", "Final Y position: ${drawer.y}")

            // ✅ Add bottom padding (important for printers)
            val finalHeight = (drawer.y + 40).toInt()

            val safeHeight = finalHeight.coerceIn(1, height)

            Log.d("KOT_DEBUG", "Cropping bitmap to height: $safeHeight")

            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                width,
                safeHeight
            )

        } catch (e: Exception) {

            Log.e("KOT_ERROR", "Bitmap generation FAILED!", e)

            // fallback (safe minimal)
            Bitmap.createBitmap(384, 120, Bitmap.Config.ARGB_8888)
        }
    }
}