import org.bouncycastle.util.Properties


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}



android {
    namespace = "za.co.varsitycollege.st10204772.opsc7312_poe"
    compileSdk = 34
    buildFeatures.buildConfig = true
    defaultConfig {
        applicationId = "za.co.varsitycollege.st10204772.opsc7312_poe"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        //Spotify Client ID?
       // val spotifyClientID = project.hasProperty("spotify.client_id")
       // buildConfigField ("String", "CLIENT_ID", "\"${spotifyClientID}\"")

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    //CircleImageView Library
    implementation(libs.circleimageview)
    implementation (libs.auth)
    implementation (libs.androidx.browser)
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}