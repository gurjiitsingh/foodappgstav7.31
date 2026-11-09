import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.it10x.foodappgstav7_31.viewmodel.ThemeViewModel
import com.it10x.foodappgstav7_31.ui.theme.PosThemeMode

@Composable
fun ThemeSettingsScreen(vm: ThemeViewModel = viewModel()) {

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // 🔹 TOP CONTENT (PREVIEW AREA)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                "Theme Settings",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Divider()

            Text(
                "Live Preview",
                style = MaterialTheme.typography.titleMedium
            )

            // 👉 SIMPLE PREVIEW CARD (you can replace with POS UI later)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Primary Button",
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(onClick = {}) {
                        Text("Action")
                    }
                }
            }
        }

        // 🔥 BOTTOM THEME SELECTOR
        Surface(
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

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
                            .height(40.dp)
                            .wrapContentWidth()
                            .clip(RoundedCornerShape(50))
                            .background(bgColor)
                            .clickable { vm.setThemeMode(mode) }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.name,
                            color = contentColor,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

