plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
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

    //CircleImageView Library
    implementation(libs.circleimageview)

    //Spotify Authentication
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("com.spotify.android:auth:1.2.5")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation (libs.androidx.browser)

    //Glide for image loading
    implementation (libs.glide)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.messaging.ktx)

    //Google SSO with OAuth2.0
    implementation (libs.androidx.credentials.v150alpha05)
    implementation(libs.google.play.services.auth)
    implementation(libs.play.services.location)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.google.play.services.auth)
    implementation (libs.googleid)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.google.play.services.auth)
    implementation(libs.androidx.core.ktx)

    //Fingerprint
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

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
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

