package com.github.lunatrius.schematica.world.schematic;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.structure.template.Template;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.nbt.NBTHelper;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.storage.Schematic;

public class SchematicStructure extends SchematicFormat {
    @Override
    public ISchematic readFromNBT(final NBTTagCompound tagCompound) {
        final ItemStack icon = SchematicUtil.getIconFromNBT(tagCompound);

        final Template template = new Template();
        template.read(tagCompound);

        final Schematic schematic = new Schematic(icon,
                template.size.getX(), template.size.getY(), template.size.getZ(), template.getAuthor());

        for (Template.BlockInfo block : template.blocks) {
            schematic.setBlockState(block.pos, block.blockState);
            if (block.tileentityData != null) {
                try {
                    // This position isn't included by default
                    block.tileentityData.setInteger("x", block.pos.getX());
                    block.tileentityData.setInteger("y", block.pos.getY());
                    block.tileentityData.setInteger("z", block.pos.getZ());

                    final TileEntity tileEntity = NBTHelper.readTileEntityFromCompound(block.tileentityData);
                    if (tileEntity != null) {
                        schematic.setTileEntity(block.pos, tileEntity);
                    }
                } catch (final Exception e) {
                    Reference.logger.error("TileEntity failed to load properly!", e);
                }
            }
        }

        // for (Template.EntityInfo entity : template.entities) {
        //     schematic.addEntity(...);
        // }

        return schematic;
    }

    @Override
    public boolean writeToNBT(final NBTTagCompound tagCompound, final ISchematic schematic) {
        Template template = new Template();
        template.size = new BlockPos(schematic.getWidth(), schematic.getHeight(), schematic.getLength());

        template.setAuthor(schematic.getAuthor());

        // NOTE: Can't use MutableBlockPos here because we're keeping a reference to it in BlockInfo
        for (BlockPos pos : BlockPos.getAllInBox(BlockPos.ORIGIN, template.size.add(-1, -1, -1))) {
            final TileEntity tileEntity = schematic.getTileEntity(pos);
            final NBTTagCompound compound;
            if (tileEntity != null) {
                compound = NBTHelper.writeTileEntityToCompound(tileEntity);
                // Tile entities in structures don't store these coords
                compound.removeTag("x");
                compound.removeTag("y");
                compound.removeTag("z");
            } else {
                compound = null;
            }

            template.blocks.add(new Template.BlockInfo(pos, schematic.getBlockState(pos), compound));
        }

        for (Entity entity : schematic.getEntities()) {
            try {
                // Entity positions are already offset via NBTHelper.reloadEntity
                Vec3d vec3d = new Vec3d(entity.posX, entity.posY, entity.posZ);
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                entity.writeToNBTOptional(nbttagcompound);
                BlockPos blockpos;

                // TODO: Vanilla has a check like this, but we don't; this doesn't seem to
                // cause any problems though.
                // if (entity instanceof EntityPainting) {
                //     blockpos = ((EntityPainting)entity).getHangingPosition().subtract(startPos);
                // } else {
                blockpos = new BlockPos(vec3d);
                // }

                template.entities.add(new Template.EntityInfo(vec3d, blockpos, nbttagcompound));
            } catch (final Throwable t) {
                Reference.logger.error("Entity {} failed to save, skipping!", entity, t);
            }
        }

        template.writeToNBT(tagCompound);
        return true;
    }

    @Override
    public String getName() {
        return Names.Formats.STRUCTURE;
    }

    @Override
    public String getExtension() {
        return Names.Extensions.STRUCTURE;
    }
}
