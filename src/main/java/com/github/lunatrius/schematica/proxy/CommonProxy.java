package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.core.util.MBlockPos;
import com.github.lunatrius.core.version.VersionChecker;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.command.CommandSchematicaList;
import com.github.lunatrius.schematica.command.CommandSchematicaRemove;
import com.github.lunatrius.schematica.command.CommandSchematicaSave;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.handler.QueueTickHandler;
import com.github.lunatrius.schematica.nbt.NBTConversionException;
import com.github.lunatrius.schematica.nbt.NBTHelper;
import com.github.lunatrius.schematica.network.PacketHandler;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.chunk.SchematicContainer;
import com.github.lunatrius.schematica.world.schematic.SchematicUtil;
import com.github.lunatrius.schematica.world.storage.Schematic;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.io.File;
import java.util.List;

public abstract class CommonProxy {
    public boolean isSaveEnabled = true;
    public boolean isLoadEnabled = true;

    public void preInit(FMLPreInitializationEvent event) {
        Reference.logger = event.getModLog();
        ConfigurationHandler.init(event.getSuggestedConfigurationFile());

        VersionChecker.registerMod(event.getModMetadata(), Reference.FORGE);
    }

    public void init(FMLInitializationEvent event) {
        PacketHandler.init();

        FMLCommonHandler.instance().bus().register(QueueTickHandler.INSTANCE);
        FMLCommonHandler.instance().bus().register(DownloadHandler.INSTANCE);
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandSchematicaSave());
        event.registerServerCommand(new CommandSchematicaList());
        event.registerServerCommand(new CommandSchematicaRemove());
    }

    public void createFolders() {
        if (!ConfigurationHandler.schematicDirectory.exists()) {
            if (!ConfigurationHandler.schematicDirectory.mkdirs()) {
                Reference.logger.warn("Could not create schematic directory [{}]!", ConfigurationHandler.schematicDirectory.getAbsolutePath());
            }
        }
    }

    public abstract File getDataDirectory();

    public void resetSettings() {
        this.isSaveEnabled = true;
        this.isLoadEnabled = true;
    }

    public void copyChunkToSchematic(final ISchematic schematic, final World world, final int chunkX, final int chunkZ, final int minX, final int maxX, final int minY, final int maxY, final int minZ, final int maxZ) {
        final MBlockPos pos = new MBlockPos();
        final MBlockPos localPos = new MBlockPos();
        final int localMinX = minX < (chunkX << 4) ? 0 : (minX & 15);
        final int localMaxX = maxX > ((chunkX << 4) + 15) ? 15 : (maxX & 15);
        final int localMinZ = minZ < (chunkZ << 4) ? 0 : (minZ & 15);
        final int localMaxZ = maxZ > ((chunkZ << 4) + 15) ? 15 : (maxZ & 15);

        for (int chunkLocalX = localMinX; chunkLocalX <= localMaxX; chunkLocalX++) {
            for (int chunkLocalZ = localMinZ; chunkLocalZ <= localMaxZ; chunkLocalZ++) {
                for (int y = minY; y <= maxY; y++) {
                    final int x = chunkLocalX | (chunkX << 4);
                    final int z = chunkLocalZ | (chunkZ << 4);

                    final int localX = x - minX;
                    final int localY = y - minY;
                    final int localZ = z - minZ;

                    pos.set(x, y, z);
                    localPos.set(localX, localY, localZ);

                    try {
                        final IBlockState blockState = world.getBlockState(pos);
                        final Block block = blockState.getBlock();
                        final boolean success = schematic.setBlockState(localPos, blockState);

                        if (success && block.hasTileEntity(blockState)) {
                            final TileEntity tileEntity = world.getTileEntity(pos);
                            if (tileEntity != null) {
                                try {
                                    final TileEntity reloadedTileEntity = NBTHelper.reloadTileEntity(tileEntity, minX, minY, minZ);
                                    schematic.setTileEntity(localPos, reloadedTileEntity);
                                } catch (NBTConversionException nce) {
                                    Reference.logger.error("Error while trying to save tile entity '{}'!", tileEntity, nce);
                                    schematic.setBlockState(localPos, Blocks.bedrock.getDefaultState());
                                }
                            }
                        }
                    } catch (Exception e) {
                        Reference.logger.error("Something went wrong!", e);
                    }
                }
            }
        }

        final int minX1 = localMinX | (chunkX << 4);
        final int minZ1 = localMinZ | (chunkZ << 4);
        final int maxX1 = localMaxX | (chunkX << 4);
        final int maxZ1 = localMaxZ | (chunkZ << 4);
        final AxisAlignedBB bb = AxisAlignedBB.fromBounds(minX1, minY, minZ1, maxX1 + 1, maxY + 1, maxZ1 + 1);
        final List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, bb);
        for (Entity entity : entities) {
            try {
                final Entity reloadedEntity = NBTHelper.reloadEntity(entity, minX, minY, minZ);
                schematic.addEntity(reloadedEntity);
            } catch (NBTConversionException nce) {
                Reference.logger.error("Error while trying to save entity '{}'!", entity, nce);
            }
        }
    }

    public boolean saveSchematic(EntityPlayer player, File directory, String filename, World world, BlockPos from, BlockPos to) {
        try {
            String iconName = "";

            try {
                String[] parts = filename.split(";");
                if (parts.length == 2) {
                    iconName = parts[0];
                    filename = parts[1];
                }
            } catch (Exception e) {
                Reference.logger.error("Failed to parse icon data!", e);
            }

            final int minX = Math.min(from.getX(), to.getX());
            final int maxX = Math.max(from.getX(), to.getX());
            final int minY = Math.min(from.getY(), to.getY());
            final int maxY = Math.max(from.getY(), to.getY());
            final int minZ = Math.min(from.getZ(), to.getZ());
            final int maxZ = Math.max(from.getZ(), to.getZ());

            final short width = (short) (Math.abs(maxX - minX) + 1);
            final short height = (short) (Math.abs(maxY - minY) + 1);
            final short length = (short) (Math.abs(maxZ - minZ) + 1);

            final ISchematic schematic = new Schematic(SchematicUtil.getIconFromName(iconName), width, height, length);
            final SchematicContainer container = new SchematicContainer(schematic, player, world, new File(directory, filename), minX, maxX, minY, maxY, minZ, maxZ);
            QueueTickHandler.INSTANCE.queueSchematic(container);

            return true;
        } catch (Exception e) {
            Reference.logger.error("Failed to save schematic!", e);
        }
        return false;
    }

    public abstract boolean loadSchematic(EntityPlayer player, File directory, String filename);

    public abstract boolean isPlayerQuotaExceeded(EntityPlayer player);

    public abstract File getPlayerSchematicDirectory(EntityPlayer player, boolean privateDirectory);
}
