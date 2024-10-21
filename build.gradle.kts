plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.2"
}

repositories {
    mavenCentral()
}

configurations.all {
    resolutionStrategy {
        sortArtifacts(ResolutionStrategy.SortOrder.DEPENDENCY_FIRST)
    }
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("org.jetbrains:annotations:24.1.0")
    implementation("org.json:json:20240303")
    implementation(fileTree("lib") { include("*.jar") })
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

intellij {
    version.set("2024.1")
    type.set("IC")
    plugins.set(listOf("java"))
    updateSinceUntilBuild.set(true)
    sameSinceUntilBuild.set(false)
}

val createOpenApiSourceJar = tasks.register<Jar>("createOpenApiSourceJar") {
    from(sourceSets.main.get().java) {
        include("**/com/example/plugin/openapi/**/*.java")
    }
    from(kotlin.sourceSets.main.get().kotlin) {
        include("**/com/example/plugin/openapi/**/*.kt")
    }

    destinationDirectory = layout.buildDirectory.dir("libs").get().asFile
    archiveClassifier = "src"
}

tasks {
    buildSearchableOptions {
        enabled = false
    }

    buildPlugin {
        dependsOn(createOpenApiSourceJar)
        from(createOpenApiSourceJar) {
            into("lib/src")
        }
    }

    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
            languageVersion = "1.9"
        }
    }

    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "17"
            languageVersion = "1.9"
        }
    }

    patchPluginXml {
        sinceBuild.set("241")  // 2024.1
        untilBuild.set("")     // No upper bound
    }

    val copyDependencies by registering(Copy::class) {
        from(configurations.runtimeClasspath)
        into(layout.buildDirectory.dir("dependencies"))
    }

    buildPlugin {
        dependsOn(copyDependencies)
        from(layout.buildDirectory.dir("dependencies")) {
            into("lib")
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }
    }
}