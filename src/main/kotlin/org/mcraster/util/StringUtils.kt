package org.mcraster.util

const val INT_WITHOUT_SIGN_MAX_STRING_LENGTH = Int.MAX_VALUE.toString().length

object StringUtils {

    fun Int.toFixedLengthString(): String {
        val withoutSign = if (this >= 0) toString() else toString().substring(1)
        val sign = if (this >= 0) "+" else "-"
        return sign + withoutSign.padStart(INT_WITHOUT_SIGN_MAX_STRING_LENGTH, '0')
    }

}
