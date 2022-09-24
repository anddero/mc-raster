package org.mcraster.model

import org.mcraster.model.Limits.MAX_CACHE_SIZE_REGIONS
import org.mcraster.model.Limits.REGION_LENGTH_BLOCKS
import org.mcraster.util.INT_WITHOUT_SIGN_MAX_STRING_LENGTH
import org.mcraster.util.StringUtils.toFixedLengthString
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Represents a disk directory containing several files holding portions of model data.
 * Disk-bound stands for the characteristic of the model - since large models would not fit entirely in memory when
 * worked on, this model is continuously interacting with the disk - loading portions of the model on demand and saving
 * portions of the model if low on memory. If this class is created with an existing directory, it represents the model
 * contained in that directory and will interact with its data as expected - so progress can be saved and shared among
 * instances of the program.
 *
 * The disk is not updated immediately after every change, make sure to call flush() to ensure all changes reach the
 * disk.
 */
class DiskBoundModel(private val directory: File) : Iterable<Block> {

    private val loadedRegionsByRegionXz = mutableMapOf<Pair<Int, Int>, Region>()
    private var regionFileReadCount = 0
    private var regionFileWriteCount = 0

    operator fun get(pos: BlockPos): BlockType {
        val xCoord = HorizontalCoordinate(pos.x)
        val zCoord = HorizontalCoordinate(pos.z)
        return getRegion(regionX = xCoord.region, regionZ = zCoord.region).get(x = xCoord, z = zCoord, y = pos.y)
    }

    operator fun set(pos: BlockPos, block: BlockType) {
        val xCoord = HorizontalCoordinate(pos.x)
        val zCoord = HorizontalCoordinate(pos.z)
        return getRegion(regionX = xCoord.region, regionZ = zCoord.region)
            .set(x = xCoord, z = zCoord, y = pos.y, value = block)
    }

    /**
     * Save all unsaved changes to disk that have been made so far.
     */
    fun flush() {
        loadedRegionsByRegionXz.forEach { xzregion ->
            writeRegionFileIfUnsaved(xzregion.key.first, xzregion.key.second, xzregion.value)
        }
    }

    override fun iterator(): Iterator<Block> = BinaryModelIterator(this)

    private fun getRegion(regionX: Int, regionZ: Int) =
        loadedRegionsByRegionXz[Pair(regionX, regionZ)] ?: loadRegionFromFile(regionX, regionZ)

    private fun loadRegionFromFile(regionX: Int, regionZ: Int): Region {
        reduceCacheIfRequired()
        val regionFile = File(directory, getRegionFileName(regionX = regionX, regionZ = regionZ))
        val newRegion = Region()
        if (regionFile.exists()) {
            if (regionFile.isFile) {
                FileInputStream(regionFile).use { newRegion.read(it) }
                ++regionFileReadCount
            } else throw RuntimeException("Cannot read region file: " + regionFile.absolutePath)
        }
        loadedRegionsByRegionXz[Pair(regionX, regionZ)] = newRegion
        return newRegion
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

    private fun writeRegionFileIfUnsaved(regionX: Int, regionZ: Int, region: Region) {
        if (region.isChangedAfterCreateLoadOrSave) {
            val regionFile = File(directory, getRegionFileName(regionX = regionX, regionZ = regionZ))
            FileOutputStream(regionFile, false).use { region.write(it) }
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

    private class BinaryModelIterator(model: DiskBoundModel) : Iterator<Block> {
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
            return Block(
                BlockPos(
                    x = regionX * REGION_LENGTH_BLOCKS + regionLocalBlock.pos.x,
                    y = regionLocalBlock.pos.y,
                    z = regionZ * REGION_LENGTH_BLOCKS + regionLocalBlock.pos.z
                ),
                type = regionLocalBlock.type
            )
        }

    }

}
