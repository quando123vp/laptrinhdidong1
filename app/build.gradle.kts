plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")      // ⚙️ Kotlin Android plugin
    id("org.jetbrains.kotlin.kapt")         // 🧩 Annotation Processor (Glide, Room, v.v.)
    id("com.google.gms.google-services")    // 🔥 Firebase
}

android {
    namespace = "com.example.laptrinhdidong1"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.laptrinhdidong1"
        minSdk = 24
        targetSdk = 36
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

    // ⚙️ Đồng bộ Java và Kotlin dùng JVM 11 → Fix lỗi kapt target mismatch
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    // ✅ Dùng Toolchain mới (build ổn định hơn)
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // 🧱 Android cơ bản
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.9.3")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // 🔥 Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")

    // 📊 MPAndroidChart (hiển thị biểu đồ)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.github.amlcurran.showcaseview:library:5.4.3")

    // 🖼️ Glide (load ảnh đại diện, ảnh Firebase Storage)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // 🔍 Hỗ trợ Kotlin toolchain build ổn định hơn
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // 🧪 Kiểm thử
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("com.google.android.gms:play-services-auth:20.5.0")
// hoặc phiên bản mới nhất
    implementation("com.google.firebase:firebase-auth:22.1.1")
// đảm bảo có Firebase Auth
}
