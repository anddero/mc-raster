package org.mcraster.util

class ByteVec3(dim1: Int, dim2: Int, dim3: Int, initialValue: Byte)
    : DimensionalByteArray(dim1, dim2, dim3, initialValue = initialValue) {

    operator fun get(i1: Int, i2: Int, i3: Int) = super.get(i1, i2, i3)

    operator fun set(i1: Int, i2: Int, i3: Int, value: Byte) = super.set(i1, i2, i3, value = value)

}
