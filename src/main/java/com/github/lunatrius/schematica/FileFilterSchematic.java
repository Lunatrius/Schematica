package com.github.lunatrius.schematica;

import java.io.File;
import java.io.FileFilter;

public class FileFilterSchematic implements FileFilter {
    private final boolean directory;

    public FileFilterSchematic(boolean dir) {
        this.directory = dir;
    }

    @Override
    public boolean accept(File file) {
        if (this.directory) {
            return file.isDirectory();
        }

        return file.getName().toLowerCase().endsWith(".schematic");
    }
}
