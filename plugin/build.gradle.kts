import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "org.mcraster"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":common"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    // Security: force safe versions of vulnerable transitive dependencies
    constraints {
        // CVE-2025-67030: Directory traversal vulnerability in extractFile method, fixed in 4.0.3
        implementation("org.codehaus.plexus:plexus-utils:4.0.3") {
            because("CVE-2025-67030: Directory traversal vulnerability in extractFile method, fixed in 4.0.3")
        }
        // CVE-2025-48924: StackOverflowError vulnerability fixed in 3.18.0
        implementation("org.apache.commons:commons-lang3:3.18.0") {
            because("CVE-2025-48924: Uncontrolled recursion in ClassUtils.getClass fixed in 3.18.0")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {

    test {
        useJUnitPlatform()
    }

    withType<KotlinCompile>().configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }

    jar {
        archiveBaseName.set("mc-raster-loader")
        from(sourceSets.main.get().output)
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE // Without it, got error when building
    }

}
