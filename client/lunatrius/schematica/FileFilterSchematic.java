package lunatrius.schematica;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FileFilterSchematic extends FileFilter implements java.io.FileFilter {
	private final boolean directory;

	public FileFilterSchematic(boolean dir) {
		super();
		this.directory = dir;
	}

	@Override
	public boolean accept(File file) {
		if (this.directory) {
			return file.isDirectory();
		}

		return file.getPath().toLowerCase().endsWith(".schematic");
	}

	@Override
	public String getDescription() {
		return "Schematic files (*.schematic)";
	}
}
