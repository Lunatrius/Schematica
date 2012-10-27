package lunatrius.schematica;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FileFilterSchematic extends FileFilter implements java.io.FileFilter {
	public FileFilterSchematic() {
		super();
	}

	@Override
	public boolean accept(File file) {
		if (file.isDirectory()) {
			return false;
		}

		return file.getPath().toLowerCase().endsWith(".schematic");
	}

	@Override
	public String getDescription() {
		return "Schematic files (*.schematic)";
	}
}
