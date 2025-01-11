@file:OptIn(ExperimentalUnsignedTypes::class)

package org.toontownkt.bam.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.toontownkt.bam.BamFile

@Serializable
public sealed interface PandaObject

@JvmInline
@Serializable(with = ObjPointerSerializer::class)
public value class ObjPointer<@Suppress("unused") T : PandaObject>(public val objectId: UShort) {
    @Deprecated("Use unsafeDowncast instead for some type safety", ReplaceWith("unsafeDowncast()"))
    public fun <U : PandaObject> unsafeCast(): ObjPointer<U> = ObjPointer(objectId)
}

public inline fun <reified T, reified U> ObjPointer<T>.upcast(): ObjPointer<U>
        where U : PandaObject,
              T : U {
    return ObjPointer(objectId)
}

public inline fun <reified T, reified U> ObjPointer<T>.unsafeDowncast(): ObjPointer<U>
        where T : PandaObject,
              U : T {
    return ObjPointer(objectId)
}

public inline fun <reified T : PandaObject> ObjPointer<T>.resolve(bam: BamFile): T = bam[this]
public inline fun <reified T : PandaObject> ObjList<T>.resolveAll(bam: BamFile): List<T> = map { it.resolve(bam) }

public object ObjPointerSerializer : KSerializer<ObjPointer<*>> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("ObjPointer", PrimitiveKind.SHORT)

    override fun deserialize(decoder: Decoder): ObjPointer<*> {
        return ObjPointer<PandaObject>(decoder.decodeShort().toUShort())
    }

    override fun serialize(encoder: Encoder, value: ObjPointer<*>) {
        encoder.encodeShort(value.objectId.toShort())
    }

}


@Serializable
public sealed interface PandaNode : PandaObject {
    public val name: String
    public val state: ObjPointer<RenderState>
    public val transform: ObjPointer<TransformState>
    public val effects: ObjPointer<RenderEffects>
    public val drawControlMask: UInt
    public val drawShowMask: UInt
    public val intoCollideMask: UInt
    public val boundsType: UByte
    public val keys: Map<String, String>
    public val children: ObjList<PandaNode>
}

@Serializable
public sealed interface ModelRoot : ModelNode

@Serializable
public sealed interface ModelNode : PandaNode {
    public val preserveTransform: UByte
    public val preserveAttributes: UShort
}

@Serializable
public sealed interface Character : PartBundleNode

@Serializable
public sealed interface PartBundleNode : PandaNode {
    public val bundles: ObjList<PartBundle>
}

@Serializable
public sealed interface PartGroup : PandaObject {
    public val name: String
    public val children: ObjList<PartGroup>
}

@Serializable
public sealed interface PartBundle : PartGroup {
    public val animPreloadTable: ObjPointer<AnimPreloadTable>?
    public val blendType: UByte
    public val animBlendFlag: Boolean
    public val frameBlendFlag: Boolean
    public val rootTransform: Matrix4f
}

@Serializable
public sealed interface AnimPreloadTable : PandaObject {
    public val entries: List<AnimPreloadTableEntry>
}

@Serializable
public sealed interface AnimPreloadTableEntry {
    public val baseName: String
    public val baseFrameRate: Float
    public val frames: Int

}


@Serializable
public sealed interface CharacterJointBundle : PartBundle

@Serializable
public sealed interface GeomEntry {
    public val geom: ObjPointer<Geom>
    public val renderState: ObjPointer<RenderState>
}

@Serializable
@SerialName("GeomEntry")
public data class GeomEntryImpl(
    public override val geom: ObjPointer<Geom>,
    override val renderState: ObjPointer<RenderState>
) :
    GeomEntry

@Serializable
public sealed interface Geom : PandaObject {
    public val vertexData: ObjPointer<GeomVertexData>
    public val primitives: ObjList<GeomPrimitive>
    public val primitiveType: UByte
    public val shadeModel: UByte
    public val geomRendering: UShort
    public val boundsType: UByte
}

// TODO: missing PTA data
@Serializable
public sealed interface GeomPrimitive : PandaObject {
    public val shadeModel: UByte
    public val firstVertex: Int
    public val numVertices: Int
    public val indexType: UByte
    public val usageHint: UByte
    public val indices: ObjPointer<GeomVertexArrayData>?
}

@Serializable
public sealed interface GeomTriangles : GeomPrimitive

@Serializable
public sealed interface GeomVertexData : PandaObject {
    public val name: String
    public val format: ObjPointer<GeomVertexFormat>
    public val usageHint: UByte
    public val arrays: ObjList<GeomVertexArrayData>
    public val transformTable: ObjPointer<TransformTable>?
    public val transformBlendTable: ObjPointer<TransformBlendTable>?
    public val sliderTable: ObjPointer<SliderTable>?
}

@Serializable
public sealed interface SliderDef {
    public val name: String
    public val slider: ObjPointer<VertexSlider>
    public val rows: SparseArray
}

@Serializable
public sealed interface VertexSlider : PandaObject

@Serializable
public sealed interface CharacterVertexSlider : VertexSlider {
    public val characterSlider: ObjPointer<CharacterSlider>
}

@Serializable
public sealed interface CharacterSlider : MovingPartScalar

@Serializable
public sealed interface MovingPartScalar : MovingPart<Float>

@Serializable
public sealed interface SliderTable : PandaObject {
    public val sliders: List<SliderDef>
}

@Serializable
public sealed interface TransformTable : PandaObject {
    public val transforms: ObjList<VertexTransform>
}

@Serializable
public sealed interface TransformBlendTable : PandaObject {
    public val blends: List<TransformBlend>
    public val rows: SparseArray
}

@Serializable
public sealed interface TransformEntry {
    public val transform: ObjPointer<VertexTransform>
    public val weight: Float
}

@Serializable
public sealed interface TransformBlend {
    public val entries: List<TransformEntry>
}

@Serializable
public sealed interface VertexTransform : PandaObject

@Serializable
public sealed interface JointVertexTransform : VertexTransform {
    public val joint: ObjPointer<CharacterJoint>
}

@Serializable
public sealed interface MovingPartMatrix : MovingPart<Matrix4f>

@Serializable
public sealed interface MovingPart<T> : MovingPartBase {
    public val value: T
    public val defaultValue: T
}

@Serializable
public sealed interface MovingPartBase : PartGroup {
    public val forcedChannel: ObjPointer<AnimChannelBase>?
}

@Serializable
public sealed interface AnimChannelBase : AnimGroup {
    public val lastFrame: UShort
}

@Serializable
public sealed interface AnimGroup : PandaObject {
    public val name: String
    public val root: ObjPointer<AnimBundle>
    public val children: ObjList<AnimGroup>
}

@Serializable
public sealed interface GeomVertexArrayData : PandaObject {
    public val usageHint: UByte
    public val arrayFormat: ObjPointer<GeomVertexArrayFormat>
    public val data: UByteArray
}

@Serializable
public sealed interface SparseArray {
    public val subRanges: List<Pair<Int, Int>>
    public val inverse: Boolean
}

@Serializable
public sealed interface GeomNode : PandaNode {
    public val geoms: List<GeomEntry>
}

@Serializable
public sealed interface GeomVertexFormat : PandaObject {
    public val animationSpec: GeomVertexAnimationSpec
    public val formats: ObjList<GeomVertexArrayFormat>
}

@Serializable
public sealed interface GeomVertexArrayFormat : PandaObject {
    public val stride: UShort
    public val totalBytes: UShort
    public val padTo: UByte
    public val divisor: UShort?
    public val columns: List<GeomVertexColumn>
}

@Serializable
public sealed interface GeomVertexColumn {
    public val name: ObjPointer<InternalName>
    public val numComponents: UByte
    public val numericType: UByte
    public val vertexType: UByte
    public val start: UShort
    public val columnAlignment: UByte?
}

@Serializable
public sealed interface InternalName : PandaObject {
    public val name: String
}


@Serializable
public sealed interface GeomVertexAnimationSpec {
    public val animationType: UByte
    public val numTransforms: UShort
    public val indexedTransforms: Boolean
}

@Serializable
public sealed interface CharacterJoint : MovingPartMatrix {
    public val character: ObjPointer<Character>
    public val netTransformNodes: ObjList<PandaNode>
    public val localTransformNodes: ObjList<PandaNode>
    public val initialNetTransformInverse: Matrix4f
}

@Serializable
public sealed interface GeomTristrips : GeomPrimitive


@Serializable
public sealed interface RenderState : PandaObject {
    public val attribs: List<RenderAttribEntry>
}

@Serializable
public sealed interface RenderAttrib : PandaObject

@Serializable
public sealed interface RenderAttribEntry {
    public val attrib: ObjPointer<RenderAttrib>
    public val override: Int
}

@Serializable
public sealed interface RenderEffects : PandaObject {
    public val effects: ObjList<RenderEffect>
}

@Serializable
public sealed interface RenderEffect : PandaObject

@Serializable
public sealed interface CharacterJointEffect : RenderEffect {
    public val character: ObjPointer<Character>
}

@Serializable
public sealed interface TextureAttrib : RenderAttrib {
    public val offAllStages: Boolean
    public val offStages: ObjList<TextureStage>
    public val onStages: List<OnTextureStage>
}

@Serializable
public sealed interface TextureStage : PandaObject {
    public val name: String
    public val sort: Int
    public val priority: Int
    public val textureCoordName: ObjPointer<InternalName>?
    public val mode: UByte
    public val color: Color
    public val rgbScale: UByte
    public val alphaScale: UByte
    public val savedResult: Boolean
    public val textureViewOffset: Int?
    public val combineRgbMode: UByte
    public val combineRgbOperands: UByte
    public val combineRgbSource0: UByte
    public val combineRgbOperand0: UByte
    public val combineRgbSource1: UByte
    public val combineRgbOperand1: UByte
    public val combineRgbSource2: UByte
    public val combineRgbOperand2: UByte

    public val combineAlphaMode: UByte
    public val numCombineAlphaOperands: UByte
    public val combineAlphaSource0: UByte
    public val combineAlphaOperand0: UByte
    public val combineAlphaSource1: UByte
    public val combineAlphaOperand1: UByte
    public val combineAlphaSource2: UByte
    public val combineAlphaOperand2: UByte
}

@Serializable
public sealed interface Color {
    public val colorVec: Vector4f
}

@Serializable
public sealed interface OnTextureStage {
    public val stage: ObjPointer<TextureStage>
    public val texture: ObjPointer<Texture>
    public val implicitSort: UShort
    public val override: Int?
    public val samplerState: SamplerState?
}

@Serializable
public sealed interface SamplerState {
    public val wrapU: UByte
    public val wrapV: UByte
    public val wrapW: UByte
    public val minFilter: UByte
    public val magFilter: UByte
    public val anisotropicDegree: Short
    public val borderColor: Color
    public val minLod: Float
    public val maxLod: Float
    public val lodBias: Float
}

@Serializable
public sealed interface Texture : PandaObject {
    public val name: String
    public val fileName: String
    public val alphaFileName: String
    public val primaryFileNumChannels: UByte
    public val alphaFileChannel: UByte
    public val textureType: UByte
    public val hasReadMipmaps: Boolean?
    public val wrapU: UByte
    public val wrapV: UByte
    public val wrapW: UByte
    public val minFilter: UByte
    public val magFilter: UByte
    public val anisotropicDegree: Short
    public val borderColor: Color
    public val minLod: Float?
    public val maxLod: Float?
    public val lodBias: Float?
    public val compression: UByte
    public val qualityLevel: UByte
    public val format: UByte
    public val numComponents: UByte
    public val usageHint: UByte?
    public val autoTextureScale: UByte?
    public val originalFileXSize: UInt
    public val originalFileYSize: UInt
    public val simpleRamImage: SimpleRawImage?
    public val clearColor: Color?
    public val xSize: UInt?
    public val ySize: UInt?
    public val zSize: UInt?
    public val padXSize: UInt?
    public val padYSize: UInt?
    public val padZSize: UInt?
    public val numViews: UInt?
    public val componentType: UByte?
    public val componentWidth: UByte?
    public val rawImageCompression: UByte?
    public val ramImages: List<RamImage>?
}

@Serializable
public sealed interface RamImage {
    public val pageSize: UInt
    public val data: UByteArray
}

@Serializable
public sealed interface SimpleRawImage {
    public val xSize: UInt
    public val ySize: UInt
    public val dateGenerated: Int
    public val data: UByteArray
}

@Serializable
public sealed interface TransparencyAttrib : RenderAttrib {
    public val mode: Byte
}

@Serializable
public sealed interface ColorAttrib : RenderAttrib {
    public val attribType: Byte
    public val color: Color
}

@Serializable
public sealed interface AnimBundleNode : PandaNode {
    public val bundle: ObjPointer<AnimBundle>
}

@Serializable
public sealed interface AnimBundle : AnimGroup {
    public val fps: Float
    public val numFrames: UShort
}

@Serializable
public sealed interface AnimChannel<T> : AnimChannelBase

@Serializable
public sealed interface AnimChannelMatrix : AnimChannel<Matrix4f>

@Serializable
public sealed interface AnimChannelMatrixXfmTable : AnimChannelMatrix {
    public val compressedChannels: Boolean
    public val newHPRConvention: Boolean
    public val data: List<List<Float>>
}

@Serializable
public sealed interface ParametricCurve : PandaNode {
    public val curveType: Byte
    public val numDimensions: Byte
}

@Serializable
public sealed interface PiecewiseCurve : ParametricCurve {
    public val segments: List<CurveSegment>
}

@Serializable
public sealed interface CurveSegment {
    public val curve: ObjPointer<ParametricCurve>
    public val tend: Double
}

@Serializable
public sealed interface NurbsCurve : PiecewiseCurve {
    public val order: Byte
    public val controlVertices: List<ControlVertex>
}

@Serializable
public sealed interface ControlVertex {
    public val vertex: Vector4f
    public val weight: Double
}

@Serializable
public sealed interface CubicCurveseg : ParametricCurve {
    public val xBasis: Vector4f
    public val yBasis: Vector4f
    public val zBasis: Vector4f
    public val wBasis: Vector4f
    public val rational: Boolean
}

@Serializable
public sealed interface CollisionNode : PandaNode {
    public val solids: ObjList<CollisionSolid>
    public val fromCollideMask: UInt
}

@Serializable
public sealed interface CollisionSolid : PandaObject {
    public val flags: UByte
    public val effectiveNormal: Vector3f?
}

@Serializable
public sealed interface CollisionPlane : CollisionSolid {
    public val plane: Vector4f
}

@Serializable
public sealed interface CollisionPolygon : CollisionPlane {
    public val points: List<PointDef>
    public val to2DMatrix: Matrix4f
}

@Serializable
public sealed interface PointDef {
    public val point: Vector2f
    public val normalized: Vector2f
}

@Serializable
public sealed interface CollisionSphere : CollisionSolid {
    public val center: Vector3f
    public val radius: Float
}

@Serializable
public sealed interface CullBinAttrib : RenderAttrib {
    public val binName: String
    public val drawOrder: Int
}

@Serializable
public sealed interface DepthWriteAttrib : RenderAttrib {
    public val mode: Byte
}

@Serializable
public sealed interface CullFaceAttrib : RenderAttrib {
    public val mode: Byte
    public val reverse: Boolean
}

@Serializable
public sealed interface BillboardEffect : RenderEffect {
    public val off: Boolean
    public val upVector: Vector3f
    public val eyeRelative: Boolean
    public val axialRotate: Boolean
    public val offset: Float
    public val lookAtPoint: Vector3f
    public val lookAt: NodePath?
    public val fixedDepth: Boolean?
}

@Serializable
public sealed interface DecalEffect : RenderEffect

@Serializable
public sealed interface LODNode : PandaNode {
    public val center: Vector3f
    public val switches: List<SwitchVector>
}

@Serializable
public sealed interface SwitchVector {
    public val `in`: Float
    public val out: Float
}

public typealias CollisionTube = CollisionCapsule

@Serializable
public sealed interface CollisionCapsule : CollisionSolid {
    public val a: Vector3f
    public val b: Vector3f
    public val radius: Float
}

@Serializable
public sealed interface AnimInterface {
    public val numFrames: Int
    public val frameRate: Float
    public val playMode: UByte
    public val startTime: Float
    public val startFrame: Float
    public val playFrames: Float
    public val fromFrame: Int
    public val toFrame: Int
    public val playRate: Float
    public val paused: Boolean
    public val pausedF: Float
}

@Serializable
public sealed interface SequenceNode : PandaNode, AnimInterface

@Serializable
public sealed interface UserVertexTransform : VertexTransform {
    public val matrix: Matrix4f
}

@Serializable
public sealed interface GeomPoints : GeomPrimitive

@Serializable
public sealed interface SheetNode : PandaNode {
    public val nullObj: ObjPointer<PandaObject>?
}

@Serializable
public sealed interface UvScrollNode : PandaNode {
    public val uSpeed: Float
    public val vSpeed: Float
    public val wSpeed: Float?
    public val rSpeed: Float?

}

@Serializable
public sealed interface NodePath {
    public val nodes: ObjList<PandaNode>
}