package com.github.lunatrius.schematica;

import com.github.lunatrius.core.version.VersionChecker;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.lib.Reference;
import com.github.lunatrius.schematica.proxy.CommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MODID, name = Reference.NAME, guiFactory = Reference.GUI_FACTORY)
public class Schematica {
	@Instance(Reference.MODID)
	public static Schematica instance;

	@SidedProxy(serverSide = Reference.PROXY_SERVER, clientSide = Reference.PROXY_CLIENT)
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Reference.logger = event.getModLog();
		ConfigurationHandler.init(event.getSuggestedConfigurationFile());
		proxy.setConfigEntryClasses();

		proxy.registerKeybindings();
		proxy.createFolders();

		VersionChecker.registerMod(event.getModMetadata(), Reference.FORGE);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		try {
			proxy.registerEvents();
		} catch (Exception e) {
			Reference.logger.fatal("Could not initialize the mod!", e);
			throw new RuntimeException(e);
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
