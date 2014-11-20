package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.network.PacketHandler;
import com.github.lunatrius.schematica.network.message.MessageCapabilities;
import com.github.lunatrius.schematica.reference.Reference;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.player.EntityPlayerMP;

public class PlayerHandler {
    public static final PlayerHandler INSTANCE = new PlayerHandler();

    private PlayerHandler() {}

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            try {
                PacketHandler.INSTANCE.sendTo(new MessageCapabilities(ConfigurationHandler.printerEnabled, ConfigurationHandler.saveEnabled, ConfigurationHandler.loadEnabled), (EntityPlayerMP) event.player);
            } catch (Exception ex) {
                Reference.logger.error("Failed to send capabilities!", ex);
            }
        }
    }
}
