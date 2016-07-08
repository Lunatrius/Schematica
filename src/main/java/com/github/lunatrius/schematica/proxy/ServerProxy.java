package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.schematica.command.CommandSchematicaDownload;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.handler.PlayerHandler;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class ServerProxy extends CommonProxy {
    private WeakReference<MinecraftServer> serverWeakReference = null;

    @Override
    public void init(final FMLInitializationEvent event) {
        super.init(event);

        MinecraftForge.EVENT_BUS.register(PlayerHandler.INSTANCE);
    }

    @Override
    public void serverStarting(final FMLServerStartingEvent event) {
        super.serverStarting(event);
        event.registerServerCommand(new CommandSchematicaDownload());
        this.serverWeakReference = new WeakReference<MinecraftServer>(event.getServer());
    }

    @Override
    public File getDataDirectory() {
        final MinecraftServer server = this.serverWeakReference != null ? this.serverWeakReference.get() : null;
        final File file = server != null ? server.getFile(".") : new File(".");
        try {
            return file.getCanonicalFile();
        } catch (final IOException e) {
            Reference.logger.warn("Could not canonize path!", e);
        }
        return file;
    }

    @Override
    public boolean loadSchematic(final EntityPlayer player, final File directory, final String filename) {
        return false;
    }

    @Override
    public boolean isPlayerQuotaExceeded(final EntityPlayer player) {
        int spaceUsed = 0;

        //Space used by private directory
        File schematicDirectory = getPlayerSchematicDirectory(player, true);
        spaceUsed += getSpaceUsedByDirectory(schematicDirectory);

        //Space used by public directory
        schematicDirectory = getPlayerSchematicDirectory(player, false);
        spaceUsed += getSpaceUsedByDirectory(schematicDirectory);
        return ((spaceUsed / 1024) > ConfigurationHandler.playerQuotaKilobytes);
    }

    private int getSpaceUsedByDirectory(final File directory) {
        int spaceUsed = 0;
        //If we don't have a player directory yet, then they haven't uploaded any files yet.
        if (directory == null || !directory.exists()) {
            return 0;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            files = new File[0];
        }
        for (final File path : files) {
            spaceUsed += path.length();
        }
        return spaceUsed;
    }

    @Override
    public File getPlayerSchematicDirectory(final EntityPlayer player, final boolean privateDirectory) {
        final UUID playerId = player.getUniqueID();
        if (playerId == null) {
            Reference.logger.warn("Unable to identify player {}", player.toString());
            return null;
        }

        final File playerDir = new File(ConfigurationHandler.schematicDirectory.getAbsolutePath(), playerId.toString());
        if (privateDirectory) {
            return new File(playerDir, "private");
        } else {
            return new File(playerDir, "public");
        }
    }
}
