package com.github.lunatrius.schematica.client.renderer;

import com.github.lunatrius.core.util.vector.Vector3f;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.world.SchematicWorld;

import java.util.Comparator;

public class RendererSchematicChunkComparator implements Comparator<RendererSchematicChunk> {
    private final Vector3f position = new Vector3f();
    private final Vector3f schematicPosition = new Vector3f();
    private SchematicWorld schematic = null;

    @Override
    public int compare(RendererSchematicChunk rendererSchematicChunk1, RendererSchematicChunk rendererSchematicChunk2) {
        if (rendererSchematicChunk1.isInFrustrum && !rendererSchematicChunk2.isInFrustrum) {
            return -1;
        } else if (!rendererSchematicChunk1.isInFrustrum && rendererSchematicChunk2.isInFrustrum) {
            return 1;
        } else {
            this.position.set(ClientProxy.playerPosition).sub(this.schematicPosition);
            final float dist1 = this.position.lengthSquaredTo(rendererSchematicChunk1.centerPosition);
            final float dist2 = this.position.lengthSquaredTo(rendererSchematicChunk2.centerPosition);
            return dist1 > dist2 ? 1 : (dist1 < dist2 ? -1 : 0);
        }
    }

    public void setSchematic(SchematicWorld schematic) {
        this.schematic = schematic;
        this.schematicPosition.set(schematic.position.toVector3f());
    }
}
