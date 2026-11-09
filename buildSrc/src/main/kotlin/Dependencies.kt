object BuildPlugins {

    // Used in normal the 'new' plugin block
    val androidApplication = Dependency(
        module = "com.android.application",
        version = "8.11.0"
    )
    val ksp = Dependency(
        module = "com.google.devtools.ksp",
        version = "2.2.0-2.0.2"
    )
    val androidKotlin = Dependency(
        module = "org.jetbrains.kotlin.android",
        version = "2.2.0"
    )
    val composeCompiler = Dependency(
        module="org.jetbrains.kotlin.plugin.compose",
        version = "2.2.0"
    )
    val detekt = Dependency(
        module = "io.gitlab.arturbosch.detekt",
        version = "1.21.0"
    )
    val protoBuff = Dependency(
        module = "com.google.protobuf",
        version = "0.9.4"
    )
    val kotlinKsp = Dependency(
        module = "com.google.devtools.ksp",
        version = "1.5.30-1.0.0-beta09"
    )
    val mavenPublish = Dependency(
        module = "maven-publish"
    )
    val kotlinKapt = Dependency(
        module = "kotlin-kapt"
    )

    val kotlinParcelize = Dependency(
        module = "kotlin-parcelize"
    )
    val hiltPlugin = Dependency(
        module = "com.google.dagger.hilt.android",
        version = "2.56.2"
    )

    // Used in the traditional buildScript/classpath block
    val ciUtilsPlugin = Dependency(
        group = "com.gentlebreeze.gradle",
        module = "ci-utils",
        version = "1.5.0.245426"
    )

    val slackUtilsPlugin = Dependency(
        group = "com.gentlebreeze.gradle",
        module = "slack-utils",
        version = ciUtilsPlugin.version
    )

    val androidJunit5 = Dependency(
        group = "de.mannodermaus.gradle.plugins",
        module = "android-junit5",
        version = "1.13.1.0"
    )
    const val androidJunit5ApplyName = "de.mannodermaus.android-junit5"

    val dokka = Dependency(
        group = "org.jetbrains.dokka",
        module = "dokka-gradle-plugin",
        version = "1.9.20"
    )
    const val dokkaApplyName = "org.jetbrains.dokka"

    val hilt = Dependency(
        group = "com.google.dagger",
        module = "hilt-android-gradle-plugin",
        version = hiltPlugin.version
    )

    val googleServicesPlugin = Dependency(
        module = "com.google.gms.google-services",
        version = "4.4.2"
    )

    val crashlyticsPlugin = Dependency(
        module = "com.google.firebase.crashlytics",
        version = "3.0.3"
    )

    val kotlinxSerialization = Dependency(
        module = "org.jetbrains.kotlin.plugin.serialization",
        version = "1.8.10"
    )
}

object AndroidSdk {

    const val minSdk = 25
    const val targetSdk = 36
    const val compileSdk = 36
    const val namespace = "com.wlvpn.consumervpn"
}

object Dependencies {

    // kotlin
    val kotlinCore = Dependency(
        group = "androidx.core",
        module = "core-ktx",
        version = "1.17.0"
    )

    // VPN Module
    val vpnModule = Dependency(
        group = "com.gentlebreeze.vpn.module",
        module = "VPNModule-API",
        version = "2.12.338913"
    )
    val vpnModuleOpenVPN = Dependency(
        group = vpnModule.group,
        module = "VPNModule-API-OpenVPN",
        version = vpnModule.version
    )
    val vpnModuleWireGuard = Dependency(
        group = vpnModule.group,
        module = "VPNModule-API-StrongSwan",
        version = vpnModule.version
    )
    val vpnModuleStrongSwan = Dependency(
        group = vpnModule.group,
        module = "VPNModule-API-WireGuard",
        version = vpnModule.version
    )

    // Native protocols
    val nativeOpenVpn = Dependency(
        group = "com.gentlebreeze.vpn.openvpn",
        module = "NativeOpenVPN",
        version = "2.1.0338542@aar"
    )
    val nativeStrongSwan = Dependency(
        group = "com.gentlebreeze.vpn.strongswan",
        module = "NativeStrongSwan",
        version = "2.3.0338621"
    )
    val nativeWireGuard = Dependency(
        group = "com.netprotect.nativewireguard",
        module = "NativeWireGuard",
        version = "1.0.20211029.492.338623"
    )

    // Ok Interceptors
    val okInterceptors = Dependency(
        group = "com.netprotect.okinterceptors",
        module = "lib",
        version = "1.0.338624"
    )

    // Retrofit
    val retrofit = Dependency(
        group = "com.squareup.retrofit2",
        module = "retrofit",
        version = "2.11.0"
    )

    val retrofitMoshiConverter = Dependency(
        group = "com.squareup.retrofit2",
        module = "converter-moshi",
        version = retrofit.version
    )

    // Detekt
    val detektFormatting = Dependency(
        group = "io.gitlab.arturbosch.detekt",
        module = "detekt-formatting",
        version = BuildPlugins.detekt.version
    )
    val detektGitlabReport = Dependency(
        group = "com.gitlab.cromefire",
        module = "detekt-gitlab-report",
        version = "0.2.2"
    )

    // Dagger
    val dagger = Dependency(
        group = "com.google.dagger",
        module = "dagger-android",
        version = "2.43.2"
    )
    val daggerAnnotation = Dependency(
        group = dagger.group,
        module = "dagger-android-processor",
        version = dagger.version
    )
    val daggerCompiler = Dependency(
        group = dagger.group,
        module = "dagger-compiler",
        version = dagger.version
    )

    // Timber
    val timber = Dependency(
        group = "com.jakewharton.timber",
        module = "timber",
        version = "5.0.1"
    )

    // Package cloud
    val packagecloud = Dependency(
        group = "io.packagecloud.maven.wagon",
        module = "maven-packagecloud-wagon",
        version = "0.0.6"
    )

    // Nativencrkeyption
    val nativencrkeyption =
        Dependency(
            group = "com.netprotect.nativencrkeyption",
            module = "nativencrkeyption",
            version = "1.1.0338543"
        )

    // DataStore
    val dataStoreProto = Dependency(
        group = "androidx.datastore",
        module = "datastore",
        version = "1.1.2"
    )
    val dataStorePreferences = Dependency(
        group = dataStoreProto.group,
        module = "datastore-preferences",
        version = dataStoreProto.version
    )
    val dataStoreCore = Dependency(
        group = dataStoreProto.group,
        module = "datastore-core",
        version = dataStoreProto.version
    )

    // ProtoBuf
    val protoBuff = Dependency(
        group = "com.google.protobuf",
        module = "protobuf-javalite",
        version = "4.28.3"
    )
    val protoBuffArtifact = Dependency(
        group = protoBuff.group,
        module = "protoc",
        version = "4.28.3"
    )

    // Mockk
    val mockk = Dependency(
        group = "io.mockk",
        module = "mockk",
        version = "1.14.4"
    )

    // Junit
    val junit = Dependency(
        group = "org.junit.jupiter",
        module = "junit-jupiter-api",
        version = "5.13.1"
    )
    val junitParams = Dependency(
        group = junit.group,
        module = "junit-jupiter-params",
        version = junit.version
    )
    val junitEngine = Dependency(
        group = junit.group,
        module = "junit-jupiter-engine",
        version = junit.version
    )

    // Lifecycle
    val lifecycle = Dependency(
        group = "androidx.lifecycle",
        module = "lifecycle-runtime-ktx",
        version = "2.8.7"
    )

    val lifecycleRuntimeCompose = Dependency(
        group = lifecycle.group,
        module = "lifecycle-runtime-compose",
        version = lifecycle.version
    )
    val lifecycleCompose = Dependency(
        group = lifecycle.group,
        module = "lifecycle-viewmodel-compose",
        version = lifecycle.version
    )

    // Compose
    val compose = Dependency(
        version = "2.2.0"
    )

    val composeRuntime = Dependency(
        group = "androidx.compose.runtime",
        module = "runtime",
    )

    val composeRuntimeLivedata = Dependency(
        group = composeRuntime.group,
        module = "runtime-livedata",
    )

    val composeBom = Dependency(
        group = "androidx.compose",
        module = "compose-bom",
        version = "2025.05.01"
    )

    val composeUi = Dependency(
        group = "androidx.compose.ui",
        module = "ui",
    )

    val composeUiGraphics = Dependency(
        group = composeUi.group,
        module = "ui-graphics",
    )

    val composeUiPreview = Dependency(
        group = composeUi.group,
        module = "ui-tooling",
    )

    val composeUiToolingPreview = Dependency(
        group = composeUi.group,
        module = "ui-tooling-preview",
    )

    val composeMaterial = Dependency(
        group = "androidx.compose.material3",
        module = "material3",
    )

    val composeMaterialIcons = Dependency(
        group = "androidx.compose.material",
        module = "material-icons-extended",
    )

    val composeFoundation = Dependency(
        group = "androidx.compose.foundation",
        module = "foundation",
    )

    val composeFoundationLayout = Dependency(
        group = composeFoundation.group,
        module = "foundation-layout",
    )

    val composeActivity = Dependency(
        group = "androidx.activity",
        module = "activity-compose",
        version = "1.10.1"
    )

    val composeNavigation = Dependency(
        group = "androidx.navigation",
        module = "navigation-compose",
        version = "2.8.9"
    )

    val composeLottie = Dependency(
        group = "com.airbnb.android",
        module = "lottie-compose",
        version = "6.1.0"
    )

    val kiwiComposeNavigation = Dependency(
        group = "com.kiwi.navigation-compose.typed",
        module = "core",
        version = "0.10.0"
    )

    val composeTv = Dependency(
        group = "androidx.tv",
        module = "tv-material",
        version = "1.0.0"
    )

    // QR code generation (TV forgot password and About screens)
    val zxingCore = Dependency(
        group = "com.google.zxing",
        module = "core",
        version = "3.5.3"
    )

    val kotlinxSerialization = Dependency(
        group = "org.jetbrains.kotlinx",
        module = "kotlinx-serialization-core",
        version = "1.5.0"
    )

    val coil = Dependency(
        group = "io.coil-kt",
        module = "coil-compose",
        version = "2.7.0"
    )

    val accompanistSwipeRefresh = Dependency(
        group = "com.google.accompanist",
        module = "accompanist-swiperefresh",
        version = "0.25.0"
    )

    val accompanistNavigation = Dependency(
        group = "com.google.accompanist",
        module = "accompanist-navigation-animation",
        version = "0.30.1"
    )

    val accompanistSystemUiController = Dependency(
        group = "com.google.accompanist",
        module = "accompanist-systemuicontroller",
        version = "0.34.0"
    )

    val accompanistPermissions = Dependency(
        group = "com.google.accompanist",
        module = "accompanist-permissions",
        version = "0.36.0"
    )

    //Material
    val materialAndroid = Dependency(
        group = "com.google.android.material",
        module = "material",
        version = "1.12.0"
    )

    val googleFont = Dependency(
        group = "androidx.compose.ui",
        module = "ui-text-google-fonts",
    )
    
    //Hilt
    val daggerHilt = Dependency(
        group = dagger.group,
        module = "hilt-android",
        version = BuildPlugins.hiltPlugin.version
    )

    val daggerHiltCompiler = Dependency(
        group = daggerHilt.group,
        module = "hilt-android-compiler",
        version = daggerHilt.version
    )

    val hiltCompiler = Dependency(
        group = "androidx.hilt",
        module = "hilt-compiler",
        version = "1.2.0"
    )

    val hiltNavigationCompose = Dependency(
        group = hiltCompiler.group,
        module = "hilt-navigation-compose",
        version = hiltCompiler.version
    )

    //Splash screen =
    val splashScreen = Dependency(
        group = kotlinCore.group,
        module = "core-splashscreen",
        version = "1.0.1"
    )

    val coroutinesAndroid = Dependency(
        group = "org.jetbrains.kotlinx",
        module = "kotlinx-coroutines-android",
        version = "1.10.2"
    )

    val firebase = Dependency(
        group = "com.google.firebase",
        module = "firebase-bom",
        version = "33.14.0"
    )

    val firebaseCrashlytics = Dependency(
        group = firebase.group,
        module = "firebase-crashlytics-ktx",
    )

    val firebaseAnalytics = Dependency(
        group = firebase.group,
        module = "firebase-analytics-ktx"
    )

    // Leak canary

    val leakCanary = Dependency(
        group = "com.squareup.leakcanary",
        module = "leakcanary-android",
        version = "2.14"
    )

    // Logback
    val logback = Dependency(
        group = "com.github.tony19",
        module = "logback-android",
        version = "3.0.0"
    )

    // Sl4j
    val sl4j = Dependency(
        group = "org.slf4j",
        module = "slf4j-api",
        version = "2.0.7"
    )

    val vpnSdk = Dependency(
        group = "com.wlvpn.vpnsdk",
        module = "sdkv2",
        version = "2.8.2.364333"
    )

    val desugaringJavaApi = Dependency(
        group = "com.android.tools",
        module = "desugar_jdk_libs_nio",
        version = "2.0.3"
    )
}
