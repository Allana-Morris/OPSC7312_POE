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

                buildConfigField("String", "CLIENT_SECRET", "44bdee846c714d22ad432b9b7cb1451b")
                buildConfigField("String", "CLIENT_ID", "eb9b8af983d94603adaa1d212cf58980")
                buildConfigField("String", "REDIRECT_URI", "myapp://callback")
                buildConfigField("String", "GOOGLE_ID", "905988466931-h3di4chs18somrfitguu3g95b0bf72sb.apps.googleusercontent.com")

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
    //implementation(files("libs/spotify-auth-release-2.1.0.aar"))
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("com.spotify.android:auth:1.2.5")

    //Glide for image loading
    implementation (libs.glide)
    implementation ("androidx.security:security-crypto:1.1.0-alpha03")
    annotationProcessor (libs.compiler)

    //Google SSO with OAuth2.0
    implementation (libs.androidx.credentials.v150alpha05)
    implementation (libs.androidx.credentials.play.services.auth)
    implementation (libs.googleid)

    // Firebase Authentication
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
    implementation(libs.googleid)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

