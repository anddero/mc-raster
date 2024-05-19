# mc-raster
Tools for automatically generating Minecraft worlds from geographic data.

# Input Data Preparation

## Estonia

### Elevation Data

To fetch elevation data as chunks of DEM with 1m precision, follow these steps:

1. Open up Maa-amet's interactive XGis map, with area grid enabled: https://xgis.maaamet.ee/xgis2/page/link/bO0y2dFb
2. The grid size can be adjusted from the menu to contain smaller or bigger chunks.
3. Open Maa-amet's height data download page: https://geoportaal.maaamet.ee/est/Ruumiandmed/Korgusandmed/Laadi-korgusandmed-alla-p614.html
4. Choose "Maapinna kõrgusmudelid", "Eraldusvõimega 1m XYZ" and enter the chunk code as seen on the interactive map.
