package com.it10x.foodappgstav7_31.printer.common

import android.graphics.Bitmap
import java.io.OutputStream
import kotlinx.coroutines.delay

object EscPosImagePrinter {

    // =========================================================
    // COMMON BITMAP -> ESC/POS RASTER
    // =========================================================

    fun convertBitmapToRaster(
        bitmap: Bitmap
    ): ByteArray {

        val width = bitmap.width
        val height = bitmap.height

        val bytes = ArrayList<Byte>()

        val bytesPerLine = (width + 7) / 8

        // GS v 0
        bytes.add(0x1D)
        bytes.add(0x76)
        bytes.add(0x30)
        bytes.add(0x00)

        // Width
        bytes.add(
            (bytesPerLine % 256).toByte()
        )

        bytes.add(
            (bytesPerLine / 256).toByte()
        )

        // Height
        bytes.add(
            (height % 256).toByte()
        )

        bytes.add(
            (height / 256).toByte()
        )

        for (y in 0 until height) {

            for (x in 0 until bytesPerLine * 8 step 8) {

                var byte = 0

                for (bit in 0 until 8) {

                    val xPos = x + bit

                    if (xPos < width) {

                        val pixel =
                            bitmap.getPixel(xPos, y)

                        val r =
                            (pixel shr 16) and 0xff

                        val g =
                            (pixel shr 8) and 0xff

                        val b =
                            pixel and 0xff

                        val gray =
                            (r + g + b) / 3

                        if (gray < 128) {
                            byte =
                                byte or
                                        (1 shl (7 - bit))
                        }
                    }
                }

                bytes.add(byte.toByte())
            }
        }

        return bytes.toByteArray()
    }


    // =========================================================
    // COMMON IMAGE CHUNKING FOR STREAM PRINTERS
    // BT + LAN
    // =========================================================

    fun printBitmapInChunks(
        output: OutputStream,
        bitmap: Bitmap,
        chunkHeight: Int = 48,
        packetSize: Int = 1024,
        delayMs: Long = 20
    ) {

        var y = 0

        while (y < bitmap.height) {

            val height =
                minOf(
                    chunkHeight,
                    bitmap.height - y
                )

            val chunk =
                Bitmap.createBitmap(
                    bitmap,
                    0,
                    y,
                    bitmap.width,
                    height
                )

            try {

                val bytes =
                    convertBitmapToRaster(chunk)

                var offset = 0

                while (offset < bytes.size) {

                    val end =
                        minOf(
                            offset + packetSize,
                            bytes.size
                        )

                    output.write(
                        bytes,
                        offset,
                        end - offset
                    )

                    output.flush()

                    offset = end

                    if (delayMs > 0) {
                        Thread.sleep(delayMs)
                    }
                }

            } finally {

                chunk.recycle()
            }

            y += height
        }
    }


    // =========================================================
    // COMMON ESC/POS COMMANDS
    // =========================================================

    fun initCommand(): ByteArray =
        byteArrayOf(
            0x1B,
            0x40
        )

    fun beepCommand(): ByteArray =
        byteArrayOf(
            0x1B,
            0x42,
            0x03,
            0x02
        )

    fun centerCommand(): ByteArray =
        byteArrayOf(
            0x1B,
            0x61,
            0x01
        )

    fun leftCommand(): ByteArray =
        byteArrayOf(
            0x1B,
            0x61,
            0x00
        )

    fun feedAndCutCommand(): ByteArray =
        byteArrayOf(
            0x1B,
            0x64,
            0x03,
            0x1D,
            0x56,
            0x01
        )
}