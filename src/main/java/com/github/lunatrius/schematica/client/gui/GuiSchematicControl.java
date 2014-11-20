package com.github.lunatrius.schematica.client.gui;

import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.schematica.SchematicPrinter;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.client.renderer.RendererSchematicGlobal;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.world.SchematicWorld;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiSchematicControl extends GuiScreen {
    private static final Vector3i ZERO = new Vector3i();

    @SuppressWarnings("unused")
    private final GuiScreen prevGuiScreen;

    private final SchematicWorld schematic;
    private final SchematicPrinter printer;

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

    private final String strMoveSchematic = I18n.format("schematica.gui.moveschematic");
    private final String strLayers = I18n.format("schematica.gui.layers");
    private final String strOperations = I18n.format("schematica.gui.operations");
    private final String strAll = I18n.format("schematica.gui.all");
    private final String strX = I18n.format("schematica.gui.x");
    private final String strY = I18n.format("schematica.gui.y");
    private final String strZ = I18n.format("schematica.gui.z");
    private final String strMaterials = I18n.format("schematica.gui.materials");
    private final String strPrinter = I18n.format("schematica.gui.printer");

    public GuiSchematicControl(GuiScreen guiScreen) {
        this.prevGuiScreen = guiScreen;
        this.schematic = Schematica.proxy.getActiveSchematic();
        this.printer = SchematicPrinter.INSTANCE;
    }

    @Override
    public void initGui() {
        this.centerX = this.width / 2;
        this.centerY = this.height / 2;

        this.buttonList.clear();

        int id = 0;

        this.btnDecX = new GuiButton(id++, this.centerX - 50, this.centerY - 30, 30, 20, I18n.format("schematica.gui.decrease"));
        this.buttonList.add(this.btnDecX);

        this.btnAmountX = new GuiButton(id++, this.centerX - 15, this.centerY - 30, 30, 20, Integer.toString(ClientProxy.INCREMENTS[this.incrementX]));
        this.buttonList.add(this.btnAmountX);

        this.btnIncX = new GuiButton(id++, this.centerX + 20, this.centerY - 30, 30, 20, I18n.format("schematica.gui.increase"));
        this.buttonList.add(this.btnIncX);

        this.btnDecY = new GuiButton(id++, this.centerX - 50, this.centerY - 5, 30, 20, I18n.format("schematica.gui.decrease"));
        this.buttonList.add(this.btnDecY);

        this.btnAmountY = new GuiButton(id++, this.centerX - 15, this.centerY - 5, 30, 20, Integer.toString(ClientProxy.INCREMENTS[this.incrementY]));
        this.buttonList.add(this.btnAmountY);

        this.btnIncY = new GuiButton(id++, this.centerX + 20, this.centerY - 5, 30, 20, I18n.format("schematica.gui.increase"));
        this.buttonList.add(this.btnIncY);

        this.btnDecZ = new GuiButton(id++, this.centerX - 50, this.centerY + 20, 30, 20, I18n.format("schematica.gui.decrease"));
        this.buttonList.add(this.btnDecZ);

        this.btnAmountZ = new GuiButton(id++, this.centerX - 15, this.centerY + 20, 30, 20, Integer.toString(ClientProxy.INCREMENTS[this.incrementZ]));
        this.buttonList.add(this.btnAmountZ);

        this.btnIncZ = new GuiButton(id++, this.centerX + 20, this.centerY + 20, 30, 20, I18n.format("schematica.gui.increase"));
        this.buttonList.add(this.btnIncZ);

        this.btnDecLayer = new GuiButton(id++, this.width - 90, this.height - 150, 25, 20, I18n.format("schematica.gui.decrease"));
        this.buttonList.add(this.btnDecLayer);

        this.btnIncLayer = new GuiButton(id++, this.width - 35, this.height - 150, 25, 20, I18n.format("schematica.gui.increase"));
        this.buttonList.add(this.btnIncLayer);

        this.btnHide = new GuiButton(id++, this.width - 90, this.height - 105, 80, 20, I18n.format(this.schematic != null && this.schematic.isRendering ? "schematica.gui.hide" : "schematica.gui.show"));
        this.buttonList.add(this.btnHide);

        this.btnMove = new GuiButton(id++, this.width - 90, this.height - 80, 80, 20, I18n.format("schematica.gui.movehere"));
        this.buttonList.add(this.btnMove);

        this.btnFlip = new GuiButton(id++, this.width - 90, this.height - 55, 80, 20, I18n.format("schematica.gui.flip"));
        this.buttonList.add(this.btnFlip);

        this.btnRotate = new GuiButton(id++, this.width - 90, this.height - 30, 80, 20, I18n.format("schematica.gui.rotate"));
        this.buttonList.add(this.btnRotate);

        this.btnMaterials = new GuiButton(id++, 10, this.height - 70, 80, 20, I18n.format("schematica.gui.materials"));
        this.buttonList.add(this.btnMaterials);

        this.btnPrint = new GuiButton(id++, 10, this.height - 30, 80, 20, I18n.format(this.printer.isPrinting() ? "schematica.gui.on" : "schematica.gui.off"));
        this.buttonList.add(this.btnPrint);

        this.btnDecX.enabled = this.schematic != null;
        this.btnAmountX.enabled = this.schematic != null;
        this.btnIncX.enabled = this.schematic != null;

        this.btnDecY.enabled = this.schematic != null;
        this.btnAmountY.enabled = this.schematic != null;
        this.btnIncY.enabled = this.schematic != null;

        this.btnDecZ.enabled = this.schematic != null;
        this.btnAmountZ.enabled = this.schematic != null;
        this.btnIncZ.enabled = this.schematic != null;

        this.btnDecLayer.enabled = this.schematic != null;
        this.btnIncLayer.enabled = this.schematic != null;
        this.btnHide.enabled = this.schematic != null;
        this.btnMove.enabled = this.schematic != null;
        // this.btnFlip.enabled = this.settings.schematic != null;
        this.btnFlip.enabled = false;
        this.btnRotate.enabled = this.schematic != null;
        this.btnMaterials.enabled = this.schematic != null;
        this.btnPrint.enabled = this.schematic != null && this.printer.isEnabled();
    }

    @Override
    protected void actionPerformed(GuiButton guiButton) {
        if (guiButton.enabled) {
            if (guiButton.id == this.btnDecX.id) {
                this.schematic.position.x -= ClientProxy.INCREMENTS[this.incrementX];
                RendererSchematicGlobal.INSTANCE.refresh();
            } else if (guiButton.id == this.btnIncX.id) {
                this.schematic.position.x += ClientProxy.INCREMENTS[this.incrementX];
                RendererSchematicGlobal.INSTANCE.refresh();
            } else if (guiButton.id == this.btnAmountX.id) {
                this.incrementX = (this.incrementX + 1) % ClientProxy.INCREMENTS.length;
                this.btnAmountX.displayString = Integer.toString(ClientProxy.INCREMENTS[this.incrementX]);
            } else if (guiButton.id == this.btnDecY.id) {
                this.schematic.position.y -= ClientProxy.INCREMENTS[this.incrementY];
                RendererSchematicGlobal.INSTANCE.refresh();
            } else if (guiButton.id == this.btnIncY.id) {
                this.schematic.position.y += ClientProxy.INCREMENTS[this.incrementY];
                RendererSchematicGlobal.INSTANCE.refresh();
            } else if (guiButton.id == this.btnAmountY.id) {
                this.incrementY = (this.incrementY + 1) % ClientProxy.INCREMENTS.length;
                this.btnAmountY.displayString = Integer.toString(ClientProxy.INCREMENTS[this.incrementY]);
            } else if (guiButton.id == this.btnDecZ.id) {
                this.schematic.position.z -= ClientProxy.INCREMENTS[this.incrementZ];
                RendererSchematicGlobal.INSTANCE.refresh();
            } else if (guiButton.id == this.btnIncZ.id) {
                this.schematic.position.z += ClientProxy.INCREMENTS[this.incrementZ];
                RendererSchematicGlobal.INSTANCE.refresh();
            } else if (guiButton.id == this.btnAmountZ.id) {
                this.incrementZ = (this.incrementZ + 1) % ClientProxy.INCREMENTS.length;
                this.btnAmountZ.displayString = Integer.toString(ClientProxy.INCREMENTS[this.incrementZ]);
            } else if (guiButton.id == this.btnDecLayer.id) {
                if (this.schematic != null) {
                    this.schematic.decrementRenderingLayer();
                }
                RendererSchematicGlobal.INSTANCE.refresh();
            } else if (guiButton.id == this.btnIncLayer.id) {
                if (this.schematic != null) {
                    this.schematic.incrementRenderingLayer();
                }
                RendererSchematicGlobal.INSTANCE.refresh();
            } else if (guiButton.id == this.btnHide.id) {
                this.btnHide.displayString = I18n.format(this.schematic != null && this.schematic.toggleRendering() ? "schematica.gui.hide" : "schematica.gui.show");
            } else if (guiButton.id == this.btnMove.id) {
                ClientProxy.moveSchematicToPlayer(this.schematic);
                RendererSchematicGlobal.INSTANCE.refresh();
            } else if (guiButton.id == this.btnFlip.id) {
                if (this.schematic != null) {
                    this.schematic.flip();
                    RendererSchematicGlobal.INSTANCE.createRendererSchematicChunks(this.schematic);
                    SchematicPrinter.INSTANCE.refresh();
                }
            } else if (guiButton.id == this.btnRotate.id) {
                if (this.schematic != null) {
                    this.schematic.rotate();
                    RendererSchematicGlobal.INSTANCE.createRendererSchematicChunks(this.schematic);
                    SchematicPrinter.INSTANCE.refresh();
                }
            } else if (guiButton.id == this.btnMaterials.id) {
                this.mc.displayGuiScreen(new GuiSchematicMaterials(this));
            } else if (guiButton.id == this.btnPrint.id && this.printer.isEnabled()) {
                boolean isPrinting = this.printer.togglePrinting();
                this.btnPrint.displayString = I18n.format(isPrinting ? "schematica.gui.on" : "schematica.gui.off");
            }
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

        int renderingLayer = this.schematic != null ? this.schematic.renderingLayer : -1;
        drawCenteredString(this.fontRendererObj, renderingLayer < 0 ? this.strAll : Integer.toString(renderingLayer + 1), this.width - 50, this.height - 145, 0xFFFFFF);

        Vector3i position = this.schematic != null ? this.schematic.position : ZERO;
        drawString(this.fontRendererObj, this.strX, this.centerX - 65, this.centerY - 24, 0xFFFFFF);
        drawString(this.fontRendererObj, Integer.toString(position.x), this.centerX + 55, this.centerY - 24, 0xFFFFFF);

        drawString(this.fontRendererObj, this.strY, this.centerX - 65, this.centerY + 1, 0xFFFFFF);
        drawString(this.fontRendererObj, Integer.toString(position.y), this.centerX + 55, this.centerY + 1, 0xFFFFFF);

        drawString(this.fontRendererObj, this.strZ, this.centerX - 65, this.centerY + 26, 0xFFFFFF);
        drawString(this.fontRendererObj, Integer.toString(position.z), this.centerX + 55, this.centerY + 26, 0xFFFFFF);

        super.drawScreen(par1, par2, par3);
    }
}
