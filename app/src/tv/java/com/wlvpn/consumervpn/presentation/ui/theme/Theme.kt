package com.wlvpn.consumervpn.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@Immutable
data class ExtendedColorScheme(
    val extendedColors: ColorFamily,
    val scheme: ColorScheme,
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
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
)

val extendedDark = ExtendedColorScheme(
    extendedColors = ColorFamily(
        accentIcon = accentIconDark,
        contentPrimary = colorTvContentPrimaryDark,
        contentSecondary = colorTvContentSecondaryDark,
        inputFocusedContainer = colorTvInputFocusedContainerDark,
        qrCodeForeground = colorTvQrCodeForegroundDark,
        qrCodeBackground = colorTvQrCodeBackgroundDark,
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
        controlNormalColor = colorControlNormalDark,
        connectButtonColor = connectButtonColorDark,
        connectHighlightedButtonColor = connectHighlightedButtonColorDark,
        connectSelectedButtonColor = connectSelectedButtonColorDark,
        disconnectButtonColor = disconnectButtonColorDark,
        disconnectHighlightedButtonColor = disconnectHighlightedButtonColorDark,
        disconnectSelectedButtonColor = disconnectSelectedButtonColorDark,
        connectingProgressIndicatorColor = connectingProgressIndicatorColorDark,
        ipRowLabelColor = ipRowLabelColorDark,
        ipRowIpColor = ipRowIpColorDark,
        locationRowLabelColor = locationRowLabelColorDark,
        locationRowLocationColor = locationRowLocationColorDark,
        locationRowItemHighlightColor = colorLocationRowItemHighlightDark,
        backgroundForgotPasswordHighlightedColor = colorBackgroundForgotPasswordHighlightedDark,
        forgotPasswordCloseHighlightedButtonColor = colorForgotPasswordCloseHighlightedDark,
        homeMenuButtonSelectedColor = colorHomeMenuButtonSelectedDark,
        settingsScreenScrimColor = colorSettingsScrimDark,
        settingsBackgroundColor = colorSettingsBackgroundDark,
        settingsItemFocusedContainerColor = colorSettingsItemFocusedContainerDark,
        settingsDialogBackgroundColor = colorSettingsDialogBackgroundDark,
        settingsDialogButtonFocusedColor = colorSettingsDialogButtonFocusedDark,
        aboutUsCloseHighlightedButtonColor = colorAboutUsCloseHighlightedDark
    ),
    scheme = darkScheme
)

@Immutable
data class ColorFamily(
    // Brand-coloured icons: connected shield, location pin
    val accentIcon: Color,
    // Text and icons on TV dialogs, settings rows and focused list items
    val contentPrimary: Color,
    val contentSecondary: Color,
    val inputFocusedContainer: Color,
    val qrCodeForeground: Color,
    val qrCodeBackground: Color,
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
    val connectButtonColor: Color,
    val connectHighlightedButtonColor: Color,
    val connectSelectedButtonColor: Color,
    val disconnectButtonColor: Color,
    val disconnectHighlightedButtonColor: Color,
    val disconnectSelectedButtonColor: Color,
    val connectingProgressIndicatorColor: Color,
    val ipRowLabelColor: Color,
    val ipRowIpColor: Color,
    val locationRowLabelColor: Color,
    val locationRowLocationColor: Color,
    val locationRowItemHighlightColor: Color,
    val backgroundForgotPasswordHighlightedColor: Color,
    val forgotPasswordCloseHighlightedButtonColor: Color,
    val homeMenuButtonSelectedColor: Color,
    val settingsScreenScrimColor: Color,
    val settingsBackgroundColor: Color,
    val settingsDialogBackgroundColor: Color,
    val settingsItemFocusedContainerColor: Color,
    val settingsDialogButtonFocusedColor: Color,
    val aboutUsCloseHighlightedButtonColor: Color
)

val LocalColors = staticCompositionLocalOf { extendedDark }
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = extendedDark
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.scheme.background.toArgb()
            window.decorView.setBackgroundColor(colorScheme.scheme.background.toArgb())
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
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