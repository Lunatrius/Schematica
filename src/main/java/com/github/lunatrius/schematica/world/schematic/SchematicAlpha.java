package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.schematica.api.event.PreSchematicSaveEvent;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SchematicAlpha extends SchematicFormat {
    @Override
    public SchematicWorld readFromNBT(NBTTagCompound tagCompound) {
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

        short[][][] blocks = new short[width][height][length];
        byte[][][] metadata = new byte[width][height][length];

        Short id = null;
        Map<Short, Short> oldToNew = new HashMap<Short, Short>();
        if (tagCompound.hasKey(Names.NBT.MAPPING_SCHEMATICA)) {
            NBTTagCompound mapping = tagCompound.getCompoundTag(Names.NBT.MAPPING_SCHEMATICA);
            Set<String> names = mapping.func_150296_c();
            for (String name : names) {
                oldToNew.put(mapping.getShort(name), (short) GameData.getBlockRegistry().getId(name));
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    int index = x + (y * length + z) * width;
                    blocks[x][y][z] = (short) ((localBlocks[index] & 0xFF) | (extra ? ((extraBlocks[index] & 0xFF) << 8) : 0));
                    metadata[x][y][z] = (byte) (localMetadata[index] & 0xFF);
                    if ((id = oldToNew.get(blocks[x][y][z])) != null) {
                        blocks[x][y][z] = id;
                    }
                }
            }
        }

        List<TileEntity> tileEntities = new ArrayList<TileEntity>();
        NBTTagList tileEntitiesList = tagCompound.getTagList(Names.NBT.TILE_ENTITIES, Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < tileEntitiesList.tagCount(); i++) {
            try {
                TileEntity tileEntity = TileEntity.createAndLoadEntity(tileEntitiesList.getCompoundTagAt(i));
                if (tileEntity != null) {
                    tileEntities.add(tileEntity);
                }
            } catch (Exception e) {
                Reference.logger.error("TileEntity failed to load properly!", e);
            }
        }

        return new SchematicWorld(icon, blocks, metadata, tileEntities, width, height, length);
    }

    @Override
    public boolean writeToNBT(NBTTagCompound tagCompound, SchematicWorld world) {
        NBTTagCompound tagCompoundIcon = new NBTTagCompound();
        ItemStack icon = world.getIcon();
        icon.writeToNBT(tagCompoundIcon);
        tagCompound.setTag(Names.NBT.ICON, tagCompoundIcon);

        tagCompound.setShort(Names.NBT.WIDTH, (short) world.getWidth());
        tagCompound.setShort(Names.NBT.LENGTH, (short) world.getLength());
        tagCompound.setShort(Names.NBT.HEIGHT, (short) world.getHeight());

        int size = world.getWidth() * world.getLength() * world.getHeight();
        byte localBlocks[] = new byte[size];
        byte localMetadata[] = new byte[size];
        byte extraBlocks[] = new byte[size];
        byte extraBlocksNibble[] = new byte[(int) Math.ceil(size / 2.0)];
        boolean extra = false;

        Map<String, Short> mappings = new HashMap<String, Short>();
        for (int x = 0; x < world.getWidth(); x++) {
            for (int y = 0; y < world.getHeight(); y++) {
                for (int z = 0; z < world.getLength(); z++) {
                    int index = x + (y * world.getLength() + z) * world.getWidth();
                    int blockId = world.getBlockIdRaw(x, y, z);
                    localBlocks[index] = (byte) blockId;
                    localMetadata[index] = (byte) world.getBlockMetadata(x, y, z);
                    if ((extraBlocks[index] = (byte) (blockId >> 8)) > 0) {
                        extra = true;
                    }

                    String name = GameData.getBlockRegistry().getNameForObject(world.getBlockRaw(x, y, z));
                    if (!mappings.containsKey(name)) {
                        mappings.put(name, (short)blockId);
                    }
                }
            }
        }

        for (int i = 0; i < extraBlocksNibble.length; i++) {
            if (i * 2 + 1 < extraBlocks.length) {
                extraBlocksNibble[i] = (byte) ((extraBlocks[i * 2 + 0] << 4) | extraBlocks[i * 2 + 1]);
            } else {
                extraBlocksNibble[i] = (byte) (extraBlocks[i * 2 + 0] << 4);
            }
        }

        int count = 20;
        NBTTagList tileEntitiesList = new NBTTagList();
        for (TileEntity tileEntity : world.getTileEntities()) {
            NBTTagCompound tileEntityTagCompound = new NBTTagCompound();
            try {
                tileEntity.writeToNBT(tileEntityTagCompound);
                tileEntitiesList.appendTag(tileEntityTagCompound);
            } catch (Exception e) {
                int pos = tileEntity.xCoord + (tileEntity.yCoord * world.getLength() + tileEntity.zCoord) * world.getWidth();
                if (--count > 0) {
                    Block block = world.getBlockRaw(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
                    Reference.logger.error(String.format("Block %s[%s] with TileEntity %s failed to save! Replacing with bedrock...", block, block != null ? GameData.getBlockRegistry().getNameForObject(block) : "?", tileEntity.getClass().getName()), e);
                }
                localBlocks[pos] = (byte) GameData.getBlockRegistry().getId(Blocks.bedrock);
                localMetadata[pos] = 0;
                extraBlocks[pos] = 0;
            }
        }


        PreSchematicSaveEvent event = new PreSchematicSaveEvent(mappings);
        //TODO: Post event on the bus.
        //MinecraftForge.EVENT_BUS.post(event);

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
        tagCompound.setTag(Names.NBT.ENTITIES, new NBTTagList());
        tagCompound.setTag(Names.NBT.TILE_ENTITIES, tileEntitiesList);
        tagCompound.setTag(Names.NBT.MAPPING_SCHEMATICA, nbtMapping);
        final NBTTagCompound extendedMetadata = event.extendedMetadata;
        if (!extendedMetadata.hasNoTags()) {
            tagCompound.setTag(Names.NBT.EXTENDED_METADATA, extendedMetadata);
        }

        return true;
    }
}
