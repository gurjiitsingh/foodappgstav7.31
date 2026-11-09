package com.it10x.foodappgstav7_31.printer.bluetooth

import android.bluetooth.BluetoothAdapter
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.OutputStream
import java.util.UUID
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.it10x.foodappgstav7_31.printer.common.EscPosImagePrinter
import com.it10x.foodappgstav7_31.printer.common.EscPosText
import com.it10x.foodappgstav7_31.printer.escpos.EscPosQr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object BluetoothPrinter {

    private const val TAG = "PRINT_BT"

    private val SPP_UUID: UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var socket: android.bluetooth.BluetoothSocket? = null
    private var output: OutputStream? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    // =============================
    // TEST PRINT
    // =============================
    fun printTest(
        mac: String,
        roleLabel: String,
        onResult: (Boolean) -> Unit
    ) {
        printText(
            mac,
            """
            ****************************
                 TEST PRINT
            ****************************
            Printer Role : $roleLabel
            Connection   : BLUETOOTH
            Status       : OK
            ----------------------------
            
            
            """.trimIndent(),
            onResult
        )
    }

    // =============================
    // CORE PRINT (ORDER / AUTO)
    // =============================
    fun printText(
        mac: String,
        text: String,
        onResult: (Boolean) -> Unit,
        qrData: String? = null,
    ) {

        Thread {

            try {

                val adapter =
                    BluetoothAdapter.getDefaultAdapter()
                        ?: throw IllegalStateException(
                            "Bluetooth not supported"
                        )

                if (!adapter.isEnabled) {
                    throw IllegalStateException(
                        "Bluetooth is OFF"
                    )
                }

                ensureConnection(mac)

                val out = output
                    ?: throw Exception("No connection")

                // ==========================================
                // BILL TEXT
                // ==========================================

                val bytes =
                    EscPosText.build(text)

                out.write(bytes)

                // ==========================================
                // PAYMENT QR
                // ==========================================

                if (!qrData.isNullOrBlank()) {

                    Log.d(
                        TAG,
                        "BT QR DATA = ${qrData.take(100)}"
                    )

                    val qrBytes =
                        EscPosQr.build(
                            data = qrData
                        )

                    Log.d(
                        TAG,
                        "BT QR BYTES = ${qrBytes.size}"
                    )

                    out.write(qrBytes)
                }

                // ==========================================
                // SPACE AFTER QR
                // ==========================================

                out.write(
                    byteArrayOf(
                        0x0A,
                        0x0A,
                        0x0A
                    )
                )

                // ==========================================
                // CUT AFTER QR
                // ==========================================

                out.write(
                    byteArrayOf(
                        0x1D,
                        0x56,
                        0x01
                    )
                )

                out.flush()

                Thread.sleep(30)

                mainHandler.post {
                    onResult(true)
                }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Bluetooth print failed",
                    e
                )

                resetConnection()

                mainHandler.post {
                    onResult(false)
                }
            }

        }.start()
    }

    fun printText_withoutqr(
        mac: String,
        text: String,
        onResult: (Boolean) -> Unit
    ) {

        Thread {

            try {

                val adapter =
                    BluetoothAdapter.getDefaultAdapter()
                        ?: throw IllegalStateException(
                            "Bluetooth not supported"
                        )

                if (!adapter.isEnabled) {
                    throw IllegalStateException(
                        "Bluetooth is OFF"
                    )
                }

                ensureConnection(mac)

                val out = output
                    ?: throw Exception("No connection")

                // ==========================================
                // COMMON TEXT FORMAT
                // ==========================================

                val bytes = EscPosText.build(text)

                out.write(bytes)

                out.flush()

                Thread.sleep(30)

                mainHandler.post {
                    onResult(true)
                }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Bluetooth print failed",
                    e
                )

                resetConnection()

                mainHandler.post {
                    onResult(false)
                }
            }

        }.start()
    }






    // =============================
// PRINT LOGO + TEXT + QR (SMART FALLBACK)
// =============================
    fun printLogoTextQr(
        mac: String,
        logoBitmap: Bitmap?,   // nullable
        qrBitmap: Bitmap?,     // nullable
        text: String,
        onResult: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {


            try {
                val adapter = BluetoothAdapter.getDefaultAdapter()
                    ?: throw IllegalStateException("Bluetooth not supported")

                if (!adapter.isEnabled) {
                    throw IllegalStateException("Bluetooth is OFF")
                }

                ensureConnection(mac)
                val out = output ?: throw Exception("No connection")

                adapter.cancelDiscovery()

                // INIT
                out.write(byteArrayOf(0x1B, 0x40))

                // 🔔 BEEP
                out.write(byteArrayOf(0x1B, 0x42, 0x03, 0x02))

                // =============================
                // CENTER ALIGN (for logo & QR)
                // =============================
                out.write(byteArrayOf(0x1B, 0x61, 0x01))

                // =============================
                // PRINT LOGO (if exists)
                // =============================
                if (logoBitmap != null) {
                    val logoBytes = convertBitmapToRaster(logoBitmap)
                    out.write(logoBytes)
                    // 🔥 spacing after QR
                    //output.write(byteArrayOf(0x0A))
                }


                // =============================
                // PRINT QR (if exists)
                // =============================
                if (qrBitmap != null) {

                    // small gap before QR
                   // output.write(byteArrayOf(0x0A))

                    // 🛑 CRITICAL: wait BEFORE QR (buffer clear)
                    out.flush()
                    Thread.sleep(10)

                    // center QR
                    // print QR safely
                    printBitmapInChunks(out, qrBitmap)

                    // 🛑 CRITICAL: wait AFTER QR (finish printing)
                    out.flush()
                    Thread.sleep(20)
                    out.write(byteArrayOf(0x0A))

                    // single minimal feed
                    //output.write(byteArrayOf(0x0A))
                }

                // =============================
                // LEFT ALIGN TEXT
                // =============================
                out.write(byteArrayOf(0x1B, 0x61, 0x00))

                val safeText = text
                    .replace("\n", "\r\n")
                    .toByteArray(Charsets.US_ASCII)

                out.write(safeText)
                out.write(byteArrayOf(0x0A, 0x0A))

                // =============================
                // FEED + CUT
                // =============================
                out.write(byteArrayOf(
                    0x0A, 0x0A,   // safer than ESC d
                    0x1D, 0x56, 0x01
                ))

                out.flush()
                Thread.sleep(10)


                withContext(Dispatchers.Main) {
                    onResult(true)
                }

            } catch (e: Exception) {
                Log.d(TAG, "Print logo + text + QR failed", e)
                resetConnection()
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            } finally {
               // try { output?.close() } catch (_: Exception) {}
            }
        }
    }




    private fun convertBitmapToRaster(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val originalHeight = bitmap.height

//        val padding = 24 // 🔥 extra safety rows
        val height = originalHeight //+ padding

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

                    if (y < originalHeight && xPos < width) {
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

    private fun printBitmapInChunks(output: OutputStream, bitmap: Bitmap) {
        val chunkHeight = 24

        var y = 0
        while (y < bitmap.height) {

            val height = minOf(chunkHeight, bitmap.height - y)

            val chunk = Bitmap.createBitmap(
                bitmap,
                0,
                y,
                bitmap.width,
                height
            )

            val bytes = convertBitmapToRaster(chunk)

            output.write(bytes)
            output.flush()

            Thread.sleep(20)

            y += height
        }
    }





    fun resizeBitmap(bitmap: Bitmap, targetWidth: Int): Bitmap {
        val ratio = bitmap.height.toFloat() / bitmap.width
        val height = (targetWidth * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, targetWidth, height, true)
    }


    private fun ensureConnection(mac: String) {
        try {
            if (
                socket != null &&
                socket!!.isConnected &&
                output != null
            ) {
                try {
                    output!!.write(byteArrayOf(0x00))
                    output!!.flush()
                    return
                } catch (e: Exception) {

                    try {
                        output?.close()
                        socket?.close()
                    } catch (_: Exception) {}

                    socket = null
                    output = null
                }
            }

            val adapter = BluetoothAdapter.getDefaultAdapter()
                ?: throw IllegalStateException("Bluetooth not supported")

            if (!adapter.isEnabled) {
                throw IllegalStateException("Bluetooth is OFF")
            }

            val device = adapter.getRemoteDevice(mac)

            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)

            adapter.cancelDiscovery()
            socket!!.connect()

            output = socket!!.outputStream

            Log.e(TAG, "Bluetooth connected once")

        } catch (e: Exception) {
            Log.e(TAG, "Connection failed", e)
            socket = null
            output = null
            throw e
        }
    }


    private fun resetConnection() {
        try {
            output?.close()
        } catch (_: Exception) {}

        try {
            socket?.close()
        } catch (_: Exception) {}

        output = null
        socket = null

        Log.d("CON1", "Bluetooth connection reset")
    }



    // =============================
// PRINT IMAGE PRINT
// =============================
    fun printBitmap(
        mac: String,
        bitmap: Bitmap,
        onResult: (Boolean) -> Unit
    ) {

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val adapter =
                    BluetoothAdapter.getDefaultAdapter()
                        ?: throw Exception("Bluetooth not supported")

                if (!adapter.isEnabled) {
                    throw Exception("Bluetooth OFF")
                }

                adapter.cancelDiscovery()

                // =============================
                // CONNECT
                // =============================

                ensureConnection(mac)

                val out = output
                    ?: throw Exception("No output")

                // =============================
                // INIT
                // =============================

                out.write(
                    byteArrayOf(
                        0x1B,
                        0x40
                    )
                )

                // =============================
                // BEEP
                // =============================

                out.write(
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

                out.write(
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
                    output = out,
                    bitmap = bitmap,
                    chunkHeight = 48,
                    packetSize = 1024,
                    delayMs = 20
                )

                // =============================
                // FEED + CUT
                // =============================

                out.write(
                    byteArrayOf(
                        0x0A,
                        0x0A,
                        0x1D,
                        0x56,
                        0x01
                    )
                )

                out.flush()

                Thread.sleep(100)

                withContext(Dispatchers.Main) {
                    onResult(true)
                }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "BT bitmap failed",
                    e
                )

                resetConnection()

                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }



    private fun printBitmapInChunksBT(
        output: OutputStream,
        bitmap: Bitmap
    ){

        val chunkHeight = 48

        var y = 0


        while(y < bitmap.height){

            val height =
                minOf(
                    chunkHeight,
                    bitmap.height-y
                )


            val chunk =
                Bitmap.createBitmap(
                    bitmap,
                    0,
                    y,
                    bitmap.width,
                    height
                )


            val bytes =
                convertBitmapToRaster(chunk)


            var offset = 0

            val packetSize = 1024


            while(offset < bytes.size){

                val end =
                    minOf(
                        offset + packetSize,
                        bytes.size
                    )


                output.write(
                    bytes,
                    offset,
                    end-offset
                )

                output.flush()

                offset = end

                Thread.sleep(20)
            }


            y += height
        }
    }







}



