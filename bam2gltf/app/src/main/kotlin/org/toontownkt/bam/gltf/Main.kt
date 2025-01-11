@file:OptIn(
    ExperimentalSerializationApi::class, ExperimentalEncodingApi::class, ExperimentalUnsignedTypes::class,
)
@file:Suppress("OPT_IN_USAGE", "KotlinRedundantDiagnosticSuppress")

package org.toontownkt.bam.gltf

import gltf.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import org.joml.*
import org.toontownkt.bam.*
import org.toontownkt.bam.types.*
import org.toontownkt.bam.types.RenderState
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.io.encoding.ExperimentalEncodingApi

private val json = Json { prettyPrint = true }

fun BamGlTFBuilder.globalTransform(ptr: ObjPointer<PandaNode>): Matrix4f {
    val transforms = mutableListOf(ptr.resolve(bam).transform.resolve(bam).toMatrix4f())
    walkUpParents(ptr) { parentPtr ->
        val parent = bam[parentPtr]
        transforms += bam[parent.transform].toMatrix4f()

        null
    }
    return transforms.asReversed().reduceOrNull { acc, x -> acc * x } ?: Matrix4f()
}

// bam files stores the v texture coordinate inverted
private fun fixUVTextureCoords(bam: BamFile, format: GeomVertexArrayFormat, data: ByteArray): ByteArray {
    val columns = format.columns.toArrayColumns(bam)
    val index = columns.indexOfFirst { it == ArrayColumn.UV }
    return if (index >= 0) {
        val offset = columns.take(index).sumOf { it.byteLen }
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

        buffer.position(offset.toInt())

        while (true) {
            val element = buffer.position()
            val u = buffer.getFloat()
            val v = 1.0f - buffer.getFloat()

            buffer.position(element)
            buffer.putFloat(u)
            buffer.putFloat(v)

            if ((element + format.stride.toInt()) >= buffer.limit()) {
                break
            } else {
                buffer.position(element + format.stride.toInt())
            }
        }

        buffer.rewind()

        ByteArray(buffer.remaining()).also { buffer.get(it) }
    } else {
        data
    }
}

private fun BamGlTFBuilder.fixMeshPosition(data: GeomVertexData, globalTransform: Matrix4f): Map<ArrayColumn, GlTFId> {
    val arrays = data.arrays.resolveAll(bam)
    val map = mutableMapOf<ArrayColumn, GlTFId>()
    arrays.map { arr ->
        val format = arr.arrayFormat.resolve(bam)
        val columns = format.columns.toArrayColumns(bam)
        if (columns.contains(ArrayColumn.Vertex)) {
            val stride = columns.sumOf { it.byteLen }.toInt()
            val offset = columns.take(columns.indexOf(ArrayColumn.Vertex)).sumOf { it.byteLen }.toInt()
            val byteData = ByteBuffer.wrap(arr.data.toByteArray()).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                while (hasRemaining()) {
                    val row = position()

                    position(row + offset)

                    mark()
                    val vec = Vector4f(getVec3f(), 1.0f) * globalTransform
                    reset()

                    putFloat(vec.x)
                    putFloat(vec.y)
                    putFloat(vec.z)

                    position(row + stride)
                }
            }.run {
                rewind()
                ByteArray(remaining()).also { get(it) }
            }
            Triple(columns, fixUVTextureCoords(bam, format, byteData), stride)
        } else {
            Triple(columns, fixUVTextureCoords(bam, format, arr.data.toByteArray()), format.stride.toInt())
        }
    }.map { (columns, arr, stride) ->
        val view = createBufferView(arr, columns)
        for (column in columns) {
            val (component, accessor) = column.toAccessorTypes() ?: continue
            map[column] = accessor(
                Accessor(
                    component,
                    (arr.size / stride).toLong(),
                    accessor,
                    bufferView = view,
                    byteOffset = data.findByteOffset(bam, column)
                )
            )
        }
    }
    return map
}

private fun BamGlTFBuilder.findJoint(node: ObjPointer<PandaNode>): ObjPointer<CharacterJoint> {
    val character = bam.getInstancesOf<Character>().values.first()
    return character.bundles[0].resolve(bam).findJoint(bam, node)!!
}

private fun PartGroup.findJoint(bam: BamFile, node: ObjPointer<PandaNode>): ObjPointer<CharacterJoint>? {
    children.forEach {
        val part = it.resolve(bam)
        if (part is CharacterJoint && part.netTransformNodes.contains(node)) {
            return it.unsafeDowncast()
        }

        val joint = part.findJoint(bam, node)
        if (joint != null) return joint
    }
    return null
}

private fun BamGlTFBuilder.findBoundNode(nodePtr: ObjPointer<PandaNode>): ObjPointer<PandaNode>? =
    walkUpParents(nodePtr) { node ->
        val effects = node.resolve(bam).effects.resolve(bam).effects.resolveAll(bam)
        if (effects.filterIsInstance<CharacterJointEffect>().isNotEmpty()) {
            node
        } else null
    }

private fun BamGlTFBuilder.createWeightsAndJoints(
    vertexData: GeomVertexData,
    node: ObjPointer<PandaNode>,
): Pair<GlTFId, GlTFId>? {
    val weightBuilder = ByteArrayOutputStream()
    val jointBuilder = ByteArrayOutputStream()
    val arrays = vertexData.arrays.resolveAll(bam)

    // assert the first data array has vertices
    val format = vertexData.format.resolve(bam)
    assert(format.formats.resolveAll(bam)[0].columns.toArrayColumns(bam).contains(ArrayColumn.Vertex))

    val firstArray = arrays[0]
    val count = firstArray.data.size.toLong() / firstArray.arrayFormat.resolve(bam).stride.toLong()

    val boundNode = findBoundNode(node)
    var jointIndex: Int? = null
    if (boundNode != null) {
        val joint = findJoint(boundNode)
        jointIndex = extraData.rootSkinJoints[joint]!!
    }

    if (jointIndex != null) {
        for (i in 0..<count) {
            weightBuilder.write(Vector4f(1f, 0f, 0f, 0f).serialize())
            jointBuilder.write(ByteBuffer.wrap(ByteArray(8)).run {
                order(ByteOrder.LITTLE_ENDIAN)
                for (j in 0..<4) {
                    putShort(
                        if (j == 0) {
                            jointIndex.toShort()
                        } else {
                            0
                        }
                    )
                }
                rewind()
                ByteArray(8).also { get(it) }
            })
        }
    } else {
        val blendTable = (vertexData.transformBlendTable ?: return null).resolve(bam)
        val dataArray = arrays
            .firstOrNull {
                !it.arrayFormat.resolve(bam).columns.toArrayColumns(bam).contains(ArrayColumn.TransformBlend)
            } ?: return null
        val blendArray = arrays.firstOrNull {
            it.arrayFormat.resolve(bam).columns.toArrayColumns(bam).contains(ArrayColumn.TransformBlend)
        } ?: return null

        val dataArrayRows = dataArray.data.size / bam[dataArray.arrayFormat].stride.toInt()
        val blendArrayRows = blendArray.data.size / ArrayColumn.TransformBlend.byteLen.toInt()
        assert(dataArrayRows == blendArrayRows)

        val blendArrayBuffer = ByteBuffer.wrap(blendArray.data.toByteArray()).order(ByteOrder.LITTLE_ENDIAN)

        for (i in 0..<blendArrayRows) {
            val tableIndex = blendArrayBuffer.getShort().toUShort()
            val entry = blendTable.blends[tableIndex.toInt()]

            val weight = Vector4f(0f)
            val joint = mutableListOf<UShort>(0U, 0U, 0U, 0U)

            val transformEntries = entry.entries.map {
                val jointVertexTransform = it.transform.unsafeDowncast<_, JointVertexTransform>().resolve(bam)
                it.weight to extraData.rootSkinJoints[jointVertexTransform.joint]!!
            }

            val entries =
                if (transformEntries.size < 4) {
                    transformEntries
                } else {
                    val e = transformEntries.sortedByDescending { it.first }.take(4)
                    val newWeight = e.sumOf { it.first.toDouble() }
                    e.map { (it.first.toDouble() / newWeight).toFloat() to it.second }
                }

            entries.forEachIndexed { index, (weightVal, jointTransform) ->
                weight.setComponent(index, weightVal)
                joint[index] = jointTransform.toUShort()
            }

            weightBuilder.write(weight.serialize())
            jointBuilder.write(ByteBuffer.wrap(ByteArray(8)).run {
                order(ByteOrder.LITTLE_ENDIAN)
                for (j in joint) {
                    putShort(j.toShort())
                }
                rewind()
                ByteArray(8).also { get(it) }
            })
        }
    }

    val weightData = weightBuilder.toByteArray()
    val jointData = jointBuilder.toByteArray()
    val weightDataBufferView =
        bufferView(
            BufferView(
                buffer(Buffer(weightData.size.toLong(), uri = weightData.createDataUri())),
                weightData.size.toLong(),
                target = BufferViewTarget.ARRAY_BUFFER
            )
        )
    val jointDataBufferView =
        bufferView(
            BufferView(
                buffer(Buffer(jointData.size.toLong(), uri = jointData.createDataUri())),
                jointData.size.toLong(),
                target = BufferViewTarget.ARRAY_BUFFER
            )
        )

    val weightAccessor = accessor(
        Accessor(
            ComponentType.FLOAT,
            count,
            AccessorType.VEC4,
            bufferView = weightDataBufferView
        )
    )
    val jointAccessor = accessor(
        Accessor(
            ComponentType.UNSIGNED_SHORT,
            count,
            AccessorType.VEC4,
            bufferView = jointDataBufferView
        )
    )

    return weightAccessor to jointAccessor
}

fun BamGlTFBuilder.createBufferView(byteData: ByteArray, columns: List<ArrayColumn>): GlTFId {
    val stride = columns.sumOf { it.byteLen }.toLong()
    val buf = buffer(Buffer(byteData.size.toLong(), uri = byteData.createDataUri()))
    return bufferView(
        BufferView(
            buf,
            byteData.size.toLong(),
            byteStride = if (columns.size == 1) null else stride,
            target = if (columns.contains(ArrayColumn.Index)) BufferViewTarget.ELEMENT_ARRAY_BUFFER else BufferViewTarget.ARRAY_BUFFER,
            name = columns.toString()
        )
    )
}

fun BamGlTFBuilder.createBufferViews(geomVertexDataPtr: ObjPointer<GeomVertexData>): Map<ArrayColumn, GlTFId> {
    extraData.cachedBufferViews[geomVertexDataPtr]?.let {
        return it
    }

    val geomVertexData = geomVertexDataPtr.resolve(bam)
    val map = mutableMapOf<ArrayColumn, GlTFId>()

    val formats = geomVertexData.format.resolve(bam).formats.resolveAll(bam).zip(
        geomVertexData.arrays.resolveAll(bam)
    )

    for ((format, data) in formats) {
        val columns = format.columns.toArrayColumns(bam)
        val byteData = fixUVTextureCoords(bam, format, data.data.toByteArray())

        val view = createBufferView(byteData, columns)

        for (column in columns) {
            map[column] = view
        }
    }

    extraData.cachedBufferViews[geomVertexDataPtr] = map

    return map
}

fun BamGlTFBuilder.createMesh(geomNodePtr: ObjPointer<GeomNode>): GlTFId {
    extraData.cachedMeshes[geomNodePtr]?.let {
        return it
    }

    val geomNode = geomNodePtr.resolve(bam)
    val meshPrimitives = geomNode.geoms.flatMap { geomEntry ->
        val geom = geomEntry.geom.resolve(bam)
        val vertexData = geom.vertexData.resolve(bam)
        val (vertexArrayFormat, vertexArrayData) = vertexData.arrays.resolveAll(bam).firstNotNullOf { arr ->
            if (arr.arrayFormat.resolve(bam).columns.toArrayColumns(bam)
                    .contains(ArrayColumn.Vertex)
            ) arr.arrayFormat.resolve(bam) to arr.data else null
        }

        val material = createMaterial(geomEntry.renderState)

        geom.primitives.map { geomPrimitiveObjPtr ->
            val geomPrimitive = geomPrimitiveObjPtr.resolve(bam)
            val mode = when (geomPrimitive) {
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

            val weightsAndJoints = createWeightsAndJoints(
                vertexData,
                geomNodePtr.upcast()
            )

            assert(geomPrimitive.firstVertex >= 0)

            val vertexOffset =
                if (geomPrimitive.numVertices < 0) 0 else geomPrimitive.firstVertex.toLong() * vertexArrayFormat.stride.toLong()

            val count = if (geomPrimitive.numVertices < 0) {
                val num = (vertexArrayData.size.toLong() - vertexOffset)
                val dem = (vertexArrayFormat.stride.toLong())
                if ((num % dem) != 0L) error("bad math ${num.toFloat() / dem.toFloat()}")
                num / dem
            } else {
                geomPrimitive.numVertices.toLong()
            }

            val boundNode = findBoundNode(geomNodePtr.upcast())

            val accessors = if (boundNode == null) {
                createBufferViews(geom.vertexData).entries.mapNotNull { (k, v) ->
                    val (component, accessor) = k.toAccessorTypes() ?: return@mapNotNull null
                    k to accessor(
                        Accessor(
                            component,
                            count,
                            accessor,
                            bufferView = v,
                            byteOffset = vertexData.findByteOffset(bam, k)
                        )
                    )
                }.toMap().toMutableMap()
            } else {
                fixMeshPosition(vertexData, globalTransform(geomNodePtr.upcast())).toMutableMap()
            }

            geomPrimitive.indices?.let {
                val indicesData = it.resolve(bam).data.toByteArray()
                val buffer = buffer(Buffer(indicesData.size.toLong(), uri = indicesData.createDataUri()))
                val view = bufferView(
                    BufferView(
                        buffer,
                        indicesData.size.toLong(),
                        target = BufferViewTarget.ELEMENT_ARRAY_BUFFER
                    )
                )
                val accessor = accessor(
                    Accessor(
                        ComponentType.UNSIGNED_SHORT,
                        indicesData.size.toLong() / 2L,
                        AccessorType.SCALAR,
                        bufferView = view
                    )
                )

                accessors += ArrayColumn.Index to accessor
            }

            MeshPrimitive(
                attributes = buildJsonObject {
                    accessors.forEach { entry ->
                        when (entry.key) {
                            ArrayColumn.Index -> {}
                            ArrayColumn.TransformBlend -> TODO()
                            ArrayColumn.UV -> put("TEXCOORD_0", entry.value.inner)
                            ArrayColumn.Vertex -> put("POSITION", entry.value.inner)
                        }
                    }
                    weightsAndJoints?.let { (weights, joints) ->
                        put("JOINTS_0", joints.inner)
                        put("WEIGHTS_0", weights.inner)
                    }
                },
                indices = accessors.firstNotNullOfOrNull { (k, v) -> if (k == ArrayColumn.Index) v else null },
                mode = mode,
                material = material,
            )
        }
    }

    val id = mesh(Mesh(primitives = meshPrimitives, name = geomNode.name + "Mesh"))
    extraData.cachedMeshes[geomNodePtr] = id

    return id
}

fun BamGlTFBuilder.createNode(nodePtr: ObjPointer<PandaNode>): GlTFId {
    extraData.cachedNodes[nodePtr]?.let {
        return it
    }

    val node = nodePtr.resolve(bam)

    var mesh: GlTFId? = null

    if (node is GeomNode) {
        mesh = createMesh(nodePtr.unsafeDowncast())
    }

    var rootJoint: GlTFId? = null
    if (node is Character) {
        val joints = mutableListOf<Pair<ObjPointer<CharacterJoint>, GlTFId>>()
        extraData.rootSkeletonSkin = createSkeleton(nodePtr.unsafeDowncast(), joints)
        rootJoint = joints.first().second
    }


    val skinned = mesh?.let { meshId ->
        val meshGltf = meshes[meshId.inner.toInt()]
        meshGltf.primitives.any { prim ->
            prim.attributes.jsonObject["JOINTS_0"] != null
        }
    } ?: false

    val transform = node.transform.resolve(bam).toMatrix4f()
    val translation =
        Vector3f(transform.m30(), transform.m31(), transform.m32())


    val scale = transform.getScale(Vector3f())
    var rotation = transform.getUnnormalizedRotation(Quaternionf())

    // if root node, rotate model so y+ up and z+ forward
    if (nodePtr.objectId == 1U.toUShort()) {
        rotation = rotation.rotateX(Math.toRadians(-90.0).toFloat()).rotateZ(Math.toRadians(180.0).toFloat())
    }

    val id = node(
        Node(
            name = node.name,
            mesh = mesh,
            translation = encodeVec3f(translation),
            rotation = encodeQuatf(rotation),
            scale = encodeVec3f(scale),
            skin = if (skinned) extraData.rootSkeletonSkin!! else null
        )
    )
    val children =
        node.children.map { child -> createNode(child) } + (rootJoint?.let { listOf(it) } ?: emptyList())
    nodes[id.inner.toInt()] = nodes[id.inner.toInt()].copy(children = children.ifEmpty { null })


    extraData.cachedNodes[nodePtr] = id

    return id
}

fun BamGlTFBuilder.createScenes(root: GlTFId) {
    scene(Scene(nodes = listOf(root)))
}

fun BamGlTFBuilder.createMaterial(renderStatePtr: ObjPointer<RenderState>): GlTFId {
    extraData.cachedMaterials[renderStatePtr]?.let {
        return it
    }

    val renderState = renderStatePtr.resolve(bam)
    val attribs = renderState.attribs.map { bam[it.attrib] }

    var name: String? = null
    val textureAttrib = attribs.filterIsInstance<TextureAttrib>().firstOrNull()
    val transparencyAttrib = attribs.filterIsInstance<TransparencyAttrib>().firstOrNull()
    val colorAttrib = attribs.filterIsInstance<ColorAttrib>().firstOrNull()

    val alpha = transparencyAttrib?.let {
        MaterialAlphaMode.BLEND
    }
    val color = colorAttrib?.color?.colorVec


    val texture = textureAttrib?.let { attrib ->
        val tex = bam[attrib.onStages[0].texture]
        name = tex.fileName


        val textureFile = resolveResource(tex.fileName)
        val textureData = textureFile.readBytes()
        val textureBuffer = buffer(Buffer(textureData.size.toLong(), uri = textureData.createDataUri()))
        val textureBufferView = bufferView(BufferView(textureBuffer, textureData.size.toLong()))
        val image = image(
            Image(
                name = tex.fileName, bufferView = textureBufferView, mimeType = when (textureFile.extension) {
                    "png" -> MimeType.image_png
                    "jpg" -> MimeType.image_jpeg
                    "jpeg" -> MimeType.image_jpeg
                    else -> error(textureFile.extension)
                }
            )
        )

        assert(tex.magFilter == 1U.toUByte())
        assert(tex.minFilter == 5U.toUByte())
        assert(tex.wrapU == 1U.toUByte())
        assert(tex.wrapV == 1U.toUByte())
        assert(tex.alphaFileName.isBlank())

        val sampler = sampler(
            Sampler(
                magFilter = SamplerMagFilter.LINEAR,
                minFilter = SamplerMinFilter.LINEAR_MIPMAP_LINEAR,
                wrapS = SamplerWrapS.REPEAT,
                wrapT = SamplerWrapT.REPEAT,
            )
        )

        val texture = texture(gltf.Texture(name = tex.fileName, sampler = sampler, source = image))

        texture
    }

    val id = material(
        Material(
            name = name,
            pbrMetallicRoughness = MaterialPBRMetallicRoughness(
                baseColorTexture = texture?.let { TextureInfo(index = it) },
                baseColorFactor = color?.let {
                    encodeVec4f(it)
                }),
            alphaMode = alpha,
            doubleSided = true

        )
    )

    extraData.cachedMaterials[renderStatePtr] = id

    return id

}

fun BamGlTFBuilder.createJoint(
    partGroupPtr: ObjPointer<PartGroup>,
    joints: MutableList<Pair<ObjPointer<CharacterJoint>, GlTFId>> = mutableListOf(),
): GlTFId {
    val partGroup = partGroupPtr.resolve(bam)

    if (partGroup !is CharacterJoint) {
        require(partGroup.children.size == 1)
        return createJoint(partGroup.children[0], joints)
    }

    val characterJointPtr = partGroupPtr.unsafeDowncast<_, CharacterJoint>()
    val characterJoint: CharacterJoint = partGroup


    val translation: Vector3f = characterJoint.value.getTranslation(Vector3f())
    val rotation: Quaternionf = characterJoint.value.getUnnormalizedRotation(Quaternionf())
    val scale: Vector3f = characterJoint.value.getScale(Vector3f())


    val id = node(
        Node(
            name = characterJoint.name,
            translation = encodeVec3f(translation),
            rotation = encodeQuatf(rotation),
            scale = encodeVec3f(scale),
        )
    )

    joints += characterJointPtr to id

    nodes[id.inner.toInt()] = nodes[id.inner.toInt()].copy(
        children = characterJoint.children.map {
            require(it.resolve(bam) is CharacterJoint)
            createJoint(it.unsafeDowncast(), joints)
        }.ifEmpty { null }
    )


    return id
}

fun BamGlTFBuilder.createSkeleton(
    characterPtr: ObjPointer<Character>,
    joints: MutableList<Pair<ObjPointer<CharacterJoint>, GlTFId>>
): GlTFId {
    val character = characterPtr.resolve(bam)

    require(character.bundles.size == 1)
    val bundlePtr = character.bundles.first()
    val bundle = bundlePtr.resolve(bam)

    require(bundle.children.size == 1)
    createJoint(bundle.children.first().unsafeDowncast(), joints)

    val inverseJointData = joints.fold(ByteArrayOutputStream()) { acc, x ->
        acc.apply {
            write(x.first.resolve(bam).initialNetTransformInverse.serialize())
        }
    }.toByteArray()

    val buf = buffer(Buffer(inverseJointData.size.toLong(), uri = inverseJointData.createDataUri()))
    val bufView = bufferView(BufferView(buf, inverseJointData.size.toLong()))
    val accessor =
        accessor(Accessor(ComponentType.FLOAT, joints.size.toLong(), AccessorType.MAT4, bufferView = bufView))

    extraData.rootSkinJoints += joints.mapIndexed { index, pair -> pair.first to index }
    return skin(
        Skin(
            joints.map { it.second }, name = character.name, inverseBindMatrices = accessor, skeleton = joints[0].second
        )
    )
}

fun BamGlTFBuilder.createAnimations() {
    if (extraData.animationFiles.isEmpty()) return
    for (animationFile in extraData.animationFiles) {

        val tables = animationFile.getInstancesOf<AnimChannelMatrixXfmTable>()

        // assert that only one root animation is present
        assert(tables.values.map { it.root }.toSet().size == 1)
        val root = animationFile[tables.values.first().root]

        val numFrames = root.numFrames
        val timestampData = (0..<numFrames.toInt()).map { it.toFloat() / root.fps }.serialize()
        val timestampAccessor = accessor(
            Accessor(
                ComponentType.FLOAT, numFrames.toLong(), AccessorType.SCALAR, bufferView = bufferView(
                    BufferView(
                        buffer(Buffer(timestampData.size.toLong(), uri = timestampData.createDataUri())),
                        timestampData.size.toLong()
                    )
                )
            )
        )

        val channels = mutableListOf<AnimationChannel>()
        val animationSamplers = mutableListOf<AnimationSampler>()

        tables.values.forEach { table ->
            require(table.newHPRConvention)
            require(!table.compressedChannels)

            val node = GlTFId(nodes.indexOfFirst { it.name == table.name }
                .toLong()
                .also { require(it != -1L) }
            )

            val animationRoot = animationFile[table.root]
            val frames = animationRoot.numFrames.toInt()
            require((0..<frames).all { table.getShear(it) == Vector3f(0f) })

            val posData = (0..<frames).map { table.getPos(it) }.flatMap { pos ->
                encodeVec3f(pos).map { it.toFloat() }
            }.serialize()
            val rotData = (0..<frames).map { table.getQuat(it) }.flatMap { quat ->
                encodeQuatf(quat).map { it.toFloat() }
            }.serialize()
            val scaleData = (0..<frames).map { table.getScale(it) }.flatMap { scale ->
                encodeVec3f(scale).map { it.toFloat() }
            }.serialize()

            val channelList =
                listOf(TargetPath.translation to posData, TargetPath.rotation to rotData, TargetPath.scale to scaleData)
            for ((path, data) in channelList) {
                val accessorType = if (path == TargetPath.rotation) AccessorType.VEC4 else AccessorType.VEC3
                val channelAccessor = accessor(
                    Accessor(
                        ComponentType.FLOAT, frames.toLong(), accessorType, bufferView = bufferView(
                            BufferView(
                                buffer(
                                    Buffer(
                                        data.size.toLong(),
                                        uri = data.createDataUri()
                                    )
                                ),
                                data.size.toLong()
                            )
                        )
                    )
                )

                val animationSampler =
                    AnimationSampler(
                        timestampAccessor,
                        channelAccessor,
                        interpolation = AnimationInterpolation.LINEAR
                    ).let {
                        val id = animationSamplers.size
                        animationSamplers += it
                        GlTFId(id.toLong())
                    }
                channels += AnimationChannel(animationSampler, AnimationChannelTarget(path, node = node))
            }
        }

        animation(Animation(channels, animationSamplers, name = animationFile.asModelRoot()!!.name))
    }
}

fun BamFile.createGlTF(resourceRoot: File, animations: List<File>): GlTF =
    BamGlTFBuilder(BamContext(this, animations.map { BamFile.fromFile(it) }, resourceRoot)).run {
        val root = createNode(ObjPointer(1U))
        createScenes(root)
        createAnimations()
        build(Asset(version = "2.0", generator = "bam2gltf"))
    }

fun main() {
    val bamFile = File("../../library/open-toontown-resources/phase_3/models/char/minnie-1200.bam")
    val rawBam = RawBamFile.fromFile(bamFile)
    val bam = BamFile.fromRaw(rawBam)

    val gltf = bam.createGlTF(
        File("../../library/open-toontown-resources"),
        //listOf(),
        listOf(File("../../library/open-toontown-resources/phase_3/models/char/minnie-walk.bam")),
    )

    //println(json.encodeToString(gltf))
    json.encodeToStream(gltf, File("/tmp/mine.gltf").outputStream())
}
