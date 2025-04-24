plugins {
    kotlin("jvm") version "1.9.22"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.telegram:telegrambots:6.8.0")  // Библиотека для Telegram Bot API
    implementation("org.slf4j:slf4j-simple:2.0.9")  // Для логирования
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")
}

application {
    mainClass.set("org.example.MainKt")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "org.example.MainKt"
    }
}

tasks.shadowJar {
    archiveBaseName.set("HealthyLifeBot")
    archiveClassifier.set("")
    archiveVersion.set("")
}