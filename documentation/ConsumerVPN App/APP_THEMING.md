# ConsumerVPN

This document will describe the theming aspects that can be updated in order
to customize the app's theme.

## Current approach

This version of ConsumerVPN is built using [Material3][1] guidelines. And 
[Jetpack Compose][2].

The base theme was generated using the [Material3 Theme builder][3] in combination with
the [Material3 Design Kit][4], the theme builder allows you to choose a base combination
of colors to be used by the components. The Material3 kit file allows a preview of all
the base components available, you can also add extended colors to the theme builder.

Typography can also be generated in the theme and all this configuration can be exported
as `Export -> Jetpack Compose (Theme.kt)`

## Generated Theme files

1. Color.kt
2. Theme.kt
3. Type.kt

### Color.kt file

This file contains the Tokens that will be referenced by the Theme.kt class when creating
the App's theme, by default the Material3 plugin will generate automatic number of schemes
in `dark/light` variants (3 if no extender colors were added or 6 otherwise)

The basic tokens are name according to Material3, usage on each component can be referenced 
on the [Material3][1] guidelines page.

You can remove any variant not needed in your implementation, this App is only using the 
base implementation with extended colors. We recommend using the suffix `dark/light` on any
extended color to be easily recognizable. 

Extended colors are named based on purpose so they can be easily understandable.

```kotlin
//Light scheme tokens example
val primaryLight = Color(0xFF3A608F)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFD3E4FF)
//Extended Colors Light
val colorDisconnectButtonLight = Color(0xFFCC8C89)
val colorWindowBackgroundGradientStartEndLight = Color(0xFFffffff)
val colorWindowBackgroundGradientCenterLight = Color(0xFFaeb1cb)
//Dark scheme tokens example
val primaryDark = Color(0xFFA3C9FE)
val onPrimaryDark = Color(0xFF00315C)
val primaryContainerDark = Color(0xFF1F4876)
//Extended Colors Dark
val colorWindowBackgroundGradientStartEndDark = Color(0xFF141720)
val colorWindowBackgroundGradientCenterDark = Color(0xFF3E414B)
val colorDisconnectButtonDark = Color(0xFFc9352e)
```

### Type.kt file

This class allows you to define a custom Typography to use in the app, when using the
[Material3 Theme builder][3] you can select a Typography to be used by the app, this
will generate the needed cert and the different typographies used by material 3 modifying 
the base font family with the desired one.

Something worth mentioning is that this file is the place were we can customize the 
different Typography properties as needed, size, letter spacing, line height and so on.

### Theme.kt file

This is the most important class for the customization of the app, since here we will be
using the other classes in order to create the app theme, we have some important components
that will be described next

#### Color Schemes

[Material3][1] uses this color schemes as the base for all the base components, if we set
a custom color scheme (we can define multiple depending on the app's need, like a dark/light
scheme if needed) all the base components we create will by default use this set of colors
without any extra configuration needed.

This implementation uses a combination of base(Material3) and extended Color Scheme in both
light/dark variants. For the extended schemes an object `ColorFamily` was created to contain
all the custom colors we needed. This object can be extended as needed to add more colors.

The tokens that we added to the Color.kt are referenced here.

#### LocalComposition

We define a localComposition of Colors, this gives a default color scheme to be used by the 
app in case the theme has not been started properly. This app uses the extendedDark color 
scheme by Default

```kotlin
val LocalColors = staticCompositionLocalOf { extendedDark }
```

#### App theme composable function

Then we have the theme setup function.

```kotlin
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> extendedDark
        else -> extendedLight
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.scheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
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
```

Here initially the colorScheme is defined base on the user theme selection by using
`isSystemInDarkTheme()`, next we have a section that defines the statusBar color, we
use the same background color for the statusBar to give an edge to edge aspect by design.

Lastly we we create a `CompositionLocalProvider` with the selected scheme, in this 
section the MaterialTheme is created, passing the generated colorScheme, typography
and other values that we want to define in the app theme. Dimens and Shapes can also 
be customized here.

## Adding a new color to the theme

If a new color is needed to customize the app this can be done following this simple steps.

1. A color should be added to the `ColorFamily` object that exist in Theme.kt, our recommendation
for naming is to use a purpose specific name.
```kotlin
@Immutable
data class ColorFamily(
    //Previously defined colors are here
    val myNewColor: Color,
)
```
2. This new color now needs to be defined as a token in the `Color.kt` file, if the
app is using both light/dark schemes a token need to be defined for each variant.
```kotlin
//Extended Colors Light
val myNewColorLight = Color(0xFFCC8C89)
//Extended Colors Dark
val myNewColorDark = Color(0xFF141720)
```
3. Now we need to modify the `ExtendedColorScheme` object/s on the `Theme.kt` class
referencing the newly added tokens.
```kotlin
val extendedDark = ExtendedColorScheme(
    extendedColors = ColorFamily(
        //Other extended colors present here
        myNewColor = myNewColorDark
    ),
    scheme = darkScheme
)
```

## Referencing the theme values withing the app.

After finishing setting up the theme this need to be called on the app. This application
is build using compose navigation, so we only use one `MainActivity`, in the onCreate()
method we define a compose navigation graph and inside `setContent` we reference our theme
created in `Theme.kt`.
```kotlin
AppTheme {
    //Everything defined within will use the values passed to our theme
}
```
Since we only use this activity to contain the app NavigationController and the bottom
navigation objects we don't need to instantiate it anywhere else.
Inside a `Composable` object like a screen we can reference the color or typographies 
like...
```kotlin
    LocalColors.current.extendedColors.gradientStartEnd
    MaterialTheme.typography.headlineLarge
    //Material Colors can also be accessed like this (extended colors cannot)
    MaterialTheme.colorScheme.primary
```

## Splash Screen

For the splash screen we are using the [`core-splashscreen`][5] dependency, can be found on
the :app `build.gradle` file.
This allows the usage of animated icons for the splash screen. Animated icons can
be implemented for API 31+ by creating an [Animated Vector Drawable(AVD)][6], and referencing such
asset from a Theme that has `Theme.SplashScreen` defined as parent. This will be the 
theme used by the app. Some properties defined here are:
1. `windowSplashScreenBackground` The color to be used as background for the splash screen. It is
set to `brand_splash_background`, which has light and dark values in `brand_colors.xml`
2. `postSplashScreenTheme` Theme to be used after the splash screen is shown
3. `windowSplashScreenAnimatedIcon` The icon displayed on the splash screen. This app contains
an AVD referenced on the `v31/themes.xml` file, and also a static icon defined in `/themes.xml`
Note: On API < 31, if the value of windowSplashScreenAnimatedIcon is an adaptive icon , it will be 
cropped and scaled. The workaround is to respectively assign windowSplashScreenAnimatedIcon and 
windowSplashScreenIconBackgroundColor to the values of the adaptive icon foreground and background
4. `windowSplashScreenAnimationDuration` This defines the duration for the splash screen
and needs to be 0 if using AVD.

## Dark theme only

If you wish for your application to exclusively use a dark theme, regardless of the system's current 
theme preference, you can achieve this by modifying a specific configuration flag within your app's 
resources. Here's how to enforce dark mode only:

1. Go to the `bools.xml` [file](../../app/src/main/res/values/bools.xml).
2. Change the flag `theme_dark_theme_only` to `true`.
3. The splash screen is shown before the app starts, so it still follows the system setting. Set
the light-theme splash colours in `values/brand_colors.xml` to the same values as
`values-night/brand_colors.xml`.
4. Re-build the app.

In Compose code, use `isAppInDarkTheme()` rather than `isSystemInDarkTheme()` when choosing
theme-specific artwork, so the flag is respected.

[1]: https://m3.material.io/
[2]: https://developer.android.com/compose
[3]: https://www.figma.com/community/plugin/1034969338659738588/material-theme-builder
[4]: https://www.figma.com/community/file/1035203688168086460
[5]: https://developer.android.com/reference/kotlin/androidx/core/splashscreen/SplashScreen
[6]: https://developer.android.com/reference/android/graphics/drawable/AnimatedVectorDrawable