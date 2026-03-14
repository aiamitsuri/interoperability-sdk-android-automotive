plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "rust.android.car1"
    useLibrary("android.car")
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "rust.android.car1"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // noinspection GradlePath
    // compileOnly(files("K:/AndroidStudioSDK/platforms/android-36.1/optional/android.car.jar"))

    // --- THE RUST BRIDGE (Required) ---
    // JNA allows Kotlin to call your .so file
    //noinspection UseTomlInstead
    implementation("net.java.dev.jna:jna:5.18.1@aar")

    // Coroutines are required for your 'async' fetch_interoperability function
    //noinspection UseTomlInstead
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}