package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.block.state.BlockStateHelper;
import com.github.lunatrius.schematica.client.renderer.RenderSchematic;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;

public class OverlayHandler {
    private static final FMLControlledNamespacedRegistry<Block> BLOCK_REGISTRY = GameData.getBlockRegistry();
    private final Minecraft minecraft = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onText(RenderGameOverlayEvent.Text event) {
        if (this.minecraft.gameSettings.showDebugInfo && ConfigurationHandler.showDebugInfo) {
            final SchematicWorld schematic = ClientProxy.schematic;
            if (schematic != null && schematic.isRendering) {
                event.left.add("");
                event.left.add("[§6Schematica§r] " + schematic.getDebugDimensions());
                // event.left.add("[§6Schematica§r] " + RenderSchematic.INSTANCE.getDebugInfoEntities());
                event.left.add("[§6Schematica§r] " + RenderSchematic.INSTANCE.getDebugInfoTileEntities());
                event.left.add("[§6Schematica§r] " + RenderSchematic.INSTANCE.getDebugInfoRenders());

                final MovingObjectPosition mop = ClientProxy.movingObjectPosition;
                if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    final BlockPos pos = mop.getBlockPos();
                    final IBlockState blockState = schematic.getBlockState(pos);

                    event.right.add("");
                    event.right.add(String.valueOf(BLOCK_REGISTRY.getNameForObject(blockState.getBlock())) + " [§6S§r]");

                    for (final String formattedProperty : BlockStateHelper.getFormattedProperties(blockState)) {
                        event.right.add(formattedProperty + " [§6S§r]");
                    }
                }
            }
        }
    }
}
