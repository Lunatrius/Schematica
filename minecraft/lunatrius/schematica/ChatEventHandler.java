package lunatrius.schematica;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.ForgeSubscribe;

public class ChatEventHandler {
	private final Settings settings = Settings.instance();

	@ForgeSubscribe
	public void onClientChatReceivedEvent(ClientChatReceivedEvent event) {
		this.settings.chatLines++;

		if (this.settings.isPrinterEnabled && this.settings.chatLines < 10) {
			if (event.message.contains(Settings.sbcDisablePrinter)) {
				Settings.logger.logInfo("Printer is disabled on this server.");
				this.settings.isPrinterEnabled = false;
			}
		}
	}
}
