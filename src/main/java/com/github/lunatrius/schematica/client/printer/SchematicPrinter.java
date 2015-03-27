package com.github.lunatrius.schematica.client.printer;

import com.github.lunatrius.core.util.MBlockPos;
import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.schematica.client.printer.registry.PlacementData;
import com.github.lunatrius.schematica.client.printer.registry.PlacementRegistry;
import com.github.lunatrius.schematica.client.util.BlockStateToItemStack;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fluids.BlockFluidBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SchematicPrinter {
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

    private boolean placeBlock(final WorldClient world, final EntityPlayerSP player, final BlockPos pos) {
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

        final ItemStack itemStack = BlockStateToItemStack.getItemStack(blockState, new MovingObjectPosition(player), this.schematic, pos);
        if (itemStack == null || itemStack.getItem() == null) {
            Reference.logger.debug("{} is missing a mapping!", blockState);
            return false;
        }

        if (placeBlock(world, player, realPos, blockState, itemStack)) {
            this.timeout[x][y][z] = (byte) ConfigurationHandler.timeout;

            if (!ConfigurationHandler.placeInstantly) {
                return true;
            }
        }

        return false;
    }

    private boolean isSolid(final World world, final BlockPos pos, final EnumFacing side) {
        final BlockPos offset = new BlockPos(pos).offset(side);

        final IBlockState blockState = world.getBlockState(offset);
        final Block block = blockState.getBlock();

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

    private List<EnumFacing> getSolidSides(final World world, final BlockPos pos) {
        if (!ConfigurationHandler.placeAdjacent) {
            return Arrays.asList(EnumFacing.VALUES);
        }

        final List<EnumFacing> list = new ArrayList<EnumFacing>();

        for (final EnumFacing side : EnumFacing.VALUES) {
            if (isSolid(world, pos, side)) {
                list.add(side);
            }
        }

        return list;
    }

    private boolean placeBlock(final WorldClient world, final EntityPlayerSP player, final BlockPos pos, final IBlockState blockState, final ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemBucket || itemStack.getItem() == Items.sign) {
            return false;
        }

        final PlacementData data = PlacementRegistry.INSTANCE.getPlacementData(blockState, itemStack);
        if (data != null && !data.isValidPlayerFacing(blockState, player, pos, world)) {
            return false;
        }

        final List<EnumFacing> solidSides = getSolidSides(world, pos);

        if (solidSides.size() == 0) {
            return false;
        }

        final EnumFacing direction;
        final float offsetX;
        final float offsetY;
        final float offsetZ;

        if (data != null) {
            final List<EnumFacing> validDirections = data.getValidBlockFacings(solidSides, blockState);
            if (validDirections.size() == 0) {
                return false;
            }

            direction = validDirections.get(0);
            offsetX = data.getOffsetX(blockState);
            offsetY = data.getOffsetY(blockState);
            offsetZ = data.getOffsetZ(blockState);
        } else {
            direction = solidSides.get(0);
            offsetX = 0.5f;
            offsetY = 0.5f;
            offsetZ = 0.5f;
        }

        if (!swapToItem(player.inventory, itemStack)) {
            return false;
        }

        return placeBlock(world, player, pos, direction, offsetX, offsetY, offsetZ);

    }

    private boolean placeBlock(final WorldClient world, final EntityPlayerSP player, final BlockPos pos, final EnumFacing direction, final float offsetX, final float offsetY, final float offsetZ) {
        final ItemStack itemStack = player.getCurrentEquippedItem();
        boolean success = false;

        final BlockPos offset = new BlockPos(pos).offset(direction);
        final EnumFacing side = direction.getOpposite();

        success = !ForgeEventFactory.onPlayerInteract(player, Action.RIGHT_CLICK_BLOCK, world, offset, side).isCanceled();
        if (success) {
            success = this.minecraft.playerController.onPlayerRightClick(player, world, itemStack, offset, side, new Vec3(offset.getX() + offsetX, offset.getY() + offsetY, offset.getZ() + offsetZ));
            if (success) {
                player.swingItem();
            }
        }


        if (itemStack != null && itemStack.stackSize == 0 && success) {
            player.inventory.mainInventory[player.inventory.currentItem] = null;
        }

        return success;
    }

    private void syncSneaking(final EntityPlayerSP player, final boolean isSneaking) {
        player.setSneaking(isSneaking);
        player.sendQueue.addToSendQueue(new C0BPacketEntityAction(player, isSneaking ? C0BPacketEntityAction.Action.START_SNEAKING : C0BPacketEntityAction.Action.STOP_SNEAKING));
    }

    private boolean swapToItem(final InventoryPlayer inventory, final ItemStack itemStack) {
        return swapToItem(inventory, itemStack, true);
    }

    private boolean swapToItem(final InventoryPlayer inventory, final ItemStack itemStack, final boolean swapSlots) {
        final int slot = getInventorySlotWithItem(inventory, itemStack);

        if (this.minecraft.playerController.isInCreativeMode() && (slot < Constants.Inventory.InventoryOffset.HOTBAR || slot >= Constants.Inventory.InventoryOffset.HOTBAR + Constants.Inventory.Size.HOTBAR) && ConfigurationHandler.swapSlotsQueue.size() > 0) {
            inventory.currentItem = getNextSlot();
            inventory.setInventorySlotContents(inventory.currentItem, itemStack.copy());
            this.minecraft.playerController.sendSlotPacket(inventory.getStackInSlot(inventory.currentItem), Constants.Inventory.SlotOffset.HOTBAR + inventory.currentItem);
            return true;
        }

        if (slot >= Constants.Inventory.InventoryOffset.HOTBAR && slot < Constants.Inventory.InventoryOffset.HOTBAR + Constants.Inventory.Size.HOTBAR) {
            inventory.currentItem = slot;
            return true;
        } else if (swapSlots && slot >= Constants.Inventory.InventoryOffset.INVENTORY && slot < Constants.Inventory.InventoryOffset.INVENTORY + Constants.Inventory.Size.INVENTORY) {
            if (swapSlots(inventory, slot)) {
                return swapToItem(inventory, itemStack, false);
            }
        }

        return false;
    }

    private int getInventorySlotWithItem(final InventoryPlayer inventory, final ItemStack itemStack) {
        for (int i = 0; i < inventory.mainInventory.length; i++) {
            if (inventory.mainInventory[i] != null && inventory.mainInventory[i].isItemEqual(itemStack)) {
                return i;
            }
        }
        return -1;
    }

    private boolean swapSlots(final InventoryPlayer inventory, final int from) {
        if (ConfigurationHandler.swapSlotsQueue.size() > 0) {
            final int slot = getNextSlot();

            final ItemStack itemStack = inventory.mainInventory[slot + Constants.Inventory.InventoryOffset.HOTBAR];
            swapSlots(from, slot, itemStack == null || itemStack.stackSize == 0);
            return true;
        }

        return false;
    }

    private int getNextSlot() {
        final int slot = ConfigurationHandler.swapSlotsQueue.poll() % Constants.Inventory.Size.HOTBAR;
        ConfigurationHandler.swapSlotsQueue.offer(slot);
        return slot;
    }

    private boolean swapSlots(int from, int to, final boolean targetEmpty) {
        if (from >= Constants.Inventory.InventoryOffset.HOTBAR && from < Constants.Inventory.InventoryOffset.HOTBAR + Constants.Inventory.Size.HOTBAR) {
            from = Constants.Inventory.SlotOffset.HOTBAR + (from - Constants.Inventory.InventoryOffset.HOTBAR);
        } else if (from >= Constants.Inventory.InventoryOffset.INVENTORY && from < Constants.Inventory.InventoryOffset.INVENTORY + Constants.Inventory.Size.INVENTORY) {
            from = Constants.Inventory.SlotOffset.INVENTORY + (from - Constants.Inventory.InventoryOffset.INVENTORY);
        } else {
            return false;
        }

        if (to >= Constants.Inventory.InventoryOffset.HOTBAR && to < Constants.Inventory.InventoryOffset.HOTBAR + Constants.Inventory.Size.HOTBAR) {
            to = Constants.Inventory.SlotOffset.HOTBAR + (to - Constants.Inventory.InventoryOffset.HOTBAR);
        } else if (to >= Constants.Inventory.InventoryOffset.INVENTORY && to < Constants.Inventory.InventoryOffset.INVENTORY + Constants.Inventory.Size.INVENTORY) {
            to = Constants.Inventory.SlotOffset.INVENTORY + (to - Constants.Inventory.InventoryOffset.INVENTORY);
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

    private ItemStack clickSlot(final int slot) {
        return this.minecraft.playerController.windowClick(this.minecraft.thePlayer.inventoryContainer.windowId, slot, 0, 0, this.minecraft.thePlayer);
    }
}
