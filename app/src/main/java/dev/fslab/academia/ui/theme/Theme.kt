package dev.fslab.academia.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Cores customizadas para a aplicação que mudam conforme o tema
 */
data class AcademiaColors(
    val background: Color,
    val backgroundGradientStart: Color,
    val backgroundGradientEnd: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textOnPrimary: Color,
    val textInput: Color,
    val primary: Color,
    val primaryDark: Color,
    val iconGray: Color,
    val inputBorder: Color,
    val mediumGray: Color,
    val errorBackground: Color,
    val errorText: Color,
    val errorButton: Color,
    val error: Color,
    val successBackground: Color,
    val successText: Color,
    val success: Color,
    val lightGray: Color,
    val featureBlue: Color,
    val featureGreen: Color,
    val featureOrange: Color,
    val featureCyan: Color,
    val featurePink: Color,
    val featureRed: Color,
    val isDark: Boolean = false
)

/**
 * Cores para tema escuro — Fábrica 4 (Mapeado direto do Figma)
 */
val DarkAcademiaTesteColors = AcademiaColors(
    background = DarkBg,
    backgroundGradientStart = DarkBg,
    backgroundGradientEnd = DarkBgGradient,
    surface = SurfaceDark,
    textPrimary = TextWhite,
    textSecondary = TextGraySecondary,
    textTertiary = DarkTextTertiary,
    textOnPrimary = TextDarkOnPrimary,
    textInput = TextWhite,
    primary = PrimaryNeon,
    primaryDark = SecondaryDarkGreen,
    iconGray = IconMuted,
    inputBorder = DarkInputBorder,
    mediumGray = TextGraySecondary,
    errorBackground = ErrorBackground,
    errorText = ErrorText,
    errorButton = ErrorButton,
    error = ErrorText,
    successBackground = SuccessBackground,
    successText = SuccessText,
    success = PrimaryNeon,
    lightGray = DarkInputBg,
    featureBlue = CardBlue,
    featureGreen = CardGreen,
    featureOrange = CardOrange,
    featureCyan = Color(0xFF22D3EE),
    featurePink = CardPurple,
    featureRed = ErrorButton,
    isDark = true
)

/**
 * Cores para tema claro — Paleta verde orgânica (#f0f7da / #77ab59)
 */
val LightAcademiaColors = AcademiaColors(
    background = LightBg,
    backgroundGradientStart = LightBg,
    backgroundGradientEnd = LightBg,
    surface = SurfaceWhite,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textTertiary = TextTertiaryLight,
    textOnPrimary = LightTextOnPrimary,
    textInput = TextPrimaryLight,
    primary = LightPrimary,
    primaryDark = SecondaryDarkGreen,
    iconGray = TextSecondaryLight,
    inputBorder = InputBorderLight,
    mediumGray = LightGray,
    errorBackground = Color(0xFFFFEBEE),
    errorText = Color(0xFFB00020),
    errorButton = Color(0xFFB00020),
    error = Color(0xFFB00020),
    successBackground = Color(0xFFDCEFCC),
    successText = Color(0xFF3D7A27),
    success = LightPrimary,
    lightGray = SurfaceLight,
    featureBlue = CardBlue,
    featureGreen = CardGreen,
    featureOrange = CardOrange,
    featureCyan = Color(0xFF06B6D4),
    featurePink = CardPurple,
    featureRed = Color(0xFFB00020),
    isDark = false
)

val LocalAcademiaColors = compositionLocalOf { DarkAcademiaTesteColors }

/**
 * DarkColorScheme - Paleta Material 3
 */
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryNeon,
    secondary = SecondaryDarkGreen,
    tertiary = PrimaryNeon,
    background = DarkBg,
    surface = SurfaceDark,
    onPrimary = TextDarkOnPrimary,
    onSecondary = TextWhite,
    onTertiary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite
)

/**
 * LightColorScheme - Paleta Material 3
 */
private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = SecondaryDarkGreen,
    tertiary = LightPrimary,
    background = LightBg,
    surface = SurfaceWhite,
    onPrimary = LightTextOnPrimary,
    onSecondary = SurfaceWhite,
    onTertiary = LightTextOnPrimary,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight
)

@Composable
fun AcademiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val academiaColors = if (darkTheme) DarkAcademiaTesteColors else LightAcademiaColors

    CompositionLocalProvider(LocalAcademiaColors provides academiaColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography, // Lembre-se de configurar as fontes aqui!
            content = content
        )
    }
}
