package com.github.lunatrius.schematica.client.printer.nbtsync;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.util.HashMap;

public class SyncRegistry {
    public static final SyncRegistry INSTANCE = new SyncRegistry();

    private HashMap<Block, NBTSync> map = new HashMap<Block, NBTSync>();

    public void register(final Block block, final NBTSync handler) {
        if (block == null || handler == null) {
            return;
        }

        this.map.put(block, handler);
    }

    public NBTSync getHandler(final Block block) {
        return this.map.get(block);
    }

    static {
        INSTANCE.register(Blocks.command_block, new NBTSyncCommandBlock());
        INSTANCE.register(Blocks.standing_sign, new NBTSyncSign());
        INSTANCE.register(Blocks.wall_sign, new NBTSyncSign());
    }
}
