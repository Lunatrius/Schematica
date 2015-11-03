package com.github.lunatrius.schematica.client.gui.save;

import com.github.lunatrius.core.client.gui.GuiNumericField;
import com.github.lunatrius.core.client.gui.GuiScreenBase;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.BlockPos;

import java.io.File;
import java.io.IOException;

public class GuiSchematicSave extends GuiScreenBase {
    private int centerX = 0;
    private int centerY = 0;

    private GuiButton btnPointA = null;

    private GuiNumericField numericAX = null;
    private GuiNumericField numericAY = null;
    private GuiNumericField numericAZ = null;

    private GuiButton btnPointB = null;

    private GuiNumericField numericBX = null;
    private GuiNumericField numericBY = null;
    private GuiNumericField numericBZ = null;

    private GuiButton btnEnable = null;
    private GuiButton btnSave = null;
    private GuiTextField tfFilename = null;

    private String filename = "";

    private final String strSaveSelection = I18n.format(Names.Gui.Save.SAVE_SELECTION);
    private final String strX = I18n.format(Names.Gui.X);
    private final String strY = I18n.format(Names.Gui.Y);
    private final String strZ = I18n.format(Names.Gui.Z);
    private final String strOn = I18n.format(Names.Gui.ON);
    private final String strOff = I18n.format(Names.Gui.OFF);

    public GuiSchematicSave(GuiScreen guiScreen) {
        super(guiScreen);
    }

    @Override
    public void initGui() {
        this.centerX = this.width / 2;
        this.centerY = this.height / 2;

        this.buttonList.clear();

        int id = 0;

        this.btnPointA = new GuiButton(id++, this.centerX - 130, this.centerY - 55, 100, 20, I18n.format(Names.Gui.Save.POINT_RED));
        this.buttonList.add(this.btnPointA);

        this.numericAX = new GuiNumericField(this.fontRendererObj, id++, this.centerX - 130, this.centerY - 30);
        this.buttonList.add(this.numericAX);

        this.numericAY = new GuiNumericField(this.fontRendererObj, id++, this.centerX - 130, this.centerY - 5);
        this.buttonList.add(this.numericAY);

        this.numericAZ = new GuiNumericField(this.fontRendererObj, id++, this.centerX - 130, this.centerY + 20);
        this.buttonList.add(this.numericAZ);

        this.btnPointB = new GuiButton(id++, this.centerX + 30, this.centerY - 55, 100, 20, I18n.format(Names.Gui.Save.POINT_BLUE));
        this.buttonList.add(this.btnPointB);

        this.numericBX = new GuiNumericField(this.fontRendererObj, id++, this.centerX + 30, this.centerY - 30);
        this.buttonList.add(this.numericBX);

        this.numericBY = new GuiNumericField(this.fontRendererObj, id++, this.centerX + 30, this.centerY - 5);
        this.buttonList.add(this.numericBY);

        this.numericBZ = new GuiNumericField(this.fontRendererObj, id++, this.centerX + 30, this.centerY + 20);
        this.buttonList.add(this.numericBZ);

        this.btnEnable = new GuiButton(id++, this.width - 210, this.height - 30, 50, 20, ClientProxy.isRenderingGuide ? this.strOn : this.strOff);
        this.buttonList.add(this.btnEnable);

        this.tfFilename = new GuiTextField(id++, this.fontRendererObj, this.width - 155, this.height - 29, 100, 18);
        this.textFields.add(this.tfFilename);

        this.btnSave = new GuiButton(id++, this.width - 50, this.height - 30, 40, 20, I18n.format(Names.Gui.Save.SAVE));
        this.btnSave.enabled = ClientProxy.isRenderingGuide || ClientProxy.schematic != null;
        this.buttonList.add(this.btnSave);

        this.tfFilename.setMaxStringLength(1024);
        this.tfFilename.setText(this.filename);

        setMinMax(this.numericAX);
        setMinMax(this.numericAY);
        setMinMax(this.numericAZ);
        setMinMax(this.numericBX);
        setMinMax(this.numericBY);
        setMinMax(this.numericBZ);

        setPoint(this.numericAX, this.numericAY, this.numericAZ, ClientProxy.pointA);
        setPoint(this.numericBX, this.numericBY, this.numericBZ, ClientProxy.pointB);
    }

    private void setMinMax(GuiNumericField numericField) {
        numericField.setMinimum(Constants.World.MINIMUM_COORD);
        numericField.setMaximum(Constants.World.MAXIMUM_COORD);
    }

    private void setPoint(GuiNumericField numX, GuiNumericField numY, GuiNumericField numZ, BlockPos point) {
        numX.setValue(point.getX());
        numY.setValue(point.getY());
        numZ.setValue(point.getZ());
    }

    @Override
    protected void actionPerformed(GuiButton guiButton) {
        if (guiButton.enabled) {
            if (guiButton.id == this.btnPointA.id) {
                ClientProxy.movePointToPlayer(ClientProxy.pointA);
                ClientProxy.updatePoints();
                setPoint(this.numericAX, this.numericAY, this.numericAZ, ClientProxy.pointA);
            } else if (guiButton.id == this.numericAX.id) {
                ClientProxy.pointA.x = this.numericAX.getValue();
                ClientProxy.updatePoints();
            } else if (guiButton.id == this.numericAY.id) {
                ClientProxy.pointA.y = this.numericAY.getValue();
                ClientProxy.updatePoints();
            } else if (guiButton.id == this.numericAZ.id) {
                ClientProxy.pointA.z = this.numericAZ.getValue();
                ClientProxy.updatePoints();
            } else if (guiButton.id == this.btnPointB.id) {
                ClientProxy.movePointToPlayer(ClientProxy.pointB);
                ClientProxy.updatePoints();
                setPoint(this.numericBX, this.numericBY, this.numericBZ, ClientProxy.pointB);
            } else if (guiButton.id == this.numericBX.id) {
                ClientProxy.pointB.x = this.numericBX.getValue();
                ClientProxy.updatePoints();
            } else if (guiButton.id == this.numericBY.id) {
                ClientProxy.pointB.y = this.numericBY.getValue();
                ClientProxy.updatePoints();
            } else if (guiButton.id == this.numericBZ.id) {
                ClientProxy.pointB.z = this.numericBZ.getValue();
                ClientProxy.updatePoints();
            } else if (guiButton.id == this.btnEnable.id) {
                ClientProxy.isRenderingGuide = !ClientProxy.isRenderingGuide && Schematica.proxy.isSaveEnabled;
                this.btnEnable.displayString = ClientProxy.isRenderingGuide ? this.strOn : this.strOff;
                this.btnSave.enabled = ClientProxy.isRenderingGuide || ClientProxy.schematic != null;
            } else if (guiButton.id == this.btnSave.id) {
                String path = this.tfFilename.getText() + ".schematic";
                if (ClientProxy.isRenderingGuide) {
                    if (Schematica.proxy.saveSchematic(this.mc.thePlayer, ConfigurationHandler.schematicDirectory, path, this.mc.theWorld, ClientProxy.pointMin, ClientProxy.pointMax)) {
                        this.filename = "";
                        this.tfFilename.setText(this.filename);
                    }
                } else {
                    SchematicFormat.writeToFileAndNotify(new File(ConfigurationHandler.schematicDirectory, path), ClientProxy.schematic.getSchematic(), this.mc.thePlayer);
                }
            }
        }
    }

    @Override
    protected void keyTyped(char character, int code) throws IOException {
        super.keyTyped(character, code);
        this.filename = this.tfFilename.getText();
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

        super.drawScreen(par1, par2, par3);
    }
}
