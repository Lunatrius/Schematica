package com.github.lunatrius.schematica.client.printer;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PrinterUtil {

    private static float[] getNeededRotations(Vec3d vec) {
        Vec3d eyesPos = getEyesPos();

        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        return new float[]{
                Minecraft.getMinecraft().player.rotationYaw + MathHelper.wrapDegrees(yaw - Minecraft.getMinecraft().player.rotationYaw),
                Minecraft.getMinecraft().player.rotationPitch + MathHelper.wrapDegrees(pitch - Minecraft.getMinecraft().player.rotationPitch)};
    }

    public static void faceVectorPacketInstant(Vec3d vec) {
        float[] rotations = getNeededRotations(vec);

        Minecraft.getMinecraft().getConnection().sendPacket(new CPacketPlayer.Rotation(rotations[0], rotations[1], Minecraft.getMinecraft().player.onGround));
    }

    public static Vec3d getEyesPos() {
        return new Vec3d(Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posY + Minecraft.getMinecraft().player.getEyeHeight(), Minecraft.getMinecraft().player.posZ);
    }

}
