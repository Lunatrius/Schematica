package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.client.renderer.RenderSchematic;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WorldHandler {
    @SubscribeEvent
    public void onLoad(final WorldEvent.Load event) {
        if (event.world.isRemote) {
            RenderSchematic.INSTANCE.setWorldAndLoadRenderers(ClientProxy.schematic);
            event.world.addWorldAccess(RenderSchematic.INSTANCE);
        }
    }

    @SubscribeEvent
    public void onUnload(final WorldEvent.Unload event) {
        if (event.world.isRemote) {
            event.world.removeWorldAccess(RenderSchematic.INSTANCE);
        }
    }
}
