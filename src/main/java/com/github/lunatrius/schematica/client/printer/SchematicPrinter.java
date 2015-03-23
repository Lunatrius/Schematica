package com.github.lunatrius.schematica.client.printer;

import com.github.lunatrius.core.util.MBlockPos;
import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.config.BlockInfo;
import com.github.lunatrius.schematica.config.PlacementData;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.common.registry.GameData;

import java.util.ArrayList;
import java.util.List;

public class SchematicPrinter {
    public static final int WILDCARD_METADATA = -1;
    public static final int SIZE_CRAFTING_OUT = 1;
    public static final int SIZE_CRAFTING_IN = 4;
    public static final int SIZE_ARMOR = 4;
    public static final int SIZE_INVENTORY = 3 * 9;
    public static final int SIZE_HOTBAR = 9;

    public static final int SLOT_OFFSET_CRAFTING_OUT = 0;
    public static final int SLOT_OFFSET_CRAFTING_IN = SLOT_OFFSET_CRAFTING_OUT + SIZE_CRAFTING_OUT;
    public static final int SLOT_OFFSET_ARMOR = SLOT_OFFSET_CRAFTING_IN + SIZE_CRAFTING_IN;
    public static final int SLOT_OFFSET_INVENTORY = SLOT_OFFSET_ARMOR + SIZE_ARMOR;
    public static final int SLOT_OFFSET_HOTBAR = SLOT_OFFSET_INVENTORY + SIZE_INVENTORY;

    public static final int INV_OFFSET_HOTBAR = 0;
    public static final int INV_OFFSET_INVENTORY = INV_OFFSET_HOTBAR + 9;

    public static final SchematicPrinter INSTANCE = new SchematicPrinter();

    private final Minecraft minecraft = Minecraft.getMinecraft();

    private boolean isEnabled = true;
    private boolean isPrinting = false;

    private SchematicWorld schematic = null;
    private byte[][][] timeout = null;

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean togglePrinting() {
        this.isPrinting = !this.isPrinting && this.schematic != null;
        return this.isPrinting;
    }

    public boolean isPrinting() {
        return this.isPrinting;
    }

    public void setPrinting(boolean isPrinting) {
        this.isPrinting = isPrinting;
    }

    public SchematicWorld getSchematic() {
        return this.schematic;
    }

    public void setSchematic(SchematicWorld schematic) {
        this.isPrinting = false;
        this.schematic = schematic;
        refresh();
    }

    public void refresh() {
        if (this.schematic != null) {
            this.timeout = new byte[this.schematic.getWidth()][this.schematic.getHeight()][this.schematic.getLength()];
        } else {
            this.timeout = null;
        }
    }

    public boolean print() {
        final EntityPlayerSP player = this.minecraft.thePlayer;
        final WorldClient world = this.minecraft.theWorld;

        syncSneaking(player, true);

        final Vector3i trans = ClientProxy.playerPosition.clone().sub(this.schematic.position.x, this.schematic.position.y, this.schematic.position.z).toVector3i();
        final int minX = Math.max(0, trans.x - 3);
        final int maxX = Math.min(this.schematic.getWidth(), trans.x + 4);
        final int minY = Math.max(0, trans.y - 3);
        final int maxY = Math.min(this.schematic.getHeight(), trans.y + 4);
        final int minZ = Math.max(0, trans.z - 3);
        final int maxZ = Math.min(this.schematic.getLength(), trans.z + 4);

        final int slot = player.inventory.currentItem;
        final boolean isSneaking = player.isSneaking();

        final MBlockPos pos = new MBlockPos();

        final boolean isRenderingLayer = this.schematic.isRenderingLayer;
        final int renderingLayer = this.schematic.renderingLayer;
        for (pos.y = minY; pos.y < maxY; pos.y++) {
            if (isRenderingLayer && pos.y != renderingLayer) {
                continue;
            }

            for (pos.x = minX; pos.x < maxX; pos.x++) {
                for (pos.z = minZ; pos.z < maxZ; pos.z++) {
                    try {
                        if (placeBlock(world, player, pos)) {
                            player.inventory.currentItem = slot;
                            syncSneaking(player, isSneaking);
                            return true;
                        }
                    } catch (Exception e) {
                        Reference.logger.error("Could not place block!", e);
                        player.inventory.currentItem = slot;
                        syncSneaking(player, isSneaking);
                        return false;
                    }
                }
            }
        }

        player.inventory.currentItem = slot;
        syncSneaking(player, isSneaking);
        return true;
    }

    private boolean placeBlock(WorldClient world, EntityPlayerSP player, BlockPos pos) {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        if (this.timeout[x][y][z] > 0) {
            this.timeout[x][y][z] -= ConfigurationHandler.placeDelay;
            return false;
        }

        final int wx = this.schematic.position.x + x;
        final int wy = this.schematic.position.y + y;
        final int wz = this.schematic.position.z + z;
        final BlockPos realPos = new BlockPos(wx, wy, wz);

        final IBlockState blockState = this.schematic.getBlockState(pos);
        final Block block = blockState.getBlock();
        final int metadata = block.getMetaFromState(blockState);

        final IBlockState realBlockState = world.getBlockState(realPos);
        final Block realBlock = realBlockState.getBlock();
        final int realMetadata = realBlock.getMetaFromState(realBlockState);

        // TODO: compare block states directly?
        if (block == realBlock && metadata == realMetadata) {
            return false;
        }

        if (ConfigurationHandler.destroyBlocks && !world.isAirBlock(realPos) && this.minecraft.playerController.isInCreativeMode()) {
            this.minecraft.playerController.clickBlock(realPos, EnumFacing.DOWN);

            this.timeout[x][y][z] = (byte) ConfigurationHandler.timeout;

            return !ConfigurationHandler.destroyInstantly;
        }

        if (this.schematic.isAirBlock(pos)) {
            return false;
        }

        if (!realBlock.isReplaceable(world, realPos)) {
            return false;
        }

        final Item item = BlockInfo.getItemFromBlock(block);
        if (item == null) {
            Reference.logger.debug(GameData.getBlockRegistry().getNameForObject(block) + " is missing a mapping!");
            return false;
        }

        if (placeBlock(this.minecraft, world, player, realPos, item, metadata)) {
            this.timeout[x][y][z] = (byte) ConfigurationHandler.timeout;

            if (!ConfigurationHandler.placeInstantly) {
                return true;
            }
        }

        return false;
    }

    private boolean isSolid(World world, BlockPos pos, EnumFacing side) {
        final BlockPos offset = new BlockPos(pos).offset(side);

        final IBlockState blockState = world.getBlockState(offset);
        Block block = blockState.getBlock();

        if (block == null) {
            return false;
        }

        if (block.isAir(world, offset)) {
            return false;
        }

        if (block instanceof BlockFluidBase) {
            return false;
        }

        if (block.isReplaceable(world, offset)) {
            return false;
        }

        return true;
    }

    private EnumFacing[] getSolidSides(World world, BlockPos pos) {
        List<EnumFacing> list = new ArrayList<EnumFacing>();

        for (EnumFacing side : EnumFacing.values()) {
            if (isSolid(world, pos, side)) {
                list.add(side);
            }
        }

        EnumFacing[] sides = new EnumFacing[list.size()];
        return list.toArray(sides);
    }

    private boolean placeBlock(Minecraft minecraft, WorldClient world, EntityPlayerSP player, BlockPos pos, Item item, int itemDamage) {
        if (item instanceof ItemBucket || item == Items.sign) {
            return false;
        }

        PlacementData data = BlockInfo.getPlacementDataFromItem(item);

        if (!isValidOrientation(player, pos, data, itemDamage)) {
            return false;
        }

        EnumFacing[] solidSides = getSolidSides(world, pos);
        EnumFacing direction = null;
        float offsetY = 0.0f;

        if (solidSides.length > 0) {
            int metadata = WILDCARD_METADATA;

            if (data != null) {
                EnumFacing[] validDirections = data.getValidDirections(solidSides, itemDamage);
                if (validDirections.length > 0) {
                    direction = validDirections[0];
                }

                offsetY = data.getOffsetFromMetadata(itemDamage);

                if (data.maskMetaInHand != -1) {
                    metadata = data.getMetaInHand(itemDamage);
                }
            } else {
                direction = solidSides[0];
            }

            if (!swapToItem(player.inventory, item, metadata)) {
                return false;
            }
        }

        if (direction != null || !ConfigurationHandler.placeAdjacent) {
            return placeBlock(minecraft, world, player, pos, direction, 0.0f, offsetY, 0.0f);
        }

        return false;
    }

    private boolean isValidOrientation(EntityPlayer player, BlockPos pos, PlacementData data, int metadata) {
        if (data != null) {
            switch (data.type) {
            case BLOCK: {
                return true;
            }

            case PLAYER: {
                Integer integer = data.mapping.get(ClientProxy.orientation);
                if (integer != null) {
                    return integer == (metadata & data.maskMeta);
                }
                break;
            }

            case PISTON: {
                Integer integer = data.mapping.get(ClientProxy.orientation);
                if (integer != null) {
                    return BlockPistonBase.getFacingFromEntity(null, pos, player) == BlockPistonBase.getFacing(metadata);
                }
                break;
            }
            }
            return false;
        }

        return true;
    }

    private boolean placeBlock(Minecraft minecraft, WorldClient world, EntityPlayerSP player, BlockPos pos, EnumFacing direction, float offsetX, float offsetY, float offsetZ) {
        ItemStack itemStack = player.getCurrentEquippedItem();
        boolean success = false;

        final BlockPos offset = new BlockPos(pos).offset(direction);

        EnumFacing side = direction.getOpposite();

        // copypasted from n.m.client.Minecraft to sooth finicky servers
        success = !ForgeEventFactory.onPlayerInteract(minecraft.thePlayer, Action.RIGHT_CLICK_BLOCK, world, offset, side).isCanceled();
        if (success) {
            // still not assured!
            success = minecraft.playerController.onPlayerRightClick(player, world, itemStack, offset, side, new Vec3(offset.getX() + offsetX, offset.getY() + offsetY, offset.getZ() + offsetZ));
            if (success) {
                // yes, some servers actually care about this.
                minecraft.thePlayer.swingItem();
            }
        }

        if (itemStack != null && itemStack.stackSize == 0 && success) {
            player.inventory.mainInventory[player.inventory.currentItem] = null;
        }

        return success;
    }

    private void syncSneaking(EntityPlayerSP player, boolean isSneaking) {
        player.setSneaking(isSneaking);
        player.sendQueue.addToSendQueue(new C0BPacketEntityAction(player, isSneaking ? C0BPacketEntityAction.Action.START_SNEAKING : C0BPacketEntityAction.Action.STOP_SNEAKING));
    }

    private boolean swapToItem(InventoryPlayer inventory, Item item, int itemDamage) {
        return swapToItem(inventory, item, itemDamage, true);
    }

    private boolean swapToItem(InventoryPlayer inventory, Item item, int itemDamage, boolean swapSlots) {
        int slot = getInventorySlotWithItem(inventory, item, itemDamage);

        if (this.minecraft.playerController.isInCreativeMode() && (slot < INV_OFFSET_HOTBAR || slot >= INV_OFFSET_HOTBAR + SIZE_HOTBAR) && ConfigurationHandler.swapSlotsQueue.size() > 0) {
            inventory.currentItem = getNextSlot();
            inventory.setInventorySlotContents(inventory.currentItem, new ItemStack(item, 1, itemDamage));
            this.minecraft.playerController.sendSlotPacket(inventory.getStackInSlot(inventory.currentItem), SLOT_OFFSET_HOTBAR + inventory.currentItem);
            return true;
        }

        if (slot >= INV_OFFSET_HOTBAR && slot < INV_OFFSET_HOTBAR + SIZE_HOTBAR) {
            inventory.currentItem = slot;
            return true;
        } else if (swapSlots && slot >= INV_OFFSET_INVENTORY && slot < INV_OFFSET_INVENTORY + SIZE_INVENTORY) {
            if (swapSlots(inventory, slot)) {
                return swapToItem(inventory, item, itemDamage, false);
            }
        }
        return false;
    }

    private int getInventorySlotWithItem(InventoryPlayer inventory, Item item, int itemDamage) {
        for (int i = 0; i < inventory.mainInventory.length; i++) {
            if (inventory.mainInventory[i] != null && inventory.mainInventory[i].getItem() == item && (itemDamage == WILDCARD_METADATA || inventory.mainInventory[i].getItemDamage() == itemDamage)) {
                return i;
            }
        }
        return -1;
    }

    private boolean swapSlots(InventoryPlayer inventory, int from) {
        if (ConfigurationHandler.swapSlotsQueue.size() > 0) {
            int slot = getNextSlot();

            ItemStack itemStack = inventory.mainInventory[slot + INV_OFFSET_HOTBAR];
            swapSlots(from, slot, itemStack == null || itemStack.stackSize == 0);
            return true;
        }

        return false;
    }

    private int getNextSlot() {
        int slot = ConfigurationHandler.swapSlotsQueue.poll() % SIZE_HOTBAR;
        ConfigurationHandler.swapSlotsQueue.offer(slot);
        return slot;
    }

    private boolean swapSlots(int from, int to, boolean targetEmpty) {
        if (from >= INV_OFFSET_HOTBAR && from < INV_OFFSET_HOTBAR + SIZE_HOTBAR) {
            from = SLOT_OFFSET_HOTBAR + (from - INV_OFFSET_HOTBAR);
        } else if (from >= INV_OFFSET_INVENTORY && from < INV_OFFSET_INVENTORY + SIZE_INVENTORY) {
            from = SLOT_OFFSET_INVENTORY + (from - INV_OFFSET_INVENTORY);
        } else {
            return false;
        }

        if (to >= INV_OFFSET_HOTBAR && to < INV_OFFSET_HOTBAR + SIZE_HOTBAR) {
            to = SLOT_OFFSET_HOTBAR + (to - INV_OFFSET_HOTBAR);
        } else if (to >= INV_OFFSET_INVENTORY && to < INV_OFFSET_INVENTORY + SIZE_INVENTORY) {
            to = SLOT_OFFSET_INVENTORY + (to - INV_OFFSET_INVENTORY);
        } else {
            return false;
        }

        clickSlot(from);
        clickSlot(to);
        if (!targetEmpty) {
            clickSlot(from);
        }

        return true;
    }

    private ItemStack clickSlot(int slot) {
        return this.minecraft.playerController.windowClick(this.minecraft.thePlayer.inventoryContainer.windowId, slot, 0, 0, this.minecraft.thePlayer);
    }
}
