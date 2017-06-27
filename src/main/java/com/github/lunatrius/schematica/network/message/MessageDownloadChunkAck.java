package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageDownloadChunkAck implements IMessage, IMessageHandler<MessageDownloadChunkAck, IMessage> {
    private int baseX;
    private int baseY;
    private int baseZ;

    public MessageDownloadChunkAck() {
    }

    public MessageDownloadChunkAck(final int baseX, final int baseY, final int baseZ) {
        this.baseX = baseX;
        this.baseY = baseY;
        this.baseZ = baseZ;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        this.baseX = buf.readShort();
        this.baseY = buf.readShort();
        this.baseZ = buf.readShort();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeShort(this.baseX);
        buf.writeShort(this.baseY);
        buf.writeShort(this.baseZ);
    }

    @Override
    public IMessage onMessage(final MessageDownloadChunkAck message, final MessageContext ctx) {
        final EntityPlayerMP player = ctx.getServerHandler().player;
        final SchematicTransfer transfer = DownloadHandler.INSTANCE.transferMap.get(player);
        if (transfer != null) {
            transfer.confirmChunk(message.baseX, message.baseY, message.baseZ);
        }

        return null;
    }
}
