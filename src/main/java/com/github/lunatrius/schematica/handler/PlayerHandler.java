package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.network.PacketHandler;
import com.github.lunatrius.schematica.network.message.MessageCapabilities;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class PlayerHandler {
    public static final PlayerHandler INSTANCE = new PlayerHandler();

    private PlayerHandler() {}

    @SubscribeEvent
    public void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            try {
                PacketHandler.INSTANCE.sendTo(new MessageCapabilities(ConfigurationHandler.printerEnabled, ConfigurationHandler.saveEnabled, ConfigurationHandler.loadEnabled), (EntityPlayerMP) event.player);
            } catch (final Exception ex) {
                Reference.logger.error("Failed to send capabilities!", ex);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            DownloadHandler.INSTANCE.transferMap.remove(event.player);
        }
    }
}
