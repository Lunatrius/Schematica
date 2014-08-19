package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.client.gui.GuiSchematicControl;
import com.github.lunatrius.schematica.client.gui.GuiSchematicLoad;
import com.github.lunatrius.schematica.client.gui.GuiSchematicSave;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.ForgeHooks;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import static cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import static cpw.mods.fml.common.gameevent.InputEvent.MouseInputEvent;

public class KeyInputHandler {
	public static final String CATEGORY = "schematica.key.category";
	public static final String LOAD = "schematica.key.load";
	public static final String SAVE = "schematica.key.save";
	public static final String CONTROL = "schematica.key.control";

	private static final KeyBinding KEY_BINDING_LOAD = new KeyBinding(LOAD, Keyboard.KEY_DIVIDE, CATEGORY);
	private static final KeyBinding KEY_BINDING_SAVE = new KeyBinding(SAVE, Keyboard.KEY_MULTIPLY, CATEGORY);
	private static final KeyBinding KEY_BINDING_CONTROL = new KeyBinding(CONTROL, Keyboard.KEY_SUBTRACT, CATEGORY);

	public static final KeyBinding[] KEY_BINDINGS = new KeyBinding[] {
			KEY_BINDING_LOAD, KEY_BINDING_SAVE, KEY_BINDING_CONTROL
	};

	private final Minecraft minecraft = Minecraft.getMinecraft();

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		for (KeyBinding keyBinding : KEY_BINDINGS) {
			if (keyBinding.isPressed()) {
				if (this.minecraft.currentScreen == null) {
					GuiScreen guiScreen = null;
					if (keyBinding == KEY_BINDING_LOAD) {
						guiScreen = new GuiSchematicLoad(this.minecraft.currentScreen);
					} else if (keyBinding == KEY_BINDING_SAVE) {
						guiScreen = new GuiSchematicSave(this.minecraft.currentScreen);
					} else if (keyBinding == KEY_BINDING_CONTROL) {
						guiScreen = new GuiSchematicControl(this.minecraft.currentScreen);
					}

					if (guiScreen != null) {
						this.minecraft.displayGuiScreen(guiScreen);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onMouseInput(MouseInputEvent event) {
		if (this.minecraft.gameSettings.keyBindPickBlock.isPressed()) {
			try {
				final SchematicWorld schematic = Schematica.proxy.getActiveSchematic();
				boolean revert = true;

				if (schematic != null) {
					revert = pickBlock(schematic, 1.0f);
				}

				if (revert) {
					final int eventButton = Mouse.getEventButton();
					KeyBinding.onTick(eventButton - 100);
				}
			} catch (Exception e) {
				Reference.logger.error("Could not pick block!", e);
			}
		}
	}

	private boolean pickBlock(final SchematicWorld schematic, final float partialTicks) {
		final MovingObjectPosition objectMouseOver = rayTrace(schematic, partialTicks);
		boolean revert = false;

		// Minecraft.func_147112_ai
		if (objectMouseOver != null) {
			final EntityClientPlayerMP player = this.minecraft.thePlayer;

			if (objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) {
				revert = true;
			}

			if (!ForgeHooks.onPickBlock(objectMouseOver, player, schematic)) {
				return revert;
			}

			if (player.capabilities.isCreativeMode) {
				final Block block = schematic.getBlock(objectMouseOver.blockX, objectMouseOver.blockY, objectMouseOver.blockZ);
				final int metadata = schematic.getBlockMetadata(objectMouseOver.blockX, objectMouseOver.blockY, objectMouseOver.blockZ);
				if (block == Blocks.double_stone_slab || block == Blocks.double_wooden_slab || block == Blocks.snow_layer) {
					player.inventory.setInventorySlotContents(player.inventory.currentItem, new ItemStack(block, 1, metadata & 0xF));
				}

				final int slot = player.inventoryContainer.inventorySlots.size() - 9 + player.inventory.currentItem;
				this.minecraft.playerController.sendSlotPacket(player.inventory.getStackInSlot(player.inventory.currentItem), slot);
			}
		}

		return revert;
	}

	private MovingObjectPosition rayTrace(final SchematicWorld schematic, final float partialTicks) {
		final EntityLivingBase renderViewEntity = this.minecraft.renderViewEntity;
		final double blockReachDistance = this.minecraft.playerController.getBlockReachDistance();

		final double posX = renderViewEntity.posX;
		final double posY = renderViewEntity.posY;
		final double posZ = renderViewEntity.posZ;

		renderViewEntity.posX -= schematic.position.x;
		renderViewEntity.posY -= schematic.position.y;
		renderViewEntity.posZ -= schematic.position.z;

		final Vec3 vecPosition = renderViewEntity.getPosition(partialTicks);
		final Vec3 vecLook = renderViewEntity.getLook(partialTicks);
		final Vec3 vecExtendedLook = vecPosition.addVector(vecLook.xCoord * blockReachDistance, vecLook.yCoord * blockReachDistance, vecLook.zCoord * blockReachDistance);

		renderViewEntity.posX = posX;
		renderViewEntity.posY = posY;
		renderViewEntity.posZ = posZ;

		return schematic.func_147447_a(vecPosition, vecExtendedLook, false, false, true);
	}
}
