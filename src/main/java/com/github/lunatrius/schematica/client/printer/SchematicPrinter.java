package com.github.lunatrius.schematica.client.printer;

import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.schematica.client.printer.registry.PlacementData;
import com.github.lunatrius.schematica.client.printer.registry.PlacementRegistry;
import com.github.lunatrius.schematica.client.util.BlockToItemStack;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Reference;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.ArrayList;
import java.util.List;

public class SchematicPrinter {
    public static final SchematicPrinter INSTANCE = new SchematicPrinter();
    public static final FMLControlledNamespacedRegistry<Block> BLOCK_REGISTRY = GameData.getBlockRegistry();

    private final Minecraft minecraft = Minecraft.getMinecraft();

    private boolean isEnabled;
    private boolean isPrinting;

    private SchematicWorld schematic = null;
    private byte[][][] timeout = null;

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean togglePrinting() {
        this.isPrinting = !this.isPrinting;
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
        final EntityClientPlayerMP player = this.minecraft.thePlayer;
        final World world = this.minecraft.theWorld;

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

        final boolean isRenderingLayer = this.schematic.isRenderingLayer;
        final int renderingLayer = this.schematic.renderingLayer;
        for (int y = minY; y < maxY; y++) {
            if (isRenderingLayer && y != renderingLayer) {
                continue;
            }

            for (int x = minX; x < maxX; x++) {
                for (int z = minZ; z < maxZ; z++) {
                    try {
                        if (placeBlock(world, player, x, y, z)) {
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

    private boolean placeBlock(World world, EntityPlayer player, int x, int y, int z) {
        if (this.timeout[x][y][z] > 0) {
            this.timeout[x][y][z] -= ConfigurationHandler.placeDelay;
            return false;
        }

        final int wx = this.schematic.position.x + x;
        final int wy = this.schematic.position.y + y;
        final int wz = this.schematic.position.z + z;

        final Block block = this.schematic.getBlock(x, y, z);
        final Block realBlock = world.getBlock(wx, wy, wz);
        final int metadata = this.schematic.getBlockMetadata(x, y, z);
        final int realMetadata = world.getBlockMetadata(wx, wy, wz);

        if (block == realBlock && metadata == realMetadata) {
            return false;
        }

        if (ConfigurationHandler.destroyBlocks && !world.isAirBlock(wx, wy, wz) && this.minecraft.playerController.isInCreativeMode()) {
            this.minecraft.playerController.clickBlock(wx, wy, wz, 0);

            this.timeout[x][y][z] = (byte) ConfigurationHandler.timeout;

            return !ConfigurationHandler.destroyInstantly;
        }

        if (this.schematic.isAirBlock(x, y, z)) {
            return false;
        }

        if (!realBlock.isReplaceable(world, wx, wy, wz)) {
            return false;
        }

        final ItemStack itemStack = BlockToItemStack.getItemStack(player, block, this.schematic, x, y, z);
        if (itemStack == null || itemStack.getItem() == null) {
            Reference.logger.debug("{} is missing a mapping!", BLOCK_REGISTRY.getNameForObject(block));
            return false;
        }

        if (placeBlock(this.minecraft, world, player, wx, wy, wz, block, metadata, itemStack)) {
            this.timeout[x][y][z] = (byte) ConfigurationHandler.timeout;

            if (!ConfigurationHandler.placeInstantly) {
                return true;
            }
        }

        return false;
    }

    private boolean isSolid(World world, int x, int y, int z, ForgeDirection side) {
        x += side.offsetX;
        y += side.offsetY;
        z += side.offsetZ;

        Block block = world.getBlock(x, y, z);

        if (block == null) {
            return false;
        }

        if (block.isAir(world, x, y, z)) {
            return false;
        }

        if (block instanceof BlockFluidBase) {
            return false;
        }

        if (block.isReplaceable(world, x, y, z)) {
            return false;
        }

        return true;
    }

    private ForgeDirection[] getSolidSides(World world, int x, int y, int z) {
        List<ForgeDirection> list = new ArrayList<ForgeDirection>();

        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            if (isSolid(world, x, y, z, side)) {
                list.add(side);
            }
        }

        ForgeDirection[] sides = new ForgeDirection[list.size()];
        return list.toArray(sides);
    }

    private boolean placeBlock(Minecraft minecraft, World world, EntityPlayer player, int x, int y, int z, Block block, int metadata, ItemStack itemStack) {
        if (isBlacklisted(block, itemStack)) {
            return false;
        }

        PlacementData data = PlacementRegistry.INSTANCE.getPlacementData(block, itemStack);

        if (!isValidOrientation(player, x, y, z, data, metadata)) {
            return false;
        }

        ForgeDirection[] solidSides = getSolidSides(world, x, y, z);
        ForgeDirection direction = ForgeDirection.UNKNOWN;
        float offsetY = 0.0f;

        if (solidSides.length > 0) {
            if (data != null) {
                ForgeDirection[] validDirections = data.getValidDirections(solidSides, metadata);
                if (validDirections.length > 0) {
                    direction = validDirections[0];
                }

                offsetY = data.getOffsetFromMetadata(metadata);
            } else {
                direction = solidSides[0];
            }

            if (!swapToItem(player.inventory, itemStack)) {
                return false;
            }
        }

        if (direction != ForgeDirection.UNKNOWN || !ConfigurationHandler.placeAdjacent) {
            return placeBlock(minecraft, world, player, x, y, z, direction, 0.0f, offsetY, 0.0f);
        }

        return false;
    }

    private boolean isBlacklisted(Block block, ItemStack itemStack) {
        if (block instanceof IFluidBlock || block instanceof BlockLiquid) {
            return true;
        }

        if (itemStack.getItem() instanceof ItemBucket) {
            return true;
        }

        if (itemStack.getItem() == Items.sign) {
            return true;
        }

        return false;
    }

    private boolean isValidOrientation(EntityPlayer player, int x, int y, int z, PlacementData data, int metadata) {
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
                    return BlockPistonBase.determineOrientation(null, x, y, z, player) == BlockPistonBase.getPistonOrientation(metadata);
                }
                break;
            }
            }
            return false;
        }

        return true;
    }

    private boolean placeBlock(Minecraft minecraft, World world, EntityPlayer player, int x, int y, int z, ForgeDirection direction, float offsetX, float offsetY, float offsetZ) {
        ItemStack itemStack = player.getCurrentEquippedItem();
        boolean success = false;

        x += direction.offsetX;
        y += direction.offsetY;
        z += direction.offsetZ;

        int side = direction.getOpposite().ordinal();

		/* copypasted from n.m.client.Minecraft to sooth finicky servers */
        success = !ForgeEventFactory.onPlayerInteract(minecraft.thePlayer, Action.RIGHT_CLICK_BLOCK, x, y, z, side, world).isCanceled();
        if (success) {
            // still not assured!
            success = minecraft.playerController.onPlayerRightClick(player, world, itemStack, x, y, z, side, Vec3.createVectorHelper(x + offsetX, y + offsetY, z + offsetZ));
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

    private void syncSneaking(EntityClientPlayerMP player, boolean isSneaking) {
        player.setSneaking(isSneaking);
        player.sendQueue.addToSendQueue(new C0BPacketEntityAction(player, isSneaking ? 1 : 2));
    }

    private boolean swapToItem(InventoryPlayer inventory, ItemStack itemStack) {
        return swapToItem(inventory, itemStack, true);
    }

    private boolean swapToItem(InventoryPlayer inventory, ItemStack itemStack, boolean swapSlots) {
        int slot = getInventorySlotWithItem(inventory, itemStack);

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

    private boolean swapSlots(InventoryPlayer inventory, int from) {
        if (ConfigurationHandler.swapSlotsQueue.size() > 0) {
            int slot = getNextSlot();

            swapSlots(from, slot);
            return true;
        }

        return false;
    }

    private int getNextSlot() {
        int slot = ConfigurationHandler.swapSlotsQueue.poll() % Constants.Inventory.Size.HOTBAR;
        ConfigurationHandler.swapSlotsQueue.offer(slot);
        return slot;
    }

    private boolean swapSlots(final int from, final int to) {
        return this.minecraft.playerController.windowClick(this.minecraft.thePlayer.inventoryContainer.windowId, from, to, 2, this.minecraft.thePlayer) == null;
    }
}
