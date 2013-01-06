package lunatrius.schematica.gui;

import lunatrius.schematica.Settings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StringTranslate;

public class GuiSchematicControl extends GuiScreen {
	private final Settings settings = Settings.instance();
	@SuppressWarnings("unused")
	private final GuiScreen prevGuiScreen;

	private final StringTranslate strTranslate = StringTranslate.getInstance();

	private int centerX = 0;
	private int centerY = 0;

	private GuiButton btnDecX = null;
	private GuiButton btnAmountX = null;
	private GuiButton btnIncX = null;

	private GuiButton btnDecY = null;
	private GuiButton btnAmountY = null;
	private GuiButton btnIncY = null;

	private GuiButton btnDecZ = null;
	private GuiButton btnAmountZ = null;
	private GuiButton btnIncZ = null;

	private GuiButton btnDecLayer = null;
	private GuiButton btnIncLayer = null;

	private GuiButton btnHide = null;
	private GuiButton btnMove = null;
	private GuiButton btnFlip = null;
	private GuiButton btnRotate = null;

	private GuiButton btnMaterials = null;
	private GuiButton btnPrint = null;

	private int incrementX = 0;
	private int incrementY = 0;
	private int incrementZ = 0;

	private final String strMoveSchematic = this.strTranslate.translateKey("schematic.moveschematic");
	private final String strLayers = this.strTranslate.translateKey("schematic.layers");
	private final String strOperations = this.strTranslate.translateKey("schematic.operations");
	private final String strAll = this.strTranslate.translateKey("schematic.all");
	private final String strX = this.strTranslate.translateKey("schematic.x");
	private final String strY = this.strTranslate.translateKey("schematic.y");
	private final String strZ = this.strTranslate.translateKey("schematic.z");
	private final String strMaterials = this.strTranslate.translateKey("schematic.materials");
	private final String strPrinter = this.strTranslate.translateKey("schematic.printer");

	public GuiSchematicControl(GuiScreen guiScreen) {
		this.prevGuiScreen = guiScreen;
	}

	@Override
	public void initGui() {
		this.centerX = this.width / 2;
		this.centerY = this.height / 2;

		this.controlList.clear();

		int id = 0;

		this.btnDecX = new GuiButton(id++, this.centerX - 50, this.centerY - 30, 30, 20, this.strTranslate.translateKey("schematic.decrease"));
		this.controlList.add(this.btnDecX);

		this.btnAmountX = new GuiButton(id++, this.centerX - 15, this.centerY - 30, 30, 20, Integer.toString(this.settings.increments[this.incrementX]));
		this.controlList.add(this.btnAmountX);

		this.btnIncX = new GuiButton(id++, this.centerX + 20, this.centerY - 30, 30, 20, this.strTranslate.translateKey("schematic.increase"));
		this.controlList.add(this.btnIncX);

		this.btnDecY = new GuiButton(id++, this.centerX - 50, this.centerY - 5, 30, 20, this.strTranslate.translateKey("schematic.decrease"));
		this.controlList.add(this.btnDecY);

		this.btnAmountY = new GuiButton(id++, this.centerX - 15, this.centerY - 5, 30, 20, Integer.toString(this.settings.increments[this.incrementY]));
		this.controlList.add(this.btnAmountY);

		this.btnIncY = new GuiButton(id++, this.centerX + 20, this.centerY - 5, 30, 20, this.strTranslate.translateKey("schematic.increase"));
		this.controlList.add(this.btnIncY);

		this.btnDecZ = new GuiButton(id++, this.centerX - 50, this.centerY + 20, 30, 20, this.strTranslate.translateKey("schematic.decrease"));
		this.controlList.add(this.btnDecZ);

		this.btnAmountZ = new GuiButton(id++, this.centerX - 15, this.centerY + 20, 30, 20, Integer.toString(this.settings.increments[this.incrementZ]));
		this.controlList.add(this.btnAmountZ);

		this.btnIncZ = new GuiButton(id++, this.centerX + 20, this.centerY + 20, 30, 20, this.strTranslate.translateKey("schematic.increase"));
		this.controlList.add(this.btnIncZ);

		this.btnDecLayer = new GuiButton(id++, this.width - 90, this.height - 150, 25, 20, this.strTranslate.translateKey("schematic.decrease"));
		this.controlList.add(this.btnDecLayer);

		this.btnIncLayer = new GuiButton(id++, this.width - 35, this.height - 150, 25, 20, this.strTranslate.translateKey("schematic.increase"));
		this.controlList.add(this.btnIncLayer);

		this.btnHide = new GuiButton(id++, this.width - 90, this.height - 105, 80, 20, this.strTranslate.translateKey(this.settings.isRenderingSchematic ? "schematic.hide" : "schematic.show"));
		this.controlList.add(this.btnHide);

		this.btnMove = new GuiButton(id++, this.width - 90, this.height - 80, 80, 20, this.strTranslate.translateKey("schematic.movehere"));
		this.controlList.add(this.btnMove);

		this.btnFlip = new GuiButton(id++, this.width - 90, this.height - 55, 80, 20, this.strTranslate.translateKey("schematic.flip"));
		this.controlList.add(this.btnFlip);

		this.btnRotate = new GuiButton(id++, this.width - 90, this.height - 30, 80, 20, this.strTranslate.translateKey("schematic.rotate"));
		this.controlList.add(this.btnRotate);

		this.btnMaterials = new GuiButton(id++, 10, this.height - 70, 80, 20, this.strTranslate.translateKey("schematic.materials"));
		this.controlList.add(this.btnMaterials);

		this.btnPrint = new GuiButton(id++, 10, this.height - 30, 80, 20, this.strTranslate.translateKey(this.settings.isPrinting ? "schematic.disable" : "schematic.enable"));
		this.controlList.add(this.btnPrint);

		this.btnDecLayer.enabled = this.settings.schematic != null;
		this.btnIncLayer.enabled = this.settings.schematic != null;
		this.btnHide.enabled = this.settings.schematic != null;
		this.btnMove.enabled = this.settings.schematic != null;
		this.btnFlip.enabled = this.settings.schematic != null;
		this.btnRotate.enabled = this.settings.schematic != null;
		this.btnMaterials.enabled = this.settings.schematic != null;
		this.btnPrint.enabled = this.settings.schematic != null && this.settings.isPrinterEnabled;
	}

	@Override
	protected void actionPerformed(GuiButton guiButton) {
		if (guiButton.enabled) {
			if (guiButton.id == this.btnDecX.id) {
				this.settings.offset.x -= this.settings.increments[this.incrementX];
				this.settings.reloadChunkCache();
			} else if (guiButton.id == this.btnIncX.id) {
				this.settings.offset.x += this.settings.increments[this.incrementX];
				this.settings.reloadChunkCache();
			} else if (guiButton.id == this.btnAmountX.id) {
				this.incrementX = (this.incrementX + 1) % this.settings.increments.length;
				this.btnAmountX.displayString = Integer.toString(this.settings.increments[this.incrementX]);
			} else if (guiButton.id == this.btnDecY.id) {
				this.settings.offset.y -= this.settings.increments[this.incrementY];
				this.settings.reloadChunkCache();
			} else if (guiButton.id == this.btnIncY.id) {
				this.settings.offset.y += this.settings.increments[this.incrementY];
				this.settings.reloadChunkCache();
			} else if (guiButton.id == this.btnAmountY.id) {
				this.incrementY = (this.incrementY + 1) % this.settings.increments.length;
				this.btnAmountY.displayString = Integer.toString(this.settings.increments[this.incrementY]);
			} else if (guiButton.id == this.btnDecZ.id) {
				this.settings.offset.z -= this.settings.increments[this.incrementZ];
				this.settings.reloadChunkCache();
			} else if (guiButton.id == this.btnIncZ.id) {
				this.settings.offset.z += this.settings.increments[this.incrementZ];
				this.settings.reloadChunkCache();
			} else if (guiButton.id == this.btnAmountZ.id) {
				this.incrementZ = (this.incrementZ + 1) % this.settings.increments.length;
				this.btnAmountZ.displayString = Integer.toString(this.settings.increments[this.incrementZ]);
			} else if (guiButton.id == this.btnDecLayer.id) {
				if (this.settings.schematic != null) {
					this.settings.renderingLayer = MathHelper.clamp_int(this.settings.renderingLayer - 1, -1, this.settings.schematic.height() - 1);
				} else {
					this.settings.renderingLayer = -1;
				}
				this.settings.refreshSchematic();
			} else if (guiButton.id == this.btnIncLayer.id) {
				if (this.settings.schematic != null) {
					this.settings.renderingLayer = MathHelper.clamp_int(this.settings.renderingLayer + 1, -1, this.settings.schematic.height() - 1);
				} else {
					this.settings.renderingLayer = -1;
				}
				this.settings.refreshSchematic();
			} else if (guiButton.id == this.btnHide.id) {
				this.settings.toggleRendering();
				this.btnHide.displayString = this.strTranslate.translateKey(this.settings.isRenderingSchematic ? "schematic.hide" : "schematic.show");
			} else if (guiButton.id == this.btnMove.id) {
				this.settings.moveHere();
			} else if (guiButton.id == this.btnFlip.id) {
				this.settings.flipWorld();
			} else if (guiButton.id == this.btnRotate.id) {
				this.settings.rotateWorld();
			} else if (guiButton.id == this.btnMaterials.id) {
				this.settings.minecraft.displayGuiScreen(new GuiSchematicMaterials(this));
			} else if (guiButton.id == this.btnPrint.id && this.settings.isPrinterEnabled) {
				this.settings.isPrinting = !this.settings.isPrinting;
				this.btnPrint.displayString = this.strTranslate.translateKey(this.settings.isPrinting ? "schematic.disable" : "schematic.enable");
			}
		}
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		// drawDefaultBackground();

		drawCenteredString(this.fontRenderer, this.strMoveSchematic, this.centerX, this.centerY - 45, 0xFFFFFF);
		drawCenteredString(this.fontRenderer, this.strMaterials, 50, this.height - 85, 0xFFFFFF);
		drawCenteredString(this.fontRenderer, this.strPrinter, 50, this.height - 45, 0xFFFFFF);
		drawCenteredString(this.fontRenderer, this.strLayers, this.width - 50, this.height - 165, 0xFFFFFF);
		drawCenteredString(this.fontRenderer, this.strOperations, this.width - 50, this.height - 120, 0xFFFFFF);

		drawCenteredString(this.fontRenderer, this.settings.renderingLayer < 0 ? this.strAll : Integer.toString(this.settings.renderingLayer + 1), this.width - 50, this.height - 145, 0xFFFFFF);

		drawString(this.fontRenderer, this.strX, this.centerX - 65, this.centerY - 24, 0xFFFFFF);
		drawString(this.fontRenderer, Integer.toString(this.settings.offset.x), this.centerX + 55, this.centerY - 24, 0xFFFFFF);

		drawString(this.fontRenderer, this.strY, this.centerX - 65, this.centerY + 1, 0xFFFFFF);
		drawString(this.fontRenderer, Integer.toString(this.settings.offset.y), this.centerX + 55, this.centerY + 1, 0xFFFFFF);

		drawString(this.fontRenderer, this.strZ, this.centerX - 65, this.centerY + 26, 0xFFFFFF);
		drawString(this.fontRenderer, Integer.toString(this.settings.offset.z), this.centerX + 55, this.centerY + 26, 0xFFFFFF);

		super.drawScreen(par1, par2, par3);
	}
}
