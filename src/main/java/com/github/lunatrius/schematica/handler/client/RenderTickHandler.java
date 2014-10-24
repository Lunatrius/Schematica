package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.tooltip.TooltipHandler;
import com.github.lunatrius.schematica.world.SchematicWorld;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class RenderTickHandler {
    private final Minecraft minecraft = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        final SchematicWorld schematic = Schematica.proxy.getActiveSchematic();

        ClientProxy.movingObjectPosition = schematic != null ? rayTrace(schematic, 1.0f) : null;

        TooltipHandler.INSTANCE.renderTooltip(schematic, ClientProxy.movingObjectPosition);
    }

    private MovingObjectPosition rayTrace(final SchematicWorld schematic, final float partialTicks) {
        final EntityLivingBase renderViewEntity = this.minecraft.renderViewEntity;
        final double blockReachDistance = this.minecraft.playerController.getBlockReachDistance();

        final double posX = renderViewEntity.posX;
        final double posY = renderViewEntity.posY;
        final double posZ = renderViewEntity.posZ;

        renderViewEntity.posX -= schematic.position.x;
        renderViewEntity.posY -= schematic.position.y;
        renderViewEntity.posZ -= schematic.position.z;

        final Vec3 vecPosition = renderViewEntity.getPosition(partialTicks);
        final Vec3 vecLook = renderViewEntity.getLook(partialTicks);
        final Vec3 vecExtendedLook = vecPosition.addVector(vecLook.xCoord * blockReachDistance, vecLook.yCoord * blockReachDistance, vecLook.zCoord * blockReachDistance);

        renderViewEntity.posX = posX;
        renderViewEntity.posY = posY;
        renderViewEntity.posZ = posZ;

        return schematic.func_147447_a(vecPosition, vecExtendedLook, false, false, true);
    }
}
