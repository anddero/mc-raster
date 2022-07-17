import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application
}

group = "org.mcraster"
version = "1.0-SNAPSHOT"

repositories {
    maven { url = uri("https://repo.osgeo.org/repository/release/") }
    mavenCentral()
}

val geotoolsVersion = "26.1"
dependencies {
    testImplementation(kotlin("test"))
    implementation("org.geotools:gt-shapefile:$geotoolsVersion")
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

application {
    mainClass.set("MainKt")
}
