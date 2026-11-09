package com.it10x.foodappgstav7_31.printer.common

object EscPosText {

    /**
     * Convert receipt text into the common ESC/POS byte stream.
     *
     * This function prints TEXT ONLY.
     *
     * CUT is intentionally NOT included here.
     * The caller decides when to cut.
     */
    fun build(text: String): ByteArray {

        val output = ArrayList<Byte>()

        // ==========================================
        // INIT
        // ==========================================

        output.add(0x1B)
        output.add(0x40)

        // ==========================================
        // BEEP
        // ==========================================

        output.add(0x1B)
        output.add(0x42)
        output.add(0x03)
        output.add(0x02)

        // ==========================================
        // TEXT
        // ==========================================

        val safeText = text
            .replace("\r\n", "\n")
            .replace("\n", "\r\n")
            .toByteArray(Charsets.US_ASCII)

        output.addAll(
            safeText.toList()
        )

        // ==========================================
        // FEED 3 LINES
        // ==========================================

        output.add(0x1B)
        output.add(0x64)
        output.add(0x03)

        return output.toByteArray()
    }
}