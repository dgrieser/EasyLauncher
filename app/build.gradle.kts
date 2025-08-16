import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.github.droidworksstudio.launcher"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.easy.launcher"
        minSdk = 24
        targetSdk = 36
        versionCode = 33
        versionName = "0.3.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["internetPermission"] = "android.permission.INTERNET"
        manifestPlaceholders["fineLocationPermission"] = "android.permission.ACCESS_FINE_LOCATION"
        manifestPlaceholders["coarseLocationPermission"] = "android.permission.ACCESS_COARSE_LOCATION"
    }

    lint {
        disable.add("UseRequiresApi")
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    flavorDimensions.add("internet")
    productFlavors {
        create("withInternet") {
            dimension = "internet"
            manifestPlaceholders["internetPermission"] = "android.permission.INTERNET"
            manifestPlaceholders["fineLocationPermission"] =
                "android.permission.ACCESS_FINE_LOCATION"
            manifestPlaceholders["coarseLocationPermission"] =
                "android.permission.ACCESS_COARSE_LOCATION"
            resValue("string", "app_name", "Easy Launcher")
            val weatherFile = project.rootProject.file("weather.properties")
            if (weatherFile.exists()) {
                val properties = Properties()
                weatherFile.inputStream().use { inputStream ->
                    properties.load(inputStream)
                }
                val apiKey = properties.getProperty("WEATHER_API_KEY") ?: ""
                buildConfigField(
                    type = "String",
                    name = "API_KEY",
                    value = "\"$apiKey\""
                )
            } else {
                buildConfigField(
                    type = "String",
                    name = "API_KEY",
                    value = "\"REMOVE\""
                )
                println("weather.properties file not found.")
            }
        }

        create("withoutInternet") {
            dimension = "internet"
            manifestPlaceholders["internetPermission"] = "REMOVE"
            manifestPlaceholders["fineLocationPermission"] = "REMOVE"
            manifestPlaceholders["coarseLocationPermission"] = "REMOVE"
            resValue("string", "app_name", "Easy Launcher")
            buildConfigField(
                type = "String",
                name = "API_KEY",
                value = "\"REMOVE\""
            )
        }

        create("withInternetNightly") {
            dimension = "internet"
            applicationIdSuffix = ".nightly"
            versionNameSuffix = "-nightly"
            manifestPlaceholders["internetPermission"] = "android.permission.INTERNET"
            resValue("string", "app_name", "Easy Launcher (Nightly)")
            val weatherFile = project.rootProject.file("weather.properties")
            if (weatherFile.exists()) {
                val properties = Properties()
                weatherFile.inputStream().use { inputStream ->
                    properties.load(inputStream)
                }
                val apiKey = properties.getProperty("WEATHER_API_KEY") ?: ""
                buildConfigField(
                    type = "String",
                    name = "API_KEY",
                    value = "\"$apiKey\""
                )
            } else {
                buildConfigField(
                    type = "String",
                    name = "API_KEY",
                    value = "\"REMOVE\""
                )
                println("weather.properties file not found.")
            }
        }

        create("withoutInternetNightly") {
            dimension = "internet"
            applicationIdSuffix = ".nightly"
            versionNameSuffix = "-nightly"
            manifestPlaceholders["internetPermission"] = "REMOVE"
            resValue("string", "app_name", "Easy Launcher (Nightly)")
            buildConfigField(
                type = "String",
                name = "API_KEY",
                value = "\"REMOVE\""
            )
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    applicationVariants.all {
        if (buildType.name == "release") {
            outputs.all {
                val output = this as? com.android.build.gradle.internal.api.BaseVariantOutputImpl
                if (output?.outputFileName?.endsWith(".apk") == true) {
                    output.outputFileName =
                        "${defaultConfig.applicationId}_v${defaultConfig.versionName}-Release.apk"
                }
            }
        }
        if (buildType.name == "debug") {
            outputs.all {
                val output = this as? com.android.build.gradle.internal.api.BaseVariantOutputImpl
                if (output?.outputFileName?.endsWith(".apk") == true) {
                    output.outputFileName =
                        "${defaultConfig.applicationId}_v${defaultConfig.versionName}-Debug.apk"
                }
            }
        }
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.process)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.work.runtime.ktx)
    implementation(libs.recyclerview)
    implementation(libs.preference)
    implementation(libs.biometric.ktx)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.ui.android)

    implementation(libs.acra.core)
    implementation(libs.acra.dialog)
    implementation(libs.acra.mail)
    implementation(libs.androidx.palette.ktx)

    //noinspection KaptUsageInsteadOfKsp
    kapt(libs.room.compiler)
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
    implementation(libs.color.chooser)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
