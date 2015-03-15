package com.github.lunatrius.schematica.tooltip;

import com.github.lunatrius.core.client.gui.FontRendererHelper;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.List;

public class TooltipHandler {
    public static final TooltipHandler INSTANCE = new TooltipHandler();

    private static final FMLControlledNamespacedRegistry<Block> BLOCK_REGISTRY = GameData.getBlockRegistry();
    private static final int PADDING = 6;

    private final Minecraft minecraft = Minecraft.getMinecraft();

    private int width = 1280;
    private int height = 720;

    public boolean renderTooltip(final SchematicWorld schematic, final MovingObjectPosition objectMouseOver) {
        if (objectMouseOver != null) {
            if (objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) {
                return false;
            }

            if (this.minecraft.gameSettings.showDebugInfo || this.minecraft.currentScreen != null && !(this.minecraft.currentScreen instanceof GuiChat)) {
                return false;
            }

            final List<String> lines = getText(schematic, objectMouseOver.getBlockPos());

            if (!lines.isEmpty()) {
                final ScaledResolution scaledResolution = new ScaledResolution(this.minecraft, this.minecraft.displayWidth, this.minecraft.displayHeight);
                this.width = scaledResolution.getScaledWidth();
                this.height = scaledResolution.getScaledHeight();

                drawHoveringText(lines, ConfigurationHandler.tooltipX, ConfigurationHandler.tooltipY, this.minecraft.fontRendererObj);
            }

            GL11.glDisable(GL11.GL_LIGHTING);
        }

        return false;
    }

    private List<String> getText(final SchematicWorld schematic, final BlockPos pos) {
        final List<String> list = new ArrayList<String>();

        final IBlockState blockState = schematic.getBlockState(pos);
        final Block block = blockState.getBlock();
        final int blockMetadata = block.getMetaFromState(blockState);

        list.add(block.getLocalizedName());
        list.add("\u00a77" + BLOCK_REGISTRY.getNameForObject(block) + "\u00a7r : " + blockMetadata);

        return list;
    }

    private void drawHoveringText(final List<String> lines, final float x, final float y, final FontRenderer fontRenderer) {
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        int width = 0;
        int height = lines.size() * (fontRenderer.FONT_HEIGHT + 1) - 1;
        for (String line : lines) {
            int strWidth = fontRenderer.getStringWidth(line);

            if (strWidth > width) {
                width = strWidth;
            }
        }

        int posX = MathHelper.clamp_int((int) (this.width / 100f * x), width / 2 + PADDING, this.width - (width + 1) / 2 - PADDING);
        int posY = MathHelper.clamp_int((int) (this.height / 100f * y), PADDING, this.height - height - PADDING);

        posX -= width / 2;

        final int colorBackground = 0xF0100010;
        drawGradientRect(posX - 3, posY - 4, posX + width + 3, posY - 3, colorBackground, colorBackground);
        drawGradientRect(posX - 3, posY + height + 3, posX + width + 3, posY + height + 4, colorBackground, colorBackground);
        drawGradientRect(posX - 3, posY - 3, posX + width + 3, posY + height + 3, colorBackground, colorBackground);
        drawGradientRect(posX - 4, posY - 3, posX - 3, posY + height + 3, colorBackground, colorBackground);
        drawGradientRect(posX + width + 3, posY - 3, posX + width + 4, posY + height + 3, colorBackground, colorBackground);

        final int colorTop = 0x505000FF;
        final int colorBottom = (colorTop & 0xFEFEFE) >> 1 | colorTop & 0xFF000000;
        drawGradientRect(posX - 3, posY - 3 + 1, posX - 3 + 1, posY + height + 3 - 1, colorTop, colorBottom);
        drawGradientRect(posX + width + 2, posY - 3 + 1, posX + width + 3, posY + height + 3 - 1, colorTop, colorBottom);
        drawGradientRect(posX - 3, posY - 3, posX + width + 3, posY - 3 + 1, colorTop, colorTop);
        drawGradientRect(posX - 3, posY + height + 2, posX + width + 3, posY + height + 3, colorBottom, colorBottom);

        posX += width / 2;

        for (String line : lines) {
            FontRendererHelper.drawCenteredString(fontRenderer, line, posX, posY, 0xFFFFFFFF);
            posY += 10;
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        RenderHelper.enableStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    }

    private void drawGradientRect(final int x0, final int y0, final int x1, final int y1, final int colorA, final int colorB) {
        float alphaA = (colorA >> 24 & 255) / 255f;
        float redA = (colorA >> 16 & 255) / 255f;
        float greenA = (colorA >> 8 & 255) / 255f;
        float blueA = (colorA & 255) / 255f;
        float alphaB = (colorB >> 24 & 255) / 255f;
        float redB = (colorB >> 16 & 255) / 255f;
        float greenB = (colorB >> 8 & 255) / 255f;
        float blueB = (colorB & 255) / 255f;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.startDrawingQuads();
        worldRenderer.setColorRGBA_F(redA, greenA, blueA, alphaA);
        worldRenderer.addVertex(x1, y0, 300);
        worldRenderer.addVertex(x0, y0, 300);
        worldRenderer.setColorRGBA_F(redB, greenB, blueB, alphaB);
        worldRenderer.addVertex(x0, y1, 300);
        worldRenderer.addVertex(x1, y1, 300);
        tessellator.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
