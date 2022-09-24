package org.mcraster.util

import java.io.File

object FileUtils {

    fun getResource(path: String) = File(
        FileUtils::class.java.getResource(path)?.toURI() ?: throw RuntimeException("Resource file not found: $path")
    )

    fun File.asLinesDataSource(): DataSource<String> = FileLinesDataSource(this)

    private class FileLinesDataSource(private val file: File) : DataSource<String> {
        override fun <R> use(block: (Sequence<String>) -> R) = file.useLines { lines -> block.invoke(lines) }
    }

}
