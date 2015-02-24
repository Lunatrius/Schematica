package com.github.lunatrius.schematica.client.renderer.chunk;

import com.github.lunatrius.schematica.client.renderer.chunk.overlay.RenderOverlayList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.EnumWorldBlockLayer;

public class OverlayRenderDispatcher extends ChunkRenderDispatcher {
    @Override
    public ListenableFuture uploadChunk(final EnumWorldBlockLayer layer, final WorldRenderer worldRenderer, final RenderChunk renderChunk, final CompiledChunk compiledChunk) {
        if (Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            if (OpenGlHelper.useVbo()) {
                uploadVertexBuffer(worldRenderer, renderChunk.getVertexBufferByLayer(layer.ordinal()));
            } else {
                uploadDisplayList(worldRenderer, ((RenderOverlayList) renderChunk).getDisplayList(layer, compiledChunk), renderChunk);
            }

            worldRenderer.setTranslation(0.0, 0.0, 0.0);
            return Futures.immediateFuture(null);
        } else {
            final ListenableFutureTask listenableFutureTask = ListenableFutureTask.create(new Runnable() {
                @Override
                public void run() {
                    uploadChunk(layer, worldRenderer, renderChunk, compiledChunk);
                }
            }, null);

            synchronized (this.queueChunkUploads) {
                this.queueChunkUploads.add(listenableFutureTask);
                return listenableFutureTask;
            }
        }
    }
}
