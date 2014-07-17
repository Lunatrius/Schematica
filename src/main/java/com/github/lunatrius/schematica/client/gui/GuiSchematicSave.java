package com.github.lunatrius.schematica.client.gui;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

public class GuiSchematicSave extends GuiScreen {
	@SuppressWarnings("unused")
	private final GuiScreen prevGuiScreen;

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

	private final String strSaveSelection = I18n.format("schematica.gui.saveselection");
	private final String strX = I18n.format("schematica.gui.x");
	private final String strY = I18n.format("schematica.gui.y");
	private final String strZ = I18n.format("schematica.gui.z");

	public GuiSchematicSave(GuiScreen guiScreen) {
		this.prevGuiScreen = guiScreen;
	}

	@Override
	public void initGui() {
		this.centerX = this.width / 2;
		this.centerY = this.height / 2;

		this.buttonList.clear();

		int id = 0;

		this.btnPointA = new GuiButton(id++, this.centerX - 130, this.centerY - 55, 100, 20, I18n.format("schematica.gui.point.red"));
		this.buttonList.add(this.btnPointA);

		this.btnDecAX = new GuiButton(id++, this.centerX - 130, this.centerY - 30, 30, 20, I18n.format("schematica.gui.decrease"));
		this.buttonList.add(this.btnDecAX);

		this.btnAmountAX = new GuiButton(id++, this.centerX - 95, this.centerY - 30, 30, 20, Integer.toString(ClientProxy.INCREMENTS[this.incrementAX]));
		this.buttonList.add(this.btnAmountAX);

		this.btnIncAX = new GuiButton(id++, this.centerX - 60, this.centerY - 30, 30, 20, I18n.format("schematica.gui.increase"));
		this.buttonList.add(this.btnIncAX);

		this.btnDecAY = new GuiButton(id++, this.centerX - 130, this.centerY - 5, 30, 20, I18n.format("schematica.gui.decrease"));
		this.buttonList.add(this.btnDecAY);

		this.btnAmountAY = new GuiButton(id++, this.centerX - 95, this.centerY - 5, 30, 20, Integer.toString(ClientProxy.INCREMENTS[this.incrementAY]));
		this.buttonList.add(this.btnAmountAY);

		this.btnIncAY = new GuiButton(id++, this.centerX - 60, this.centerY - 5, 30, 20, I18n.format("schematica.gui.increase"));
		this.buttonList.add(this.btnIncAY);

		this.btnDecAZ = new GuiButton(id++, this.centerX - 130, this.centerY + 20, 30, 20, I18n.format("schematica.gui.decrease"));
		this.buttonList.add(this.btnDecAZ);

		this.btnAmountAZ = new GuiButton(id++, this.centerX - 95, this.centerY + 20, 30, 20, Integer.toString(ClientProxy.INCREMENTS[this.incrementAZ]));
		this.buttonList.add(this.btnAmountAZ);

		this.btnIncAZ = new GuiButton(id++, this.centerX - 60, this.centerY + 20, 30, 20, I18n.format("schematica.gui.increase"));
		this.buttonList.add(this.btnIncAZ);

		this.btnPointB = new GuiButton(id++, this.centerX + 30, this.centerY - 55, 100, 20, I18n.format("schematica.gui.point.blue"));
		this.buttonList.add(this.btnPointB);

		this.btnDecBX = new GuiButton(id++, this.centerX + 30, this.centerY - 30, 30, 20, I18n.format("schematica.gui.decrease"));
		this.buttonList.add(this.btnDecBX);

		this.btnAmountBX = new GuiButton(id++, this.centerX + 65, this.centerY - 30, 30, 20, Integer.toString(ClientProxy.INCREMENTS[this.incrementBX]));
		this.buttonList.add(this.btnAmountBX);

		this.btnIncBX = new GuiButton(id++, this.centerX + 100, this.centerY - 30, 30, 20, I18n.format("schematica.gui.increase"));
		this.buttonList.add(this.btnIncBX);

		this.btnDecBY = new GuiButton(id++, this.centerX + 30, this.centerY - 5, 30, 20, I18n.format("schematica.gui.decrease"));
		this.buttonList.add(this.btnDecBY);

		this.btnAmountBY = new GuiButton(id++, this.centerX + 65, this.centerY - 5, 30, 20, Integer.toString(ClientProxy.INCREMENTS[this.incrementBY]));
		this.buttonList.add(this.btnAmountBY);

		this.btnIncBY = new GuiButton(id++, this.centerX + 100, this.centerY - 5, 30, 20, I18n.format("schematica.gui.increase"));
		this.buttonList.add(this.btnIncBY);

		this.btnDecBZ = new GuiButton(id++, this.centerX + 30, this.centerY + 20, 30, 20, I18n.format("schematica.gui.decrease"));
		this.buttonList.add(this.btnDecBZ);

		this.btnAmountBZ = new GuiButton(id++, this.centerX + 65, this.centerY + 20, 30, 20, Integer.toString(ClientProxy.INCREMENTS[this.incrementBZ]));
		this.buttonList.add(this.btnAmountBZ);

		this.btnIncBZ = new GuiButton(id++, this.centerX + 100, this.centerY + 20, 30, 20, I18n.format("schematica.gui.increase"));
		this.buttonList.add(this.btnIncBZ);

		this.btnEnable = new GuiButton(id++, this.width - 210, this.height - 30, 50, 20, I18n.format(ClientProxy.isRenderingGuide ? "schematica.gui.disable" : "schematica.gui.enable"));
		this.buttonList.add(this.btnEnable);

		this.tfFilename = new GuiTextField(this.fontRendererObj, this.width - 155, this.height - 29, 100, 18);

		this.btnSave = new GuiButton(id++, this.width - 50, this.height - 30, 40, 20, I18n.format("schematica.gui.save"));
		this.btnSave.enabled = ClientProxy.isRenderingGuide;
		this.buttonList.add(this.btnSave);

		this.tfFilename.setMaxStringLength(1024);
		this.tfFilename.setText(this.filename);
	}

	@Override
	protected void actionPerformed(GuiButton guiButton) {
		if (guiButton.enabled) {
			if (guiButton.id == this.btnPointA.id) {
				ClientProxy.movePointToPlayer(ClientProxy.pointA);
				ClientProxy.updatePoints();
			} else if (guiButton.id == this.btnDecAX.id) {
				ClientProxy.pointA.x -= ClientProxy.INCREMENTS[this.incrementAX];
				ClientProxy.updatePoints();
			} else if (guiButton.id == this.btnIncAX.id) {
				ClientProxy.pointA.x += ClientProxy.INCREMENTS[this.incrementAX];
				ClientProxy.updatePoints();
			} else if (guiButton.id == this.btnAmountAX.id) {
				this.incrementAX = (this.incrementAX + 1) % ClientProxy.INCREMENTS.length;
				this.btnAmountAX.displayString = Integer.toString(ClientProxy.INCREMENTS[this.incrementAX]);
			} else if (guiButton.id == this.btnDecAY.id) {
				ClientProxy.pointA.y -= ClientProxy.INCREMENTS[this.incrementAY];
				ClientProxy.updatePoints();
			} else if (guiButton.id == this.btnIncAY.id) {
				ClientProxy.pointA.y += ClientProxy.INCREMENTS[this.incrementAY];
				ClientProxy.updatePoints();
			} else if (guiButton.id == this.btnAmountAY.id) {
				this.incrementAY = (this.incrementAY + 1) % ClientProxy.INCREMENTS.length;
				this.btnAmountAY.displayString = Integer.toString(ClientProxy.INCREMENTS[this.incrementAY]);
			} else if (guiButton.id == this.btnDecAZ.id) {
				ClientProxy.pointA.z -= ClientProxy.INCREMENTS[this.incrementAZ];
				ClientProxy.updatePoints();
			} else if (guiButton.id == this.btnIncAZ.id) {
				ClientProxy.pointA.z += ClientProxy.INCREMENTS[this.incrementAZ];
				ClientProxy.updatePoints();
			} else if (guiButton.id == this.btnAmountAZ.id) {
				this.incrementAZ = (this.incrementAZ + 1) % ClientProxy.INCREMENTS.length;
				this.btnAmountAZ.displayString = Integer.toString(ClientProxy.INCREMENTS[this.incrementAZ]);
			} else if (guiButton.id == this.btnPointB.id) {
				ClientProxy.movePointToPlayer(ClientProxy.pointB);
				ClientProxy.updatePoints();
			} else if (guiButton.id == this.btnDecBX.id) {
				ClientProxy.pointB.x -= ClientProxy.INCREMENTS[this.incrementBX];
				ClientProxy.updatePoints();
			} else if (guiButton.id == this.btnIncBX.id) {
				ClientProxy.pointB.x += ClientProxy.INCREMENTS[this.incrementBX];
				ClientProxy.updatePoints();
			} else if (guiButton.id == this.btnAmountBX.id) {
				this.incrementBX = (this.incrementBX + 1) % ClientProxy.INCREMENTS.length;
				this.btnAmountBX.displayString = Integer.toString(ClientProxy.INCREMENTS[this.incrementBX]);
			} else if (guiButton.id == this.btnDecBY.id) {
				ClientProxy.pointB.y -= ClientProxy.INCREMENTS[this.incrementBY];
				ClientProxy.updatePoints();
			} else if (guiButton.id == this.btnIncBY.id) {
				ClientProxy.pointB.y += ClientProxy.INCREMENTS[this.incrementBY];
				ClientProxy.updatePoints();
			} else if (guiButton.id == this.btnAmountBY.id) {
				this.incrementBY = (this.incrementBY + 1) % ClientProxy.INCREMENTS.length;
				this.btnAmountBY.displayString = Integer.toString(ClientProxy.INCREMENTS[this.incrementBY]);
			} else if (guiButton.id == this.btnDecBZ.id) {
				ClientProxy.pointB.z -= ClientProxy.INCREMENTS[this.incrementBZ];
				ClientProxy.updatePoints();
			} else if (guiButton.id == this.btnIncBZ.id) {
				ClientProxy.pointB.z += ClientProxy.INCREMENTS[this.incrementBZ];
				ClientProxy.updatePoints();
			} else if (guiButton.id == this.btnAmountBZ.id) {
				this.incrementBZ = (this.incrementBZ + 1) % ClientProxy.INCREMENTS.length;
				this.btnAmountBZ.displayString = Integer.toString(ClientProxy.INCREMENTS[this.incrementBZ]);
			} else if (guiButton.id == this.btnEnable.id) {
				ClientProxy.isRenderingGuide = !ClientProxy.isRenderingGuide && Schematica.proxy.isSaveEnabled;
				this.btnEnable.displayString = I18n.format(ClientProxy.isRenderingGuide ? "schematica.gui.disable" : "schematica.gui.enable");
				this.btnSave.enabled = ClientProxy.isRenderingGuide;
			} else if (guiButton.id == this.btnSave.id) {
				String path = this.tfFilename.getText() + ".schematic";
				if (Schematica.proxy.saveSchematic(null, ConfigurationHandler.schematicDirectory, path, this.mc.theWorld, ClientProxy.pointMin, ClientProxy.pointMax)) {
					this.filename = "";
					this.tfFilename.setText(this.filename);
				}
			}
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int action) {
		this.tfFilename.mouseClicked(x, y, action);
		super.mouseClicked(x, y, action);
	}

	@Override
	protected void keyTyped(char character, int code) {
		this.tfFilename.textboxKeyTyped(character, code);
		this.filename = this.tfFilename.getText();
		super.keyTyped(character, code);
	}

	@Override
	public void updateScreen() {
		this.tfFilename.updateCursorCounter();
		super.updateScreen();
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		// drawDefaultBackground();

		drawString(this.fontRendererObj, this.strSaveSelection, this.width - 205, this.height - 45, 0xFFFFFF);

		drawString(this.fontRendererObj, this.strX, this.centerX - 145, this.centerY - 24, 0xFFFFFF);
		drawString(this.fontRendererObj, Integer.toString(ClientProxy.pointA.x), this.centerX - 25, this.centerY - 24, 0xFFFFFF);

		drawString(this.fontRendererObj, this.strY, this.centerX - 145, this.centerY + 1, 0xFFFFFF);
		drawString(this.fontRendererObj, Integer.toString(ClientProxy.pointA.y), this.centerX - 25, this.centerY + 1, 0xFFFFFF);

		drawString(this.fontRendererObj, this.strZ, this.centerX - 145, this.centerY + 26, 0xFFFFFF);
		drawString(this.fontRendererObj, Integer.toString(ClientProxy.pointA.z), this.centerX - 25, this.centerY + 26, 0xFFFFFF);

		drawString(this.fontRendererObj, this.strX, this.centerX + 15, this.centerY - 24, 0xFFFFFF);
		drawString(this.fontRendererObj, Integer.toString(ClientProxy.pointB.x), this.centerX + 135, this.centerY - 24, 0xFFFFFF);

		drawString(this.fontRendererObj, this.strY, this.centerX + 15, this.centerY + 1, 0xFFFFFF);
		drawString(this.fontRendererObj, Integer.toString(ClientProxy.pointB.y), this.centerX + 135, this.centerY + 1, 0xFFFFFF);

		drawString(this.fontRendererObj, this.strZ, this.centerX + 15, this.centerY + 26, 0xFFFFFF);
		drawString(this.fontRendererObj, Integer.toString(ClientProxy.pointB.z), this.centerX + 135, this.centerY + 26, 0xFFFFFF);

		this.tfFilename.drawTextBox();

		super.drawScreen(par1, par2, par3);
	}
}
