package org.mcraster.util

import java.io.InputStream
import java.io.OutputStream

class FastByteArray(
    private vararg val dimensions: Int,
    initialValue: Byte
) {

    private val array = ByteArray(dimensions.reduce { a, b -> a * b })

    init {
        if (dimensions.any { dimension -> dimension <= 0 }) {
            throw RuntimeException("All ${javaClass.simpleName} dimensions must be bigger than 0, given $dimensions")
        }
        for (i in array.indices) array[i] = initialValue
    }

    operator fun get(vararg indices: Int) = array[getRawIndex(indices)]
    operator fun set(vararg indices: Int, value: Byte) {
        array[getRawIndex(indices)] = value
    }
    fun size() = array.size

    private fun getRawIndex(indices: IntArray) = indices.indices.sumOf { dimensions[it] * indices[it] }

    companion object {
        fun OutputStream.write(fastByteArray: FastByteArray) = write(fastByteArray.array)
        fun InputStream.read(fastByteArray: FastByteArray) = read(fastByteArray.array)
    }

}
