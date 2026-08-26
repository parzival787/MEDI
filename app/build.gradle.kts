plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.medi"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.medi"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.vision.common)
    implementation(libs.play.services.mlkit.text.recognition.common)
    implementation(libs.play.services.mlkit.text.recognition)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))

    // Firebase dependencies
    implementation(libs.google.firebase.auth)
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth")

    // Credential Manager libraries for Google Sign-In
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // jBCrypt for password hashing
    implementation("org.mindrot:jbcrypt:0.4")


    implementation("com.google.ai.client.generativeai:generativeai:0.5.0")
    implementation("com.google.guava:guava:33.0.0-android")

    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.android.volley:volley:1.2.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // Explicit stable versions
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // In your dependencies block in build.gradle (Module: app)
// Or any recent stable version
    // Import the Firebase BoM (manages versions for you)
    implementation("com.google.firebase:firebase-bom:33.1.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

// Add the Firebase products you need
    implementation ("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

// THIS ONE IS ESSENTIAL FOR IMAGE UPLOADS
    implementation("com.google.firebase:firebase-storage")
    // In build.gradle (Module: app)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.10.0")

    // --- FIREBASE FIX ---
    // 1. Add the Firebase Bill of Materials (BoM).
    // This will manage the versions of all other Firebase libraries.
    implementation("com.google.firebase:firebase-bom:32.3.1")

    // 2. Now, declare your Firebase dependencies WITHOUT specifying their versions.
    // The BoM will handle this.
    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-firestore")
    implementation ("com.google.firebase:firebase-storage")
    implementation("com.google.android.material:material:1.10.0")

    implementation("com.razorpay:checkout:1.6.36")
}