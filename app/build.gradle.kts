import kotlin.math.log

plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.player.vlcplayerplugin"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 28

        // 添加NDK配置
        ndk {
            // 明确指定ABI
            abiFilters.add("arm64-v8a")
        }
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
        buildConfig = true

    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // 修改productFlavors配置
    flavorDimensions += "abi"
    productFlavors {
        create("arm64") {
            dimension = "abi"
            ndk {
                // 明确指定ABI
                abiFilters.clear()
                abiFilters.add("arm64-v8a")
            }
        }
    }

    // 添加sourceSets配置确保JNI库路径正确
    sourceSets {
        getByName("arm64") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }
    android.libraryVariants.all { variant ->
        variant.outputs.configureEach {
            if (outputFile.name.endsWith(".aar")) {
               logger.log(  LogLevel.DEBUG,"输出文件名: ${outputFile.name}")
            }

        }
        true

    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
//    implementation(libs.vlc)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(project(":pluginlibrary"))
    implementation(libs.androidx.lifecycle.livedata.core)

    // 如果使用本地VLC库，添加以下配置
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
}