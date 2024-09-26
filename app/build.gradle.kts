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
    }
}

dependencies {

    //CircleImageView Library
    implementation(libs.circleimageview)

    //Spotify Authentication
    implementation(files("libs/spotify-auth-release-2.1.0.aar"))

    //Google SSO with OAuth2.0
    implementation (libs.androidx.credentials.v150alpha05)
    implementation (libs.androidx.credentials.play.services.auth)
    implementation (libs.googleid)

    // Import the BoM for the Firebase platform
    implementation(platform(libs.firebase.bom))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.firebase.auth)

    // Also add the dependency for the Google Play services library and specify its version
    implementation(libs.google.play.services.auth)

    //OTP
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
   // implementation(libs.firebase.ui.auth)
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")


    implementation(libs.androidx.core.ktx)
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
    implementation(libs.googleid)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

