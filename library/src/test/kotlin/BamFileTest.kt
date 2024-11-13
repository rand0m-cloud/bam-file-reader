import org.toontownkt.bam.BamFile
import org.toontownkt.bam.types.asModelRoot
import java.io.File
import kotlin.test.Test

class BamFileTest {
    @Test
    fun `loads minnie-1200 without an exception`() {
        BamFile.fromFile(File("open-toontown-resources/phase_3/models/char/minnie-1200.bam")).asModelRoot()!!
    }

    @Test
    fun `loads laff_o_meter without an exception`() {
        BamFile.fromFile(File("open-toontown-resources/phase_3/models/gui/laff_o_meter.bam")).asModelRoot()!!
    }

    @Test
    fun `loads minnie-run without an exception`() {
        BamFile.fromFile(File("open-toontown-resources/phase_3/models/char/minnie-run.bam")).asModelRoot()!!
    }
}