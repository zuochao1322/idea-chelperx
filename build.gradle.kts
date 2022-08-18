plugins {
    java
    id("org.jetbrains.intellij") version "1.8.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.13.3")
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("commons-lang:commons-lang:2.6")
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("org.json:json:20220320")
    implementation(fileTree("lib") {listOf("*.jar")})
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

intellij {
    version.set("2022.2.1")
    plugins.set(listOf("java"))
}

tasks{
    buildSearchableOptions {
        enabled = false
    }
}

