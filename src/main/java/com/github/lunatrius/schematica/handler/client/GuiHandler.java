package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiHandler {
    public static final GuiHandler INSTANCE = new GuiHandler();

    @SubscribeEvent
    public void onGuiOpen(final GuiOpenEvent event) {
        if (SchematicPrinter.INSTANCE.isPrinting()) {
            if (event.getGui() instanceof GuiEditSign) {
                event.setGui(null);
            }
        }
    }
}
