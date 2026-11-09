# Rebranding ConsumerVPN

This guide lists everything that changes when the app is rebranded for a client, and where each
item lives. Rebranding does not change the app's screens or behaviour.

A client brand has five parts:

1. [Identity and links](#1-identity-and-links): `brand.properties`
2. [Colour](#2-colour): `Color.kt` and `brand_colors.xml`
3. [Logo and icon artwork](#3-logo-and-icon-artwork): drawable and mipmap resources
4. [Optional imagery and type](#4-optional-imagery-and-type)
5. [Release signing](#5-release-signing): `signing.properties`, kept out of the repository

VPN account keys (`account_name`, `api_key` and so on) are separate. They are described in
[Getting started](../GETTING_STARTED_CONSUMER_VPN_APP.md#vpn-sdk-keys-setup).

## 1. Identity and links

Everything in this group is set in [`brand.properties`](../../brand.properties) in the project
root. The build generates the matching resources, so these values are not repeated anywhere else
in the code.

| Key | Used for | Generated as |
|---|---|---|
| `app.name` | Launcher label, system VPN settings, and text that mentions the app (kill switch help, TV About and forgot password screens) | `R.string.app_name` |
| `app.applicationId` | Google Play and on-device ID. Never change it after the first release. | `applicationId` |
| `url.forgotPassword` | Forgot password link (phone web view, TV text and QR code) | `R.string.url_forgot_password` |
| `url.signUp` | Sign-up link | `R.string.url_sign_up` |
| `url.contactSupport` | Settings › Contact support | `R.string.url_contact_support` |
| `url.termsOfService` | Settings › Terms of service | `R.string.url_terms_service` |
| `url.privacyPolicy` | Settings › Privacy policy | `R.string.url_policy` |
| `url.aboutUs` | TV About screen (text and QR code) | `R.string.url_about_us` |
| `flags.hostname` | Country flag images. Leave as is unless you host your own. | `BuildConfig.FLAGS_HOSTNAME` |

To keep several brands in one checkout, give each its own file and pass it to Gradle:

```bash
./gradlew assembleMobileRelease -PbrandFile=brands/acme.properties
```

Notes:

- Do not type the brand name into other strings. Use a `%1$s` placeholder and pass
  `stringResource(R.string.app_name)`, as the existing strings do.
- To translate the app name, add `app_name` to a localised `values-xx/strings.xml`.
- The TV QR codes are drawn at runtime from the configured URLs, so no QR image is needed.

## 2. Colour

There are two places, because some colours are drawn by Android before the app's Compose theme
is running.

### In-app colours: `Color.kt`

[`Color.kt`](../../app/src/main/java/com/wlvpn/consumervpn/presentation/ui/theme/Color.kt) holds
the Material 3 light and dark schemes plus a set of extended colours. Generate the Material 3
schemes from the client's 2–3 seed colours with Material Theme Builder, as described in
[App theming](APP_THEMING.md), then check the extended colours by hand. The most visible are:

| Token | Where it shows |
|---|---|
| `accentIcon` | Connected shield, location pin, globe icon (phone and TV) |
| `connectButtonColor…` / `primaryContainer` | Connect and Log in buttons |
| `clickableText` | Links and the selected location |
| `colorBackgroundNavigationBar` | Phone navigation bar |
| `colorWindowBackgroundGradient…` | Login screen gradient |
| `background` | Screen background |
| `colorTv…` | TV text, focused input and QR code colours |

The vector icons in `res/drawable` are re-tinted by the theme where they are shown, so their XML
fill colours do not need changing.

### Colours outside the app's screens: `brand_colors.xml`

| File | Contains |
|---|---|
| [`values/brand_colors.xml`](../../app/src/main/res/values/brand_colors.xml) | Light-theme values and the shared colours |
| [`values-night/brand_colors.xml`](../../app/src/main/res/values-night/brand_colors.xml) | Dark-theme values |
| [`tv/res/values/brand_colors.xml`](../../app/src/tv/res/values/brand_colors.xml) and `tv/res/values-night/brand_colors.xml` | TV values (dark only); keep the two files identical |

| Colour | Used for |
|---|---|
| `brand_accent` | Animated splash icon, notification highlight. Must equal `accentIcon` in `Color.kt`. |
| `brand_splash_background`, `brand_splash_icon` | Splash screen, light and dark |
| `brand_launcher_background`, `brand_launcher_background_tv` | Background layer of the adaptive launcher icons |
| `brand_notification_icon` | Notification accent |
| `brand_login_shape_…` | Solid shapes in the login background artwork |

The splash screen is shown before the app starts, so it follows the system light or dark
setting. If the app is set to dark only (`theme_dark_theme_only` in `bools.xml`), set the
light values in `values/brand_colors.xml` to the same as the dark ones.

## 3. Logo and icon artwork

Start from the client's master artwork (SVG) and export the following. Android Studio's
**New › Image Asset** and **New › Vector Asset** tools produce most of them.

| Asset | File(s) | Notes |
|---|---|---|
| Launcher icon, foreground layer | `drawable/ic_launcher_foreground.xml` | 108 × 108 dp; keep the mark inside the central 66 dp |
| Launcher icon, background layer | `brand_launcher_background` colour | |
| Monochrome (themed) icon | `mipmap-anydpi-v26/ic_launcher*.xml` | Uses the foreground layer by default. It must be a single-colour silhouette; supply a separate drawable if the foreground is not. |
| Legacy launcher bitmaps | `mipmap-*dpi/ic_launcher.webp`, `ic_launcher_round.webp` | For Android 7.x |
| Splash icon | `drawable/consumer_icon.xml` (Android 7–11), `drawable/avd_consumer_splash.xml` (Android 12+) | Fill colours reference `brand_splash_icon` and `brand_accent` |
| Notification icon | `drawable/notification_icon.xml` | White silhouette on transparent |
| Login logo | `drawable-*dpi/logo_consumer.png`, selected in `values/brand_drawables.xml` | Shown 56 dp high, up to 250 dp wide. Supply a light-theme variant if the logo would not show on the light login screen, and point `brand_logo_light_theme` to it. |
| TV launcher icon | `mipmap-*/ic_launcher_tv.png`, `mipmap-anydpi-v26/ic_launcher_tv.xml` | |
| TV banner | `mipmap-*/ic_banner_tv.png` | 320 × 180 dp |
| TV splash and login logo | as above | The TV app uses `brand_logo_dark_theme` |

Do not change in phase 1: the in-app icon set (tinted by the theme), Material navigation icons,
corner radii, layout and spacing dimensions, and flag images (served by WLVPN).

## 4. Optional imagery and type

| Item | File(s) | Notes |
|---|---|---|
| Login background shapes | `drawable/ic_bg_shapes_light.xml`, `ic_bg_shapes_dark.xml` | Solid colours come from `brand_login_shape_…`; the translucent overlays can stay. |
| Home screen world map | `drawable-*dpi/bg_dotmap.png` | The colour is part of the image, so a client colour needs new artwork. The current set only goes up to xhdpi (1120 × 568 px) and is upscaled on most phones. Supply 1680 × 852 (xxhdpi) and 2240 × 1136 px (xxxhdpi) versions too, or a single vector. Check it stays quiet on both light and dark backgrounds. |
| Fonts | `Type.kt` (phone and TV) | Any Google Font can be used by name |
| Dark theme only | `values/bools.xml` | See [App theming](APP_THEMING.md#dark-theme-only) |

## 5. Release signing

Each client release needs its own signing key, kept outside the repository. The build reads it
from one of these, in order:

1. The file passed with `-PsigningFile=<path>`
2. `signing.properties` in the project root (ignored by git); copy
   [`signing.properties.example`](../../signing.properties.example) to start
3. The environment variables `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`
   and `RELEASE_KEY_PASSWORD` (for CI)

If none is set, release and QA builds are signed with the sample staging key and Gradle prints a
warning. Do not publish such a build.

## Checklist

- [ ] `brand.properties`: name, application ID, all links
- [ ] `Color.kt`: Material 3 schemes regenerated, extended colours checked, `accentIcon` set
- [ ] `brand_colors.xml` (light, night, TV) matches `Color.kt`
- [ ] Launcher icon (foreground, background, monochrome, legacy bitmaps)
- [ ] Splash icons and notification icon
- [ ] Login logo, plus light-theme variant if needed
- [ ] TV icon, banner and logo (if the TV app is published)
- [ ] Optional: login shapes, world map, fonts, dark theme only
- [ ] Release signing configured outside the repository
- [ ] Checked on a device in light and dark themes, phone and TV
