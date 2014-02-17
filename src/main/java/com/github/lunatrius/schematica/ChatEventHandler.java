package com.github.lunatrius.schematica;

import com.github.lunatrius.schematica.lib.Reference;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class ChatEventHandler {
	private final Settings settings = Settings.instance;

	@SubscribeEvent
	public void onClientChatReceivedEvent(ClientChatReceivedEvent event) {
		this.settings.chatLines++;

		if (this.settings.isPrinterEnabled && this.settings.chatLines < 10) {
			if (event.message.getFormattedText().contains(Settings.sbcDisablePrinter)) {
				Reference.logger.info("Printer is disabled on this server.");
				this.settings.isPrinterEnabled = false;
			}
			if (event.message.getFormattedText().contains(Settings.sbcDisableSave)) {
				Reference.logger.info("Saving is disabled on this server.");
				this.settings.isSaveEnabled = false;
			}
			if (event.message.getFormattedText().contains(Settings.sbcDisableLoad)) {
				Reference.logger.info("Loading is disabled on this server.");
				this.settings.isLoadEnabled = false;
			}
		}
	}
}
