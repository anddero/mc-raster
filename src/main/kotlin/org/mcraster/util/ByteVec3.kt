package org.mcraster.util

import org.mcraster.util.FastByteArray.Companion.read
import org.mcraster.util.FastByteArray.Companion.write
import java.io.InputStream
import java.io.OutputStream

class ByteVec3(a: Int, b: Int, c: Int, initialValue: Byte) : Iterable<Byte> {

    private val array = FastByteArray(a, b, c, initialValue = initialValue)

    operator fun get(a: Int, b: Int, c: Int) = array[a, b, c]

    operator fun set(a: Int, b: Int, c: Int, value: Byte) = array.set(a, b, c, value = value)

    fun size() = array.size()

    override fun iterator() = array.iterator()

    companion object {
        fun OutputStream.write(byteVec3: ByteVec3) = this.write(byteVec3.array)
        fun InputStream.read(byteVec3: ByteVec3) = this.read(byteVec3.array)
    }

}
