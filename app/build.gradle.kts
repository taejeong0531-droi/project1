plugins {
    alias(libs.plugins.android.application)
}


android {
    namespace = "com.example.myapplication"
    compileSdk = 34

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
    }
}

dependencies {


    dependencies {
        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation(libs.constraintlayout)
        implementation(libs.lifecycle.livedata.ktx) // KTX 버전 (코틀린 확장 기능 포함)
        implementation(libs.lifecycle.viewmodel.ktx) // KTX 버전 (코틀린 확장 기능 포함)
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)

        // 아래 두 줄은 libs.lifecycle.*.ktx 와 중복되므로 제거합니다.
        // implementation (androidx.lifecycle:lifecycle-viewmodel:2.6.2)
        // implementation (androidx.lifecycle:lifecycle-livedata:2.6.2)
    }
}