package com.github.lunatrius.schematica.client.printer.nbtsync;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class NBTSync {
    protected final Minecraft minecraft = Minecraft.getMinecraft();

    public abstract boolean execute(final EntityPlayer player, final World schematic, final BlockPos pos, final World mcWorld, final BlockPos mcPos);

    public final <T extends INetHandler> boolean sendPacket(final Packet<T> packet) {
        final NetHandlerPlayClient connection = this.minecraft.getConnection();
        if (connection == null) {
            return false;
        }

        connection.sendPacket(packet);
        return true;
    }
}
