/*apply plugin: 'com.android.application'
apply plugin: 'com.google.gms.google-services'
apply plugin: 'com.google.firebase.crashlytics'
apply plugin: 'kotlin-android'
apply plugin: 'com.google.devtools.ksp'*/
import java.util.Properties
plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    compileSdk = 36

    // Version variables
    var versionMajor = 0
    var versionMinor = 0
    var versionBuild = 0

//    applicationVariants.all {
//
//        outputs.configureEach {
//            if (fileName != null && fileName.endsWith(".apk")) {
//                outputFileName = "../../../../../../publish/" +
//                        "${versionMajor}.${versionMinor}." +
//                        String.format("%05d", versionBuild) +
//                        "/$fileName"
//            }
//        }
//    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("${rootDir}/buildsystem/debug.keystore")
            storePassword = "m34gj9b4r3"
            keyAlias = "key0"
            keyPassword = "m34gj9b4r3"
        }

        create("release") {
            storeFile = file("${rootDir}/buildsystem/release.keystore")
            storePassword = "m34gj9b4r3"
            keyAlias = "key0"
            keyPassword = "m34gj9b4r3"
        }
    }

    defaultConfig {
        applicationId = "com.projects.allnotificationblocker.blockthemall"
        minSdk = 23
        targetSdk = 36
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("debug")
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures{
        buildConfig = true
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    flavorDimensions += "version"

    productFlavors {
        create("free") {
            dimension = "version"
            val versionPropsFile = file("version.properties")
            resValue("string", "app_name", "All Notification Blocker")
            buildConfigField("Boolean", "IS_PRO", "false")
            
            val versionProps = Properties()
            if (versionPropsFile.exists()) {
                try {
                    versionPropsFile.inputStream().use { versionProps.load(it) }
                } catch (e: Exception) {
                    // File exists but may be corrupted, use defaults
                }
            }
            
            // Get properties with defaults
            versionMajor = versionProps.getProperty("VERSION_MAJOR")?.toIntOrNull() ?: 1
            versionMinor = versionProps.getProperty("VERSION_MINOR")?.toIntOrNull() ?: 0
            versionBuild = (versionProps.getProperty("VERSION_BUILD")?.toIntOrNull() ?: 0) + 1
            
            // Save updated properties
            versionProps.setProperty("VERSION_MAJOR", versionMajor.toString())
            versionProps.setProperty("VERSION_MINOR", versionMinor.toString())
                versionProps.setProperty("VERSION_BUILD", versionBuild.toString())
                versionPropsFile.writer().use { versionProps.store(it, null) }
            
                versionCode = versionBuild
                versionName = "${versionMajor}.${versionMinor}.${"%05d".format(versionBuild)}"
                applicationIdSuffix = ".free"
                versionNameSuffix = "-free"
                setProperty("archivesBaseName", "AllNotificationBlocker-$versionName")
        }
        create("pro") {
            dimension = "version"
            val versionPropsFile = file("version.properties")

            resValue("string", "app_name", "All Notification Blocker (PRO)")
            buildConfigField("Boolean", "IS_PRO", "true")

            val versionProps = Properties()
            if (versionPropsFile.exists()) {
                try {
                    versionPropsFile.inputStream().use { versionProps.load(it) }
                } catch (e: Exception) {
                    // File exists but may be corrupted, use defaults
                }
            }
            
            // Get properties with defaults
            versionMajor = versionProps.getProperty("VERSION_MAJOR")?.toIntOrNull() ?: 1
            versionMinor = versionProps.getProperty("VERSION_MINOR")?.toIntOrNull() ?: 0
            versionBuild = (versionProps.getProperty("VERSION_BUILD")?.toIntOrNull() ?: 0) + 1
            
            // Save updated properties
            versionProps.setProperty("VERSION_MAJOR", versionMajor.toString())
            versionProps.setProperty("VERSION_MINOR", versionMinor.toString())
                versionProps.setProperty("VERSION_BUILD", versionBuild.toString())
                versionPropsFile.writer().use { versionProps.store(it, null) }
            
                versionCode = versionBuild
                versionName = "${versionMajor}.${versionMinor}.${"%05d".format(versionBuild)}"
                applicationIdSuffix = ".pro"
                versionNameSuffix = "-pro"
                setProperty("archivesBaseName", "AllNotificationBlocker-$versionName")
        }
    }

    namespace = "com.projects.allnotificationblocker.blockthemall"


    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.kotlin.stdlib)

// AndroidX libraries
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout.v221)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity)

// Testing libraries
    testImplementation(libs.junit.v413)
    testImplementation(libs.junit.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.junit)

// Kotlin Coroutines
    implementation (libs.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)

// Permissions and Material Design
    implementation(libs.permissions)
    implementation(libs.material)
    implementation ("com.google.android.material:material:1.12.0")

// Room Database
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)

// Utility libraries
    implementation(libs.circularimageview)
    implementation(libs.timber)
    implementation(libs.library)
    implementation(libs.gson.v2121)
    implementation(libs.contacts)
    implementation(libs.glide)
    kapt (libs.compiler.v480)
    annotationProcessor(libs.compiler)
    implementation(libs.app.update)

// Firebase and Google Play Services
    implementation(libs.firebase.analytics)
    implementation(libs.play.services.ads)
    implementation (libs.billing)
    implementation(libs.google.firebase.crashlytics)
// markdown
    implementation("io.noties.markwon:core:4.6.2")
// Multidex support
    implementation(libs.multidex)

// Additional libraries
    implementation(libs.android.switchdatetimepicker)
    implementation(libs.android.processes)
    implementation(libs.roundedimageview)
    implementation(libs.viewanimator)



    val billing_version = "8.0.0"
    implementation("com.android.billingclient:billing:$billing_version")

}

