# mc-raster
Tools for automatically generating Minecraft worlds from geographic data.

# Prerequisites
1. J2Blocks tested and working with Minecraft versions: Release 1.10, 1.12, 1.12.2.
   Not working with Minecraft Release 1.13 and many later versions, not all have been tested though.
   Therefore, if you wish to load and play the generated world, make sure to choose one of the supported Minecraft
   versions when launching the game.

# Input Data Preparation

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
