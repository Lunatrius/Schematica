package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.client.gui.GuiSchematicControl;
import com.github.lunatrius.schematica.client.gui.GuiSchematicLoad;
import com.github.lunatrius.schematica.client.gui.GuiSchematicSave;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import static cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;

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
	public void keyInput(KeyInputEvent event) {
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
}
