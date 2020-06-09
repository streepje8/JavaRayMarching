package com.streep.items;

public class Cube {

	private int x = 0;
	private int y = 0;
	private int z = 0;
	private int width = 0;
	private int height = 0;
	private int depth = 0;
	
	public Cube(int x, int y, int z, int width, int height, int depth) {
		this.setX(x);
		this.setY(y);
		this.setZ(z);
		this.setWidth(width / 2);
		this.setHeight(height / 2);
		this.setDepth(depth / 2);
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	
}
