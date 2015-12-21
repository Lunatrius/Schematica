package com.github.lunatrius.schematica.command.client;

import com.github.lunatrius.schematica.block.state.pattern.BlockStateReplacer;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.command.CommandSchematicaBase;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.state.pattern.BlockStateHelper;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;

import java.util.List;

public class CommandSchematicaReplace extends CommandSchematicaBase {
    private static final FMLControlledNamespacedRegistry<Block> BLOCK_REGISTRY = GameData.getBlockRegistry();

    @Override
    public String getCommandName() {
        return Names.Command.Replace.NAME;
    }

    @Override
    public String getCommandUsage(final ICommandSender sender) {
        return Names.Command.Replace.Message.USAGE;
    }

    @Override
    public List<String> addTabCompletionOptions(final ICommandSender sender, final String[] args, final BlockPos pos) {
        if (args.length < 3) {
            return getListOfStringsMatchingLastWord(args, BLOCK_REGISTRY.getKeys());
        }

        return null;
    }

    @Override
    public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {
        final SchematicWorld schematic = ClientProxy.schematic;
        if (schematic == null) {
            throw new CommandException(Names.Command.Replace.Message.NO_SCHEMATIC);
        }

        if (args.length != 2) {
            throw new CommandException(Names.Command.Replace.Message.USAGE);
        }

        try {
            final BlockStateReplacer.BlockStateInfo patternInfo = BlockStateReplacer.fromString(args[0]);
            final BlockStateHelper matcher = BlockStateReplacer.getMatcher(patternInfo);

            final BlockStateReplacer.BlockStateInfo replacementInfo = BlockStateReplacer.fromString(args[1]);
            final BlockStateReplacer replacer = BlockStateReplacer.forBlockState(replacementInfo.block.getDefaultState());

            final int count = schematic.replaceBlock(matcher, replacer, replacementInfo.stateData);

            sender.addChatMessage(new ChatComponentTranslation(Names.Command.Replace.Message.SUCCESS, count));
        } catch (final Exception e) {
            Reference.logger.error("Something went wrong!", e);
            throw new CommandException(e.getMessage());
        }
    }
}
