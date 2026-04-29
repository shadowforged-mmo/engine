plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    alias(libs.plugins.shadow)
}

group = "com.shadowforgedmmo"
version = System.getenv("VERSION") ?: "dev"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.minestom)
    implementation(libs.guava)
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jython.standalone)
    implementation(libs.creative.api)
    implementation(libs.creative.serializer.minecraft)
    implementation(libs.hephaestus.api)
    implementation(libs.hephaestus.reader.blockbench)
    implementation(libs.hephaestus.runtime.minestom)
    implementation(libs.adventure.text.minimessage)
    implementation(libs.vorbis.java.core.lib)

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
