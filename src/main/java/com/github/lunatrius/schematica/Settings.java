package com.github.lunatrius.schematica;

import com.github.lunatrius.core.util.vector.Vector3f;
import com.github.lunatrius.schematica.world.SchematicWorld;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.util.ForgeDirection;

@Deprecated
public class Settings {
	@Deprecated
	public static final Settings instance = new Settings();

	@Deprecated
	private final Vector3f translationVector = new Vector3f();
	@Deprecated
	public Minecraft minecraft = Minecraft.getMinecraft();
	@Deprecated
	public Vector3f playerPosition = new Vector3f();
	@Deprecated
	public Vector3f pointA = new Vector3f();
	@Deprecated
	public Vector3f pointB = new Vector3f();
	@Deprecated
	public Vector3f pointMin = new Vector3f();
	@Deprecated
	public Vector3f pointMax = new Vector3f();
	@Deprecated
	public int rotationRender = 0;
	@Deprecated
	public ForgeDirection orientation = ForgeDirection.UNKNOWN;
	@Deprecated
	public Vector3f offset = new Vector3f();
	@Deprecated
	public boolean isRenderingGuide = false;
	@Deprecated
	public int[] increments = {
			1, 5, 15, 50, 250
	};

	@Deprecated
	private Settings() {
	}

	@Deprecated
	public Vector3f getTranslationVector() {
		this.translationVector.set(this.playerPosition).sub(this.offset);
		return this.translationVector;
	}

	@Deprecated
	public float getTranslationX() {
		return this.playerPosition.x - this.offset.x;
	}

	@Deprecated
	public float getTranslationY() {
		return this.playerPosition.y - this.offset.y;
	}

	@Deprecated
	public float getTranslationZ() {
		return this.playerPosition.z - this.offset.z;
	}

	@Deprecated
	public void updatePoints() {
		this.pointMin.x = Math.min(this.pointA.x, this.pointB.x);
		this.pointMin.y = Math.min(this.pointA.y, this.pointB.y);
		this.pointMin.z = Math.min(this.pointA.z, this.pointB.z);

		this.pointMax.x = Math.max(this.pointA.x, this.pointB.x);
		this.pointMax.y = Math.max(this.pointA.y, this.pointB.y);
		this.pointMax.z = Math.max(this.pointA.z, this.pointB.z);
	}

	@Deprecated
	public void moveHere(Vector3f point) {
		point.x = (int) Math.floor(this.playerPosition.x);
		point.y = (int) Math.floor(this.playerPosition.y - 1);
		point.z = (int) Math.floor(this.playerPosition.z);

		switch (this.rotationRender) {
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

	@Deprecated
	public void moveHere() {
		this.offset.x = (int) Math.floor(this.playerPosition.x);
		this.offset.y = (int) Math.floor(this.playerPosition.y) - 1;
		this.offset.z = (int) Math.floor(this.playerPosition.z);

		SchematicWorld schematic = Schematica.proxy.getActiveSchematic();
		if (schematic != null) {
			switch (this.rotationRender) {
			case 0:
				this.offset.x -= schematic.getWidth();
				this.offset.z += 1;
				break;
			case 1:
				this.offset.x -= schematic.getWidth();
				this.offset.z -= schematic.getLength();
				break;
			case 2:
				this.offset.x += 1;
				this.offset.z -= schematic.getLength();
				break;
			case 3:
				this.offset.x += 1;
				this.offset.z += 1;
				break;
			}
		}
	}
}
