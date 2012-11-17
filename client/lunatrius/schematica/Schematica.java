package lunatrius.schematica;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import lunatrius.schematica.util.Config;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Profiler;
import net.minecraft.src.RenderGlobal;
import net.minecraft.src.WorldRenderer;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
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
import cpw.mods.fml.relauncher.ReflectionHelper;

@Mod(modid = "Schematica")
public class Schematica {
	private final Settings settings = Settings.instance();
	private final Profiler profiler = this.settings.minecraft.mcProfiler;

	@Instance("Schematica")
	public static Schematica instance;

	private Field worldRenderersToUpdate = null;
	private int delay = -1;
	private int ticks = -1;
	private int worldRendererCount = -1;

	@PreInit
	public void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());

		config.load();
		this.settings.enableAlpha = Config.getBoolean(config, "alphaEnabled", Configuration.CATEGORY_GENERAL, this.settings.enableAlpha, "Enable transparent textures.");
		this.settings.alpha = Config.getInt(config, "alpha", Configuration.CATEGORY_GENERAL, (int) (this.settings.alpha * 255), 0, 255, "Alpha value used when rendering the schematic.") / 255.0f;
		this.settings.highlight = Config.getBoolean(config, "highlight", Configuration.CATEGORY_GENERAL, this.settings.highlight, "Highlight invalid placed blocks and to be placed blocks.");
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
						Settings.logger.log(Level.INFO, "Loading language file: " + lang);
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
			Settings.logger.log(Level.SEVERE, "Could not load language files - corrupted installation detected!", e);
			throw new RuntimeException(e);
		}

		Settings.schematicDirectory.mkdirs();
		Settings.textureDirectory.mkdirs();
	}

	@Init
	public void init(FMLInitializationEvent event) {
		try {
			MinecraftForge.EVENT_BUS.register(new RendererSchematicGlobal());

			KeyBindingRegistry.registerKeyBinding(new KeyBindingHandler(this.settings.keyBindings, new boolean[this.settings.keyBindings.length]));
			TickRegistry.registerTickHandler(new Ticker(EnumSet.of(TickType.CLIENT)), Side.CLIENT);

			initReflection();
		} catch (Exception e) {
			Settings.logger.log(Level.SEVERE, "Could not initialize the mod!", e);
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

		this.profiler.startSection("schematica");
		if (tick == TickType.CLIENT && this.settings.minecraft.thePlayer != null && this.settings.isRenderingSchematic && this.settings.schematic != null) {
			this.profiler.startSection("checkDirty");
			if (--this.delay < 0) {
				checkDirty();
				this.delay = this.worldRendererCount / 1000;
			}

			this.profiler.endStartSection("canUpdate");
			if (--this.ticks < 0) {
				RendererSchematicChunk.canUpdate = true;
				this.ticks = MathHelper.clamp_int(this.worldRendererCount / 10, 1, 100);
			}

			this.profiler.endSection();
		}
		this.profiler.endSection();

		return true;
	}

	public void initReflection() {
		try {
			this.worldRenderersToUpdate = ReflectionHelper.findField(RenderGlobal.class, "j", "worldRenderersToUpdate");
		} catch (Exception e) {
			this.worldRenderersToUpdate = null;
			e.printStackTrace();
		}
	}

	private void checkDirty() {
		if (this.worldRenderersToUpdate != null) {
			try {
				List<WorldRenderer> renderers = (List<WorldRenderer>) this.worldRenderersToUpdate.get(this.settings.minecraft.renderGlobal);
				if (renderers != null) {
					this.worldRendererCount = renderers.size();

					for (int i = 0; i < 1000 && i < renderers.size(); i++) {
						AxisAlignedBB worldRendererBoundingBox = renderers.get(this.worldRendererCount - 1 - i).rendererBoundingBox.getOffsetBoundingBox(-this.settings.offset.x, -this.settings.offset.y, -this.settings.offset.z);
						for (RendererSchematicChunk renderer : this.settings.sortedRendererSchematicChunk) {
							if (renderer.getBoundingBox().intersectsWith(worldRendererBoundingBox)) {
								renderer.setDirty();
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
}
