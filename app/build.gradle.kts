plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android") version "1.9.0"
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}


android {
    namespace = "com.example.myapplication"
    compileSdk = 34
    
    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx) // KTX 버전 (코틀린 확장 기능 포함)
    implementation(libs.lifecycle.viewmodel.ktx) // KTX 버전 (코틀린 확장 기능 포함)
    
    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    
    // Material Design
    implementation("com.google.android.material:material:1.11.0")
    
    // Room
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    
    // Java 8+ API desugaring support
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}