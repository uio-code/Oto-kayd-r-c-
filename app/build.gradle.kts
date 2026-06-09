// Uygulamanın bağımlılıklarını, versiyonunu ve SDK hedeflerini içerir.
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.opensource.autoswiper"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.opensource.autoswiper"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Zero-cost, tamamen ücretsiz Google ve JetBrains açık kaynak kütüphaneleri
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    // Çoklu hız ve asenkron işlemler için Coroutine altyapısı
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
