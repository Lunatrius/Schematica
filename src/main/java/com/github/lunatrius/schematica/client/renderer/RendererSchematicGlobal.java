package com.github.lunatrius.schematica.client.renderer;

import com.github.lunatrius.core.util.vector.Vector3f;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.Settings;
import com.github.lunatrius.schematica.world.SchematicWorld;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

import java.util.Collections;

public class RendererSchematicGlobal {
	private final Minecraft minecraft = Minecraft.getMinecraft();
	private final Settings settings = Settings.instance;
	private final Profiler profiler = this.minecraft.mcProfiler;

	private final Frustrum frustrum = new Frustrum();
	private final RendererSchematicChunkSorter rendererSchematicChunkSorter = new RendererSchematicChunkSorter();

	@SubscribeEvent
	public void onRender(RenderWorldLastEvent event) {
		EntityPlayerSP player = this.minecraft.thePlayer;
		if (player != null) {
			this.settings.playerPosition.x = (float) (player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks);
			this.settings.playerPosition.y = (float) (player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks);
			this.settings.playerPosition.z = (float) (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks);

			this.settings.rotationRender = MathHelper.floor_double(player.rotationYaw / 90) & 3;

			this.settings.orientation = getOrientation(player);

			this.profiler.startSection("schematica");
			SchematicWorld schematic = Schematica.proxy.getActiveSchematic();
			if ((schematic != null && schematic.isRendering()) || this.settings.isRenderingGuide) {
				render(schematic);
			}

			this.profiler.endSection();
		}
	}

	private ForgeDirection getOrientation(EntityPlayer player) {
		if (player.rotationPitch > 45) {
			return ForgeDirection.DOWN;
		} else if (player.rotationPitch < -45) {
			return ForgeDirection.UP;
		} else {
			switch (MathHelper.floor_double(player.rotationYaw / 90.0 + 0.5) & 3) {
			case 0:
				return ForgeDirection.SOUTH;
			case 1:
				return ForgeDirection.WEST;
			case 2:
				return ForgeDirection.NORTH;
			case 3:
				return ForgeDirection.EAST;
			}
		}

		return ForgeDirection.UNKNOWN;
	}

	public void render(SchematicWorld schematic) {
		GL11.glPushMatrix();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);

		GL11.glTranslatef(-this.settings.getTranslationX(), -this.settings.getTranslationY(), -this.settings.getTranslationZ());

		this.profiler.startSection("schematic");
		if (schematic != null && schematic.isRendering()) {
			this.profiler.startSection("updateFrustrum");
			updateFrustrum();

			this.profiler.endStartSection("sortAndUpdate");
			if (RendererSchematicChunk.getCanUpdate()) {
				sortAndUpdate();
			}

			this.profiler.endStartSection("render");
			int pass;
			for (pass = 0; pass < 3; pass++) {
				for (RendererSchematicChunk renderer : this.settings.sortedRendererSchematicChunk) {
					renderer.render(pass);
				}
			}
			this.profiler.endSection();
		}

		this.profiler.endStartSection("guide");

		RenderHelper.createBuffers();

		this.profiler.startSection("dataPrep");
		if (schematic != null && schematic.isRendering()) {
			RenderHelper.drawCuboidOutline(RenderHelper.VEC_ZERO, Schematica.proxy.getActiveSchematic().dimensions(), RenderHelper.LINE_ALL, 0.75f, 0.0f, 0.75f, 0.25f);
		}

		if (this.settings.isRenderingGuide) {
			Vector3f start;
			Vector3f end;

			start = this.settings.pointMin.clone().sub(this.settings.offset);
			end = this.settings.pointMax.clone().sub(this.settings.offset);
			end.add(1, 1, 1);
			RenderHelper.drawCuboidOutline(start, end, RenderHelper.LINE_ALL, 0.0f, 0.75f, 0.0f, 0.25f);

			start = this.settings.pointA.clone().sub(this.settings.offset);
			end = start.clone().add(1, 1, 1);
			RenderHelper.drawCuboidOutline(start, end, RenderHelper.LINE_ALL, 0.75f, 0.0f, 0.0f, 0.25f);
			RenderHelper.drawCuboidSurface(start, end, RenderHelper.QUAD_ALL, 0.75f, 0.0f, 0.0f, 0.25f);

			start = this.settings.pointB.clone().sub(this.settings.offset);
			end = start.clone().add(1, 1, 1);
			RenderHelper.drawCuboidOutline(start, end, RenderHelper.LINE_ALL, 0.0f, 0.0f, 0.75f, 0.25f);
			RenderHelper.drawCuboidSurface(start, end, RenderHelper.QUAD_ALL, 0.0f, 0.0f, 0.75f, 0.25f);
		}

		int quadCount = RenderHelper.getQuadCount();
		int lineCount = RenderHelper.getLineCount();

		if (quadCount > 0 || lineCount > 0) {
			GL11.glDisable(GL11.GL_TEXTURE_2D);

			GL11.glLineWidth(1.5f);

			GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

			this.profiler.endStartSection("quad");
			if (quadCount > 0) {
				GL11.glVertexPointer(3, 0, RenderHelper.getQuadVertexBuffer());
				GL11.glColorPointer(4, 0, RenderHelper.getQuadColorBuffer());
				GL11.glDrawArrays(GL11.GL_QUADS, 0, quadCount);
			}

			this.profiler.endStartSection("line");
			if (lineCount > 0) {
				GL11.glVertexPointer(3, 0, RenderHelper.getLineVertexBuffer());
				GL11.glColorPointer(4, 0, RenderHelper.getLineColorBuffer());
				GL11.glDrawArrays(GL11.GL_LINES, 0, lineCount);
			}

			this.profiler.endSection();

			GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
			GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		this.profiler.endSection();

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GL11.glPopMatrix();
	}

	private void updateFrustrum() {
		this.frustrum.setPosition(this.settings.getTranslationX(), this.settings.getTranslationY(), this.settings.getTranslationZ());
		for (RendererSchematicChunk rendererSchematicChunk : this.settings.sortedRendererSchematicChunk) {
			rendererSchematicChunk.isInFrustrum = this.frustrum.isBoundingBoxInFrustum(rendererSchematicChunk.getBoundingBox());
		}
	}

	private void sortAndUpdate() {
		Collections.sort(this.settings.sortedRendererSchematicChunk, this.rendererSchematicChunkSorter);

		for (RendererSchematicChunk rendererSchematicChunk : this.settings.sortedRendererSchematicChunk) {
			if (rendererSchematicChunk.getDirty()) {
				rendererSchematicChunk.updateRenderer();
				break;
			}
		}
	}
}
