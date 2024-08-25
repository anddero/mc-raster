package org.mcraster.util

object OptionalUtils {

    fun <T> T?.orThrow(msg: String = "Value is not present"): T =
        this ?: throw RuntimeException(msg)

    fun <T> T?.orElse(action: () -> Unit): T? {
        action()
        return this
    }

}
