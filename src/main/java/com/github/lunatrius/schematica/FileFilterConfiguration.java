package com.github.lunatrius.schematica;

import java.io.File;

public class FileFilterConfiguration implements java.io.FileFilter {
	@Override
	public boolean accept(File file) {
		String filename = file.getName().toLowerCase();
		return (filename.startsWith("alias") || filename.startsWith("flip") || filename.startsWith("rotation")) && filename.endsWith(".properties");
	}
}
