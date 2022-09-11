package org.mcraster.util

import java.io.File

object FileUtils {

    fun getResource(path: String) = File(
        FileUtils::class.java.getResource(path)?.toURI() ?: throw RuntimeException("Resource file not found: $path")
    )

}
