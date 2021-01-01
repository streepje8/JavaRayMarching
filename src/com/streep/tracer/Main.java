package com.streep.tracer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;

import com.streep.items.Cube;
import com.streep.items.Sphere;

public class Main {
	
	public static ArrayList<Cube> cubes = new ArrayList<Cube>();
	public static ArrayList<Sphere> spheres = new ArrayList<Sphere>();
	public static float iTime = 0;
	public static int glowsize = 10;
	public static int shader = 0;
	public static int smoothing = 20;
	public static int processes = 4;
	public static int resolution = 8;
	
	public static float Coefficient(float deltaX, float deltaY, float deltaZ) {
		return (float) (Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2) + Math.pow(deltaZ, 2)));
	}
	
	public static void main(String[] args) {
		float startTime = System.currentTimeMillis();
		JFrame f = new JFrame("3d March");
		if(args.length > 0) {
			try {
				processes = Integer.parseInt(args[0]);
				System.out.println("Using " + processes + " processes");
			} catch(Exception e) {
				System.err.println("Process count must be an integer!");
				System.exit(0);
			}
			if(args.length > 1) {
				try {
					resolution = Integer.parseInt(args[0]);
					System.out.println("Using " + ((1f / (float)(resolution)) * 100f) + "% resolution");
				} catch(Exception e) {
					System.err.println("Resolution count must be an integer!");
					System.exit(0);
				}
			}
			if(args.length > 2) {
				try {
					smoothing = Integer.parseInt(args[0]);
					System.out.println("Using k=" + smoothing + " smoothing");
				} catch(Exception e) {
					System.err.println("Smooth strength must be an integer!");
					System.exit(0);
				}
			}
			if(args.length > 3) {
				try {
					glowsize = Integer.parseInt(args[0]);
					System.out.println("Using a glowsize of " + glowsize);
				} catch(Exception e) {
					System.err.println("Glowsize must be an integer!");
					System.exit(0);
				}
			}
		}
		Sphere s = new Sphere(0,0,120,100);
		cubes.add(new Cube(0,-100,120,100,100,100));
		spheres.add(s);
		f.setSize(800,400);
		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		long lastTime = System.nanoTime();
		final double ticks = 60000D;
		double ns = 1000000000 / ticks;    
		double delta = 0;
		long lastdebugtime = 0;
		int frames = 0;
		boolean switchu = false;
		while(true){
			if((System.currentTimeMillis() - lastdebugtime) >= 1000) {
	    		System.out.println("[FPS] " + frames);
	    		lastdebugtime = System.currentTimeMillis();
	    		frames = 0;
	    	}
			frames++;
			iTime = System.currentTimeMillis() - startTime;
			try {
		    long now = System.nanoTime();
		    delta += (now - lastTime) / ns;
		    lastTime = now;
		    if(delta >= 1){
		    	if(switchu) {
		    		s.setX(s.getX() + 5);
		    		if(s.getX() >= 50) {
		    			switchu = false;
		    		}
		    	} else {
		    		s.setX(s.getX() - 5);
		    		if(s.getX() <= -50) {
		    			switchu = true;
		    		}
		    	}
		    	BufferedImage buffer = new BufferedImage(f.getWidth(), f.getHeight(), BufferedImage.TYPE_INT_RGB);
		    	Graphics g = buffer.getGraphics();
		    	g.setColor(new Color(0,0,0));
		    	g.fillRect(0, 0, f.getWidth(), f.getHeight());
		    	
		    	int startZ = 0;
		    	ArrayList<Renderer> rlist = new ArrayList<Renderer>();
		    	int part = f.getHeight() / processes;
		    	for(int o = -(f.getHeight() / 2); o < (f.getHeight() / 2); o += part) {
	    			Renderer r = new Renderer(startZ, -(f.getWidth() / 2), f.getWidth() / 2, o, o + part, f,0,f.getWidth(),o + (f.getHeight() / 2), o + (f.getHeight() / 2) + part);
    				r.start();
    				rlist.add(r);
				}
		    	for(Renderer r : rlist) {
		    		r.join();
		    		g.drawImage(r.result, r.rsx, r.rsy, null);
		    	}
		    	f.getGraphics().drawImage(buffer, 0, 0, null);
		        delta--;
		    }
			} catch(Exception e) {
				System.out.println("ERROR » " + e.getMessage());
			}
		}
	}
	
	private static float[] add(float[] eye, float dst) {
		float[] res = {0f,0f,0f};
		res[0] = eye[0] + dst;
		res[1] = eye[1] + dst;
		res[2] = eye[2] + dst;
		return res;
	}

	public static float length(float x, float y, float z) {
		return (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
	}
	
	public static float smin( float a, float b, float k )
	{
	    float h = (float) ((float) Math.max(k-Math.abs(a-b), 0.0 )/ k);
	    return (float) ((float) Math.min( a, b ) - h*h*k*(1.0/4.0));
	}
	
	
	public static float DstToScene(float x, float y, float z) {
		float result = 800f;
		for(Sphere s : spheres) {
			float dstToSphere = signedDstToSphere(x, y, z, s.getX(), s.getY(), s.getZ(), s.getRadius());
			result = smin(dstToSphere, result,smoothing);
		}
		for(Cube c : cubes) {
			float dstToBox = signedDstToCube(x, y, z, c.getX(), c.getY(), c.getZ(), c.getWidth(), c.getHeight(), c.getDepth());
			result = smin(dstToBox, result,smoothing);
		}

		//cool effects
		//result = (float) Math.sin(x * y * z);
		//result = (float) Math.cos(result);
		//result = (float) Math.tan(result);
		//result = (float) Math.sin(result);
		return result;
	}

	public static float[] estimateNormal(float x, float y, float z) {
		float[] vec3 = {0,0,0}; //wanna be vector
		vec3[0] = DstToScene((float) (x + Math.E), y, z) - DstToScene((float) (x - Math.E), y, z); //x
		vec3[1] = DstToScene(x,(float)(y + Math.E), z) - DstToScene(x,(float) (y - Math.E), z); //y
		vec3[2] = DstToScene(x,y,(float) (z + Math.E)) - DstToScene(x, y,(float) (z - Math.E)); //z
		return vec3;
	}
	
	public static float signedDstToSphere(float x2, float y2, float z2, int x, int y,int z, int radius) {
		return sdSphere(x2 - x, y2 - y,z2 - z, radius);
	}

	
	private static float signedDstToCube(float x, float y, float z, int x2, int y2, int z2, int width, int height, int depth) {
		float Bx = x2 - x;
		float By = y2 - y;
		float Bz = z2 - z;
		float CheckX = Math.abs(Bx) - width;
		float CheckY = Math.abs(By) - height;
		float CheckZ = Math.abs(Bz) - depth;
		return (float) (length(Math.max(CheckX,0f),Math.max(CheckY, 0f),Math.max(CheckZ, 0f)) + Math.min(Math.max(CheckX,Math.max(CheckY,Math.max(CheckZ, 0f))),0f));
	}
	
	public static float sdSphere(float x, float y, float z, float s)
	{
	  return length(x,y,z)-s;
	}
	
	
	//Lighting
	public static float getColor(float x, float y, float z) {
		float[] normal = estimateNormal(x, y, z);
		
		// You normally need to normalise a normal!
		normal = normalize(normal);
		float[] LightDirection = {0,0,-1f};
		LightDirection = normalize(LightDirection);
		float dotProDuct = Math.max(0.1f, dot(LightDirection, normal));
		return (dotProDuct / 1) * 255;
	}

	public static float[] add(float[] one, float[] two) {
		float[] res = {0,0,0};
		res[0] = one[0] + two[0];
		res[1] = one[1] + two[1];
		res[2] = one[2] + two[2];
		return res;
	}

	public static float[] multiply(float[] one, float[] two) {
		float[] result = {0,0,0};
		result[0] = one[0] * two[0];
		result[1] = one[1] * two[1];
		result[2] = one[2] * two[2];
		return result;
	}
	
	public static float[] multiply(float[] one, float two) {
		float[] result = {0,0,0};
		result[0] = one[0] * two;
		result[1] = one[1] * two;
		result[2] = one[2] * two;
		return result;
	}
	
	public static float[] reflect(float[] negatiefL, float[] n) {
		float[] r = {0,0,0};
		r = subtract(negatiefL,multiply(n,dot(negatiefL,n)*2f));
		return r;
	}

	public static float[] subtract(float[] one, float[] two) {
		float[] res = {0,0,0};
		res[0] = one[0] - two[0];
		res[1] = one[1] - two[1];
		res[2] = one[2] - two[2];
		return res;
	}

	public static float[] negatief(float[] l) {
		float[] result = {0,0,0};
		result[0] = -l[0];
		result[1] = -l[1];
		result[2] = -l[2];
		return result;
	}
	
	public static float length(float[] vec) {
		return (float) Math.sqrt(dot(vec,vec));
	}
	
	public static float[] normalize(float[] vec) {
		float[] result = {0,0,0};
		float l = length(vec);
		result[0] = (vec[0] / l);
		result[1] = (vec[1] / l);
		result[2] = (vec[2] / l);
		return result;
	}
	
	public static float dot(float[] one, float[] two) {
		return (one[0] * two[0]) + (one[1] * two[1]) + (one[2] * two[2]);
	}
}

class Renderer implements Runnable {

	private int startZ; 
	public int startx;
	private int endx;
	public int starty;
	private int endy;
	public int rsx = 0;
	public int rsy = 0;
	public int rex = 0;
	public int rey = 0;
	public BufferedImage result; 
	private Graphics g;
	private JFrame f;
	private Thread t;
	
	public Renderer(int startZ, int startx, int endx, int starty, int endy, JFrame f, int rstartx, int rendx, int rstarty, int rendy) {
		this.startZ = startZ;
		this.startx = startx;
		this.endx = endx;
		this.starty = starty;
		this.endy = endy;
		this.result = new BufferedImage(endx-startx,endy-starty,BufferedImage.TYPE_INT_RGB);
		this.g = this.result.getGraphics();
		this.f = f;
		this.rsx = rstartx;
		this.rsy = rstarty;
		this.rex = rendx;
		this.rey = rendy;
	}
	
	public void join() {
		try {
			this.t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		for(int x = this.startx; x < this.endx; x+= Main.resolution) {
			for(int y = this.starty; y < this.endy; y+= Main.resolution) {
				try {
				float z = this.startZ;
				boolean hit = false;
				float dst = 1000;
				while(z < 100 && hit == false) {
					hit = false;
					dst = Main.DstToScene(x, y, z);
					int color = 0;
					Color c = new Color(color,color,color);
					if(dst <= 0.0001f) {
						hit = true;
						int CR = Math.round(Main.getColor(x,y,z));
						c = new Color(0,0,CR);
					}
					int toset = color;
					int whiteness = 255;
					if(dst <= Main.glowsize) {
						color = 255;
						if((color - Math.round(whiteness * (dst / Main.glowsize))) < 255 && (color - Math.round(whiteness * (dst / Main.glowsize))) > 0) {
							color -= Math.round(whiteness * (dst / Main.glowsize));
							c = new Color(color,color,color);
						} else {
							color = toset;
						}
					}
					if(Main.shader == 1) {
						int ci = 16777215 - c.getRGB();
						g.setColor(new Color(ci));
					} else {
						if(Main.shader == 2) {
							int r = c.getRed();
							int gr = c.getGreen();
							int b = c.getBlue();
							int ci = (r + gr + b) / 3;
							g.setColor(new Color(ci,ci,ci));
						} else {
							if(Main.shader == 3) {
								g.setColor(c);
								g.fillRect(x + (f.getWidth() / 2) - this.startx,(y * -1) + (f.getHeight() / 2) - this.starty, Main.resolution, Main.resolution);
							} else {
								if(Main.shader == 4) {
									g.setColor(c);
									if(y % 2 == 0) {
										g.fillRect((int) (((x)) + (f.getWidth() / 2)) - this.startx,((x + y) / 2) + (f.getHeight() / 2) - this.starty, Main.resolution, Main.resolution);
									} else {
										g.fillRect((int) (((x + y) / 2) + (f.getWidth() / 2)) - this.startx,((y)) + (f.getHeight() / 2) - this.starty, Main.resolution, Main.resolution);
									}
								} else {
									if(Main.shader == 5) {
										int CR = Math.abs((c.getRed() - c.getGreen()) + c.getBlue());
										g.setColor(new Color(CR,CR,CR));
									} else {
										g.setColor(c);
									}
								}
							}
						}
					}
					if(Main.shader != 3 && Main.shader != 4) {
						g.fillRect(x + (f.getWidth() / 2) - rsx,y + (f.getHeight() / 2) - rsy, Main.resolution, Main.resolution);
					}
					z += dst;
				}
				} catch(Exception e) {
					System.out.println(e.getMessage());
					System.exit(0);
				}
			}
		}
	}
	
	public Thread start() {
		Thread t = new Thread(this);
		t.start();
		this.t = t;
		return t;
	}
}
