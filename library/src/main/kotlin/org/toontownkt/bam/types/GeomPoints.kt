package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("GeomPoints")
public data class GeomPointsImpl(val geomPrimitive: GeomPrimitive) : GeomPoints, GeomPrimitive by geomPrimitive

public fun BamFactoryScope.getGeomPoints(): GeomPoints = GeomPointsImpl(getGeomPrimitive())