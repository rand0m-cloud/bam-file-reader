package org.toontownkt.bam

import kotlinx.serialization.Contextual
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector4f
import org.toontownkt.bam.types.ObjPointer
import org.toontownkt.bam.types.PandaNode
import org.toontownkt.bam.types.TransformState

public data class NodePath(val paths: List<String>) {
    public fun child(child: String): NodePath = NodePath(paths + child)

    override fun toString(): String {
        val path = paths.joinToString("/") {
            it.ifBlank {
                "\"\""
            }
        }

        return "/$path"
    }

    public companion object {
        public val ROOT: NodePath = NodePath(listOf())
    }
}

public data class NodeTree(
    val node: ObjPointer<PandaNode>,
    val name: String,
    val children: List<NodeTree>
) {
    public fun visitBreadthFirst(visitor: (NodeTree) -> Unit) {
        visitor(this)
        children.forEach { visitor(it) }
    }

    public fun visitDepthFirst(visitor: (NodeTree) -> Unit) {
        children.forEach { it.visitDepthFirst(visitor) }
        visitor(this)
    }
}

public fun PandaNode.visit(
    bamFile: BamFile,
    bamPath: NodePath = NodePath.ROOT,
    visitor: (NodePath, PandaNode) -> Unit
) {
    visitor(bamPath, this)
    children.forEach {
        val child = bamFile[it]
        child.visit(bamFile, bamPath.child(child.name), visitor)
    }
}

public fun BamFile.toNodeTree(node: ObjPointer<PandaNode> = ObjPointer(1U)): NodeTree {
    return NodeTree(node, this[node].name, this[node].children.map { toNodeTree(it) })
}


public fun TransformState.toMatrix4f(): Matrix4f {
    return when (this) {
        is TransformState.Components -> {
            assert(shear == Vector3f(0f))
            if (hpr != null && quat != null) {
                Matrix4f().translationRotateScale(pos, Quaternionf(quat.x, quat.y, quat.z, quat.w), scale)
            } else {
                Matrix4f().translate(pos).scale(scale)
            }

        }

        is TransformState.Matrix -> {
            matrix
        }
    }
}