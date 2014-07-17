package com.github.lunatrius.schematica.client.gui;

import com.github.lunatrius.schematica.FileFilterSchematic;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.Settings;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.lib.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
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
import java.util.List;

public class GuiSchematicLoad extends GuiScreen {
	private static final FileFilterSchematic FILE_FILTER_FOLDER = new FileFilterSchematic(true);
	private static final FileFilterSchematic FILE_FILTER_SCHEMATIC = new FileFilterSchematic(false);

	private final Settings settings = Settings.instance;
	private final GuiScreen prevGuiScreen;
	private GuiSchematicLoadSlot guiSchematicLoadSlot;

	private GuiButton btnOpenDir = null;
	private GuiButton btnDone = null;

	private final String strTitle = I18n.format("schematica.gui.title");
	private final String strFolderInfo = I18n.format("schematica.gui.folderInfo");

	protected File currentDirectory = ConfigurationHandler.schematicDirectory;
	protected final List<GuiSchematicEntry> schematicFiles = new ArrayList<GuiSchematicEntry>();

	public GuiSchematicLoad(GuiScreen guiScreen) {
		this.prevGuiScreen = guiScreen;
	}

	@Override
	public void initGui() {
		int id = 0;

		this.btnOpenDir = new GuiButton(id++, this.width / 2 - 154, this.height - 36, 150, 20, I18n.format("schematica.gui.openFolder"));
		this.buttonList.add(this.btnOpenDir);

		this.btnDone = new GuiButton(id++, this.width / 2 + 4, this.height - 36, 150, 20, I18n.format("schematica.gui.done"));
		this.buttonList.add(this.btnDone);

		this.guiSchematicLoadSlot = new GuiSchematicLoadSlot(this);

		reloadSchematics();
	}

	@Override
	protected void actionPerformed(GuiButton guiButton) {
		if (guiButton.enabled) {
			if (guiButton.id == this.btnOpenDir.id) {
				boolean retry = false;

				try {
					Class c = Class.forName("java.awt.Desktop");
					Object m = c.getMethod("getDesktop").invoke(null);
					c.getMethod("browse", URI.class).invoke(m, ConfigurationHandler.schematicDirectory.toURI());
				} catch (Throwable e) {
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
				this.mc.displayGuiScreen(this.prevGuiScreen);
			} else {
				this.guiSchematicLoadSlot.actionPerformed(guiButton);
			}
		}
	}

	@Override
	public void drawScreen(int x, int y, float partialTicks) {
		this.guiSchematicLoadSlot.drawScreen(x, y, partialTicks);

		drawCenteredString(this.fontRendererObj, this.strTitle, this.width / 2, 4, 0x00FFFFFF);
		drawCenteredString(this.fontRendererObj, this.strFolderInfo, this.width / 2 - 78, this.height - 12, 0x00808080);

		super.drawScreen(x, y, partialTicks);
	}

	@Override
	public void onGuiClosed() {
		// loadSchematic();
	}

	protected void changeDirectory(String directory) {
		this.currentDirectory = new File(this.currentDirectory, directory);

		reloadSchematics();
	}

	protected void reloadSchematics() {
		String name = null;
		Item item = null;

		this.schematicFiles.clear();

		try {
			if (!this.currentDirectory.getCanonicalPath().equals(ConfigurationHandler.schematicDirectory.getCanonicalPath())) {
				this.schematicFiles.add(new GuiSchematicEntry("..", Items.lava_bucket, 0, true));
			}
		} catch (IOException e) {
			Reference.logger.error("Failed to add GuiSchematicEntry!", e);
		}

		File[] filesFolders = this.currentDirectory.listFiles(FILE_FILTER_FOLDER);
		if (filesFolders == null) {
			Reference.logger.error(String.format("listFiles returned null (directory: %s)!", this.currentDirectory));
		} else {
			for (File file : filesFolders) {
				if (file == null) {
					continue;
				}

				name = file.getName();

				File[] files = file.listFiles();
				item = (files == null || files.length == 0) ? Items.bucket : Items.water_bucket;

				this.schematicFiles.add(new GuiSchematicEntry(name, item, 0, file.isDirectory()));
			}
		}

		File[] filesSchematics = this.currentDirectory.listFiles(FILE_FILTER_SCHEMATIC);
		if (filesSchematics == null || filesSchematics.length == 0) {
			this.schematicFiles.add(new GuiSchematicEntry(I18n.format("schematica.gui.noschematic"), Blocks.dirt, 0, false));
		} else {
			for (File file : filesSchematics) {
				name = file.getName();

				this.schematicFiles.add(new GuiSchematicEntry(name, SchematicWorld.getIconFromFile(file), file.isDirectory()));
			}
		}
	}

	private void loadSchematic() {
		int selectedIndex = this.guiSchematicLoadSlot.selectedIndex;

		try {
			if (selectedIndex >= 0 && selectedIndex < this.schematicFiles.size()) {
				GuiSchematicEntry schematic = this.schematicFiles.get(selectedIndex);
				Schematica.proxy.loadSchematic(null, this.currentDirectory, schematic.getName());
			}
		} catch (Exception e) {
			Reference.logger.error("Failed to load schematic!", e);
		}
		this.settings.moveHere(Schematica.proxy.getActiveSchematic());
	}
}
