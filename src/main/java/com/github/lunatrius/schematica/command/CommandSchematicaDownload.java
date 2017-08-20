package com.github.lunatrius.schematica.command;

import com.github.lunatrius.core.util.FileUtils;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileFilterSchematic;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSchematicaDownload extends CommandSchematicaBase {
    private static final FileFilterSchematic FILE_FILTER_SCHEMATIC = new FileFilterSchematic(false);

    @Override
    public String getName() {
        return Names.Command.Download.NAME;
    }

    @Override
    public String getUsage(final ICommandSender sender) {
        return Names.Command.Download.Message.USAGE;
    }

    @Override
    public List<String> getTabCompletions(final MinecraftServer server, final ICommandSender sender, final String[] args, final BlockPos pos) {
        if (!(sender instanceof EntityPlayer)) {
            return Collections.emptyList();
        }

        final File directory = Schematica.proxy.getPlayerSchematicDirectory((EntityPlayer) sender, true);
        final File[] files = directory.listFiles(FILE_FILTER_SCHEMATIC);

        if (files != null) {
            final List<String> filenames = new ArrayList<String>();

            for (final File file : files) {
                filenames.add(file.getName());
            }

            return getListOfStringsMatchingLastWord(args, filenames);
        }

        return Collections.emptyList();
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(getUsage(sender));
        }

        if (!(sender instanceof EntityPlayerMP)) {
            throw new CommandException(Names.Command.Download.Message.PLAYERS_ONLY);
        }

        final String filename = String.join(" ", args);
        final EntityPlayerMP player = (EntityPlayerMP) sender;
        final File directory = Schematica.proxy.getPlayerSchematicDirectory(player, true);
        if (!FileUtils.contains(directory, filename)) {
            Reference.logger.error("{} has tried to download the file {}", player.getName(), filename);
            throw new CommandException(Names.Command.Download.Message.DOWNLOAD_FAILED);
        }

        final ISchematic schematic = SchematicFormat.readFromFile(directory, filename);

        if (schematic != null) {
            DownloadHandler.INSTANCE.transferMap.put(player, new SchematicTransfer(schematic, filename));
            sender.sendMessage(new TextComponentTranslation(Names.Command.Download.Message.DOWNLOAD_STARTED, filename));
        } else {
            throw new CommandException(Names.Command.Download.Message.DOWNLOAD_FAILED);
        }
    }
}
