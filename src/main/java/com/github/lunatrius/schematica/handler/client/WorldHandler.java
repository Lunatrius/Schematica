package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.client.world.SchematicUpdater;
import com.github.lunatrius.schematica.reference.Reference;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;

public class WorldHandler {
    @SubscribeEvent
    public void onLoad(final WorldEvent.Load event) {
        if (event.world.isRemote) {
            addWorldAccess(event.world, SchematicUpdater.INSTANCE);
        }
    }

    @SubscribeEvent
    public void onUnload(final WorldEvent.Unload event) {
        if (event.world.isRemote) {
            removeWorldAccess(event.world, SchematicUpdater.INSTANCE);
        }
    }

    public static void addWorldAccess(final World world, final IWorldAccess schematic) {
        if (world != null && schematic != null) {
            Reference.logger.debug("Adding world access to {}", world);
            world.addWorldAccess(schematic);
        }
    }

    public static void removeWorldAccess(final World world, final IWorldAccess schematic) {
        if (world != null && schematic != null) {
            Reference.logger.debug("Removing world access from {}", world);
            world.removeWorldAccess(schematic);
        }
    }
}
