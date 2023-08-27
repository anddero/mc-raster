package org.mcraster

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import org.mcraster.BoxType.EmptyPrevEmpty
import org.mcraster.BoxType.EmptyPrevFull
import org.mcraster.BoxType.FullPrevEmpty
import org.mcraster.BoxType.FullPrevFull
import org.mcraster.Obj3dDisplayProps.BG_PAINT
import org.mcraster.Obj3dDisplayProps.BOX_SIZE
import org.mcraster.Obj3dDisplayProps.CANVAS_DIMS
import org.mcraster.Obj3dDisplayProps.PADDING
import org.mcraster.Obj3dDisplayProps.PADDING_BORDER
import org.mcraster.Object3dState.Companion.OBJECT_3D_DIMS
import org.mcraster.builder.WorldBuilder
import org.mcraster.reader.ConfigReader
import org.mcraster.reader.ShapefileReader
import java.io.File
import java.time.LocalDateTime
import kotlin.math.roundToInt

fun printPolygons() {
    ShapefileReader.lazyReadShpPolygonsLestYx(File("input-resources/customArea1/waterbody.shp"))
        .use { multiPolygonSeq ->
            val multiPolygons = multiPolygonSeq.take(10).toList()
            println("N=${multiPolygons.size}" +
                    ", minParts=${multiPolygons.minBy { it.polygons.size }.polygons.size}" +
                    ", maxParts=${multiPolygons.maxBy { it.polygons.size }.polygons.size}" +
                    ", avgParts=${multiPolygons.map { it.polygons.size }.sum() / multiPolygons.size.toFloat()}}"
            )
        }
}

fun generateCustomArea(configFileName: String) {
    println("Start generating $configFileName at ${LocalDateTime.now()}")
    runCatching {
        WorldBuilder.buildWithJ2Blocks(
            buildConfig = ConfigReader.readConfig(configDir = "input-resources", configFile = configFileName)
        )
    }.exceptionOrNull()
        ?.let { throwable ->
            System.err.println("--- Throwable caught in generateCustomArea() at ${LocalDateTime.now()} ---")
            throwable.printStackTrace()
            System.err.println("--- Throwable End ---")
        }
}

// TODO Update README with a working terrain example and instructions on how to set up
fun main() = singleWindowApplication {
    MaterialTheme {
        val (view, setView) = remember { mutableStateOf(View.Main) }
        when (view) {
            View.Main -> viewMain(setView)
            View.Draw3d -> viewDraw3dObj(setView)
        }
    }
}

private enum class View {
    Main,
    Draw3d
}

@Composable
private fun viewMain(setView: (View) -> Unit) {
    Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {
        buttonCenter("Generate Custom Area 1") { generateCustomArea("customArea1.yml") }
        buttonCenter("Generate Custom Area 2") { generateCustomArea("customArea2.yml") }
        buttonCenter("Print Polygons") { printPolygons() }
        buttonCenter("Print Current Thread") { println("Current Thread: ${Thread.currentThread().name}") }
        buttonCenter("Draw 3D Object") { setView(View.Draw3d) }
    }
}

@Composable
private fun ColumnScope.buttonCenter(text: String, onClick: () -> Unit) =
    Button(modifier = Modifier.align(Alignment.CenterHorizontally), onClick = onClick) { Text(text) }

@Composable
private fun RowScope.buttonTop(text: String, onClick: () -> Unit) =
    Button(modifier = Modifier.align(Alignment.Top), onClick = onClick) { Text(text) }

@Composable
private fun viewDraw3dObj(setView: (View) -> Unit) {
    val state = remember { mutableStateOf(Object3dState()) }
    Column(Modifier.fillMaxSize(), Arrangement.Center) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)) {
            buttonTop("Back to Menu") { setView(View.Main) }
            buttonTop("Reset") { state.value = Object3dState() }
            buttonTop("Save as 'obj3d.dump'") { state.value.dump("obj3d.dump") }
            buttonTop("<--") { state.value = state.value.navLeft() }
            buttonTop("-->") { state.value = state.value.navRight() }
        }
        Row(Modifier.fillMaxWidth(), Arrangement.Center) {
            viewDraw3dObjCanvas(state)
        }
    }
}

private fun Color.toPaint(): Paint {
    val paint = Paint()
    paint.color = this
    return paint
}

private enum class BoxType(val paint: Paint) {
    EmptyPrevEmpty(Color.White.toPaint()),
    FullPrevEmpty(Color.Red.copy(green = 0.2f, blue = 0.2f).toPaint()),
    EmptyPrevFull(Color.Gray.toPaint()),
    FullPrevFull(Color.Red.toPaint())
}

private typealias Obj3dRow = List<BoxType>
private typealias Obj3dLayer = List<Obj3dRow>

private class Object3dState(
    val index: Int = 0,
    val layers: List<Obj3dLayer> = listOf(List(OBJECT_3D_DIMS) { List(OBJECT_3D_DIMS) { EmptyPrevEmpty } })
) {

    fun navLeft() = if (index == 0) this else Object3dState(index - 1, updateLayers())

    fun navRight() = Object3dState(index + 1, updateLayers())

    fun flip(x: Int, y: Int): Object3dState {
        if (x < 0 || x >= OBJECT_3D_DIMS || y < 0 || y >= OBJECT_3D_DIMS) return this
        val copy = layers[index].map { it.toMutableList() }
        when (copy[y][x]) {
            EmptyPrevEmpty -> copy[y][x] = FullPrevEmpty
            FullPrevEmpty -> copy[y][x] = EmptyPrevEmpty
            EmptyPrevFull -> copy[y][x] = FullPrevFull
            FullPrevFull -> copy[y][x] = EmptyPrevFull
        }
        val newLayers = layers.subList(0, index) + listOf(copy) + layers.subList(index + 1, layers.size)
        return Object3dState(index, newLayers)
    }

    fun dump(fileName: String) {
        File(fileName).outputStream().bufferedWriter().use { writer ->
            writer.write("Dimensions: Width $OBJECT_3D_DIMS, Height $OBJECT_3D_DIMS")
            writer.newLine()
            for ((i, layer) in layers.withIndex()) {
                writer.write("Layer $i")
                writer.newLine()
                for (row in layer) {
                    writer.write("    ")
                    for (box in row) {
                        when (box) {
                            EmptyPrevEmpty, EmptyPrevFull -> writer.write("-")
                            FullPrevEmpty, FullPrevFull -> writer.write("X")
                        }
                        writer.write(" ")
                    }
                    writer.newLine()
                }
            }
        }
    }

    private fun updateLayers() = layers.subList(0, index + 1) +
            (index + 1 until layers.size).map { layers[it].updateLayer(layers[it - 1]) } +
            if (index + 1 == layers.size) listOf(layers.last().getNextEmptyLayer()) else emptyList()

    companion object {

        const val OBJECT_3D_DIMS = 40

        private fun Obj3dLayer.getNextEmptyLayer() = map { prevRow -> prevRow.map { it.getNextEmptyBox() } }

        private fun BoxType.getNextEmptyBox() = when (this) {
            EmptyPrevEmpty, EmptyPrevFull -> EmptyPrevEmpty
            FullPrevEmpty, FullPrevFull -> EmptyPrevFull
        }

        private fun Obj3dLayer.updateLayer(prev: Obj3dLayer) =
            prev.zip(this).map { (prevRow, row) -> row.updateRow(prevRow) }

        private fun Obj3dRow.updateRow(prev: Obj3dRow) =
            prev.zip(this).map { (prevBox, box) -> box.update(prevBox) }

        private fun BoxType.update(prev: BoxType) = when (prev) {
            EmptyPrevEmpty, EmptyPrevFull -> {
                when (this) {
                    EmptyPrevEmpty, EmptyPrevFull -> EmptyPrevEmpty
                    FullPrevEmpty, FullPrevFull -> FullPrevEmpty
                }
            }
            FullPrevEmpty, FullPrevFull -> {
                when (this) {
                    EmptyPrevEmpty, EmptyPrevFull -> EmptyPrevFull
                    FullPrevEmpty, FullPrevFull -> FullPrevFull
                }
            }
        }

    }

}

private object Obj3dDisplayProps {

    const val PADDING = 1
    const val PADDING_BORDER = 3
    const val BOX_SIZE = 20
    val BG_PAINT = Color.Black.toPaint()

    const val CANVAS_DIMS = OBJECT_3D_DIMS * (BOX_SIZE + PADDING) + PADDING_BORDER * 2 - 1

}

@Composable
private fun viewDraw3dObjCanvas(state: MutableState<Object3dState>) {
    Canvas(
        modifier = Modifier
            .size(CANVAS_DIMS.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    println("tap $offset")
                    state.value = state.value.flip(x = offset.x.toBoxIndex(), y = offset.y.toBoxIndex())
                }
            }
    ) {
        drawIntoCanvas { canvas ->
            canvas.withSave {
                println("Drawing")
                canvas.drawRect(0f, 0f, CANVAS_DIMS.toFloat(), CANVAS_DIMS.toFloat(), BG_PAINT)
                for ((y, row) in state.value.layers[state.value.index].withIndex()) {
                    for ((x, box) in row.withIndex()) {
                        val left = x * (BOX_SIZE + PADDING) + PADDING_BORDER
                        val top = y * (BOX_SIZE + PADDING) + PADDING_BORDER
                        canvas.drawRect(
                            left = left.toFloat(),
                            top = top.toFloat(),
                            right = (left + BOX_SIZE).toFloat(),
                            bottom = (top + BOX_SIZE).toFloat(),
                            paint = box.paint
                        )
                    }
                }
            }
        }
    }
}

private fun Float.toBoxIndex(): Int {
    val mouseCoordNorm = roundToInt() - PADDING_BORDER
    return if (mouseCoordNorm % (BOX_SIZE + PADDING) < BOX_SIZE) mouseCoordNorm / (BOX_SIZE + PADDING) else -1
}
