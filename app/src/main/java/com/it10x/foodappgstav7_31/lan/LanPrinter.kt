package com.it10x.foodappgstav7_31.printer.lan

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.it10x.foodappgstav7_31.printer.common.EscPosImagePrinter
import com.it10x.foodappgstav7_31.printer.common.EscPosText
import com.it10x.foodappgstav7_31.printer.escpos.EscPosQr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

object LanPrinter {

    private const val TAG = "LanPrinter"
    private const val TIMEOUT = 3000

    private val mainHandler = Handler(Looper.getMainLooper())

// -----------------------------
//FULL BILL AS IMAGE PRINT
// -----------------------------

    fun printBitmap(
        ip: String,
        port: Int,
        bitmap: android.graphics.Bitmap,
        onResult: (Boolean) -> Unit
    ) {

        Log.d(
            "IMAGE_TEST",
            "LanPrinter.printBitmap()"
        )

        Log.d(
            "IMAGE_TEST",
            "Bitmap size = ${bitmap.width} x ${bitmap.height}"
        )

        CoroutineScope(Dispatchers.IO).launch {

            var socket: Socket? = null
            var output: OutputStream? = null

            try {

                socket = Socket()

                socket.connect(
                    InetSocketAddress(ip, port),
                    TIMEOUT
                )

                output = socket.getOutputStream()

                // =============================
                // INIT
                // =============================

                output.write(
                    byteArrayOf(
                        0x1B,
                        0x40
                    )
                )

                // =============================
                // BEEP
                // =============================

                output.write(
                    byteArrayOf(
                        0x1B,
                        0x42,
                        0x03,
                        0x02
                    )
                )

                // =============================
                // CENTER
                // =============================

                output.write(
                    byteArrayOf(
                        0x1B,
                        0x61,
                        0x01
                    )
                )

                // =============================
                // COMMON IMAGE FORMAT
                // =============================

                EscPosImagePrinter.printBitmapInChunks(
                    output = output,
                    bitmap = bitmap,
                    chunkHeight = 48,
                    packetSize = 1024,
                    delayMs = 20
                )

                // =============================
                // FEED
                // =============================

                output.write(
                    byteArrayOf(
                        0x0A
                    )
                )

                // =============================
                // CUT
                // =============================

                output.write(
                    byteArrayOf(
                        0x1D,
                        0x56,
                        0x01
                    )
                )

                output.flush()

                withContext(Dispatchers.Main) {
                    onResult(true)
                }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Bitmap print failed",
                    e
                )

                withContext(Dispatchers.Main) {
                    onResult(false)
                }

            } finally {

                try {
                    output?.close()
                } catch (_: Exception) {
                }

                try {
                    socket?.close()
                } catch (_: Exception) {
                }
            }
        }
    }

    // -----------------------------
    // TEST PRINT
    // -----------------------------
    fun printTest(
        ip: String,
        port: Int,
        roleLabel: String,
        onResult: (Boolean) -> Unit
    )
    {
        val testText = """
        ****************************
             TEST PRINT
        ****************************
        Printer Role : $roleLabel
        Connection   : LAN
        IP Address   : $ip
        Port         : $port
        Status       : OK
        ----------------------------


    """.trimIndent()

        printText(
            ip = ip,
            port = port,
            text = testText,
            onResult = onResult
        )
    }

    // -----------------------------
    // CORE PRINT
    // -----------------------------
    fun printText(
        ip: String,
        port: Int,
        text: String,
        onResult: (Boolean) -> Unit,
        qrData: String? = null,
    ) {

        CoroutineScope(Dispatchers.IO).launch {

            var socket: Socket? = null
            var output: OutputStream? = null

            try {

                socket = Socket()

                socket.connect(
                    InetSocketAddress(ip, port),
                    TIMEOUT
                )

                output = socket.getOutputStream()

                // ==========================================
                // BILL TEXT
                // ==========================================

                val bytes = EscPosText.build(text)

                output.write(bytes)

                // ==========================================
                // PAYMENT QR
                // ==========================================

                if (!qrData.isNullOrBlank()) {

                    val qrBytes =
                        EscPosQr.build(
                            data = qrData
                        )

                    output.write(qrBytes)
                }

                // ==========================================
// SPACE AFTER QR
// ==========================================

                output.write(
                    byteArrayOf(
                        0x0A,
                        0x0A,
                        0x0A
                    )
                )

// ==========================================
// CUT AFTER QR
// ==========================================

                output.write(
                    byteArrayOf(
                        0x1D,
                        0x56,
                        0x01
                    )
                )

                output.flush()

                withContext(Dispatchers.Main) {
                    onResult(true)
                }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "LAN text print failed",
                    e
                )

                withContext(Dispatchers.Main) {
                    onResult(false)
                }

            } finally {

                try {
                    output?.close()
                    socket?.close()
                } catch (_: Exception) {
                }
            }
        }
    }




    private fun printBitmapInChunks(
        output: OutputStream,
        bitmap: android.graphics.Bitmap
    ) {
        val chunkHeight = 48

        var y = 0

        while (y < bitmap.height) {

            val height = minOf(chunkHeight, bitmap.height - y)

            val chunk = android.graphics.Bitmap.createBitmap(
                bitmap,
                0,
                y,
                bitmap.width,
                height
            )

            val bytes = convertBitmapToRaster(chunk)

            output.write(bytes)
            output.flush()

            Thread.sleep(40)

            y += height
        }
    }
    private fun convertBitmapToRaster(bitmap: android.graphics.Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height

        val bytes = ArrayList<Byte>()
        val bytesPerLine = (width + 7) / 8

        // GS v 0
        bytes.add(0x1D)
        bytes.add(0x76)
        bytes.add(0x30)
        bytes.add(0x00)

        bytes.add((bytesPerLine % 256).toByte())
        bytes.add((bytesPerLine / 256).toByte())

        bytes.add((height % 256).toByte())
        bytes.add((height / 256).toByte())

        for (y in 0 until height) {
            for (x in 0 until bytesPerLine * 8 step 8) {

                var byte = 0

                for (bit in 0 until 8) {
                    val xPos = x + bit

                    if (xPos < width) {
                        val pixel = bitmap.getPixel(xPos, y)

                        val r = (pixel shr 16) and 0xff
                        val g = (pixel shr 8) and 0xff
                        val b = pixel and 0xff

                        val gray = (r + g + b) / 3

                        if (gray < 128) {
                            byte = byte or (1 shl (7 - bit))
                        }
                    }
                }

                bytes.add(byte.toByte())
            }
        }

        return bytes.toByteArray()
    }


}
