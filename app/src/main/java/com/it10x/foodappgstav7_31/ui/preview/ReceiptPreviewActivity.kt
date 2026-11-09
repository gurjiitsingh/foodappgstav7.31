package com.it10x.foodappgstav7_31.ui.preview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.io.File

class ReceiptPreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val path = intent.getStringExtra("image")

        if (path.isNullOrEmpty()) {
            finish()
            return
        }

        val file = File(path)

        if (!file.exists()) {
            finish()
            return
        }

        // ✅ FORCE FRESH LOAD (avoid cache issue)
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inMutable = true
        }

        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)

        if (bitmap == null) {
            finish()
            return
        }

        setContent {
            ReceiptPreviewScreen(bitmap)
        }
    }
}

@Composable
fun ReceiptPreviewScreen(bitmap: Bitmap) {

    val scrollState = androidx.compose.foundation.rememberScrollState()

    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .horizontalScroll(scrollState),
        contentAlignment = androidx.compose.ui.Alignment.TopCenter
    ) {
        androidx.compose.foundation.Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Preview",
            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            modifier = androidx.compose.ui.Modifier
                .wrapContentWidth()
                .wrapContentHeight()
        )
    }
}