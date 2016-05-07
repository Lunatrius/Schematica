package com.github.lunatrius.schematica.client.renderer.chunk;

import com.github.lunatrius.schematica.client.renderer.chunk.overlay.RenderOverlayList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;

public class OverlayRenderDispatcher extends ChunkRenderDispatcher {
    public OverlayRenderDispatcher() {
        super();
    }

    public OverlayRenderDispatcher(int countRenderBuilders) {
        super(countRenderBuilders);
    }

    @Override
    public ListenableFuture<Object> uploadChunk(final BlockRenderLayer layer, final VertexBuffer buffer, final RenderChunk renderChunk, final CompiledChunk compiledChunk, final double distanceSq) {
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread() || OpenGlHelper.useVbo()) {
            return super.uploadChunk(layer, buffer, renderChunk, compiledChunk, distanceSq);
        }

        uploadDisplayList(buffer, ((RenderOverlayList) renderChunk).getDisplayList(layer, compiledChunk), renderChunk);

        buffer.setTranslation(0.0, 0.0, 0.0);
        return Futures.immediateFuture(null);
    }
}
