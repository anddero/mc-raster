import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application
}

group = "org.mcraster"
version = "1.0-SNAPSHOT"

repositories {
    maven { url = uri("https://repo.osgeo.org/repository/release/") }
//    maven { url = uri("https://repo.mcstats.org/content/repositories/releases/") }
//    maven { url = uri("https://libraries.minecraft.net/") }
    mavenCentral()
}

val geotoolsVersion = "26.1"
dependencies {
    testImplementation(kotlin("test"))
    implementation("org.geotools:gt-shapefile:$geotoolsVersion")
    implementation(files("libs/J2Blocks.jar"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
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

application {
    mainClass.set("MainKt")
}
