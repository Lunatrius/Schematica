package lunatrius.schematica;

import java.nio.FloatBuffer;
import java.util.Collections;

import lunatrius.schematica.util.Vector3f;
import lunatrius.schematica.util.Vector3i;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.Frustrum;
import net.minecraft.src.Profiler;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.ForgeSubscribe;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class RendererSchematicGlobal {
	private final Settings settings = Settings.instance();
	private final Profiler profiler = this.settings.minecraft.mcProfiler;

	private final Frustrum frustrum = new Frustrum();
	private final RendererSchematicChunkSorter rendererSchematicChunkSorter = new RendererSchematicChunkSorter();

	private final int quadBufferSize = 240;
	private final FloatBuffer quadColorBudder = BufferUtils.createFloatBuffer(this.quadBufferSize * 4);
	private final FloatBuffer quadVertexBuffer = BufferUtils.createFloatBuffer(this.quadBufferSize * 3);
	private int quadObjectCount = -1;

	private final int lineBufferSize = 240;
	private final FloatBuffer lineColorBuffer = BufferUtils.createFloatBuffer(this.lineBufferSize * 4);
	private final FloatBuffer lineVertecBuffer = BufferUtils.createFloatBuffer(this.lineBufferSize * 3);
	private int lineObjectCount = -1;

	@ForgeSubscribe
	public void onRender(RenderWorldLastEvent event) {
		if (this.settings.minecraft != null) {
			EntityPlayerSP player = this.settings.minecraft.thePlayer;
			if (player != null) {
				this.settings.playerPosition.x = (float) (player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks);
				this.settings.playerPosition.y = (float) (player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks);
				this.settings.playerPosition.z = (float) (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks);

				this.settings.rotationRender = (int) (((player.rotationYaw / 90) % 4 + 4) % 4);

				this.profiler.startSection("schematica");
				if (this.settings.isRenderingSchematic || this.settings.isRenderingGuide) {
					render();
				}

				this.profiler.endSection();
			}
		}
	}

	void render() {
		GL11.glPushMatrix();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(true);

		GL11.glTranslatef(-this.settings.getTranslationX(), -this.settings.getTranslationY(), -this.settings.getTranslationZ());

		this.profiler.startSection("schematic");
		if (this.settings.isRenderingSchematic) {
			this.profiler.startSection("updateFrustrum");
			updateFrustrum();

			this.profiler.endStartSection("sortAndUpdate");
			if (RendererSchematicChunk.canUpdate) {
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

		this.quadObjectCount = 0;
		this.lineObjectCount = 0;

		this.quadColorBudder.clear();
		this.quadVertexBuffer.clear();
		this.lineColorBuffer.clear();
		this.lineVertecBuffer.clear();

		this.profiler.startSection("dataPrep");
		if (this.settings.isRenderingSchematic) {
			drawCuboidOutline(Vector3i.ZERO, new Vector3i(this.settings.schematic.width(), this.settings.schematic.height(), this.settings.schematic.length()), 0.75f, 0.0f, 0.75f, 0.25f);
		}

		if (this.settings.isRenderingGuide) {
			Vector3i start = null;
			Vector3i end = null;

			start = this.settings.pointMin.clone().sub(this.settings.offset);
			end = this.settings.pointMax.clone().sub(this.settings.offset).add(1);
			drawCuboidOutline(start, end, 0.0f, 0.75f, 0.0f, 0.25f);

			start = this.settings.pointA.clone().sub(this.settings.offset);
			end = start.clone().add(1);
			drawCuboidOutline(start, end, 0.75f, 0.0f, 0.0f, 0.25f);
			drawCuboidSurface(start, end, 0.75f, 0.0f, 0.0f, 0.25f);

			start = this.settings.pointB.clone().sub(this.settings.offset);
			end = start.clone().add(1);
			drawCuboidOutline(start, end, 0.0f, 0.0f, 0.75f, 0.25f);
			drawCuboidSurface(start, end, 0.0f, 0.0f, 0.75f, 0.25f);
		}

		if (this.quadObjectCount > 0 || this.lineObjectCount > 0) {
			this.quadColorBudder.flip();
			this.quadVertexBuffer.flip();

			this.lineColorBuffer.flip();
			this.lineVertecBuffer.flip();

			GL11.glDisable(GL11.GL_TEXTURE_2D);

			GL11.glLineWidth(1.5f);

			GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

			this.profiler.endStartSection("quad");
			if (this.quadObjectCount > 0) {
				GL11.glColorPointer(4, 0, this.quadColorBudder);
				GL11.glVertexPointer(3, 0, this.quadVertexBuffer);
				GL11.glDrawArrays(GL11.GL_QUADS, 0, this.quadObjectCount);
			}

			this.profiler.endStartSection("line");
			if (this.lineObjectCount > 0) {
				GL11.glColorPointer(4, 0, this.lineColorBuffer);
				GL11.glVertexPointer(3, 0, this.lineVertecBuffer);
				GL11.glDrawArrays(GL11.GL_LINES, 0, this.lineObjectCount);
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

	private void drawCuboidSurface(Vector3i a, Vector3i b, float red, float green, float blue, float alpha) {
		Vector3f zero = new Vector3f(a.x, a.y, a.z).sub(this.settings.blockDelta);
		Vector3f size = new Vector3f(b.x, b.y, b.z).add(this.settings.blockDelta);

		// left
		this.quadVertexBuffer.put(zero.x).put(zero.y).put(zero.z);
		this.quadVertexBuffer.put(zero.x).put(zero.y).put(size.z);
		this.quadVertexBuffer.put(zero.x).put(size.y).put(size.z);
		this.quadVertexBuffer.put(zero.x).put(size.y).put(zero.z);

		// right
		this.quadVertexBuffer.put(size.x).put(zero.y).put(size.z);
		this.quadVertexBuffer.put(size.x).put(zero.y).put(zero.z);
		this.quadVertexBuffer.put(size.x).put(size.y).put(zero.z);
		this.quadVertexBuffer.put(size.x).put(size.y).put(size.z);

		// near
		this.quadVertexBuffer.put(size.x).put(zero.y).put(zero.z);
		this.quadVertexBuffer.put(zero.x).put(zero.y).put(zero.z);
		this.quadVertexBuffer.put(zero.x).put(size.y).put(zero.z);
		this.quadVertexBuffer.put(size.x).put(size.y).put(zero.z);

		// far
		this.quadVertexBuffer.put(zero.x).put(zero.y).put(size.z);
		this.quadVertexBuffer.put(size.x).put(zero.y).put(size.z);
		this.quadVertexBuffer.put(size.x).put(size.y).put(size.z);
		this.quadVertexBuffer.put(zero.x).put(size.y).put(size.z);

		// bottom
		this.quadVertexBuffer.put(size.x).put(zero.y).put(zero.z);
		this.quadVertexBuffer.put(size.x).put(zero.y).put(size.z);
		this.quadVertexBuffer.put(zero.x).put(zero.y).put(size.z);
		this.quadVertexBuffer.put(zero.x).put(zero.y).put(zero.z);

		// top
		this.quadVertexBuffer.put(size.x).put(size.y).put(zero.z);
		this.quadVertexBuffer.put(zero.x).put(size.y).put(zero.z);
		this.quadVertexBuffer.put(zero.x).put(size.y).put(size.z);
		this.quadVertexBuffer.put(size.x).put(size.y).put(size.z);

		for (int i = 0; i < 24; i++) {
			this.quadColorBudder.put(red).put(green).put(blue).put(alpha);
		}

		this.quadObjectCount += 24;
	}

	private void drawCuboidOutline(Vector3i a, Vector3i b, float red, float green, float blue, float alpha) {
		Vector3f zero = new Vector3f(a.x, a.y, a.z).sub(this.settings.blockDelta);
		Vector3f size = new Vector3f(b.x, b.y, b.z).add(this.settings.blockDelta);

		// bottom left
		this.lineVertecBuffer.put(zero.x).put(zero.y).put(zero.z);
		this.lineVertecBuffer.put(zero.x).put(zero.y).put(size.z);

		// top left
		this.lineVertecBuffer.put(zero.x).put(size.y).put(zero.z);
		this.lineVertecBuffer.put(zero.x).put(size.y).put(size.z);

		// bottom right
		this.lineVertecBuffer.put(size.x).put(zero.y).put(zero.z);
		this.lineVertecBuffer.put(size.x).put(zero.y).put(size.z);

		// top right
		this.lineVertecBuffer.put(size.x).put(size.y).put(zero.z);
		this.lineVertecBuffer.put(size.x).put(size.y).put(size.z);

		// bottom near
		this.lineVertecBuffer.put(zero.x).put(zero.y).put(zero.z);
		this.lineVertecBuffer.put(size.x).put(zero.y).put(zero.z);

		// top near
		this.lineVertecBuffer.put(zero.x).put(size.y).put(zero.z);
		this.lineVertecBuffer.put(size.x).put(size.y).put(zero.z);

		// bottom far
		this.lineVertecBuffer.put(zero.x).put(zero.y).put(size.z);
		this.lineVertecBuffer.put(size.x).put(zero.y).put(size.z);

		// top far
		this.lineVertecBuffer.put(zero.x).put(size.y).put(size.z);
		this.lineVertecBuffer.put(size.x).put(size.y).put(size.z);

		// near left
		this.lineVertecBuffer.put(zero.x).put(zero.y).put(zero.z);
		this.lineVertecBuffer.put(zero.x).put(size.y).put(zero.z);

		// near right
		this.lineVertecBuffer.put(size.x).put(zero.y).put(zero.z);
		this.lineVertecBuffer.put(size.x).put(size.y).put(zero.z);

		// far left
		this.lineVertecBuffer.put(zero.x).put(zero.y).put(size.z);
		this.lineVertecBuffer.put(zero.x).put(size.y).put(size.z);

		// far right
		this.lineVertecBuffer.put(size.x).put(zero.y).put(size.z);
		this.lineVertecBuffer.put(size.x).put(size.y).put(size.z);

		for (int i = 0; i < 24; i++) {
			this.lineColorBuffer.put(red).put(green).put(blue).put(alpha);
		}

		this.lineObjectCount += 24;
	}

	void updateFrustrum() {
		this.frustrum.setPosition(this.settings.getTranslationX(), this.settings.getTranslationY(), this.settings.getTranslationZ());
		for (RendererSchematicChunk rendererSchematicChunk : this.settings.sortedRendererSchematicChunk) {
			rendererSchematicChunk.isInFrustrum = this.frustrum.isBoundingBoxInFrustum(rendererSchematicChunk.getBoundingBox());
		}
	}

	void sortAndUpdate() {
		Collections.sort(this.settings.sortedRendererSchematicChunk, this.rendererSchematicChunkSorter);

		for (RendererSchematicChunk rendererSchematicChunk : this.settings.sortedRendererSchematicChunk) {
			if (rendererSchematicChunk.getDirty()) {
				rendererSchematicChunk.updateRenderer();
				break;
			}
		}
	}
}
