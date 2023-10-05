import io.jooby.gradle.RunTask
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Logging

plugins {
    val kotlinVersion = "1.9.10"
    val joobyVersion = "3.0.5"

    id("application")
    id("io.jooby.run") version joobyVersion
    id("io.spring.dependency-management") version "1.1.0"
    id("com.google.osdetector") version "1.7.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("co.uzzu.dotenv.gradle") version "2.0.0"
    id("nu.studer.jooq") version "8.2"

    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
}

val mainPackage: String by project
val joobyVersion: String by project
val postgresVersion: String by project
group = "it.rattly"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    kapt("io.jooby:jooby-apt:$joobyVersion") // MVC Annotations
    // Undertow: Server backend, Logback: Logging, Kotlin: kotlin adapter, APT: mvc annotations
    // JTE: Template engine, Whoops + Pebble: Better error page, Hikari: Connection pooling
    listOf("undertow", "logback", "kotlin", "apt", "jte", "whoops", "pebble", "hikari", "metrics").forEach {
        implementation("io.jooby:jooby-$it:$joobyVersion")
    }

    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("io.dropwizard.metrics:metrics-graphite:4.2.20")
    implementation("ch.qos.logback:logback-core:1.4.11") // Logback
    implementation("ch.qos.logback:logback-classic:1.4.11") // Logback
    implementation("gg.jte:jte:3.1.0") // JTE
    implementation("gg.jte:jte-kotlin:3.1.0") // JTE Kotlin extension
    implementation("org.reflections:reflections:0.10.2") // Reflections, useful lib to annotate MVC resources
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1") // Dotenv, for DB stuff
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // Compile time serialization

    implementation("org.jooq:jooq-kotlin")
    implementation("org.jooq:jooq-kotlin-coroutines")
    implementation("org.postgresql:postgresql:$postgresVersion") // Postgres driver
    jooqGenerator("org.postgresql:postgresql:$postgresVersion") // Postgres driver for jooq codegen

    implementation(kotlin("reflect")) // Kotlin reflection library
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
}

tasks.withType<RunTask> {
    mainClass = "$mainPackage.MainKt"
    restartExtensions = listOf("conf", "properties", "class")
    compileExtensions = listOf("java", "kt")
    port = 8080
    waitTimeBeforeRestart = 500
}

jooq {
    configurations {
        create("main") {
            jooqConfiguration.apply {
                logging = Logging.WARN
                jdbc.apply {
                    driver = env.DB_DRIVER.orNull() ?:throw IllegalStateException("Specify DB_DRIVER!")
                    url = env.DB_URL.orNull() ?: throw IllegalStateException("Specify DB_URL!")
                    user = env.DB_USER.orNull() ?: throw IllegalStateException("Specify DB_USER!")
                    password = env.DB_PASSWORD.orNull() ?: throw IllegalStateException("Specify DB_PASSWORD!")
                }

                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        forcedTypes.addAll(listOf(
                            ForcedType().apply {
                                name = "varchar"
                                includeExpression = ".*"
                                includeTypes = "JSONB?"
                            },

                            ForcedType().apply {
                                name = "varchar"
                                includeExpression = ".*"
                                includeTypes = "INET"
                            }
                        ))
                    }

                    generate.apply {
                        isKotlinNotNullInterfaceAttributes = true
                        isKotlinNotNullRecordAttributes = true
                        isPojosAsKotlinDataClasses = true
                    }

                    target.apply {
                        packageName = "$mainPackage.jooq"
                    }

                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}