package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.SchematicPrinter;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.reference.Reference;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class ChatEventHandler {
	public static final String SBC_DISABLE_PRINTER = "\u00a70\u00a72\u00a70\u00a70\u00a7e\u00a7f";
	public static final String SBC_DISABLE_SAVE = "\u00a70\u00a72\u00a71\u00a70\u00a7e\u00a7f";
	public static final String SBC_DISABLE_LOAD = "\u00a70\u00a72\u00a71\u00a71\u00a7e\u00a7f";

	public static int chatLines = 0;

	@SubscribeEvent
	public void onClientChatReceivedEvent(ClientChatReceivedEvent event) {
		if (chatLines < 20) {
			chatLines++;
			String message = event.message.getFormattedText();
			Reference.logger.debug(String.format("Message #%d: %s", chatLines, message));
			if (message.contains(SBC_DISABLE_PRINTER)) {
				Reference.logger.info("Printer is disabled on this server.");
				SchematicPrinter.INSTANCE.setEnabled(false);
			}
			if (message.contains(SBC_DISABLE_SAVE)) {
				Reference.logger.info("Saving is disabled on this server.");
				Schematica.proxy.isSaveEnabled = false;
			}
			if (message.contains(SBC_DISABLE_LOAD)) {
				Reference.logger.info("Loading is disabled on this server.");
				Schematica.proxy.isLoadEnabled = false;
			}
		}
	}
}
