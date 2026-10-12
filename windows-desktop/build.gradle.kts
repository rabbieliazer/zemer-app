import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.2.10"
    id("org.jetbrains.compose") version "1.11.4"
}

group = "com.jtech.zemer"
version = "0.1.0"

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
}

compose {
    desktop {
        application {
            mainClass = "com.jtech.zemer.windows.MainKt"
            nativeDistributions {
                targetFormats(TargetFormat.Msi, TargetFormat.Exe)
                packageName = "ZemerWindows"
                packageVersion = "0.1.0"
                description = "Zemer Windows Desktop Prototype"
                vendor = "Zemer"
            }
        }
    }
}
