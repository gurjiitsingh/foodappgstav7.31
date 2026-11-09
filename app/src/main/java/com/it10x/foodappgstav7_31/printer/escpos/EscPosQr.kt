package com.it10x.foodappgstav7_31.printer.escpos

import java.io.ByteArrayOutputStream

object EscPosQr {

    /**
     * Builds ESC/POS commands for printing a QR code.
     *
     * Works with ESC/POS printers supporting
     * the standard GS ( k QR commands.
     */
    fun build(
        data: String,
        size: Int = 6,
        errorCorrection: Int = 48
    ): ByteArray {

        require(data.isNotBlank()) {
            "QR data cannot be empty"
        }

        val output = ByteArrayOutputStream()

        // ==========================================
        // CENTER ALIGN
        // ==========================================

        output.write(
            byteArrayOf(
                0x1B,
                0x61,
                0x01
            )
        )

        // ==========================================
        // QR MODEL 2
        // GS ( k
        // ==========================================

        output.write(
            byteArrayOf(
                0x1D,
                0x28,
                0x6B,
                0x04,
                0x00,
                0x31,
                0x41,
                0x32,
                0x00
            )
        )

        // ==========================================
        // QR SIZE
        // ==========================================

        val qrSize =
            size.coerceIn(1, 16)

        output.write(
            byteArrayOf(
                0x1D,
                0x28,
                0x6B,
                0x03,
                0x00,
                0x31,
                0x43,
                qrSize.toByte()
            )
        )

        // ==========================================
        // ERROR CORRECTION
        //
        // 48 = L
        // 49 = M
        // 50 = Q
        // 51 = H
        // ==========================================

        output.write(
            byteArrayOf(
                0x1D,
                0x28,
                0x6B,
                0x03,
                0x00,
                0x31,
                0x45,
                errorCorrection.toByte()
            )
        )

        // ==========================================
        // STORE QR DATA
        // ==========================================

        val dataBytes =
            data.toByteArray(Charsets.UTF_8)

        val length =
            dataBytes.size + 3

        val pL =
            (length and 0xFF).toByte()

        val pH =
            ((length shr 8) and 0xFF).toByte()

        output.write(
            byteArrayOf(
                0x1D,
                0x28,
                0x6B,
                pL,
                pH,
                0x31,
                0x50,
                0x30
            )
        )

        output.write(dataBytes)

        // ==========================================
        // PRINT QR
        // ==========================================

        output.write(
            byteArrayOf(
                0x1D,
                0x28,
                0x6B,
                0x03,
                0x00,
                0x31,
                0x51,
                0x30
            )
        )

        // ==========================================
        // LINE FEED
        // ==========================================

        output.write(
            byteArrayOf(
                0x0A,
                0x0A
            )
        )

        return output.toByteArray()
    }
}