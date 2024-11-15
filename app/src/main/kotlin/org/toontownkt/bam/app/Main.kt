@file:OptIn(ExperimentalSerializationApi::class)

package org.toontownkt.bam.app

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.defaultStdout
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.outputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.toontownkt.bam.BamFile
import org.toontownkt.bam.bamSerializersModule
import java.io.File
import java.io.OutputStream

class JsonDump : CliktCommand("bam2json") {
    private val recursive by option("-r", "--recursive").flag().help("Recursively convert Bam files")
    private val pretty by option("-p", "--pretty").flag().help("Pretty print the output JSON")

    private val input by argument().file(mustExist = true, canBeDir = true)
    private val outputFile by option("-o", "--output").outputStream(createIfNotExist = true, truncateExisting = true)
        .defaultStdout()

    private val json by lazy {
        Json {
            serializersModule = bamSerializersModule
            prettyPrint = pretty
        }
    }

    private fun OutputStream.serializeBam(file: File) = json.encodeToStream(BamFile.fromFile(file), this)

    override fun run() {
        if (recursive) {
            assert(input.isDirectory) { "input was not a directory" }

            input.walkTopDown().filter {
                it.isFile && it.extension == "bam"
            }.forEach {
                File(it.path + ".json")
                    .outputStream()
                    .serializeBam(it)
            }
        } else {
            outputFile.serializeBam(input)
        }
    }
}

fun main(args: Array<String>) = JsonDump().main(args)