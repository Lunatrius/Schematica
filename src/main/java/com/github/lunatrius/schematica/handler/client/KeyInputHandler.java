package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.client.gui.GuiSchematicControl;
import com.github.lunatrius.schematica.client.gui.GuiSchematicLoad;
import com.github.lunatrius.schematica.client.gui.GuiSchematicSave;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeHooks;
import org.lwjgl.input.Keyboard;

import static cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import static cpw.mods.fml.common.gameevent.InputEvent.MouseInputEvent;

public class KeyInputHandler {
    public static final KeyInputHandler INSTANCE = new KeyInputHandler();

    private static final KeyBinding KEY_BINDING_LOAD = new KeyBinding(Names.Keys.LOAD, Keyboard.KEY_DIVIDE, Names.Keys.CATEGORY);
    private static final KeyBinding KEY_BINDING_SAVE = new KeyBinding(Names.Keys.SAVE, Keyboard.KEY_MULTIPLY, Names.Keys.CATEGORY);
    private static final KeyBinding KEY_BINDING_CONTROL = new KeyBinding(Names.Keys.CONTROL, Keyboard.KEY_SUBTRACT, Names.Keys.CATEGORY);

    public static final KeyBinding[] KEY_BINDINGS = new KeyBinding[] {
            KEY_BINDING_LOAD, KEY_BINDING_SAVE, KEY_BINDING_CONTROL
    };

    private final Minecraft minecraft = Minecraft.getMinecraft();

    private KeyInputHandler() {}

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
        final KeyBinding keyPickBlock = this.minecraft.gameSettings.keyBindPickBlock;
        if (keyPickBlock.isPressed()) {
            try {
                final SchematicWorld schematic = Schematica.proxy.getActiveSchematic();
                boolean revert = true;

                if (schematic != null) {
                    revert = pickBlock(schematic, ClientProxy.movingObjectPosition);
                }

                if (revert) {
                    KeyBinding.onTick(keyPickBlock.getKeyCode());
                }
            } catch (Exception e) {
                Reference.logger.error("Could not pick block!", e);
            }
        }
    }

    private boolean pickBlock(final SchematicWorld schematic, final MovingObjectPosition objectMouseOver) {
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
}
