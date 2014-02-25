package com.github.lunatrius.schematica.lib;

import com.github.lunatrius.schematica.config.Config;
import com.google.common.base.Throwables;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;

public class Reference {
	static {
		Properties prop = new Properties();

		try {
			InputStream stream = Reference.class.getClassLoader().getResourceAsStream("version.properties");
			prop.load(stream);
			stream.close();
		} catch (Exception e) {
			Throwables.propagate(e);
		}

		VERSION = prop.getProperty("version.mod");
		FORGE = prop.getProperty("version.forge");
		MINECRAFT = prop.getProperty("version.minecraft");
	}

	public static final String MODID = "Schematica";
	public static final String NAME = "Schematica";
	public static final String VERSION;
	public static final String FORGE;
	public static final String MINECRAFT;
	public static final String PROXY_COMMON = "com.github.lunatrius.schematica.CommonProxy";
	public static final String PROXY_CLIENT = "com.github.lunatrius.schematica.client.ClientProxy";

	public static Logger logger = null;
	public static Config config = null;
}
