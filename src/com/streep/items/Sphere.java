package com.streep.items;

public class Sphere {
	private int x = 0;
	private int y = 0;
	private int z = 0;
	private int radius = 0;
	
	public Sphere(int x, int y, int z,int radius) {
		this.setX(x);
		this.setY(y);
		this.setZ(z);
		this.setRadius(radius);
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

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}
	
}
