package com.github.lunatrius.schematica.command;

import com.github.lunatrius.core.util.FileUtils;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.util.Arrays;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSchematicaRemove extends CommandSchematicaBase {
    @Override
    public String getName() {
        return Names.Command.Remove.NAME;
    }

    @Override
    public String getUsage(final ICommandSender sender) {
        return Names.Command.Remove.Message.USAGE;
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(getUsage(sender));
        }

        if (!(sender instanceof EntityPlayer)) {
            throw new CommandException(Names.Command.Remove.Message.PLAYERS_ONLY);
        }

        final EntityPlayer player = (EntityPlayer) sender;

        boolean delete = false;
        String name = String.join(" ", args);

        if (args.length > 1) {
            //check if the last parameter is a hash, which constitutes a confirmation.
            final String potentialNameHash = args[args.length - 1];
            if (potentialNameHash.length() == 32) {
                //We probably have a match.
                final String[] a = Arrays.copyOfRange(args, 0, args.length - 1);
                //The name then should be everything except the last element
                name = String.join(" ", a);

                final String hash = Hashing.md5().hashString(name, Charsets.UTF_8).toString();

                if (potentialNameHash.equals(hash)) {
                    delete = true;
                }
            }
        }

        final File schematicDirectory = Schematica.proxy.getPlayerSchematicDirectory(player, true);
        final File file = new File(schematicDirectory, name);
        if (!FileUtils.contains(schematicDirectory, file)) {
            Reference.logger.error("{} has tried to download the file {}", player.getName(), name);
            throw new CommandException(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND);
        }

        if (file.exists()) {
            if (delete) {
                if (file.delete()) {
                    sender.sendMessage(new TextComponentTranslation(Names.Command.Remove.Message.SCHEMATIC_REMOVED, name));
                } else {
                    throw new CommandException(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND);
                }
            } else {
                final String hash = Hashing.md5().hashString(name, Charsets.UTF_8).toString();
                final String confirmCommand = String.format("/%s %s %s", Names.Command.Remove.NAME, name, hash);
                final ITextComponent chatComponent = new TextComponentTranslation(Names.Command.Remove.Message.ARE_YOU_SURE_START, name);
                chatComponent.appendSibling(new TextComponentString(" ["));
                chatComponent.appendSibling(withStyle(new TextComponentTranslation(Names.Command.Remove.Message.YES), TextFormatting.RED, confirmCommand));
                chatComponent.appendSibling(new TextComponentString("]"));

                sender.sendMessage(chatComponent);
            }
        } else {
            throw new CommandException(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND);
        }
    }
}
