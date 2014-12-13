package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.world.SchematicWorld;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;

public class MessageDownloadBegin implements IMessage, IMessageHandler<MessageDownloadBegin, IMessage> {
    public ItemStack icon;
    public int width;
    public int height;
    public int length;

    public MessageDownloadBegin() {
    }

    public MessageDownloadBegin(SchematicWorld schematic) {
        this.icon = schematic.getIcon();
        this.width = schematic.getWidth();
        this.height = schematic.getHeight();
        this.length = schematic.getLength();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.icon = ByteBufUtils.readItemStack(buf);
        this.width = buf.readShort();
        this.height = buf.readShort();
        this.length = buf.readShort();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, this.icon);
        buf.writeShort(this.width);
        buf.writeShort(this.height);
        buf.writeShort(this.length);
    }

    @Override
    public IMessage onMessage(MessageDownloadBegin message, MessageContext ctx) {
        // TODO: implement
        return null;
    }
}
