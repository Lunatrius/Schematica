package com.github.lunatrius.schematica.client.renderer;

import com.github.lunatrius.core.util.vector.Vector3f;
import com.github.lunatrius.schematica.Settings;

import java.util.Comparator;

public class RendererSchematicChunkSorter implements Comparator {
	private final Settings settings = Settings.instance;

	public int doCompare(RendererSchematicChunk rendererSchematicChunk1, RendererSchematicChunk rendererSchematicChunk2) {
		if (rendererSchematicChunk1.isInFrustrum && !rendererSchematicChunk2.isInFrustrum) {
			return -1;
		} else if (!rendererSchematicChunk1.isInFrustrum && rendererSchematicChunk2.isInFrustrum) {
			return 1;
		} else {
			Vector3f position = this.settings.playerPosition.clone().sub(this.settings.offset);
			double dist1 = rendererSchematicChunk1.distanceToPoint(position);
			double dist2 = rendererSchematicChunk2.distanceToPoint(position);
			return dist1 > dist2 ? 1 : (dist1 < dist2 ? -1 : 0);
		}
	}

	@Override
	public int compare(Object obj1, Object obj2) {
		return doCompare((RendererSchematicChunk) obj1, (RendererSchematicChunk) obj2);
	}
}
