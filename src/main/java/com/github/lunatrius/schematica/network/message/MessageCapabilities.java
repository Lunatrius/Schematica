package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.SchematicPrinter;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.reference.Reference;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageCapabilities implements IMessage, IMessageHandler<MessageCapabilities, IMessage> {
    public boolean isPrinterEnabled;
    public boolean isSaveEnabled;
    public boolean isLoadEnabled;

    public MessageCapabilities() {
        this(false, false, false);
    }

    public MessageCapabilities(boolean isPrinterEnabled, boolean isSaveEnabled, boolean isLoadEnabled) {
        this.isPrinterEnabled = isPrinterEnabled;
        this.isSaveEnabled = isSaveEnabled;
        this.isLoadEnabled = isLoadEnabled;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.isPrinterEnabled = buf.readBoolean();
        this.isSaveEnabled = buf.readBoolean();
        this.isLoadEnabled = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.isPrinterEnabled);
        buf.writeBoolean(this.isSaveEnabled);
        buf.writeBoolean(this.isLoadEnabled);
    }

    @Override
    public IMessage onMessage(MessageCapabilities message, MessageContext ctx) {
        SchematicPrinter.INSTANCE.setEnabled(message.isPrinterEnabled);
        Schematica.proxy.isSaveEnabled = message.isSaveEnabled;
        Schematica.proxy.isLoadEnabled = message.isLoadEnabled;

        Reference.logger.info("Server capabilities{printer={}, save={}, load={}}", message.isPrinterEnabled, message.isSaveEnabled, message.isLoadEnabled);

        return null;
    }
}
