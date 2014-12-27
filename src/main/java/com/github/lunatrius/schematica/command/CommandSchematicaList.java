package com.github.lunatrius.schematica.command;

import com.github.lunatrius.schematica.FileFilterSchematic;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.reference.Names;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.LinkedList;

public class CommandSchematicaList extends CommandBase {
    private static final FileFilterSchematic FILE_FILTER_SCHEMATIC = new FileFilterSchematic(false);

    @Override
    public String getCommandName() {
        return Names.Command.List.NAME;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return Names.Command.List.Message.USAGE;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] arguments) {
        if (!(sender instanceof EntityPlayer)) {
            sender.addChatMessage(new ChatComponentTranslation(Names.Command.Save.Message.PLAYERS_ONLY));
            return;
        }

        int page = 0;
        try {
            if (arguments.length > 0) {
                page = Integer.parseInt(arguments[0]) - 1;
                if (page < 0) {
                    page = 0;
                }
            }
        } catch (NumberFormatException e) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        final EntityPlayer player = (EntityPlayer) sender;
        int pageSize = 9; //maximum number of lines available without opening chat.
        int pageStart = page * pageSize;
        int pageEnd = pageStart + pageSize;
        int currentFile = 0;

        LinkedList<IChatComponent> componentsToSend = new LinkedList<IChatComponent>();

        File file = Schematica.proxy.getPlayerSchematicDirectory(player, true);
        final File[] files = file.listFiles(FILE_FILTER_SCHEMATIC);
        for (File path : files) {
            if (currentFile >= pageStart && currentFile < pageEnd) {
                String fileName = FilenameUtils.removeExtension(path.getName());

                IChatComponent chatComponent = new ChatComponentText(String.format("%2d (%s): %s [", (currentFile + 1), humanReadableByteCount(path.length()), fileName));
                String removeCommand = String.format("/%s %s", Names.Command.Remove.NAME, fileName);

                IChatComponent removeLink = new ChatComponentTranslation(Names.Command.List.Message.REMOVE)
                        .setChatStyle(
                                new ChatStyle()
                                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, removeCommand))
                                        .setColor(EnumChatFormatting.RED)
                        );
                chatComponent.appendSibling(removeLink);
                chatComponent.appendText("][");

                String downloadCommand = String.format("/%s %s", Names.Command.Download.NAME, fileName);
                IChatComponent downloadLink = new ChatComponentTranslation(Names.Command.List.Message.DOWNLOAD)
                        .setChatStyle(
                                new ChatStyle()
                                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, downloadCommand))
                                        .setColor(EnumChatFormatting.GREEN)
                        );
                chatComponent.appendSibling(downloadLink);
                chatComponent.appendText("]");

                componentsToSend.add(chatComponent);
            }
            ++currentFile;
        }

        if (currentFile == 0) {
            sender.addChatMessage(new ChatComponentTranslation(Names.Command.List.Message.NO_SCHEMATICS));
            return;
        }

        final int totalPages = (currentFile - 1) / pageSize;
        if (page > totalPages) {
            sender.addChatMessage(new ChatComponentTranslation(Names.Command.List.Message.NO_SUCH_PAGE));
            return;
        }

        sender.addChatMessage(new ChatComponentTranslation(Names.Command.List.Message.PAGE_HEADER, page + 1, totalPages + 1)
                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_GREEN)));
        for (IChatComponent chatComponent : componentsToSend) {
            sender.addChatMessage(chatComponent);
        }
    }

    //http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
    public static String humanReadableByteCount(long bytes) {
        final int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp - 1) + ("i");
        return String.format("%3.0f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
