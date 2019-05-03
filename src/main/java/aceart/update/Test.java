package aceart.update;

import java.util.ArrayList;
import java.util.Collections;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;

public class Test {

	public static void main(String[] args) {
		Test t = new Test();
		BlockPos bl = new BlockPos(1,0,1);
		t.getBox(bl, 5,5,5,5,5,5);
	}

	static public BlockPos lookAtCoordinates() {


		final Minecraft minecraft = Minecraft.getMinecraft();
		//		  WorldClient world = minecraft.world;
		//		  EntityPlayerSP player = minecraft.player;

		RayTraceResult hover = minecraft.objectMouseOver;

		if(hover.typeOfHit == Type.BLOCK) {
			return hover.getBlockPos();

			//			 System.out.println(pos);

			//			 sendMessage(pos);

		}
		return null;

	}

	static void sendMessage(BlockPos pos) {
		Minecraft minecraft = Minecraft.getMinecraft();
		EntityPlayerSP player = minecraft.player;
		player.sendChatMessage("Here's your block - " + pos);


	}
	
	public void buildLoop(String mainCoordinate) {
		
		
	}

	public void getBox(BlockPos centralBlock, int limit1, int limitX2, int limitY1, int limitY2, int limitZ1, int limitZ2) {
		ArrayList box = new ArrayList();
		ArrayList loopContainer = new ArrayList();

		int x = centralBlock.getX();
		int y = centralBlock.getY();
		int z = centralBlock.getZ();

		int currentY = y;

		int loop = 2;

		int lessCubes = 0;
		int mainCor = loop;
		int newX = 0, newZ = 0;
		for(int i = 1; i <= 4; i++) {

			if (i > 2)
				lessCubes = 1;
			
			mainCor = mainCor*(-1);
			for(int secCor = -loop + lessCubes; secCor <= loop - lessCubes; secCor++) {

				switch(i) {
					case 1:
					case 2: newX = mainCor; newZ = secCor; break;
					case 3:
					case 4: newX = secCor; newZ = mainCor; break;
				}
				
				System.out.println(new BlockPos(x+newX, currentY,z+newZ));
				loopContainer.add(new BlockPos(x+newX, currentY, z+newZ));
			}
			System.out.println();
		}
		Collections.shuffle(loopContainer);

		box.addAll(loopContainer);
	}


}

class Looper {
	int coordinate;

}
