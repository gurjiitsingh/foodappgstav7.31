package com.it10x.foodappgstav7_31.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// =====================================================
// THEME MODES
// =====================================================

enum class PosThemeMode {
    AUTO,
    CLASSIC,
    MIDNIGHT,
    GSTA,
    OCEAN,
    SLATE,
    LATTE
}
// =====================================================
// COLOR DATA CLASSES
// =====================================================

data class PosAccentColors(
    val cartAddBg: Color,
    val cartAddText: Color,
    val cartRemoveBorder: Color,
    val cartRemoveText: Color,

    val primaryActionBg: Color,
    val primaryActionText: Color
)

data class PosProductColors(
    val productCardBg: Color,
    val productCardText: Color
)

data class PosTopBarColors(
    val background: Color,
    val content: Color
)

data class PosCategoryColors(
    val selectedBg: Color,
    val selectedText: Color,
    val unselectedBg: Color,
    val unselectedText: Color
)

data class PosTopBarButtonColors(
    val selectedBg: Color,
    val selectedContent: Color,
    val unselectedBg: Color,
    val unselectedContent: Color
)

data class PosBillColors(
    val billBg: Color,
    val billText: Color,
    val billTab: Color,

    // Input fields
    val inputBg: Color,
    val inputBorder: Color,
    val inputActiveBg: Color,
    val inputActiveBorder: Color,

    // Common actions
    val success: Color,
    val warning: Color,
    val danger: Color
)

// =====================================================
// GLOBAL ACCESS
// =====================================================

object PosTheme {
    lateinit var accent: PosAccentColors
        internal set
    lateinit var product: PosProductColors
        internal set
    lateinit var topBar: PosTopBarColors
        internal set
    lateinit var category: PosCategoryColors
        internal set
    lateinit var topBarButton: PosTopBarButtonColors
        internal set

    lateinit var bill: PosBillColors
        internal set
}

// =====================================================
// MAIN THEME
// =====================================================

@Composable
fun FoodPosTheme(
    mode: PosThemeMode = PosThemeMode.AUTO,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()

    val finalMode = when (mode) {
        PosThemeMode.AUTO -> if (isSystemDark) PosThemeMode.MIDNIGHT else PosThemeMode.CLASSIC
        else -> mode
    }

    val colorScheme = when (finalMode) {



//        PosThemeMode.CLASSIC -> {
//
//            PosTheme.accent = PosAccentColors(
//                cartAddBg = Color(0xFFFB8500),       // Strong orange (CTA)
//                cartAddText = Color.White,
//
//                cartRemoveBorder = Color(0xFF219EBC), // Blue border
//                cartRemoveText = Color(0xFF219EBC),
//
//                primaryActionBg = Color(0xFF219EBC),  // Main action (blue)
//                primaryActionText = Color.White
//            )
//
//            PosTheme.product = PosProductColors(
//                productCardBg = Color.White,
//                productCardText = Color(0xFF023047)   // Deep navy text
//            )
//
//            PosTheme.topBar = PosTopBarColors(
//                background = Color(0xFF023047),       // Premium dark header
//                content = Color.White
//            )
//
//            PosTheme.category = PosCategoryColors(
//                selectedBg = Color(0xFFFFB703),       // Yellow highlight
//                selectedText = Color(0xFF023047),
//
//                unselectedBg = Color(0xFF8ECAE6),     // Soft blue
//                unselectedText = Color(0xFF023047)
//            )
//
//            PosTheme.topBarButton = PosTopBarButtonColors(
//                selectedBg = Color(0xFF219EBC),
//                selectedContent = Color.White,
//
//                unselectedBg = Color(0xFF8ECAE6),
//                unselectedContent = Color(0xFF023047)
//            )
//
//            PosTheme.bill = PosBillColors(
//                billBg = Color.White,
//                billText = Color(0xFF023047),
//                billTab = Color(0xFFF8FAFC),
//
//                inputBg = Color(0xFFF8FAFC),
//                inputBorder = Color(0xFFB0BEC5),
//                inputActiveBg = Color(0xFFE3F2FD),
//                inputActiveBorder = Color(0xFF219EBC),
//
//                success = Color(0xFF2E7D32),
//                warning = Color(0xFFFB8500),
//                danger = Color(0xFFC62828)
//            )
//
//            lightColorScheme(
//                primary = Color(0xFF219EBC),
//                onPrimary = Color.White,
//
//                background = Color(0xFFF8FAFC),
//                onBackground = Color(0xFF023047),
//
//                surface = Color.White,
//                onSurface = Color(0xFF023047),
//
//                error = Color(0xFFDC2626)
//            )
//
//
//        }

//        PosThemeMode.CLASSIC -> {
//
//            // =====================================================
//            // ACCENT COLORS — CART & PRIMARY ACTIONS
//            // =====================================================
//            PosTheme.accent = PosAccentColors(
//
//                // Add Item — Primary Blue
//                cartAddBg = Color(0xFF3F74DF),
//                cartAddText = Color.White,
//
//                // Remove Item — Terracotta
//                cartRemoveBorder = Color(0xFFB85334),
//                cartRemoveText = Color(0xFFB85334),
//
//                // Main action buttons — Blue
//                primaryActionBg = Color(0xFF3F74DF),
//                primaryActionText = Color.White
//            )
//
//
//            // =====================================================
//            // PRODUCT COLORS
//            // =====================================================
//            PosTheme.product = PosProductColors(
//
//                // Product tiles — Light Ice Blue
//                productCardBg = Color(0xFFC9DBF8),
//                productCardText = Color(0xFF003366)
//            )
//
//
//            // =====================================================
//            // TOP BAR
//            // =====================================================
//            PosTheme.topBar = PosTopBarColors(
//
//                // Muted Slate Blue
//                background = Color(0xFF94A4CF),
//                content = Color.White
//            )
//
//
//            // =====================================================
//            // CATEGORY BUTTONS
//            // =====================================================
//            PosTheme.category = PosCategoryColors(
//
//                // Selected category — Lime Green
//                selectedBg = Color(0xFF9BC71B),
//                selectedText = Color(0xFF1E3A00),
//
//                // Unselected category — Light Ice Blue
//                unselectedBg = Color(0xFFC9DBF8),
//                unselectedText = Color(0xFF003366)
//            )
//
//
//            // =====================================================
//            // TOP BAR BUTTONS
//            // =====================================================
//            PosTheme.topBarButton = PosTopBarButtonColors(
//
//                // Selected button — Strong Blue
//                selectedBg = Color(0xFF3F74DF),
//                selectedContent = Color.White,
//
//                // Unselected button — Slate Blue
//                unselectedBg = Color(0xFF94A4CF),
//                unselectedContent = Color.White
//            )
//
//
//            // =====================================================
//            // BILL COLORS
//            // =====================================================
//            PosTheme.bill = PosBillColors(
//
//                // Bill panel — White
//                billBg = Color.White,
//                billText = Color(0xFF003366),
//
//                // Bill tab — Slate Blue
//                billTab = Color(0xFF94A4CF),
//
//                // Input fields
//                inputBg = Color(0xFFF5F9FF),
//                inputBorder = Color(0xFF94A4CF),
//
//                // Active input — Light Sky Blue
//                inputActiveBg = Color(0xFFDCEBFA),
//                inputActiveBorder = Color(0xFF3F74DF),
//
//                // Status colors
//                success = Color(0xFF2E7D32),
//                warning = Color(0xFFDEB04A),
//                danger = Color(0xFFB85334)
//            )
//
//
//            // =====================================================
//            // MATERIAL 3 COLOR SCHEME
//            // =====================================================
//// MATERIAL 3 COLOR SCHEME
//            lightColorScheme(
//
//                // Main screen background
//                background = Color(0xFFDCEBFA),
//                onBackground = Color(0xFF003366),
//
//                // Cards and panels
//                surface = Color.White,
//                onSurface = Color(0xFF003366),
//
//                // Secondary surfaces
//                surfaceVariant = Color(0xFFC9DBF8),
//                onSurfaceVariant = Color(0xFF003366),
//
//                // ...other colors
//            )
//        }

        PosThemeMode.CLASSIC -> {

            // =====================================================
            // CLASSIC COLOR CONSTANTS
            // =====================================================
            val classicBlue = Color(0xFF1976D2)
            val classicDarkBlue = Color(0xFF1565C0)
            val classicLightBlue = Color(0xFFD6E8FA)

            val classicOrange = Color(0xFFFF9800)
            val classicGrey = Color(0xFF9E9E9E)
            val classicDarkGrey = Color(0xFF888888)

            val classicGreen = Color(0xFF4CAF50)
            val classicLightGreen = Color(0xFFE8F5E9)

            val classicTerracotta = Color(0xFFB85334)
            val classicWhite = Color.White
            val classicText = Color(0xFF202124)
            val classicLightGrey = Color(0xFFF1F3F5)
            val classicBorder = Color(0xFFE0E0E0)

            // =====================================================
            // ACCENT COLORS — CART & PRIMARY ACTIONS
            // =====================================================
            PosTheme.accent = PosAccentColors(

                // Add Item — Blue
                cartAddBg = classicBlue,
                cartAddText = classicWhite,

                // Remove Item — Terracotta
                cartRemoveBorder = classicTerracotta,
                cartRemoveText = classicTerracotta,

                // Main action buttons — Blue
                primaryActionBg = classicBlue,
                primaryActionText = classicWhite
            )

            // =====================================================
            // PRODUCT COLORS
            // =====================================================
            PosTheme.product = PosProductColors(
                productCardBg = classicLightGrey,
                productCardText = classicText
            )

            // =====================================================
            // TOP BAR
            // =====================================================
            PosTheme.topBar = PosTopBarColors(
                background = classicDarkBlue,
                content = classicWhite
            )

            // =====================================================
            // CATEGORY BUTTONS
            // =====================================================
            PosTheme.category = PosCategoryColors(

                // Selected category — Blue
                selectedBg = classicBlue,
                selectedText = classicWhite,

                // Unselected category — Light Grey
                unselectedBg =  Color(0xFFF1F3F5),
                unselectedText = classicText
            )

            // =====================================================
            // TOP BAR BUTTONS
            // =====================================================
            PosTheme.topBarButton = PosTopBarButtonColors(

                // Selected button — Blue
                selectedBg = Color(0xFFFF9800),
                selectedContent = classicWhite,

                // Unselected button — Dark Blue
                unselectedBg = classicDarkBlue,
                unselectedContent = classicWhite
            )

            // =====================================================
            // BILL COLORS
            // =====================================================
            PosTheme.bill = PosBillColors(

                // Bill panel
                billBg = classicWhite,
                billText = classicText,

                // Bill tab
                billTab = classicDarkBlue,

                // Input fields
                inputBg = classicWhite,
                inputBorder = classicBorder,

                // Active input
                inputActiveBg = classicLightGrey,
                inputActiveBorder = classicBlue,

                // Status colors
                success = classicGreen,
                warning = Color(0xFFFFC107),
                danger = classicTerracotta
            )

            // =====================================================
            // MATERIAL 3 COLOR SCHEME
            // =====================================================
            lightColorScheme(

                // Primary — Blue
                primary = classicBlue,
                onPrimary = classicWhite,
                primaryContainer = classicLightBlue,
                onPrimaryContainer = Color(0xFF003366),

                // Secondary — Grey
                secondary = classicGrey,
                onSecondary = classicWhite,
                secondaryContainer = classicLightGrey,
                onSecondaryContainer = classicText,

                // Tertiary — Orange
                tertiary = classicOrange,
                onTertiary = classicWhite,
                tertiaryContainer = Color(0xFFFFE0B2),
                onTertiaryContainer = Color(0xFF663C00),

                // Background
                background = classicLightGrey,
                onBackground = classicText,

                // Surfaces
                surface = classicWhite,
                onSurface = classicText,
                surfaceVariant = classicLightGrey,
                onSurfaceVariant = Color(0xFF555555),

                // Borders
                outline = classicDarkGrey,
                outlineVariant = classicBorder,

                // Error
                error = classicTerracotta,
                onError = classicWhite,
                errorContainer = Color(0xFFFFF8E1),
                onErrorContainer = classicText,

                // Additional Material 3 colors
                scrim = Color(0x99000000),
                inverseSurface = Color(0xFF30343B),
                inverseOnSurface = classicWhite,
                inversePrimary = Color(0xFF90CAF9)
            )
        }


        // ================= DARK =================
        PosThemeMode.MIDNIGHT -> {

            PosTheme.accent = PosAccentColors(
                cartAddBg = Color(0xFFF97316),
                cartAddText = Color.White,
                cartRemoveBorder = Color(0xFFF97316),
                cartRemoveText = Color(0xFFF97316),

                primaryActionBg = Color(0xFFF97316),
                primaryActionText = Color.White
            )

            PosTheme.product = PosProductColors(
                productCardBg = Color(0xFF1E293B),
                productCardText = Color.White
            )

            PosTheme.topBar = PosTopBarColors(
                background = Color(0xFF1E293B),
                content = Color.White
            )

            PosTheme.category = PosCategoryColors(
                selectedBg = Color(0xFFF97316),
                selectedText = Color.White,
                unselectedBg = Color(0xFF334155),
                unselectedText = Color.White.copy(alpha = 0.7f)
            )

            PosTheme.topBarButton = PosTopBarButtonColors(
                selectedBg = Color(0xFFF97316),
                selectedContent = Color.White,
                unselectedBg = Color.White.copy(alpha = 0.08f),
                unselectedContent = Color.White.copy(alpha = 0.7f)
            )

            PosTheme.bill = PosBillColors(
                billBg = Color(0xFF1E293B),
                billText = Color.White,
                billTab = Color(0xFF0F172A),

                inputBg = Color(0xFF273548),
                inputBorder = Color(0xFF475569),
                inputActiveBg = Color(0xFF334155),
                inputActiveBorder = Color(0xFFF97316),

                success = Color(0xFF22C55E),
                warning = Color(0xFFF97316),
                danger = Color(0xFFEF4444)
            )

            darkColorScheme(
                primary = Color(0xFFF97316),
                onPrimary = Color.White,
                background = Color(0xFF0F172A),
                onBackground = Color.White,
                surface = Color(0xFF1E293B),
                onSurface = Color.White,
                error = Color(0xFFDC2626)
            )

        }

        // ================= GSTA =================
        PosThemeMode.GSTA -> {

            PosTheme.accent = PosAccentColors(
                cartAddBg = Color(0xFFFACC15),       // Premium yellow
                cartAddText = Color(0xFF111827),

                cartRemoveBorder = Color(0xFFEF4444), // Red
                cartRemoveText = Color(0xFFEF4444),

                primaryActionBg = Color(0xFFFACC15), // Main POS buttons
                primaryActionText = Color(0xFF111827)
            )

            PosTheme.product = PosProductColors(
                productCardBg = Color(0xFF1F2937),    // Dark card
                productCardText = Color.White
            )

            PosTheme.topBar = PosTopBarColors(
                background = Color(0xFF111827),       // Dark header
                content = Color.White
            )

            PosTheme.category = PosCategoryColors(
                selectedBg = Color(0xFFFACC15),       // Yellow selected
                selectedText = Color(0xFF111827),

                unselectedBg = Color(0xFF1F2937),
                unselectedText = Color.White.copy(alpha = 0.75f)
            )

            PosTheme.topBarButton = PosTopBarButtonColors(
                selectedBg = Color(0xFFFACC15),
                selectedContent = Color(0xFF111827),

                unselectedBg = Color.White.copy(alpha = 0.08f),
                unselectedContent = Color.White.copy(alpha = 0.85f)
            )
            PosTheme.bill = PosBillColors(
                billBg = Color(0xFF1F2937),
                billText = Color.White,
                billTab = Color(0xFF111827),

                inputBg = Color(0xFF27303D),
                inputBorder = Color(0xFF4B5563),
                inputActiveBg = Color(0xFF374151),
                inputActiveBorder = Color(0xFFFACC15),

                success = Color(0xFF22C55E),
                warning = Color(0xFFFACC15),
                danger = Color(0xFFEF4444)
            )

            darkColorScheme(
                primary = Color(0xFFFACC15),          // Yellow
                onPrimary = Color(0xFF111827),

                background = Color(0xFF0B1120),       // Deep dark
                onBackground = Color.White,

                surface = Color(0xFF1F2937),
                onSurface = Color.White,

                error = Color(0xFFEF4444)             // Red
            )

        }


        // ================= OCEAN BLUE=================
        PosThemeMode.OCEAN -> {

            // =====================================================
            // ACCENT COLORS — CART & ACTIONS
            // =====================================================
            PosTheme.accent = PosAccentColors(
                // Add to Cart — Deep Ocean Blue
                cartAddBg = Color(0xFF035AA6),
                cartAddText = Color.White,

                // Remove Item — Blue border
                cartRemoveBorder = Color(0xFF049DD9),
                cartRemoveText = Color(0xFF049DD9),

                // Primary Action — Deep Ocean Blue
                primaryActionBg = Color(0xFF035AA6),
                primaryActionText = Color.White
            )

            // =====================================================
            // PRODUCT COLORS
            // =====================================================
            PosTheme.product = PosProductColors(
                productCardBg = Color.White,
                productCardText = Color(0xFF035AA6)
            )

            // =====================================================
            // TOP BAR
            // =====================================================
            PosTheme.topBar = PosTopBarColors(
                background = Color(0xFF035AA6),
                content = Color.White
            )

            // =====================================================
            // CATEGORY BUTTONS
            // =====================================================
            PosTheme.category = PosCategoryColors(
                // Selected category — Blue
                selectedBg = Color(0xFF049DD9),
                selectedText = Color.White,

                // Unselected category — Light blue
                unselectedBg = Color(0xFFE3F2FD),
                unselectedText = Color(0xFF035AA6)
            )

            // =====================================================
            // TOP BAR BUTTONS
            // =====================================================
            PosTheme.topBarButton = PosTopBarButtonColors(
                selectedBg = Color(0xFF049DD9),
                selectedContent = Color.White,

                unselectedBg = Color.Transparent,
                unselectedContent = Color.White.copy(alpha = 0.85f)
            )

            // =====================================================
            // BILL COLORS
            // =====================================================
            PosTheme.bill = PosBillColors(
                billBg = Color.White,
                billText = Color(0xFF035AA6),
                billTab = Color(0xFFE3F2FD),

                inputBg = Color(0xFFF5F9FF),
                inputBorder = Color(0xFFBFD6E5),
                inputActiveBg = Color(0xFFDCEBFA),
                inputActiveBorder = Color(0xFF049DD9),

                success = Color(0xFF2E7D32),
                warning = Color(0xFF1976D2),
                danger = Color(0xFFC62828)
            )

            // =====================================================
            // MATERIAL 3 COLOR SCHEME
            // =====================================================
            lightColorScheme(
                primary = Color(0xFF035AA6),
                onPrimary = Color.White,

                secondary = Color(0xFF049DD9),
                onSecondary = Color.White,

                // Light sky-blue main background
//                background = Color(0xFFB8D7F5),
//                onBackground = Color(0xFF035AA6),

                background = Color(0xFFC6DDF4),
                onBackground = Color(0xFF035AA6),

                // White cards and panels
                surface = Color.White,
                onSurface = Color(0xFF035AA6),

                // Secondary surfaces
                surfaceVariant = Color(0xFFE3F2FD),
                onSurfaceVariant = Color(0xFF035AA6),

//                outline = Color(0xFFBFD6E5),
                outline = Color(0xFF7899C4),
                error = Color(0xFFC62828),
                onError = Color.White
            )
        }

        // ================= SLATE BLUESLATE =================



        PosThemeMode.SLATE -> {

            // ACTION BUTTONS
            PosTheme.accent = PosAccentColors(

                // Add Item — Deep Teal
                cartAddBg = Color(0xFF0F766E),
                cartAddText = Color.White,

                // Remove item — red outlined action
                cartRemoveBorder = Color(0xFFC62828),
                cartRemoveText = Color(0xFFC62828),

                // Main action — deep navy
                primaryActionBg = Color(0xFF243B53),
                primaryActionText = Color.White
            )

            // PRODUCT CARDS
            PosTheme.product = PosProductColors(
                productCardBg = Color.White,
                productCardText = Color(0xFF243B53)
            )

            // TOP BAR — DEEP NAVY
            PosTheme.topBar = PosTopBarColors(
                background = Color(0xFF243B53),
                content = Color.White
            )

            // CATEGORY BUTTONS
            PosTheme.category = PosCategoryColors(
                selectedBg = Color(0xFF0F766E),
                selectedText = Color.White,

                unselectedBg = Color(0xFFE9EFF5),
                unselectedText = Color(0xFF334E68)
            )

            // TOP BAR BUTTONS
            PosTheme.topBarButton = PosTopBarButtonColors(
                selectedBg = Color.White,
                selectedContent = Color(0xFF243B53),

                unselectedBg = Color(0xFF334E68),
                unselectedContent = Color.White
            )

            // BILL SCREEN
            PosTheme.bill = PosBillColors(
                billBg = Color.White,
                billText = Color(0xFF243B53),
                billTab = Color(0xFF243B53),

                inputBg = Color.White,
                inputBorder = Color(0xFFCBD5E1),
                inputActiveBg = Color(0xFFF0FDFA),
                inputActiveBorder = Color(0xFF0F766E),

                success = Color(0xFF2E7D32),
                warning = Color(0xFFB7791F),
                danger = Color(0xFFC62828)
            )

            // LIGHT MATERIAL COLOR SCHEME
            lightColorScheme(
                primary = Color(0xFF243B53),
                onPrimary = Color.White,

                secondary = Color(0xFF0F766E),
                onSecondary = Color.White,

                background = Color(0xFFF4F6F8),
                onBackground = Color(0xFF243B53),

                surface = Color.White,
                onSurface = Color(0xFF243B53),

                surfaceVariant = Color(0xFFE9EFF5),
                onSurfaceVariant = Color(0xFF334E68),

                outline = Color(0xFFCBD5E1),

                error = Color(0xFFC62828),
                onError = Color.White
            )
        }


        // ================= LATTE combination of light coffee=================
        PosThemeMode.LATTE -> {

            PosTheme.accent = PosAccentColors(
                cartAddBg = Color(0xFFE8CCC5),       // warm soft peach (CTA)
                cartAddText = Color(0xFF1E293B),

                cartRemoveBorder = Color(0xFF807E79),
                cartRemoveText = Color(0xFF807E79),

                primaryActionBg = Color(0xFF807E79), // main brand action
                primaryActionText = Color.White
            )

            PosTheme.product = PosProductColors(
                productCardBg = Color.White,
                productCardText = Color(0xFF1E293B)
            )

            PosTheme.topBar = PosTopBarColors(
                background = Color(0xFFE3E3E3),      // soft neutral header
                content = Color(0xFF1E293B)
            )

            PosTheme.category = PosCategoryColors(
                selectedBg = Color(0xFFCFD4AE),
                selectedText = Color(0xFF1E293B),

                unselectedBg = Color(0xFFF6E7C6),
                unselectedText = Color(0xFF807E79)
            )

            PosTheme.topBarButton = PosTopBarButtonColors(
                selectedBg = Color(0xFFBDCED3),
                selectedContent = Color(0xFF1E293B),

                unselectedBg = Color.Transparent,
                unselectedContent = Color(0xFF807E79)
            )

            PosTheme.bill = PosBillColors(
                billBg = Color.White,
                billText = Color(0xFF1E293B),
                billTab = Color(0xFFE3E3E3),

                inputBg = Color(0xFFFAFAFA),
                inputBorder = Color(0xFFD6D3D1),
                inputActiveBg = Color(0xFFF6F3EF),
                inputActiveBorder = Color(0xFF807E79),

                success = Color(0xFF4CAF50),
                warning = Color(0xFFE8CCC5),
                danger = Color(0xFFD32F2F)
            )
            lightColorScheme(
                primary = Color(0xFF807E79),
                onPrimary = Color.White,

                background = Color.White,            // ✅ changed to pure white
                onBackground = Color(0xFF1E293B),

                surface = Color.White,
                onSurface = Color(0xFF1E293B),

                error = Color(0xFFDC2626)
            )

        }


        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}





//
//0xFFFFFFFF
//
//Dialog background — White
//
//Copy
//
//0xFF202124
//
//Main text — Dark
//
//Copy
//
//0xFF3F74DF
//
//Selected option — Classic Blue
//
//Copy
//
//0xFFF1F3F5
//
//Unselected option — Light Gray
//
//Copy
//
//0xFFFFFFFF
//
//Input background — White
//
//Copy
//
//0xFF888888
//
//Input border — Gray
//
//Copy
//
//0xFF3F74DF
//
//Focused input border — Blue
//
//Copy
//
//0xFFFFC107
//
//Credit — Amber
//
//Copy
//
//0xFF9E9E9E
//
//Pay Later — Gray
//
//Copy
//
//0xFF4CAF50
//
//Cash — Green
//
//Copy
//
//0xFF1976D2
//
//Card — Blue
//
//Copy
//
//0xFFFF9800
//
//UPI — Orange
//
//Copy
//
//0xFF9C27B0
//
//More options — Purple
//
//Copy