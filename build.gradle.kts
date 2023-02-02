plugins {
    kotlin("multiplatform") version "1.8.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    linuxX64("native") {
        binaries {
            executable()
        }
    }
    sourceSets {
        val nativeMain by getting
        val nativeTest by getting
    }
}
