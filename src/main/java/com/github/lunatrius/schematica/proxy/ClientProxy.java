package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.core.util.vector.Vector3d;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.client.renderer.RenderSchematic;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.command.client.CommandSchematicaReplace;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.handler.client.GuiHandler;
import com.github.lunatrius.schematica.handler.client.InputHandler;
import com.github.lunatrius.schematica.handler.client.OverlayHandler;
import com.github.lunatrius.schematica.handler.client.RenderTickHandler;
import com.github.lunatrius.schematica.handler.client.TickHandler;
import com.github.lunatrius.schematica.handler.client.WorldHandler;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;

import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.io.IOException;

public class ClientProxy extends CommonProxy {
    public static boolean isRenderingGuide = false;
    public static boolean isPendingReset = false;

    public static final Vector3d playerPosition = new Vector3d();
    public static EnumFacing orientation = null;
    public static int rotationRender = 0;

    public static SchematicWorld schematic = null;

    public static final MBlockPos pointA = new MBlockPos();
    public static final MBlockPos pointB = new MBlockPos();
    public static final MBlockPos pointMin = new MBlockPos();
    public static final MBlockPos pointMax = new MBlockPos();

    public static EnumFacing axisFlip = EnumFacing.UP;
    public static EnumFacing axisRotation = EnumFacing.UP;

    public static RayTraceResult objectMouseOver = null;

    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();

    public static void setPlayerData(final EntityPlayer player, final float partialTicks) {
        playerPosition.x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        playerPosition.y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        playerPosition.z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        orientation = getOrientation(player);

        rotationRender = MathHelper.floor_double(player.rotationYaw / 90) & 3;
    }

    private static EnumFacing getOrientation(final EntityPlayer player) {
        if (player.rotationPitch > 45) {
            return EnumFacing.DOWN;
        } else if (player.rotationPitch < -45) {
            return EnumFacing.UP;
        } else {
            switch (MathHelper.floor_double(player.rotationYaw / 90.0 + 0.5) & 3) {
            case 0:
                return EnumFacing.SOUTH;
            case 1:
                return EnumFacing.WEST;
            case 2:
                return EnumFacing.NORTH;
            case 3:
                return EnumFacing.EAST;
            }
        }

        return null;
    }

    public static void updatePoints() {
        pointMin.x = Math.min(pointA.x, pointB.x);
        pointMin.y = Math.min(pointA.y, pointB.y);
        pointMin.z = Math.min(pointA.z, pointB.z);

        pointMax.x = Math.max(pointA.x, pointB.x);
        pointMax.y = Math.max(pointA.y, pointB.y);
        pointMax.z = Math.max(pointA.z, pointB.z);
    }

    public static void movePointToPlayer(final MBlockPos point) {
        point.x = (int) Math.floor(playerPosition.x);
        point.y = (int) Math.floor(playerPosition.y);
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

    public static void moveSchematicToPlayer(final SchematicWorld schematic) {
        if (schematic != null) {
            final MBlockPos position = schematic.position;
            position.x = (int) Math.floor(playerPosition.x);
            position.y = (int) Math.floor(playerPosition.y);
            position.z = (int) Math.floor(playerPosition.z);

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
    public void preInit(final FMLPreInitializationEvent event) {
        super.preInit(event);

        final Property[] sliders = {
                ConfigurationHandler.propAlpha,
                ConfigurationHandler.propBlockDelta,
                ConfigurationHandler.propRenderDistance,
                ConfigurationHandler.propPlaceDelay,
                ConfigurationHandler.propTimeout,
                ConfigurationHandler.propPlaceDistance
        };
        for (final Property prop : sliders) {
            prop.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);
        }

        for (final KeyBinding keyBinding : InputHandler.KEY_BINDINGS) {
            ClientRegistry.registerKeyBinding(keyBinding);
        }
    }

    @Override
    public void init(final FMLInitializationEvent event) {
        super.init(event);

        MinecraftForge.EVENT_BUS.register(InputHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(TickHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(RenderTickHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ConfigurationHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(RenderSchematic.INSTANCE);
        MinecraftForge.EVENT_BUS.register(GuiHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new OverlayHandler());
        MinecraftForge.EVENT_BUS.register(new WorldHandler());

        ClientCommandHandler.instance.registerCommand(new CommandSchematicaReplace());
    }

    @Override
    public void postInit(final FMLPostInitializationEvent event) {
        super.postInit(event);

        resetSettings();
    }

    @Override
    public File getDataDirectory() {
        final File file = MINECRAFT.mcDataDir;
        try {
            return file.getCanonicalFile();
        } catch (final IOException e) {
            Reference.logger.debug("Could not canonize path!", e);
        }
        return file;
    }

    @Override
    public void resetSettings() {
        super.resetSettings();

        SchematicPrinter.INSTANCE.setEnabled(true);
        unloadSchematic();

        playerPosition.set(0, 0, 0);
        orientation = null;
        rotationRender = 0;

        pointA.set(0, 0, 0);
        pointB.set(0, 0, 0);
        updatePoints();
    }

    @Override
    public void unloadSchematic() {
        schematic = null;
        RenderSchematic.INSTANCE.setWorldAndLoadRenderers(null);
        SchematicPrinter.INSTANCE.setSchematic(null);
    }

    @Override
    public boolean loadSchematic(final EntityPlayer player, final File directory, final String filename) {
        final ISchematic schematic = SchematicFormat.readFromFile(directory, filename);
        if (schematic == null) {
            return false;
        }

        final SchematicWorld world = new SchematicWorld(schematic);

        Reference.logger.debug("Loaded {} [w:{},h:{},l:{}]", filename, world.getWidth(), world.getHeight(), world.getLength());

        ClientProxy.schematic = world;
        RenderSchematic.INSTANCE.setWorldAndLoadRenderers(world);
        SchematicPrinter.INSTANCE.setSchematic(world);
        world.isRendering = true;

        return true;
    }

    @Override
    public boolean isPlayerQuotaExceeded(final EntityPlayer player) {
        return false;
    }

    @Override
    public File getPlayerSchematicDirectory(final EntityPlayer player, final boolean privateDirectory) {
        return ConfigurationHandler.schematicDirectory;
    }
}
