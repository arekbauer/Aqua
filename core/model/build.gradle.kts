plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.arekb.aqua.core.model"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        // Must stay <= the lowest minSdk of every consumer (:app = 27, :wear = 30).
        minSdk = 27
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Exposed because the wear sync DTOs are @Serializable and consumers encode/decode them.
    api(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
}
