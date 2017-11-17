package com.github.lunatrius.schematica.client.renderer.chunk.overlay;

import com.github.lunatrius.core.client.renderer.GeometryMasks;
import com.github.lunatrius.core.client.renderer.GeometryTessellator;
import com.github.lunatrius.schematica.client.renderer.chunk.CompiledOverlay;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class RenderOverlay extends RenderChunk {
    private static enum BlockType {
        /** Purple - a block that is present in the world but not the schematic */
        EXTRA_BLOCK(0xBF00BF),
        /** Red - a mismatch between the block in the world and the schematic */
        WRONG_BLOCK(0xFF0000),
        /** Orange - a mismatch between the metadata for the block in the world and the schematic */
        WRONG_META(0xBF5F00),
        /** Blue - a block that is present in the schematic but not in the world */
        MISSING_BLOCK(0x00BFFF);

        public final int color;

        private BlockType(int color) {
            this.color = color;
        }
    }

    private final VertexBuffer vertexBuffer;

    public RenderOverlay(final World world, final RenderGlobal renderGlobal, final int index) {
        super(world, renderGlobal, index);
        this.vertexBuffer = OpenGlHelper.useVbo() ? new VertexBuffer(DefaultVertexFormats.POSITION_COLOR) : null;
    }

    @Override
    public VertexBuffer getVertexBufferByLayer(final int layer) {
        return this.vertexBuffer;
    }

    @Override
    public void rebuildChunk(final float x, final float y, final float z, final ChunkCompileTaskGenerator generator) {
        final CompiledOverlay compiledOverlay = new CompiledOverlay();
        final BlockPos from = getPosition();
        final BlockPos to = from.add(15, 15, 15);
        final BlockPos fromEx = from.add(-1, -1, -1);
        final BlockPos toEx = to.add(1, 1, 1);
        generator.getLock().lock();
        ChunkCache chunkCache;
        final SchematicWorld schematic = (SchematicWorld) this.world;

        try {
            if (generator.getStatus() != ChunkCompileTaskGenerator.Status.COMPILING) {
                return;
            }

            if (from.getX() < 0 || from.getZ() < 0 || from.getX() >= schematic.getWidth() || from.getZ() >= schematic.getLength()) {
                generator.setCompiledChunk(CompiledChunk.DUMMY);
                return;
            }

            chunkCache = new ChunkCache(this.world, fromEx, toEx, 1);
            generator.setCompiledChunk(compiledOverlay);
        } finally {
            generator.getLock().unlock();
        }

        final VisGraph visgraph = new VisGraph();

        if (!chunkCache.isEmpty()) {
            ++renderChunksUpdated;

            final World mcWorld = Minecraft.getMinecraft().world;

            final BlockRenderLayer layer = BlockRenderLayer.TRANSLUCENT;
            final BufferBuilder buffer = generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(layer);

            GeometryTessellator.setStaticDelta(ConfigurationHandler.blockDelta);

            // Elements in this array may be null, indicating that nothing should be rendered (or out of bounds)
            // 18 elements to provide padding on both sides (this padding is not rendered).
            final BlockType[][][] types = new BlockType[18][18][18];

            // Build the type array (including the padding)
            BlockPos.MutableBlockPos mcPos = new BlockPos.MutableBlockPos();
            for (final BlockPos.MutableBlockPos pos : BlockPos.getAllInBoxMutable(fromEx, toEx)) {
                if (!schematic.isInside(pos) || !schematic.layerMode.shouldUseLayer(schematic, pos.getY())) {
                    continue;
                }

                // Indices in types
                int secX = pos.getX() - fromEx.getX();
                int secY = pos.getY() - fromEx.getY();
                int secZ = pos.getZ() - fromEx.getZ();

                final IBlockState schBlockState = schematic.getBlockState(pos);
                final Block schBlock = schBlockState.getBlock();

                if (schBlockState.isOpaqueCube()) {
                    visgraph.setOpaqueCube(pos);
                }

                mcPos.setPos(pos.getX() + schematic.position.getX(), pos.getY() + schematic.position.getY(), pos.getZ() + schematic.position.getZ());
                final IBlockState mcBlockState = mcWorld.getBlockState(mcPos);
                final Block mcBlock = mcBlockState.getBlock();

                final boolean isSchAirBlock = schematic.isAirBlock(pos);
                final boolean isMcAirBlock = mcWorld.isAirBlock(mcPos) || ConfigurationHandler.isExtraAirBlock(mcBlock);

                if (ConfigurationHandler.highlightAir && !isMcAirBlock && isSchAirBlock) {
                    types[secX][secY][secZ] = BlockType.EXTRA_BLOCK;
                } else if (ConfigurationHandler.highlight) {
                    if (!isMcAirBlock) {
                        if (schBlock != mcBlock) {
                            types[secX][secY][secZ] = BlockType.WRONG_BLOCK;
                        } else if (schBlock.getMetaFromState(schBlockState) != mcBlock.getMetaFromState(mcBlockState)) {
                            types[secX][secY][secZ] = BlockType.WRONG_META;
                        }
                    } else if (!isSchAirBlock) {
                        types[secX][secY][secZ] = BlockType.MISSING_BLOCK;
                    }
                }
            }

            // Draw the type array (but not the padding)
            for (final BlockPos.MutableBlockPos pos : BlockPos.getAllInBoxMutable(from, to)) {
                int secX = pos.getX() - fromEx.getX();
                int secY = pos.getY() - fromEx.getY();
                int secZ = pos.getZ() - fromEx.getZ();

                BlockType type = types[secX][secY][secZ];

                if (type != null) {
                    if (!compiledOverlay.isLayerStarted(layer)) {
                        compiledOverlay.setLayerStarted(layer);
                        preRenderBlocks(buffer, from);
                    }

                    int sides = getSides(types, secX, secY, secZ);
                    GeometryTessellator.drawCuboid(buffer, pos, sides, 0x3F000000 | type.color);
                    compiledOverlay.setLayerUsed(layer);
                }
            }

            if (compiledOverlay.isLayerStarted(layer)) {
                postRenderBlocks(layer, x, y, z, buffer, compiledOverlay);
            }
        }

        compiledOverlay.setVisibility(visgraph.computeVisibility());
    }

    private int getSides(final BlockType[][][] types, final int x, final int y, final int z) {
        // The padding cannot be rendered (it lacks neighbors)
        if (!(x > 0 && x < 17)) {
            throw new IndexOutOfBoundsException("x cannot be in padding: " + x);
        }
        if (!(y > 0 && y < 17)) {
            throw new IndexOutOfBoundsException("y cannot be in padding: " + y);
        }
        if (!(z > 0 && z < 17)) {
            throw new IndexOutOfBoundsException("z cannot be in padding: " + z);
        }

        int sides = 0;

        final BlockType type = types[x][y][z];

        if (types[x][y - 1][z] != type) {
            sides |= GeometryMasks.Quad.DOWN;
        }

        if (types[x][y + 1][z] != type) {
            sides |= GeometryMasks.Quad.UP;
        }

        if (types[x][y][z - 1] != type) {
            sides |= GeometryMasks.Quad.NORTH;
        }

        if (types[x][y][z + 1] != type) {
            sides |= GeometryMasks.Quad.SOUTH;
        }

        if (types[x - 1][y][z] != type) {
            sides |= GeometryMasks.Quad.WEST;
        }

        if (types[x + 1][y][z] != type) {
            sides |= GeometryMasks.Quad.EAST;
        }

        return sides;
    }

    @Override
    public void preRenderBlocks(final BufferBuilder buffer, final BlockPos pos) {
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
    }

    @Override
    public void deleteGlResources() {
        super.deleteGlResources();

        if (this.vertexBuffer != null) {
            this.vertexBuffer.deleteGlBuffers();
        }
    }
}
