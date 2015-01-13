package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.chunk.SchematicContainer;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.util.ChatComponentTranslation;

import java.util.ArrayDeque;
import java.util.Queue;

public class QueueTickHandler {
    public static final QueueTickHandler INSTANCE = new QueueTickHandler();

    private final Queue<SchematicContainer> queue = new ArrayDeque<SchematicContainer>();

    private QueueTickHandler() {}

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            return;
        }

        // TODO: find a better way... maybe?
        try {
            final EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
            if (player != null && player.sendQueue != null && !player.sendQueue.getNetworkManager().isLocalChannel()) {
                processQueue();
            }
        } catch (Exception e) {
            Reference.logger.error("Something went wrong...", e);
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            return;
        }

        processQueue();
    }

    private void processQueue() {
        if (this.queue.size() == 0) {
            return;
        }

        final SchematicContainer container = this.queue.poll();
        if (container == null) {
            return;
        }

        if (container.hasNext()) {
            if (container.isFirst()) {
                final ChatComponentTranslation chatComponent = new ChatComponentTranslation(Names.Command.Save.Message.SAVE_STARTED, container.chunkCount, container.file.getName());
                container.player.addChatMessage(chatComponent);
            }

            container.next();
        }

        if (container.hasNext()) {
            this.queue.offer(container);
        } else {
            //TODO: Post Event on the bus.
            //final PostSchematicCaptureEvent event = new PostSchematicCaptureEvent(container.schematic);
            //MinecraftForge.EVENT_BUS.post(event);

            final boolean success = SchematicFormat.writeToFile(container.file, container.schematic);
            final String message = success ? Names.Command.Save.Message.SAVE_SUCCESSFUL : Names.Command.Save.Message.SAVE_FAILED;
            container.player.addChatMessage(new ChatComponentTranslation(message, container.file.getName()));
        }
    }

    public void queueSchematic(SchematicContainer container) {
        this.queue.offer(container);
    }
}
