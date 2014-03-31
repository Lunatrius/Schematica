package com.github.lunatrius.schematica.client;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.Settings;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import static cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import static cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;

public class Events {
	private final Minecraft minecraft = Minecraft.getMinecraft();

	@SubscribeEvent
	public void tick(ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			Schematica.instance.onTick();
		}
	}

	@SubscribeEvent
	public void keyInput(KeyInputEvent event) {
		for (KeyBinding keyBinding : Settings.instance.keyBindings) {
			if (keyBinding.isPressed()) {
				if (this.minecraft.currentScreen == null) {
					Schematica.instance.keyboardEvent(keyBinding);
				}
			}
		}
	}
}
