package org.mcraster

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.mcraster.reader.ShapefileReader

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "McRaster",
        state = rememberWindowState(width = 300.dp, height = 300.dp)
    ) {
        MaterialTheme {
            Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {
                Button(modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        generateCustomArea1()
                    }) {
                    Text("Generate Custom Area 1")
                }
                Button(modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
//                        printPolygons()
                    }) {
                    Text("Print Polygons")
                }
            }
        }
    }
}

fun generateCustomArea1() {
    LocalTest.generateCustomArea1()
}
