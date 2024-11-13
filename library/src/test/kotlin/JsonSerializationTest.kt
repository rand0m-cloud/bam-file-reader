import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.toontownkt.bam.BamFactory
import org.toontownkt.bam.BamFile
import org.toontownkt.bam.RawBamFile
import org.toontownkt.bam.bamSerializersModule
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonSerializationTest {
    val json =
        Json {
            prettyPrint = true
            serializersModule = bamSerializersModule
        }

    private fun testSerialize(fileName: String): String {
        val bam = BamFile.fromFile(File(fileName))
        val encoded = json.encodeToString(bam)
        val index = encoded.indexOf("toontownkt")

        assertEquals(
            -1, index,
            "type was missing @SerialName, ${if (index != -1) encoded.substring(index, index + 70) else null}"
        )

        return encoded
    }

    @Test
    fun `serializes laff_o_meter without an exception`() {
        testSerialize("open-toontown-resources/phase_3/models/gui/laff_o_meter.bam")
    }

    @Test
    fun `serializes minnie-1200 without an exception`() {
        testSerialize("open-toontown-resources/phase_3/models/char/minnie-1200.bam")
    }

    @Test
    fun `serializes minnie-run without an exception`() {
        testSerialize("open-toontown-resources/phase_3/models/char/minnie-run.bam")
    }

    @Test
    fun `serializes all bam files`() {
        val srcFiles =
            File("open-toontown-resources").walkTopDown()
                .filter { !it.isDirectory && it.extension == "bam" }.toList()
        val unknownClasses =
            srcFiles
                .flatMap { file ->
                    val bam = RawBamFile.fromFile(file)
                    bam.objectMap.keys.map { bam.getType(it).name }
                        .filter { !BamFactory.supportedClasses.contains(it) }
                }
                .toSet()

        assert(unknownClasses.isEmpty()) {
            "Unknown classes: $unknownClasses"
        }

        srcFiles.forEach {
            println(it)
            testSerialize(it.path)
        }
    }
}