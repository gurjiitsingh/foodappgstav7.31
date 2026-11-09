package com.it10x.foodappgstav7_31.printer





object EscPosCommands {

    // Alignment
    const val ALIGN_LEFT = "\u001B\u0061\u0000"
    const val ALIGN_CENTER = "\u001B\u0061\u0001"
    const val ALIGN_RIGHT = "\u001B\u0061\u0002"

    // Font
    const val FONT_A = "\u001B\u004D\u0000"
    const val FONT_B = "\u001B\u004D\u0001"

    // Bold
    const val BOLD_ON = "\u001B\u0045\u0001"
    const val BOLD_OFF = "\u001B\u0045\u0000"

    // Size
    const val NORMAL_SIZE = "\u001D\u0021\u0000"
    const val DOUBLE_HEIGHT = "\u001D\u0021\u0001"
    const val DOUBLE_WIDTH = "\u001D\u0021\u0010"
    const val DOUBLE_SIZE = "\u001D\u0021\u0011"

    // Underline
    const val UNDERLINE_ON = "\u001B\u002D\u0001"
    const val UNDERLINE_OFF = "\u001B\u002D\u0000"

    // Reset printer
    const val RESET = "\u001B@"

    // Cut
    const val CUT = "\u001D\u0056\u0000"









    // Feed Lines
    const val FEED = "\n"
}