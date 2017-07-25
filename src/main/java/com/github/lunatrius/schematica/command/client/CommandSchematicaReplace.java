package com.github.lunatrius.schematica.command.client;

import com.github.lunatrius.schematica.block.state.pattern.BlockStateReplacer;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.command.CommandSchematicaBase;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSchematicaReplace extends CommandSchematicaBase {
    @Override
    public String getName() {
        return Names.Command.Replace.NAME;
    }

    @Override
    public String getUsage(final ICommandSender sender) {
        return Names.Command.Replace.Message.USAGE;
    }

    @Override
    public List<String> getTabCompletions(final MinecraftServer server, final ICommandSender sender, final String[] args, @Nullable final BlockPos pos) {
        if (args.length < 3) {
            return getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys());
        }

        return Collections.emptyList();
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        final SchematicWorld schematic = ClientProxy.schematic;
        if (schematic == null) {
            throw new CommandException(Names.Command.Replace.Message.NO_SCHEMATIC);
        }

        if (args.length != 2) {
            throw new CommandException(Names.Command.Replace.Message.USAGE);
        }

        try {
            final BlockStateReplacer.BlockStateInfo patternInfo = BlockStateReplacer.fromString(args[0]);
            final BlockStateMatcher matcher = BlockStateReplacer.getMatcher(patternInfo);

            final BlockStateReplacer.BlockStateInfo replacementInfo = BlockStateReplacer.fromString(args[1]);
            final BlockStateReplacer replacer = BlockStateReplacer.forBlockState(replacementInfo.block.getDefaultState());

            final int count = schematic.replaceBlock(matcher, replacer, replacementInfo.stateData);

            sender.sendMessage(new TextComponentTranslation(Names.Command.Replace.Message.SUCCESS, count));
        } catch (final Exception e) {
            Reference.logger.error("Something went wrong!", e);
            throw new CommandException(e.getMessage());
        }
    }
}
