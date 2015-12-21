package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.world.storage.Schematic;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageDownloadBegin implements IMessage, IMessageHandler<MessageDownloadBegin, IMessage> {
    public ItemStack icon;
    public int width;
    public int height;
    public int length;

    public MessageDownloadBegin() {
    }

    public MessageDownloadBegin(final ISchematic schematic) {
        this.icon = schematic.getIcon();
        this.width = schematic.getWidth();
        this.height = schematic.getHeight();
        this.length = schematic.getLength();
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        this.icon = ByteBufUtils.readItemStack(buf);
        this.width = buf.readShort();
        this.height = buf.readShort();
        this.length = buf.readShort();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, this.icon);
        buf.writeShort(this.width);
        buf.writeShort(this.height);
        buf.writeShort(this.length);
    }

    @Override
    public IMessage onMessage(final MessageDownloadBegin message, final MessageContext ctx) {
        DownloadHandler.INSTANCE.schematic = new Schematic(message.icon, message.width, message.height, message.length);

        return new MessageDownloadBeginAck();
    }
}
