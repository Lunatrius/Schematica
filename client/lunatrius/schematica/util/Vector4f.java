package lunatrius.schematica.util;

public class Vector4f {
	public float x, y, z, w;

	public final static Vector4f ZERO = new Vector4f();

	public Vector4f() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
		this.w = 0;
	}

	public Vector4f(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Vector4f add(Vector4f vec) {
		return add(vec.x, vec.y, vec.z, vec.w);
	}

	public Vector4f add(int i) {
		return add(i, i, i, i);
	}

	@SuppressWarnings("hiding")
	public Vector4f add(float x, float y, float z, float w) {
		this.x += x;
		this.y += y;
		this.z += z;
		this.w += w;
		return this;
	}

	public Vector4f sub(Vector4f vec) {
		return sub(vec.x, vec.y, vec.z, vec.w);
	}

	public Vector4f sub(int i) {
		return sub(i, i, i, i);
	}

	@SuppressWarnings("hiding")
	public Vector4f sub(float x, float y, float z, float w) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		this.w -= w;
		return this;
	}

	@Override
	public Vector4f clone() {
		return new Vector4f(this.x, this.y, this.z, this.w);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Vector4f)) {
			return false;
		}

		Vector4f o = (Vector4f) obj;
		return this.x == o.x && this.y == o.y && this.z == o.z && this.w == o.w;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = (int) (71 * hash + this.x);
		hash = (int) (71 * hash + this.y);
		hash = (int) (71 * hash + this.z);
		hash = (int) (71 * hash + this.w);
		return hash;
	}
}
