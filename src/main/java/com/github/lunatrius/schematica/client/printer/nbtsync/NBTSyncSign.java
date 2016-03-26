package com.github.lunatrius.schematica.client.printer.nbtsync;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.Arrays;

public class NBTSyncSign extends NBTSync {
    @Override
    public boolean execute(final EntityPlayer player, final World schematic, final BlockPos pos, final World mcWorld, final BlockPos mcPos) {
        final TileEntity tileEntity = schematic.getTileEntity(pos);
        final TileEntity mcTileEntity = mcWorld.getTileEntity(mcPos);

        if (tileEntity instanceof TileEntitySign && mcTileEntity instanceof TileEntitySign) {
            final ITextComponent[] signText = ((TileEntitySign) tileEntity).signText;
            final ITextComponent[] mcSignText = ((TileEntitySign) mcTileEntity).signText;

            if (!Arrays.equals(signText, mcSignText)) {
                return sendPacket(new CPacketUpdateSign(mcPos, signText));
            }
        }

        return false;
    }
}
