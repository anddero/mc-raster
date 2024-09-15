# mc-raster

**Tools for automatically generating Minecraft worlds from geographic data.**

# Project Overview

The Kotlin project is divided into three modules:

1. **common** - contains functionality which is used in several other modules
2. **generator** - UI app for generating a model and a Minecraft world
   1. input different types of geographic data such as elevation, water bodies, roads, etc
   2. output a model of cubes (voxels) in a custom binary format representing the given geographic data
   3. also output a Minecraft world with 1:1 correspondence to the custom voxel model
3. **plugin** - a Paper plugin used to load the custom voxel model into an existing Minecraft world

The recommended workflow is to use **generator** app for generating the custom binary model,
and then loading the custom binary model with **plugin**.

The other option is to use **generator** app to generate both the model and the Minecraft world,
but there are several issues with this approach:
1. Only very small worlds can be generated due to memory limitations of J2Blocks.
2. The world format is old and probably only works properly if loaded with Minecraft version 1.12.2 or earlier.

# Prerequisites

This project uses Gradle as the build system and is written in the Kotlin programming language.

A Gradle wrapper is included with the sources, so Gradle does not need to be manually installed.

Ensure that you have a compatible version of the JDK installed. This project requires JDK 21.

## Generator prerequisites

1. J2Blocks library (included under libs)
    * Necessary for generating the Minecraft world.
    * Tested and working with Minecraft versions: Release 1.10, 1.12, 1.12.2.
      Not working with Minecraft Release 1.13 and many later versions, not all have been tested though.
      Therefore, if you wish to load and play the generated world, make sure to choose one of the supported Minecraft
      versions when launching the game.
    * Needs to fit the entire generated world to memory at once during generation, therefore can only handle small worlds.

## Plugin prerequisites

Nothing extra is needed to *build* the plugin.

To *use* the plugin, you need to install:

1. Paper Minecraft server: https://papermc.io/

# Build Instructions

## Option 1: IDE

Open the project with an IDE such as IntelliJ and conveniently build and run via IDE.

## Option 2: Command-Line

Use the gradle wrapper from the command-line to build and run.

* Use `./gradlew build` (Mac/Linux) or `./gradlew.bat build` (Windows) to build the entire project.
* Use `./gradlew run` (Mac/Linux) or `./gradlew.bat run` to start the Generator UI.

# Plugin Usage Instructions

1. Download and install the Paper server from https://papermc.io/downloads/paper,
following their official instructions if necessary.

2. Install the mc-raster loader plugin by copying the JAR file from `plugin/build/libs` to `<your_server_dir>/plugins`.

3. Run the Paper server.
   1. Simple example how to run the server from command-line: `java -Xms2G -Xmx2G -jar paper-1.21.1-41.jar --nogui`
   2. Refer to official instructions for more information.

4. Use a Minecraft client to join the server.

5. Optional: If you want to observe generation in real-time, make sure to teleport to the area which will be loaded.

6. Type `/help` in the game for most up-to-date instructions on how to use the plugin to load a model into the Minecraft world.
   1. Probably `/help mc-raster-loader` for instructions, unless the plugin command has changed.
   2. Probably `/mc-raster-loader <path_to_model>` to start loading, unless the plugin command has changed.

# Input Data Preparation

This section covers some of the different methods I have used to fetch geographic data for using as input to the model generator.

## Estonia

### Main Geographical Data

To fetch main geographical data like rivers, lakes, roads, houses, etc as shapefiles, follow these steps:

1. Go to Maa-amet's official web page.
2. Navigate to: "Ruumiandmed" -> "Topokaardid ja aluskaardid" -> "Eesti põhikaart 1:10 000" -> "Laadi põhikaart alla"
   or simply follow the URL: https://geoportaal.maaamet.ee/est/Ruumiandmed/Topokaardid-ja-aluskaardid/Eesti-pohikaart-1-10000/Laadi-pohikaart-alla-p612.html
3. Download the SHP archive under section "Kogu Eesti andmestik korraga vektorkaardina"
   e.g. ETAK_Eesti_pohikaart_2024_SHP.zip

### Detailed Elevation Data

To fetch elevation data as chunks of DEM with 1m precision, follow these steps:

1. Open up Maa-amet's interactive XGis map, with area grid enabled: https://xgis.maaamet.ee/xgis2/page/link/bO0y2dFb
2. The grid size can be adjusted from the menu to contain smaller or bigger chunks, but might be required to note
   down the chunk codes of interest from resolution 1:10000 in order to download the elevation data later.
3. Open Maa-amet's height data download page: https://geoportaal.maaamet.ee/est/Ruumiandmed/Korgusandmed/Laadi-korgusandmed-alla-p614.html
4. Choose "Maapinna kõrgusmudelid", "Eraldusvõimega 1m XYZ" and enter the chunk code as seen on the interactive map.
   Note that it might be required to enter a chunk code of resolution 1:10000.
