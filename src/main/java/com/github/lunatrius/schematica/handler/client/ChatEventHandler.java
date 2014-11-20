package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.SchematicPrinter;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class ChatEventHandler {
    public static final ChatEventHandler INSTANCE = new ChatEventHandler();

    public int chatLines = 0;

    private ChatEventHandler() {}

    @SubscribeEvent
    public void onClientChatReceivedEvent(ClientChatReceivedEvent event) {
        if (this.chatLines < 20) {
            this.chatLines++;
            String message = event.message.getFormattedText();
            Reference.logger.debug(String.format("Message #%d: %s", this.chatLines, message));
            if (message.contains(Names.SBC.DISABLE_PRINTER)) {
                Reference.logger.info("Printer is disabled on this server.");
                SchematicPrinter.INSTANCE.setEnabled(false);
            }
            if (message.contains(Names.SBC.DISABLE_SAVE)) {
                Reference.logger.info("Saving is disabled on this server.");
                Schematica.proxy.isSaveEnabled = false;
            }
            if (message.contains(Names.SBC.DISABLE_LOAD)) {
                Reference.logger.info("Loading is disabled on this server.");
                Schematica.proxy.isLoadEnabled = false;
            }
        }
    }
}
