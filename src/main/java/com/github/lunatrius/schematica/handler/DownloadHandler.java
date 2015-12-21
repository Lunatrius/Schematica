package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.network.PacketHandler;
import com.github.lunatrius.schematica.network.message.MessageDownloadBegin;
import com.github.lunatrius.schematica.network.message.MessageDownloadChunk;
import com.github.lunatrius.schematica.network.message.MessageDownloadEnd;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.LinkedHashMap;
import java.util.Map;

public class DownloadHandler {
    public static final DownloadHandler INSTANCE = new DownloadHandler();

    public ISchematic schematic = null;

    public final Map<EntityPlayerMP, SchematicTransfer> transferMap = new LinkedHashMap<EntityPlayerMP, SchematicTransfer>();

    private DownloadHandler() {}

    @SubscribeEvent
    public void onServerTick(final TickEvent.ServerTickEvent event) {
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
                    Reference.logger.warn("{}'s download was dropped!", player.getName());
                    return;
                }

                Reference.logger.warn("{}'s download timed out, retrying (#{})", player.getName(), transfer.retries);

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

    private void sendBegin(final EntityPlayerMP player, final SchematicTransfer transfer) {
        transfer.setState(SchematicTransfer.State.BEGIN);

        final MessageDownloadBegin message = new MessageDownloadBegin(transfer.schematic);
        PacketHandler.INSTANCE.sendTo(message, player);
    }

    private void sendChunk(final EntityPlayerMP player, final SchematicTransfer transfer) {
        transfer.setState(SchematicTransfer.State.CHUNK);

        Reference.logger.trace("Sending chunk {},{},{}", transfer.baseX, transfer.baseY, transfer.baseZ);
        final MessageDownloadChunk message = new MessageDownloadChunk(transfer.schematic, transfer.baseX, transfer.baseY, transfer.baseZ);
        PacketHandler.INSTANCE.sendTo(message, player);
    }

    private void sendEnd(final EntityPlayerMP player, final SchematicTransfer transfer) {
        final MessageDownloadEnd message = new MessageDownloadEnd(transfer.name);
        PacketHandler.INSTANCE.sendTo(message, player);
    }
}
