package com.github.lunatrius.schematica.command;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class CommandSchematicaBase extends CommandBase {
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(final MinecraftServer server, final ICommandSender sender) {
        // TODO: add logic for the client side when ready
        return super.checkPermission(server, sender) || (sender instanceof EntityPlayerMP && getRequiredPermissionLevel() <= 0);
    }

    protected <T extends ITextComponent> T withStyle(final T component, final TextFormatting formatting, @Nullable final String command) {
        final Style style = new Style();
        style.setColor(formatting);

        if (command != null) {
            style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        }

        component.setStyle(style);

        return component;
    }
}
