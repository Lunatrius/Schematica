package com.github.lunatrius.schematica.api.event;

import com.github.lunatrius.schematica.api.ISchematic;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * This event is fired after an ISchematic has been created out of a part of the world.
 * This is an appropriate place to modify the schematic's blocks, metadata and tile entities before they are persisted.
 * Register to this event using MinecraftForge.EVENT_BUS
 */
public class PostSchematicCaptureEvent extends Event {
    private final ISchematic schematic;

    public PostSchematicCaptureEvent(ISchematic schematic) {
        this.schematic = schematic;
    }

    /**
     * The collected schematic
     * @return the schematic that was just generated.
     */
    public ISchematic getSchematic() {
        return schematic;
    }
}
