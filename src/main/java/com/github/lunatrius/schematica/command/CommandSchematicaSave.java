package com.github.lunatrius.schematica.command;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSchematicaSave extends CommandSchematicaBase {
    @Override
    public String getName() {
        return Names.Command.Save.NAME;
    }

    @Override
    public String getUsage(final ICommandSender sender) {
        return Names.Command.Save.Message.USAGE;
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length < 7) {
            throw new WrongUsageException(getUsage(sender));
        }

        if (!(sender instanceof EntityPlayer)) {
            throw new CommandException(Names.Command.Save.Message.PLAYERS_ONLY);
        }

        final EntityPlayer player = (EntityPlayer) sender;

        if (Schematica.proxy.isPlayerQuotaExceeded(player)) {
            throw new CommandException(Names.Command.Save.Message.QUOTA_EXCEEDED);
        }

        final MBlockPos from = new MBlockPos();
        final MBlockPos to = new MBlockPos();
        final String filename;
        final String name;
        final String format;

        try {
            from.set(parseCoord(args[0]), parseCoord(args[1]), parseCoord(args[2]));
            to.set(parseCoord(args[3]), parseCoord(args[4]), parseCoord(args[5]));

            name = args[6];
            if (args.length >= 8) {
                format = args[7];
                if (!SchematicFormat.FORMATS.containsKey(format)) {
                    throw new CommandException(Names.Command.Save.Message.UNKNOWN_FORMAT, format);
                }
            } else {
                format = null;
            }
            filename = name + SchematicFormat.getExtension(format);
        } catch (final NumberFormatException exception) {
            throw new WrongUsageException(getUsage(sender));
        }

        Reference.logger.debug("Saving schematic from {} to {} to {}", from, to, filename);
        final File schematicDirectory = Schematica.proxy.getPlayerSchematicDirectory(player, true);
        if (schematicDirectory == null) {
            //Chances are that if this is null, we could not retrieve their UUID.
            Reference.logger.warn("Unable to determine the schematic directory for player {}", player);
            throw new CommandException(Names.Command.Save.Message.PLAYER_SCHEMATIC_DIR_UNAVAILABLE);
        }

        if (!schematicDirectory.exists()) {
            if (!schematicDirectory.mkdirs()) {
                Reference.logger.warn("Could not create player schematic directory {}", schematicDirectory.getAbsolutePath());
                throw new CommandException(Names.Command.Save.Message.PLAYER_SCHEMATIC_DIR_UNAVAILABLE);
            }
        }

        try {
            Schematica.proxy.saveSchematic(player, schematicDirectory, filename, player.getEntityWorld(), format, from, to);
            sender.sendMessage(new TextComponentTranslation(Names.Command.Save.Message.SAVE_SUCCESSFUL, name));
        } catch (final Exception e) {
            throw new CommandException(Names.Command.Save.Message.SAVE_FAILED, name);
        }
    }

    private int parseCoord(final String argument) throws NumberInvalidException {
        return parseInt(argument, Constants.World.MINIMUM_COORD, Constants.World.MAXIMUM_COORD);
    }
}
