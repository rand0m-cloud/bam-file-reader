package org.toontownkt.bam.gltf

import gltf.*
import gltf.Texture
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.toontownkt.bam.BamFile
import org.toontownkt.bam.types.*
import java.io.File

@Suppress("unused", "MemberVisibilityCanBePrivate")
class GlTFBuilder<T>(
    val extraData: T,
    val buffers: MutableList<Buffer> = mutableListOf(),
    val bufferViews: MutableList<BufferView> = mutableListOf(),
    val accessors: MutableList<Accessor> = mutableListOf(),
    val nodes: MutableList<Node> = mutableListOf(),
    val scenes: MutableList<Scene> = mutableListOf(),
    val meshes: MutableList<Mesh> = mutableListOf(),
    val samplers: MutableList<Sampler> = mutableListOf(),
    val images: MutableList<Image> = mutableListOf(),
    val textures: MutableList<Texture> = mutableListOf(),
    val materials: MutableList<Material> = mutableListOf(),
    val skins: MutableList<Skin> = mutableListOf(),
    val animations: MutableList<Animation> = mutableListOf(),
) {
    private fun <T> MutableList<T>.addWithGlTFId(t: T): GlTFId = this.size.let {
        this += t
        GlTFId(it.toLong())
    }

    fun build(asset: Asset, scene: GlTFId = GlTFId(0)): GlTF = GlTF(
        asset,
        scene = scene,
        buffers = buffers.ifEmpty { null },
        bufferViews = bufferViews.ifEmpty { null },
        accessors = accessors.ifEmpty { null },
        meshes = meshes.ifEmpty { null },
        scenes = scenes.ifEmpty { null },
        nodes = nodes.ifEmpty { null },
        samplers = samplers.ifEmpty { null },
        images = images.ifEmpty { null },
        textures = textures.ifEmpty { null },
        materials = materials.ifEmpty { null },
        skins = skins.ifEmpty { null },
        animations = animations.ifEmpty { null },
    )

    fun buffer(buffer: Buffer): GlTFId = buffers.addWithGlTFId(buffer)
    fun bufferView(bufferView: BufferView): GlTFId = bufferViews.addWithGlTFId(bufferView)
    fun accessor(accessor: Accessor): GlTFId = accessors.addWithGlTFId(accessor)
    fun mesh(mesh: Mesh): GlTFId = meshes.addWithGlTFId(mesh)
    fun scene(scene: Scene): GlTFId = scenes.addWithGlTFId(scene)
    fun node(node: Node): GlTFId = nodes.addWithGlTFId(node)
    fun sampler(sampler: Sampler): GlTFId = samplers.addWithGlTFId(sampler)
    fun image(image: Image): GlTFId = images.addWithGlTFId(image)
    fun texture(texture: Texture): GlTFId = textures.addWithGlTFId(texture)
    fun material(material: Material): GlTFId = materials.addWithGlTFId(material)
    fun skin(skin: Skin): GlTFId = skins.addWithGlTFId(skin)
    fun animation(animation: Animation): GlTFId = animations.addWithGlTFId(animation)

    internal fun Node.getTranslation(): Vector3f {
        return matrix?.let {
            val (c0, c1, c2, c3) = it.map { it.toFloat() }.chunked(4).map { Vector4f(it[0], it[1], it[2], it[3]) }
            Matrix4f(c0, c1, c2, c3).getTranslation(Vector3f())
        } ?: translation?.let {
            val (x, y, z) = it.map { it.toFloat() }
            Vector3f(x, y, z)
        } ?: Vector3f(0f)
    }
}

class BamContext(
    val bam: BamFile,
    val animationFiles: List<BamFile>,
    val resourceRoot: File,
    val childToParent: Map<ObjPointer<PandaNode>, ObjPointer<PandaNode>> = createChildToParent(bam),

    val cachedBufferViews: MutableMap<ObjPointer<GeomVertexData>, Map<ArrayColumn, GlTFId>> = mutableMapOf(),
    val cachedMaterials: MutableMap<ObjPointer<RenderState>, GlTFId> = mutableMapOf(),
    val cachedNodes: MutableMap<ObjPointer<PandaNode>, GlTFId> = mutableMapOf(),
    val cachedMeshes: MutableMap<ObjPointer<GeomNode>, GlTFId> = mutableMapOf(),

    var rootSkeletonSkin: GlTFId? = null,
    val rootSkinJoints: MutableMap<ObjPointer<CharacterJoint>, Int> = mutableMapOf()
)

typealias BamGlTFBuilder = GlTFBuilder<BamContext>

val BamGlTFBuilder.bam: BamFile get() = extraData.bam

fun BamGlTFBuilder.getParent(child: ObjPointer<PandaNode>): ObjPointer<PandaNode>? = extraData.childToParent[child]

fun BamGlTFBuilder.ifAnyParent(
    child: ObjPointer<PandaNode>,
    predicate: (ObjPointer<PandaNode>) -> Boolean
): Boolean = walkUpParents(child, predicate) ?: false

fun <T> BamGlTFBuilder.walkUpParents(
    child: ObjPointer<PandaNode>,
    predicate: (ObjPointer<PandaNode>) -> T?
): T? {
    var node = child

    while (true) {
        val parent = getParent(node) ?: break

        val result = predicate(parent)
        if (result != null) {
            return result
        }

        node = parent
    }

    return null
}

fun BamGlTFBuilder.resolveResource(fileName: String): File = extraData.resourceRoot.resolve(fileName)

