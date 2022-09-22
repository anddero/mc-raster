package org.mcraster.model

import org.mcraster.model.BinaryChunk.Companion.BLOCKS_IN_CHUNK
import org.mcraster.model.BinaryChunk.Companion.BLOCK_SIZE_BYTES
import org.mcraster.model.BinaryRegion.Companion.CHUNKS_IN_REGION
import org.mcraster.model.BinaryRegion.Companion.read
import org.mcraster.model.BinaryRegion.Companion.write
import java.io.Closeable
import java.io.File

class BinaryModel(dirName: String): Closeable {

    private val loadedRegionsByRegionXz = mutableMapOf<Pair<Int, Int>, BinaryRegion>()
    private val directory = File(dirName)

    operator fun get(x: Int, y: Int, z: Int): BlockType {
        val xCoord = HorizontalCoordinate(x)
        val zCoord = HorizontalCoordinate(z)
        return getRegion(regionX = xCoord.regionValue, regionZ = zCoord.regionValue).get(x = xCoord, z = zCoord, y = y)
    }
    operator fun set(x: Int, y: Int, z: Int, block: BlockType) {
        val xCoord = HorizontalCoordinate(x)
        val zCoord = HorizontalCoordinate(z)
        return getRegion(regionX = xCoord.regionValue, regionZ = zCoord.regionValue)
            .set(x = xCoord, z = zCoord, y = y, value = block)
    }

    override fun close() {
        loadedRegionsByRegionXz.forEach { xzregion ->
            writeRegionFile(xzregion.key.first, xzregion.key.second, xzregion.value)
        }
    }

    companion object {
        private val REGION_SIZE_MB = CHUNKS_IN_REGION * BLOCKS_IN_CHUNK * BLOCK_SIZE_BYTES / 1024 / 1024
        private val MAX_CACHE_SIZE_MB = 4096
        private val MAX_CACHE_SIZE_REGIONS = MAX_CACHE_SIZE_MB / REGION_SIZE_MB
    }

    private fun getRegion(regionX: Int, regionZ: Int) =
        loadedRegionsByRegionXz[Pair(regionX, regionZ)] ?: loadRegionFromFile(regionX, regionZ)

    private fun loadRegionFromFile(regionX: Int, regionZ: Int): BinaryRegion {
        reduceCacheIfRequired()
        val regionFile = File(directory, getRegionFileName(regionX = regionX, regionZ = regionZ))
        val newBinaryRegion = BinaryRegion()
        if (regionFile.isFile) regionFile.read(newBinaryRegion)
        loadedRegionsByRegionXz[Pair(regionX, regionZ)] = newBinaryRegion
        return newBinaryRegion
    }

    private fun reduceCacheIfRequired() {
        while (loadedRegionsByRegionXz.size >= MAX_CACHE_SIZE_REGIONS) {
            val (xz, regionToRemove) = loadedRegionsByRegionXz.minByOrNull { it.value.lastAccessTime }!!
            loadedRegionsByRegionXz.remove(xz)
            writeRegionFile(xz.first, xz.second, regionToRemove)
        }
    }

    private fun getRegionFileName(regionX: Int, regionZ: Int) = "R_${regionX}_${regionZ}.bin"

    private fun writeRegionFile(regionX: Int, regionZ: Int, region: BinaryRegion) {
        val regionFile = File(directory, getRegionFileName(regionX = regionX, regionZ = regionZ))
        regionFile.write(region)
    }

}
