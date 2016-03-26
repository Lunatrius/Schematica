package com.github.lunatrius.schematica.client.printer.nbtsync;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NBTSyncCommandBlock extends NBTSync {
    @Override
    public boolean execute(final EntityPlayer player, final World schematic, final BlockPos pos, final World mcWorld, final BlockPos mcPos) {
        final TileEntity tileEntity = schematic.getTileEntity(pos);
        final TileEntity mcTileEntity = mcWorld.getTileEntity(mcPos);

        if (tileEntity instanceof TileEntityCommandBlock && mcTileEntity instanceof TileEntityCommandBlock) {
            final CommandBlockBaseLogic commandBlockLogic = ((TileEntityCommandBlock) tileEntity).getCommandBlockLogic();
            final CommandBlockBaseLogic mcCommandBlockLogic = ((TileEntityCommandBlock) mcTileEntity).getCommandBlockLogic();

            if (!commandBlockLogic.getCommand().equals(mcCommandBlockLogic.getCommand())) {
                final PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());

                packetBuffer.writeByte(mcCommandBlockLogic.getCommandBlockType());
                mcCommandBlockLogic.fillInInfo(packetBuffer);
                packetBuffer.writeString(commandBlockLogic.getCommand());
                packetBuffer.writeBoolean(mcCommandBlockLogic.shouldTrackOutput());

                return sendPacket(new CPacketCustomPayload("MC|AdvCdm", packetBuffer));
            }
        }

        return false;
    }
}
