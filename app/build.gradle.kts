plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.moorixlabs.bluedrive"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.moorixlabs.bluedrive"
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
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/LICENSE.md",
                "META-INF/LICENSE.markdown",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/NOTICE.md",
                "META-INF/NOTICE.markdown", // 👈 this one fixes your new error
                "META-INF/ASL2.0",
                "META-INF/INDEX.LIST"
            )
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    // These components are used directly by the Java/XML screens.
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.photoview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
