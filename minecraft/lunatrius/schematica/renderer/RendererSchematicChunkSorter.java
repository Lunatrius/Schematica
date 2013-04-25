package lunatrius.schematica.renderer;

import lunatrius.schematica.Settings;
import lunatrius.schematica.util.Vector3f;

import java.util.Comparator;

public class RendererSchematicChunkSorter implements Comparator {
	private final Settings settings = Settings.instance();

	public int doCompare(RendererSchematicChunk par1RendererSchematicChunk, RendererSchematicChunk par2RendererSchematicChunk) {
		if (par1RendererSchematicChunk.isInFrustrum && !par2RendererSchematicChunk.isInFrustrum) {
			return -1;
		} else if (!par1RendererSchematicChunk.isInFrustrum && par2RendererSchematicChunk.isInFrustrum) {
			return 1;
		} else {
			Vector3f position = this.settings.playerPosition.clone().sub(this.settings.offset.x, this.settings.offset.y, this.settings.offset.z);
			double dist1 = par1RendererSchematicChunk.distanceToPoint(position);
			double dist2 = par2RendererSchematicChunk.distanceToPoint(position);
			return dist1 > dist2 ? 1 : (dist1 < dist2 ? -1 : 0);
		}
	}

	@Override
	public int compare(Object par1Obj, Object par2Obj) {
		return doCompare((RendererSchematicChunk) par1Obj, (RendererSchematicChunk) par2Obj);
	}
}
