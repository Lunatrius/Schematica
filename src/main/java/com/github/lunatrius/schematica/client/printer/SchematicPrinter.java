package com.github.lunatrius.schematica.client.printer;

import com.github.lunatrius.core.util.math.BlockPosHelper;
import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.block.state.BlockStateHelper;
import com.github.lunatrius.schematica.client.printer.nbtsync.NBTSync;
import com.github.lunatrius.schematica.client.printer.nbtsync.SyncRegistry;
import com.github.lunatrius.schematica.client.printer.registry.PlacementData;
import com.github.lunatrius.schematica.client.printer.registry.PlacementRegistry;
import com.github.lunatrius.schematica.client.util.BlockStateToItemStack;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.*;

public class SchematicPrinter {
    public static final SchematicPrinter INSTANCE = new SchematicPrinter();

    private final Minecraft minecraft = Minecraft.getMinecraft();

    private boolean isEnabled = true;
    private boolean isPrinting = false;

    private SchematicWorld schematic = null;
    private byte[][][] timeout = null;
    private HashMap<BlockPos, Integer> syncBlacklist = new HashMap<BlockPos, Integer>();
    private List<Vec3d> rollingVel = new ArrayList<>();
    private int rollingPos = 0;
    private Vec3d averageVelocity = new Vec3d(0,0,0);


    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void setEnabled(final boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean togglePrinting() {
        this.isPrinting = !this.isPrinting && this.schematic != null;
        return this.isPrinting;
    }

    public boolean isPrinting() {
        return this.isPrinting;
    }

    public void setPrinting(final boolean isPrinting) {
        this.isPrinting = isPrinting;
    }

    public SchematicWorld getSchematic() {
        return this.schematic;
    }

    public void setSchematic(final SchematicWorld schematic) {
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
        this.syncBlacklist.clear();
    }
    //----------------------------------------------------------------------------------------------------------------------------------------
    public boolean print(final WorldClient world, final EntityPlayerSP player) {
        final double dX = ClientProxy.playerPosition.x - this.schematic.position.x;
        final double dY = ClientProxy.playerPosition.y - this.schematic.position.y+player.getEyeHeight();
        final double dZ = ClientProxy.playerPosition.z - this.schematic.position.z;
        final int x = (int) Math.floor(dX);
        final int y = (int) Math.floor(dY);
        final int z = (int) Math.floor(dZ);
        final int range = ConfigurationHandler.placeDistance;
        final int minX = Math.max(0, x - range);
        final int maxX = Math.min(this.schematic.getWidth() - 1, x + range);
        int minY = Math.max(0, y - range);
        int maxY = Math.min(this.schematic.getHeight() - 1, y + range);
        final int minZ = Math.max(0, z - range);
        final int maxZ = Math.min(this.schematic.getLength() - 1, z + range);
        final int priority = ConfigurationHandler.priority;

        final int rollover = ConfigurationHandler.directionalPriority;

        if (rollover > 0) {
            if (rollover < rollingVel.size()) {
                rollingVel.clear();
                rollingPos = 0;
            }

            Vec3d cm = new Vec3d(player.motionX, 0, player.motionZ);

            if (cm.x != 0 && cm.z != 0) {
                if (rollingVel.size() == rollover) {
                    rollingVel.set(rollingPos, cm.normalize());
                } else {
                    rollingVel.add(rollingPos, cm.normalize());
                }
                rollingPos++;
                if (rollingPos >= rollover) {
                    rollingPos = 0;
                }
                double vx = 0;
                double vz = 0;
                for (final Vec3d pos : rollingVel) {
                    vx = vx + pos.x;
                    vz = vz + pos.z;
                }
                averageVelocity = new Vec3d(vx, 0, vz).normalize().scale(-1000);
            }
        } else if (rollover == 0) {
            averageVelocity = new Vec3d(0,0,0);
        } else {
            averageVelocity = new Vec3d(10000,0,10);
        }



        if (minX > maxX || minY > maxY || minZ > maxZ) {
            return false;
        }

        final int slot = player.inventory.currentItem;
        final boolean isSneaking = player.isSneaking();

        switch (schematic.layerMode) {
        case ALL: break;
        case SINGLE_LAYER:
            if (schematic.renderingLayer > maxY) {
                return false;
            }
            maxY = schematic.renderingLayer;
            //$FALL-THROUGH$
        case ALL_BELOW:
            if (schematic.renderingLayer < minY) {
                return false;
            }
            maxY = schematic.renderingLayer;
            break;
        }

        syncSneaking(player, true);

        final double blockReachDistance = this.minecraft.playerController.getBlockReachDistance() - 0.1;
        final double blockReachDistanceSq = blockReachDistance * blockReachDistance;
        List<MBlockPos> inRange = new ArrayList<>();
        List<MBlockPos> inRangeB = new ArrayList<>();
        for (final MBlockPos pos : BlockPosHelper.getAllInBoxXZY(minX, minY, minZ, maxX, maxY, maxZ)) {
            if (pos.distanceSqToCenter(dX, dY, dZ) > blockReachDistanceSq) {
                continue;
            }
            if (priority > 1 && pos.y > dY-2) {
                inRangeB.add(new MBlockPos(pos));
            } else {
                inRange.add(new MBlockPos(pos));
            }

        }

        MBCompareDist distcomp = new MBCompareDist(new Vec3d(dX+ averageVelocity.x, dY, dZ+ averageVelocity.z));
        MBCompareHeight heightcomp = new MBCompareHeight();

        if (priority > 1) { // 1 is layers, 2 is pillars, 3 is below only
            inRange.sort(heightcomp);
            inRange.sort(distcomp);
            if (priority == 2) {
                inRangeB.sort(heightcomp);
                inRangeB.sort(distcomp);
                inRange.addAll(inRangeB);
            }
        } else {
            inRange.sort(distcomp);
            inRange.sort(heightcomp);

        }

        for (final MBlockPos pos: inRange) {
            try {
                if (placeBlock(world, player, pos)) {
                    return syncSlotAndSneaking(player, slot, isSneaking, true);
                }
            } catch (final Exception e) {
                Reference.logger.error("Could not place block!", e);
                return syncSlotAndSneaking(player, slot, isSneaking, false);
            }
        }
        return syncSlotAndSneaking(player, slot, isSneaking, true);
    }



    private boolean placeBlock(final WorldClient world, final EntityPlayerSP player, final BlockPos pos) {

        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        if (this.timeout[x][y][z] > 0) {
            this.timeout[x][y][z]--;
            return false;
        }

        final int wx = this.schematic.position.x + x;
        final int wy = this.schematic.position.y + y;
        final int wz = this.schematic.position.z + z;
        final BlockPos realPos = new BlockPos(wx, wy, wz);

        final IBlockState blockState = this.schematic.getBlockState(pos);
        final IBlockState realBlockState = world.getBlockState(realPos);
        final Block realBlock = realBlockState.getBlock();

        if (BlockStateHelper.areBlockStatesEqual(blockState, realBlockState)) {
            // TODO: clean up this mess
            final NBTSync handler = SyncRegistry.INSTANCE.getHandler(realBlock);
            if (handler != null) {
                this.timeout[x][y][z] = (byte) ConfigurationHandler.timeout;

                Integer tries = this.syncBlacklist.get(realPos);
                if (tries == null) {
                    tries = 0;
                } else if (tries >= 10) {
                    return false;
                }

                Reference.logger.trace("Trying to sync block at {} {}", realPos, tries);
                final boolean success = handler.execute(player, this.schematic, pos, world, realPos);
                if (success) {
                    this.syncBlacklist.put(realPos, tries + 1);
                }

                return success;
            }

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

        final ItemStack itemStack = BlockStateToItemStack.getItemStack(blockState, new RayTraceResult(player), this.schematic, pos, player);
        if (itemStack.isEmpty()) {
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

    /*
     *This is called for every side, and checks if you can place against that side.
     * -pos is the block we're attempting to place.
     * -side is the side we're checking for placeability.
     * This checks every side, regardless of
     */
    private boolean isSolid(final World world, final BlockPos pos, final EnumFacing side) {
        final BlockPos offset = pos.offset(side);
        final IBlockState blockState = world.getBlockState(offset);
        final Block block = blockState.getBlock();


        if (block.isAir(blockState, world, offset)) {
            //printDebug(side + ": failed- is Air.");
            return false;
        }


        if (block instanceof BlockLiquid) {
            //printDebug(side + ": failed- is fluid. (How did you get here?)");
            return false;
        }

        if (block.isReplaceable(world, offset)) {
            //printDebug(side + ": failed- block is replaceable?");
            return false;
        }

        //printDebug(side +": Passed!");

        return true;
    }

    private List<EnumFacing> getSolidSides(final World world, final BlockPos pos) {
        if (!ConfigurationHandler.placeAdjacent) {
            return Arrays.asList(EnumFacing.VALUES);
        }

        final List<EnumFacing> list = new ArrayList<>();

        for (final EnumFacing side : EnumFacing.VALUES) {
            if (isSolid(world, pos, side)) {
                list.add(side);
            }
        }

        return list;
    }

    private boolean placeBlock(final WorldClient world, final EntityPlayerSP player, final BlockPos pos, final IBlockState blockState, final ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemBucket) {
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

        printDebug("2: {"+pos+"} succeeded on: " + solidSides);
        final EnumFacing direction;
        final float offsetX;
        final float offsetY;
        final float offsetZ;
        final int extraClicks;

        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        double px = player.posX;
        double py = player.posY;
        double pz = player.posZ;

        if (data != null) {
            final List<EnumFacing> validDirections = data.getValidBlockFacings(solidSides, blockState);
            if (validDirections.size() == 0) {
                return false;
            }
            direction = validDirections.get(0);
            offsetX = data.getOffsetX(blockState);
            offsetY = data.getOffsetY(blockState);
            offsetZ = data.getOffsetZ(blockState);
            extraClicks = data.getExtraClicks(blockState);
        } else {
            direction = solidSides.get(0);
            offsetX = 0.5f;
            offsetY = 0.5f;
            offsetZ = 0.5f;
            extraClicks = 0;
        }

        if (ConfigurationHandler.stealthMode) {
            boolean passed = false;
            for (EnumFacing face : solidSides) {
                printDebug(face.toString());
                switch (face) {
                    case UP:
                        if (y >= py) {passed=true;}
                        break;
                    case DOWN:
                        if (y <= py+2) {passed=true;}
                        break;
                    case SOUTH:
                        if (blockState.isFullBlock()) {
                            if (z >= pz-1) {passed=true;}
                        } else {
                            if (z >= pz-2) {passed=true;}
                        }
                        break;
                    case NORTH:
                        if (blockState.isFullBlock()) {
                            if (z <= pz) {passed=true;}
                        } else {
                            if (z <= pz+1) {passed=true;}
                        }
                        break;
                    case EAST:
                        if (blockState.isFullBlock()) {
                            if (x >= px-1) {passed=true;}
                        } else {
                            if (x >= px-2) {passed=true;}
                        }
                        break;
                    case WEST:
                        if (blockState.isFullBlock()) {
                            if (x <= px) {passed=true;}
                        } else {
                            if (x <= px+1) {passed=true;}
                        }
                        break;
                }
            }
            if (!passed) {
                return false;
            }
            printDebug("Passed all checks.");
        }

        if (!swapToItem(player.inventory, itemStack)) {
            return false;
        }
        return placeBlock(world, player, pos, direction, offsetX, offsetY, offsetZ, extraClicks);
    }

    /*
     * Handles resources
     */

    private boolean placeBlock(final WorldClient world, final EntityPlayerSP player, final BlockPos pos, final EnumFacing direction, final float offsetX, final float offsetY, final float offsetZ, final int extraClicks) {
        printDebug("3: placing.");
        final EnumHand hand = EnumHand.MAIN_HAND;
        final ItemStack itemStack = player.getHeldItem(hand);
        boolean success = false;

        if (!this.minecraft.playerController.isInCreativeMode() && !itemStack.isEmpty() && itemStack.getCount() <= extraClicks) {
            return false;
        }

        final BlockPos offset = pos.offset(direction);
        final EnumFacing side = direction.getOpposite();
        final Vec3d hitVec = new Vec3d(offset.getX() + offsetX, offset.getY() + offsetY, offset.getZ() + offsetZ);

        success = placeBlock(world, player, itemStack, offset, side, hitVec, hand);
        for (int i = 0; success && i < extraClicks; i++) {
            success = placeBlock(world, player, itemStack, offset, side, hitVec, hand);
        }

        if (itemStack.getCount() == 0 && success) {
            player.inventory.mainInventory.set(player.inventory.currentItem, ItemStack.EMPTY);
        }

        return success;
    }

    /*
     * Actually PLACES the blocks. Is called twice for multi-part blocks, meaning JUST slabs.
     */

    private boolean placeBlock(final WorldClient world, final EntityPlayerSP player, final ItemStack itemStack, final BlockPos pos, final EnumFacing side, final Vec3d hitVec, final EnumHand hand) {
        // FIXME: where did this event go?
        /*
        if (ForgeEventFactory.onPlayerInteract(player, Action.RIGHT_CLICK_BLOCK, world, pos, side, hitVec).isCanceled()) {
            return false;
        }
        */

        // FIXME: when an adjacent block is not required the blocks should be placed 1 block away from the actual position (because air is replaceable)

        final BlockPos ref = pos.offset(side);
        final Vec3d cent = new Vec3d(ref.getX()+.5, ref.getY()+.5, ref.getZ()+.5);
        final double x = pos.getX()-ref.getX();
        final double y = pos.getY()-ref.getY();
        final double z = pos.getZ()-ref.getZ();
        final Vec3d epiclook = new Vec3d(x*.5+cent.x,y*.5+cent.y,z*.5+cent.z);

        PrinterUtil.faceVectorPacketInstant(epiclook);

        final BlockPos actualPos = ConfigurationHandler.placeAdjacent ? pos : pos.offset(side);
        final EnumActionResult result = this.minecraft.playerController.processRightClickBlock(player, world, actualPos, side, hitVec, hand);
        if ((result != EnumActionResult.SUCCESS)) {
            return false;
        }

        player.swingArm(hand);
        return true;
    }


    private boolean syncSlotAndSneaking(final EntityPlayerSP player, final int slot, final boolean isSneaking, final boolean success) {
        player.inventory.currentItem = slot;
        syncSneaking(player, isSneaking);
        return success;
    }

    private void syncSneaking(final EntityPlayerSP player, final boolean isSneaking) {
        player.setSneaking(isSneaking);
        player.connection.sendPacket(new CPacketEntityAction(player, isSneaking ? CPacketEntityAction.Action.START_SNEAKING : CPacketEntityAction.Action.STOP_SNEAKING));
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
        for (int i = 0; i < inventory.mainInventory.size(); i++) {
            if (inventory.mainInventory.get(i).isItemEqual(itemStack)) {
                return i;
            }
        }
        return -1;
    }

    private boolean swapSlots(final InventoryPlayer inventory, final int from) {
        if (ConfigurationHandler.swapSlotsQueue.size() > 0) {
            final int slot = getNextSlot();

            swapSlots(from, slot);
            return true;
        }

        return false;
    }

    private int getNextSlot() {
        final int slot = ConfigurationHandler.swapSlotsQueue.poll() % Constants.Inventory.Size.HOTBAR;
        ConfigurationHandler.swapSlotsQueue.offer(slot);
        return slot;
    }

    private boolean swapSlots(final int from, final int to) {
        return this.minecraft.playerController.windowClick(this.minecraft.player.inventoryContainer.windowId, from, to, ClickType.SWAP, this.minecraft.player) == ItemStack.EMPTY;
    }

    private void printDebug(String message) {
        if(ConfigurationHandler.debugMode) {
            this.minecraft.player.sendMessage(new TextComponentTranslation(I18n.format(message)));
        }
    }

    private List<Vec3d> raycast(Vec3d endpoint) {
        List<Vec3d> blocks = null;
        blocks.add(new Vec3d(1,2,3));

        return blocks;
    }
}

class MBCompareDist implements Comparator<MBlockPos> {
    Vec3d p;
    public MBCompareDist(Vec3d point) {
        p = new Vec3d(point.x-.5, point.y-.5, point.z-.5);
    }
    public int compare(MBlockPos A, MBlockPos B) {
        if (A.x == B.x && A.z == B.z) {
            return 0;
        } else if (Math.pow(A.x-p.x, 2)+Math.pow(A.z-p.z,2) > Math.pow(B.x-p.x, 2)+Math.pow(B.z-p.z,2)) {
            return 1;
        } else {
            return -1;
        }
    }
}

class MBCompareHeight implements Comparator<MBlockPos> {

    public int compare(MBlockPos A, MBlockPos B) {
        if (A.y == B.y) {
            return 0;
        } else if (A.y > B.y) {
            return 1;
        } else {
            return -1;
        }
    }
}
