package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.block.state.BlockStateHelper;
import com.github.lunatrius.schematica.client.renderer.RenderSchematic;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

public class OverlayHandler {
    private final Minecraft minecraft = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onText(final RenderGameOverlayEvent.Text event) {
        if (this.minecraft.gameSettings.showDebugInfo && ConfigurationHandler.showDebugInfo) {
            final SchematicWorld schematic = ClientProxy.schematic;
            if (schematic != null && schematic.isRendering) {
                final ArrayList<String> left = event.getLeft();
                final ArrayList<String> right = event.getRight();

                left.add("");
                left.add("[§6Schematica§r] " + schematic.getDebugDimensions());
                // event.left.add("[§6Schematica§r] " + RenderSchematic.INSTANCE.getDebugInfoEntities());
                left.add("[§6Schematica§r] " + RenderSchematic.INSTANCE.getDebugInfoTileEntities());
                left.add("[§6Schematica§r] " + RenderSchematic.INSTANCE.getDebugInfoRenders());

                final RayTraceResult rtr = ClientProxy.objectMouseOver;
                if (rtr != null && rtr.typeOfHit == RayTraceResult.Type.BLOCK) {
                    final BlockPos pos = rtr.getBlockPos();
                    final IBlockState blockState = schematic.getBlockState(pos);

                    right.add("");
                    right.add(String.valueOf(Block.REGISTRY.getNameForObject(blockState.getBlock())) + " [§6S§r]");

                    for (final String formattedProperty : BlockStateHelper.getFormattedProperties(blockState)) {
                        right.add(formattedProperty + " [§6S§r]");
                    }
                }
            }
        }
    }
}
