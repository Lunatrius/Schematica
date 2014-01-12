package com.github.lunatrius.schematica.client.gui;

import net.minecraft.item.ItemStack;

public class GuiSchematicEntry {
	private final String name;
	private final int itemID;
	private final int itemDamage;
	private final boolean isDirectory;
	private final ItemStack itemStack;

	public GuiSchematicEntry(String name, ItemStack itemStack, boolean isDirectory) {
		this.name = name;
		this.itemID = itemStack.itemID;
		this.itemDamage = itemStack.getItemDamage();
		this.isDirectory = isDirectory;
		this.itemStack = new ItemStack(itemStack.itemID, 1, itemStack.getItemDamage());
	}

	public GuiSchematicEntry(String name, int itemID, int itemDamage, boolean isDirectory) {
		this.name = name;
		this.itemID = itemID;
		this.itemDamage = itemDamage;
		this.isDirectory = isDirectory;
		this.itemStack = new ItemStack(itemID, 1, itemDamage);
	}

	public String getName() {
		return this.name;
	}

	public int getItemID() {
		return this.itemID;
	}

	public int getItemDamage() {
		return this.itemDamage;
	}

	public boolean isDirectory() {
		return this.isDirectory;
	}

	public ItemStack getItemStack() {
		return this.itemStack;
	}
}
