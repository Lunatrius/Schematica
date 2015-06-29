package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.core.util.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.api.event.PreSchematicSaveEvent;
import com.github.lunatrius.schematica.nbt.NBTHelper;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.storage.Schematic;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SchematicAlpha extends SchematicFormat {
    private static final FMLControlledNamespacedRegistry<Block> BLOCK_REGISTRY = GameData.getBlockRegistry();

    @Override
    public ISchematic readFromNBT(NBTTagCompound tagCompound) {
        ItemStack icon = SchematicUtil.getIconFromNBT(tagCompound);

        byte localBlocks[] = tagCompound.getByteArray(Names.NBT.BLOCKS);
        byte localMetadata[] = tagCompound.getByteArray(Names.NBT.DATA);

        boolean extra = false;
        byte extraBlocks[] = null;
        byte extraBlocksNibble[] = null;
        if (tagCompound.hasKey(Names.NBT.ADD_BLOCKS)) {
            extra = true;
            extraBlocksNibble = tagCompound.getByteArray(Names.NBT.ADD_BLOCKS);
            extraBlocks = new byte[extraBlocksNibble.length * 2];
            for (int i = 0; i < extraBlocksNibble.length; i++) {
                extraBlocks[i * 2 + 0] = (byte) ((extraBlocksNibble[i] >> 4) & 0xF);
                extraBlocks[i * 2 + 1] = (byte) (extraBlocksNibble[i] & 0xF);
            }
        } else if (tagCompound.hasKey(Names.NBT.ADD_BLOCKS_SCHEMATICA)) {
            extra = true;
            extraBlocks = tagCompound.getByteArray(Names.NBT.ADD_BLOCKS_SCHEMATICA);
        }

        short width = tagCompound.getShort(Names.NBT.WIDTH);
        short length = tagCompound.getShort(Names.NBT.LENGTH);
        short height = tagCompound.getShort(Names.NBT.HEIGHT);

        Short id = null;
        Map<Short, Short> oldToNew = new HashMap<Short, Short>();
        if (tagCompound.hasKey(Names.NBT.MAPPING_SCHEMATICA)) {
            NBTTagCompound mapping = tagCompound.getCompoundTag(Names.NBT.MAPPING_SCHEMATICA);
            Set<String> names = mapping.getKeySet();
            for (String name : names) {
                oldToNew.put(mapping.getShort(name), (short) BLOCK_REGISTRY.getId(name));
            }
        }

        final MBlockPos pos = new MBlockPos();
        ISchematic schematic = new Schematic(icon, width, height, length);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    int index = x + (y * length + z) * width;
                    int blockID = (localBlocks[index] & 0xFF) | (extra ? ((extraBlocks[index] & 0xFF) << 8) : 0);
                    int meta = localMetadata[index] & 0xFF;

                    if ((id = oldToNew.get((short) blockID)) != null) {
                        blockID = id;
                    }

                    final Block block = BLOCK_REGISTRY.getObjectById(blockID);
                    pos.set(x, y, z);
                    try {
                        final IBlockState blockState = block.getStateFromMeta(meta);
                        schematic.setBlockState(pos, blockState);
                    } catch (Exception e) {
                        Reference.logger.error("Could not set block state at {} to {} with metadata {}", pos, BLOCK_REGISTRY.getNameForObject(block), meta, e);
                    }
                }
            }
        }

        NBTTagList tileEntitiesList = tagCompound.getTagList(Names.NBT.TILE_ENTITIES, Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < tileEntitiesList.tagCount(); i++) {
            try {
                TileEntity tileEntity = NBTHelper.readTileEntityFromCompound(tileEntitiesList.getCompoundTagAt(i));
                if (tileEntity != null) {
                    schematic.setTileEntity(tileEntity.getPos(), tileEntity);
                }
            } catch (Exception e) {
                Reference.logger.error("TileEntity failed to load properly!", e);
            }
        }

        return schematic;
    }

    @Override
    public boolean writeToNBT(NBTTagCompound tagCompound, ISchematic schematic) {
        NBTTagCompound tagCompoundIcon = new NBTTagCompound();
        ItemStack icon = schematic.getIcon();
        icon.writeToNBT(tagCompoundIcon);
        tagCompound.setTag(Names.NBT.ICON, tagCompoundIcon);

        tagCompound.setShort(Names.NBT.WIDTH, (short) schematic.getWidth());
        tagCompound.setShort(Names.NBT.LENGTH, (short) schematic.getLength());
        tagCompound.setShort(Names.NBT.HEIGHT, (short) schematic.getHeight());

        int size = schematic.getWidth() * schematic.getLength() * schematic.getHeight();
        byte localBlocks[] = new byte[size];
        byte localMetadata[] = new byte[size];
        byte extraBlocks[] = new byte[size];
        byte extraBlocksNibble[] = new byte[(int) Math.ceil(size / 2.0)];
        boolean extra = false;

        final MBlockPos pos = new MBlockPos();
        Map<String, Short> mappings = new HashMap<String, Short>();
        for (int x = 0; x < schematic.getWidth(); x++) {
            for (int y = 0; y < schematic.getHeight(); y++) {
                for (int z = 0; z < schematic.getLength(); z++) {
                    final int index = x + (y * schematic.getLength() + z) * schematic.getWidth();
                    final IBlockState blockState = schematic.getBlockState(pos.set(x, y, z));
                    final Block block = blockState.getBlock();
                    final int blockId = BLOCK_REGISTRY.getId(block);
                    localBlocks[index] = (byte) blockId;
                    localMetadata[index] = (byte) block.getMetaFromState(blockState);
                    if ((extraBlocks[index] = (byte) (blockId >> 8)) > 0) {
                        extra = true;
                    }

                    String name = String.valueOf(BLOCK_REGISTRY.getNameForObject(block));
                    if (!mappings.containsKey(name)) {
                        mappings.put(name, (short) blockId);
                    }
                }
            }
        }

        int count = 20;
        NBTTagList tileEntitiesList = new NBTTagList();
        for (TileEntity tileEntity : schematic.getTileEntities()) {
            try {
                NBTTagCompound tileEntityTagCompound = NBTHelper.writeTileEntityToCompound(tileEntity);
                tileEntitiesList.appendTag(tileEntityTagCompound);
            } catch (Exception e) {
                final BlockPos tePos = tileEntity.getPos();
                final int index = tePos.getX() + (tePos.getY() * schematic.getLength() + tePos.getZ()) * schematic.getWidth();
                if (--count > 0) {
                    final IBlockState blockState = schematic.getBlockState(tePos);
                    Block block = blockState.getBlock();
                    Reference.logger.error("Block {}[{}] with TileEntity {} failed to save! Replacing with bedrock...", block, block != null ? BLOCK_REGISTRY.getNameForObject(block) : "?", tileEntity.getClass().getName(), e);
                }
                localBlocks[index] = (byte) BLOCK_REGISTRY.getId(Blocks.bedrock);
                localMetadata[index] = 0;
                extraBlocks[index] = 0;
            }
        }

        for (int i = 0; i < extraBlocksNibble.length; i++) {
            if (i * 2 + 1 < extraBlocks.length) {
                extraBlocksNibble[i] = (byte) ((extraBlocks[i * 2 + 0] << 4) | extraBlocks[i * 2 + 1]);
            } else {
                extraBlocksNibble[i] = (byte) (extraBlocks[i * 2 + 0] << 4);
            }
        }

        final NBTTagList entityList = new NBTTagList();
        final List<Entity> entities = schematic.getEntities();
        for (Entity entity : entities) {
            try {
                final NBTTagCompound entityCompound = NBTHelper.writeEntityToCompound(entity);
                if (entityCompound != null) {
                    entityList.appendTag(entityCompound);
                }
            } catch (Throwable t) {
                Reference.logger.error("Entity {} failed to save, skipping!", entity, t);
            }
        }

        PreSchematicSaveEvent event = new PreSchematicSaveEvent(schematic, mappings);
        MinecraftForge.EVENT_BUS.post(event);

        NBTTagCompound nbtMapping = new NBTTagCompound();
        for (Map.Entry<String, Short> entry : mappings.entrySet()) {
            nbtMapping.setShort(entry.getKey(), entry.getValue());
        }

        tagCompound.setString(Names.NBT.MATERIALS, Names.NBT.FORMAT_ALPHA);
        tagCompound.setByteArray(Names.NBT.BLOCKS, localBlocks);
        tagCompound.setByteArray(Names.NBT.DATA, localMetadata);
        if (extra) {
            tagCompound.setByteArray(Names.NBT.ADD_BLOCKS, extraBlocksNibble);
        }
        tagCompound.setTag(Names.NBT.ENTITIES, entityList);
        tagCompound.setTag(Names.NBT.TILE_ENTITIES, tileEntitiesList);
        tagCompound.setTag(Names.NBT.MAPPING_SCHEMATICA, nbtMapping);
        final NBTTagCompound extendedMetadata = event.extendedMetadata;
        if (!extendedMetadata.hasNoTags()) {
            tagCompound.setTag(Names.NBT.EXTENDED_METADATA, extendedMetadata);
        }

        return true;
    }
}
