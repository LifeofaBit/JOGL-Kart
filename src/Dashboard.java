package fin;

import java.awt.Color;
import java.awt.Font;
import java.util.concurrent.TimeUnit;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

public class Dashboard {
	protected TextureLoader loader = null;
	public static final String PATH_TO_TEXTURES = "/Users/mattdowns/Documents/workspace/fin/src/fin/course_textures/";
//	public static final String PATH_TO_TEXTURES = "c://Users/donovanjustice1/workspace/fin/course_textures/";

	public float orthoX = 1;
	private int[] dash_texture = new int[3];
	private TextRenderer textr = null;

	public Dashboard(final GL2 gl, TextureLoader loader) {
		this.loader = loader;
		loadDashTextures();
		textr = new TextRenderer(new Font("SansSerif", Font.BOLD, 12));
	}
	
	protected void loadDashTextures() {
		dash_texture[0] = loader.generateTexture();
		dash_texture[1] = loader.generateTexture();
		try {
			loader.loadTexture(dash_texture[0], PATH_TO_TEXTURES + "dashboard.png", false);
			loader.loadTexture(dash_texture[1], PATH_TO_TEXTURES + "steeringwheel.png", false);
			loader.loadTexture(dash_texture[2], PATH_TO_TEXTURES + "minimap.png", false);

		} catch ( Exception e ) {
			System.err.println( "Unable to load texture: " + e.getMessage() );
		}
	}
	
	public String getTime(long timeElapsed) {
		String time = String.format("%02d:%02d:%02d",
			    TimeUnit.NANOSECONDS.toMinutes(timeElapsed) - 
			    TimeUnit.HOURS.toMinutes(TimeUnit.NANOSECONDS.toHours(timeElapsed)),
			    TimeUnit.NANOSECONDS.toSeconds(timeElapsed) - 
			    TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(timeElapsed)),
			    TimeUnit.NANOSECONDS.toMillis(timeElapsed) - 
			    TimeUnit.SECONDS.toMillis(TimeUnit.NANOSECONDS.toSeconds(timeElapsed)));
		
		return time;
	}
	
	public void draw(final GL2 gl, final GLUT glut, float vel, float wheelTurn, 
			int place, long timeElapsed, int mode, int lap, Car[] car) {
		
		String time = getTime(timeElapsed);
		
		float ang = -1.5f * vel;
		
		if (mode == 1) {
		
			gl.glBindTexture(gl.GL_TEXTURE_2D, dash_texture[0]);
			gl.glBegin(gl.GL_QUADS);	
			
			gl.glTexCoord2f(1, 1); gl.glVertex3f(1, 1f, -1.7f);
			gl.glTexCoord2f(0, 1); gl.glVertex3f(-1, 1f, -1.7f);
			gl.glTexCoord2f(0, 0); gl.glVertex3f(-1, -1f, -1.7f);
			gl.glTexCoord2f(1, 0); gl.glVertex3f(1, -1f, -1.7f);
			
			gl.glEnd();
			
			//Steering Wheel
			gl.glPushMatrix();
			gl.glTranslatef(0, -0.9f, 0);
			gl.glRotatef(wheelTurn, 0, 0, 1);
			gl.glBindTexture(gl.GL_TEXTURE_2D, dash_texture[1]);
			gl.glBegin(gl.GL_QUADS);	
			gl.glTexCoord2f(1, 1); gl.glVertex3f(0.5f, 0.5f, -1.7f);
			gl.glTexCoord2f(0, 1); gl.glVertex3f(-0.5f, 0.5f, -1.7f);
			gl.glTexCoord2f(0, 0); gl.glVertex3f(-0.5f, -0.5f, -1.7f);
			gl.glTexCoord2f(1, 0); gl.glVertex3f(0.5f, -0.5f, -1.7f);
			gl.glEnd();
			gl.glPopMatrix();
			
			//Minimap
			gl.glPushMatrix();
			gl.glScalef(0.15f, 0.15f, 1);
			gl.glTranslatef(-4.1f, -5.25f, 0);
			gl.glBindTexture(gl.GL_TEXTURE_2D, dash_texture[2]);
			gl.glBegin(gl.GL_QUADS);
			gl.glTexCoord2f(1, 1); gl.glVertex3f(1, 1f, -1.7f);
			gl.glTexCoord2f(0, 1); gl.glVertex3f(-1, 1f, -1.7f);
			gl.glTexCoord2f(0, 0); gl.glVertex3f(-1, -1f, -1.7f);
			gl.glTexCoord2f(1, 0); gl.glVertex3f(1, -1f, -1.7f);
			gl.glEnd();
			
			float offset = 0.025f;
			// draw markers
			gl.glDisable(GL2.GL_TEXTURE_2D);
			gl.glScalef(2, 2, 1);
			gl.glTranslatef(-0.5f, -0.5f, 0);
			
			for (int i = 0; i < 4; i++) {
				gl.glPushMatrix();
				float x = car[i].getPosX() / 800.0f;
				float y = car[i].getPosY() / 800.0f;
				
				if (i == 0) { gl.glColor3f(1, 0, 0); }
				else if (i == 1) { gl.glColor3f(0, 1, 0); }
				else if (i == 2) { gl.glColor3f(0.8f, 0.498039f, 0.196078f); }
				else if (i == 3) { gl.glColor3f(0, 0, 1); }
				
				gl.glBegin(gl.GL_QUADS);
				gl.glVertex3f(x+offset, y+offset, -1.7f);
				gl.glVertex3f(x-offset, y+offset, -1.7f);
				gl.glVertex3f(x-offset, y-offset, -1.7f);
				gl.glVertex3f(x+offset, y-offset, -1.7f);
				gl.glEnd();
				gl.glPopMatrix();
			}
			
			gl.glPopMatrix();
		
			//Speedometer
			gl.glPushMatrix();
			//Rotate speedometer needle
			gl.glTranslatef(0.595f, -0.755f, 0);
			gl.glRotatef(ang, 0, 0, 1); //Rotate needle based on vel
			gl.glRotatef(135, 0, 0, 1); // This is 0 mph
			gl.glTranslatef(-0.595f, 0.755f, 0);
			
			//Move needle to correct position
			gl.glTranslatef(0.595f, -0.755f, 0);
			gl.glScalef(0.0625f, 0.125f, 1);
			gl.glDisable(GL2.GL_TEXTURE_2D);
			gl.glBegin(gl.GL_TRIANGLES);
			gl.glColor3f(1, 0, 0);
			gl.glVertex3f(-0.125f, 0f, -1.7f);
			gl.glVertex3f(0, 0.75f, -1.7f);
			gl.glVertex3f(0.125f, 0, -1.7f);
			gl.glEnd();
			
	
			gl.glEnable(GL2.GL_TEXTURE_2D);
			
			gl.glPushMatrix();
			
			gl.glRotatef(135, 1, 0, 0);
			gl.glPopMatrix();
					
			gl.glPopMatrix();
		
		//Display speed
		textr.setColor(Color.WHITE);
		textr.begin3DRendering();
		textr.draw3D((int) Math.floor(vel)+"", 0.49f, -0.765f, -1.5f, 0.003f);
		textr.end3DRendering();
		}
		
		//Display time
		textr.setColor(Color.WHITE);
		textr.begin3DRendering();
		textr.draw3D(time, 0.49f, 0.765f, -1.5f, 0.005f);
		textr.end3DRendering();
		
		//Display lap
		textr.setColor(Color.WHITE);
		textr.begin3DRendering();
		textr.draw3D("Lap "+lap, -0.49f, 0.765f, -1.5f, 0.005f);
		textr.end3DRendering();
	}
}