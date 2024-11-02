plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)

    kotlin("kapt") // Apply the Kotlin KAPT plugin
    id("com.google.devtools.ksp") version "2.0.21-1.0.26"
}

android {
    namespace = "za.co.varsitycollege.st10204772.opsc7312_poe"
    compileSdk = 35

    defaultConfig {
        applicationId = "za.co.varsitycollege.st10204772.opsc7312_poe"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["redirectHostName"] = "auth"
        manifestPlaceholders["redirectSchemeName"] = "spotify-sdk"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging{
        @Suppress("DEPRECATION")
        exclude("META-INF/DEPENDENCIES")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    val room_version = "2.6.1"

    implementation ("androidx.room:room-ktx:$room_version")
    kapt ("androidx.room:room-compiler:$room_version")

    //CircleImageView Library
    implementation(libs.circleimageview)

    //Spotify Authentication
   // implementation(files("libs/spotify-auth-release-2.1.0.aar"))
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("com.spotify.android:auth:1.2.5")
    implementation (libs.androidx.browser)

    //Glide for image loading
    implementation (libs.glide)
    implementation(libs.firebase.storage.ktx)

    //Google SSO with OAuth2.0
    implementation (libs.androidx.credentials.v150alpha05)
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation (libs.googleid)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.google.play.services.auth)
    implementation(libs.androidx.core.ktx)

    implementation (libs.firebase.storage)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.common.ktx)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

