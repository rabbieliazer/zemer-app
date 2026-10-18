plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(21)
}

group = "com.jtech.zemer"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.xerial:sqlite-jdbc:3.45.3.0")
}
