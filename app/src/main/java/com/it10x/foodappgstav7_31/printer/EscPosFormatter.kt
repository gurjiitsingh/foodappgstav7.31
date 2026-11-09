package com.it10x.foodappgstav7_31.printer


object EscPosFormatter {


    // Double height + Double width
    fun big(): ByteArray {

        return byteArrayOf(
            0x1D,
            0x21,
            0x11
        )
    }


    // Normal size
    fun normal(): ByteArray {

        return byteArrayOf(
            0x1D,
            0x21,
            0x00
        )
    }


    // Bold ON
    fun boldOn(): ByteArray {

        return byteArrayOf(
            0x1B,
            0x45,
            0x01
        )
    }


    // Bold OFF
    fun boldOff(): ByteArray {

        return byteArrayOf(
            0x1B,
            0x45,
            0x00
        )
    }
}