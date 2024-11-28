@file:OptIn(ExperimentalUnsignedTypes::class, ExperimentalSerializationApi::class)

package org.toontownkt.bam.gltf

import gltf.GlTF
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.internal.readJson
import org.toontownkt.bam.BamFile
import org.toontownkt.bam.RawBamFile
import org.toontownkt.bam.types.*
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun main() {
    val bamFile = File("../../library/open-toontown-resources/phase_3/models/char/mickey-1200.bam")
    val rawBam = RawBamFile.fromFile(bamFile)
    val bam = BamFile.fromRaw(rawBam)


    var objs = mutableListOf<DrawTreeScope.DrawObject>()
    object : DrawTreeScope {
        override val bam: BamFile = bam

        override fun drawObject(obj: DrawTreeScope.DrawObject) {
            objs += obj
        }

    }.drawTree(
        bam.asModelRoot()!!
    )

    println(Json.decodeFromStream<GlTF>(File("/tmp/untitled.gltf").inputStream()))
}

interface DrawTreeScope {
    val bam: BamFile

    data class DrawObject(
        val name: String,
        val texture: String,
        val vertexDatas: List<VertexArrayData>,
        val primitive: GeomPrimitive
    )

    fun drawObject(obj: DrawObject)
}

fun DrawTreeScope.drawTree(node: PandaNode) {
    if (node is GeomNode) {
        node.geoms.forEach {
            drawGeomEntry(node.name, it)
        }
    }

    node.children.forEach { drawTree(bam[it]) }
}

fun DrawTreeScope.drawGeomEntry(name: String, geomEntry: GeomEntry) {

    val geom = bam[geomEntry.geom]

    var textureFileName: String? = null

    val state = bam[geomEntry.renderState]
    val attrib = bam[state.attribs[0].attrib]
    if (attrib is TextureAttrib) {
        val texture = bam[attrib.onStages[0].texture]
        textureFileName = texture.fileName
    }


    val vertexData = bam[geom.vertexData]
    val datas = vertexData.arrays.map {
        val arr = bam[it]
        val format = bam[arr.arrayFormat]
        val columns = format.columns.map {
            ArrayColumn.fromGeomVertexColumn(bam, it)
        }
        VertexArrayData(columns, it)
    }

    assert(geom.primitives.size == 1)

    val indices = geom.primitives.mapNotNull {
        bam[it].indices
    }.map {
        val arrayData = bam[it]
        val columns = bam[arrayData.arrayFormat].columns.map {
            ArrayColumn.fromGeomVertexColumn(bam, it)
        }
        VertexArrayData(columns, it)
    }

    drawObject(DrawTreeScope.DrawObject(name, textureFileName.orEmpty(), datas + indices, bam[geom.primitives[0]]))
}

data class Vertex(val x: Float, val y: Float, val z: Float) {
    override fun toString(): String = "v $x $y $z"
}

data class UV(val u: Float, val v: Float) {
    override fun toString(): String = "vt $u $v"
}

sealed class ArrayColumn(val byteLen: UInt) {
    data object Vertex : ArrayColumn(12U)
    data object UV : ArrayColumn(8U)
    data object TransformBlend : ArrayColumn(2U)
    data object Index : ArrayColumn(2U)

    companion object {
        fun fromGeomVertexColumn(bamFile: BamFile, col: GeomVertexColumn): ArrayColumn =
            when (val name = bamFile[col.name].name) {
                "vertex" -> Vertex
                "texcoord" -> UV
                "transform_blend" -> TransformBlend
                "index" -> Index
                else -> error("don't know column $name")
            }
    }
}

data class VertexArrayData(val columns: List<ArrayColumn>, val data: ObjPointer<GeomVertexArrayData>)
