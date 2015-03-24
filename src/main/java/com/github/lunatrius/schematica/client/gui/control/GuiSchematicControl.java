package com.github.lunatrius.schematica.client.gui.control;

import com.github.lunatrius.core.client.gui.GuiNumericField;
import com.github.lunatrius.core.client.gui.GuiScreenBase;
import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.client.renderer.RenderSchematic;
import com.github.lunatrius.schematica.client.util.RotationHelper;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.client.config.GuiUnicodeGlyphButton;

import java.io.IOException;

public class GuiSchematicControl extends GuiScreenBase {
    private final SchematicWorld schematic;
    private final SchematicPrinter printer;

    private int centerX = 0;
    private int centerY = 0;

    private GuiNumericField numericX = null;
    private GuiNumericField numericY = null;
    private GuiNumericField numericZ = null;

    private GuiButton btnLayerMode = null;
    private GuiNumericField nfLayer = null;

    private GuiButton btnHide = null;
    private GuiButton btnMove = null;
    private GuiButton btnFlip = null;
    private GuiButton btnRotateDirection = null;
    private GuiButton btnRotate = null;

    private GuiButton btnMaterials = null;
    private GuiButton btnPrint = null;

    private final String strMoveSchematic = I18n.format(Names.Gui.Control.MOVE_SCHEMATIC);
    private final String strOperations = I18n.format(Names.Gui.Control.OPERATIONS);
    private final String strAll = I18n.format(Names.Gui.Control.MODE_ALL);
    private final String strLayers = I18n.format(Names.Gui.Control.MODE_LAYERS);
    private final String strMaterials = I18n.format(Names.Gui.Control.MATERIALS);
    private final String strPrinter = I18n.format(Names.Gui.Control.PRINTER);
    private final String strHide = I18n.format(Names.Gui.Control.HIDE);
    private final String strShow = I18n.format(Names.Gui.Control.SHOW);
    private final String strX = I18n.format(Names.Gui.X);
    private final String strY = I18n.format(Names.Gui.Y);
    private final String strZ = I18n.format(Names.Gui.Z);
    private final String strOn = I18n.format(Names.Gui.ON);
    private final String strOff = I18n.format(Names.Gui.OFF);

    public GuiSchematicControl(GuiScreen guiScreen) {
        super(guiScreen);
        this.schematic = ClientProxy.schematic;
        this.printer = SchematicPrinter.INSTANCE;
    }

    @Override
    public void initGui() {
        this.centerX = this.width / 2;
        this.centerY = this.height / 2;

        this.buttonList.clear();

        int id = 0;

        this.numericX = new GuiNumericField(this.fontRendererObj, id++, this.centerX - 50, this.centerY - 30, 100, 20);
        this.buttonList.add(this.numericX);

        this.numericY = new GuiNumericField(this.fontRendererObj, id++, this.centerX - 50, this.centerY - 5, 100, 20);
        this.buttonList.add(this.numericY);

        this.numericZ = new GuiNumericField(this.fontRendererObj, id++, this.centerX - 50, this.centerY + 20, 100, 20);
        this.buttonList.add(this.numericZ);

        this.btnLayerMode = new GuiButton(id++, this.width - 90, this.height - 150 - 25, 80, 20, this.schematic != null && this.schematic.isRenderingLayer ? this.strLayers : this.strAll);
        this.buttonList.add(this.btnLayerMode);

        this.nfLayer = new GuiNumericField(this.fontRendererObj, id++, this.width - 90, this.height - 150, 80, 20);
        this.buttonList.add(this.nfLayer);

        this.btnHide = new GuiButton(id++, this.width - 90, this.height - 105, 80, 20, this.schematic != null && this.schematic.isRendering ? this.strHide : this.strShow);
        this.buttonList.add(this.btnHide);

        this.btnMove = new GuiButton(id++, this.width - 90, this.height - 80, 80, 20, I18n.format(Names.Gui.Control.MOVE_HERE));
        this.buttonList.add(this.btnMove);

        this.btnFlip = new GuiButton(id++, this.width - 90, this.height - 55, 80, 20, I18n.format(Names.Gui.Control.FLIP));
        this.buttonList.add(this.btnFlip);

        this.btnRotateDirection = new GuiButton(id++, this.width - 180, this.height - 30, 80, 20, I18n.format(Names.Gui.Control.TRANSFORM_PREFIX + ClientProxy.axisRotation.getName()));
        this.buttonList.add(this.btnRotateDirection);

        this.btnRotate = new GuiUnicodeGlyphButton(id++, this.width - 90, this.height - 30, 80, 20, " " + I18n.format(Names.Gui.Control.ROTATE), "\u21bb", 2.0f);
        this.buttonList.add(this.btnRotate);

        this.btnMaterials = new GuiButton(id++, 10, this.height - 70, 80, 20, this.strMaterials);
        this.buttonList.add(this.btnMaterials);

        this.btnPrint = new GuiButton(id++, 10, this.height - 30, 80, 20, this.printer.isPrinting() ? this.strOn : this.strOff);
        this.buttonList.add(this.btnPrint);

        this.numericX.setEnabled(this.schematic != null);
        this.numericY.setEnabled(this.schematic != null);
        this.numericZ.setEnabled(this.schematic != null);

        this.btnLayerMode.enabled = this.schematic != null;
        this.nfLayer.setEnabled(this.schematic != null && this.schematic.isRenderingLayer);

        this.btnHide.enabled = this.schematic != null;
        this.btnMove.enabled = this.schematic != null;
        this.btnFlip.enabled = this.schematic != null;
        this.btnFlip.enabled = false;
        this.btnRotateDirection.enabled = this.schematic != null;
        this.btnRotate.enabled = this.schematic != null;
        this.btnMaterials.enabled = this.schematic != null;
        this.btnPrint.enabled = this.schematic != null && this.printer.isEnabled();

        setMinMax(this.numericX);
        setMinMax(this.numericY);
        setMinMax(this.numericZ);

        if (this.schematic != null) {
            setPoint(this.numericX, this.numericY, this.numericZ, this.schematic.position);
        }

        this.nfLayer.setMinimum(0);
        this.nfLayer.setMaximum(this.schematic != null ? this.schematic.getHeight() - 1 : 0);
        if (this.schematic != null) {
            this.nfLayer.setValue(this.schematic.renderingLayer);
        }
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
            if (this.schematic == null) {
                return;
            }

            if (guiButton.id == this.numericX.id) {
                this.schematic.position.x = this.numericX.getValue();
                RenderSchematic.INSTANCE.refresh();
            } else if (guiButton.id == this.numericY.id) {
                this.schematic.position.y = this.numericY.getValue();
                RenderSchematic.INSTANCE.refresh();
            } else if (guiButton.id == this.numericZ.id) {
                this.schematic.position.z = this.numericZ.getValue();
                RenderSchematic.INSTANCE.refresh();
            } else if (guiButton.id == this.btnLayerMode.id) {
                this.schematic.isRenderingLayer = !this.schematic.isRenderingLayer;
                this.btnLayerMode.displayString = this.schematic.isRenderingLayer ? this.strLayers : this.strAll;
                this.nfLayer.setEnabled(this.schematic.isRenderingLayer);
                RenderSchematic.INSTANCE.refresh();
            } else if (guiButton.id == this.nfLayer.id) {
                this.schematic.renderingLayer = this.nfLayer.getValue();
                RenderSchematic.INSTANCE.refresh();
            } else if (guiButton.id == this.btnHide.id) {
                this.btnHide.displayString = this.schematic.toggleRendering() ? this.strHide : this.strShow;
            } else if (guiButton.id == this.btnMove.id) {
                ClientProxy.moveSchematicToPlayer(this.schematic);
                RenderSchematic.INSTANCE.refresh();
                setPoint(this.numericX, this.numericY, this.numericZ, this.schematic.position);
            } else if (guiButton.id == this.btnFlip.id) {
                // TODO: implement flip logic
                SchematicPrinter.INSTANCE.refresh();
            } else if (guiButton.id == this.btnRotateDirection.id) {
                final EnumFacing[] values = EnumFacing.VALUES;
                ClientProxy.axisRotation = values[((ClientProxy.axisRotation.ordinal() + 1) % values.length)];
                guiButton.displayString = I18n.format(Names.Gui.Control.TRANSFORM_PREFIX + ClientProxy.axisRotation.getName());
            } else if (guiButton.id == this.btnRotate.id) {
                if (RotationHelper.INSTANCE.rotate(this.schematic, ClientProxy.axisRotation, isShiftKeyDown())) {
                    setPoint(this.numericX, this.numericY, this.numericZ, this.schematic.position);
                    RenderSchematic.INSTANCE.refresh();
                    SchematicPrinter.INSTANCE.refresh();
                }
            } else if (guiButton.id == this.btnMaterials.id) {
                this.mc.displayGuiScreen(new GuiSchematicMaterials(this));
            } else if (guiButton.id == this.btnPrint.id && this.printer.isEnabled()) {
                boolean isPrinting = this.printer.togglePrinting();
                this.btnPrint.displayString = isPrinting ? this.strOn : this.strOff;
            }
        }
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        super.handleKeyboardInput();

        if (this.btnFlip.enabled) {
            this.btnFlip.packedFGColour = isShiftKeyDown() ? 0xFF0000 : 0x000000;
        }

        if (this.btnRotate.enabled) {
            this.btnRotate.packedFGColour = isShiftKeyDown() ? 0xFF0000 : 0x000000;
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        // drawDefaultBackground();

        drawCenteredString(this.fontRendererObj, this.strMoveSchematic, this.centerX, this.centerY - 45, 0xFFFFFF);
        drawCenteredString(this.fontRendererObj, this.strMaterials, 50, this.height - 85, 0xFFFFFF);
        drawCenteredString(this.fontRendererObj, this.strPrinter, 50, this.height - 45, 0xFFFFFF);
        drawCenteredString(this.fontRendererObj, this.strLayers, this.width - 50, this.height - 165, 0xFFFFFF);
        drawCenteredString(this.fontRendererObj, this.strOperations, this.width - 50, this.height - 120, 0xFFFFFF);

        drawString(this.fontRendererObj, this.strX, this.centerX - 65, this.centerY - 24, 0xFFFFFF);
        drawString(this.fontRendererObj, this.strY, this.centerX - 65, this.centerY + 1, 0xFFFFFF);
        drawString(this.fontRendererObj, this.strZ, this.centerX - 65, this.centerY + 26, 0xFFFFFF);

        super.drawScreen(par1, par2, par3);
    }
}
