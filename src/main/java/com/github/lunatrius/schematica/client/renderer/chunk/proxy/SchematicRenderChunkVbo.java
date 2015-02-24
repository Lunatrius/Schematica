package com.github.lunatrius.schematica.client.renderer.chunk.proxy;

import com.github.lunatrius.schematica.proxy.ClientProxy;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SchematicRenderChunkVbo extends RenderChunk {
    public SchematicRenderChunkVbo(final World world, final RenderGlobal renderGlobal, final BlockPos pos, final int index) {
        super(world, renderGlobal, pos, index);
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
