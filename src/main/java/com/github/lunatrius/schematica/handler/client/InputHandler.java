package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.client.gui.GuiSchematicControl;
import com.github.lunatrius.schematica.client.gui.GuiSchematicLoad;
import com.github.lunatrius.schematica.client.gui.GuiSchematicSave;
import com.github.lunatrius.schematica.client.renderer.RendererSchematicGlobal;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeHooks;
import org.lwjgl.input.Keyboard;

public class InputHandler {
    public static final InputHandler INSTANCE = new InputHandler();

    private static final KeyBinding KEY_BINDING_LOAD = new KeyBinding(Names.Keys.LOAD, Keyboard.KEY_DIVIDE, Names.Keys.CATEGORY);
    private static final KeyBinding KEY_BINDING_SAVE = new KeyBinding(Names.Keys.SAVE, Keyboard.KEY_MULTIPLY, Names.Keys.CATEGORY);
    private static final KeyBinding KEY_BINDING_CONTROL = new KeyBinding(Names.Keys.CONTROL, Keyboard.KEY_SUBTRACT, Names.Keys.CATEGORY);
    private static final KeyBinding KEY_BINDING_LAYER_INC = new KeyBinding(Names.Keys.LAYER_INC, Keyboard.KEY_NONE, Names.Keys.CATEGORY);
    private static final KeyBinding KEY_BINDING_LAYER_DEC = new KeyBinding(Names.Keys.LAYER_DEC, Keyboard.KEY_NONE, Names.Keys.CATEGORY);

    public static final KeyBinding[] KEY_BINDINGS = new KeyBinding[] {
            KEY_BINDING_LOAD, KEY_BINDING_SAVE, KEY_BINDING_CONTROL, KEY_BINDING_LAYER_INC, KEY_BINDING_LAYER_DEC
    };

    private final Minecraft minecraft = Minecraft.getMinecraft();

    private InputHandler() {}

    @SubscribeEvent
    public void onKeyInput(InputEvent event) {
        if (this.minecraft.currentScreen == null) {
            if (KEY_BINDING_LOAD.isPressed()) {
                this.minecraft.displayGuiScreen(new GuiSchematicLoad(this.minecraft.currentScreen));
            }

            if (KEY_BINDING_SAVE.isPressed()) {
                this.minecraft.displayGuiScreen(new GuiSchematicSave(this.minecraft.currentScreen));
            }

            if (KEY_BINDING_CONTROL.isPressed()) {
                this.minecraft.displayGuiScreen(new GuiSchematicControl(this.minecraft.currentScreen));
            }

            if (KEY_BINDING_LAYER_INC.isPressed()) {
                final SchematicWorld schematic = Schematica.proxy.getActiveSchematic();
                if (schematic != null && schematic.isRenderingLayer) {
                    schematic.renderingLayer = MathHelper.clamp_int(schematic.renderingLayer + 1, 0, schematic.getHeight() - 1);
                    RendererSchematicGlobal.INSTANCE.refresh();
                }
            }

            if (KEY_BINDING_LAYER_DEC.isPressed()) {
                final SchematicWorld schematic = Schematica.proxy.getActiveSchematic();
                if (schematic != null && schematic.isRenderingLayer) {
                    schematic.renderingLayer = MathHelper.clamp_int(schematic.renderingLayer - 1, 0, schematic.getHeight() - 1);
                    RendererSchematicGlobal.INSTANCE.refresh();
                }
            }

            handlePickBlock();
        }
    }

    private void handlePickBlock() {
        final KeyBinding keyPickBlock = this.minecraft.gameSettings.keyBindPickBlock;
        if (keyPickBlock.isPressed()) {
            try {
                final SchematicWorld schematic = Schematica.proxy.getActiveSchematic();
                boolean revert = true;

                if (schematic != null && schematic.isRendering) {
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

            final MovingObjectPosition mcObjectMouseOver = this.minecraft.objectMouseOver;
            if (mcObjectMouseOver != null && mcObjectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                final int x = mcObjectMouseOver.blockX - schematic.position.x;
                final int y = mcObjectMouseOver.blockY - schematic.position.y;
                final int z = mcObjectMouseOver.blockZ - schematic.position.z;
                if (x == objectMouseOver.blockX && y == objectMouseOver.blockY && z == objectMouseOver.blockZ) {
                    return true;
                }
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
