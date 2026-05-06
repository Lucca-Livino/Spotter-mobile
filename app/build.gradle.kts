plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dev.fslab.academia"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.fslab.academia"
        minSdk = 28
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {


    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material:material-icons-extended")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Core Android + Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // UI - Jetpack Compose + Material 3
    implementation(platform(libs.androidx.compose.bom))
//    implementation(libs.androidx.ui)
//    implementation(libs.androidx.ui.graphics)
//    implementation(libs.androidx.ui.tooling.preview)
//    implementation(libs.androidx.material3)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material-icons-extended")

    // DataStore Preferences - persistência de tema e preferências
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // HTTP - Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // OkHttp core (necessário para CookieJar e cookieJar builder)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // PersistentCookieJar — persiste cookies entre sessões via SharedPreferences
    implementation("com.github.franmontiel:PersistentCookieJar:v1.0.1")

    // Jetpack Security — armazenamento criptografado dos cookies (AES-256)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Coroutines para operações assíncronas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // WebSocket - Socket.IO (notificações em tempo real)
    implementation("io.socket:socket.io-client:2.1.1") {
        exclude(group = "org.json", module = "json")
    }

    // CameraX - acesso completo à câmera
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // ML Kit Barcode Scanning - leitura de QR Codes
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // Coil - carregamento assíncrono de imagens (avatares, etc.)
    implementation("io.coil-kt:coil-compose:2.5.0")
    // Coil GIF - decoders animados (GIF, WebP animado, HEIF animado)
    implementation("io.coil-kt:coil-gif:2.5.0")

    // ExoPlayer (media3) - reprodução de vídeo VP9/WebM, mais robusto que MediaPlayer
    implementation("androidx.media3:media3-exoplayer:1.3.1")

    // Armazenamento seguro de credenciais (AES-256 + Android Keystore)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Autenticação biométrica (impressão digital / reconhecimento facial)
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // Firebase Cloud Messaging (notificações push)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
}