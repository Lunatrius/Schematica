package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.schematica.handler.PlayerHandler;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.IOException;

public class ServerProxy extends CommonProxy {
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        FMLCommonHandler.instance().bus().register(PlayerHandler.INSTANCE);
    }

    @Override
    public File getDataDirectory() {
        final File file = MinecraftServer.getServer().getFile(".");
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            Reference.logger.info("Could not canonize path!", e);
        }
        return file;
    }

    @Override
    public boolean loadSchematic(EntityPlayer player, File directory, String filename) {
        return false;
    }

    @Override
    public void setActiveSchematic(SchematicWorld world) {
    }

    @Override
    public void setActiveSchematic(SchematicWorld world, EntityPlayer player) {
    }

    @Override
    public SchematicWorld getActiveSchematic() {
        return null;
    }

    @Override
    public SchematicWorld getActiveSchematic(EntityPlayer player) {
        return null;
    }
}
