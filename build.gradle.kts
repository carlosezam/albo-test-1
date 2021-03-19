import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30"
    application
}

group = "me.mobile"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit"))

    implementation("io.ktor:ktor-client-core:1.5.2")
    implementation("io.ktor:ktor-client-json:1.5.2")
    implementation("io.ktor:ktor-client-gson:1.5.2")

    implementation("io.ktor:ktor-client-jackson:1.5.2")


    implementation("io.ktor:ktor-client-cio:1.5.2")
    implementation("io.ktor:ktor-client-serialization-jvm:1.5.2")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "MainKt"
}