package fin;

import java.io.BufferedReader;
import java.io.FileReader;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

public class Wheel {
	public static final int DIM = 3;
	public float[] pos = new float[DIM];
	public float[] vel = new float[DIM];
	public float[] acc = new float[DIM];
	public GLModel wheel;
	protected final float diameter;
	
	public Wheel(final GL2 gl, String[] wheelFile) {
		try {
			FileReader reader = new FileReader(wheelFile[0]);
			BufferedReader in = new BufferedReader(reader);
			// Initialize the car model
			wheel = new GLModel(in, true, wheelFile[1], gl);
			reader.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.diameter = wheel.getXLength();
	}
	
	public void updatePosition(int time) {
	}
	
	
	
	public void draw(final GL2 gl) {
		//gl.glDisable(GL2.GL_TEXTURE_2D);
		wheel.opengldraw(gl);
	}
}