package lunatrius.schematica.gui;

import lunatrius.schematica.Settings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.StringTranslate;

public class GuiSchematicSave extends GuiScreen {
	private final Settings settings = Settings.instance();
	@SuppressWarnings("unused")
	private final GuiScreen prevGuiScreen;

	private final StringTranslate strTranslate = StringTranslate.getInstance();

	private int centerX = 0;
	private int centerY = 0;

	private GuiButton btnPointA = null;

	private GuiButton btnDecAX = null;
	private GuiButton btnAmountAX = null;
	private GuiButton btnIncAX = null;

	private GuiButton btnDecAY = null;
	private GuiButton btnAmountAY = null;
	private GuiButton btnIncAY = null;

	private GuiButton btnDecAZ = null;
	private GuiButton btnAmountAZ = null;
	private GuiButton btnIncAZ = null;

	private GuiButton btnPointB = null;

	private GuiButton btnDecBX = null;
	private GuiButton btnAmountBX = null;
	private GuiButton btnIncBX = null;

	private GuiButton btnDecBY = null;
	private GuiButton btnAmountBY = null;
	private GuiButton btnIncBY = null;

	private GuiButton btnDecBZ = null;
	private GuiButton btnAmountBZ = null;
	private GuiButton btnIncBZ = null;

	private int incrementAX = 0;
	private int incrementAY = 0;
	private int incrementAZ = 0;

	private int incrementBX = 0;
	private int incrementBY = 0;
	private int incrementBZ = 0;

	private GuiButton btnEnable = null;
	private GuiButton btnSave = null;
	private GuiTextField tfFilename = null;

	private String filename = "";

	private final String strSaveSelection = this.strTranslate.translateKey("schematic.saveselection");
	private final String strX = this.strTranslate.translateKey("schematic.x");
	private final String strY = this.strTranslate.translateKey("schematic.y");
	private final String strZ = this.strTranslate.translateKey("schematic.z");

	public GuiSchematicSave(GuiScreen guiScreen) {
		this.prevGuiScreen = guiScreen;
	}

	@Override
	public void initGui() {
		this.centerX = this.width / 2;
		this.centerY = this.height / 2;

		this.controlList.clear();

		int id = 0;

		this.btnPointA = new GuiButton(id++, this.centerX - 130, this.centerY - 55, 100, 20, this.strTranslate.translateKey("schematic.point.red"));
		this.controlList.add(this.btnPointA);

		this.btnDecAX = new GuiButton(id++, this.centerX - 130, this.centerY - 30, 30, 20, this.strTranslate.translateKey("schematic.decrease"));
		this.controlList.add(this.btnDecAX);

		this.btnAmountAX = new GuiButton(id++, this.centerX - 95, this.centerY - 30, 30, 20, Integer.toString(this.settings.increments[this.incrementAX]));
		this.controlList.add(this.btnAmountAX);

		this.btnIncAX = new GuiButton(id++, this.centerX - 60, this.centerY - 30, 30, 20, this.strTranslate.translateKey("schematic.increase"));
		this.controlList.add(this.btnIncAX);

		this.btnDecAY = new GuiButton(id++, this.centerX - 130, this.centerY - 5, 30, 20, this.strTranslate.translateKey("schematic.decrease"));
		this.controlList.add(this.btnDecAY);

		this.btnAmountAY = new GuiButton(id++, this.centerX - 95, this.centerY - 5, 30, 20, Integer.toString(this.settings.increments[this.incrementAY]));
		this.controlList.add(this.btnAmountAY);

		this.btnIncAY = new GuiButton(id++, this.centerX - 60, this.centerY - 5, 30, 20, this.strTranslate.translateKey("schematic.increase"));
		this.controlList.add(this.btnIncAY);

		this.btnDecAZ = new GuiButton(id++, this.centerX - 130, this.centerY + 20, 30, 20, this.strTranslate.translateKey("schematic.decrease"));
		this.controlList.add(this.btnDecAZ);

		this.btnAmountAZ = new GuiButton(id++, this.centerX - 95, this.centerY + 20, 30, 20, Integer.toString(this.settings.increments[this.incrementAZ]));
		this.controlList.add(this.btnAmountAZ);

		this.btnIncAZ = new GuiButton(id++, this.centerX - 60, this.centerY + 20, 30, 20, this.strTranslate.translateKey("schematic.increase"));
		this.controlList.add(this.btnIncAZ);

		this.btnPointB = new GuiButton(id++, this.centerX + 30, this.centerY - 55, 100, 20, this.strTranslate.translateKey("schematic.point.blue"));
		this.controlList.add(this.btnPointB);

		this.btnDecBX = new GuiButton(id++, this.centerX + 30, this.centerY - 30, 30, 20, this.strTranslate.translateKey("schematic.decrease"));
		this.controlList.add(this.btnDecBX);

		this.btnAmountBX = new GuiButton(id++, this.centerX + 65, this.centerY - 30, 30, 20, Integer.toString(this.settings.increments[this.incrementBX]));
		this.controlList.add(this.btnAmountBX);

		this.btnIncBX = new GuiButton(id++, this.centerX + 100, this.centerY - 30, 30, 20, this.strTranslate.translateKey("schematic.increase"));
		this.controlList.add(this.btnIncBX);

		this.btnDecBY = new GuiButton(id++, this.centerX + 30, this.centerY - 5, 30, 20, this.strTranslate.translateKey("schematic.decrease"));
		this.controlList.add(this.btnDecBY);

		this.btnAmountBY = new GuiButton(id++, this.centerX + 65, this.centerY - 5, 30, 20, Integer.toString(this.settings.increments[this.incrementBY]));
		this.controlList.add(this.btnAmountBY);

		this.btnIncBY = new GuiButton(id++, this.centerX + 100, this.centerY - 5, 30, 20, this.strTranslate.translateKey("schematic.increase"));
		this.controlList.add(this.btnIncBY);

		this.btnDecBZ = new GuiButton(id++, this.centerX + 30, this.centerY + 20, 30, 20, this.strTranslate.translateKey("schematic.decrease"));
		this.controlList.add(this.btnDecBZ);

		this.btnAmountBZ = new GuiButton(id++, this.centerX + 65, this.centerY + 20, 30, 20, Integer.toString(this.settings.increments[this.incrementBZ]));
		this.controlList.add(this.btnAmountBZ);

		this.btnIncBZ = new GuiButton(id++, this.centerX + 100, this.centerY + 20, 30, 20, this.strTranslate.translateKey("schematic.increase"));
		this.controlList.add(this.btnIncBZ);

		this.btnEnable = new GuiButton(id++, this.width - 210, this.height - 30, 50, 20, this.strTranslate.translateKey(this.settings.isRenderingGuide ? "schematic.disable" : "schematic.enable"));
		this.controlList.add(this.btnEnable);

		this.tfFilename = new GuiTextField(this.fontRenderer, this.width - 155, this.height - 29, 100, 18);

		this.btnSave = new GuiButton(id++, this.width - 50, this.height - 30, 40, 20, this.strTranslate.translateKey("schematic.save"));
		this.btnSave.enabled = this.settings.isRenderingGuide;
		this.controlList.add(this.btnSave);

		this.tfFilename.setMaxStringLength(1024);
		this.tfFilename.setText(this.filename);
	}

	@Override
	protected void actionPerformed(GuiButton guiButton) {
		if (guiButton.enabled) {
			if (guiButton.id == this.btnPointA.id) {
				this.settings.moveHere(this.settings.pointA);
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnDecAX.id) {
				this.settings.pointA.x -= this.settings.increments[this.incrementAX];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnIncAX.id) {
				this.settings.pointA.x += this.settings.increments[this.incrementAX];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnAmountAX.id) {
				this.incrementAX = (this.incrementAX + 1) % this.settings.increments.length;
				this.btnAmountAX.displayString = Integer.toString(this.settings.increments[this.incrementAX]);
			} else if (guiButton.id == this.btnDecAY.id) {
				this.settings.pointA.y -= this.settings.increments[this.incrementAY];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnIncAY.id) {
				this.settings.pointA.y += this.settings.increments[this.incrementAY];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnAmountAY.id) {
				this.incrementAY = (this.incrementAY + 1) % this.settings.increments.length;
				this.btnAmountAY.displayString = Integer.toString(this.settings.increments[this.incrementAY]);
			} else if (guiButton.id == this.btnDecAZ.id) {
				this.settings.pointA.z -= this.settings.increments[this.incrementAZ];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnIncAZ.id) {
				this.settings.pointA.z += this.settings.increments[this.incrementAZ];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnAmountAZ.id) {
				this.incrementAZ = (this.incrementAZ + 1) % this.settings.increments.length;
				this.btnAmountAZ.displayString = Integer.toString(this.settings.increments[this.incrementAZ]);
			} else if (guiButton.id == this.btnPointB.id) {
				this.settings.moveHere(this.settings.pointB);
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnDecBX.id) {
				this.settings.pointB.x -= this.settings.increments[this.incrementBX];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnIncBX.id) {
				this.settings.pointB.x += this.settings.increments[this.incrementBX];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnAmountBX.id) {
				this.incrementBX = (this.incrementBX + 1) % this.settings.increments.length;
				this.btnAmountBX.displayString = Integer.toString(this.settings.increments[this.incrementBX]);
			} else if (guiButton.id == this.btnDecBY.id) {
				this.settings.pointB.y -= this.settings.increments[this.incrementBY];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnIncBY.id) {
				this.settings.pointB.y += this.settings.increments[this.incrementBY];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnAmountBY.id) {
				this.incrementBY = (this.incrementBY + 1) % this.settings.increments.length;
				this.btnAmountBY.displayString = Integer.toString(this.settings.increments[this.incrementBY]);
			} else if (guiButton.id == this.btnDecBZ.id) {
				this.settings.pointB.z -= this.settings.increments[this.incrementBZ];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnIncBZ.id) {
				this.settings.pointB.z += this.settings.increments[this.incrementBZ];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnAmountBZ.id) {
				this.incrementBZ = (this.incrementBZ + 1) % this.settings.increments.length;
				this.btnAmountBZ.displayString = Integer.toString(this.settings.increments[this.incrementBZ]);
			} else if (guiButton.id == this.btnEnable.id) {
				this.settings.isRenderingGuide = !this.settings.isRenderingGuide;
				this.btnEnable.displayString = this.strTranslate.translateKey(this.settings.isRenderingGuide ? "schematic.disable" : "schematic.enable");
				this.btnSave.enabled = this.settings.isRenderingGuide;
			} else if (guiButton.id == this.btnSave.id) {
				String path = this.tfFilename.getText() + ".schematic";
				if (this.settings.saveSchematic(Settings.schematicDirectory, path, this.settings.pointMin, this.settings.pointMax)) {
					this.filename = "";
					this.tfFilename.setText(this.filename);
				}
			}
		}
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		this.tfFilename.mouseClicked(par1, par2, par3);
		super.mouseClicked(par1, par2, par3);
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		this.tfFilename.textboxKeyTyped(par1, par2);
		this.filename = this.tfFilename.getText();
		super.keyTyped(par1, par2);
	}

	@Override
	public void updateScreen() {
		this.tfFilename.updateCursorCounter();
		super.updateScreen();
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		// drawDefaultBackground();

		drawString(this.fontRenderer, this.strSaveSelection, this.width - 205, this.height - 45, 0xFFFFFF);

		drawString(this.fontRenderer, this.strX, this.centerX - 145, this.centerY - 24, 0xFFFFFF);
		drawString(this.fontRenderer, Integer.toString(this.settings.pointA.x), this.centerX - 25, this.centerY - 24, 0xFFFFFF);

		drawString(this.fontRenderer, this.strY, this.centerX - 145, this.centerY + 1, 0xFFFFFF);
		drawString(this.fontRenderer, Integer.toString(this.settings.pointA.y), this.centerX - 25, this.centerY + 1, 0xFFFFFF);

		drawString(this.fontRenderer, this.strZ, this.centerX - 145, this.centerY + 26, 0xFFFFFF);
		drawString(this.fontRenderer, Integer.toString(this.settings.pointA.z), this.centerX - 25, this.centerY + 26, 0xFFFFFF);

		drawString(this.fontRenderer, this.strX, this.centerX + 15, this.centerY - 24, 0xFFFFFF);
		drawString(this.fontRenderer, Integer.toString(this.settings.pointB.x), this.centerX + 135, this.centerY - 24, 0xFFFFFF);

		drawString(this.fontRenderer, this.strY, this.centerX + 15, this.centerY + 1, 0xFFFFFF);
		drawString(this.fontRenderer, Integer.toString(this.settings.pointB.y), this.centerX + 135, this.centerY + 1, 0xFFFFFF);

		drawString(this.fontRenderer, this.strZ, this.centerX + 15, this.centerY + 26, 0xFFFFFF);
		drawString(this.fontRenderer, Integer.toString(this.settings.pointB.z), this.centerX + 135, this.centerY + 26, 0xFFFFFF);

		this.tfFilename.drawTextBox();

		super.drawScreen(par1, par2, par3);
	}
}
