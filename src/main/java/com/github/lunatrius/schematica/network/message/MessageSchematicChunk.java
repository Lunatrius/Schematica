package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.nbt.NBTHelper;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.world.SchematicWorld;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

public class MessageSchematicChunk implements IMessage, IMessageHandler<MessageSchematicChunk, IMessage> {
    public int baseX;
    public int baseY;
    public int baseZ;

    public short blocks[][][];
    public byte metadata[][][];
    public List<TileEntity> tileEntities;
    public List<Entity> entities;

    public MessageSchematicChunk() {
    }

    public MessageSchematicChunk(SchematicWorld schematic, int baseX, int baseY, int baseZ) {
        this.baseX = baseX;
        this.baseY = baseY;
        this.baseZ = baseZ;

        this.blocks = new short[Constants.SchematicChunk.WIDTH][Constants.SchematicChunk.HEIGHT][Constants.SchematicChunk.LENGTH];
        this.metadata = new byte[Constants.SchematicChunk.WIDTH][Constants.SchematicChunk.HEIGHT][Constants.SchematicChunk.LENGTH];
        this.tileEntities = new ArrayList<TileEntity>();
        this.entities = new ArrayList<Entity>();

        for (int x = 0; x < Constants.SchematicChunk.WIDTH; x++) {
            for (int y = 0; y < Constants.SchematicChunk.HEIGHT; y++) {
                for (int z = 0; z < Constants.SchematicChunk.LENGTH; z++) {
                    this.blocks[x][y][z] = (short) schematic.getBlockIdRaw(baseX + x, baseY + y, baseZ + z);
                    this.metadata[x][y][z] = (byte) schematic.getBlockMetadata(baseX + x, baseY + y, baseZ + z);
                    final TileEntity tileEntity = schematic.getTileEntity(baseX + x, baseY + y, baseZ + z);
                    if (tileEntity != null) {
                        this.tileEntities.add(tileEntity);
                    }
                }
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.baseX = buf.readShort();
        this.baseY = buf.readShort();
        this.baseZ = buf.readShort();

        this.blocks = new short[Constants.SchematicChunk.WIDTH][Constants.SchematicChunk.HEIGHT][Constants.SchematicChunk.LENGTH];
        this.metadata = new byte[Constants.SchematicChunk.WIDTH][Constants.SchematicChunk.HEIGHT][Constants.SchematicChunk.LENGTH];
        this.tileEntities = new ArrayList<TileEntity>();
        this.entities = new ArrayList<Entity>();

        for (int x = 0; x < Constants.SchematicChunk.WIDTH; x++) {
            for (int y = 0; y < Constants.SchematicChunk.HEIGHT; y++) {
                for (int z = 0; z < Constants.SchematicChunk.LENGTH; z++) {
                    this.blocks[x][y][z] = buf.readShort();
                    this.metadata[x][y][z] = buf.readByte();
                }
            }
        }

        final NBTTagCompound compound = ByteBufUtils.readTag(buf);
        this.tileEntities = NBTHelper.readTileEntitiesFromCompound(compound, this.tileEntities);

        final NBTTagCompound compound2 = ByteBufUtils.readTag(buf);
        this.entities = NBTHelper.readEntitiesFromCompound(compound2, this.entities);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(this.baseX);
        buf.writeShort(this.baseY);
        buf.writeShort(this.baseZ);

        for (int x = 0; x < Constants.SchematicChunk.WIDTH; x++) {
            for (int y = 0; y < Constants.SchematicChunk.HEIGHT; y++) {
                for (int z = 0; z < Constants.SchematicChunk.LENGTH; z++) {
                    buf.writeShort(this.blocks[x][y][z]);
                    buf.writeByte(this.metadata[x][y][z]);
                }
            }
        }

        final NBTTagCompound compound = NBTHelper.writeTileEntitiesToCompound(this.tileEntities);
        ByteBufUtils.writeTag(buf, compound);

        final NBTTagCompound compound1 = NBTHelper.writeEntitiesToCompound(this.entities);
        ByteBufUtils.writeTag(buf, compound1);
    }

    @Override
    public IMessage onMessage(MessageSchematicChunk message, MessageContext ctx) {
        // TODO: implement
        return null;
    }
}
