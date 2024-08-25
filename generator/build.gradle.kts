import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
    id("org.jetbrains.compose") version "1.6.11"
}

group = "org.mcraster"
version = "1.0-SNAPSHOT"

repositories {
    maven { url = uri("https://repo.osgeo.org/repository/release/") }
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

val geotoolsVersion = "29.2"
val jtsVersion = "1.19.0"
val jacksonVersion = "2.15.2"
dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":common"))
    implementation(compose.desktop.currentOs)
    implementation("org.geotools:gt-shapefile:$geotoolsVersion")
    implementation("org.locationtech.jts:jts-core:$jtsVersion")
    implementation(files("libs/J2Blocks.jar"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}
