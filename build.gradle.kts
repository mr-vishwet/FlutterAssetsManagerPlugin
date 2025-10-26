plugins {
    id("org.jetbrains.intellij") version "1.17.0"
    // Only include Kotlin if not bundled by IntelliJ Plugin SDK
    //id("org.jetbrains.kotlin.jvm") version "1.9.20" apply false // Apply false to avoid classpath conflict
}

repositories {
    mavenCentral()
}

intellij {
    version.set("2023.3") // Matches IC-233.11799.241
    type.set("IC") // IntelliJ Community
    //plugins.set(listOf("flutter", "dart")) // Updated versions
}

dependencies {
    implementation("org.yaml:snakeyaml:2.0") // For pubspec.yaml parsing
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.antlr:ST4:4.3.4") // StringTemplate for code generation
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
}
