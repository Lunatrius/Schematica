package lunatrius.schematica;

import java.util.EnumSet;

import net.minecraft.src.KeyBinding;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.TickType;

public class KeyBindingHandler extends KeyBindingRegistry.KeyHandler {
	public KeyBindingHandler(KeyBinding[] keyBindings, boolean[] repeatings) {
		super(keyBindings, repeatings);
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT, TickType.RENDER);
	}

	@Override
	public String getLabel() {
		return "KB";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
		Schematica.instance.keyboardEvent(kb, true);
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
		Schematica.instance.keyboardEvent(kb, false);
	}
}
