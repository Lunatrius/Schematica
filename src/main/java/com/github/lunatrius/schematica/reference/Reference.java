package com.github.lunatrius.schematica.reference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

public class Reference {
    public static final String MODID = "Schematica";
    public static final String MODID_LOWER = MODID.toLowerCase(Locale.ENGLISH);
    public static final String NAME = "Schematica";
    public static final String VERSION = "${version}";
    public static final String FORGE = "${forgeversion}";
    public static final String MINECRAFT = "${mcversion}";
    public static final String PROXY_SERVER = "com.github.lunatrius.schematica.proxy.ServerProxy";
    public static final String PROXY_CLIENT = "com.github.lunatrius.schematica.proxy.ClientProxy";
    public static final String GUI_FACTORY = "com.github.lunatrius.schematica.client.gui.GuiFactory";

    public static Logger logger = LogManager.getLogger(Reference.MODID);
}
