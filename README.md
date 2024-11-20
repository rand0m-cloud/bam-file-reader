# bam-file-reader
![Maven Central Version](https://img.shields.io/maven-central/v/io.github.rand0m-cloud.bam-file-reader/core)

`bam-file-reader` is a Kotlin library and command-line application for converting `.bam` assets (used by the Panda3D game engine) into JSON. The library simplifies working with binary data from `.bam` files by parsing and converting them into a readable JSON structure.

## Features

- **Kotlin Library**: Available on Maven Central, `bam-file-reader` allows you to load `.bam` files into memory and access their contents in a structured way.
- **CLI Application**: The `bam2json` application is provided as a standalone executable (`bam2json.jar`) for quickly converting `.bam` files to JSON.
- **File Parsing**: Currently, the library parses assets found in [`open-toontown/resources`](https://github.com/open-toontown/resources), with plans to increase file version coverage in the future.

## Installation

To use `bam-file-reader` in your Kotlin project, add the following dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.rand0m-cloud.bam-file-reader:core:{version}")
}
```

You can also download the standalone `bam2json` application from the [GitHub Releases](https://github.com/rand0m-cloud/bam-file-reader/releases) page and run `bam2json.jar`.

## Command-Line Example

```bash
$ java -jar bam2json.jar --pretty resources/phase_5/models/props/bird.bam

{
    "majorVersion": 6,
    "minorVersion": 24,
    "littleEndian": true,
    "objects": {
        "1": {
            "type": "ModelRoot",
            "modelNode": {
                "type": "ModelNode",
                "pandaNode": {
                    "type": "PandaNode",
                    "name": "bird.egg",
                    "state": 2,
                    "transform": 3,
                    "effects": 4,
                    "drawControlMask": 0,
                    "drawShowMask": 4294967295,
                    "intoCollideMask": 0,
                    "boundsType": 0,
                    "keys": {},
                    "children": [
                        5
                    ]
                },
                "preserveTransform": 0,
                "preserveAttributes": 0
            }
        },
        ...
    }
}

```
To convert all .bam files in a directory (and its subdirectories) to JSON, use the `--recursive` (or `-r`) flag. This will generate a .bam.json file for every .bam file found.
## Reporting Bugs

If you encounter any bugs or issues with `bam-file-reader`, please open an issue on the [GitHub Issues page](https://github.com/rand0m-cloud/bam-file-reader/issues). This library is not complete, and more file format versions are yet to be implemented. We appreciate feedback and bug reports.

## License

This project is licensed under the MIT and Apache License.
