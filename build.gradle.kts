plugins {
    kotlin("jvm") version "1.9.22"
    application
    id("com.gradleup.shadow") version "9.2.0"
}

group = "com.shadowforgedmmo"
version = System.getenv("VERSION") ?: "dev"

repositories {
    mavenCentral()

    maven("https://jitpack.io")

    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("net.minestom:minestom-snapshots:1_21_5-468b85eb42")

    implementation("com.google.guava:guava:33.0.0-jre")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.fasterxml.jackson.core:jackson-core:2.13.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")

    implementation("org.python:jython-standalone:2.7.4b2")

    implementation("team.unnamed:creative-api:1.8.2-SNAPSHOT")
    implementation("team.unnamed:creative-serializer-minecraft:1.8.2-SNAPSHOT")

    implementation("team.unnamed:mocha:3.0.0")

    implementation("com.github.shadowforged-mmo:hephaestus-engine:81f9474b17")

    implementation("net.kyori:adventure-text-minimessage:4.16.0")

    implementation("org.gagravarr:vorbis-java-core:0.8")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.shadowforgedmmo.engine.MainKt")
}
