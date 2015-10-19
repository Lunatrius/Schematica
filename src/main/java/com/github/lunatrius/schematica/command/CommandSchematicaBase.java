package com.github.lunatrius.schematica.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

public abstract class CommandSchematicaBase extends CommandBase {
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        // TODO: add logic for the client side when ready
        return canCommandSenderUse(sender);
    }

    @Override
    public boolean canCommandSenderUse(ICommandSender sender) {
        return super.canCommandSenderUse(sender) || (sender instanceof EntityPlayerMP && getRequiredPermissionLevel() <= 0);
    }
}
