package org.mcraster.reader

import org.mcraster.model.BlockPos
import org.mcraster.util.FileUtils.asLinesDataSource
import org.mcraster.util.OptionalUtils.orThrow
import java.io.File

object Obj3dReader {

    fun readFile(
        file: File
    ) = file.asLinesDataSource()
        .transform { Obj3dFileLinesTransformer(it.iterator()).asSequence() }

    private class Obj3dFileLinesTransformer(private val lineIter: Iterator<String>) : Iterator<BlockPos> {

        private var line = lineIter.next() // ignore first line (Dimensions)
        private var charIter = emptyList<Char>().iterator()
        private var optionalNext: BlockPos? = nextInternal(BlockPos(-1, -1, -1))

        override fun hasNext() = optionalNext != null

        override fun next(): BlockPos {
            val returnVal = optionalNext.orThrow("Called next() without hasNext()?")
            optionalNext = nextInternal(returnVal)
            return returnVal
        }

        private fun nextInternal(prevPos: BlockPos): BlockPos? {
            var y = prevPos.y
            var z = prevPos.z
            var x = prevPos.x

            while(true) {
                val nextX = nextCurrentLine(x)
                if (nextX != null) return BlockPos(x = nextX, y = y, z = z)
                if (!lineIter.hasNext()) return null
                line = lineIter.next()
                if (line.startsWith("Layer ")) {
                    ++y
                    z = -1
                    continue
                }
                if (!line.startsWith(" ".repeat(4))) throw RuntimeException("Unrecognized line: $line")
                ++z
                x = -1
                charIter = line.substring(4).iterator()
            }
        }

        private fun nextCurrentLine(prevIndex: Int): Int? {
            var x = prevIndex
            while (charIter.hasNext()) {
                ++x
                val found = charIter.next() == 'X'
                charIter.next() // ignore the following space
                if (found) return x
            }
            return null
        }

    }

}
