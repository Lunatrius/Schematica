package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.SchematicPrinter;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class TickHandler {
    public static final TickHandler INSTANCE = new TickHandler();

    private final Minecraft minecraft = Minecraft.getMinecraft();

    private int ticks = -1;

    private TickHandler() {}

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        /* TODO: is this still needed?
        Reference.logger.info("Scheduling client settings reset.");
        ClientProxy.isPendingReset = true;
        */
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Reference.logger.info("Scheduling client settings reset.");
        ClientProxy.isPendingReset = true;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            this.minecraft.mcProfiler.startSection("schematica");
            SchematicWorld schematic = Schematica.proxy.getActiveSchematic();
            if (this.minecraft.thePlayer != null && schematic != null && schematic.isRendering) {
                this.minecraft.mcProfiler.startSection("printer");
                SchematicPrinter printer = SchematicPrinter.INSTANCE;
                if (printer.isEnabled() && printer.isPrinting() && this.ticks-- < 0) {
                    this.ticks = ConfigurationHandler.placeDelay;

                    printer.print();
                }

                this.minecraft.mcProfiler.endSection();
            }

            if (ClientProxy.isPendingReset) {
                Schematica.proxy.resetSettings();
                ClientProxy.isPendingReset = false;
                Reference.logger.info("Client settings have been reset.");
            }

            this.minecraft.mcProfiler.endSection();
        }
    }
}
