package com.github.lunatrius.schematica.client.gui.control;

import com.github.lunatrius.core.client.gui.GuiHelper;
import com.github.lunatrius.schematica.client.util.BlockList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;

class GuiSchematicMaterialsSlot extends GuiSlot {
    private final Minecraft minecraft = Minecraft.getMinecraft();

    private final GuiSchematicMaterials guiSchematicMaterials;

    protected int selectedIndex = -1;

    public GuiSchematicMaterialsSlot(final GuiSchematicMaterials par1) {
        super(Minecraft.getMinecraft(), par1.width, par1.height, 16, par1.height - 34, 24);
        this.guiSchematicMaterials = par1;
        this.selectedIndex = -1;
    }

    @Override
    protected int getSize() {
        return this.guiSchematicMaterials.blockList.size();
    }

    @Override
    protected void elementClicked(final int index, final boolean par2, final int par3, final int par4) {
        this.selectedIndex = index;
    }

    @Override
    protected boolean isSelected(final int index) {
        return index == this.selectedIndex;
    }

    @Override
    protected void drawBackground() {
    }

    @Override
    protected void drawContainerBackground(final Tessellator tessellator) {
    }

    @Override
    protected int getScrollBarX() {
        return this.width / 2 + getListWidth() / 2 + 2;
    }

    @Override
    protected void drawSlot(final int index, final int x, final int y, final int par4, final int mouseX, final int mouseY) {
        final BlockList.WrappedItemStack wrappedItemStack = this.guiSchematicMaterials.blockList.get(index);
        final ItemStack itemStack = wrappedItemStack.itemStack;

        final String itemName = wrappedItemStack.getItemStackDisplayName();
        final String amount = wrappedItemStack.getFormattedAmount();

        GuiHelper.drawItemStackWithSlot(this.minecraft.renderEngine, itemStack, x, y);

        this.guiSchematicMaterials.drawString(this.minecraft.fontRendererObj, itemName, x + 24, y + 6, 0xFFFFFF);
        this.guiSchematicMaterials.drawString(this.minecraft.fontRendererObj, amount, x + 215 - this.minecraft.fontRendererObj.getStringWidth(amount), y + 6, 0xFFFFFF);

        if (mouseX > x && mouseY > y && mouseX <= x + 18 && mouseY <= y + 18) {
            this.guiSchematicMaterials.renderToolTip(itemStack, mouseX, mouseY);
            GlStateManager.disableLighting();
        }
    }
}
