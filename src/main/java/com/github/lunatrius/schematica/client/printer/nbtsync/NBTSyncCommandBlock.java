package com.github.lunatrius.schematica.client.printer.nbtsync;

import io.netty.buffer.Unpooled;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class NBTSyncCommandBlock extends NBTSync {
    @Override
    public boolean execute(final EntityPlayer player, final World schematic, final BlockPos pos, final World mcWorld, final BlockPos mcPos) {
        final TileEntity tileEntity = schematic.getTileEntity(pos);
        final TileEntity mcTileEntity = mcWorld.getTileEntity(mcPos);

        if (tileEntity instanceof TileEntityCommandBlock && mcTileEntity instanceof TileEntityCommandBlock) {
            final CommandBlockLogic commandBlockLogic = ((TileEntityCommandBlock) tileEntity).getCommandBlockLogic();
            final CommandBlockLogic mcCommandBlockLogic = ((TileEntityCommandBlock) mcTileEntity).getCommandBlockLogic();

            if (!commandBlockLogic.getCustomName().equals(mcCommandBlockLogic.getCustomName())) {
                final PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());

                packetBuffer.writeByte(mcCommandBlockLogic.func_145751_f());
                mcCommandBlockLogic.func_145757_a(packetBuffer);
                packetBuffer.writeString(commandBlockLogic.getCustomName());
                packetBuffer.writeBoolean(mcCommandBlockLogic.shouldTrackOutput());

                return sendPacket(new C17PacketCustomPayload("MC|AdvCdm", packetBuffer));
            }
        }

        return false;
    }
}
