package com.github.lunatrius.schematica.command;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileUtils;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import joptsimple.internal.Strings;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.io.File;
import java.util.Arrays;

public class CommandSchematicaRemove extends CommandSchematicaBase {
    @Override
    public String getCommandName() {
        return Names.Command.Remove.NAME;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return Names.Command.Remove.Message.USAGE;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] arguments) throws CommandException{
        if (arguments.length < 1) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        if (!(sender instanceof EntityPlayer)) {
            throw new CommandException(Names.Command.Remove.Message.PLAYERS_ONLY);
        }

        final EntityPlayer player = (EntityPlayer) sender;

        boolean delete = false;
        String name = Strings.join(arguments, " ");

        if (arguments.length > 1) {
            //check if the last parameter is a hash, which constitutes a confirmation.
            String potentialNameHash = arguments[arguments.length - 1];
            if (potentialNameHash.length() == 32) {
                //We probably have a match.
                String[] a = Arrays.copyOfRange(arguments, 0, arguments.length - 1);
                //The name then should be everything except the last element
                name = Strings.join(a, " ");

                String hash = Hashing.md5().hashString(name, Charsets.UTF_8).toString();

                if (potentialNameHash.equals(hash)) {
                    delete = true;
                }
            }
        }

        String filename = String.format("%s.schematic", name);
        File schematicDirectory = Schematica.proxy.getPlayerSchematicDirectory(player, true);
        File file = new File(schematicDirectory, filename);
        if (!FileUtils.contains(schematicDirectory, file)) {
            Reference.logger.error("{} has tried to download the file {}", player.getCommandSenderName(), filename);
            throw new CommandException(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND);
        }

        if (file.exists()) {
            if (delete) {
                if (file.delete()) {
                    sender.addChatMessage(new ChatComponentTranslation(Names.Command.Remove.Message.SCHEMATIC_REMOVED, name));
                } else {
                    throw new CommandException(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND);
                }
            } else {
                String hash = Hashing.md5().hashString(name, Charsets.UTF_8).toString();
                String confirmCommand = String.format("/%s %s %s", Names.Command.Remove.NAME, name, hash);
                final IChatComponent chatComponent = new ChatComponentTranslation(Names.Command.Remove.Message.ARE_YOU_SURE_START, name)
                        .appendSibling(new ChatComponentText(" ["))
                        .appendSibling(
                                //Confirmation link
                                new ChatComponentTranslation(Names.Command.Remove.Message.YES)
                                        .setChatStyle(
                                                new ChatStyle()
                                                        .setColor(EnumChatFormatting.RED)
                                                        .setChatClickEvent(
                                                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, confirmCommand)
                                                        )
                                        )
                        )
                        .appendSibling(new ChatComponentText("]"));

                sender.addChatMessage(chatComponent);
            }
        } else {
            throw new CommandException(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND);
        }
    }
}
