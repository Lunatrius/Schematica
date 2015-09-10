package com.github.lunatrius.schematica.client.printer.nbtsync;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import java.util.Arrays;

public class NBTSyncSign extends NBTSync {
    @Override
    public boolean execute(final EntityPlayer player, final World schematic, final BlockPos pos, final World mcWorld, final BlockPos mcPos) {
        final TileEntity tileEntity = schematic.getTileEntity(pos);
        final TileEntity mcTileEntity = mcWorld.getTileEntity(mcPos);

        if (tileEntity instanceof TileEntitySign && mcTileEntity instanceof TileEntitySign) {
            final IChatComponent[] signText = ((TileEntitySign) tileEntity).signText;
            final IChatComponent[] mcSignText = ((TileEntitySign) mcTileEntity).signText;

            if (!Arrays.equals(signText, mcSignText)) {
                return sendPacket(new C12PacketUpdateSign(mcPos, signText));
            }
        }

        return false;
    }
}
