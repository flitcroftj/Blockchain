plugins {
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30"
    id("org.openjfx.javafxplugin") version "0.0.9"
    id("application")
    java
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "org.example"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.4.2")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")

    implementation("net.sourceforge.tess4j", "tess4j", "4.5.2")
}

javafx {
    version = "11.0.2"
    modules = listOf("javafx.base", "javafx.graphics", "javafx.controls", "javafx.swing", "javafx.fxml")
    // configuration = "compileOnly"
}

application {
    mainClass.set("Main")
    mainClassName = "Main"
}

