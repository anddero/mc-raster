package org.mcraster.util

class ByteVec3(a: Int, b: Int, c: Int, initialValue: Byte)
    : DimensionalByteArray(a, b, c, initialValue = initialValue) {

    operator fun get(a: Int, b: Int, c: Int) = super.get(a, b, c)

    operator fun set(a: Int, b: Int, c: Int, value: Byte) = super.set(a, b, c, value = value)

}
