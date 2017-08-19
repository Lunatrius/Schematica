package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.File;

public class MessageDownloadEnd implements IMessage, IMessageHandler<MessageDownloadEnd, IMessage> {
    public String name;

    public MessageDownloadEnd() {
    }

    public MessageDownloadEnd(final String name) {
        this.name = name;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        this.name = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.name);
    }

    @Override
    public IMessage onMessage(final MessageDownloadEnd message, final MessageContext ctx) {
        final File directory = Schematica.proxy.getPlayerSchematicDirectory(null, true);
        final boolean success = SchematicFormat.writeToFile(directory, message.name, null, DownloadHandler.INSTANCE.schematic);

        if (success) {
            Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation(Names.Command.Download.Message.DOWNLOAD_SUCCEEDED, message.name));
        }

        DownloadHandler.INSTANCE.schematic = null;

        return null;
    }
}
