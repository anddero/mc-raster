package org.mcraster

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import org.mcraster.reader.ShapefileReader
import kotlin.math.roundToInt

fun main() = singleWindowApplication {
    MaterialTheme {
        val (view, setView) = remember { mutableStateOf(View.Main) }
        when (view) {
            View.Main -> MainView(setView)
            View.Draw3d -> Draw3dView(setView)
        }
    }
}

private enum class View {
    Main,
    Draw3d
}

@Composable
private fun MainView(setView: (View) -> Unit) {
    Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {
        Button(modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = {
                generateCustomArea1()
            }) {
            Text("Generate Custom Area 1")
        }
        Button(modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = {
                printPolygons()
            }) {
            Text("Print Polygons")
        }
        Button(modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = {
                println("Current Thread: ${Thread.currentThread().name}")
            }) {
            Text("Print Current Thread")
        }
        Button(modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = {
                setView(View.Draw3d)
            }) {
            Text("Draw 3D Object")
        }
    }
}

@Composable
private fun Draw3dView(setView: (View) -> Unit) {
    Column(Modifier.fillMaxSize(), Arrangement.Center) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)) {
            Button(modifier = Modifier.align(Alignment.Top),
                onClick = {
                    setView(View.Main)
                }) {
                Text("Back to Menu")
            }
            Button(modifier = Modifier.align(Alignment.Top),
                onClick = {
                    // TODO Impl reset
                }) {
                Text("Reset")
            }
            Button(modifier = Modifier.align(Alignment.Top),
                onClick = {
                    // TODO Impl save as
                }) {
                Text("Save as...")
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), Arrangement.Center) {
            Canvas3d()
        }
    }
}

private enum class BoxType {
    Empty,
    Full
}

@Composable
private fun Canvas3d() {
//    val bitmap = remember { mutableStateOf(ImageBitmap(100, 100)) }
    val pad = 1
    val padMul = 3
    val sz = 20
    val wh = 20
    val emptyPaint = Paint()
    emptyPaint.color = Color.White
    val fillPaint = Paint()
    fillPaint.color = Color.Red
    val bgPaint = Paint()
    bgPaint.color = Color.Black

    val canvasSzWh = wh * (sz + pad) + pad * (padMul * 2 - 1)
    val boxes = remember { mutableStateOf(List(wh) { List(wh) { BoxType.Empty } } )}
    Canvas(
        modifier = Modifier
            .size(canvasSzWh.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    println("tap $offset")
                    val normx = offset.x.roundToInt() - pad * padMul
                    val normy = offset.y.roundToInt() - pad * padMul
                    val rx = normx % (sz + pad)
                    val ry = normy % (sz + pad)
                    val cx = normx / (sz + pad)
                    val cy = normy / (sz + pad)
                    if (rx < sz && ry < sz) {
                        val copy = boxes.value.map { it.toMutableList() }.toMutableList()
                        when (copy[cy][cx]) {
                            BoxType.Empty -> copy[cy][cx] = BoxType.Full
                            BoxType.Full -> copy[cy][cx] = BoxType.Empty
                        }
                        boxes.value = copy
                    }
                }
            }
    ) {
        drawIntoCanvas { canvas ->
            canvas.withSave {
                println("Drawing")
                canvas.drawRect(0f, 0f, canvasSzWh.toFloat(), canvasSzWh.toFloat(), bgPaint)
                for ((y, row) in boxes.value.withIndex()) {
                    for ((x, box) in row.withIndex()) {
                        val xCoord = x * (sz + pad) + pad * padMul
                        val yCoord = y * (sz + pad) + pad * padMul
                        val xxCoord = xCoord + sz
                        val yyCoord = yCoord + sz
                        canvas.drawRect(
                            xCoord.toFloat(),
                            yCoord.toFloat(),
                            xxCoord.toFloat(),
                            yyCoord.toFloat(),
                            when (box) {
                                BoxType.Empty -> emptyPaint; BoxType.Full -> fillPaint
                            }
                        )
                    }
                }
//                canvas.drawRect(0f, 0f, 10f, 10f, Paint())
//                canvas.drawImage(bitmap.value, Offset.Zero, Paint())
            }
        }
    }
}

fun generateCustomArea1() {
    LocalTest.generateCustomArea1()
}
