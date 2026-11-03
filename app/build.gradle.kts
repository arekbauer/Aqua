plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.aboutLibraries)
    id("jacoco")
}

android {
    namespace = "com.arekb.aqua"
    sourceSets {
        getByName("androidTest").assets.directories.add("$projectDir/schemas")
    }
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.arekb.aqua"
        minSdk = 27
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:2.2.10"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    add("ksp", libs.androidx.room.compiler)
    implementation(libs.hilt.android)
    add("ksp", libs.hilt.compiler)
    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose.m3)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.room.testing)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

jacoco {
    toolVersion = "0.8.13"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val excludes = listOf("**/R.class", "**/R\$*.class", "**/BuildConfig.*", "**/Manifest*.*", "**/*_Factory.*", "**/*_MembersInjector.*", "**/Hilt_*")
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) { exclude(excludes) },
        fileTree(layout.buildDirectory.dir("intermediates/javac/debug")) { exclude(excludes) }
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(fileTree(layout.buildDirectory) { include("jacoco/testDebugUnitTest.exec") })
}




