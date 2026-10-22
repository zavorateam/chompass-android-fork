import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

fun bcString(value: String): String =
    "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

val secretsProps = Properties().apply {
    rootProject.file(".env").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
val geminiDebugApiKey: String = (secretsProps.getProperty("GEMINI_API_KEY") ?: "").trim()
val jevDebugApiKey: String = (secretsProps.getProperty("JEV_API_KEY") ?: "").trim()

android {
    namespace = "app.chompass"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aistudio.chompass.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 100
        versionName = "5.11.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("${rootDir}/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "GEMINI_API_KEY", bcString(""))
            buildConfigField("String", "JEV_API_KEY", bcString(""))
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("String", "GEMINI_API_KEY", bcString(geminiDebugApiKey))
            buildConfigField("String", "JEV_API_KEY", bcString(jevDebugApiKey))
        }
    }

    sourceSets {
        getByName("debug") {
            kotlin.srcDir("src/grounded/java")
        }
        getByName("release") {
            kotlin.srcDir("src/groundedStubs/java")
        }
        getByName("test") {
            kotlin.srcDir("src/grounded/java")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        optIn.addAll(
            "androidx.health.connect.client.ExperimentalHealthConnectApi",
            "androidx.compose.material3.ExperimentalMaterial3Api",
            "androidx.compose.foundation.ExperimentalFoundationApi",
            "androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "androidx.compose.ui.ExperimentalComposeUiApi",
        )
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.coil.compose)
    debugImplementation(libs.gson)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.health.connect)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.zxing.cpp.android)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.litertlm.android)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
