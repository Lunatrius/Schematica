package com.github.lunatrius.schematica.client.renderer;

import com.github.lunatrius.core.util.vector.Vector3f;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.world.SchematicWorld;

import java.util.Comparator;

public class RendererSchematicChunkSorter implements Comparator {
	private final Vector3f position = new Vector3f();
	private final Vector3f schematicPosition = new Vector3f();
	private SchematicWorld schematic = null;

	public int doCompare(RendererSchematicChunk rendererSchematicChunk1, RendererSchematicChunk rendererSchematicChunk2) {
		if (rendererSchematicChunk1.isInFrustrum && !rendererSchematicChunk2.isInFrustrum) {
			return -1;
		} else if (!rendererSchematicChunk1.isInFrustrum && rendererSchematicChunk2.isInFrustrum) {
			return 1;
		} else {
			this.position.set(ClientProxy.playerPosition).sub(this.schematicPosition);
			double dist1 = rendererSchematicChunk1.distanceToPoint(this.position);
			double dist2 = rendererSchematicChunk2.distanceToPoint(this.position);
			return dist1 > dist2 ? 1 : (dist1 < dist2 ? -1 : 0);
		}
	}

	@Override
	public int compare(Object obj1, Object obj2) {
		return doCompare((RendererSchematicChunk) obj1, (RendererSchematicChunk) obj2);
	}

	public void setSchematic(SchematicWorld schematic) {
		this.schematic = schematic;
		this.schematicPosition.set(schematic.position.toVector3f());
	}
}
