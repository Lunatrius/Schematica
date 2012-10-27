package lunatrius.schematica;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.Properties;
import java.util.logging.Level;

import lunatrius.schematica.util.Config;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;

@Mod(modid = "Schematica")
public class Schematica {
	private final Settings settings = Settings.instance();
	private int ticks = -1;

	@Instance("Schematica")
	public static Schematica instance;

	@PreInit
	public void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());

		config.load();
		this.settings.enableAlpha = Config.getBoolean(config, "alphaEnabled", Configuration.CATEGORY_GENERAL, this.settings.enableAlpha, "Enable transparent textures.");
		this.settings.alpha = Config.getInt(config, "alpha", Configuration.CATEGORY_GENERAL, (int) (this.settings.alpha * 255), 0, 255, "Alpha value used when rendering the schematic.") / 255.0f;
		this.settings.highlight = Config.getBoolean(config, "highlight", Configuration.CATEGORY_GENERAL, this.settings.highlight, "Highlight invalid placed blocks and to be placed blocks.");
		this.settings.renderRange.x = Config.getInt(config, "renderRangeX", Configuration.CATEGORY_GENERAL, this.settings.renderRange.x, 5, 50, "Render range along the X axis.");
		this.settings.renderRange.y = Config.getInt(config, "renderRangeY", Configuration.CATEGORY_GENERAL, this.settings.renderRange.y, 5, 50, "Render range along the Y axis.");
		this.settings.renderRange.z = Config.getInt(config, "renderRangeZ", Configuration.CATEGORY_GENERAL, this.settings.renderRange.z, 5, 50, "Render range along the Z axis.");
		this.settings.blockDelta = Config.getFloat(config, "blockDelta", Configuration.CATEGORY_GENERAL, this.settings.blockDelta, 0.0f, 0.5f, "Delta value used for highlighting (if you're having issue with overlapping textures try setting this value higher).");
		config.save();

		try {
			String assetsDir = "lunatrius/schematica/assets/";
			String langDir = assetsDir + "lang/";
			ClassLoader classLoader = getClass().getClassLoader();
			InputStream stream = classLoader.getResourceAsStream(langDir + "lang.txt");

			BufferedReader input = new BufferedReader(new InputStreamReader(stream));
			try {
				String lang = "";
				while ((lang = input.readLine()) != null) {
					if (lang.length() > 0) {
						System.out.println("Loading language file: " + lang);
						InputStream streamLang = classLoader.getResourceAsStream(langDir + lang + ".lang");
						Properties properties = new Properties();
						properties.load(streamLang);
						for (String key : properties.stringPropertyNames()) {
							LanguageRegistry.instance().addStringLocalization(key, lang, properties.getProperty(key));
						}
					}
				}
			} finally {
				input.close();
			}
		} catch (Exception e) {
			FMLCommonHandler.instance().getFMLLogger().log(Level.SEVERE, "Could not load language files - corrupted installation detected!", e);
			throw new RuntimeException(e);
		}

		Settings.schematicDirectory.mkdirs();
		Settings.textureDirectory.mkdirs();
	}

	@Init
	public void init(FMLInitializationEvent event) {
		try {
			MinecraftForge.EVENT_BUS.register(new Render());

			KeyBindingRegistry.registerKeyBinding(new KeyBindingHandler(this.settings.keyBindings, new boolean[this.settings.keyBindings.length]));
			TickRegistry.registerTickHandler(new Ticker(EnumSet.of(TickType.CLIENT)), Side.CLIENT);
		} catch (Exception e) {
			FMLCommonHandler.instance().getFMLLogger().log(Level.SEVERE, "Could not initialize the mod!", e);
			throw new RuntimeException(e);
		}
	}

	public void keyboardEvent(KeyBinding keyBinding, boolean down) {
		if (down) {
			this.settings.keyboardEvent(keyBinding);
		}
	}

	public boolean onTick(TickType tick, boolean start) {
		if (start) {
			return true;
		}

		if (--this.ticks < 0 && tick == TickType.CLIENT && this.settings.minecraft.thePlayer != null && this.settings.isRenderingSchematic && this.settings.schematic != null) {
			this.ticks = 2;
			updateWorldMatrix();
		}

		return true;
	}

	private void updateWorldMatrix() {
		SchematicWorld world = this.settings.schematic;
		int[][][] worldMatrix = this.settings.schematicMatrix;

		World mcWorld = this.settings.minecraft.theWorld;

		int x, y, z;
		int blockId = 0;
		int realBlockId = 0;

		for (x = 0; x < world.width(); x++) {
			for (y = 0; y < world.height(); y++) {
				for (z = 0; z < world.length(); z++) {
					worldMatrix[x][y][z] = 0;
					try {
						blockId = world.getBlockId(x, y, z);
						realBlockId = mcWorld.getBlockId(x + this.settings.offset.x, y + this.settings.offset.y, z + this.settings.offset.z);

						if (realBlockId != 0) {
							if (blockId != realBlockId) {
								worldMatrix[x][y][z] = 0x02;
							} else if (world.getBlockMetadata(x, y, z) != mcWorld.getBlockMetadata(x + this.settings.offset.x, y + this.settings.offset.y, z + this.settings.offset.z)) {
								worldMatrix[x][y][z] = 0x04;
							}
						} else if (realBlockId == 0 && blockId > 0 && blockId < 0x1000) {
							worldMatrix[x][y][z] = 0x01;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
