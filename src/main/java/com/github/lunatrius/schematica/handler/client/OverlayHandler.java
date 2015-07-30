package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.world.SchematicWorld;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class OverlayHandler {
    private static final FMLControlledNamespacedRegistry<Block> BLOCK_REGISTRY = GameData.getBlockRegistry();
    private final Minecraft minecraft = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onText(RenderGameOverlayEvent.Text event) {
        if (this.minecraft.gameSettings.showDebugInfo && ConfigurationHandler.showDebugInfo) {
            final SchematicWorld schematic = Schematica.proxy.getActiveSchematic();
            if (schematic != null && schematic.isRendering) {
                event.left.add("");
                event.left.add("[§6Schematica§r] " + schematic.getDebugDimensions());

                final MovingObjectPosition mop = ClientProxy.movingObjectPosition;
                if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    final Block block = schematic.getBlock(mop.blockX, mop.blockY, mop.blockZ);
                    final int metadata = schematic.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ);

                    event.right.add("");
                    event.right.add(BLOCK_REGISTRY.getNameForObject(block) + " : " + metadata + " [§6S§r]");
                }
            }
        }
    }
}
