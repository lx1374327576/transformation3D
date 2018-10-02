package test;

import java.io.File;
import java.io.FileNotFoundException;

import com.jogamp.opengl.GL3;

import unsw.graphics.Application3D;
import unsw.graphics.Matrix4;
import unsw.graphics.Shader;
import unsw.graphics.examples.Arrow;
import unsw.graphics.world.LevelIO;
import unsw.graphics.world.Terrain;
import unsw.graphics.world.World;

public class Test1 extends Application3D{
	 private Terrain terrain;

	    public Test1(Terrain terrain) {
	    	super("Assignment 2", 800, 600);
	        this.terrain = terrain;

	    }

	    public static void main(String[] args) throws FileNotFoundException {
	        Terrain terrain = LevelIO.load(new File("res/worlds/test1.json"));
	        Test1 test1 = new Test1(terrain);
	        test1.start();
	    }

		@Override
		public void display(GL3 gl) {
			super.display(gl);
		}

		@Override
		public void destroy(GL3 gl) {
			super.destroy(gl);
		}

		@Override
		public void init(GL3 gl) {
			super.init(gl);

		}

		@Override
		public void reshape(GL3 gl, int width, int height) {
	        super.reshape(gl, width, height);
	        Shader.setProjMatrix(gl, Matrix4.perspective(60, width/(float)height, 1, 100));
		}
}
