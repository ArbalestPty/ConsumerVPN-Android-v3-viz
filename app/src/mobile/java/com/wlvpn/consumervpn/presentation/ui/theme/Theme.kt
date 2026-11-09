package com.wlvpn.consumervpn.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.booleanResource
import androidx.core.view.WindowCompat
import com.wlvpn.consumervpn.R.bool

@Immutable
data class ExtendedColorScheme(
    val extendedColors: ColorFamily,
    val scheme: ColorScheme,
)

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

val extendedLight = ExtendedColorScheme(
    extendedColors = ColorFamily(
        accentIcon = accentIconLight,
        disconnectedColor = colorDisconnectButtonLight,
        gradientStartEnd = colorWindowBackgroundGradientStartEndLight,
        gradientCenter = colorWindowBackgroundGradientCenterLight,
        clickableTextColor = clickableTextLight,
        backgroundNavigationBar = colorBackgroundNavigationBarLight,
        controlHighlightColor = colorControlHighlightLight,
        backgroundLocationNormal = colorBackgroundLocationNormalLight,
        backgroundLocationHighlight = colorBackgroundLocationHighlightLight,
        backgroundItemLocation = colorBackgroundItemLocationLight,
        backgroundButtonLocation = colorBackgroundButtonLocationLight,
        controlNormalColor = colorControlNormalLight
    ),
    scheme = lightScheme,
)

val extendedDark = ExtendedColorScheme(
    extendedColors = ColorFamily(
        accentIcon = accentIconDark,
        disconnectedColor = colorDisconnectButtonDark,
        gradientStartEnd = colorWindowBackgroundGradientStartEndDark,
        gradientCenter = colorWindowBackgroundGradientCenterDark,
        clickableTextColor = clickableTextDark,
        backgroundNavigationBar = colorBackgroundNavigationBarDark,
        controlHighlightColor = colorControlHighlightDark,
        backgroundLocationNormal = colorBackgroundLocationNormalDark,
        backgroundLocationHighlight = colorBackgroundLocationHighlightDark,
        backgroundItemLocation = colorBackgroundItemLocationDark,
        backgroundButtonLocation = colorBackgroundButtonLocationDark,
        controlNormalColor = colorControlNormalDark
    ),
    scheme = darkScheme
)

@Immutable
data class ColorFamily(
    // Brand-coloured icons: connected shield, location pin, globe
    val accentIcon: Color,
    val disconnectedColor: Color,
    val gradientStartEnd: Color,
    val gradientCenter: Color,
    val clickableTextColor: Color,
    val backgroundNavigationBar: Color,
    val controlHighlightColor: Color,
    val backgroundLocationNormal: Color,
    val backgroundLocationHighlight: Color,
    val backgroundItemLocation: Color,
    val backgroundButtonLocation: Color,
    val controlNormalColor: Color,
)

//Used when you don't want to define extended colorFamily
val unspecified_scheme = ColorFamily(
    accentIcon = Color.Unspecified,
    disconnectedColor = Color.Unspecified,
    gradientStartEnd = Color.Unspecified,
    gradientCenter = Color.Unspecified,
    clickableTextColor = Color.Unspecified,
    backgroundNavigationBar = Color.Unspecified,
    controlHighlightColor = Color.Unspecified,
    backgroundLocationNormal = Color.Unspecified,
    backgroundLocationHighlight = Color.Unspecified,
    backgroundItemLocation = Color.Unspecified,
    backgroundButtonLocation = Color.Unspecified,
    controlNormalColor = Color.Unspecified
)

val LocalColors = staticCompositionLocalOf { extendedDark }

/**
 * Whether the app is showing its dark theme: either the system is in dark mode or the
 * theme_dark_theme_only flag forces it. Use this, not isSystemInDarkTheme(), to choose
 * theme-specific artwork.
 */
@Composable
fun isAppInDarkTheme(): Boolean =
    booleanResource(bool.theme_dark_theme_only) || isSystemInDarkTheme()

@Composable
fun AppTheme(
    darkTheme: Boolean = isAppInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) extendedDark else extendedLight
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.scheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalColors provides colorScheme) {
        MaterialTheme(
            colorScheme = colorScheme.scheme,
            typography = AppTypography,
            content = content
        )
    }
}


val MaterialTheme.dimens: Dimens
    @Composable
    @ReadOnlyComposable
    get() = LocalDimens.current

val MaterialTheme.elevation: Elevation
    @Composable
    @ReadOnlyComposable
    get() = LocalElevation.current