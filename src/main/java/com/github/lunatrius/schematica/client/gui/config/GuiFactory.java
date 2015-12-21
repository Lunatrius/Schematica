package com.github.lunatrius.schematica.client.gui.config;

import com.github.lunatrius.core.client.gui.config.GuiConfigComplex;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Set;

public class GuiFactory implements IModGuiFactory {
    @Override
    public void initialize(final Minecraft minecraftInstance) {
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return GuiModConfig.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(final RuntimeOptionCategoryElement element) {
        return null;
    }

    public static class GuiModConfig extends GuiConfigComplex {
        public GuiModConfig(final GuiScreen guiScreen) {
            super(guiScreen, Reference.MODID, ConfigurationHandler.configuration, Names.Config.LANG_PREFIX);
        }
    }
}
