@file:OptIn(ExperimentalUnsignedTypes::class, ExperimentalSerializationApi::class, ExperimentalEncodingApi::class)

package org.toontownkt.bam.gltf

import gltf.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.json.put
import org.joml.*
import org.toontownkt.bam.*
import org.toontownkt.bam.types.*
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.asin
import kotlin.math.atan2

private val json = Json { prettyPrint = true }

@Suppress("unused")
class GlTFBuilder<T>(
    val extraData: T,
    private val _buffers: MutableList<Buffer> = mutableListOf(),
    private val _bufferViews: MutableList<BufferView> = mutableListOf(),
    private val _accessors: MutableList<Accessor> = mutableListOf(),
    private val _nodes: MutableList<Node> = mutableListOf(),
    private val _scenes: MutableList<Scene> = mutableListOf(),
    private val _meshes: MutableList<Mesh> = mutableListOf(),
) {
    private fun <T> MutableList<T>.addWithGlTFId(t: T): GlTFId = this.size.let {
        this += t
        GlTFId(it.toLong())
    }

    val buffers: List<Buffer> = _buffers
    val bufferViews: List<BufferView> = _bufferViews
    val accessors: List<Accessor> = _accessors
    val nodes: List<Node> = _nodes
    val scenes: List<Scene> = _scenes
    val meshes: List<Mesh> = _meshes

    fun build(asset: Asset, scene: GlTFId = GlTFId(0)): GlTF = GlTF(
        asset,
        scene = scene,
        buffers = _buffers,
        bufferViews = _bufferViews,
        accessors = _accessors,
        meshes = _meshes,
        scenes = _scenes,
        nodes = _nodes
    )

    fun buffer(buffer: Buffer): GlTFId = _buffers.addWithGlTFId(buffer)
    fun bufferView(bufferView: BufferView): GlTFId = _bufferViews.addWithGlTFId(bufferView)
    fun accessor(accessor: Accessor): GlTFId = _accessors.addWithGlTFId(accessor)
    fun mesh(mesh: Mesh): GlTFId = _meshes.addWithGlTFId(mesh)
    fun scene(scene: Scene): GlTFId = _scenes.addWithGlTFId(scene)
    fun node(node: Node): GlTFId = _nodes.addWithGlTFId(node)
}

class BamContextBuilder(
    val bam: BamFile,
    val geomVertexArrayDataToBuffer: MutableMap<ObjPointer<GeomVertexArrayData>, GlTFId> = mutableMapOf(),
    val geomVertexArrayDataToBufferView: MutableMap<ObjPointer<GeomVertexArrayData>, GlTFId> = mutableMapOf(),
    val geomToAccessor: MutableMap<ObjPointer<Geom>, Map<ObjPointer<GeomPrimitive>, Map<ArrayColumn, GlTFId>>> = mutableMapOf(),
    val geomToMesh: MutableMap<ObjPointer<GeomNode>, GlTFId> = mutableMapOf(),
    val geomVertexArrayDatas: Map<ObjPointer<GeomVertexArrayData>, GeomVertexArrayData> = bam.getInstancesOf<GeomVertexArrayData>(),
    val geomNodes: Map<ObjPointer<GeomNode>, GeomNode> = bam.getInstancesOf<GeomNode>()
)

typealias BamGlTFBuilder = GlTFBuilder<BamContextBuilder>

val BamGlTFBuilder.bam: BamFile get() = extraData.bam
val BamGlTFBuilder.geomVertexArrayDatas: Map<ObjPointer<GeomVertexArrayData>, GeomVertexArrayData>
    get() = extraData.geomVertexArrayDatas
val BamGlTFBuilder.geomNodes: Map<ObjPointer<GeomNode>, GeomNode> get() = extraData.geomNodes

fun BamGlTFBuilder.createBuffers() {
    val map =
        geomVertexArrayDatas.mapValues { (_, data) ->
            val buf = Buffer(data.data.size.toLong(), uri = data.data.toByteArray().createDataUri())
            buffer(buf)
        }
    extraData.geomVertexArrayDataToBuffer += map
}

fun BamGlTFBuilder.createBufferViews() {
    val map = geomVertexArrayDatas.map { (id, data) ->
        val format = bam[data.arrayFormat]
        val columns = format.columns.map { ArrayColumn.fromGeomVertexColumn(bam, it) }
        val view = BufferView(
            extraData.geomVertexArrayDataToBuffer[id]!!,
            data.data.size.toLong(),
            byteStride = format.stride.toLong(),
            target = if (columns.contains(ArrayColumn.Index)) BufferViewTarget.ELEMENT_ARRAY_BUFFER else BufferViewTarget.ARRAY_BUFFER,
            name = columns.toString()
        )

        id to bufferView(view)
    }.toMap()
    extraData.geomVertexArrayDataToBufferView += map
}

fun BamGlTFBuilder.createAccessors() {
    val map: Map<ObjPointer<Geom>, Map<ObjPointer<GeomPrimitive>, Map<ArrayColumn, GlTFId>>> =
        geomNodes.values.flatMap { geomNode ->
            geomNode.geoms.map { geomEntry ->
                val geom = bam[geomEntry.geom]

                val data = bam[geom.vertexData]
                val formats =
                    bam[data.format].formats.withIndex().associate { data.arrays[it.index] to bam[it.value].columns }
                val textureAttrib = bam[bam[geomEntry.renderState].attribs[0].attrib] as? TextureAttrib
                println(textureAttrib?.let {
                    bam[it.onStages[0].texture].fileName
                })
                geomEntry.geom to geom.primitives.map { geomPrimitivePtr ->
                    val geomPrimitive = bam[geomPrimitivePtr]

                    val indexColumn = geomPrimitive.indices?.let {
                        listOf(it to bam[bam[it].arrayFormat].columns)
                    }.orEmpty().toMap()

                    geomPrimitivePtr to (formats + indexColumn).entries.flatMap { (obj, columns) ->
                        columns.mapIndexedNotNull { columnIndex, vertexColumn ->
                            val column = ArrayColumn.fromGeomVertexColumn(bam, vertexColumn)
                            val bufferView = extraData.geomVertexArrayDataToBufferView[obj]!!

                            val geomVertexArrayData = bam[obj]
                            val format = bam[geomVertexArrayData.arrayFormat]

                            val (componentType, accessorType) = column.toAccessorTypes()
                                ?: return@mapIndexedNotNull null

                            val offset = columns.take(columnIndex).fold(0L) { acc, x ->
                                acc + ArrayColumn.fromGeomVertexColumn(bam, x).byteLen.toLong()
                            }

                            assert(geomPrimitive.firstVertex >= 0)

                            val vertexOffset =
                                if (geomPrimitive.numVertices < 0) 0 else geomPrimitive.firstVertex.toLong() * format.stride.toLong()

                            val count = if (geomPrimitive.numVertices < 0) {
                                val num = (geomVertexArrayData.data.size.toLong() - vertexOffset)
                                val dem = (format.stride.toLong())
                                if ((num % dem) != 0L) error("bad math ${num.toFloat() / dem.toFloat()}")
                                num / dem
                            } else {
                                geomPrimitive.numVertices.toLong()
                            }

                            val accessor = accessor(
                                Accessor(
                                    componentType,
                                    count,
                                    accessorType,
                                    bufferView = bufferView,
                                    byteOffset = offset,
                                    name = column.toString()
                                )
                            )

                            column to accessor
                        }
                    }.toMap()
                }.toMap()
            }
        }.toMap()

    extraData.geomToAccessor += map
}

fun BamGlTFBuilder.createMeshes() {
    val map: Map<ObjPointer<GeomNode>, GlTFId> = geomNodes.entries.associate { entry ->
        val meshPrimitives = entry.value.geoms.flatMap {
            val geom = bam[it.geom]
            val geomAccessors = extraData.geomToAccessor[it.geom]!!
            geom.primitives.map { geomPrimitiveObjPtr ->
                val prim = bam[geomPrimitiveObjPtr]
                val primAccessors = geomAccessors[geomPrimitiveObjPtr]!!
                val mode = when (prim) {
                    is GeomTrianglesImpl -> {
                        MeshPrimitiveMode.TRIANGLES
                    }

                    is GeomPointsImpl -> {
                        MeshPrimitiveMode.POINTS
                    }

                    is GeomTristripsImpl -> {
                        MeshPrimitiveMode.TRIANGLE_STRIP
                    }

                    else -> TODO()
                }

                MeshPrimitive(
                    attributes = buildJsonObject {
                        primAccessors.forEach { entry ->
                            when (entry.key) {
                                ArrayColumn.Index -> {}
                                ArrayColumn.TransformBlend -> TODO()
                                ArrayColumn.UV -> put("TEXCOORD_0", entry.value.inner)
                                ArrayColumn.Vertex -> put("POSITION", entry.value.inner)
                            }
                        }
                    },
                    indices = primAccessors.firstNotNullOfOrNull { entry -> if (entry.key == ArrayColumn.Index) entry.value else null },
                    mode = mode
                )
            }
        }
        entry.key to mesh(Mesh(primitives = meshPrimitives, name = entry.value.name + "Mesh"))
    }

    extraData.geomToMesh += map
}

fun BamGlTFBuilder.createNodes() {
    val tree = bam.toNodeTree()
    val nodeToNode = mutableMapOf<ObjPointer<PandaNode>, GlTFId>()

    tree.visitDepthFirst {
        val node = bam[it.node]
        extraData.geomToMesh
        val mesh = if (node is GeomNode) {
            extraData.geomToMesh[it.node.cast()]
        } else null
        val children = it.children.map { child -> nodeToNode[child.node]!! }
        val transform = bam[node.transform].toMatrix4f()
        val translation =
            Vector3f(transform.m30(), transform.m31(), transform.m32())


        val upper3x3 = transform.get3x3(Matrix3f())
        val scale = transform.getScale(Vector3f())
        val pureRotation = Matrix3f(
            upper3x3.getColumn(0, Vector3f()).normalize(),
            upper3x3.getColumn(1, Vector3f()).normalize(),
            upper3x3.getColumn(2, Vector3f()).normalize()
        )

        val pitchYawRoll = pureRotation.getPitchYawRoll()
        var rotation = Quaternionf()

        // if root node, rotate model so y+ up and z+ forward
        if (it.node.objectId == 1U.toUShort()) {
            rotation = rotation.rotateX(Math.toRadians(-90.0).toFloat()).rotateZ(Math.toRadians(180.0).toFloat())
        }
        nodeToNode[it.node] =
            node(
                Node(
                    name = it.name,
                    mesh = mesh,
                    children = children,
                    translation = listOf(translation.x.toDouble(), translation.y.toDouble(), translation.z.toDouble()),
                    rotation = listOf(
                        rotation.x.toDouble(),
                        rotation.y.toDouble(),
                        rotation.z.toDouble(),
                        rotation.w.toDouble(),
                    ),
                    scale = listOf(scale.x.toDouble(), scale.y.toDouble(), scale.z.toDouble())
                )
            )
    }
}

fun BamGlTFBuilder.createScenes() {
    scene(Scene(nodes = listOf(GlTFId(nodes.indices.last().toLong()))))
}

fun Matrix3f.getPitchYawRoll(): Vector3f {
    val pitch = asin(-m20().toDouble()).toFloat()              // Rotation about X-axis
    val yaw = atan2(m10().toDouble(), m00().toDouble()).toFloat() // Rotation about Y-axis
    val roll = atan2(m21().toDouble(), m22().toDouble()).toFloat() // Rotation about Z-axis

    return Vector3f(pitch, yaw, roll)


}

fun BamFile.createGlTF(): GlTF =
    BamGlTFBuilder(BamContextBuilder(this)).run {
        createBuffers()
        createBufferViews()
        createAccessors()
        createMeshes()
        createNodes()
        createScenes()
        build(Asset(version = "2.0", generator = "bam2gltf"))
    }

fun main() {
    val bamFile = File("../../library/open-toontown-resources/phase_3/models/char/mickey-1200.bam")
    val rawBam = RawBamFile.fromFile(bamFile)
    val bam = BamFile.fromRaw(rawBam)

    //println(bam.toNodeTree())
    val gltf = bam.createGlTF()

    //println(json.encodeToString(gltf))
    json.encodeToStream(gltf, File("/tmp/mine.gltf").outputStream())
}

fun ByteArray.createDataUri(): String {
    val data = Base64.encode(this)
    return "data:application/octet-stream;base64,$data"
}

sealed class ArrayColumn(val byteLen: UInt) {
    data object Vertex : ArrayColumn(12U)
    data object UV : ArrayColumn(8U)
    data object TransformBlend : ArrayColumn(2U)
    data object Index : ArrayColumn(2U)

    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        fun fromInternalName(name: InternalName): ArrayColumn = when (name.name) {
            "vertex" -> Vertex
            "texcoord" -> UV
            "transform_blend" -> TransformBlend
            "index" -> Index
            else -> error("don't know column $name")
        }

        fun fromGeomVertexColumn(bamFile: BamFile, col: GeomVertexColumn): ArrayColumn =
            fromInternalName(bamFile[col.name])

    }

    fun toAccessorTypes(): Pair<ComponentType, AccessorType>? = when (this) {
        Index -> ComponentType.UNSIGNED_SHORT to AccessorType.SCALAR
        TransformBlend -> null
        UV -> ComponentType.FLOAT to AccessorType.VEC2
        Vertex -> ComponentType.FLOAT to AccessorType.VEC3
    }
}