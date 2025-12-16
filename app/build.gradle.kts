plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    signingConfigs {
        getByName("debug") {
            keyAlias = "fangs123"
            keyPassword = "fangs123"
            storeFile = file("/fangs.jks")
            storePassword = "fangs123"
        }
        create("release") {
            keyAlias = "fangs123"
            keyPassword = "fangs123"
            storeFile = file("/fangs.jks")
            storePassword = "fangs123"
        }
    }

    namespace = "com.yxh.fangs"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.yxh.jks.fangs"
        minSdk = 24
        targetSdk = 36
        versionCode = 10
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isZipAlignEnabled = true
            isShrinkResources = false
            isDebuggable = false
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isMinifyEnabled = false
            isZipAlignEnabled = false
            isShrinkResources = false
            isDebuggable = true   // ← 必须开启调试
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
        viewBinding = true //使用viewbinding
    }

    //在这里添加：
    lintOptions {
        isCheckReleaseBuilds = false
        isAbortOnError = false
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(mapOf("name" to "lib-bmcore-release", "ext" to "aar"))
    implementation(libs.play.services.location)
    debugImplementation(libs.spiderman.debug)
    releaseImplementation(libs.spiderman.release)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    implementation(libs.room.runtime)
    implementation(libs.room.paging)
    implementation(libs.room.rxjava3)
    // Java 用 annotationProcessor
    annotationProcessor(libs.room.compiler)
}