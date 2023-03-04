import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"
    id("org.jetbrains.compose") version "1.3.0"
}

group = "org.mcraster"
version = "1.0-SNAPSHOT"

repositories {
    maven { url = uri("https://repo.osgeo.org/repository/release/") }
//    maven { url = uri("https://repo.mcstats.org/content/repositories/releases/") }
//    maven { url = uri("https://libraries.minecraft.net/") }
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val geotoolsVersion = "26.1"
val jtsVersion = "1.19.0"
dependencies {
    testImplementation(kotlin("test"))
    implementation(compose.desktop.currentOs)
    implementation("org.geotools:gt-shapefile:$geotoolsVersion")
    implementation("org.locationtech.jts:jts-core:$jtsVersion")
    implementation(files("libs/J2Blocks.jar"))
//    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
//    implementation(files("libs/minecraft-server.jar"))
//    implementation("com.mojang:minecraft-server:1.4.4") // Where can we get this from?
//    implementation("org.geotools:gt-geotiff:$geotoolsVersion")
//    implementation("org.geotools:gt-geopkg:$geotoolsVersion")
//    implementation("org.apache.commons:commons-imaging:1.0-alpha2")
//    implementation("org.gdal:gdal:3.3.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}
