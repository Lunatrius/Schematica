package com.github.lunatrius.schematica.client.gui.load;

import com.github.lunatrius.schematica.client.gui.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;

public class GuiSchematicLoadSlot extends GuiSlot {
    private final Minecraft minecraft = Minecraft.getMinecraft();

    private final GuiSchematicLoad guiSchematicLoad;

    protected int selectedIndex = -1;

    public GuiSchematicLoadSlot(GuiSchematicLoad guiSchematicLoad) {
        super(Minecraft.getMinecraft(), guiSchematicLoad.width, guiSchematicLoad.height, 16, guiSchematicLoad.height - 40, 24);
        this.guiSchematicLoad = guiSchematicLoad;
    }

    @Override
    protected int getSize() {
        return this.guiSchematicLoad.schematicFiles.size();
    }

    @Override
    protected void elementClicked(int index, boolean par2, int par3, int par4) {
        GuiSchematicEntry schematic = this.guiSchematicLoad.schematicFiles.get(index);
        if (schematic.isDirectory()) {
            this.guiSchematicLoad.changeDirectory(schematic.getName());
            this.selectedIndex = -1;
        } else {
            this.selectedIndex = index;
        }
    }

    @Override
    protected boolean isSelected(int index) {
        return index == this.selectedIndex;
    }

    @Override
    protected void drawBackground() {
    }

    @Override
    protected void drawContainerBackground(Tessellator tessellator) {
    }

    @Override
    protected void drawSlot(int index, int x, int y, int par4, int mouseX, int mouseY) {
        if (index < 0 || index >= this.guiSchematicLoad.schematicFiles.size()) {
            return;
        }

        GuiSchematicEntry schematic = this.guiSchematicLoad.schematicFiles.get(index);
        String schematicName = schematic.getName();

        if (schematic.isDirectory()) {
            schematicName += "/";
        } else {
            schematicName = schematicName.replaceAll("(?i)\\.schematic$", "");
        }

        GuiHelper.drawItemStack(this.minecraft.renderEngine, this.minecraft.fontRendererObj, x, y, schematic.getItemStack());

        this.guiSchematicLoad.drawString(this.minecraft.fontRendererObj, schematicName, x + 24, y + 6, 0x00FFFFFF);
    }
}
