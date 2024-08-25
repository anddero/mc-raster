package org.mcraster.model

import org.mcraster.model.Block.RegionLocalBlock
import org.mcraster.model.DiskBoundModel.RegionIndex.Companion.regionIndex
import org.mcraster.pos.Limits.DEFAULT_MAX_CACHE_SIZE_MB
import org.mcraster.pos.Limits.DISK_REGION_SIZE_MB_APPROX
import org.mcraster.pos.Limits.isWithinLimits
import org.mcraster.pos.BlockPos
import org.mcraster.util.CachedMap
import org.mcraster.util.StringUtils.intFixedLengthRegex
import org.mcraster.util.StringUtils.toFixedLengthString
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Represents a disk directory containing several files holding portions of model data.
 * Disk-bound stands for the characteristic of the model - since large models would not fit entirely in memory when
 * worked on, this model is continuously interacting with the disk - loading portions of the model on demand and saving
 * portions of the model if low on memory. If this class is created with an existing directory, it represents the model
 * contained in that directory and will interact with its data as expected - so progress can be saved and shared among
 * instances of the program.
 *
 * The model will be bound to the specified directory. If overwrite is set to true, the existing model data
 * in the directory will be removed on initialization. The directory and any unrecognized files (files with names not
 * matching region file name pattern) in the directory will not be removed.
 *
 * The disk is not updated immediately after every change, make sure to call flush() to ensure all changes reach the
 * disk.
 */
class DiskBoundModel(private val directory: File, overwrite: Boolean = false) : Iterable<Block> {

    private val regionsByIndex = object : CachedMap<RegionIndex, Region>(DEFAULT_MAX_CACHE_SIZE_MB) {
        override fun getCacheLineSizeMB() = DISK_REGION_SIZE_MB_APPROX

        override fun Map.Entry<RegionIndex, Region>.isOlderThan(otherCacheLine: Map.Entry<RegionIndex, Region>) =
            this.value.lastAccessTime.isBefore(otherCacheLine.value.lastAccessTime)

        override fun load(key: RegionIndex) = loadRegionFileFromDisk(key)

        override fun onDrop(entry: Map.Entry<RegionIndex, Region>) = ensureRegionFileSavedToDisk(entry.key, entry.value)
    }

    init {
        if (directory.exists()) {
            if (!directory.isDirectory) throw RuntimeException("Not a directory: $directory")
            if (overwrite) {
                println("Overwrite mode is set, removing all existing region files")
                val allModelRegionIndices = getAllModelRegionIndices()
                println("Found ${allModelRegionIndices.size} existing region files")
                allModelRegionIndices.forEach { regionIndex ->
                    val regionFile = getRegionFile(regionIndex)
                    if (regionFile.delete()) {
                        println("Removed existing region file: $regionFile")
                    } else throw RuntimeException("Failed to remove region file: $regionFile")
                }
            }
        } else if (!directory.mkdirs()) throw RuntimeException("Failed to create directory: $directory")
    }

    var maxCacheSizeMB: Int by regionsByIndex::maxCacheSizeMB

    fun getBlock(pos: BlockPos) = regionsByIndex[pos.regionIndex].getBlock(pos = pos)

    /**
     * Return the highest block height at this horizontal coordinate, or null if there are no blocks.
     */
    fun getHighestBlockY(pos: BlockPos.HorBlockPos) = regionsByIndex[pos.regionIndex].getHighestBlockY(pos = pos)

    fun setBlock(pos: BlockPos, block: BlockType) {
        if (!pos.isWithinLimits()) { // TODO Height is currently a hard limit and should be checked at chunk
            System.err.println("CRITICAL: setBlock called with $pos and $block which is out of acceptable limits")
        }
        regionsByIndex[pos.regionIndex].setBlock(pos = pos, value = block)
    }

    /**
     * Save all unsaved changes to disk that have been made so far.
     */
    fun flush() =
        regionsByIndex.forEachCacheLine { ensureRegionFileSavedToDisk(regionIndex = it.key, region = it.value) }

    override fun iterator(): Iterator<Block> = BinaryModelIterator(this)

    private fun getAllModelRegionIndices(): Set<RegionIndex> {
        val regionIndices = regionsByIndex.getCacheLineKeys().toMutableSet()
        val dirFileNames = directory.list() ?: throw RuntimeException("Failed to list region files in model dir")
        dirFileNames.forEach { fileName ->
            val match = regionFileNameRegex.matchEntire(fileName)
            if (match != null) {
                match.groupValues
                    .let { RegionIndex(x = it[1].toInt(), z = it[2].toInt()) }
                    .let { regionIndices.add(it) }
            } else {
                System.err.println("Ignoring unrecognized file in model directory: $fileName")
            }
        }
        return regionIndices.toSet()
    }

    private fun loadRegionFileFromDisk(key: RegionIndex): Region {
        val regionFile = getRegionFile(key)
        val newRegion = Region()
        if (regionFile.exists()) {
            if (regionFile.isFile) {
                FileInputStream(regionFile).use { fileInputStream ->
                    GZIPInputStream(fileInputStream).use { newRegion.read(it) }
                }
                println("Loaded region file: $regionFile")
            } else throw RuntimeException("Cannot read region file: " + regionFile.absolutePath)
        }
        return newRegion
    }

    private fun ensureRegionFileSavedToDisk(regionIndex: RegionIndex, region: Region) {
        if (region.isChangedAfterCreateLoadOrSave) {
            val regionFile = getRegionFile(regionIndex)
            FileOutputStream(regionFile, false).use { fileOutputStream ->
                GZIPOutputStream(fileOutputStream).use { region.write(it) }
            }
            println("Written region file: $regionFile")
        }
    }

    private fun getRegionFile(regionIndex: RegionIndex) = File(directory, getRegionFileName(regionIndex))

    private class BinaryModelIterator(model: DiskBoundModel) : Iterator<Block> {
        private val regionIterator: Iterator<Pair<RegionIndex, Iterator<RegionLocalBlock>>>
        private var regionX = 0
        private var regionZ = 0
        private var blockIterator = emptySequence<RegionLocalBlock>().iterator()

        init {
            regionIterator = model.getAllModelRegionIndices()
                .sortedWith { a, b ->
                    if (a.x < b.x) -1
                    else if (a.x == b.x) {
                        if (a.z < b.z) -1 else if (a.z == b.z) 0 else 1
                    } else 1
                }
                .map { regionIndex -> Pair(regionIndex, model.regionsByIndex[regionIndex].iterator()) }
                .iterator()
        }

        override fun hasNext() = blockIterator.hasNext() || regionIterator.hasNext()

        override fun next(): Block {
            if (!blockIterator.hasNext()) {
                val (regionIndex, iter) = regionIterator.next()
                regionX = regionIndex.x
                regionZ = regionIndex.z
                blockIterator = iter
            }
            val regionLocalBlock = blockIterator.next()
            return regionLocalBlock.toBlock(regionX = regionX, regionZ = regionZ)
        }

    }

    private data class RegionIndex(val x: Int, val z: Int) {
        companion object {
            val BlockPos.regionIndex get() = RegionIndex(x = this.regionX, z = this.regionZ)
            val BlockPos.HorBlockPos.regionIndex get() = RegionIndex(x = this.regionX, z = this.regionZ)
        }
    }

    companion object {
        private fun getRegionFileName(regionIndex: RegionIndex): String {
            val xString = regionIndex.x.toFixedLengthString()
            val zString = regionIndex.z.toFixedLengthString()
            return "R$xString$zString.dat"
        }
        private val regionFileNameRegex = "^R(${intFixedLengthRegex})(${intFixedLengthRegex})\\.dat\$".toRegex()
    }

}
