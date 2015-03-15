package com.github.lunatrius.schematica.client.renderer;

import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public class SchematicBlockRendererDispatcher extends BlockRendererDispatcher {
    private final Minecraft minecraft = Minecraft.getMinecraft();

    public SchematicBlockRendererDispatcher(final BlockModelShapes blockModelShapes, final GameSettings gameSettings) {
        super(blockModelShapes, gameSettings);
    }

    @Override
    public boolean renderBlock(final IBlockState blockState, final BlockPos pos, final IBlockAccess blockAccess, final WorldRenderer worldRenderer) {
        final SchematicWorld schematic = ClientProxy.schematic;
        final BlockPos realPos = new BlockPos(pos.getX() + schematic.position.x, pos.getY() + schematic.position.y, pos.getZ() + schematic.position.z);
        final WorldClient world = this.minecraft.theWorld;

        return world.isAirBlock(realPos) && super.renderBlock(blockState, pos, blockAccess, worldRenderer);
    }
}
