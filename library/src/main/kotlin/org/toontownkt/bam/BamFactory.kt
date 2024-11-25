package org.toontownkt.bam

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.toontownkt.bam.types.*
import java.nio.ByteBuffer

public interface BamFactoryScope {
    public val buf: ByteBuffer
    public val bam: RawBamFile

    public val bamMajorVersion: UInt
        get() = bam.majorVersion.toUInt()
    public val bamMinorVersion: UInt
        get() = bam.minorVersion.toUInt()

    public fun getU8(): UByte = buf.getU8()
    public fun getI8(): Byte = buf.getI8()
    public fun getU16(): UShort = buf.getU16()
    public fun getI16(): Short = buf.getI16()
    public fun getU32(): UInt = buf.getU32()
    public fun getI32(): Int = buf.getI32()
    public fun getU64(): ULong = buf.getU64()
    public fun getI64(): Long = buf.getI64()

    public fun getF32(): Float = buf.getFloat()
    public fun getF64(): Double = buf.getDouble()

    public fun getBool(): Boolean = buf.getBool()

    public fun getLengthPrefixedString(): String =
        buf.getLengthPrefixedString()

    public fun getVec2f(): Vector2f = buf.getVec2f()
    public fun getVec3f(): Vector3f = buf.getVec3f()
    public fun getVec4f(): Vector4f = buf.getVec4f()
    public fun getMatrix4f(): Matrix4f = buf.getMatrix4f()
    public fun getColor(): Color = buf.getColor()
}

public inline fun <reified T : PandaObject> BamFactoryScope.getObjPointer(): ObjPointer<T> = buf.getObjPointer()
public inline fun <reified T : PandaObject> BamFactoryScope.getObjPointerOrNull(): ObjPointer<T>? =
    buf.getObjPointerOrNull()

public inline fun <reified T : PandaObject> BamFactoryScope.getObjPointerList(): ObjList<T> = buf.getObjPointerList()

public object BamFactory {
    private val types = mutableMapOf<String, BamFactoryScope.() -> PandaObject>()

    private fun registerType(name: String, builder: BamFactoryScope.() -> PandaObject): Unit = types.set(name, builder)

    public fun buildType(bamFile: RawBamFile, buf: ByteBuffer, name: String): PandaObject {
        val obj = types[name]?.invoke(object : BamFactoryScope {
            override val bam: RawBamFile = bamFile
            override val buf: ByteBuffer = buf
        }) ?: error("don't have factory for $name")

        //TODO: enable assert
        //assert(!buf.hasRemaining()) {
        //    "didn't complete consume the buffer for $name"
        //}

        return obj
    }

    public val supportedClasses: Set<String>
        get() = types.keys

    init {
        registerType("PandaNode") {
            getPandaNode()
        }

        registerType("GeomNode") {
            getGeomNode()
        }
        registerType("Geom") {
            getGeom()
        }

        registerType("GeomVertexData") {
            getGeomVertexData()
        }

        registerType("GeomVertexFormat") {
            getGeomVertexFormat()
        }

        registerType("GeomVertexArrayData") {
            getGeomVertexArrayData()
        }

        registerType("GeomVertexArrayFormat") {
            getGeomVertexArrayFormat()
        }

        registerType("InternalName") {
            getInternalName()
        }

        registerType("GeomTristrips") {
            getGeomTristrips()
        }

        registerType("RenderState") {
            getRenderState()
        }

        registerType("TransformState") {
            getTransformState()
        }

        registerType("RenderEffects") {
            getRenderEffects()
        }

        registerType("TextureStage") {
            getTextureStage()
        }

        registerType("TextureAttrib") {
            getTextureAttrib()
        }

        registerType("Texture") {
            getTexture()
        }

        registerType("TransparencyAttrib") {
            getTransparenyAttrib()
        }

        registerType("Character") {
            getCharacter()
        }

        registerType("TransformBlendTable") {
            getTransformBlendTable()
        }

        registerType("JointVertexTransform") {
            getJointVertexTransform()
        }

        registerType("CharacterJoint") {
            getCharacterJoint()
        }

        registerType("ModelRoot") {
            getModelRoot()
        }

        registerType("ModelNode") {
            getModelNode()
        }

        registerType("CharacterJointEffect") {
            getCharacterJointEffect()
        }

        registerType("GeomTriangles") {
            getGeomTriangles()
        }

        registerType("ColorAttrib") {
            getColorAttrib()
        }

        registerType("CharacterJointBundle") {
            getCharacterJointBundle()
        }

        registerType("PartGroup") {
            getPartGroup()
        }

        registerType("AnimBundle") {
            getAnimBundle()
        }

        registerType("AnimBundleNode") {
            getAnimBundleNode()
        }

        registerType("AnimGroup") {
            getAnimGroup()
        }

        registerType("AnimChannelMatrixXfmTable") {
            getAnimChannelMatrixXfmTable()
        }

        registerType("NurbsCurve") {
            getNurbsCurve()
        }

        registerType("CubicCurveseg") {
            getCubicCurveseg()
        }

        registerType("CollisionNode") {
            getCollisionNode()
        }

        registerType("CollisionSolid") {
            getCollisionSolid()
        }

        registerType("CollisionPolygon") {
            getCollisionPolygon()
        }

        registerType("CollisionSphere") {
            getCollisionSphere()
        }

        registerType("CullBinAttrib") {
            getCullBinAttrib()
        }

        registerType("DepthWriteAttrib") {
            getDepthWriteAttrib()
        }

        registerType("CullFaceAttrib") {
            getCullFaceAttrib()
        }

        registerType("BillboardEffect") {
            getBillboardEffect()
        }

        registerType("DecalEffect") {
            getDecalEffect()
        }

        registerType("LODNode") {
            getLODNode()
        }

        registerType("CollisionTube") {
            getCollisionCapsule()
        }

        registerType("CollisionCapsule") {
            getCollisionCapsule()
        }

        registerType("SequenceNode") {
            getSequenceNode()
        }

        registerType("UserVertexTransform") {
            getUserVertexTransform()
        }

        registerType("GeomPoints") {
            getGeomPoints()
        }

        registerType("SheetNode") {
            getSheetNode()
        }

        registerType("UvScrollNode") {
            getUvScrollNode()
        }
    }

}

public inline fun <reified T : PandaObject> RawBamFile.instanceBamType(id: UShort): T =
    BamFactory.buildType(
        this,
        objectMap[id]?.data?.wrapToLEByteBuffer() ?: error("missing object $id"),
        typeHandles[objectMap[id]!!.handle]?.name ?: error("missing type handle ${objectMap[id]!!.handle}")
    ) as T

public inline fun <reified T : PandaObject> RawBamFile.instanceBamTypeOrNull(id: UShort): T? =
    if (id == 0U.toUShort()) {
        null
    } else {
        BamFactory.buildType(
            this,
            objectMap[id]?.data?.wrapToLEByteBuffer() ?: error("missing object $id"),
            typeHandles[objectMap[id]!!.handle]?.name ?: error("missing type handle ${objectMap[id]!!.handle}")
        ) as T
    }
