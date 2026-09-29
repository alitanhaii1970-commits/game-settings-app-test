plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.gamesettings.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gamesettings.app.test"
        minSdk = 24
        targetSdk = 34
        // در بیلد لوکال = 1؛ در GitHub Actions به شماره‌ی اجرا ست می‌شه تا نصب آپدیت روی نسخه‌ی قبلی جواب بده
        versionCode = (System.getenv("BUILD_NUMBER")?.toIntOrNull()) ?: 1
        versionName = "1.0.${System.getenv("BUILD_NUMBER") ?: "0"}"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = false
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    // Firebase (فقط Firestore لازمه؛ خواندن لیست بازی‌ها عمومیه و نیاز به لاگین نداره)
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))
    implementation("com.google.firebase:firebase-firestore")

    // بارگذاری عکس از URL
    implementation("io.coil-kt:coil:2.6.0")
}
