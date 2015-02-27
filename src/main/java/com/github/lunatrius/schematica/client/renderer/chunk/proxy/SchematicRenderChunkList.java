package com.github.lunatrius.schematica.client.renderer.chunk.proxy;

import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.world.SchematicWorld;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.ListedRenderChunk;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SchematicRenderChunkList extends ListedRenderChunk {
    public SchematicRenderChunkList(final World world, final RenderGlobal renderGlobal, final BlockPos pos, final int index) {
        super(world, renderGlobal, pos, index);
    }

    @Override
    public void rebuildChunk(final float x, final float y, final float z, final ChunkCompileTaskGenerator generator) {
        generator.getLock().lock();

        try {
            if (generator.getStatus() == ChunkCompileTaskGenerator.Status.COMPILING) {
                final BlockPos from = getPosition();
                final SchematicWorld schematic = (SchematicWorld) this.world;

                if (from.getX() < 0 || from.getZ() < 0 || from.getX() >= schematic.getWidth() || from.getZ() >= schematic.getLength()) {
                    final SetVisibility visibility = new SetVisibility();
                    visibility.setAllVisible(true);

                    final CompiledChunk dummy = new CompiledChunk();
                    dummy.setVisibility(visibility);

                    generator.setCompiledChunk(dummy);
                    return;
                }
            }
        } finally {
            generator.getLock().unlock();
        }

        super.rebuildChunk(x, y, z, generator);
    }

    @Override
    public void preRenderBlocks(final WorldRenderer worldRendererIn, final BlockPos pos) {
        ClientProxy.setDispatcherSchematic();

        super.preRenderBlocks(worldRendererIn, pos);
    }

    @Override
    public void postRenderBlocks(final EnumWorldBlockLayer layer, final float x, final float y, final float z, final WorldRenderer worldRenderer, final CompiledChunk compiledChunk) {
        super.postRenderBlocks(layer, x, y, z, worldRenderer, compiledChunk);

        ClientProxy.setDispatcherVanilla();
    }
}
