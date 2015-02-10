package fin;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

public class Car {
	// General
	private float length;
	private float width;
	private float weight;
	private Edge[] edges;
	private final Boolean user_controlled;

	/*
	 *  Dynamics
	 */
	// Constants
	public static final int DIM = 3;
	private final float coeff_drag = 0.4257f;
	private final float coeff_rr = 12.8f;
	
	// Variables
	private float[] pos = new float[DIM];
	private float[] vel = new float[DIM];
	private float[] acc = new float[DIM];
	private float[] heading = new float[DIM]; 	// Direction vector
	private float dir_angle;
	private float speed = 0;
	private int gear = 0;
	private float force = 0;
	public Wheel[] wheels = new Wheel[4];
	public GLModel model;
	public enum DIR { UP, RIGHT, DOWN, LEFT };
	public int place;
	public int nextCheckpoint;
	public boolean CPupdate;
	public int lapCount;
	public int halflapCount;
	public boolean halfLap;
	private float distTravelled = 0.0f;
	
	
	public Car(final GL2 gl, String[] carFile, String[] wheelFile, float w, float x, float y, Boolean control, int place) {
		// For Model
		FileReader reader;
		BufferedReader in;
		try {
			reader = new FileReader(carFile[0]);
			in = new BufferedReader(reader);
			// Initialize the car model
			model = new GLModel(in, true, carFile[1], gl);
			reader.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int i = 0; i < 4; i++)
			wheels[i] = new Wheel(gl, wheelFile);

		
		this.user_controlled = control;
		
		// For Dynamics
		this.length = model.getXLength();
		this.width = model.getYWidth();
		this.weight = w;
		
		pos[0] = x;
		pos[1] = y;
		pos[2] = 0;
		vel[0] = 0; vel[1] = 0; vel[2] = 0;
		acc[0] = 0; acc[1] = 0; acc[2] = 0;
		heading[0] = 0; heading[1] = 0;
		
		this.nextCheckpoint = 1;
		this.CPupdate = false;
		
		this.lapCount = 1;
		this.halflapCount = 0;
		this.halfLap = false;
	}

	public void updatePosition(float time, float throttle, float angle) {
		this.speed = (float) Math.sqrt((vel[0] * vel[0]) + (vel[1] * vel[1]) + (vel[2] * vel[2]));
		this.dir_angle = angle;
		this.force = throttle;
		if (this.force > 15000) this.force = 15000;
		
		// Calculate longtitudal forces (Drag, Rolling Resist, Traction)
		float[] trac = new float[DIM];
		float[] drag = new float[DIM];
		float[] rr = new float[DIM];
		float[] tot_force = new float[DIM];
		for (int i = 0; i < DIM; i++) {
			if (i == 0) 
				heading[i] = (float) Math.cos(dir_angle / 180 * Math.PI);
			else if (i == 1)
				heading[i] = (float) Math.sin(dir_angle / 180 * Math.PI);
			
			trac[i] = (throttle) * heading[i];
			drag[i] = coeff_drag * vel[i] * speed;
			rr[i] = coeff_rr * vel[i];
			tot_force[i] = trac[i] - drag[i] - rr[i];

			acc[i] = tot_force[i] / weight;
			vel[i] = vel[i] + acc[i] * time;
			pos[i] = pos[i] + vel[i] * time;
		}
	}
	
	public void updateCheckpoint(float CP_x, float CP_y) {
		float radius = 32.0f;
		//If the car is within half the area of the checkpoint, set to the next checkpoint
		if (pos[0] <= (CP_x + radius) && pos[0] >= (CP_x - radius)) {
			if (pos[1] <= (CP_y + radius) && pos[1] >= (CP_y - radius)) {
				 nextCheckpoint++;
				 setCheckpointUpdated(false);
				 if (nextCheckpoint > 28) {
					 nextCheckpoint = 0;
				 }
			}
		}
	}
	
	public int getNextCheckpoint() { return this.nextCheckpoint; }
	public boolean isCheckpointUpdated() { return this.CPupdate; }
	public void setCheckpointUpdated(boolean bool) { this.CPupdate = bool; }
	

	//Lap Detection
	public int getLapCount() { return this.lapCount; }
	
	public int gethalfLapCount() { return this.halflapCount; }
	
	public boolean getHalfLap() {return this.halfLap; }
	
	public boolean checkLapCount(int targetLapCount, float[] finish) {
		
		float radius = 32.0f;
		float finX = finish[0];
		float finY = finish[1];
		
		if (this.halfLap) {  // CHeck if car has made it over half way around
			if (this.pos[0] <= (finX + radius) && this.pos[0] >= (finX - radius) && this.pos[1] >= finY) {
				this.lapCount++;
				this.halfLap = false;
			}
		}

		if (this.lapCount > targetLapCount) { // greater than lapCount is end of game
			return true;
		}
		return false;
	}
	
	public void checkHalfLap(float[] half) {
		
		float radius = 32.0f;
		float halfX = half[0];
		float halfY = half[1];
		
		if (!this.halfLap) {  // Check if car hasn't made it half way around
			if (this.pos[0] <= (halfX + radius) && this.pos[0] >= (halfX - radius) && this.pos[1] <= halfY) {
				this.halflapCount++;
				this.halfLap = true;
			}
		}
	}
	
	public void setDistTravelled(float val) {
		this.distTravelled = val;
	}
	
	public float getDistTravelled() {
		return this.distTravelled;
	}
	
	public Boolean isUser() { return user_controlled; }
	public void setPlace(int p) { this.place = p; }
	public int getPlace() { return this.place; }
	public float getPosX() { return pos[0]; }
	public float getPosY() { return pos[1]; }
	public float getPosZ() { return pos[2]; }
	public float getVelX() { return vel[0]; }
	public float getVelY() { return vel[1]; }
	public float getVelZ() { return vel[2]; }
	public float getAccX() { return acc[0]; }
	public float getAccY() { return acc[1]; }
	public float getSpeed() { return speed; }
	public float getHeadingX() { return heading[0]; }
	public float getHeadingY() { return heading[1]; }
	public float getLength() { return length; }
	public float getForce() { return force; }
	
	public float[] getNE(float angle) {
		float car_angle = (float) Math.atan2(width / 2, length / 2);
		float cut = (float) Math.hypot(length / 2, width / 2);
		return new float[] {
			pos[0] + (cut) * (float) Math.cos((angle / 180 * Math.PI) - car_angle),
			pos[1] + (cut) * (float) Math.sin((angle / 180 * Math.PI) - car_angle)
		};
	}
		
	public float[] getSE(float angle) {
		float car_angle = (float) Math.atan2(width / 2, length / 2);
		float cut = (float) Math.hypot(length / 2, width / 2);
		return new float[] {
			pos[0] + (cut) * (float) Math.cos(((180 + angle) / 180 * Math.PI) + car_angle),
			pos[1] + (cut) * (float) Math.sin(((180 + angle) / 180 * Math.PI) + car_angle)
		};
	}
	
	public float[] getSW(float angle) {
		float car_angle = (float) Math.atan2(width / 2, length / 2);
		float cut = (float) Math.hypot(length / 2, width / 2);
		return new float[] {
			pos[0] + (cut) * (float) Math.cos(((180 + angle) / 180 * Math.PI) - car_angle),
			pos[1] + (cut) * (float) Math.sin(((180 + angle) / 180 * Math.PI) - car_angle)
		};
	}
	
	public float[] getNW(float angle) {
		float car_angle = (float) Math.atan2(width / 2, length / 2);
		float cut = (float) Math.hypot(length / 2, width / 2);
		return new float[] {
			pos[0] + (cut) * (float) Math.cos((angle / 180 * Math.PI) + car_angle),
			pos[1] + (cut) * (float) Math.sin((angle / 180 * Math.PI) + car_angle)
		};
	}
	
	public float[][] getShape() {
		return new float[][] { getSW(dir_angle), getSE(dir_angle), getNE(dir_angle), getNW(dir_angle) };
	}
	
	public Edge[] getEdges() {
		return new Edge[] {
			new Edge(getSW(dir_angle), getSE(dir_angle)),
			new Edge(getSE(dir_angle), getNE(dir_angle)),
			new Edge(getNE(dir_angle), getNW(dir_angle)),
			new Edge(getNW(dir_angle), getSW(dir_angle))
		};
	}
	
	public void setPosX(float x) { pos[0] = x; }
	public void setPosY(float y) { pos[1] = y; }
	
	public void setPos(float x, float y, float z) { pos[0] = x; pos[1] = y; pos[2] = z; }
	public void setVel(float x, float y) { vel[0] = x; vel[1] = y; }
	public void setAcc(float x, float y) { acc[0] = x; acc[1] = y; }
	public void setForce(float f) { force = f; }
	
	public void setHeading(float angle) {
		heading[0] = (float) Math.cos(angle / 180 * Math.PI);
		heading[1] = (float) Math.sin(angle / 180 * Math.PI);
	}
	
	public Projection project(Point2D.Float axis) {
		float min = (axis.x * getShape()[0][0]) + (axis.y * getShape()[0][1]);
		float max = min;
		for (int i = 1; i < getShape().length; i++) {
			float t = (axis.x * getShape()[i][0]) + (axis.y * getShape()[i][1]);
			if (t < min) min = t;
			if (t > max) max = t;
		}
		
		return new Projection(min, max);
	}

	public void draw(final GL2 gl, float time, float turn) {
		gl.glTranslatef(0, 0, model.getZHeight() / 2 - 0.4f);
		gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(180, 0, 1, 0);
		model.opengldraw(gl);
		for (int i = 0; i < 4; i++) {
			gl.glPushMatrix();
			gl.glScalef(2, 2, 2);
			if (i == 0) { 		// right front
				gl.glTranslatef(-1.1f, -0.35f, -0.9f);
				gl.glRotatef(90, 0, 1, 0);
				rotateWheel(gl, i, time, turn);
			}
			else if (i == 1) { 	// left front
				gl.glTranslatef(-1.1f, -0.35f, 0.9f);
				gl.glRotatef(-90, 0, 1, 0);
				rotateWheel(gl, i, time, turn);
			}
			else if (i == 2) { 	// left back
				gl.glTranslatef(1.1f, -0.35f, -0.9f);
				gl.glRotatef(90, 0, 1, 0);
				rotateWheel(gl, i, time, turn);
			}
			else if (i == 3) { 	// right back
				gl.glTranslatef(1.1f, -0.35f, 0.9f);
				gl.glRotatef(-90, 0, 1, 0);
				rotateWheel(gl, i, time, turn);
			}
			wheels[i].draw(gl);
			gl.glPopMatrix();
		}

		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glEnable(GL2.GL_TEXTURE_2D);
	}

	public void rotateWheel(final GL2 gl, int wheel, float rot, float turn) {
		if (wheel == 0) { // left front
			gl.glRotatef(turn, 0, 1, 0); // rotate left/right
			gl.glRotatef(-rot, 1, 0, 0);
		}
		else if (wheel == 1) { // right front
			gl.glRotatef(turn, 0, 1, 0); // rotate left/right
			gl.glRotatef(rot, 1, 0, 0);
		}
		else if (wheel == 2) { // left back
			gl.glRotatef(-rot, 1, 0, 0);
		}
		else if (wheel == 3) { // right back
			gl.glRotatef(rot, 1, 0, 0);
		}	
	}
}