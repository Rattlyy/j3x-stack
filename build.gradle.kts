import io.jooby.gradle.RunTask

val joobyVersion = "3.0.5"

plugins {
    val kotlinVersion = "1.9.0"
    val joobyVersion = "3.0.5"

    id("application")
    id("io.jooby.run") version joobyVersion
    id("io.spring.dependency-management") version "1.1.0"
    id("com.google.osdetector") version "1.7.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"

    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
}

group = "it.rattly"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    kapt("io.jooby:jooby-apt:$joobyVersion")
    listOf("undertow", "logback", "kotlin", "apt", "jte", "whoops", "pebble").forEach {
        implementation("io.jooby:jooby-$it:$joobyVersion")
    }

    implementation("ch.qos.logback:logback-core:1.4.11")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("gg.jte:jte:3.1.0")
    implementation("gg.jte:jte-kotlin:3.1.0")
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    implementation(kotlin("reflect"))
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
}

tasks.withType<RunTask> {
    mainClass = "com.rattly.crazy.AppKt"
    restartExtensions = listOf("conf", "properties", "class")
    compileExtensions = listOf("java", "kt")
    port = 8080
    waitTimeBeforeRestart = 500
}

kotlin {
    jvmToolchain(20)
}

application {
    mainClass.set("MainKt")
}