package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.world.SchematicWorld;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageSchematicBegin implements IMessage, IMessageHandler<MessageSchematicBegin, IMessage> {
    public int width;
    public int height;
    public int length;

    public MessageSchematicBegin() {
    }

    public MessageSchematicBegin(SchematicWorld schematic) {
        this.width = schematic.getWidth();
        this.height = schematic.getHeight();
        this.length = schematic.getLength();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.width = buf.readShort();
        this.height = buf.readShort();
        this.length = buf.readShort();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(this.width);
        buf.writeShort(this.height);
        buf.writeShort(this.length);
    }

    @Override
    public IMessage onMessage(MessageSchematicBegin message, MessageContext ctx) {
        // TODO: implement
        return null;
    }
}
