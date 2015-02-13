package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.client.renderer.RenderSchematic;
import com.github.lunatrius.schematica.world.SchematicWorld;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class OverlayHandler {
    private final Minecraft minecraft = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onText(RenderGameOverlayEvent.Text event) {
        if (this.minecraft.gameSettings.showDebugInfo) {
            final SchematicWorld schematic = Schematica.proxy.getActiveSchematic();
            if (schematic != null) {
                event.left.add("");
                event.left.add("[§6Schematica§r] " + schematic.getDebugDimensions());
                // event.left.add("[§6Schematica§r] " + RenderSchematic.INSTANCE.getDebugInfoEntities());
                event.left.add("[§6Schematica§r] " + RenderSchematic.INSTANCE.getDebugInfoTileEntities());
                event.left.add("[§6Schematica§r] " + RenderSchematic.INSTANCE.getDebugInfoRenders());
            }
        }
    }
}
