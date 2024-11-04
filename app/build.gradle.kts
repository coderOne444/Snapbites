import org.apache.tools.ant.util.JavaEnvUtils.VERSION_1_8

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.jetpackcomposenew"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.jetpackcomposenew"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"  // Make sure this aligns with your Compose version
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    //firebase implements
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx:21.0.7")
    implementation("com.google.android.gms:play-services-location:latest_version")



    // Compose BOM (Bill of Materials) to ensure compatibility across Compose libraries
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.navigation:navigation-compose:2.5.3") // or the latest version

    // UI and foundation libraries for Compose
    implementation("androidx.compose.ui:ui:1.5.1")  // Updated to latest stable
    implementation("androidx.compose.foundation:foundation:1.5.1")  // Updated to latest stable
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.1")  // Updated to latest stable
    implementation("androidx.compose.ui:ui-tooling:1.5.1")  // Ensure latest version for debugging
    implementation("io.coil-kt:coil-compose:2.2.2")

    // Material 3 and extended icons
    implementation("androidx.compose.material3:material3:1.1.1")  // Use the latest stable version
    implementation("androidx.compose.material:material-icons-extended:1.5.1")  // Align with Compose versions

    // Navigation component for Jetpack Compose
    implementation("androidx.navigation:navigation-compose:2.7.1")  // Updated to latest stable

    // Google Play services
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation(libs.play.services.maps)

    // AndroidX libraries
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth.ktx)

    // Unit and UI testing dependencies
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter)
    androidTestImplementation(libs.androidx.junit)
    implementation("io.coil-kt:coil-compose:2.2.2")
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.1")  // Align with Compose versions

    // Debugging tools for Compose
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.1")  // Align with Compose versions
    debugImplementation(libs.androidx.ui.test.manifest)
}
