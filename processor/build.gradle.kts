plugins {
    kotlin("jvm") version "2.4.0"
}

group = "com.moshk"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.9")
}

kotlin {
    jvmToolchain(21)
}
