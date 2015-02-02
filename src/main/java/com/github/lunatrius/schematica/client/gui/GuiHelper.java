package com.github.lunatrius.schematica.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiHelper {
    private static final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

    public static void drawItemStack(TextureManager textureManager, FontRenderer fontRenderer, int x, int y, ItemStack itemStack) {
        drawItemStackSlot(textureManager, x, y);

        if (itemStack != null && itemStack.getItem() != null) {
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.enableGUIStandardItemLighting();
            renderItem.renderItemIntoGUI(itemStack, x + 2, y + 2);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        }
    }

    public static void drawItemStackSlot(TextureManager textureManager, int x, int y) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        textureManager.bindTexture(Gui.statIcons);
        Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.startDrawingQuads();
        worldRenderer.addVertexWithUV(x + 1 + 0, y + 1 + 18, 0, 0 * 0.0078125f, 18 * 0.0078125f);
        worldRenderer.addVertexWithUV(x + 1 + 18, y + 1 + 18, 0, 18 * 0.0078125f, 18 * 0.0078125f);
        worldRenderer.addVertexWithUV(x + 1 + 18, y + 1 + 0, 0, 18 * 0.0078125f, 0 * 0.0078125f);
        worldRenderer.addVertexWithUV(x + 1 + 0, y + 1 + 0, 0, 0 * 0.0078125f, 0 * 0.0078125f);
        tessellator.draw();
    }
}
