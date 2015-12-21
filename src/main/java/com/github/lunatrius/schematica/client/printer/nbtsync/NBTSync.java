package com.github.lunatrius.schematica.client.printer.nbtsync;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public abstract class NBTSync {
    protected final Minecraft minecraft = Minecraft.getMinecraft();

    public abstract boolean execute(final EntityPlayer player, final World schematic, final BlockPos pos, final World mcWorld, final BlockPos mcPos);

    public final <T extends INetHandler> boolean sendPacket(final Packet<T> packet) {
        final NetHandlerPlayClient netHandler = this.minecraft.getNetHandler();
        if (netHandler == null) {
            return false;
        }

        netHandler.addToSendQueue(packet);
        return true;
    }
}
