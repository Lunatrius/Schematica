package lunatrius.schematica;

import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiSlot;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.RenderEngine;
import net.minecraft.src.RenderHelper;
import net.minecraft.src.Tessellator;
import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

class GuiSchematicMaterialsSlot extends GuiSlot {
	private final Settings settings = Settings.instance();
	private final FontRenderer fontRenderer = this.settings.minecraft.fontRenderer;
	private final RenderEngine renderEngine = this.settings.minecraft.renderEngine;

	private final GuiSchematicMaterials guiSchematicMaterials;

	protected int selectedIndex = -1;

	public GuiSchematicMaterialsSlot(GuiSchematicMaterials par1) {
		super(Settings.instance().minecraft, par1.width, par1.height, 16, par1.height - 34, 24);
		this.guiSchematicMaterials = par1;
		this.selectedIndex = -1;
	}

	@Override
	protected int getSize() {
		return this.guiSchematicMaterials.blockList.size();
	}

	@Override
	protected void elementClicked(int index, boolean par2) {
		this.selectedIndex = index;
	}

	@Override
	protected boolean isSelected(int index) {
		return index == this.selectedIndex;
	}

	@Override
	protected void drawBackground() {
	}

	@Override
	protected void drawContainerBackground(Tessellator tessellator) {
	}

	@Override
	protected void drawSlot(int index, int x, int y, int par4, Tessellator tessellator) {
		ItemStack itemStack = this.guiSchematicMaterials.blockList.get(index);
		String itemName = Item.itemsList[itemStack.itemID].func_77653_i(itemStack);
		String amount = Integer.toString(itemStack.stackSize);

		drawItemStack(x, y, itemStack);

		this.guiSchematicMaterials.drawString(this.fontRenderer, itemName, x + 24, y + 6, 16777215);
		this.guiSchematicMaterials.drawString(this.fontRenderer, amount, x + 215 - this.fontRenderer.getStringWidth(amount), y + 6, 16777215);
	}

	private void drawItemStack(int x, int y, ItemStack itemStack) {
		drawItemStackSlot(x, y);

		if (itemStack != null) {
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.enableGUIStandardItemLighting();
			Settings.renderItem.renderItemIntoGUI(this.fontRenderer, this.renderEngine, itemStack, x + 2, y + 2);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		}
	}

	private void drawItemStackSlot(int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ForgeHooksClient.bindTexture("/gui/slot.png", 0);
		Tessellator var10 = Tessellator.instance;
		var10.startDrawingQuads();
		var10.addVertexWithUV(x + 1 + 0, y + 1 + 18, 0, 0 * 0.0078125F, 18 * 0.0078125F);
		var10.addVertexWithUV(x + 1 + 18, y + 1 + 18, 0, 18 * 0.0078125F, 18 * 0.0078125F);
		var10.addVertexWithUV(x + 1 + 18, y + 1 + 0, 0, 18 * 0.0078125F, 0 * 0.0078125F);
		var10.addVertexWithUV(x + 1 + 0, y + 1 + 0, 0, 0 * 0.0078125F, 0 * 0.0078125F);
		var10.draw();
	}
}
