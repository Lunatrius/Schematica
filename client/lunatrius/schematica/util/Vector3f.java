package lunatrius.schematica.util;

public class Vector3f {
	public float x, y, z;

	public final static Vector3f ZERO = new Vector3f();

	public Vector3f() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}

	public Vector3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3f add(Vector3f vec) {
		return add(vec.x, vec.y, vec.z);
	}

	public Vector3f add(float i) {
		return add(i, i, i);
	}

	@SuppressWarnings("hiding")
	public Vector3f add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Vector3f sub(Vector3i vec) {
		return sub(vec.x, vec.y, vec.z);
	}

	public Vector3f sub(float i) {
		return sub(i, i, i);
	}

	@SuppressWarnings("hiding")
	public Vector3f sub(float x, float y, float z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}

	@Override
	public Vector3f clone() {
		return new Vector3f(this.x, this.y, this.z);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Vector3f)) {
			return false;
		}

		Vector3f o = (Vector3f) obj;
		return this.x == o.x && this.y == o.y && this.z == o.z;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = (int) (71 * hash + this.x);
		hash = (int) (71 * hash + this.y);
		hash = (int) (71 * hash + this.z);
		return hash;
	}
}
