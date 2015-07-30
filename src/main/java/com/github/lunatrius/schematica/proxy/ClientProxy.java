package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.core.util.vector.Vector3d;
import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.schematica.SchematicPrinter;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.client.renderer.RendererSchematicGlobal;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.handler.client.ChatEventHandler;
import com.github.lunatrius.schematica.handler.client.InputHandler;
import com.github.lunatrius.schematica.handler.client.OverlayHandler;
import com.github.lunatrius.schematica.handler.client.RenderTickHandler;
import com.github.lunatrius.schematica.handler.client.TickHandler;
import com.github.lunatrius.schematica.handler.client.WorldHandler;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.File;
import java.io.IOException;

public class ClientProxy extends CommonProxy {
    public static boolean isRenderingGuide = false;
    public static boolean isPendingReset = false;

    public static final Vector3d playerPosition = new Vector3d();
    public static ForgeDirection orientation = ForgeDirection.UNKNOWN;
    public static int rotationRender = 0;

    public static final Vector3i pointA = new Vector3i();
    public static final Vector3i pointB = new Vector3i();
    public static final Vector3i pointMin = new Vector3i();
    public static final Vector3i pointMax = new Vector3i();

    public static MovingObjectPosition movingObjectPosition = null;

    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();

    private SchematicWorld schematicWorld = null;

    public static void setPlayerData(EntityPlayer player, float partialTicks) {
        playerPosition.x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        playerPosition.y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        playerPosition.z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

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
        if (schematic != null) {
            Vector3i position = schematic.position;
            position.x = (int) Math.floor(playerPosition.x);
            position.y = (int) Math.floor(playerPosition.y) - 1;
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
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        final Property[] sliders = {
                ConfigurationHandler.propAlpha,
                ConfigurationHandler.propBlockDelta,
                ConfigurationHandler.propPlaceDelay,
                ConfigurationHandler.propTimeout,
                ConfigurationHandler.propTooltipX,
                ConfigurationHandler.propTooltipY
        };
        for (Property prop : sliders) {
            prop.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);
        }

        for (KeyBinding keyBinding : InputHandler.KEY_BINDINGS) {
            ClientRegistry.registerKeyBinding(keyBinding);
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        FMLCommonHandler.instance().bus().register(InputHandler.INSTANCE);
        FMLCommonHandler.instance().bus().register(TickHandler.INSTANCE);
        FMLCommonHandler.instance().bus().register(RenderTickHandler.INSTANCE);
        FMLCommonHandler.instance().bus().register(ConfigurationHandler.INSTANCE);

        MinecraftForge.EVENT_BUS.register(RendererSchematicGlobal.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ChatEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new OverlayHandler());
        MinecraftForge.EVENT_BUS.register(new WorldHandler());
    }

    @Override
    public File getDataDirectory() {
        final File file = MINECRAFT.mcDataDir;
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            Reference.logger.debug("Could not canonize path!", e);
        }
        return file;
    }

    @Override
    public void resetSettings() {
        super.resetSettings();

        ChatEventHandler.INSTANCE.chatLines = 0;

        SchematicPrinter.INSTANCE.setEnabled(true);
        SchematicPrinter.INSTANCE.setSchematic(null);

        RendererSchematicGlobal.INSTANCE.destroyRendererSchematicChunks();

        WorldHandler.removeWorldAccess(MINECRAFT.theWorld, getActiveSchematic());
        setActiveSchematic(null);

        playerPosition.set(0, 0, 0);
        orientation = ForgeDirection.UNKNOWN;
        rotationRender = 0;

        pointA.set(0, 0, 0);
        pointB.set(0, 0, 0);
        updatePoints();
    }

    @Override
    public boolean loadSchematic(EntityPlayer player, File directory, String filename) {
        ISchematic schematic = SchematicFormat.readFromFile(directory, filename);
        if (schematic == null) {
            return false;
        }

        SchematicWorld world = new SchematicWorld(schematic);

        Reference.logger.debug("Loaded {} [w:{},h:{},l:{}]", filename, world.getWidth(), world.getHeight(), world.getLength());

        WorldHandler.removeWorldAccess(MINECRAFT.theWorld, getActiveSchematic());
        setActiveSchematic(world);
        WorldHandler.addWorldAccess(MINECRAFT.theWorld, getActiveSchematic());
        RendererSchematicGlobal.INSTANCE.createRendererSchematicChunks(world);
        SchematicPrinter.INSTANCE.setSchematic(world);
        world.isRendering = true;

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

    @Override
    public boolean isPlayerQuotaExceeded(EntityPlayer player) {
        return false;
    }

    @Override
    public File getPlayerSchematicDirectory(EntityPlayer player, boolean privateDirectory) {
        return ConfigurationHandler.schematicDirectory;
    }
}
