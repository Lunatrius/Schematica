package com.github.lunatrius.schematica.client.gui.load;

import com.github.lunatrius.core.client.gui.GuiScreenBase;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileFilterSchematic;
import com.github.lunatrius.schematica.world.schematic.SchematicUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import org.lwjgl.Sys;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiSchematicLoad extends GuiScreenBase {
    private static final FileFilterSchematic FILE_FILTER_FOLDER = new FileFilterSchematic(true);
    private static final FileFilterSchematic FILE_FILTER_SCHEMATIC = new FileFilterSchematic(false);

    private GuiSchematicLoadSlot guiSchematicLoadSlot;

    private GuiButton btnOpenDir = null;
    private GuiButton btnDone = null;

    private final String strTitle = I18n.format(Names.Gui.Load.TITLE);
    private final String strFolderInfo = I18n.format(Names.Gui.Load.FOLDER_INFO);
    private String strNoSchematic = I18n.format(Names.Gui.Load.NO_SCHEMATIC);

    protected File currentDirectory = ConfigurationHandler.schematicDirectory;
    protected final List<GuiSchematicEntry> schematicFiles = new ArrayList<GuiSchematicEntry>();

    public GuiSchematicLoad(final GuiScreen guiScreen) {
        super(guiScreen);
    }

    @Override
    public void initGui() {
        int id = 0;

        this.btnOpenDir = new GuiButton(id++, this.width / 2 - 154, this.height - 36, 150, 20, I18n.format(Names.Gui.Load.OPEN_FOLDER));
        this.buttonList.add(this.btnOpenDir);

        this.btnDone = new GuiButton(id++, this.width / 2 + 4, this.height - 36, 150, 20, I18n.format(Names.Gui.DONE));
        this.buttonList.add(this.btnDone);

        this.guiSchematicLoadSlot = new GuiSchematicLoadSlot(this);

        reloadSchematics();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.guiSchematicLoadSlot.handleMouseInput();
    }

    @Override
    protected void actionPerformed(final GuiButton guiButton) {
        if (guiButton.enabled) {
            if (guiButton.id == this.btnOpenDir.id) {
                boolean retry = false;

                try {
                    final Class<?> c = Class.forName("java.awt.Desktop");
                    final Object m = c.getMethod("getDesktop").invoke(null);
                    c.getMethod("browse", URI.class).invoke(m, ConfigurationHandler.schematicDirectory.toURI());
                } catch (final Throwable e) {
                    retry = true;
                }

                if (retry) {
                    Reference.logger.info("Opening via Sys class!");
                    Sys.openURL("file://" + ConfigurationHandler.schematicDirectory.getAbsolutePath());
                }
            } else if (guiButton.id == this.btnDone.id) {
                if (Schematica.proxy.isLoadEnabled) {
                    loadSchematic();
                }
                this.mc.displayGuiScreen(this.parentScreen);
            } else {
                this.guiSchematicLoadSlot.actionPerformed(guiButton);
            }
        }
    }

    @Override
    public void drawScreen(final int x, final int y, final float partialTicks) {
        this.guiSchematicLoadSlot.drawScreen(x, y, partialTicks);

        drawCenteredString(this.fontRenderer, this.strTitle, this.width / 2, 4, 0x00FFFFFF);
        drawCenteredString(this.fontRenderer, this.strFolderInfo, this.width / 2 - 78, this.height - 12, 0x00808080);

        super.drawScreen(x, y, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        // loadSchematic();
    }

    protected void changeDirectory(final String directory) {
        this.currentDirectory = new File(this.currentDirectory, directory);

        try {
            this.currentDirectory = this.currentDirectory.getCanonicalFile();
        } catch (final IOException ioe) {
            Reference.logger.error("Failed to canonize directory!", ioe);
        }

        reloadSchematics();
    }

    protected void reloadSchematics() {
        String name = null;
        Item item = null;

        this.schematicFiles.clear();

        try {
            if (!this.currentDirectory.getCanonicalPath().equals(ConfigurationHandler.schematicDirectory.getCanonicalPath())) {
                this.schematicFiles.add(new GuiSchematicEntry("..", Items.LAVA_BUCKET, 0, true));
            }
        } catch (final IOException e) {
            Reference.logger.error("Failed to add GuiSchematicEntry!", e);
        }

        final File[] filesFolders = this.currentDirectory.listFiles(FILE_FILTER_FOLDER);
        if (filesFolders == null) {
            Reference.logger.error("listFiles returned null (directory: {})!", this.currentDirectory);
        } else {
            Arrays.sort(filesFolders, (final File a, final File b) -> a.getName().compareToIgnoreCase(b.getName()));
            for (final File file : filesFolders) {
                if (file == null) {
                    continue;
                }

                name = file.getName();

                final File[] files = file.listFiles();
                item = (files == null || files.length == 0) ? Items.BUCKET : Items.WATER_BUCKET;

                this.schematicFiles.add(new GuiSchematicEntry(name, item, 0, file.isDirectory()));
            }
        }

        final File[] filesSchematics = this.currentDirectory.listFiles(FILE_FILTER_SCHEMATIC);
        if (filesSchematics == null || filesSchematics.length == 0) {
            this.schematicFiles.add(new GuiSchematicEntry(this.strNoSchematic, Blocks.DIRT, 0, false));
        } else {
            Arrays.sort(filesSchematics, (final File a, final File b) -> a.getName().compareToIgnoreCase(b.getName()));
            for (final File file : filesSchematics) {
                name = file.getName();

                this.schematicFiles.add(new GuiSchematicEntry(name, SchematicUtil.getIconFromFile(file), file.isDirectory()));
            }
        }
    }

    private void loadSchematic() {
        final int selectedIndex = this.guiSchematicLoadSlot.selectedIndex;

        try {
            if (selectedIndex >= 0 && selectedIndex < this.schematicFiles.size()) {
                final GuiSchematicEntry schematicEntry = this.schematicFiles.get(selectedIndex);
                if (Schematica.proxy.loadSchematic(null, this.currentDirectory, schematicEntry.getName())) {
                    final SchematicWorld schematic = ClientProxy.schematic;
                    if (schematic != null) {
                        ClientProxy.moveSchematicToPlayer(schematic);
                    }
                }
            }
        } catch (final Exception e) {
            Reference.logger.error("Failed to load schematic!", e);
        }
    }
}
