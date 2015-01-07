package com.github.lunatrius.schematica.client.renderer;

import com.github.lunatrius.core.util.vector.Vector3d;
import com.github.lunatrius.core.util.vector.Vector3f;
import com.github.lunatrius.schematica.client.renderer.shader.ShaderProgram;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.ArrayList;
import java.util.List;

public class RendererSchematicChunk {
    private static final ShaderProgram SHADER_ALPHA = new ShaderProgram("schematica", null, "shaders/alpha.frag");

    private static boolean canUpdate = false;

    public boolean isInFrustrum = false;

    public final Vector3d centerPosition = new Vector3d();

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final Profiler profiler = this.minecraft.mcProfiler;
    private final SchematicWorld schematic;
    private final List<TileEntity> tileEntities = new ArrayList<TileEntity>();
    private final Vector3d distance = new Vector3d();

    private final AxisAlignedBB boundingBox = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);

    private boolean needsUpdate = true;
    private int glList = -1;
    // TODO: move this away from GL lists
    private int glListHighlight = -1;

    public RendererSchematicChunk(SchematicWorld schematicWorld, int baseX, int baseY, int baseZ) {
        this.schematic = schematicWorld;
        this.boundingBox.setBounds(baseX * Constants.SchematicChunk.WIDTH, baseY * Constants.SchematicChunk.HEIGHT, baseZ * Constants.SchematicChunk.LENGTH, (baseX + 1) * Constants.SchematicChunk.WIDTH, (baseY + 1) * Constants.SchematicChunk.HEIGHT, (baseZ + 1) * Constants.SchematicChunk.LENGTH);

        this.centerPosition.x = (int) ((baseX + 0.5) * Constants.SchematicChunk.WIDTH);
        this.centerPosition.y = (int) ((baseY + 0.5) * Constants.SchematicChunk.HEIGHT);
        this.centerPosition.z = (int) ((baseZ + 0.5) * Constants.SchematicChunk.LENGTH);

        int x, y, z;
        for (TileEntity tileEntity : this.schematic.getTileEntities()) {
            x = tileEntity.xCoord;
            y = tileEntity.yCoord;
            z = tileEntity.zCoord;

            if (x < this.boundingBox.minX || x >= this.boundingBox.maxX) {
                continue;
            } else if (z < this.boundingBox.minZ || z >= this.boundingBox.maxZ) {
                continue;
            } else if (y < this.boundingBox.minY || y >= this.boundingBox.maxY) {
                continue;
            }

            this.tileEntities.add(tileEntity);
        }

        this.glList = GL11.glGenLists(3);
        this.glListHighlight = GL11.glGenLists(3);
    }

    public void delete() {
        if (this.glList != -1) {
            GL11.glDeleteLists(this.glList, 3);
        }
        if (this.glListHighlight != -1) {
            GL11.glDeleteLists(this.glListHighlight, 3);
        }
    }

    public AxisAlignedBB getBoundingBox() {
        return this.boundingBox;
    }

    public static void setCanUpdate(boolean parCanUpdate) {
        canUpdate = parCanUpdate;
    }

    public static boolean getCanUpdate() {
        return canUpdate;
    }

    public void setDirty() {
        this.needsUpdate = true;
    }

    public boolean getDirty() {
        return this.needsUpdate;
    }

    public void updateRenderer() {
        if (this.needsUpdate) {
            this.needsUpdate = false;
            setCanUpdate(false);

            RenderHelper.createBuffers();

            for (int pass = 0; pass < 3; pass++) {
                RenderHelper.initBuffers();

                int minX, maxX, minY, maxY, minZ, maxZ;

                minX = (int) this.boundingBox.minX;
                maxX = Math.min((int) this.boundingBox.maxX, this.schematic.getWidth());
                minY = (int) this.boundingBox.minY;
                maxY = Math.min((int) this.boundingBox.maxY, this.schematic.getHeight());
                minZ = (int) this.boundingBox.minZ;
                maxZ = Math.min((int) this.boundingBox.maxZ, this.schematic.getLength());

                int renderingLayer = this.schematic.renderingLayer;
                if (this.schematic.isRenderingLayer) {
                    if (renderingLayer >= minY && renderingLayer < maxY) {
                        minY = renderingLayer;
                        maxY = renderingLayer + 1;
                    } else {
                        minY = maxY = 0;
                    }
                }

                GL11.glNewList(this.glList + pass, GL11.GL_COMPILE);
                renderBlocks(pass, minX, minY, minZ, maxX, maxY, maxZ);
                GL11.glEndList();

                GL11.glNewList(this.glListHighlight + pass, GL11.GL_COMPILE);
                int quadCount = RenderHelper.getQuadCount();
                int lineCount = RenderHelper.getLineCount();

                if (quadCount > 0 || lineCount > 0) {
                    GL11.glDisable(GL11.GL_TEXTURE_2D);

                    GL11.glLineWidth(1.5f);

                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

                    if (quadCount > 0) {
                        GL11.glVertexPointer(3, 0, RenderHelper.getQuadVertexBuffer());
                        GL11.glColorPointer(4, 0, RenderHelper.getQuadColorBuffer());
                        GL11.glDrawArrays(GL11.GL_QUADS, 0, quadCount);
                    }

                    if (lineCount > 0) {
                        GL11.glVertexPointer(3, 0, RenderHelper.getLineVertexBuffer());
                        GL11.glColorPointer(4, 0, RenderHelper.getLineColorBuffer());
                        GL11.glDrawArrays(GL11.GL_LINES, 0, lineCount);
                    }

                    GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
                    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                }

                GL11.glEndList();
            }

            RenderHelper.destroyBuffers();
        }
    }

    public void render(int renderPass) {
        if (!this.isInFrustrum) {
            return;
        }

        if (this.distance.set(ClientProxy.playerPosition).sub(this.schematic.position.x, this.schematic.position.y, this.schematic.position.z).sub(this.centerPosition).lengthSquared() > 25600) {
            return;
        }

        // some mods enable this, beats me why - it's supposed to be disabled!
        GL11.glDisable(GL11.GL_LIGHTING);

        this.profiler.startSection("blocks");
        this.minecraft.renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        if (OpenGlHelper.shadersSupported && ConfigurationHandler.enableAlpha) {
            GL20.glUseProgram(SHADER_ALPHA.getProgram());
            GL20.glUniform1f(GL20.glGetUniformLocation(SHADER_ALPHA.getProgram(), "alpha_multiplier"), ConfigurationHandler.alpha);
        }

        GL11.glCallList(this.glList + renderPass);

        if (OpenGlHelper.shadersSupported && ConfigurationHandler.enableAlpha) {
            GL20.glUseProgram(0);
        }

        this.profiler.endStartSection("highlight");
        GL11.glCallList(this.glListHighlight + renderPass);

        this.profiler.endStartSection("tileEntities");
        renderTileEntities(renderPass);

        // re-enable blending... spawners disable it, somewhere...
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // re-set alpha func... beacons set it to (GL_GREATER, 0.5f)
        // EntityRenderer sets it to (GL_GREATER, 0.1f) before dispatching the event
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);

        this.profiler.endSection();
    }

    public void renderBlocks(int renderPass, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        IBlockAccess mcWorld = this.minecraft.theWorld;
        RenderBlocks renderBlocks = RendererSchematicGlobal.INSTANCE.renderBlocks;

        int x, y, z, wx, wy, wz;
        int sides;
        Block block, mcBlock;
        Vector3f zero = new Vector3f();
        Vector3f size = new Vector3f();

        int ambientOcclusion = this.minecraft.gameSettings.ambientOcclusion;
        this.minecraft.gameSettings.ambientOcclusion = 0;

        Tessellator.instance.startDrawingQuads();

        for (y = minY; y < maxY; y++) {
            for (z = minZ; z < maxZ; z++) {
                for (x = minX; x < maxX; x++) {
                    try {
                        block = this.schematic.getBlock(x, y, z);

                        wx = this.schematic.position.x + x;
                        wy = this.schematic.position.y + y;
                        wz = this.schematic.position.z + z;

                        mcBlock = mcWorld.getBlock(wx, wy, wz);

                        sides = 0;
                        if (block != null) {
                            if (block.shouldSideBeRendered(this.schematic, x, y - 1, z, 0)) {
                                sides |= RenderHelper.QUAD_DOWN;
                            }

                            if (block.shouldSideBeRendered(this.schematic, x, y + 1, z, 1)) {
                                sides |= RenderHelper.QUAD_UP;
                            }

                            if (block.shouldSideBeRendered(this.schematic, x, y, z - 1, 2)) {
                                sides |= RenderHelper.QUAD_NORTH;
                            }

                            if (block.shouldSideBeRendered(this.schematic, x, y, z + 1, 3)) {
                                sides |= RenderHelper.QUAD_SOUTH;
                            }

                            if (block.shouldSideBeRendered(this.schematic, x - 1, y, z, 4)) {
                                sides |= RenderHelper.QUAD_WEST;
                            }

                            if (block.shouldSideBeRendered(this.schematic, x + 1, y, z, 5)) {
                                sides |= RenderHelper.QUAD_EAST;
                            }
                        }

                        boolean isAirBlock = mcWorld.isAirBlock(wx, wy, wz);

                        if (!isAirBlock) {
                            if (ConfigurationHandler.highlight && renderPass == 2) {
                                if (block == Blocks.air && ConfigurationHandler.highlightAir) {
                                    zero.set(x, y, z);
                                    size.set(x + 1, y + 1, z + 1);
                                    if (ConfigurationHandler.drawQuads) {
                                        RenderHelper.drawCuboidSurface(zero, size, RenderHelper.QUAD_ALL, 0.75f, 0.0f, 0.75f, 0.25f);
                                    }
                                    if (ConfigurationHandler.drawLines) {
                                        RenderHelper.drawCuboidOutline(zero, size, RenderHelper.LINE_ALL, 0.75f, 0.0f, 0.75f, 0.25f);
                                    }
                                } else if (block != mcBlock) {
                                    zero.set(x, y, z);
                                    size.set(x + 1, y + 1, z + 1);
                                    if (ConfigurationHandler.drawQuads) {
                                        RenderHelper.drawCuboidSurface(zero, size, sides, 1.0f, 0.0f, 0.0f, 0.25f);
                                    }
                                    if (ConfigurationHandler.drawLines) {
                                        RenderHelper.drawCuboidOutline(zero, size, sides, 1.0f, 0.0f, 0.0f, 0.25f);
                                    }
                                } else if (this.schematic.getBlockMetadata(x, y, z) != mcWorld.getBlockMetadata(wx, wy, wz)) {
                                    zero.set(x, y, z);
                                    size.set(x + 1, y + 1, z + 1);
                                    if (ConfigurationHandler.drawQuads) {
                                        RenderHelper.drawCuboidSurface(zero, size, sides, 0.75f, 0.35f, 0.0f, 0.25f);
                                    }
                                    if (ConfigurationHandler.drawLines) {
                                        RenderHelper.drawCuboidOutline(zero, size, sides, 0.75f, 0.35f, 0.0f, 0.25f);
                                    }
                                }
                            }
                        } else if (block != Blocks.air) {
                            if (ConfigurationHandler.highlight && renderPass == 2) {
                                zero.set(x, y, z);
                                size.set(x + 1, y + 1, z + 1);
                                if (ConfigurationHandler.drawQuads) {
                                    RenderHelper.drawCuboidSurface(zero, size, sides, 0.0f, 0.75f, 1.0f, 0.25f);
                                }
                                if (ConfigurationHandler.drawLines) {
                                    RenderHelper.drawCuboidOutline(zero, size, sides, 0.0f, 0.75f, 1.0f, 0.25f);
                                }
                            }

                            if (block != null && block.canRenderInPass(renderPass)) {
                                renderBlocks.renderBlockByRenderType(block, x, y, z);
                            }
                        }
                    } catch (Exception e) {
                        Reference.logger.error("Failed to render block!", e);
                    }
                }
            }
        }

        Tessellator.instance.draw();

        this.minecraft.gameSettings.ambientOcclusion = ambientOcclusion;
    }

    public void renderTileEntities(int renderPass) {
        if (renderPass != 0) {
            return;
        }

        IBlockAccess mcWorld = this.minecraft.theWorld;

        int x, y, z;
        Block mcBlock;

        GL11.glColor4f(1.0f, 1.0f, 1.0f, ConfigurationHandler.alpha);

        try {
            for (TileEntity tileEntity : this.tileEntities) {
                x = tileEntity.xCoord;
                y = tileEntity.yCoord;
                z = tileEntity.zCoord;

                if (this.schematic.isRenderingLayer && this.schematic.renderingLayer != y) {
                    continue;
                }

                mcBlock = mcWorld.getBlock(x + this.schematic.position.x, y + this.schematic.position.y, z + this.schematic.position.z);

                if (mcBlock == Blocks.air) {
                    TileEntitySpecialRenderer tileEntitySpecialRenderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(tileEntity);
                    if (tileEntitySpecialRenderer != null) {
                        try {
                            tileEntitySpecialRenderer.renderTileEntityAt(tileEntity, x, y, z, 0);

                            OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                            GL11.glDisable(GL11.GL_TEXTURE_2D);
                            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
                        } catch (Exception e) {
                            Reference.logger.error("Failed to render a tile entity!", e);
                        }
                        GL11.glColor4f(1.0f, 1.0f, 1.0f, ConfigurationHandler.alpha);
                    }
                }
            }
        } catch (Exception ex) {
            Reference.logger.error("Failed to render tile entities!", ex);
        }
    }
}
