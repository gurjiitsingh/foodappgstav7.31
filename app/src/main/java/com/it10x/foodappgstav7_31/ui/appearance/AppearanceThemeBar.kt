package com.it10x.foodappgstav7_31.ui.appearance



import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.it10x.foodappgstav7_31.viewmodel.ThemeViewModel
import com.it10x.foodappgstav7_31.ui.theme.PosThemeMode

/**
 * 🎨 Appearance Theme Bottom Bar
 *
 * ✔ Used for selecting app theme
 * ✔ Shows as bottom strip
 * ✔ Instant UI update
 */
@Composable
fun AppearanceThemeBar(
    vm: ThemeViewModel = viewModel()
) {
    val themeModeString by vm.themeMode.collectAsState()
    val themeMode = PosThemeMode.valueOf(themeModeString)

    val modes = listOf(
        PosThemeMode.AUTO,
        PosThemeMode.CLASSIC,
        PosThemeMode.MIDNIGHT,
        PosThemeMode.GSTA,
        PosThemeMode.OCEAN,
        PosThemeMode.SLATE,
        PosThemeMode.LATTE
    )

    Surface(
        tonalElevation = 12.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Spacer(modifier = Modifier.width(8.dp))

            modes.forEach { mode ->

                val isSelected = themeMode == mode

                val bgColor = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant

                val contentColor = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .height(42.dp)
                        .clip(RoundedCornerShape(50))
                        .background(bgColor)
                        .clickable {
                            vm.setThemeMode(mode)
                        }
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.name.replace("_", " "),
                        color = contentColor,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}