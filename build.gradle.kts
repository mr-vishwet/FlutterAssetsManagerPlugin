plugins {
    id("org.jetbrains.intellij") version "1.17.0"
    kotlin("jvm") version "1.9.20" // Ensure Kotlin JVM plugin is applied
}

repositories {
    mavenCentral()
    maven { url = uri("https://plugins.jetbrains.com/maven") } // Add IntelliJ plugin repository
}

intellij {
    version.set("2023.3") // Matches IC-233.11799.241
    type.set("IC") // IntelliJ Community
    //plugins.set(listOf("io.flutter:73.0", "com.jetbrains.dart:233.14888")) // Compatible plugin versions
}

dependencies {
    implementation("org.yaml:snakeyaml:2.0") // For pubspec.yaml parsing
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.antlr:ST4:4.3.4") // StringTemplate for code generation
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17" // Match JDK 17
    }
    patchPluginXml {
        version.set("1.0.0")
    }

    // Ensure test tasks use JUnit
    test {
        useJUnitPlatform()
    }

    instrumentCode {
        enabled = false // Disable instrumentation
    }
}