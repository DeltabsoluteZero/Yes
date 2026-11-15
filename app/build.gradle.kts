plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
}

android {
    namespace = "com.pixyroot"
    compileSdk = 37
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.pixyroot"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        // Flavor-specific values as defaults
        resValue("string", "app_name", "PixyRoot")
        resValue("string", "app_short_name", "PixyRoot")
        buildConfigField("boolean", "IS_PIXELSU", "false")
        buildConfigField("String", "ROOT_BACKEND", "\"pixyroot\"")
        manifestPlaceholders["appName"] = "PixyRoot"
        manifestPlaceholders["authority"] = "com.pixyroot.fileprovider"
    }

    // flavorDimensions += "appType"
    //
    // productFlavors {
    //     create("pixyroot") {
    //         dimension = "appType"
    //         applicationId = "com.pixyroot"
    //         versionNameSuffix = "-pixyroot"
    //         resValue("string", "app_name", "PixyRoot")
    //         resValue("string", "app_short_name", "PixyRoot")
    //         buildConfigField("boolean", "IS_PIXELSU", "false")
    //         buildConfigField("String", "ROOT_BACKEND", "\"pixyroot\"")
    //         manifestPlaceholders["appName"] = "PixyRoot"
    //         manifestPlaceholders["authority"] = "com.pixyroot.fileprovider"
    //     }
    //
    //     create("pixelsu") {
    //         dimension = "appType"
    //         applicationId = "com.pixelsu"
    //         versionNameSuffix = "-pixelsu"
    //         resValue("string", "app_name", "PixelSU")
    //         resValue("string", "app_short_name", "PixelSU")
    //         buildConfigField("boolean", "IS_PIXELSU", "true")
    //         buildConfigField("String", "ROOT_BACKEND", "\"pixelsu\"")
    //         manifestPlaceholders["appName"] = "PixelSU"
    //         manifestPlaceholders["authority"] = "com.pixelsu.fileprovider"
    //     }
    // }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        viewBinding = true
        dataBinding = false
        compose = false
        buildConfig = true
    }

    packagingOptions {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0"
            )
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // WorkManager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Biometric
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // Encryption
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Root/Shell
    implementation("eu.chainfire:libsuperuser:1.1.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}

kapt {
    correctErrorTypes = true
}
