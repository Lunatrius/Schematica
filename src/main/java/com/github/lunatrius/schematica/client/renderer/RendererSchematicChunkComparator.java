package com.github.lunatrius.schematica.client.renderer;

import com.github.lunatrius.core.util.vector.Vector3f;
import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.schematica.proxy.ClientProxy;

import java.util.Comparator;

public class RendererSchematicChunkComparator implements Comparator<RendererSchematicChunk> {
    private final Vector3f position = new Vector3f();
    private final Vector3f schematicPosition = new Vector3f();

    @Override
    public int compare(RendererSchematicChunk rendererSchematicChunk1, RendererSchematicChunk rendererSchematicChunk2) {
        if (rendererSchematicChunk1.isInFrustrum && !rendererSchematicChunk2.isInFrustrum) {
            return -1;
        } else if (!rendererSchematicChunk1.isInFrustrum && rendererSchematicChunk2.isInFrustrum) {
            return 1;
        } else {
            final float dist1 = this.position.lengthSquaredTo(rendererSchematicChunk1.centerPosition);
            final float dist2 = this.position.lengthSquaredTo(rendererSchematicChunk2.centerPosition);
            return dist1 > dist2 ? 1 : (dist1 < dist2 ? -1 : 0);
        }
    }

    public void setPosition(Vector3i position) {
        this.position.set(ClientProxy.playerPosition).sub(position.toVector3f(this.schematicPosition));
    }
}
