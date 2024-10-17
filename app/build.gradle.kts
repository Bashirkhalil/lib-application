plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
  //  alias(libs.plugins.okhttp)

}

android {
    namespace = "com.example.myapplicationtest"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplicationtest"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
           // abiFilters.add("armeabi")
            abiFilters.add("arm64-v8a")
            abiFilters.add("armeabi-v7a")
            //abiFilters.add("x86")
            abiFilters.add("x86_64")
        }
        packagingOptions {

            excludes += setOf(
                "lib/armeabi/**",
                "lib/armeabi-v7a/libtest.so",
                "lib/x86/libtest.so",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/rxjava.properties",
                "isoparser-default.properties",
                "META-INF/*.kotlin_module",
                "META-INF/**"
            )

            jniLibs {
                useLegacyPackaging = true
            }
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

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/libs")
        }
    }


//    externalNativeBuild {
//        cmake {
//            path = file("src/main/cpp/CMakeLists.txt")
//            version = "3.22.1"
//        }
//    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

//    implementation(libs.retrofit)
//    implementation(libs.retrofit_gson_converter)
    implementation(libs.okhttp)
    implementation(libs.retrofit.gson.converter)
    implementation(libs.retrofit)
    //implementation(libs.updateapputilszz)


}