import java.util.Properties
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id(BuildPlugins.androidApplication.module)
    id(BuildPlugins.androidKotlin.module)
    id(BuildPlugins.ksp.module)
    id(BuildPlugins.hiltPlugin.module)
    id(BuildPlugins.detekt.module) version BuildPlugins.detekt.version
    id(BuildPlugins.protoBuff.module) version BuildPlugins.protoBuff.version
    id(BuildPlugins.composeCompiler.module)
}

apply(plugin = BuildPlugins.androidJunit5ApplyName)

apply(from = "../config/quality/detekt/detekt-config.gradle")
apply(from = "../config/quality/lint/lint-config.gradle")

// ---- Brand configuration ----
// Identity and links come from brand.properties in the project root, or from the file passed with
// -PbrandFile=<path>. See documentation/ConsumerVPN App/REBRANDING.md.
val brandFile: File = rootProject.file(
    providers.gradleProperty("brandFile").getOrElse("brand.properties")
)
val brandProperties = loadProperties(brandFile)
    ?: throw GradleException("Brand file not found: ${brandFile.absolutePath}")

fun brand(key: String): String =
    brandProperties.getProperty(key)?.trim()?.takeIf { it.isNotEmpty() }
        ?: throw GradleException("Missing or empty '$key' in ${brandFile.name}")

// ---- Release signing ----
// Release keys must not live in the repository. They are read from signing.properties in the
// project root (ignored by git), from the file passed with -PsigningFile=<path>, or from the
// RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS and RELEASE_KEY_PASSWORD
// environment variables. If none are set, the sample staging key is used and a warning is shown.
val releaseSigningProperties: Properties = run {
    val file = rootProject.file(
        providers.gradleProperty("signingFile").getOrElse("signing.properties")
    )
    loadProperties(file) ?: Properties().apply {
        mapOf(
            "storeFile" to "RELEASE_STORE_FILE",
            "storePassword" to "RELEASE_STORE_PASSWORD",
            "keyAlias" to "RELEASE_KEY_ALIAS",
            "keyPassword" to "RELEASE_KEY_PASSWORD",
        ).forEach { (key, env) -> System.getenv(env)?.let { setProperty(key, it) } }
    }
}.let { configured ->
    if (configured.getProperty("storeFile").isNullOrBlank()) {
        logger.warn(
            "Release signing is not configured, so the sample staging key is used. " +
                "Do not publish this build. See signing.properties.example."
        )
        loadProperties(rootProject.file("config/keystores/sample-release-signing.properties"))
            ?: throw GradleException("Sample release signing properties are missing")
    } else {
        configured
    }
}

android {
    namespace = AndroidSdk.namespace
    compileSdk = AndroidSdk.compileSdk

    defaultConfig {
        // Set in brand.properties. Never change it after the first release.
        applicationId = brand("app.applicationId")
        minSdk = AndroidSdk.minSdk
        targetSdk = AndroidSdk.targetSdk
        versionCode = getVersionCode()
        versionName = getVersionName()
        buildConfigField("String", "CLIENT", "\"Android-${versionName}b${versionCode}\"")
        buildConfigField("String", "VPN_SDK_VERSION", "\"${Dependencies.vpnSdk.version}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "FLAGS_HOSTNAME", "\"${brand("flags.hostname")}\"")

        // Brand strings, generated from brand.properties
        resValue("string", "app_name", brand("app.name").escapeForAndroidString())
        mapOf(
            "url_forgot_password" to "url.forgotPassword",
            "url_sign_up" to "url.signUp",
            "url_contact_support" to "url.contactSupport",
            "url_terms_service" to "url.termsOfService",
            "url_policy" to "url.privacyPolicy",
            "url_about_us" to "url.aboutUs",
        ).forEach { (resourceName, key) ->
            resValue("string", resourceName, brand(key).escapeForAndroidString())
        }
    }

    // The debug key is for local development only. Release signing is configured above.
    signingConfigs {

        getByName("debug") {
            storeFile = rootProject.file("config/keystores/debug-consumer.jks")
            keyAlias = "consumervpn-v2"
            keyPassword = "5MLdn3mB7pCFJ7"
            storePassword = "5MLdn3mB7pCFJ7"
        }

        create("release") {
            storeFile = rootProject.file(releaseSigningProperties.getProperty("storeFile"))
            storePassword = releaseSigningProperties.getProperty("storePassword")
            keyAlias = releaseSigningProperties.getProperty("keyAlias")
            keyPassword = releaseSigningProperties.getProperty("keyPassword")
        }
    }

    setFlavorDimensions(listOf("platform"))

    productFlavors {
        create("mobile") {
            dimension = "platform"
            buildConfigField(type = "boolean", name = "IS_TV_BUILD", value = "false")
        }

        create("tv") {
            dimension = "platform"
            buildConfigField(type = "boolean", name = "IS_TV_BUILD", value = "true")
        }
    }

    buildTypes {
        debug  {
            isDebuggable = true
            applicationIdSuffix = ".debug"

            signingConfig = signingConfigs.getByName("debug")

        }

        create("qa") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")
        }

        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Dependencies.compose.version
    }
    packaging {
        jniLibs {
            // OpenVPN does not work without this when is run through an aab bundle
            useLegacyPackaging = true
        }
        resources {
            // Resolves build failure during ':app:mergeMobileDebugJavaResource'
            // Both BouncyCastle (bcprov) and JSpecify include this MANIFEST.MF file.
            // Excluding it prevents the DuplicateRelativeFileException and does not affect runtime.
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(Dependencies.kotlinCore.mergedId)
    implementation(Dependencies.lifecycle.mergedId)

    //Datastore
    implementation(Dependencies.dataStoreProto.mergedId)
    implementation(Dependencies.dataStoreCore.mergedId)
    implementation(Dependencies.dataStorePreferences.mergedId)

    // Compose
    implementation(platform(Dependencies.composeBom.mergedId))
    implementation(Dependencies.composeActivity.mergedId)
    implementation(Dependencies.composeUi.mergedId)
    implementation(Dependencies.composeUiGraphics.mergedId)
    implementation(Dependencies.composeUiPreview.mergedId)
    implementation(Dependencies.composeMaterial.mergedId)
    implementation(Dependencies.composeMaterialIcons.mergedId)
    implementation(Dependencies.composeNavigation.mergedId)
    implementation(Dependencies.composeRuntimeLivedata.mergedId)
    implementation(Dependencies.lifecycleRuntimeCompose.mergedId)
    implementation(Dependencies.googleFont.mergedId)
    implementation(Dependencies.accompanistPermissions.mergedId)

    // Coil
    implementation(Dependencies.coil.mergedId)

    //Dagger Hilt
    implementation(Dependencies.daggerHilt.mergedId)
    ksp(Dependencies.daggerHiltCompiler.mergedId)

    implementation(Dependencies.hiltNavigationCompose.mergedId)

    // Protobuff
    implementation(Dependencies.protoBuff.mergedId)

    // Logging
    implementation(Dependencies.logback.mergedId)
    implementation(Dependencies.sl4j.mergedId)
    implementation(Dependencies.timber.mergedId)

    // VPN SDK
    implementation(Dependencies.vpnSdk.mergedId)

    // Detekt plugins
    detektPlugins(Dependencies.detektFormatting.mergedId)
    detektPlugins(Dependencies.detektGitlabReport.mergedId)

    //Leak Canary
    debugImplementation (Dependencies.leakCanary.mergedId)

    // Testing
    testImplementation(Dependencies.junit.mergedId)
    testImplementation(Dependencies.mockk.mergedId)
    testImplementation(Dependencies.junitParams.mergedId)
    testRuntimeOnly(Dependencies.junitEngine.mergedId)

    // Splash
    implementation(Dependencies.splashScreen.mergedId)

    // TV
    "tvImplementation"(Dependencies.composeTv.mergedId)
    "tvImplementation"(Dependencies.zxingCore.mergedId)

    coreLibraryDesugaring(Dependencies.desugaringJavaApi.mergedId)

}

protobuf {
    protoc {
        artifact =  Dependencies.protoBuffArtifact.mergedId
    }

    generateProtoTasks {
        all().forEach { task ->
            task.plugins{
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

fun getVersionCode(): Int = Integer.valueOf(System.getenv("CI_PIPELINE_IID") ?: "1")

fun getVersionName(): String {
    val versionMajor = properties["version.major"]
    val versionMinor = properties["version.minor"]
    val versionPatch = properties["version.patch"]
    val pipeline = Integer.valueOf(System.getenv("CI_PIPELINE_IID") ?: "1")

    return "${versionMajor}.${versionMinor}.${versionPatch}.${pipeline}"
}

fun loadProperties(file: File): Properties? =
    file.takeIf { it.isFile }?.reader(Charsets.UTF_8)?.use { reader ->
        Properties().apply { load(reader) }
    }

// Escapes the characters that aapt treats specially in string resources.
fun String.escapeForAndroidString(): String =
    replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\"", "\\\"")
        .let { if (it.startsWith("@") || it.startsWith("?")) "\\$it" else it }
