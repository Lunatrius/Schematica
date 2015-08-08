package com.github.lunatrius.schematica.client.world;

import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.schematica.client.renderer.RendererSchematicChunk;
import com.github.lunatrius.schematica.client.renderer.RendererSchematicGlobal;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IWorldAccess;

public class SchematicUpdater implements IWorldAccess {
    public static final SchematicUpdater INSTANCE = new SchematicUpdater();

    @Override
    public void markBlockForUpdate(final int x, final int y, final int z) {
        markBlocksForUpdate(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    }

    @Override
    public void markBlockForRenderUpdate(final int x, final int y, final int z) {
        markBlocksForUpdate(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    }

    @Override
    public void markBlockRangeForRenderUpdate(final int x0, final int y0, final int z0, final int x1, final int y1, final int z1) {
        markBlocksForUpdate(x0 - 1, y0 - 1, z0 - 1, x1 + 1, y1 + 1, z1 + 1);
    }

    private void markBlocksForUpdate(final int x0, final int y0, final int z0, final int x1, final int y1, final int z1) {
        final SchematicWorld schematic = ClientProxy.schematic;
        if (schematic == null) {
            return;
        }

        final Vector3i position = schematic.position;
        final AxisAlignedBB boundingBox = AxisAlignedBB.getBoundingBox(x0 - position.x, y0 - position.y, z0 - position.z, x1 - position.x, y1 - position.y, z1 - position.z);
        for (final RendererSchematicChunk renderer : RendererSchematicGlobal.INSTANCE.sortedRendererSchematicChunk) {
            if (!renderer.getDirty() && renderer.getBoundingBox().intersectsWith(boundingBox)) {
                renderer.setDirty();
            }
        }
    }

    @Override
    public void playSound(final String soundName, final double x, final double y, final double z, final float volume, final float pitch) {
    }

    @Override
    public void playSoundToNearExcept(final EntityPlayer player, final String soundName, final double x, final double y, final double z, final float volume, final float pitch) {
    }

    @Override
    public void spawnParticle(final String type, final double x, final double y, final double z, final double velX, final double velY, final double velZ) {
    }

    @Override
    public void onEntityCreate(final Entity entity) {
    }

    @Override
    public void onEntityDestroy(final Entity entity) {
    }

    @Override
    public void playRecord(final String recordName, final int x, final int y, final int z) {
    }

    @Override
    public void broadcastSound(final int id, final int x, final int y, final int z, final int par5) {
    }

    @Override
    public void playAuxSFX(final EntityPlayer player, final int id, final int x, final int y, final int z, final int par6) {
    }

    @Override
    public void destroyBlockPartially(final int id, final int x, final int y, final int z, final int partialDamage) {
    }

    @Override
    public void onStaticEntitiesChanged() {
    }
}
