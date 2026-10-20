package dev.betterhunter.app.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val HunterColors = darkColorScheme(
    primary = Color(0xFFC3F28B),
    onPrimary = Color(0xFF17240F),
    primaryContainer = Color(0xFF293C21),
    onPrimaryContainer = Color(0xFFD3F7B0),
    secondary = Color(0xFFB6CBBE),
    onSecondary = Color(0xFF14251C),
    secondaryContainer = Color(0xFF26372E),
    onSecondaryContainer = Color(0xFFD6E9DD),
    background = Color(0xFF0E1311),
    onBackground = Color(0xFFF0F4EF),
    surface = Color(0xFF151C18),
    onSurface = Color(0xFFF0F4EF),
    surfaceVariant = Color(0xFF202B24),
    onSurfaceVariant = Color(0xFFA4B2A7),
    outline = Color(0xFF536057),
    outlineVariant = Color(0xFF2C3930),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF482624),
    onErrorContainer = Color(0xFFFFDAD6),
)
private val HunterTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 37.sp, letterSpacing = (-0.8).sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 21.sp, lineHeight = 27.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 23.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
)

@Composable
fun BetterHunterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HunterColors,
        typography = HunterTypography,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(18.dp),
            large = RoundedCornerShape(24.dp),
            extraLarge = RoundedCornerShape(28.dp),
        ),
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background, content = content)
    }
}
