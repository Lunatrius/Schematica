package com.github.lunatrius.schematica.client.renderer;

import com.github.lunatrius.schematica.client.renderer.vertex.VertexFormats;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.BlockPos;
import org.lwjgl.opengl.GL11;

public class TessellatorShape extends Tessellator {
    public static final int QUAD_DOWN = 0x01;
    public static final int QUAD_UP = 0x02;
    public static final int QUAD_NORTH = 0x04;
    public static final int QUAD_SOUTH = 0x08;
    public static final int QUAD_WEST = 0x10;
    public static final int QUAD_EAST = 0x20;
    public static final int QUAD_ALL = QUAD_DOWN | QUAD_UP | QUAD_NORTH | QUAD_SOUTH | QUAD_WEST | QUAD_EAST;

    public static final int LINE_DOWN_WEST = 0x11;
    public static final int LINE_UP_WEST = 0x12;
    public static final int LINE_DOWN_EAST = 0x21;
    public static final int LINE_UP_EAST = 0x22;
    public static final int LINE_DOWN_NORTH = 0x05;
    public static final int LINE_UP_NORTH = 0x06;
    public static final int LINE_DOWN_SOUTH = 0x09;
    public static final int LINE_UP_SOUTH = 0x0A;
    public static final int LINE_NORTH_WEST = 0x14;
    public static final int LINE_NORTH_EAST = 0x24;
    public static final int LINE_SOUTH_WEST = 0x18;
    public static final int LINE_SOUTH_EAST = 0x28;
    public static final int LINE_ALL = LINE_DOWN_WEST | LINE_UP_WEST | LINE_DOWN_EAST | LINE_UP_EAST | LINE_DOWN_NORTH | LINE_UP_NORTH | LINE_DOWN_SOUTH | LINE_UP_SOUTH | LINE_NORTH_WEST | LINE_NORTH_EAST | LINE_SOUTH_WEST | LINE_SOUTH_EAST;

    private int mode = -1;

    public TessellatorShape(final int size) {
        super(size);
    }

    public void setTranslation(final double x, final double y, final double z) {
        getWorldRenderer().setTranslation(x, y, z);
    }

    public void startQuads() {
        start(GL11.GL_QUADS);
    }

    public void startLines() {
        start(GL11.GL_LINES);
    }

    public void start(final int mode) {
        this.mode = mode;
        getWorldRenderer().startDrawing(mode);
        getWorldRenderer().setVertexFormat(VertexFormats.ABSTRACT);
    }

    @Override
    public int draw() {
        this.mode = -1;
        return super.draw();
    }

    public void drawCuboid(final BlockPos pos, final int sides, final float red, final float green, final float blue, final float alpha) {
        drawCuboid(pos, pos, sides, red, green, blue, alpha);
    }

    public void drawCuboid(final BlockPos begin, final BlockPos end, final int sides, final float red, final float green, final float blue, final float alpha) {
        if (this.mode == -1 || sides == 0) {
            return;
        }

        final double x0 = begin.getX() - ConfigurationHandler.blockDelta;
        final double y0 = begin.getY() - ConfigurationHandler.blockDelta;
        final double z0 = begin.getZ() - ConfigurationHandler.blockDelta;
        final double x1 = end.getX() + 1 + ConfigurationHandler.blockDelta;
        final double y1 = end.getY() + 1 + ConfigurationHandler.blockDelta;
        final double z1 = end.getZ() + 1 + ConfigurationHandler.blockDelta;

        if (this.mode == GL11.GL_QUADS) {
            drawQuads(x0, y0, z0, x1, y1, z1, sides, red, green, blue, alpha);
        } else if (this.mode == GL11.GL_LINES) {
            drawLines(x0, y0, z0, x1, y1, z1, sides, red, green, blue, alpha);
        } else {
            throw new IllegalStateException("Unsupported mode!");
        }
    }

    private void drawQuads(final double x0, final double y0, final double z0, final double x1, final double y1, final double z1, final int sides, final float red, final float green, final float blue, final float alpha) {
        final WorldRenderer worldRenderer = getWorldRenderer();
        worldRenderer.setColorRGBA_F(red, green, blue, alpha);

        if ((sides & QUAD_DOWN) != 0) {
            worldRenderer.addVertex(x1, y0, z0);
            worldRenderer.addVertex(x1, y0, z1);
            worldRenderer.addVertex(x0, y0, z1);
            worldRenderer.addVertex(x0, y0, z0);
        }

        if ((sides & QUAD_UP) != 0) {
            worldRenderer.addVertex(x1, y1, z0);
            worldRenderer.addVertex(x0, y1, z0);
            worldRenderer.addVertex(x0, y1, z1);
            worldRenderer.addVertex(x1, y1, z1);
        }

        if ((sides & QUAD_NORTH) != 0) {
            worldRenderer.addVertex(x1, y0, z0);
            worldRenderer.addVertex(x0, y0, z0);
            worldRenderer.addVertex(x0, y1, z0);
            worldRenderer.addVertex(x1, y1, z0);
        }

        if ((sides & QUAD_SOUTH) != 0) {
            worldRenderer.addVertex(x0, y0, z1);
            worldRenderer.addVertex(x1, y0, z1);
            worldRenderer.addVertex(x1, y1, z1);
            worldRenderer.addVertex(x0, y1, z1);
        }

        if ((sides & QUAD_WEST) != 0) {
            worldRenderer.addVertex(x0, y0, z0);
            worldRenderer.addVertex(x0, y0, z1);
            worldRenderer.addVertex(x0, y1, z1);
            worldRenderer.addVertex(x0, y1, z0);
        }

        if ((sides & QUAD_EAST) != 0) {
            worldRenderer.addVertex(x1, y0, z1);
            worldRenderer.addVertex(x1, y0, z0);
            worldRenderer.addVertex(x1, y1, z0);
            worldRenderer.addVertex(x1, y1, z1);
        }
    }

    private void drawLines(final double x0, final double y0, final double z0, final double x1, final double y1, final double z1, final int sides, final float red, final float green, final float blue, final float alpha) {
        final WorldRenderer worldRenderer = getWorldRenderer();
        worldRenderer.setColorRGBA_F(red, green, blue, alpha);

        if ((sides & LINE_DOWN_WEST) != 0) {
            worldRenderer.addVertex(x0, y0, z0);
            worldRenderer.addVertex(x0, y0, z1);
        }

        if ((sides & LINE_UP_WEST) != 0) {
            worldRenderer.addVertex(x0, y1, z0);
            worldRenderer.addVertex(x0, y1, z1);
        }

        if ((sides & LINE_DOWN_EAST) != 0) {
            worldRenderer.addVertex(x1, y0, z0);
            worldRenderer.addVertex(x1, y0, z1);
        }

        if ((sides & LINE_UP_EAST) != 0) {
            worldRenderer.addVertex(x1, y1, z0);
            worldRenderer.addVertex(x1, y1, z1);
        }

        if ((sides & LINE_DOWN_NORTH) != 0) {
            worldRenderer.addVertex(x0, y0, z0);
            worldRenderer.addVertex(x1, y0, z0);
        }

        if ((sides & LINE_UP_NORTH) != 0) {
            worldRenderer.addVertex(x0, y1, z0);
            worldRenderer.addVertex(x1, y1, z0);
        }

        if ((sides & LINE_DOWN_SOUTH) != 0) {
            worldRenderer.addVertex(x0, y0, z1);
            worldRenderer.addVertex(x1, y0, z1);
        }

        if ((sides & LINE_UP_SOUTH) != 0) {
            worldRenderer.addVertex(x0, y1, z1);
            worldRenderer.addVertex(x1, y1, z1);
        }

        if ((sides & LINE_NORTH_WEST) != 0) {
            worldRenderer.addVertex(x0, y0, z0);
            worldRenderer.addVertex(x0, y1, z0);
        }

        if ((sides & LINE_NORTH_EAST) != 0) {
            worldRenderer.addVertex(x1, y0, z0);
            worldRenderer.addVertex(x1, y1, z0);
        }

        if ((sides & LINE_SOUTH_WEST) != 0) {
            worldRenderer.addVertex(x0, y0, z1);
            worldRenderer.addVertex(x0, y1, z1);
        }

        if ((sides & LINE_SOUTH_EAST) != 0) {
            worldRenderer.addVertex(x1, y0, z1);
            worldRenderer.addVertex(x1, y1, z1);
        }
    }
}
