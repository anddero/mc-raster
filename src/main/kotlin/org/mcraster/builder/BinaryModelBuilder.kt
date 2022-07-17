package org.mcraster.builder

class BinaryModelBuilder {

    // TODO 1. Specify one-by-one (builder style) all source files that are included into the process.
    // TODO   1.1. Information needed for reading: file path, file format (Shapefile/XYZ/...)
    // TODO   1.2. Information needed for building: data nature (DEM/Water/Road/...), coordinate system (L-EST/...)
    // TODO 2. Specify build configuration
    // TODO   2.1. World boundaries
    // TODO   2.2. Point of origin (real coordinates that would be considered (0,0))
    // TODO 3. Initialize build process
    // TODO   3.1. Input file list will be sorted (DEM data, then water data, then road data, ...)
    // TODO     3.1.1. Each element could be optional ideally (can have flat worlds, or worlds without water, etc)
    // TODO   3.2. Building will be done gradually, each file contributing one by one
    // TODO     3.2.1. File must be streamed in chunks, not loading the entire file contents to memory
    // TODO     3.2.2. Build output must also be streamed to files (TODO Create FS structure and file formats)
    // TODO     3.2.3. When streaming input, can immediately filter out objects outside of the world boundaries
    // TODO   3.3. Carry out post-build checks on the generated data (world intact without missing blocks, etc)
    // TODO     3.3.1. Can carry out simple corrections by interpolation

}