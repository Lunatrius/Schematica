package com.github.lunatrius.schematica.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public abstract class CommandSchematicaBase extends CommandBase {
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        // TODO: add logic for the client side when ready
        return super.canCommandSenderUseCommand(sender);
    }
}
