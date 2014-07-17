package com.github.lunatrius.schematica;

import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.world.SchematicWorld;

@Deprecated
public class Settings {
	@Deprecated
	public static final Settings instance = new Settings();

	@Deprecated
	public Vector3i pointA = new Vector3i();
	@Deprecated
	public Vector3i pointB = new Vector3i();
	@Deprecated
	public Vector3i pointMin = new Vector3i();
	@Deprecated
	public Vector3i pointMax = new Vector3i();
	@Deprecated
	public int[] increments = {
			1, 5, 15, 50, 250
	};

	@Deprecated
	private Settings() {
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
	public void moveHere(Vector3i point) {
		point.x = (int) Math.floor(ClientProxy.playerPosition.x);
		point.y = (int) Math.floor(ClientProxy.playerPosition.y - 1);
		point.z = (int) Math.floor(ClientProxy.playerPosition.z);

		switch (ClientProxy.rotationRender) {
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
	public void moveHere(SchematicWorld schematic) {
		Vector3i position = schematic.position;
		position.x = (int) Math.floor(ClientProxy.playerPosition.x);
		position.y = (int) Math.floor(ClientProxy.playerPosition.y) - 1;
		position.z = (int) Math.floor(ClientProxy.playerPosition.z);

		if (schematic != null) {
			switch (ClientProxy.rotationRender) {
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
}
