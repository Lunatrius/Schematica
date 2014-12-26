package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.network.PacketHandler;
import com.github.lunatrius.schematica.network.message.MessageDownloadBegin;
import com.github.lunatrius.schematica.network.message.MessageDownloadChunk;
import com.github.lunatrius.schematica.network.message.MessageDownloadEnd;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.LinkedHashMap;
import java.util.Map;

public class DownloadHandler {
    public static final DownloadHandler INSTANCE = new DownloadHandler();

    public SchematicWorld schematic = null;

    public final Map<EntityPlayerMP, SchematicTransfer> transferMap = new LinkedHashMap<EntityPlayerMP, SchematicTransfer>();

    private DownloadHandler() {}

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            return;
        }

        processQueue();
    }

    private void processQueue() {
        if (this.transferMap.size() == 0) {
            return;
        }

        final EntityPlayerMP player = this.transferMap.keySet().iterator().next();
        final SchematicTransfer transfer = this.transferMap.remove(player);

        if (transfer == null) {
            return;
        }

        if (!transfer.state.isWaiting()) {
            if (++transfer.timeout >= Constants.Network.TIMEOUT) {
                if (++transfer.retries >= Constants.Network.RETRIES) {
                    Reference.logger.warn(String.format("%s's download was dropped!", player.getDisplayName()));
                    return;
                }

                Reference.logger.warn(String.format("%s's download timed out, retrying (#%d)", player.getDisplayName(), transfer.retries));

                sendChunk(player, transfer);
                transfer.timeout = 0;
            }
        } else if (transfer.state == SchematicTransfer.State.BEGIN_WAIT) {
            sendBegin(player, transfer);
        } else if (transfer.state == SchematicTransfer.State.CHUNK_WAIT) {
            sendChunk(player, transfer);
        } else if (transfer.state == SchematicTransfer.State.END_WAIT) {
            sendEnd(player, transfer);
            return;
        }

        this.transferMap.put(player, transfer);
    }

    private void sendBegin(EntityPlayerMP player, SchematicTransfer transfer) {
        transfer.setState(SchematicTransfer.State.BEGIN);

        MessageDownloadBegin message = new MessageDownloadBegin(transfer.schematic);
        PacketHandler.INSTANCE.sendTo(message, player);
    }

    private void sendChunk(EntityPlayerMP player, SchematicTransfer transfer) {
        transfer.setState(SchematicTransfer.State.CHUNK);

        MessageDownloadChunk message = new MessageDownloadChunk(transfer.schematic, transfer.baseX, transfer.baseY, transfer.baseZ);
        PacketHandler.INSTANCE.sendTo(message, player);
    }

    private void sendEnd(EntityPlayerMP player, SchematicTransfer transfer) {
        MessageDownloadEnd message = new MessageDownloadEnd(transfer.name);
        PacketHandler.INSTANCE.sendTo(message, player);
    }
}
