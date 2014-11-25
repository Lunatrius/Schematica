package com.github.lunatrius.schematica.nbt;

import com.github.lunatrius.schematica.reference.Names;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class NBTHelper {
    public static List<TileEntity> readTileEntitiesFromCompound(final NBTTagCompound compound) {
        return readTileEntitiesFromCompound(compound, new ArrayList<TileEntity>());
    }

    public static List<TileEntity> readTileEntitiesFromCompound(final NBTTagCompound compound, final List<TileEntity> tileEntities) {
        final NBTTagList tagList = compound.getTagList(Names.NBT.TILE_ENTITIES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++) {
            final NBTTagCompound tileEntityCompound = tagList.getCompoundTagAt(i);
            final TileEntity tileEntity = TileEntity.createAndLoadEntity(tileEntityCompound);
            tileEntities.add(tileEntity);
        }

        return tileEntities;
    }

    public static NBTTagCompound writeTileEntitiesToCompound(final List<TileEntity> tileEntities) {
        return writeTileEntitiesToCompound(tileEntities, new NBTTagCompound());
    }

    public static NBTTagCompound writeTileEntitiesToCompound(final List<TileEntity> tileEntities, final NBTTagCompound compound) {
        final NBTTagList tagList = new NBTTagList();
        for (TileEntity tileEntity : tileEntities) {
            final NBTTagCompound tileEntityCompound = new NBTTagCompound();
            tileEntity.writeToNBT(tileEntityCompound);
            tagList.appendTag(tileEntityCompound);
        }

        compound.setTag(Names.NBT.TILE_ENTITIES, tagList);

        return compound;
    }

    public static List<Entity> readEntitiesFromCompound(final NBTTagCompound compound) {
        return readEntitiesFromCompound(compound, null, new ArrayList<Entity>());
    }

    public static List<Entity> readEntitiesFromCompound(final NBTTagCompound compound, final World world) {
        return readEntitiesFromCompound(compound, world, new ArrayList<Entity>());
    }

    public static List<Entity> readEntitiesFromCompound(final NBTTagCompound compound, final List<Entity> entities) {
        return readEntitiesFromCompound(compound, null, entities);
    }

    public static List<Entity> readEntitiesFromCompound(final NBTTagCompound compound, final World world, final List<Entity> entities) {
        final NBTTagList tagList = compound.getTagList(Names.NBT.ENTITIES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++) {
            final NBTTagCompound entityCompound = tagList.getCompoundTagAt(i);
            final Entity entity = EntityList.createEntityFromNBT(entityCompound, world);
            entities.add(entity);
        }

        return entities;
    }

    public static NBTTagCompound writeEntitiesToCompound(final List<Entity> entities) {
        return writeEntitiesToCompound(entities, new NBTTagCompound());
    }

    public static NBTTagCompound writeEntitiesToCompound(final List<Entity> entities, final NBTTagCompound compound) {
        final NBTTagList tagList = new NBTTagList();
        for (Entity entity : entities) {
            final NBTTagCompound entityCompound = new NBTTagCompound();
            entity.writeToNBT(entityCompound);
            tagList.appendTag(entityCompound);
        }

        compound.setTag(Names.NBT.ENTITIES, tagList);

        return compound;
    }

    public static TileEntity reloadTileEntity(TileEntity tileEntity) throws TileEntityException {
        return reloadTileEntity(tileEntity, 0, 0, 0);
    }

    public static TileEntity reloadTileEntity(TileEntity tileEntity, int offsetX, int offsetY, int offsetZ) throws TileEntityException {
        if (tileEntity == null) {
            return null;
        }

        try {
            NBTTagCompound tileEntityCompound = new NBTTagCompound();
            tileEntity.writeToNBT(tileEntityCompound);

            tileEntity = TileEntity.createAndLoadEntity(tileEntityCompound);
            tileEntity.xCoord -= offsetX;
            tileEntity.yCoord -= offsetY;
            tileEntity.zCoord -= offsetZ;
        } catch (Exception e) {
            throw new TileEntityException(tileEntity, e);
        }

        return tileEntity;
    }
}
