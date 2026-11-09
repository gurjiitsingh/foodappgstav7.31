package com.it10x.foodappgstav7_31.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape   // ✅ correct
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_31.ui.theme.PosTheme

@Composable
fun TopBarNavButton(
    selected: Boolean,
    icon: ImageVector,
    description: String,
    size: Dp,
    shape: Shape,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    // 🎯 THEME COLORS
    val bgColor =
        if (selected) MaterialTheme.colorScheme.primary
        else Color.Transparent

    val contentColor =
        if (selected) MaterialTheme.colorScheme.onPrimary
        else PosTheme.topBar.content.copy(alpha = 0.7f)

    Box(
        modifier = Modifier
            .height(size)
            .clip(shape) // ripple follows shape
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = contentColor
            )

            Spacer(Modifier.width(6.dp))

            Text(
                text = description,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}