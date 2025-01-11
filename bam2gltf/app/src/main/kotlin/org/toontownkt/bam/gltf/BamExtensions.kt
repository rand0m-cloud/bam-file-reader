package org.toontownkt.bam.gltf

import org.joml.Math
import org.joml.Quaternionf
import org.joml.Vector3f
import org.toontownkt.bam.BamFile
import org.toontownkt.bam.toNodeTree
import org.toontownkt.bam.types.*

fun AnimChannelMatrixXfmTable.getScale(frame: Int): Vector3f {
    assert(newHPRConvention)
    assert(!compressedChannels)

    val scaleX = data[0]
    val scaleY = data[1]
    val scaleZ = data[2]
    val defaultX = if (data[0].isEmpty()) 1f else null
    val defaultY = if (data[1].isEmpty()) 1f else null
    val defaultZ = if (data[2].isEmpty()) 1f else null

    return Vector3f(
        defaultX ?: scaleX[frame % scaleX.size],
        defaultY ?: scaleY[frame % scaleY.size],
        defaultZ ?: scaleZ[frame % scaleZ.size]
    )
}

fun AnimChannelMatrixXfmTable.getPos(frame: Int): Vector3f {
    assert(newHPRConvention)
    assert(!compressedChannels)

    val posX = data[9]
    val posY = data[10]
    val posZ = data[11]
    val defaultX = if (data[9].isEmpty()) 0f else null
    val defaultY = if (data[10].isEmpty()) 0f else null
    val defaultZ = if (data[11].isEmpty()) 0f else null

    return Vector3f(
        defaultX ?: posX[frame % posX.size],
        defaultY ?: posY[frame % posY.size],
        defaultZ ?: posZ[frame % posZ.size]
    )
}

fun AnimChannelMatrixXfmTable.getShear(frame: Int): Vector3f {
    assert(newHPRConvention)
    assert(!compressedChannels)

    val shearA = data[3]
    val shearB = data[4]
    val shearC = data[5]
    val defaultA = if (data[3].isEmpty()) 0f else null
    val defaultB = if (data[4].isEmpty()) 0f else null
    val defaultC = if (data[5].isEmpty()) 0f else null

    return Vector3f(
        defaultA ?: shearA[frame % shearA.size],
        defaultB ?: shearB[frame % shearB.size],
        defaultC ?: shearC[frame % shearC.size]
    )
}

fun AnimChannelMatrixXfmTable.getQuat(frame: Int): Quaternionf {
    assert(newHPRConvention)
    assert(!compressedChannels)

    val heading = data[6]
    val pitch = data[7]
    val row = data[8]
    val defaultH = if (data[6].isEmpty()) 0f else null
    val defaultP = if (data[7].isEmpty()) 0f else null
    val defaultR = if (data[8].isEmpty()) 0f else null

    val hpr = Vector3f(
        defaultH ?: heading[frame % heading.size],
        defaultP ?: pitch[frame % pitch.size],
        defaultR ?: row[frame % row.size]
    )
    hpr.x = Math.toRadians(hpr.x)
    hpr.y = Math.toRadians(hpr.y)
    hpr.z = Math.toRadians(hpr.z)

    return Quaternionf().rotateZ(hpr.z).rotateY(hpr.y).rotateX(hpr.x)
    //return Quaternionf().rotateZ(hpr.z).rotateX(hpr.y).rotateY(hpr.x)
}

internal fun createChildToParent(bam: BamFile): Map<ObjPointer<PandaNode>, ObjPointer<PandaNode>> {
    val map = mutableMapOf<ObjPointer<PandaNode>, ObjPointer<PandaNode>>()
    val tree = bam.toNodeTree()

    tree.visitBreadthFirst { treeNode ->
        if (treeNode.parent == null) return@visitBreadthFirst
        map[treeNode.node] = treeNode.parent!!
    }

    return map
}

internal fun GeomVertexData.findByteOffset(bam: BamFile, arrayColumn: ArrayColumn): Long {
    val columns = arrays.resolveAll(bam)
        .firstNotNullOf {

            val columns = it.arrayFormat.resolve(bam).columns.toArrayColumns(bam)
            if (columns.contains(arrayColumn)) columns else null
        }
    return columns.take(columns.indexOf(arrayColumn)).sumOf { it.byteLen }.toLong()
}
