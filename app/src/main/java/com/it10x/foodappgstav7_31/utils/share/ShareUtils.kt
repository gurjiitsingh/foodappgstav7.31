package com.it10x.foodappgstav7_31.utils.share

import android.content.Context
import android.content.Intent
import android.net.Uri

object ShareUtils {

    fun shareFile(
        context: Context,
        uri: Uri,
        mimeType: String,
        title: String = "Share Receipt"
    ) {

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(intent, title)
        )
    }

    fun shareToWhatsApp(
        context: Context,
        uri: Uri,
        mimeType: String
    ) {

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            `package` = "com.whatsapp"
        }

        context.startActivity(intent)
    }

    fun shareSms(
        context: Context,
        message: String
    ) {

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:")
            putExtra("sms_body", message)
        }

        context.startActivity(intent)
    }
}