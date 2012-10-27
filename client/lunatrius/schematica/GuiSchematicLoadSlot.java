package lunatrius.schematica;

import net.minecraft.src.GuiSlot;
import net.minecraft.src.Tessellator;
import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.opengl.GL11;

public class GuiSchematicLoadSlot extends GuiSlot {
	private final Settings settings = Settings.instance();
	final GuiSchematicLoad parentSchematicGuiChooser;

	public GuiSchematicLoadSlot(GuiSchematicLoad schematicGuiChooser) {
		super(Settings.instance().minecraft, schematicGuiChooser.width, schematicGuiChooser.height, 32, schematicGuiChooser.height - 55 + 4, 36);
		this.parentSchematicGuiChooser = schematicGuiChooser;
	}

	@Override
	protected int getSize() {
		return this.settings.getSchematicFiles().size();
	}

	@Override
	protected void elementClicked(int index, boolean par2) {
		this.settings.selectedSchematic = index;
	}

	@Override
	protected boolean isSelected(int index) {
		return index == this.settings.selectedSchematic;
	}

	@Override
	protected int getContentHeight() {
		return this.getSize() * 36;
	}

	@Override
	protected void drawBackground() {
		this.parentSchematicGuiChooser.drawDefaultBackground();
	}

	@Override
	protected void drawSlot(int index, int x, int y, int par4, Tessellator tessellator) {
		String schematicName = this.settings.getSchematicFiles().get(index).replaceAll("(?i)\\.schematic$", "");

		ForgeHooksClient.bindTexture("/gui/unknown_pack.png", 0);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_I(0x00FFFFFF);
		tessellator.addVertexWithUV(x, y + par4, 0.0, 0.0, 1.0);
		tessellator.addVertexWithUV(x + 32, y + par4, 0.0, 1.0, 1.0);
		tessellator.addVertexWithUV(x + 32, y, 0.0, 1.0, 0.0);
		tessellator.addVertexWithUV(x, y, 0.0, 0.0, 0.0);
		tessellator.draw();
		this.parentSchematicGuiChooser.drawString(this.settings.minecraft.fontRenderer, schematicName, x + 32 + 2, y + 1, 0x00FFFFFF);
	}
}
