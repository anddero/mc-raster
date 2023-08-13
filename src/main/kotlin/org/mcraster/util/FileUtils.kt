package org.mcraster.util

import java.io.File

object FileUtils {

    fun File.asLazyLines(): LazyData<String> = FileLinesLazyData(this)

    private class FileLinesLazyData(private val file: File) : LazyData<String>() {
        override fun <R> use(consume: (Sequence<String>) -> R) = file.useLines { lineSeq -> consume(lineSeq) }
    }

}
