package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.core.util.vector.Vector3f;
import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.schematica.SchematicPrinter;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.client.renderer.RendererSchematicGlobal;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.handler.client.ChatEventHandler;
import com.github.lunatrius.schematica.handler.client.KeyInputHandler;
import com.github.lunatrius.schematica.handler.client.TickHandler;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import com.github.lunatrius.schematica.world.schematic.SchematicUtil;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.File;

public class ClientProxy extends CommonProxy {
	// TODO: remove this and replace the 3 sepparate buttons with a single control
	public static final int[] INCREMENTS = {
			1, 5, 15, 50, 250
	};

	public static RendererSchematicGlobal rendererSchematicGlobal = null;
	public static boolean isRenderingGuide = false;
	public static boolean isPendingReset = false;

	public static final Vector3f playerPosition = new Vector3f();
	public static ForgeDirection orientation = ForgeDirection.UNKNOWN;
	public static int rotationRender = 0;

	public static final Vector3i pointA = new Vector3i();
	public static final Vector3i pointB = new Vector3i();
	public static final Vector3i pointMin = new Vector3i();
	public static final Vector3i pointMax = new Vector3i();

	private SchematicWorld schematicWorld = null;

	public static void setPlayerData(EntityPlayer player, float partialTicks) {
		playerPosition.x = (float) (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks);
		playerPosition.y = (float) (player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks);
		playerPosition.z = (float) (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks);

		orientation = getOrientation(player);

		rotationRender = MathHelper.floor_double(player.rotationYaw / 90) & 3;
	}

	private static ForgeDirection getOrientation(EntityPlayer player) {
		if (player.rotationPitch > 45) {
			return ForgeDirection.DOWN;
		} else if (player.rotationPitch < -45) {
			return ForgeDirection.UP;
		} else {
			switch (MathHelper.floor_double(player.rotationYaw / 90.0 + 0.5) & 3) {
			case 0:
				return ForgeDirection.SOUTH;
			case 1:
				return ForgeDirection.WEST;
			case 2:
				return ForgeDirection.NORTH;
			case 3:
				return ForgeDirection.EAST;
			}
		}

		return ForgeDirection.UNKNOWN;
	}

	public static void updatePoints() {
		pointMin.x = Math.min(pointA.x, pointB.x);
		pointMin.y = Math.min(pointA.y, pointB.y);
		pointMin.z = Math.min(pointA.z, pointB.z);

		pointMax.x = Math.max(pointA.x, pointB.x);
		pointMax.y = Math.max(pointA.y, pointB.y);
		pointMax.z = Math.max(pointA.z, pointB.z);
	}

	public static void movePointToPlayer(Vector3i point) {
		point.x = (int) Math.floor(playerPosition.x);
		point.y = (int) Math.floor(playerPosition.y - 1);
		point.z = (int) Math.floor(playerPosition.z);

		switch (rotationRender) {
		case 0:
			point.x -= 1;
			point.z += 1;
			break;
		case 1:
			point.x -= 1;
			point.z -= 1;
			break;
		case 2:
			point.x += 1;
			point.z -= 1;
			break;
		case 3:
			point.x += 1;
			point.z += 1;
			break;
		}
	}

	public static void moveSchematicToPlayer(SchematicWorld schematic) {
		Vector3i position = schematic.position;
		position.x = (int) Math.floor(playerPosition.x);
		position.y = (int) Math.floor(playerPosition.y) - 1;
		position.z = (int) Math.floor(playerPosition.z);

		if (schematic != null) {
			switch (rotationRender) {
			case 0:
				position.x -= schematic.getWidth();
				position.z += 1;
				break;
			case 1:
				position.x -= schematic.getWidth();
				position.z -= schematic.getLength();
				break;
			case 2:
				position.x += 1;
				position.z -= schematic.getLength();
				break;
			case 3:
				position.x += 1;
				position.z += 1;
				break;
			}
		}
	}

	@Override
	public void setConfigEntryClasses() {
		ConfigurationHandler.propAlpha.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);
		ConfigurationHandler.propBlockDelta.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);
		ConfigurationHandler.propPlaceDelay.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);
		ConfigurationHandler.propTimeout.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);
	}

	@Override
	public void registerKeybindings() {
		for (KeyBinding keyBinding : KeyInputHandler.KEY_BINDINGS) {
			ClientRegistry.registerKeyBinding(keyBinding);
		}
	}

	@Override
	public void registerEvents() {
		FMLCommonHandler.instance().bus().register(new KeyInputHandler());
		FMLCommonHandler.instance().bus().register(new TickHandler());
		FMLCommonHandler.instance().bus().register(new ConfigurationHandler());

		rendererSchematicGlobal = new RendererSchematicGlobal();
		MinecraftForge.EVENT_BUS.register(rendererSchematicGlobal);
		MinecraftForge.EVENT_BUS.register(new ChatEventHandler());
	}

	@Override
	public File getDataDirectory() {
		return Minecraft.getMinecraft().mcDataDir;
	}

	@Override
	public void resetSettings() {
		super.resetSettings();

		ChatEventHandler.chatLines = 0;

		SchematicPrinter.INSTANCE.setEnabled(true);
		SchematicPrinter.INSTANCE.setSchematic(null);

		rendererSchematicGlobal.destroyRendererSchematicChunks();

		setActiveSchematic(null);

		playerPosition.set(0, 0, 0);
		orientation = ForgeDirection.UNKNOWN;
		rotationRender = 0;

		pointA.set(0, 0, 0);
		pointB.set(0, 0, 0);
		updatePoints();
	}

	@Override
	public boolean saveSchematic(EntityPlayer player, File directory, String filename, World world, Vector3i from, Vector3i to) {
		try {
			String iconName = "";

			try {
				String[] parts = filename.split(";");
				if (parts.length == 2) {
					iconName = parts[0];
					filename = parts[1];
				}
			} catch (Exception e) {
				Reference.logger.error("Failed to parse icon data!", e);
			}

			SchematicWorld schematic = getSchematicFromWorld(world, from, to);
			schematic.setIcon(SchematicUtil.getIconFromName(iconName));
			SchematicFormat.writeToFile(directory, filename, schematic);

			return true;
		} catch (Exception e) {
			Reference.logger.error("Failed to save schematic!", e);
		}
		return false;
	}

	@Override
	public boolean loadSchematic(EntityPlayer player, File directory, String filename) {
		SchematicWorld schematic = SchematicFormat.readFromFile(directory, filename);
		if (schematic == null) {
			return false;
		}

		Reference.logger.info(String.format("Loaded %s [w:%d,h:%d,l:%d]", filename, schematic.getWidth(), schematic.getHeight(), schematic.getLength()));

		Schematica.proxy.setActiveSchematic(schematic);
		rendererSchematicGlobal.createRendererSchematicChunks(schematic);
		SchematicPrinter.INSTANCE.setSchematic(schematic);
		schematic.isRendering = true;

		return true;
	}

	@Override
	public void setActiveSchematic(SchematicWorld world) {
		this.schematicWorld = world;
	}

	@Override
	public void setActiveSchematic(SchematicWorld world, EntityPlayer player) {
		setActiveSchematic(world);
	}

	@Override
	public SchematicWorld getActiveSchematic() {
		return this.schematicWorld;
	}

	@Override
	public SchematicWorld getActiveSchematic(EntityPlayer player) {
		return getActiveSchematic();
	}
}
