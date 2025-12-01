plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
}

group = "com.abidev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("com.abidev.mdeditor")
    mainClass.set("com.abidev.mdeditor.MDEditorApplication")
}

javafx {
    version = "23"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.web", "javafx.swing")
}

dependencies {
    implementation("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")
    implementation("org.commonmark:commonmark:0.21.0")
    implementation("com.gluonhq.emoji:emoji:1.1.0")
    implementation("com.gluonhq.emoji:offline:1.1.0") // añade sprites offline
    implementation("io.github.mkpaz:atlantafx-base:2.1.0") // AtlantisFX base
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jlink {
    imageZip.set(layout.buildDirectory.file("/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "app"
    }
}
