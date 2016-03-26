package com.github.lunatrius.schematica.api.event;

import com.github.lunatrius.schematica.api.ISchematic;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Map;

/**
 * This event is fired after the schematic has been Captured, but before it is serialized to the schematic format.
 * This is your opportunity to add Metadata.
 * Register to this event using MinecraftForge.EVENT_BUS
 */
public class PreSchematicSaveEvent extends Event {
    private final Map<String, Short> mappings;

    /**
     * The schematic that will be saved.
     */
    public final ISchematic schematic;

    /**
     * The Extended Metadata tag compound provides a facility to add custom metadata to the schematic.
     */
    public final NBTTagCompound extendedMetadata;

    @Deprecated
    public PreSchematicSaveEvent(final Map<String, Short> mappings) {
        this(null, mappings);
    }

    public PreSchematicSaveEvent(final ISchematic schematic, final Map<String, Short> mappings) {
        this.schematic = schematic;
        this.mappings = mappings;
        this.extendedMetadata = new NBTTagCompound();
    }

    /**
     * Replaces the block mapping from one name to another. Use this method with care as it is possible that the schematic
     * will not be usable or will have blocks missing if you use an invalid value.
     *
     * Attempting to remap two blocks to the same name will result in a DuplicateMappingException. If you wish for this
     * type of collision, you can work around it by merging the two sets of block into a single BlockType in the
     * PostSchematicCaptureEvent.
     * @param oldName The old name of the block mapping.
     * @param newName The new name of the block Mapping.
     * @return true if a mapping was replaced.
     * @throws DuplicateMappingException
     */
    public boolean replaceMapping(final String oldName, final String newName) throws DuplicateMappingException {
        if (this.mappings.containsKey(newName)) {
            throw new DuplicateMappingException(
                    String.format(
                            "Could not replace block type %s, the block type %s already exists in the schematic.",
                            oldName, newName
                    )
            );
        }

        final Short id = this.mappings.get(oldName);
        if (id != null) {
            this.mappings.remove(oldName);
            this.mappings.put(newName, id);
            return true;
        }

        return false;
    }
}
