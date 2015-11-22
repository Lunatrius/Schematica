package com.github.lunatrius.schematica.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

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

        return file.getName().toLowerCase(Locale.ENGLISH).endsWith(".schematic");
    }
}
