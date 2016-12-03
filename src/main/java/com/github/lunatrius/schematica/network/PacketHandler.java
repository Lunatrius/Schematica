package com.github.lunatrius.schematica.network;

import com.github.lunatrius.schematica.network.message.MessageCapabilities;
import com.github.lunatrius.schematica.network.message.MessageDownloadBegin;
import com.github.lunatrius.schematica.network.message.MessageDownloadBeginAck;
import com.github.lunatrius.schematica.network.message.MessageDownloadChunk;
import com.github.lunatrius.schematica.network.message.MessageDownloadChunkAck;
import com.github.lunatrius.schematica.network.message.MessageDownloadEnd;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MODID);

    public static void init() {
        INSTANCE.registerMessage(MessageCapabilities.class, MessageCapabilities.class, 0, Side.CLIENT);

        INSTANCE.registerMessage(MessageDownloadBegin.class, MessageDownloadBegin.class, 1, Side.CLIENT);
        INSTANCE.registerMessage(MessageDownloadBeginAck.class, MessageDownloadBeginAck.class, 2, Side.SERVER);
        INSTANCE.registerMessage(MessageDownloadChunk.class, MessageDownloadChunk.class, 3, Side.CLIENT);
        INSTANCE.registerMessage(MessageDownloadChunkAck.class, MessageDownloadChunkAck.class, 4, Side.SERVER);
        INSTANCE.registerMessage(MessageDownloadEnd.class, MessageDownloadEnd.class, 5, Side.CLIENT);
    }
}
