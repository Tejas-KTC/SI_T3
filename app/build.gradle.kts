plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.si_t3"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.si_t3"
        minSdk = 24
        targetSdk = 35
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation ("com.arthenica:ffmpeg-kit-full:6.0-2")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("com.google.android.exoplayer:exoplayer:2.19.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}