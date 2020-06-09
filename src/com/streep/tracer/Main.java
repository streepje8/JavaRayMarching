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
	public static int glowsize = 0;
	public static int shader = 0;
	
	public static float Coefficient(float deltaX, float deltaY, float deltaZ) {
		return (float) (Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2) + Math.pow(deltaZ, 2)));
	}
	
	public static void main(String[] args) {
		float startTime = System.currentTimeMillis();
		JFrame f = new JFrame("3d March");
		cubes.add(new Cube(0,-100,120,100,100,100));
		spheres.add(new Sphere(0,0,120,100));
		f.setSize(800,400);
		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		long lastTime = System.nanoTime();
		final double ticks = 60D;
		double ns = 1000000000 / ticks;    
		double delta = 0;
		while(true){
			iTime = System.currentTimeMillis() - startTime;
			try {
		    long now = System.nanoTime();
		    delta += (now - lastTime) / ns;
		    lastTime = now;
		    if(delta >= 1){
		    	BufferedImage buffer = new BufferedImage(f.getWidth(), f.getHeight(), BufferedImage.TYPE_INT_RGB);
		    	Graphics g = buffer.getGraphics();
		    	g.setColor(new Color(0,0,0));
		    	g.fillRect(0, 0, f.getWidth(), f.getHeight());
		    	
		    	float[] direction = {0f,0f,0f};
		    	float[] ray = {0f, 0f, 0f};
		    	int startZ = 0;
		    	for(int y = -(f.getHeight() / 2); y < (f.getHeight() / 2); y++) {
		    		for(int x = -(f.getWidth() / 2); x < (f.getWidth() / 2); x++) {
			    		float z = startZ;
			    		boolean hit = false;
			    		float dst = 1000;
			    		while(z < 100 && hit == false) {
			    			hit = false;
			    			dst = DstToScene(x, y, z);
			    			int color = 0;
			    			Color c = new Color(color,color,color);
			    			if(dst <= 0.0001f) {
			    				hit = true;
			    				int CR = Math.round(getColor(x,y,z));
			    				c = new Color(0,0,CR);
			    			}
			    			int toset = color;
			    			int whiteness = 255;
			    			if(dst <= glowsize) {
			    				color = 255;
			    				if((color - Math.round(whiteness * (dst / glowsize))) < 255 && (color - Math.round(whiteness * (dst / glowsize))) > 0) {
			    					color -= Math.round(whiteness * (dst / glowsize));
			    					c = new Color(color,color,color);
			    				} else {
			    					color = toset;
			    				}
			    			}
			    			if(shader == 1) {
			    				int ci = 16777215 - c.getRGB();
			    				g.setColor(new Color(ci));
			    			} else {
			    				if(shader == 2) {
			    					int r = c.getRed();
			    					int gr = c.getGreen();
			    					int b = c.getBlue();
			    					int ci = (r + gr + b) / 3;
			    					g.setColor(new Color(ci,ci,ci));
			    				} else {
			    					if(shader == 3) {
			    						g.setColor(c);
			    						g.fillRect(x + (f.getWidth() / 2),(y * -1) + (f.getHeight() / 2), 1, 1);
			    					} else {
			    						if(shader == 4) {
			    							g.setColor(c);
			    							if(y % 2 == 0) {
			    								g.fillRect((int) (((x)) + (f.getWidth() / 2)),((x + y) / 2) + (f.getHeight() / 2), 1, 1);
			    							} else {
			    								g.fillRect((int) (((x + y) / 2) + (f.getWidth() / 2)),((y)) + (f.getHeight() / 2), 1, 1);
			    							}
			    						} else {
			    							if(shader == 5) {
			    								int CR = Math.abs((c.getRed() - c.getGreen()) + c.getBlue());
			    								g.setColor(new Color(CR,CR,CR));
			    							} else {
			    								g.setColor(c);
			    							}
			    						}
			    					}
			    				}
			    			}
			    			if(shader != 3 && shader != 4) {
			    				g.fillRect(x + (f.getWidth() / 2), y + (f.getHeight() / 2), 1, 1);
			    			}
			    			direction[2] = dst;
			    			z += direction[2];
			    		}
		    		}
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
	
	public static float DstToScene(float x, float y, float z) {
		float result = 800f;
		for(Sphere s : spheres) {
			float dstToSphere = signedDstToSphere(x, y, z, s.getX(), s.getY(), s.getZ(), s.getRadius());
			result = Math.min(dstToSphere, result);
		}
		for(Cube c : cubes) {
			float dstToBox = signedDstToCube(x, y, z, c.getX(), c.getY(), c.getZ(), c.getWidth(), c.getHeight(), c.getDepth());
			result = Math.min(dstToBox, result);
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
