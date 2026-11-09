package com.it10x.foodappgstav7_31.ui.bill

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PaymentButton(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            )
            .clip(RoundedCornerShape(12.dp))
            .background(color.darken(0.35f))
            .padding(bottom = 3.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            color.lighten(0.25f),
                            color,
                            color.darken(0.12f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

        }
    }
}

private fun Color.lighten(amount: Float): Color =
    Color(
        red = (red + (1f - red) * amount).coerceAtMost(1f),
        green = (green + (1f - green) * amount).coerceAtMost(1f),
        blue = (blue + (1f - blue) * amount).coerceAtMost(1f),
        alpha = alpha
    )

private fun Color.darken(amount: Float): Color =
    Color(
        red = (red * (1f - amount)).coerceAtLeast(0f),
        green = (green * (1f - amount)).coerceAtLeast(0f),
        blue = (blue * (1f - amount)).coerceAtLeast(0f),
        alpha = alpha
    )

//package com.it10x.foodappgstav7_31.ui.bill
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//@Composable
//fun PaymentButton(
//    modifier: Modifier = Modifier,
//    text: String,
//    color: Color,
//    onClick: () -> Unit
//) {
//    Box(
//        modifier = modifier
//            .height(48.dp)
//            .shadow(
//                elevation = 1.dp,
//                shape = RoundedCornerShape(12.dp),
//                clip = false
//            )
//            .clip(RoundedCornerShape(12.dp))
//            .background(color.darken(0.35f))
//            .padding(bottom = 3.dp)
//            .clickable(onClick = onClick),
//        contentAlignment = Alignment.Center
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .clip(RoundedCornerShape(10.dp))
//                .background(
//                    Brush.verticalGradient(
//                        colors = listOf(
//                            color.lighten(0.25f),
//                            color,
//                            color.darken(0.12f)
//                        )
//                    )
//                ),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = text,
//                fontSize = 13.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.White
//            )
//        }
//    }
//}
//
//private fun Color.lighten(amount: Float): Color =
//    Color(
//        red = (red + (1f - red) * amount).coerceAtMost(1f),
//        green = (green + (1f - green) * amount).coerceAtMost(1f),
//        blue = (blue + (1f - blue) * amount).coerceAtMost(1f),
//        alpha = alpha
//    )
//
//private fun Color.darken(amount: Float): Color =
//    Color(
//        red = (red * (1f - amount)).coerceAtLeast(0f),
//        green = (green * (1f - amount)).coerceAtLeast(0f),
//        blue = (blue * (1f - amount)).coerceAtLeast(0f),
//        alpha = alpha
//    )