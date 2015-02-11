package com.github.lunatrius.schematica.client.renderer;

import com.github.lunatrius.core.util.MBlockPos;
import com.github.lunatrius.core.util.vector.Vector3d;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.client.renderer.shader.ShaderProgram;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.world.SchematicWorld;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.ChunkRenderContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderList;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VboRenderList;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.IRenderChunkFactory;
import net.minecraft.client.renderer.chunk.ListChunkFactory;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.VboChunkFactory;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IWorldAccess;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import javax.vecmath.Vector3f;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class RenderSchematic extends RenderGlobal implements IWorldAccess, IResourceManagerReloadListener {
    public static final RenderSchematic INSTANCE = new RenderSchematic(Minecraft.getMinecraft());

    public static final int RENDER_DISTANCE = 32;
    public static final int CHUNKS_XZ = (RENDER_DISTANCE + 1) * 2;
    public static final int CHUNKS_Y = 16;
    public static final int CHUNKS = CHUNKS_XZ * CHUNKS_XZ * CHUNKS_Y;
    public static final int PASS = 2;

    private static final ShaderProgram SHADER_ALPHA = new ShaderProgram("schematica", null, "shaders/alpha.frag");
    private static final Vector3d PLAYER_POSITION_OFFSET = new Vector3d();
    private final Minecraft mc;
    private final Profiler profiler;
    private final RenderManager renderManager;
    private final TessellatorShape tessellator = new TessellatorShape(0x200000);
    private final MBlockPos tmp = new MBlockPos();
    private SchematicWorld world;
    private Set<RenderChunk> chunksToUpdate = Sets.newLinkedHashSet();
    private List<RenderSchematic.ContainerLocalRenderInformation> renderInfos = Lists.newArrayListWithCapacity(CHUNKS);
    private ViewFrustum viewFrustum;
    private double frustumUpdatePosX = Double.MIN_VALUE;
    private double frustumUpdatePosY = Double.MIN_VALUE;
    private double frustumUpdatePosZ = Double.MIN_VALUE;
    private int frustumUpdatePosChunkX = Integer.MIN_VALUE;
    private int frustumUpdatePosChunkY = Integer.MIN_VALUE;
    private int frustumUpdatePosChunkZ = Integer.MIN_VALUE;
    private double lastViewEntityX = Double.MIN_VALUE;
    private double lastViewEntityY = Double.MIN_VALUE;
    private double lastViewEntityZ = Double.MIN_VALUE;
    private double lastViewEntityPitch = Double.MIN_VALUE;
    private double lastViewEntityYaw = Double.MIN_VALUE;
    private final ChunkRenderDispatcher renderDispatcher = new ChunkRenderDispatcher();
    private ChunkRenderContainer renderContainer;
    private int renderDistanceChunks = -1;
    private int countEntitiesTotal;
    private int countEntitiesRendered;
    private int countTileEntitiesTotal;
    private int countTileEntitiesRendered;
    private boolean vboEnabled = false;
    private IRenderChunkFactory renderChunkFactory;
    private double prevRenderSortX;
    private double prevRenderSortY;
    private double prevRenderSortZ;
    private boolean displayListEntitiesDirty = true;
    private int frameCount = 0;

    public RenderSchematic(final Minecraft minecraft) {
        super(minecraft);
        this.mc = minecraft;
        this.profiler = minecraft.mcProfiler;
        this.renderManager = minecraft.getRenderManager();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager.bindTexture(0);
        this.vboEnabled = OpenGlHelper.useVbo();

        if (this.vboEnabled) {
            this.renderContainer = new VboRenderList();
            this.renderChunkFactory = new VboChunkFactory();
        } else {
            this.renderContainer = new RenderList();
            this.renderChunkFactory = new ListChunkFactory();
        }
    }

    @Override
    public void onResourceManagerReload(final IResourceManager resourceManager) {}

    @Override
    public void makeEntityOutlineShader() {}

    @Override
    public void renderEntityOutlineFramebuffer() {}

    @Override
    protected boolean isRenderEntityOutlines() {
        return false;
    }

    @Override
    public void setWorldAndLoadRenderers(final WorldClient worldClient) {
        if (worldClient instanceof SchematicWorld) {
            setWorldAndLoadRenderers((SchematicWorld) worldClient);
        } else {
            setWorldAndLoadRenderers(null);
        }
    }

    public void setWorldAndLoadRenderers(final SchematicWorld world) {
        if (this.world != null) {
            this.world.removeWorldAccess(this);
        }

        this.frustumUpdatePosX = Double.MIN_VALUE;
        this.frustumUpdatePosY = Double.MIN_VALUE;
        this.frustumUpdatePosZ = Double.MIN_VALUE;
        this.frustumUpdatePosChunkX = Integer.MIN_VALUE;
        this.frustumUpdatePosChunkY = Integer.MIN_VALUE;
        this.frustumUpdatePosChunkZ = Integer.MIN_VALUE;
        this.renderManager.set(world);
        this.world = world;

        if (world != null) {
            world.addWorldAccess(this);
            loadRenderers();
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        EntityPlayerSP player = this.mc.thePlayer;
        if (player != null) {
            ClientProxy.setPlayerData(player, event.partialTicks);

            this.profiler.startSection("schematica");
            this.profiler.startSection("schematic");
            SchematicWorld schematic = Schematica.proxy.getActiveSchematic();

            if (schematic != null && schematic.isRendering) {
                GlStateManager.pushMatrix();
                renderSchematic(schematic, event.partialTicks);
                GlStateManager.popMatrix();
            }

            this.profiler.endStartSection("guide");
            if (ClientProxy.isRenderingGuide || schematic != null && schematic.isRendering) {
                GlStateManager.pushMatrix();
                GlStateManager.disableTexture2D();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

                this.tessellator.setTranslation(-ClientProxy.playerPosition.x, -ClientProxy.playerPosition.y, -ClientProxy.playerPosition.z);

                if (ClientProxy.isRenderingGuide) {
                    this.tessellator.startQuads();
                    this.tessellator.drawCuboid(ClientProxy.pointA, TessellatorShape.QUAD_ALL, 0.75f, 0.0f, 0.0f, 0.25f);
                    this.tessellator.drawCuboid(ClientProxy.pointB, TessellatorShape.QUAD_ALL, 0.0f, 0.0f, 0.75f, 0.25f);
                    this.tessellator.draw();
                }

                this.tessellator.startLines();
                if (ClientProxy.isRenderingGuide) {
                    this.tessellator.drawCuboid(ClientProxy.pointA, TessellatorShape.QUAD_ALL, 0.75f, 0.0f, 0.0f, 0.25f);
                    this.tessellator.drawCuboid(ClientProxy.pointB, TessellatorShape.QUAD_ALL, 0.0f, 0.0f, 0.75f, 0.25f);
                    this.tessellator.drawCuboid(ClientProxy.pointMin, ClientProxy.pointMax, TessellatorShape.QUAD_ALL, 0.0f, 0.75f, 0.0f, 0.5f);
                }
                if (schematic != null && schematic.isRendering) {
                    this.tmp.set(schematic.position).add(schematic.getWidth() - 1, schematic.getHeight() - 1, schematic.getLength() - 1);
                    this.tessellator.drawCuboid(schematic.position, this.tmp, TessellatorShape.LINE_ALL, 0.75f, 0.0f, 0.75f, 0.5f);
                }
                this.tessellator.draw();

                GlStateManager.disableBlend();
                GlStateManager.enableTexture2D();
                GlStateManager.popMatrix();
            }

            this.profiler.endSection();
            this.profiler.endSection();
        }
    }

    private void renderSchematic(final SchematicWorld schematic, final float partialTicks) {
        if (this.world != schematic) {
            this.world = schematic;

            loadRenderers();
        }

        PLAYER_POSITION_OFFSET.set(ClientProxy.playerPosition).sub(this.world.position.x, this.world.position.y, this.world.position.z);

        if (OpenGlHelper.shadersSupported && ConfigurationHandler.enableAlpha) {
            GL20.glUseProgram(SHADER_ALPHA.getProgram());
            GL20.glUniform1f(GL20.glGetUniformLocation(SHADER_ALPHA.getProgram(), "alpha_multiplier"), ConfigurationHandler.alpha);
        }

        final int fps = Math.max(Minecraft.getDebugFPS(), 30);
        renderWorld(partialTicks, System.nanoTime() + 1000000000 / fps);

        if (OpenGlHelper.shadersSupported && ConfigurationHandler.enableAlpha) {
            GL20.glUseProgram(0);
        }
    }

    private void renderWorld(final float partialTicks, final long finishTimeNano) {
        GlStateManager.enableCull();
        this.profiler.endStartSection("culling");
        final Frustum frustum = new Frustum();
        final Entity entity = this.mc.getRenderViewEntity();

        final double x = PLAYER_POSITION_OFFSET.x;
        final double y = PLAYER_POSITION_OFFSET.y;
        final double z = PLAYER_POSITION_OFFSET.z;
        frustum.setPosition(x, y, z);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        this.profiler.endStartSection("prepareterrain");
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        RenderHelper.disableStandardItemLighting();

        this.profiler.endStartSection("terrain_setup");
        setupTerrain(entity, partialTicks, frustum, this.frameCount++, isInsideWorld(x, y, z));

        this.profiler.endStartSection("updatechunks");
        updateChunks(finishTimeNano);

        this.profiler.endStartSection("terrain");
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        renderBlockLayer(EnumWorldBlockLayer.SOLID, partialTicks, PASS, entity);
        renderBlockLayer(EnumWorldBlockLayer.CUTOUT_MIPPED, partialTicks, PASS, entity);
        this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
        renderBlockLayer(EnumWorldBlockLayer.CUTOUT, partialTicks, PASS, entity);
        this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        this.profiler.endStartSection("entities");
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        renderEntities(entity, frustum, partialTicks);
        GlStateManager.disableBlend();
        RenderHelper.disableStandardItemLighting();
        disableLightmap();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();

        GlStateManager.enableCull();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GlStateManager.depthMask(false);
        GlStateManager.pushMatrix();
        this.profiler.endStartSection("translucent");
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        renderBlockLayer(EnumWorldBlockLayer.TRANSLUCENT, partialTicks, PASS, entity);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableCull();
    }

    private boolean isInsideWorld(final double x, final double y, final double z) {
        return x >= -1 && y >= -1 && z >= -1 && x <= this.world.getWidth() && y <= this.world.getHeight() && z <= this.world.getLength();
    }

    private void disableLightmap() {
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public void refresh() {
        loadRenderers();
    }

    @Override
    public void loadRenderers() {
        if (this.world != null) {
            this.displayListEntitiesDirty = true;
            this.renderDistanceChunks = this.mc.gameSettings.renderDistanceChunks;
            final boolean vbo = this.vboEnabled;
            this.vboEnabled = OpenGlHelper.useVbo();

            if (vbo && !this.vboEnabled) {
                this.renderContainer = new RenderList();
                this.renderChunkFactory = new ListChunkFactory();
            } else if (!vbo && this.vboEnabled) {
                this.renderContainer = new VboRenderList();
                this.renderChunkFactory = new VboChunkFactory();
            }

            if (this.viewFrustum != null) {
                this.viewFrustum.deleteGlResources();
            }

            stopChunkUpdates();
            this.viewFrustum = new ViewFrustum(this.world, this.mc.gameSettings.renderDistanceChunks, this, this.renderChunkFactory);

            final double posX = PLAYER_POSITION_OFFSET.x;
            final double posZ = PLAYER_POSITION_OFFSET.z;
            this.viewFrustum.updateChunkPositions(posX, posZ);
        }
    }

    @Override
    protected void stopChunkUpdates() {
        this.chunksToUpdate.clear();
        this.renderDispatcher.stopChunkUpdates();
    }

    @Override
    public void createBindEntityOutlineFbs(final int p_72720_1_, final int p_72720_2_) {}

    @Override
    public void renderEntities(final Entity renderViewEntity, final ICamera camera, final float partialTicks) {
        final int entityPass = 0;

        this.profiler.startSection("prepare");
        TileEntityRendererDispatcher.instance.cacheActiveRenderInfo(this.world, this.mc.getTextureManager(), this.mc.fontRendererObj, this.mc.getRenderViewEntity(), partialTicks);
        this.renderManager.cacheActiveRenderInfo(this.world, this.mc.fontRendererObj, this.mc.getRenderViewEntity(), this.mc.pointedEntity, this.mc.gameSettings, partialTicks);

        this.countEntitiesTotal = 0;
        this.countEntitiesRendered = 0;

        this.countTileEntitiesTotal = 0;
        this.countTileEntitiesRendered = 0;

        final double x = PLAYER_POSITION_OFFSET.x;
        final double y = PLAYER_POSITION_OFFSET.y;
        final double z = PLAYER_POSITION_OFFSET.z;

        TileEntityRendererDispatcher.staticPlayerX = x;
        TileEntityRendererDispatcher.staticPlayerY = y;
        TileEntityRendererDispatcher.staticPlayerZ = z;

        this.renderManager.setRenderPosition(x, y, z);
        this.mc.entityRenderer.enableLightmap();

        this.profiler.endStartSection("blockentities");
        RenderHelper.enableStandardItemLighting();

        for (final ContainerLocalRenderInformation renderInfo : this.renderInfos) {
            for (final TileEntity tileEntity : (List<TileEntity>) renderInfo.renderChunk.getCompiledChunk().getTileEntities()) {
                final AxisAlignedBB renderBB = tileEntity.getRenderBoundingBox();

                this.countTileEntitiesTotal++;
                if (!tileEntity.shouldRenderInPass(entityPass) || !camera.isBoundingBoxInFrustum(renderBB)) {
                    continue;
                }

                TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, partialTicks, -1);
                this.countTileEntitiesRendered++;
            }
        }

        this.mc.entityRenderer.disableLightmap();
        this.profiler.endSection();
    }

    @Override
    public String getDebugInfoRenders() {
        final int total = this.viewFrustum.renderChunks.length;
        int rendered = 0;

        for (final ContainerLocalRenderInformation renderInfo : this.renderInfos) {
            final CompiledChunk compiledChunk = renderInfo.renderChunk.compiledChunk;

            if (compiledChunk != CompiledChunk.DUMMY && !compiledChunk.isEmpty()) {
                rendered++;
            }
        }

        return String.format("C: %d/%d %sD: %d, %s", rendered, total, this.mc.renderChunksMany ? "(s) " : "", this.renderDistanceChunks, this.renderDispatcher.getDebugInfo());
    }

    @Override
    public String getDebugInfoEntities() {
        return String.format("E: %d/%d", this.countEntitiesRendered, this.countEntitiesTotal);
    }

    public String getDebugInfoTileEntities() {
        return String.format("TE: %d/%d", this.countTileEntitiesRendered, this.countTileEntitiesTotal);
    }

    @Override
    public void setupTerrain(final Entity viewEntity, final double partialTicks, final ICamera camera, final int frameCount, final boolean playerSpectator) {
        if (this.mc.gameSettings.renderDistanceChunks != this.renderDistanceChunks) {
            loadRenderers();
        }

        this.profiler.startSection("camera");
        final double posX = PLAYER_POSITION_OFFSET.x;
        final double posY = PLAYER_POSITION_OFFSET.y;
        final double posZ = PLAYER_POSITION_OFFSET.z;

        final double deltaX = posX - this.frustumUpdatePosX;
        final double deltaY = posY - this.frustumUpdatePosY;
        final double deltaZ = posZ - this.frustumUpdatePosZ;

        final int chunkCoordX = MathHelper.floor_double(posX) >> 4;
        final int chunkCoordY = MathHelper.floor_double(posY) >> 4;
        final int chunkCoordZ = MathHelper.floor_double(posZ) >> 4;

        if (this.frustumUpdatePosChunkX != chunkCoordX || this.frustumUpdatePosChunkY != chunkCoordY || this.frustumUpdatePosChunkZ != chunkCoordZ || deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 16.0) {
            this.frustumUpdatePosX = posX;
            this.frustumUpdatePosY = posY;
            this.frustumUpdatePosZ = posZ;
            this.frustumUpdatePosChunkX = chunkCoordX;
            this.frustumUpdatePosChunkY = chunkCoordY;
            this.frustumUpdatePosChunkZ = chunkCoordZ;
            this.viewFrustum.updateChunkPositions(posX, posZ);
        }

        this.profiler.endStartSection("renderlistcamera");
        final double x = PLAYER_POSITION_OFFSET.x;
        final double y = PLAYER_POSITION_OFFSET.y;
        final double z = PLAYER_POSITION_OFFSET.z;
        this.renderContainer.initialize(x, y, z);

        this.profiler.endStartSection("culling");
        final BlockPos posEye = new BlockPos(x, y + viewEntity.getEyeHeight(), z);
        final RenderChunk renderchunk = this.viewFrustum.getRenderChunk(posEye);
        final BlockPos blockpos = new BlockPos(MathHelper.floor_double(x) & ~0xF, MathHelper.floor_double(y) & ~0xF, MathHelper.floor_double(z) & ~0xF);

        this.displayListEntitiesDirty = this.displayListEntitiesDirty || !this.chunksToUpdate.isEmpty() || posX != this.lastViewEntityX || posY != this.lastViewEntityY || posZ != this.lastViewEntityZ || viewEntity.rotationPitch != this.lastViewEntityPitch || viewEntity.rotationYaw != this.lastViewEntityYaw;
        this.lastViewEntityX = posX;
        this.lastViewEntityY = posY;
        this.lastViewEntityZ = posZ;
        this.lastViewEntityPitch = viewEntity.rotationPitch;
        this.lastViewEntityYaw = viewEntity.rotationYaw;

        if (this.displayListEntitiesDirty) {
            this.displayListEntitiesDirty = false;
            this.renderInfos = Lists.newArrayListWithCapacity(CHUNKS);

            final LinkedList<ContainerLocalRenderInformation> renderInfoList = Lists.newLinkedList();
            boolean renderChunksMany = this.mc.renderChunksMany;

            if (renderchunk == null) {
                final int chunkY = posEye.getY() > 0 ? 248 : 8;

                for (int chunkX = -this.renderDistanceChunks; chunkX <= this.renderDistanceChunks; chunkX++) {
                    for (int chunkZ = -this.renderDistanceChunks; chunkZ <= this.renderDistanceChunks; chunkZ++) {
                        final RenderChunk renderChunk = this.viewFrustum.getRenderChunk(new BlockPos((chunkX << 4) + 8, chunkY, (chunkZ << 4) + 8));

                        if (renderChunk != null && camera.isBoundingBoxInFrustum(renderChunk.boundingBox)) {
                            renderChunk.setFrameIndex(frameCount);
                            renderInfoList.add(new ContainerLocalRenderInformation(renderChunk, null, 0, null));
                        }
                    }
                }
            } else {
                boolean add = false;
                final ContainerLocalRenderInformation renderInfo = new ContainerLocalRenderInformation(renderchunk, null, 0, null);
                final Set<EnumFacing> visibleSides = getVisibleSides(posEye);

                if (!visibleSides.isEmpty() && visibleSides.size() == 1) {
                    final Vector3f viewVector = getViewVector(viewEntity, partialTicks);
                    final EnumFacing facing = EnumFacing.getFacingFromVector(viewVector.x, viewVector.y, viewVector.z).getOpposite();
                    visibleSides.remove(facing);
                }

                if (visibleSides.isEmpty()) {
                    add = true;
                }

                if (add && !playerSpectator) {
                    this.renderInfos.add(renderInfo);
                } else {
                    if (playerSpectator && this.world.getBlockState(posEye).getBlock().isOpaqueCube()) {
                        renderChunksMany = false;
                    }

                    renderchunk.setFrameIndex(frameCount);
                    renderInfoList.add(renderInfo);
                }
            }

            while (!renderInfoList.isEmpty()) {
                final ContainerLocalRenderInformation renderInfo = renderInfoList.poll();
                final RenderChunk renderChunk = renderInfo.renderChunk;
                final EnumFacing facing = renderInfo.facing;
                final BlockPos posChunk = renderChunk.getPosition();
                this.renderInfos.add(renderInfo);

                for (final EnumFacing side : EnumFacing.values()) {
                    final RenderChunk neighborRenderChunk = getNeighborRenderChunk(posEye, posChunk, side);

                    if ((!renderChunksMany || !renderInfo.setFacing.contains(side.getOpposite())) && (!renderChunksMany || facing == null || renderChunk.getCompiledChunk().isVisible(facing.getOpposite(), side)) && neighborRenderChunk != null && neighborRenderChunk.setFrameIndex(frameCount) && camera.isBoundingBoxInFrustum(neighborRenderChunk.boundingBox)) {
                        final ContainerLocalRenderInformation renderInfoNext = new ContainerLocalRenderInformation(neighborRenderChunk, side, renderInfo.counter + 1, null);
                        renderInfoNext.setFacing.addAll(renderInfo.setFacing);
                        renderInfoNext.setFacing.add(side);
                        renderInfoList.add(renderInfoNext);
                    }
                }
            }
        }

        this.renderDispatcher.clearChunkUpdates();
        final Set<RenderChunk> set = this.chunksToUpdate;
        this.chunksToUpdate = Sets.newLinkedHashSet();

        for (final ContainerLocalRenderInformation renderInfo : this.renderInfos) {
            final RenderChunk renderChunk = renderInfo.renderChunk;

            if (renderChunk.isNeedsUpdate() || renderChunk.isCompileTaskPending() || set.contains(renderChunk)) {
                this.displayListEntitiesDirty = true;

                // TODO: remove?
                if (false && isPositionInRenderChunk(blockpos, renderInfo.renderChunk)) {
                    this.profiler.startSection("build near");
                    this.renderDispatcher.updateChunkNow(renderChunk);
                    renderChunk.setNeedsUpdate(false);
                    this.profiler.endSection();
                } else {
                    this.chunksToUpdate.add(renderChunk);
                }
            }
        }

        this.chunksToUpdate.addAll(set);
        this.profiler.endSection();
    }

    private boolean isPositionInRenderChunk(final BlockPos pos, final RenderChunk renderChunk) {
        final BlockPos blockPos = renderChunk.getPosition();
        if (MathHelper.abs_int(pos.getX() - blockPos.getX()) > 16) {
            return false;
        }

        if (MathHelper.abs_int(pos.getY() - blockPos.getY()) > 16) {
            return false;
        }

        if (MathHelper.abs_int(pos.getZ() - blockPos.getZ()) > 16) {
            return false;
        }

        return true;
    }

    private Set<EnumFacing> getVisibleSides(final BlockPos pos) {
        final VisGraph visgraph = new VisGraph();
        final BlockPos posChunk = new BlockPos(pos.getX() & ~0xF, pos.getY() & ~0xF, pos.getZ() & ~0xF);

        for (final BlockPos.MutableBlockPos mutableBlockPos : (Iterable<BlockPos.MutableBlockPos>) BlockPos.getAllInBoxMutable(posChunk, posChunk.add(15, 15, 15))) {
            if (this.world.getBlockState(mutableBlockPos).getBlock().isOpaqueCube()) {
                visgraph.func_178606_a(mutableBlockPos);
            }
        }

        return visgraph.func_178609_b(pos);
    }

    private RenderChunk getNeighborRenderChunk(final BlockPos posEye, final BlockPos posChunk, final EnumFacing side) {
        final BlockPos offset = posChunk.offset(side, 16);
        if (MathHelper.abs_int(posEye.getX() - offset.getX()) > this.renderDistanceChunks * 16) {
            return null;
        }

        if (offset.getY() < 0 || offset.getY() >= 256) {
            return null;
        }

        if (MathHelper.abs_int(posEye.getZ() - offset.getZ()) > this.renderDistanceChunks * 16) {
            return null;
        }

        return this.viewFrustum.getRenderChunk(offset);
    }

    @Override
    protected Vector3f getViewVector(final Entity entity, final double partialTicks) {
        float rotationPitch = (float) (entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks);
        final float rotationYaw = (float) (entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks);

        if (this.mc.gameSettings.thirdPersonView == 2) {
            rotationPitch += 180.0f;
        }

        final float f1 = (float) (1 / (360 / (Math.PI * 2)));
        final float f2 = MathHelper.cos(-rotationYaw * f1 - (float) Math.PI);
        final float f3 = MathHelper.sin(-rotationYaw * f1 - (float) Math.PI);
        final float f4 = -MathHelper.cos(-rotationPitch * f1);
        final float f5 = MathHelper.sin(-rotationPitch * f1);
        return new Vector3f(f3 * f4, f5, f2 * f4);
    }

    @Override
    public int renderBlockLayer(final EnumWorldBlockLayer layer, final double partialTicks, final int pass, final Entity entity) {
        RenderHelper.disableStandardItemLighting();

        if (layer == EnumWorldBlockLayer.TRANSLUCENT) {
            this.profiler.startSection("translucent_sort");
            final double posX = PLAYER_POSITION_OFFSET.x;
            final double posY = PLAYER_POSITION_OFFSET.y;
            final double posZ = PLAYER_POSITION_OFFSET.z;

            final double deltaX = posX - this.prevRenderSortX;
            final double deltaY = posY - this.prevRenderSortY;
            final double deltaZ = posZ - this.prevRenderSortZ;

            if (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 1.0) {
                this.prevRenderSortX = posX;
                this.prevRenderSortY = posY;
                this.prevRenderSortZ = posZ;
                int count = 0;

                for (final ContainerLocalRenderInformation renderInfo : this.renderInfos) {
                    if (renderInfo.renderChunk.compiledChunk.isLayerStarted(layer) && count++ < 15) {
                        this.renderDispatcher.updateTransparencyLater(renderInfo.renderChunk);
                    }
                }
            }

            this.profiler.endSection();
        }

        this.profiler.startSection("filterempty");
        int count = 0;
        final boolean isTranslucent = layer == EnumWorldBlockLayer.TRANSLUCENT;
        final int start = isTranslucent ? this.renderInfos.size() - 1 : 0;
        final int end = isTranslucent ? -1 : this.renderInfos.size();
        final int step = isTranslucent ? -1 : 1;

        for (int index = start; index != end; index += step) {
            final RenderChunk renderchunk = this.renderInfos.get(index).renderChunk;

            if (!renderchunk.getCompiledChunk().isLayerEmpty(layer)) {
                count++;
                this.renderContainer.addRenderChunk(renderchunk, layer);
            }
        }

        this.profiler.endStartSection("render_" + layer);
        renderBlockLayer(layer);
        this.profiler.endSection();

        return count;
    }

    private void renderBlockLayer(final EnumWorldBlockLayer layer) {
        this.mc.entityRenderer.enableLightmap();

        if (OpenGlHelper.useVbo()) {
            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        }

        this.renderContainer.renderChunkLayer(layer);

        if (OpenGlHelper.useVbo()) {
            final List<VertexFormatElement> elements = DefaultVertexFormats.BLOCK.getElements();

            for (final VertexFormatElement element : elements) {
                final VertexFormatElement.EnumUsage usage = element.getUsage();
                final int index = element.getIndex();

                switch (usage) {
                case POSITION:
                    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                    break;

                case UV:
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + index);
                    GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                    break;

                case COLOR:
                    GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
                    GlStateManager.resetColor();
                    break;
                }
            }
        }

        this.mc.entityRenderer.disableLightmap();
    }

    @Override
    public void updateClouds() {
    }

    @Override
    public void renderSky(final float partialTicks, final int pass) {
    }

    @Override
    public void renderClouds(final float partialTicks, final int pass) {
    }

    @Override
    public boolean hasCloudFog(final double x, final double y, final double z, final float partialTicks) {
        return false;
    }

    @Override
    public void updateChunks(final long finishTimeNano) {
        this.displayListEntitiesDirty |= this.renderDispatcher.runChunkUploads(finishTimeNano);

        final Iterator<RenderChunk> iterator = this.chunksToUpdate.iterator();
        while (iterator.hasNext()) {
            final RenderChunk renderChunk = iterator.next();
            if (!this.renderDispatcher.updateChunkLater(renderChunk)) {
                break;
            }

            renderChunk.setNeedsUpdate(false);
            iterator.remove();
        }
    }

    @Override
    public void renderWorldBorder(final Entity entity, final float partialTicks) {}

    @Override
    public void drawBlockDamageTexture(final Tessellator tessellator, final WorldRenderer worldRenderer, final Entity entity, final float partialTicks) {}

    @Override
    public void drawSelectionBox(final EntityPlayer player, final MovingObjectPosition movingObjectPosition, final int p_72731_3_, final float partialTicks) {}

    @Override
    public void markBlockForUpdate(final BlockPos pos) {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        markBlocksForUpdate(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    }

    @Override
    public void notifyLightSet(final BlockPos pos) {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        markBlocksForUpdate(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    }

    @Override
    public void markBlockRangeForRenderUpdate(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
        markBlocksForUpdate(x1 - 1, y1 - 1, z1 - 1, x2 + 1, y2 + 1, z2 + 1);
    }

    @Override
    public void markBlocksForUpdate(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
        this.viewFrustum.markBlocksForUpdate(x1, y1, z1, x2, y2, z2);
    }

    @Override
    public void playRecord(final String name, final BlockPos pos) {}

    @Override
    public void playSound(final String name, final double x, final double y, final double z, final float volume, final float pitch) {}

    @Override
    public void playSoundToNearExcept(final EntityPlayer player, final String name, final double x, final double y, final double z, final float volume, final float pitch) {}

    @Override
    public void spawnParticle(final int p_180442_1_, final boolean p_180442_2_, final double p_180442_3_, final double p_180442_5_, final double p_180442_7_, final double p_180442_9_, final double p_180442_11_, final double p_180442_13_, final int... p_180442_15_) {}

    @Override
    public void onEntityAdded(final Entity entityIn) {}

    @Override
    public void onEntityRemoved(final Entity entityIn) {}

    @Override
    public void deleteAllDisplayLists() {}

    @Override
    public void broadcastSound(final int p_180440_1_, final BlockPos pos, final int p_180440_3_) {}

    @Override
    public void playAusSFX(final EntityPlayer player, final int p_180439_2_, final BlockPos pos, final int p_180439_4_) {}

    @Override
    public void sendBlockBreakProgress(final int breakerId, final BlockPos pos, final int progress) {}

    @Override
    public void setDisplayListEntitiesDirty() {
        this.displayListEntitiesDirty = true;
    }

    @SideOnly(Side.CLIENT)
    class ContainerLocalRenderInformation {
        final RenderChunk renderChunk;
        final EnumFacing facing;
        final Set<EnumFacing> setFacing;
        final int counter;

        private ContainerLocalRenderInformation(final RenderChunk renderChunk, final EnumFacing facing, final int counter) {
            this.setFacing = EnumSet.noneOf(EnumFacing.class);
            this.renderChunk = renderChunk;
            this.facing = facing;
            this.counter = counter;
        }

        ContainerLocalRenderInformation(final RenderChunk p_i46249_2_, final EnumFacing p_i46249_3_, final int p_i46249_4_, final Object p_i46249_5_) {
            this(p_i46249_2_, p_i46249_3_, p_i46249_4_);
        }
    }
}
