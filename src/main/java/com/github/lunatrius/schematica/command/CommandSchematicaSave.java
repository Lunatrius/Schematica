package com.github.lunatrius.schematica.command;

import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;

public class CommandSchematicaSave extends CommandBase {
    @Override
    public String getCommandName() {
        return Names.Command.Save.NAME;
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return Names.Command.Save.Message.USAGE;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] arguments) {
        if (arguments.length < 7) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        if (!(sender instanceof EntityPlayer)) {
            sender.addChatMessage(new ChatComponentTranslation(Names.Command.Save.Message.PLAYERS_ONLY));
            return;
        }

        Vector3i from = new Vector3i();
        Vector3i to = new Vector3i();
        String filename;
        String name;

        try {
            from.set(Integer.parseInt(arguments[0]), Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]));
            to.set(Integer.parseInt(arguments[3]), Integer.parseInt(arguments[4]), Integer.parseInt(arguments[5]));

            name = arguments[6];
            filename = String.format("%s.schematic", name);
        } catch (NumberFormatException exception) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        final EntityPlayer player = (EntityPlayer) sender;
        Reference.logger.info(String.format("Saving schematic from %s to %s to %s", from, to, filename));
        try {
            Schematica.proxy.saveSchematic(player, ConfigurationHandler.schematicDirectory, filename, player.getEntityWorld(), from, to);
            sender.addChatMessage(new ChatComponentTranslation(Names.Command.Save.Message.SAVE_SUCCESSFUL, name));
        } catch (Exception e) {
            sender.addChatMessage(new ChatComponentTranslation(Names.Command.Save.Message.SAVE_FAILED, name));
        }
    }
}
