package org.mcraster.model

import org.mcraster.model.BinaryChunk.Companion.BLOCKS_IN_CHUNK
import org.mcraster.model.BinaryChunk.Companion.BLOCK_SIZE_BYTES
import org.mcraster.model.BinaryRegion.Companion.CHUNKS_IN_REGION
import org.mcraster.model.BinaryRegion.Companion.REGION_LENGTH_BLOCKS
import org.mcraster.model.BinaryRegion.Companion.read
import org.mcraster.model.BinaryRegion.Companion.write
import org.mcraster.util.INT_WITHOUT_SIGN_MAX_STRING_LENGTH
import org.mcraster.util.StringUtils.toFixedLengthString
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class BinaryModel(dirName: String) : Closeable, Iterable<Block> {

    private val loadedRegionsByRegionXz = mutableMapOf<Pair<Int, Int>, BinaryRegion>()
    private val directory = File(dirName)
    private var regionFileReadCount = 0
    private var regionFileWriteCount = 0

    operator fun get(x: Int, y: Int, z: Int): BlockType {
        val xCoord = HorizontalCoordinate(x)
        val zCoord = HorizontalCoordinate(z)
        return getRegion(regionX = xCoord.region, regionZ = zCoord.region).get(x = xCoord, z = zCoord, y = y)
    }

    operator fun set(x: Int, y: Int, z: Int, block: BlockType) {
        val xCoord = HorizontalCoordinate(x)
        val zCoord = HorizontalCoordinate(z)
        return getRegion(regionX = xCoord.region, regionZ = zCoord.region)
            .set(x = xCoord, z = zCoord, y = y, value = block)
    }

    override fun close() {
        loadedRegionsByRegionXz.forEach { xzregion ->
            writeRegionFileIfUnsaved(xzregion.key.first, xzregion.key.second, xzregion.value)
        }
    }

    override fun iterator(): Iterator<Block> = BinaryModelIterator(this)

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
        if (regionFile.exists()) {
            if (regionFile.isFile) {
                FileInputStream(regionFile).use { it.read(newBinaryRegion) }
                ++regionFileReadCount
            } else throw RuntimeException("Cannot read region file: " + regionFile.absolutePath)
        }
        loadedRegionsByRegionXz[Pair(regionX, regionZ)] = newBinaryRegion
        return newBinaryRegion
    }

    private fun reduceCacheIfRequired() {
        while (loadedRegionsByRegionXz.size >= MAX_CACHE_SIZE_REGIONS) {
            val (xz, regionToRemove) = loadedRegionsByRegionXz.minByOrNull { it.value.lastAccessTime }!!
            loadedRegionsByRegionXz.remove(xz)
            writeRegionFileIfUnsaved(xz.first, xz.second, regionToRemove)
        }
    }

    private fun getRegionFileName(regionX: Int, regionZ: Int): String {
        val xString = regionX.toFixedLengthString()
        val zString = regionZ.toFixedLengthString()
        return "R_${xString}_$zString.bin"
    }

    private fun writeRegionFileIfUnsaved(regionX: Int, regionZ: Int, region: BinaryRegion) {
        if (region.isChangedAfterCreateLoadOrSave) {
            val regionFile = File(directory, getRegionFileName(regionX = regionX, regionZ = regionZ))
            FileOutputStream(regionFile, false).use { it.write(region) }
            ++regionFileWriteCount
        }
    }

    private fun getRegionsXz(): Set<Pair<Int, Int>> {
        val regionsXz = loadedRegionsByRegionXz.keys
        val regionFileNames = directory.list() ?: throw RuntimeException("Failed to list region files in model dir")
        regionFileNames.forEach { fileName ->
            val numberRegex = "[-+]\\d{$INT_WITHOUT_SIGN_MAX_STRING_LENGTH}"
            val matcher = "^R($numberRegex)($numberRegex)\\.dat\$".toRegex()
            val xz = matcher.matchEntire(fileName)!!.groupValues
                .let { Pair(it[1].toInt(), it[2].toInt()) }
            regionsXz.add(xz)
        }
        return regionsXz.toSet()
    }

    private class BinaryModelIterator(model: BinaryModel) : Iterator<Block> {
        private val regionXzIterator: Iterator<Triple<Int, Int, Iterator<Block>>>
        private var regionX = 0
        private var regionZ = 0
        private var blockIterator = emptySequence<Block>().iterator()

        init {
            regionXzIterator = model.getRegionsXz()
                .sortedWith { a, b ->
                    if (a.first < b.first) -1
                    else if (a.first == b.first) {
                        if (a.second < b.second) -1 else if (a.second == b.second) 0 else 1
                    } else 1
                }.iterator()
                .asSequence()
                .map { xz -> Triple(xz.first, xz.second, model.getRegion(xz.first, xz.second).iterator()) }
                .iterator()
        }

        override fun hasNext() = blockIterator.hasNext() || regionXzIterator.hasNext()

        override fun next(): Block {
            if (!blockIterator.hasNext()) {
                val regionXz = regionXzIterator.next()
                regionX = regionXz.first
                regionZ = regionXz.second
                blockIterator = regionXz.third
            }
            val regionLocalBlock = blockIterator.next()
            return regionLocalBlock.copy(
                x = regionX * REGION_LENGTH_BLOCKS + regionLocalBlock.x,
                z = regionZ * REGION_LENGTH_BLOCKS + regionLocalBlock.z
            )
        }

    }

}
