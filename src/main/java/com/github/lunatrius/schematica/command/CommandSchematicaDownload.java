package com.github.lunatrius.schematica.command;

import com.github.lunatrius.schematica.FileFilterSchematic;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileUtils;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CommandSchematicaDownload extends CommandSchematicaBase {
    private static final FileFilterSchematic FILE_FILTER_SCHEMATIC = new FileFilterSchematic(false);

    @Override
    public String getCommandName() {
        return Names.Command.Download.NAME;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return Names.Command.Download.Message.USAGE;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (!(sender instanceof EntityPlayer)) {
            return null;
        }

        final File directory = Schematica.proxy.getPlayerSchematicDirectory((EntityPlayer) sender, true);
        final File[] files = directory.listFiles(FILE_FILTER_SCHEMATIC);

        if (files != null) {
            final List<String> filenames = new ArrayList<String>();

            for (File file : files) {
                filenames.add(FilenameUtils.removeExtension(file.getName()));
            }

            return getListOfStringsMatchingLastWord(args, filenames);
        }

        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        if (!(sender instanceof EntityPlayerMP)) {
            throw new CommandException(Names.Command.Download.Message.PLAYERS_ONLY);
        }

        final String filename = args[0] + ".schematic";
        final EntityPlayerMP player = (EntityPlayerMP) sender;
        final File directory = Schematica.proxy.getPlayerSchematicDirectory(player, true);
        if (!FileUtils.contains(directory, filename)) {
            Reference.logger.error(player.getDisplayName() + " has tried to download the file " + filename);
            throw new CommandException(Names.Command.Download.Message.DOWNLOAD_FAILED);
        }

        final ISchematic schematic = SchematicFormat.readFromFile(directory, filename);

        if (schematic != null) {
            DownloadHandler.INSTANCE.transferMap.put(player, new SchematicTransfer(schematic, filename));
            sender.addChatMessage(new ChatComponentTranslation(Names.Command.Download.Message.DOWNLOAD_STARTED, filename));
        } else {
            throw new CommandException(Names.Command.Download.Message.DOWNLOAD_FAILED);
        }
    }
}
