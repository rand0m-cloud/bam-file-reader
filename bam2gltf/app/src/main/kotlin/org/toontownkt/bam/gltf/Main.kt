@file:OptIn(ExperimentalUnsignedTypes::class, ExperimentalSerializationApi::class, ExperimentalEncodingApi::class)

package org.toontownkt.bam.gltf

import gltf.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlinx.serialization.json.internal.readJson
import org.toontownkt.bam.BamFile
import org.toontownkt.bam.RawBamFile
import org.toontownkt.bam.types.*
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private val json = Json { prettyPrint = true }

fun main() {
    val bamFile = File("../../library/open-toontown-resources/phase_3/models/char/mickey-1200.bam")
    val rawBam = RawBamFile.fromFile(bamFile)
    val bam = BamFile.fromRaw(rawBam)


    println(bam)
    var objs = mutableListOf<DrawTreeScope.DrawObject>()
    object : DrawTreeScope {
        override val bam: BamFile = bam

        override fun drawObject(obj: DrawTreeScope.DrawObject) {
            objs += obj
        }

    }.drawTree(
        bam.asModelRoot()!!
    )

    objs.forEachIndexed { index, it -> println("$index - ${it.texture}") }

    val objIndex = 0
    val obj = objs[objIndex]

    println(obj)
    val buffers = obj.vertexDatas.map {
        val data = bam[it.data].data.toByteArray()
        Buffer(byteLength = data.size.toLong(), uri = data.createDataUri())
    }
    val bufferViews = obj.vertexDatas.mapIndexed { index, it ->
        val data = bam[it.data]
        val format = bam[data.arrayFormat]
        BufferView(
            GlTFId(index.toLong()),
            data.data.size.toLong(),
            byteOffset = 0,
            byteStride = format.stride.toLong(),
            target = if (it.columns.contains(ArrayColumn.Index)) BufferViewTarget.ELEMENT_ARRAY_BUFFER else BufferViewTarget.ARRAY_BUFFER
        )

    }
    val accessors = obj.vertexDatas.mapIndexed { index, vertexData ->
        vertexData.columns.mapIndexedNotNull { columnIndex, it ->
            val data = bam[vertexData.data]
            val format = bam[data.arrayFormat]
            val offset = vertexData.columns.take(columnIndex).fold(0L) { acc, x ->
                acc + x.byteLen.toLong()
            }
            val (component, type) = when (it) {
                ArrayColumn.Index -> ComponentType.UNSIGNED_SHORT to AccessorType.SCALAR
                ArrayColumn.UV -> ComponentType.FLOAT to AccessorType.VEC2
                ArrayColumn.Vertex -> ComponentType.FLOAT to AccessorType.VEC3
                ArrayColumn.TransformBlend -> return@mapIndexedNotNull null
            }
            Accessor(
                component,
                (data.data.size / format.stride.toInt()).toLong(),
                type,
                byteOffset = offset,
                bufferView = GlTFId(index.toLong()),
                name = it.toString()
            )
        }
    }.flatten()

    val columns = obj.vertexDatas.flatMap { it.columns }.filter { it != ArrayColumn.TransformBlend }.withIndex()
    val vertexId = columns
        .firstNotNullOfOrNull { column -> column.index.takeIf { column.value == ArrayColumn.Vertex } }
    val uvId = columns
        .firstNotNullOfOrNull { column -> column.index.takeIf { column.value == ArrayColumn.UV } }
    val indexId = columns
        .firstNotNullOfOrNull { column -> column.index.takeIf { column.value == ArrayColumn.Index } }
    val meshes = listOf(
        when (obj.primitive) {
            is GeomTrianglesImpl -> {
                Mesh(
                    listOf(
                        MeshPrimitive(
                            attributes = buildJsonObject {
                                if (vertexId != null) put("POSITION", vertexId)
                                if (uvId != null) put("TEXCOORD_0", uvId)
                            },

                            indices = if (indexId != null) GlTFId(indexId.toLong()) else null

                        )
                    )
                )
            }

            is GeomTristripsImpl -> {
                Mesh(
                    listOf(
                        MeshPrimitive(
                            attributes = buildJsonObject {
                                if (vertexId != null) put("POSITION", vertexId)
                                if (uvId != null) put("TEXCOORD_0", uvId)
                            },

                            indices = if (indexId != null) GlTFId(indexId.toLong()) else null,
                            mode = MeshPrimitiveMode.TRIANGLE_STRIP
                        )
                    )
                )
            }

            else -> TODO()
        }
    )

    val scenes = listOf(
        Scene(nodes = listOf(GlTFId(0)))
    )
    val nodes = listOf(
        Node(mesh = GlTFId(0), name = "Mesh${objIndex}")
    )
    val gltf = GlTF(
        Asset("2.0", generator = "bam2gltf"),
        scene = GlTFId(0),
        buffers = buffers,
        bufferViews = bufferViews,
        accessors = accessors,
        meshes = meshes,
        scenes = scenes,
        nodes = nodes
    )

    //println(json.encodeToString(gltf))
    json.encodeToStream(gltf, File("/tmp/mine.gltf").outputStream())
}

fun ByteArray.createDataUri(): String {
    val data = Base64.encode(this)
    return "data:application/octet-stream;base64,$data"
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
