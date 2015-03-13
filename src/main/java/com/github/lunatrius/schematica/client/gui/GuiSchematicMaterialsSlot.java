package com.github.lunatrius.schematica.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;

class GuiSchematicMaterialsSlot extends GuiSlot {
    private final Minecraft minecraft = Minecraft.getMinecraft();

    private final GuiSchematicMaterials guiSchematicMaterials;

    protected int selectedIndex = -1;

    public GuiSchematicMaterialsSlot(GuiSchematicMaterials par1) {
        super(Minecraft.getMinecraft(), par1.width, par1.height, 16, par1.height - 34, 24);
        this.guiSchematicMaterials = par1;
        this.selectedIndex = -1;
    }

    @Override
    protected int getSize() {
        return this.guiSchematicMaterials.blockList.size();
    }

    @Override
    protected void elementClicked(int index, boolean par2, int par3, int par4) {
        this.selectedIndex = index;
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
    protected int getScrollBarX() {
        return this.width / 2 + getListWidth() / 2 + 2;
    }

    @Override
    protected void drawSlot(int index, int x, int y, int par4, int mouseX, int mouseY) {
        ItemStack itemStack = this.guiSchematicMaterials.blockList.get(index);

        String itemName = itemStack.getItem().getItemStackDisplayName(itemStack);
        String amount = Integer.toString(itemStack.stackSize);

        GuiHelper.drawItemStack(this.minecraft.renderEngine, this.minecraft.fontRendererObj, x, y, itemStack);

        this.guiSchematicMaterials.drawString(this.minecraft.fontRendererObj, itemName, x + 24, y + 6, 0xFFFFFF);
        this.guiSchematicMaterials.drawString(this.minecraft.fontRendererObj, amount, x + 215 - this.minecraft.fontRendererObj.getStringWidth(amount), y + 6, 0xFFFFFF);

        if (mouseX > x && mouseY > y && mouseX <= x + 18 && mouseY <= y + 18) {
            this.guiSchematicMaterials.renderToolTip(itemStack, mouseX, mouseY);
            GlStateManager.disableLighting();
        }
    }
}
