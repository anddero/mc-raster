package org.mcraster

import org.mcraster.editor.J2BlocksLevelEditorExample

fun main() {
    // TODO Next step is to create a POC for generating simple stone, dirt, grass, water and other common material
    //  structures, to make sure J2Blocks can be used as a testing utility for the backend world generator logic
    J2BlocksLevelEditorExample.execute() // Tested and working with Minecraft versions: Release 1.10, 1.12, 1.12.2
    // Not working with Minecraft Release 1.13 and later versions
}
