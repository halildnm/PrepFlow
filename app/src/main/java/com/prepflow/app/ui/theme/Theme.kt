package com.prepflow.app.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext

private val FallbackDarkColorScheme = darkColorScheme(
    primary = PrepPrimary,
    onPrimary = PrepOnPrimary,
    primaryContainer = PrepPrimaryContainer,
    onPrimaryContainer = PrepOnPrimaryContainer,
    secondary = PrepSecondary,
    onSecondary = PrepOnSecondary,
    secondaryContainer = PrepSecondaryContainer,
    onSecondaryContainer = PrepOnSecondaryContainer,
    tertiary = PrepTertiary,
    onTertiary = PrepOnTertiary,
    tertiaryContainer = PrepTertiaryContainer,
    onTertiaryContainer = PrepOnTertiaryContainer,
    background = PrepDarkBackground,
    onBackground = PrepOnBackground,
    surface = PrepDarkSurface,
    onSurface = PrepOnSurface,
    surfaceVariant = PrepDarkSurfaceVariant,
    onSurfaceVariant = PrepOnSurfaceVariant,
    surfaceContainer = PrepDarkSurfaceContainer,
    outline = PrepOutline,
    outlineVariant = PrepOutlineVariant,
    error = StatusUrgentRed
)

@Immutable
data class GlassTokens(
    val canvasDeep: Color,
    val canvasAmbientTop: Color,
    val canvasAmbientBottom: Color,
    val cardFill: Color,
    val cardFillElevated: Color,
    val cardBorder: Color,
    val cardHighlightBorder: Color,
    val navBarFill: Color,
    val navBarBorder: Color
)

val LocalGlassTokens = staticCompositionLocalOf {
    GlassTokens(
        canvasDeep = PrepDarkBackground,
        canvasAmbientTop = PrepPrimary.copy(alpha = 0.16f),
        canvasAmbientBottom = PrepTertiary.copy(alpha = 0.12f),
        cardFill = Color(0xFF141B2D).copy(alpha = 0.72f),
        cardFillElevated = Color(0xFF1A233A).copy(alpha = 0.84f),
        cardBorder = Color.White.copy(alpha = 0.11f),
        cardHighlightBorder = PrepPrimary.copy(alpha = 0.42f),
        navBarFill = Color(0xFF111827).copy(alpha = 0.88f),
        navBarBorder = Color.White.copy(alpha = 0.14f)
    )
}

private fun buildGlassTokens(scheme: ColorScheme): GlassTokens {
    val deepBase = Color(0xFF080B12)
    val tintedDeep = scheme.primary.copy(alpha = 0.04f).compositeOver(deepBase)
    val tintedCard = scheme.surfaceVariant.copy(alpha = 0.48f).compositeOver(tintedDeep)
    val elevatedCard = scheme.surfaceContainerHighest.copy(alpha = 0.62f).compositeOver(tintedDeep)
    val navFill = scheme.surfaceContainerHigh.copy(alpha = 0.78f).compositeOver(deepBase)

    return GlassTokens(
        canvasDeep = tintedDeep,
        canvasAmbientTop = scheme.primary.copy(alpha = 0.18f),
        canvasAmbientBottom = scheme.tertiary.copy(alpha = 0.14f),
        cardFill = tintedCard.copy(alpha = 0.74f),
        cardFillElevated = elevatedCard.copy(alpha = 0.86f),
        cardBorder = Color.White.copy(alpha = 0.11f),
        cardHighlightBorder = scheme.primary.copy(alpha = 0.48f),
        navBarFill = navFill.copy(alpha = 0.90f),
        navBarBorder = scheme.primary.copy(alpha = 0.20f)
    )
}

@Composable
fun PrepFlowTheme(
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val rawScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context)
    } else {
        FallbackDarkColorScheme
    }

    val deepCanvas = rawScheme.primary.copy(alpha = 0.035f).compositeOver(Color(0xFF080B12))
    val tunedScheme = rawScheme.copy(
        background = deepCanvas,
        surface = rawScheme.surface.copy(alpha = 0.92f).compositeOver(deepCanvas)
    )
    val glassTokens = buildGlassTokens(tunedScheme)

    CompositionLocalProvider(LocalGlassTokens provides glassTokens) {
        MaterialTheme(
            colorScheme = tunedScheme,
            typography = PrepFlowTypography,
            content = content
        )
    }
}
