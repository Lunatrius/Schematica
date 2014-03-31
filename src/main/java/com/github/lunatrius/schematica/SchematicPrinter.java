package com.github.lunatrius.schematica;

import com.github.lunatrius.schematica.config.BlockInfo;
import com.github.lunatrius.schematica.lib.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fluids.BlockFluidBase;

public class SchematicPrinter {
	public static final SchematicPrinter INSTANCE = new SchematicPrinter();

	private final Settings settings = Settings.instance;

	public boolean print() {
		int minX, maxX, minY, maxY, minZ, maxZ, x, y, z, wx, wy, wz, blockMetadata, slot;
		Block blockId;
		boolean isSneaking;
		EntityClientPlayerMP player = this.settings.minecraft.thePlayer;
		World world = this.settings.minecraft.theWorld;

		syncSneaking(player, true);

		minX = Math.max(0, (int) this.settings.getTranslationX() - 3);
		maxX = Math.min(this.settings.schematic.getWidth(), (int) this.settings.getTranslationX() + 3);
		minY = Math.max(0, (int) this.settings.getTranslationY() - 3);
		maxY = Math.min(this.settings.schematic.getHeight(), (int) this.settings.getTranslationY() + 3);
		minZ = Math.max(0, (int) this.settings.getTranslationZ() - 3);
		maxZ = Math.min(this.settings.schematic.getLength(), (int) this.settings.getTranslationZ() + 3);

		slot = player.inventory.currentItem;
		isSneaking = player.isSneaking();

		for (y = minY; y <= maxY; y++) {
			if (this.settings.renderingLayer >= 0) {
				if (y != this.settings.renderingLayer) {
					continue;
				}
			}

			for (x = minX; x <= maxX; x++) {
				for (z = minZ; z <= maxZ; z++) {
					blockId = this.settings.schematic.getBlock(x, y, z);

					wx = (int) this.settings.offset.x + x;
					wy = (int) this.settings.offset.y + y;
					wz = (int) this.settings.offset.z + z;

					Block block = world.getBlock(wx, wy, wz);
					if (!world.isAirBlock(wx, wy, wz) && block != null && !block.canPlaceBlockAt(world, wx, wy, wz)) {
						continue;
					}

					blockMetadata = this.settings.schematic.getBlockMetadata(x, y, z);
					if (placeBlock(this.settings.minecraft, world, player, wx, wy, wz, getMappedId(blockId), blockMetadata)) {
						if (!Reference.config.placeInstantly) {
							player.inventory.currentItem = slot;
							syncSneaking(player, isSneaking);
							return true;
						}
					}
				}
			}
		}

		player.inventory.currentItem = slot;
		syncSneaking(player, isSneaking);
		return true;
	}

	private Object getMappedId(Object block) {
		if (BlockInfo.BLOCK_ITEM_MAP.containsKey(block)) {
			return BlockInfo.BLOCK_ITEM_MAP.get(block);
		}
		return block;
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

		return true;
		// return world.isSideSolid(x, y, z, side.getOpposite(), false);
	}

	private boolean placeBlock(Minecraft minecraft, World world, EntityPlayer player, int x, int y, int z, Object itemId, int itemDamage) {
		if (!isValidOrientation(player, x, y, z, itemId, itemDamage)) {
			return false;
		}

		if (SchematicWorld.isFluidContainer(itemId) || itemId == Items.sign) {
			return false;
		}

		int side = 0;
		float offsetY = 0.0f;
		ForgeDirection direction = ForgeDirection.DOWN;
		boolean[] blocks = new boolean[] {
				isSolid(world, x, y, z, ForgeDirection.UP),
				isSolid(world, x, y, z, ForgeDirection.DOWN),
				isSolid(world, x, y, z, ForgeDirection.NORTH),
				isSolid(world, x, y, z, ForgeDirection.SOUTH),
				isSolid(world, x, y, z, ForgeDirection.WEST),
				isSolid(world, x, y, z, ForgeDirection.EAST)
		};

		for (int i = 0; i < blocks.length; i++) {
			if (blocks[i]) {
				direction = ForgeDirection.getOrientation(i).getOpposite();
				break;
			}
		}

		if (SchematicWorld.isMetadataSensitive(itemId)) {
			int itemDamageInHand = 0xF;
			if (SchematicWorld.isTorch(itemId)) {
				switch (itemDamage) {
				case 1:
					direction = ForgeDirection.WEST;
					break;
				case 2:
					direction = ForgeDirection.EAST;
					break;
				case 3:
					direction = ForgeDirection.NORTH;
					break;
				case 4:
					direction = ForgeDirection.SOUTH;
					break;
				case 5:
					direction = ForgeDirection.DOWN;
					break;
				}

				if (direction == ForgeDirection.DOWN) {
					if (World.doesBlockHaveSolidTopSurface(world, x, y - 1, z)) {
						itemDamageInHand = 0;
					}
				} else {
					if (world.isSideSolid(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ, direction, false)) {
						itemDamageInHand = 0;
					}
				}
			} else if (SchematicWorld.isBlock(itemId)) {
				itemDamageInHand = itemDamage;
			} else if (SchematicWorld.isSlab(itemId)) {
				if ((itemDamage & 0x8) != 0 && direction == ForgeDirection.DOWN) {
					direction = ForgeDirection.UP;
				} else if ((itemDamage & 0x8) == 0 && direction == ForgeDirection.UP) {
					direction = ForgeDirection.DOWN;
				}
				offsetY = (itemDamage & 0x8) == 0x0 ? 0.0f : 1.0f;
				itemDamageInHand = itemDamage & 0x7;
			} else if (SchematicWorld.isPistonBase(itemId)) {
				itemDamageInHand = 0;
			} else if (SchematicWorld.isDoubleSlab(itemId)) {
				itemDamageInHand = itemDamage;
			} else if (SchematicWorld.isContainer(itemId)) {
				itemDamageInHand = 0;
			} else if (SchematicWorld.isButton(itemId)) {
				switch (itemDamage & 0x7) {
				case 0x1:
					direction = ForgeDirection.WEST;
					break;
				case 0x2:
					direction = ForgeDirection.EAST;
					break;
				case 0x3:
					direction = ForgeDirection.NORTH;
					break;
				case 0x4:
					direction = ForgeDirection.SOUTH;
					break;
				default:
					return false;
				}

				if (world.isSideSolid(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ, direction, false)) {
					itemDamageInHand = 0;
				} else {
					return false;
				}
			} else if (SchematicWorld.isPumpkin(itemId)) {
				if (World.doesBlockHaveSolidTopSurface(world, x, y - 1, z)) {
					itemDamageInHand = 0;
				} else {
					return false;
				}
			} else if (itemId == Items.repeater) {
				itemDamageInHand = 0;
			} else if (itemId == Blocks.anvil) {
				switch (itemDamage & 0xC) {
				case 0x0:
					itemDamageInHand = 0;
					break;
				case 0x4:
					itemDamageInHand = 1;
					break;
				case 0x8:
					itemDamageInHand = 2;
					break;
				default:
					return false;
				}
			} else if (itemId == Blocks.fence_gate) {
				itemDamageInHand = 0;
			} else if (itemId == Blocks.trapdoor) {
				switch (itemDamage & 0x3) {
				case 0x0:
					direction = ForgeDirection.SOUTH;
					break;
				case 0x1:
					direction = ForgeDirection.NORTH;
					break;
				case 0x2:
					direction = ForgeDirection.EAST;
					break;
				case 0x3:
					direction = ForgeDirection.WEST;
					break;
				default:
					return false;
				}

				if ((itemDamage & 0x8) != 0) {
					offsetY = 0.75f;
				}

				if (world.isSideSolid(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ, direction, false)) {
					itemDamageInHand = 0;
				} else {
					return false;
				}
			} else {
				return false;
			}

			if (!swapToItem(player.inventory, itemId, itemDamageInHand)) {
				return false;
			}
		} else {
			if (!swapToItem(player.inventory, itemId)) {
				return false;
			}

			if (SchematicWorld.isStair(itemId)) {
				direction = (itemDamage & 0x4) == 0x0 ? ForgeDirection.DOWN : ForgeDirection.UP;
			} else if (Blocks.log == itemId) {
				if ((itemDamage & 0xC) == 0x00) {
					direction = ForgeDirection.DOWN;
				} else if ((itemDamage & 0xC) == 0x04) {
					direction = ForgeDirection.EAST;
				} else if ((itemDamage & 0xC) == 0x08) {
					direction = ForgeDirection.NORTH;
				}
			}
		}

		side = getSide(direction);

		if (side != ForgeDirection.UNKNOWN.ordinal() && blocks[side] || !Reference.config.placeAdjacent) {
			return placeBlock(minecraft, world, player, x, y, z, direction, 0.0f, offsetY, 0.0f);
		}

		return false;
	}

	private boolean isValidOrientation(EntityPlayer player, int x, int y, int z, Object itemId, int itemDamage) {
		int orientation = this.settings.orientation;

		if (SchematicWorld.isStair(itemId)) {
			switch (itemDamage & 0x3) {
			case 0:
				return orientation == 4;
			case 1:
				return orientation == 5;
			case 2:
				return orientation == 2;
			case 3:
				return orientation == 3;
			}
		} else if (SchematicWorld.isPistonBase(itemId)) {
			return BlockPistonBase.determineOrientation(null, x, y, z, player) == BlockPistonBase.getPistonOrientation(itemDamage);
		} else if (SchematicWorld.isContainer(itemId)) {
			switch (itemDamage) {
			case 2:
				return orientation == 2;
			case 3:
				return orientation == 3;
			case 4:
				return orientation == 4;
			case 5:
				return orientation == 5;
			default:
				return false;
			}
		} else if (SchematicWorld.isPumpkin(itemId)) {
			switch (itemDamage) {
			case 0x0:
				return orientation == 3;
			case 0x1:
				return orientation == 4;
			case 0x2:
				return orientation == 2;
			case 0x3:
				return orientation == 5;
			default:
				return false;
			}
		} else if (itemId == Items.repeater) {
			switch (itemDamage & 0x3) {
			case 0:
				return orientation == 3;
			case 1:
				return orientation == 4;
			case 2:
				return orientation == 2;
			case 3:
				return orientation == 5;
			}
		} else if (itemId == Blocks.anvil) {
			switch (itemDamage & 0x3) {
			case 0:
				return orientation == 5;
			case 1:
				return orientation == 3;
			case 2:
				return orientation == 4;
			case 3:
				return orientation == 2;
			}
		} else if (itemId == Blocks.fence_gate) {
			switch (itemDamage & 0x3) {
			case 0:
				return orientation == 2;
			case 1:
				return orientation == 5;
			case 2:
				return orientation == 3;
			case 3:
				return orientation == 4;
			}
		}

		return true;
	}

	private boolean placeBlock(Minecraft minecraft, World world, EntityPlayer player, int x, int y, int z, ForgeDirection direction, float offsetX, float offsetY, float offsetZ) {
		ItemStack itemStack = player.getCurrentEquippedItem();
		boolean success = false;

		x += direction.offsetX;
		y += direction.offsetY;
		z += direction.offsetZ;

		int side = getSide(direction);

		/* copypasted from n.m.client.Minecraft to sooth finicky servers */
		success = !ForgeEventFactory.onPlayerInteract(minecraft.thePlayer, Action.RIGHT_CLICK_BLOCK, x, y, z, side).isCanceled();
		if (success) {
			// still not assured!
			success = minecraft.playerController.onPlayerRightClick(player, world, itemStack, x, y, z, side, Vec3.createVectorHelper(x + offsetX, y + offsetY, z + offsetZ));
			if (success) {
				// yes, some servers actually care about this.
				minecraft.thePlayer.swingItem();
			}
		}

		if (itemStack.stackSize == 0 && success) {
			player.inventory.mainInventory[player.inventory.currentItem] = null;
		}

		return success;
	}

	private void syncSneaking(EntityClientPlayerMP player, boolean isSneaking) {
		player.setSneaking(isSneaking);
		player.sendQueue.addToSendQueue(new C0BPacketEntityAction(player, isSneaking ? 1 : 2));
	}

	private int getSide(ForgeDirection direction) {
		return direction.getOpposite().ordinal();
	}

	private boolean swapToItem(InventoryPlayer inventory, Object itemID, int itemDamage) {
		int slot = getInventorySlotWithItem(inventory, itemID, itemDamage);
		if (slot > -1 && slot < 9) {
			inventory.currentItem = slot;
			return true;
		}
		return false;
	}

	private boolean swapToItem(InventoryPlayer inventory, Object itemID) {
		int slot = getInventorySlotWithItem(inventory, itemID);
		if (slot > -1 && slot < 9) {
			inventory.currentItem = slot;
			return true;
		}
		return false;
	}

	private int getInventorySlotWithItem(InventoryPlayer inventory, Object itemID, int itemDamage) {
		if (itemID instanceof Block) {
			itemID = Item.getItemFromBlock((Block) itemID);
		}
		for (int i = 0; i < inventory.mainInventory.length; i++) {
			if (inventory.mainInventory[i] != null && inventory.mainInventory[i].getItem() == itemID && inventory.mainInventory[i].getItemDamage() == itemDamage) {
				return i;
			}
		}
		return -1;
	}

	private int getInventorySlotWithItem(InventoryPlayer inventory, Object itemID) {
		if (itemID instanceof Block) {
			itemID = Item.getItemFromBlock((Block) itemID);
		}
		for (int i = 0; i < inventory.mainInventory.length; i++) {
			if (inventory.mainInventory[i] != null && inventory.mainInventory[i].getItem() == itemID) {
				return i;
			}
		}
		return -1;
	}
}
