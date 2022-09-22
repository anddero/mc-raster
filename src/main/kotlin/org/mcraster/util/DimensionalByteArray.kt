package org.mcraster.util

import java.io.InputStream
import java.io.OutputStream

open class DimensionalByteArray(
    private vararg val dimensions: Int,
    initialValue: Byte
) : Iterable<Byte> {

    private val array = ByteArray(dimensions.reduce { a, b -> a * b })

    init {
        if (dimensions.any { dimension -> dimension <= 0 }) {
            throw RuntimeException("All ${javaClass.simpleName} dimensions must be bigger than 0, given $dimensions")
        }
        for (i in array.indices) array[i] = initialValue
    }

    operator fun get(vararg indices: Int) = array[getRawIndex(indices)]
    operator fun set(vararg indices: Int, value: Byte): Boolean {
        val rawIndex = getRawIndex(indices)
        val current = array[rawIndex]
        if (current == value) return false
        array[rawIndex] = value
        return true
    }
    fun size() = array.size

    override fun iterator() = array.iterator()

    private fun getRawIndex(indices: IntArray): Int {
        var index = 0
        for (i in indices.indices) {
            index *= dimensions[i]
            index += indices[i]
        }
        return index
    }

    companion object {
        fun OutputStream.write(dimensionalByteArray: DimensionalByteArray) = write(dimensionalByteArray.array)
        fun InputStream.read(dimensionalByteArray: DimensionalByteArray) = read(dimensionalByteArray.array)
    }

}
