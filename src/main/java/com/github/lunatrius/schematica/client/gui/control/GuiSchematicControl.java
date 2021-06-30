package com.github.lunatrius.schematica.client.gui.control;

import com.github.lunatrius.core.client.gui.GuiNumericField;
import com.github.lunatrius.core.client.gui.GuiScreenBase;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.client.gui.load.GuiSchematicLoad;
import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.client.renderer.RenderSchematic;
import com.github.lunatrius.schematica.client.util.FlipHelper;
import com.github.lunatrius.schematica.client.util.RotationHelper;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.client.world.SchematicWorld.LayerMode;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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

    private GuiButton btnUnload = null;
    private GuiButton btnLayerMode = null;
    private GuiNumericField nfLayer = null;

    private GuiButton btnHide = null;
    private GuiButton btnSnap = null;
    private GuiButton btnMove = null;
    private GuiButton btnFlipDirection = null;
    private GuiButton btnFlip = null;
    private GuiButton btnRotateDirection = null;
    private GuiButton btnRotate = null;

    private GuiButton btnMaterials = null;
    private GuiButton btnPrint = null;
    private GuiButton btnSchematics = null;


    private final String strMoveSchematic = I18n.format(Names.Gui.Control.MOVE_SCHEMATIC);
    private final String strOperations = I18n.format(Names.Gui.Control.OPERATIONS);
    private final String strUnload = I18n.format(Names.Gui.Control.UNLOAD);
    private final String strMaterials = I18n.format(Names.Gui.Control.MATERIALS);
    private final String strPrinter = I18n.format(Names.Gui.Control.PRINTER);
    private final String strHide = I18n.format(Names.Gui.Control.HIDE);
    private final String strShow = I18n.format(Names.Gui.Control.SHOW);
    private final String strX = I18n.format(Names.Gui.X);
    private final String strY = I18n.format(Names.Gui.Y);
    private final String strZ = I18n.format(Names.Gui.Z);
    private final String strOn = I18n.format(Names.Gui.ON);
    private final String strOff = I18n.format(Names.Gui.OFF);
    private final String strSchematics = I18n.format(Names.Gui.Control.SCHEMATICS);
    private final String strLayer = I18n.format(Names.Gui.Control.MODE_LAYERS);



    public GuiSchematicControl(final GuiScreen guiScreen) {
        super(guiScreen);
        this.schematic = ClientProxy.schematic;
        this.printer = SchematicPrinter.INSTANCE;
    }

    private final EnumFacing[] facingList = {EnumFacing.WEST,EnumFacing.DOWN,EnumFacing.NORTH,EnumFacing.EAST,EnumFacing.UP,EnumFacing.SOUTH};
    private final String[] facingStringList = {"X", "Y", "Z", "-X", "-Y", "-Z"};
    private final int[] facingColorList = {0xFF2222, 0x22FF22, 0x7F7FFF, 0xFF2222, 0x22FF22, 0x7F7FFF};

    @Override
    public void initGui() {

        this.centerX = this.width / 2;
        this.centerY = this.height / 2;


        this.buttonList.clear();

        int id = 0;

        this.numericX = new GuiNumericField(this.fontRenderer, id++, this.centerX - 50, this.centerY - 30, 100, 20);
        this.buttonList.add(this.numericX);

        this.numericY = new GuiNumericField(this.fontRenderer, id++, this.centerX - 50, this.centerY - 5, 100, 20);
        this.buttonList.add(this.numericY);

        this.numericZ = new GuiNumericField(this.fontRenderer, id++, this.centerX - 50, this.centerY + 20, 100, 20);
        this.buttonList.add(this.numericZ);


        this.btnSchematics = new GuiButton(id++, 10, 10,80,20, this.strSchematics);
        this.buttonList.add(this.btnSchematics);

        this.btnUnload = new GuiButton(id++,  10, 35, 80, 20, this.strUnload);
        this.buttonList.add(this.btnUnload);

        this.btnMaterials = new GuiButton(id++, 10, 85, 80, 20, this.strMaterials);
        this.buttonList.add(this.btnMaterials);

        this.btnLayerMode = new GuiButton(id++, this.width - 90, this.height - 200, 80, 20, I18n.format((this.schematic != null ? this.schematic.layerMode : LayerMode.ALL).name));
        this.buttonList.add(this.btnLayerMode);

        this.nfLayer = new GuiNumericField(this.fontRenderer, id++, this.width - 90, this.height - 175, 80, 20);
        this.buttonList.add(this.nfLayer);

        this.btnHide = new GuiButton(id++, this.width - 90, this.height - 130, 80, 20, this.schematic != null && this.schematic.isRendering ? this.strHide : this.strShow);
        this.buttonList.add(this.btnHide);

        this.btnSnap = new GuiButton(id++, this.width - 90, this.height - 105, 80, 20, I18n.format(Names.Gui.Control.SNAP));
        this.buttonList.add(this.btnSnap);

        this.btnMove = new GuiButton(id++, this.width - 90, this.height - 80, 80, 20, I18n.format(Names.Gui.Control.MOVE_HERE));
        this.buttonList.add(this.btnMove);

        this.btnFlipDirection = new GuiButton(id++, this.width - 35, this.height - 55, 24, 20, "X");
        this.btnFlipDirection.displayString = facingStringList[ClientProxy.axisFlip];
        this.btnFlipDirection.packedFGColour = facingColorList[ClientProxy.axisFlip];
        this.buttonList.add(this.btnFlipDirection);

        if (ClientProxy.axisFlip == 1) {
            this.btnFlip = new GuiUnicodeGlyphButton(id++, this.width - 90, this.height - 55, 56, 20, " " + I18n.format(Names.Gui.Control.FLIP), "\u2195", 2.0f);
        } else {
            this.btnFlip = new GuiUnicodeGlyphButton(id++, this.width - 90, this.height - 55, 56, 20, " " + I18n.format(Names.Gui.Control.FLIP), "\u2194", 2.0f);
        }
        this.buttonList.add(this.btnFlip);

        this.btnRotateDirection = new GuiButton(id++, this.width - 35, this.height - 30, 24, 20, "X");
        this.btnRotateDirection.displayString = facingStringList[ClientProxy.axisRotation];
        this.btnRotateDirection.packedFGColour = facingColorList[ClientProxy.axisRotation];
        this.buttonList.add(this.btnRotateDirection);

        this.btnRotate = new GuiUnicodeGlyphButton(id++, this.width - 90, this.height - 30, 56, 20, " " + I18n.format(Names.Gui.Control.ROTATE), "\u21ba", 2.0f);
        this.buttonList.add(this.btnRotate);

        this.btnPrint = new GuiButton(id++, 10, this.height - 30, 80, 20, this.printer.isPrinting() ? this.strPrinter+": "+this.strOn : this.strPrinter+": "+this.strOff);
        this.buttonList.add(this.btnPrint);

        this.numericX.setEnabled(this.schematic != null);
        this.numericY.setEnabled(this.schematic != null);
        this.numericZ.setEnabled(this.schematic != null);

        this.btnUnload.enabled = this.schematic != null;
        this.btnLayerMode.enabled = this.schematic != null;
        this.nfLayer.setEnabled(this.schematic != null && this.schematic.layerMode != LayerMode.ALL);

        this.btnHide.enabled = this.schematic != null;
        this.btnMove.enabled = this.schematic != null;
        this.btnFlipDirection.enabled = this.schematic != null;
        this.btnFlip.enabled = this.schematic != null;
        this.btnRotateDirection.enabled = this.schematic != null;
        this.btnRotate.enabled = this.schematic != null;
        this.btnMaterials.enabled = this.schematic != null;
        this.btnPrint.enabled = this.schematic != null && this.printer.isEnabled();

        this.btnSchematics.enabled = true;

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

    private void setMinMax(final GuiNumericField numericField) {
        numericField.setMinimum(Constants.World.MINIMUM_COORD);
        numericField.setMaximum(Constants.World.MAXIMUM_COORD);
    }

    private void setPoint(final GuiNumericField numX, final GuiNumericField numY, final GuiNumericField numZ, final BlockPos point) {
        numX.setValue(point.getX());
        numY.setValue(point.getY());
        numZ.setValue(point.getZ());
    }

    @Override
    protected void actionPerformed(final GuiButton guiButton) {
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
            } else if (guiButton.id == this.btnUnload.id) {
                Schematica.proxy.unloadSchematic();
                this.mc.displayGuiScreen(this.parentScreen);
            } else if (guiButton.id == this.btnLayerMode.id) {
                this.schematic.layerMode = LayerMode.next(this.schematic.layerMode);
                this.btnLayerMode.displayString = I18n.format(this.schematic.layerMode.name);
                this.nfLayer.setEnabled(this.schematic.layerMode != LayerMode.ALL);
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
            } else if (guiButton.id == this.btnFlipDirection.id) { //---------------------------------------------------------------------- Flip code
                ClientProxy.axisFlip = (ClientProxy.axisFlip+1)%3;
                guiButton.displayString = facingStringList[ClientProxy.axisFlip];
                guiButton.packedFGColour = facingColorList[ClientProxy.axisFlip];
                if (ClientProxy.axisFlip == 1) {
                    this.btnFlip = new GuiUnicodeGlyphButton(this.btnFlip.id, this.width - 90, this.height - 55, 56, 20, "  " + I18n.format(Names.Gui.Control.FLIP), "\u2195", 2.0f);
                } else {
                    this.btnFlip = new GuiUnicodeGlyphButton(this.btnFlip.id, this.width - 90, this.height - 55, 56, 20, " " + I18n.format(Names.Gui.Control.FLIP), "\u2194", 2.0f);
                }
                this.buttonList.set(this.btnFlip.id, this.btnFlip);
            } else if (guiButton.id == this.btnFlip.id) {
                if (FlipHelper.INSTANCE.flip(this.schematic, facingList[ClientProxy.axisFlip], isShiftKeyDown())) {
                    RenderSchematic.INSTANCE.refresh();
                    SchematicPrinter.INSTANCE.refresh();
                }
            } else if (guiButton.id == this.btnRotateDirection.id) {
                ClientProxy.axisRotation = (ClientProxy.axisRotation+1)%3;
                if (isCtrlKeyDown()) {
                    guiButton.displayString = facingStringList[ClientProxy.axisRotation+3];
                    guiButton.packedFGColour = facingColorList[ClientProxy.axisRotation+3];
                } else {
                    guiButton.displayString = facingStringList[ClientProxy.axisRotation];
                    guiButton.packedFGColour = facingColorList[ClientProxy.axisRotation];
                }
            } else if (guiButton.id == this.btnRotate.id) {
                if (isCtrlKeyDown()) {
                    if (RotationHelper.INSTANCE.rotate(this.schematic, facingList[ClientProxy.axisRotation+3], isShiftKeyDown())) {
                        setPoint(this.numericX, this.numericY, this.numericZ, this.schematic.position);
                        RenderSchematic.INSTANCE.refresh();
                        SchematicPrinter.INSTANCE.refresh();
                    }
                } else {
                    if (RotationHelper.INSTANCE.rotate(this.schematic, facingList[ClientProxy.axisRotation], isShiftKeyDown())) {
                        setPoint(this.numericX, this.numericY, this.numericZ, this.schematic.position);
                        RenderSchematic.INSTANCE.refresh();
                        SchematicPrinter.INSTANCE.refresh();
                    }
                }
            } else if (guiButton.id == this.btnMaterials.id) {
                this.mc.displayGuiScreen(new GuiSchematicMaterials(this));
            } else if (guiButton.id == this.btnPrint.id && this.printer.isEnabled()) {
                final boolean isPrinting = this.printer.togglePrinting();
                this.btnPrint.displayString = isPrinting ? this.strPrinter+": "+this.strOn : this.strPrinter+": "+this.strOff;
            } else if (guiButton.id == this.btnSchematics.id) {
                this.mc.displayGuiScreen(new GuiSchematicLoad(this.parentScreen));
            } else if (guiButton.id == this.btnSnap.id) {
                int schemx = this.schematic.position.getX();
                int schemz = this.schematic.position.getZ();
                double schemlen = this.schematic.getLength();
                double schemwidth = this.schematic.getWidth();

                this.schematic.position.x = (int)(Math.floor((schemx+128+schemwidth)/128)*128-64-schemwidth);
                this.schematic.position.z = (int)(Math.floor((schemz+128+schemlen)/128)*128-64-schemlen);
                RenderSchematic.INSTANCE.refresh();
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
            if (isCtrlKeyDown()) {
                this.btnRotate = new GuiUnicodeGlyphButton(this.btnRotate.id, this.width - 90, this.height - 30, 56, 20, " " + I18n.format(Names.Gui.Control.ROTATE), "\u21bb", 2.0f);
                this.btnRotateDirection.displayString = facingStringList[ClientProxy.axisRotation+3];
                this.btnRotateDirection.packedFGColour = facingColorList[ClientProxy.axisRotation+3];
            } else {
                this.btnRotate = new GuiUnicodeGlyphButton(this.btnRotate.id, this.width - 90, this.height - 30, 56, 20, " " + I18n.format(Names.Gui.Control.ROTATE), "\u21ba", 2.0f);
                this.btnRotateDirection.displayString = facingStringList[ClientProxy.axisRotation];
                this.btnRotateDirection.packedFGColour = facingColorList[ClientProxy.axisRotation];
            }
            this.buttonList.set(this.btnRotate.id, this.btnRotate);
            this.btnRotate.packedFGColour = isShiftKeyDown() ? 0xFF0000 : 0x000000;

        }
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        // drawDefaultBackground();

        drawCenteredString(this.fontRenderer, this.strMoveSchematic, this.centerX, this.centerY - 45, 0xFFFFFF);
        drawCenteredString(this.fontRenderer, this.strOperations, this.width - 50, this.height - 145, 0xFFFFFF);
        drawCenteredString(this.fontRenderer, this.strLayer, this.width - 50, this.height - 215, 0xFFFFFF);

        drawString(this.fontRenderer, this.strX, this.centerX - 65, this.centerY - 24, 0xFFFFFF);
        drawString(this.fontRenderer, this.strY, this.centerX - 65, this.centerY + 1, 0xFFFFFF);
        drawString(this.fontRenderer, this.strZ, this.centerX - 65, this.centerY + 26, 0xFFFFFF);

        GlStateManager.pushMatrix();
        GlStateManager.translate(centerX+71, centerY, this.zLevel);
        Entity entity = this.mc.getRenderViewEntity();
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch), -1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw), 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(-2.0F, -2.0F, -2.0F);
        OpenGlHelper.renderDirections(10);
        GlStateManager.popMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks);

    }
}
