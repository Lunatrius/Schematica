package com.github.lunatrius.schematica.util;

import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

public class FileFilterSchematic implements FileFilter {
    private final boolean directory;

    public FileFilterSchematic(final boolean dir) {
        this.directory = dir;
    }

    @Override
    public boolean accept(final File file) {
        if (this.directory) {
            return file.isDirectory();
        }

        String extension = "." + FilenameUtils.getExtension(file.getName().toLowerCase(Locale.ROOT));
        for (SchematicFormat format : SchematicFormat.FORMATS.values()) {
            if (format.getExtension().equals(extension)) {
                return true;
            }
        }
        return false;
    }
}
