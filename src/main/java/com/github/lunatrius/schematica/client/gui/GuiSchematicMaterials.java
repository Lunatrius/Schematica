package com.github.lunatrius.schematica.client.gui;

import com.github.lunatrius.schematica.Settings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSmallButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.util.ArrayList;
import java.util.List;

public class GuiSchematicMaterials extends GuiScreen {
	private final GuiScreen prevGuiScreen;
	private GuiSchematicMaterialsSlot guiSchematicMaterialsSlot;

	private GuiSmallButton btnDone = null;

	private final String strMaterialName = StatCollector.translateToLocal("schematica.gui.materialname");
	private final String strMaterialAmount = StatCollector.translateToLocal("schematica.gui.materialamount");

	protected final List<ItemStack> blockList;

	public GuiSchematicMaterials(GuiScreen guiScreen) {
		this.prevGuiScreen = guiScreen;
		if (Settings.instance().schematic != null) {
			this.blockList = Settings.instance().schematic.getBlockList();
		} else {
			this.blockList = new ArrayList<ItemStack>();
		}
	}

	@Override
	public void initGui() {
		int id = 0;

		this.btnDone = new GuiSmallButton(id++, this.width / 2 + 4, this.height - 30, StatCollector.translateToLocal("schematica.gui.done"));
		this.buttonList.add(this.btnDone);

		this.guiSchematicMaterialsSlot = new GuiSchematicMaterialsSlot(this);
	}

	@Override
	protected void actionPerformed(GuiButton guiButton) {
		if (guiButton.enabled) {
			if (guiButton.id == this.btnDone.id) {
				this.mc.displayGuiScreen(this.prevGuiScreen);
			} else {
				this.guiSchematicMaterialsSlot.actionPerformed(guiButton);
			}
		}
	}

	@Override
	public void drawScreen(int x, int y, float partialTicks) {
		this.guiSchematicMaterialsSlot.drawScreen(x, y, partialTicks);

		drawString(this.fontRenderer, this.strMaterialName, this.width / 2 - 108, 4, 0x00FFFFFF);
		drawString(this.fontRenderer, this.strMaterialAmount, this.width / 2 + 108 - this.fontRenderer.getStringWidth(this.strMaterialAmount), 4, 0x00FFFFFF);
		super.drawScreen(x, y, partialTicks);
	}
}
