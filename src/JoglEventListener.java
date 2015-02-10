package fin;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import com.jogamp.opengl.util.gl2.GLUT;

import fin.Car;
import fin.Track.Piece.Building;

import javax.media.opengl.*;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
import javax.swing.JOptionPane;

public class JoglEventListener implements GLEventListener, KeyListener, MouseListener, MouseMotionListener {
	private int windowWidth, windowHeight;

	private TextureLoader texture_loader = null;
	private Skybox current_skybox = null;
	private final float skybox_size = 1000.0f;
	private final String[] skybox_names = {
			"ThickCloudsWater", "DarkStormy",
			"FullMoon", "SunSet",
			"CloudyLightRays", "TropicalSunnyDay"
	};
	// Making this larger will allocate more skybox textures to start, giving a
	// super slow startup, but allowing you to switch between them quickly.
	// Best to use a value of 1 for production code.
	private Skybox[] skyboxes = new Skybox[ skybox_names.length ];
	private TGABuffer buffer;
	private int[] tree_tex = new int[1];

	// Course variables
	private Track course = null;
	private final int COURSE_SIZE = 800;
	private Boolean pause = true;
	private float time = 0.01f;
	private final int targetLapCount = 3;

	// Car variables
	private final int NUM_CARS = 4;
	private Car[] car = new Car[NUM_CARS];
	private float angle = 90;
	private float wheel_rot = 0;
	private float wheel_turn = 0;
	private float[] AI_angle = {90, 90, 90};
	int randAng[] = {0, 0, 0, 0};
	float randDist[] = {0, 0, 0, 0};
	float randX[] = {0, 0, 0, 0};
	float randY[] = {0, 0, 0, 0};
	private float AI_force[] = { 0, 0, 0 };
	private float AI_wheelturn[] = { 0, 0, 0 };
	private float AI_wheelrot[] = { 1, 1, 1 };
	
	private boolean lapSwitch = false;
	private boolean halfLapSwitch = false;
	
	// Dashboard
	private Dashboard dash = null;

	// Time variables
	long initTime;
	long stopTime;
	long timeElapsed;

	// Camera variables
	private float scene_eye_x;
	private float scene_eye_y;
	private float scene_eye_z;
	private float scene_look_x;
	private float scene_look_y;
	private float scene_look_z;
	private float offset = 8.0f;
	private int mode = 0;

	private int mouse_x0 = 0;
	private int mouse_y0 = 0;

	private int mouse_mode = 0;

	private final int MOUSE_MODE_NONE = 0;
	private final int MOUSE_MODE_ROTATE = 1;

	private boolean[] keys = new boolean[256];

	private GLU glu = new GLU();
	private final String MODEL_PATH = "/Users/mattdowns/Documents/workspace/fin/src/fin/models/";
//	private final String MODEL_PATH = "c://Users/donovanjustice1/workspace/fin/models/kart/";

	private final String[] CAR_COLORS = {"kartRed.mtl", "kartGreen.mtl", "kartYellow.mtl", "kartBlue.mtl"};

	private final String[] CAR_MODEL = {MODEL_PATH+"kart.obj", MODEL_PATH+CAR_COLORS[0]};
	private final String[] CAR_WHEEL = {MODEL_PATH+"kartWheel.obj", MODEL_PATH+"kartWheel.mtl"};
	
	public void displayChanged( GLAutoDrawable gLDrawable, boolean modeChanged,
			boolean deviceChanged) { }

	@Override
	public void init( GLAutoDrawable gLDrawable ) {
		GL2 gl = gLDrawable.getGL().getGL2();
		gl.glClearColor( 0.0f, 0.0f, 0.0f, 1.0f );
		gl.glColor3f( 1.0f, 1.0f, 1.0f );
		gl.glClearDepth( 1.0f );
		gl.glEnable( GL.GL_DEPTH_TEST );
		gl.glDepthFunc( GL.GL_LEQUAL );
		gl.glEnable( GL.GL_TEXTURE_2D );

		//Initialize the timer
		initTime = 0;
		stopTime = 0;
		timeElapsed = 0;

		// Initialize the texture loader and skybox.
		texture_loader = new TextureLoader( gl );
			
		skyboxes[ 0 ] = new Skybox( texture_loader, skybox_names[ 5 ] );
		current_skybox = skyboxes[ 0 ];

		// Initialize course and cars
		course = new Track(texture_loader, COURSE_SIZE);
		try {
			buffer = TGABufferMaker.make();
			gl.glEnable(GL.GL_DEPTH_TEST);
	        gl.glGenTextures(1, tree_tex, 0);
			gl.glBindTexture(GL.GL_TEXTURE_2D, tree_tex[0]);
	        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
	        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

	        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
	        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
	    	
	        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, buffer.getWidth(), buffer.getHeight(), 
	    				0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer.getBuffer());
			
		} catch (Exception e) {
			System.err.println("Unable to load tree billboard");
		}
		
		car[0] = new Car(gl, CAR_MODEL, CAR_WHEEL, 5, 72, 444, true, 4);

		//Initialize the opponent cars
		for (int i = 1; i < NUM_CARS; i++) {
			String[] carModel = {CAR_MODEL[0], MODEL_PATH+CAR_COLORS[i]};
			if (i == 1)
				car[i] = new Car(gl, carModel, CAR_WHEEL, 20, 52.25f, 458, false, 3);
			if (i == 2)
				car[i] = new Car(gl, carModel, CAR_WHEEL, 15, 72, 480, false, 2);
			if (i == 3)
				car[i] = new Car(gl, carModel, CAR_WHEEL, 12, 52.25f, 498, false, 1);
		}

		// Initialize the keys.
		for ( int i = 0; i < keys.length; ++i )
			keys[i] = false;

		// Initialize the Dashboard
		dash = new Dashboard(gl, texture_loader);

		gl.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );
		gl.glLoadIdentity();
		
		this.scene_eye_x = car[0].getPosX();
		this.scene_eye_y = car[0].getPosY() - 10;
		this.scene_eye_z = car[0].getPosZ() + 5;
		this.scene_look_x = car[0].getPosX();
		this.scene_look_y = car[0].getPosY() + 2;
		this.scene_look_z = 0;
		stopTime = System.nanoTime();
		
		course.calculuateTrackLength();
	}

	@Override
	public void reshape( GLAutoDrawable gLDrawable, int x, int y, int width, int height ) {
		windowWidth = width;
		windowHeight = height > 0 ? height : 1;

		final GL2 gl = gLDrawable.getGL().getGL2();

		gl.glViewport( 0, 0, width, height );
		gl.glMatrixMode( GLMatrixFunc.GL_PROJECTION );
		gl.glLoadIdentity();
		glu.gluPerspective( 60.0f, (float) windowWidth / windowHeight, 0.1f, skybox_size * (float) Math.sqrt( 3.0 ) / 2.0f );
	}

	@Override
	public void display( GLAutoDrawable gLDrawable ) {
		final GL2 gl = gLDrawable.getGL().getGL2();
		final GLUT glut = new GLUT();

		gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );

		gl.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );
		gl.glPushMatrix();

		final float throttle_pan = 0.25f;

		if (pause) {
			

			// Update the camera state.
			if ( keys[KeyEvent.VK_W] || keys[KeyEvent.VK_S] ) {
				float normxy = (float) Math.sqrt( scene_look_x * scene_look_x + scene_look_y * scene_look_y );
				float multiplier = keys[KeyEvent.VK_W] ? 1.0f : -1.0f;
				scene_eye_x += scene_look_x / normxy * throttle_pan * multiplier;
				scene_eye_y += scene_look_y / normxy * throttle_pan * multiplier;
			}

			if ( keys[KeyEvent.VK_R] ) {
				scene_eye_z += throttle_pan;
				if (scene_eye_z > 50) scene_eye_z = 50;
			} else if ( keys[KeyEvent.VK_F] ) {
				scene_eye_z -= throttle_pan;
				if (scene_eye_z < 0.1) scene_eye_z = 0.1f;
			}

			if ( keys[KeyEvent.VK_A] || keys[KeyEvent.VK_D] ) {
				float theta = (float) Math.atan2( scene_look_y, scene_look_x );
				float phi = (float) Math.acos( scene_look_z );

				if ( keys[KeyEvent.VK_A] )
					theta += Math.PI / 2.0;
				else if ( keys[KeyEvent.VK_D] )
					theta -= Math.PI / 2.0;

				float strafe_x = (float)( Math.cos( theta ) * Math.sin( phi ) );
				float strafe_y = (float)( Math.sin( theta ) * Math.sin( phi ) );
				float normxy = (float) Math.sqrt( strafe_x * strafe_x + strafe_y * strafe_y );

				scene_eye_x += strafe_x / normxy * throttle_pan;
				scene_eye_y += strafe_y / normxy * throttle_pan;
			}

			glu.gluLookAt( scene_eye_x, scene_eye_y, scene_eye_z,
					scene_eye_x + scene_look_x, scene_eye_y + scene_look_y, scene_eye_z + scene_look_z,
					0.0f, 0.0f, 1.0f );


		} else {
			initTime = System.nanoTime();
			timeElapsed = (initTime-stopTime);

			switch (mode) {
			case 0:	// Third Person
				scene_eye_x = car[0].getPosX() - offset * (float) Math.cos(angle / 180.0 * Math.PI);  
				scene_eye_y = car[0].getPosY() - offset * (float) Math.sin(angle / 180.0 * Math.PI);
				scene_eye_z = 3; // + offset
				scene_look_x = car[0].getPosX() + offset * (float) Math.cos(angle / 180.0 * Math.PI);
				scene_look_y = car[0].getPosY() + offset * (float) Math.sin(angle / 180.0 * Math.PI);
				scene_look_z = 0; // offset - 1
				break;
			case 1:	// First Person
				scene_eye_x = car[0].getPosX() + 3 * (float) Math.cos(angle / 180.0 * Math.PI);  
				scene_eye_y = car[0].getPosY() + 3 * (float) Math.sin(angle / 180.0 * Math.PI);
				scene_eye_z = 4; // + offset
				scene_look_x = car[0].getPosX() + offset * (float) Math.cos(angle / 180.0 * Math.PI);
				scene_look_y = car[0].getPosY() + offset * (float) Math.sin(angle / 180.0 * Math.PI);
				scene_look_z = 4; // offset - 1
				break;
			case 2: // Tire Look (Front)
				scene_eye_x = car[0].getPosX() + 10;  
				scene_eye_y = car[0].getPosY();
				scene_eye_z = 3; // + offset
				scene_look_x = car[0].getPosX() - (float) Math.cos(angle / 180.0 * Math.PI);
				scene_look_y = car[0].getPosY() - (float) Math.sin(angle / 180.0 * Math.PI);
				scene_look_z = 0; // offset - 1
				break;
			}

			glu.gluLookAt( scene_eye_x, scene_eye_y, scene_eye_z,
					scene_look_x, scene_look_y, scene_look_z,
					0.0f, 0.0f, 1.0f );
		}

		// Draws the course according to current car position
		course.draw(gl, car[0].getPosX(), car[0].getPosY());
		
		// Draw Billboard Trees
		drawAllTrees(gl);

		// Draws the city background along the course boundary
		gl.glPushMatrix();
		gl.glTranslatef(0, 0, -12);
		course.drawBG(gl);
		gl.glPopMatrix();

		// Draws AI cars
		for (int i = 1; i < NUM_CARS; i++) {
			gl.glPushMatrix();
			gl.glDisable(GL.GL_TEXTURE_2D);
			gl.glTranslatef(car[i].getPosX(), car[i].getPosY(), 0);
			gl.glRotatef(AI_angle[i-1], 0, 0, 1);
			car[i].draw(gl, AI_wheelrot[i - 1], AI_wheelturn[i - 1]);
			gl.glEnable(GL.GL_TEXTURE_2D);
			gl.glPopMatrix();
		}

		// Draw the user car
		gl.glTranslatef(car[0].getPosX(), car[0].getPosY(), 0);
		gl.glPushMatrix();
		gl.glRotatef(angle, 0, 0, 1);
		gl.glDisable(GL.GL_TEXTURE_2D);
		car[0].draw(gl, wheel_rot, wheel_turn);
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glPopMatrix();

		current_skybox.draw( gl, skybox_size );	
		gl.glPopMatrix();

		// Car Input
		if (!pause) {
			// Update cars
			if (keys[KeyEvent.VK_W] && !keys[KeyEvent.VK_S]) {
				car[0].setForce(car[0].getForce() + 10);
				if (offset > 6) offset -= 0.1f;
				else offset = 6;
			} else {
				if (keys[KeyEvent.VK_S]) {
					if (car[0].getForce() <= 0.5) car[0].setForce(car[0].getForce() - 5);
					else car[0].setForce(car[0].getForce() * 0.95f);
				} else {
					if (car[0].getForce() > 0) car[0].setForce(car[0].getForce() - 10);
					else car[0].setForce(0);

					if (offset < 9) offset += 0.1f;
					else offset = 9;
				}
			}
			

			if (keys[KeyEvent.VK_A]) {
				angle++;
				if (wheel_turn <= 15) wheel_turn += 5;
				else wheel_turn = 15;
			}
			if (keys[KeyEvent.VK_D]) {
				angle--;
				if (wheel_turn >= -15) wheel_turn -= 5;
				else wheel_turn = -15;
			}
			if (!keys[KeyEvent.VK_A] && !keys[KeyEvent.VK_D]) 
				wheel_turn /= 5;


			if (car[0].getSpeed() <= 0) wheel_rot = 0;
			else wheel_rot += car[0].getSpeed();

			for (int i = 0; i < NUM_CARS; i++) {
				if (car[i].isUser()) {
					float[] nextCP = course.getCheckpoints(car[i].getNextCheckpoint());
					if (!car[i].isCheckpointUpdated()) {
						car[i].setCheckpointUpdated(true);
					}
					car[i].updatePosition(time, car[0].getForce(), angle);
					car[i].updateCheckpoint(nextCP[0], nextCP[1]);
					
					int prehalflapCheck = car[i].gethalfLapCount();
					car[i].checkHalfLap(course.getCheckpoints(12)); //Index 12: Half Point 
					int posthalflapCheck = car[i].gethalfLapCount();
					
					if (posthalflapCheck > prehalflapCheck) {
						halfLapSwitch = true;
					} else {
						halfLapSwitch = false;
					}
					
					int prelapCheck = car[i].getLapCount();
					car[i].checkLapCount(targetLapCount, course.getCheckpoints(0));
					int postlapCheck = car[i].getLapCount();
					
					if (postlapCheck > prelapCheck) {
						lapSwitch = true;
					} else {
						lapSwitch = false;
					}
					
					boundaryCollision(car[i], angle);
					
					//Update place if car passes half or full lap
					if (lapSwitch) {
						//placeUpdate();
						lapSwitch = false;
					} else if (halfLapSwitch) {
						//placeUpdate();
						halfLapSwitch = false;
					}
					
					
				} else {
					float[] nextCP = course.getCheckpoints(car[i].getNextCheckpoint());
					// Chooses random point within circle of current checkpoint with radius 16 (1/4 of width of track)

					if (!car[i].isCheckpointUpdated()) {
						randAng[i] = (int) Math.random()*360;
						randDist[i] = (float) (Math.random() * 32);
						randX[i] = (float) (randDist[i] * Math.cos(randAng[i])) + nextCP[0];
						randY[i] = (float) (randDist[i] * Math.sin(randAng[i])) + nextCP[1];
						car[i].setCheckpointUpdated(true);
					}
					float x = randX[i] - car[i].getPosX();
					float y = randY[i] - car[i].getPosY();
					
					float prev_angle = AI_angle[i - 1];
					AI_angle[i-1] = (float) (Math.atan2(y, x) * 180 / Math.PI);
					if (Math.abs(AI_angle[i - 1] - prev_angle) <= 0.1)
						AI_force[i - 1] += 12;
					else
						AI_force[i - 1] -= 20;
					
					car[i].updatePosition(time, AI_force[i - 1], AI_angle[i-1]);
					car[i].updateCheckpoint(nextCP[0], nextCP[1]);
					if (car[i].getSpeed() <= 0) AI_wheelrot[i - 1] = 0;
					else AI_wheelrot[i - 1] += car[i].getSpeed();
					
					car[i].checkHalfLap(course.getCheckpoints(12)); //Index 12: Half Point
					car[i].checkLapCount(targetLapCount, course.getCheckpoints(0));
					boundaryCollision(car[i], AI_angle[i-1]);
				}
				
				// Check if this car has won
				if (car[i].checkLapCount(targetLapCount, course.getCheckpoints(0))) {
				if (car[i].isUser()) {
					JOptionPane.showMessageDialog(null, "You Win!\nYour time: " + dash.getTime(timeElapsed));
				} else {
					JOptionPane.showMessageDialog(null, "You Lose! :(");
				}
				pause = true;
				}
			}
			
			//Calculate distance travelled
			for (int i = 0; i < NUM_CARS; i++) {
				car[i].setDistTravelled(getCarProgress(car[i]));
			}
			
			// Check for collision with buildings
			for (int c = 0; c < NUM_CARS; c++) {
				// For some reason, if one car crashes, it affects all cars. Not ideal
				for (int i = 0; i < course.getCell(car[c].getPosX(), car[c].getPosY()).bldgs.length; i++) {
					Building bldg = course.getCell(car[c].getPosX(), car[c].getPosY()).getBuilding(i);
					MTV mtv = SAT(car[c], bldg);
					if (mtv.result) {
						resetCar(car[c], mtv, true);
					}
				}
			}
			
			// Check for collision with other cars
			for (int i = 0; i < NUM_CARS; i++) {
				for (int j = i + 1; j < NUM_CARS; j++) {
					MTV mtv = SAT(car[i], car[j]);
					MTV opp = SAT(car[j], car[i]);
					if (mtv.result) {
						resetCar(car[i], mtv, false);
						resetCar(car[j], opp, false);
					}
				}
			}
		}

		// Pop!
		gl.glPopMatrix();
		
		// Draw Dashboard
		dash.draw(gl, glut, car[0].getSpeed(), wheel_turn, car[0].getPlace(), timeElapsed, mode, car[0].getLapCount(), car);
	}
	
	public float getCarProgress(Car car) {
		float carX = car.getPosX();
		float carY = car.getPosY();
		
		int prevCP;
		float prevCP_X; float prevCP_Y;
		float nearCP_X; float nearCP_Y;
		float D; float Dx; float Dy;
		float slope;
		float car_dx; float car_dy;
		float car_hyp;
		float car_d;

		int nearCP = course.findNearestCheckpoint(carX, carY);
		float segLength = course.TrackSegmentLength(nearCP);
		
		if (nearCP == 0) {
			prevCP = 28;
		} else {
			prevCP = nearCP - 1;
		}
		
		prevCP_X = course.getCheckpoints(prevCP)[0];
		prevCP_Y = course.getCheckpoints(prevCP)[1];
		nearCP_X = course.getCheckpoints(nearCP)[0];
		nearCP_Y = course.getCheckpoints(nearCP)[1];
		
		Dx = (nearCP_X - prevCP_X);
		Dy = (nearCP_Y - prevCP_Y);
		
		D = (float) Math.sqrt((Dx * Dx) + (Dy * Dy));
		slope = Dy / Dx;
		
		car_dx = (carX - prevCP_X);
		car_dy = (carY - prevCP_Y);
		
		car_hyp = (float) Math.sqrt((car_dx * car_dx) + (car_dy * car_dy));
		car_d = Math.abs((float) (car_hyp * Math.cos(Math.atan(slope))));
		
		segLength += car_d;
		
		return segLength / course.getTrackLength();
	}
	
	public void placeUpdate() {
		for (int i = 0; i < NUM_CARS - 1; i++) {
			for (int j = 1; j < NUM_CARS; j++) {
				if (car[i].getLapCount() == car[j].getLapCount()) { // On same lap
					if(!car[i].getHalfLap()) { // car1 on first half of lap
						if (!car[j].getHalfLap()) { // car2 on first half of lap
							if (car[i].getPosY() < car[j].getPosY()) {
								if (car[i].getPlace() > car[j].getPlace()) {
									car[i].setPlace(car[i].getPlace()-1);
									car[j].setPlace(car[j].getPlace()+1);
								} else {
									car[i].setPlace(car[i].getPlace()+1);
									car[j].setPlace(car[j].getPlace()-1);
								}
							} else {
								if (car[i].getPlace() < car[j].getPlace()) {
									car[j].setPlace(car[j].getPlace()-1);
									car[i].setPlace(car[i].getPlace()+1);
								} else {
									car[j].setPlace(car[j].getPlace()+1);
									car[i].setPlace(car[i].getPlace()-1);
								}
								
							}
						} else { // car2 is on 2nd half of lap (ahead of car1)
							if (car[i].getPlace() < car[j].getPlace()) {
								car[j].setPlace(car[j].getPlace()-1);
								car[i].setPlace(car[i].getPlace()+1);
							} else {
								car[j].setPlace(car[j].getPlace()+1);
								car[i].setPlace(car[i].getPlace()-1);
							}
						}
					} else { // car1 is on 2nd half of lap
						if (!car[j].getHalfLap()) { // car2 on first half of lap
							if (car[i].getPlace() > car[j].getPlace()) {
								car[j].setPlace(car[j].getPlace()+1);
								car[i].setPlace(car[i].getPlace()-1);
							} else {
								car[j].setPlace(car[j].getPlace()-1);
								car[i].setPlace(car[i].getPlace()+1);
							}
						} else { // car2 is on 2nd half of lap
							if (car[i].getPosY() < car[j].getPosY()) {
								if (car[i].getPlace() < car[j].getPlace()) {
									car[i].setPlace(car[i].getPlace()-1);
									car[j].setPlace(car[j].getPlace()+1);
								} else {
									car[i].setPlace(car[i].getPlace()+1);
									car[j].setPlace(car[j].getPlace()-1);
								}
							} else {
								if (car[i].getPlace() < car[j].getPlace()) {
									car[j].setPlace(car[j].getPlace()-1);
									car[i].setPlace(car[i].getPlace()+1);
								} else {
									car[j].setPlace(car[j].getPlace()+1);
									car[i].setPlace(car[i].getPlace()-1);
								}
							}
						}
					}
				} 
			}
		}
	}
	
	/**
	 * Collision Detection
	 */
	public MTV SAT(Car car, Building bldg) {
		float overlap = 10000;
		Point2D.Float smallest = new Point2D.Float();
		Point2D.Float[] car_axes = getAxes(car);
		Point2D.Float[] bldg_axes = getAxes(bldg);
		
		for (int i = 0; i < car_axes.length; i++) {
			Point2D.Float axis = car_axes[i];
			Projection p1 = car.project(axis);
			Projection p2 = bldg.project(axis);

			if (!p1.overlap(p2))
				return new MTV(false, null, 0);
			else {
				float o = p1.getOverlap(p2);
				if (o < overlap) {
					overlap = o;
					smallest = axis;
				}
			}
		}

		for (int i = 0; i < bldg_axes.length; i++) {
			Point2D.Float axis = bldg_axes[i];
			Projection p1 = car.project(axis);
			Projection p2 = bldg.project(axis);

			if (!p1.overlap(p2))
				return new MTV(false, null, 0);
			else {
				float o = p1.getOverlap(p2);
				if (o < overlap) {
					overlap = o;
					smallest = axis;
				}
			}
		}

		return new MTV(true, smallest, overlap);
	}

	public MTV SAT(Car car, Car car2) {
		float overlap = 10000;
		Point2D.Float smallest = new Point2D.Float();

		Point2D.Float[] car_axes = getAxes(car);
		Point2D.Float[] bldg_axes = getAxes(car2);
		for (int i = 0; i < car_axes.length; i++) {
			Point2D.Float axis = car_axes[i];
			Projection p1 = car.project(axis);
			Projection p2 = car2.project(axis);

			if (!p1.overlap(p2))
				return new MTV(false, null, 0);
			else {
				float o = p1.getOverlap(p2);
				if (o < overlap) {
					overlap = o;
					smallest = axis;
				}
			}
		}

		for (int i = 0; i < bldg_axes.length; i++) {
			Point2D.Float axis = bldg_axes[i];
			Projection p1 = car.project(axis);
			Projection p2 = car2.project(axis);

			if (!p1.overlap(p2))
				return new MTV(false, null, 0);
			else {
				float o = p1.getOverlap(p2);
				if (o < overlap) {
					overlap = o;
					smallest = axis;
				}
			}
		}

		return new MTV(true, smallest, overlap);
	}

	void resetCar(Car car, MTV mtv, Boolean immovable) {
		car.setPosX(car.getPosX() - (mtv.coords.x * (mtv.mag / 2)));
		car.setPosY(car.getPosY() - (mtv.coords.y * (mtv.mag / 2)));
		
		// Dampen Speed
		float[] dampen = { mtv.coords.x * car.getHeadingX(), mtv.coords.y * car.getHeadingY() };
		for (int i = 0; i < 2; i++)
			dampen[i] = 1 - Math.abs(dampen[i]);
		
		if (immovable)
			car.setForce(car.getForce() * Math.min(dampen[0], dampen[1]));
		else
			car.setForce(car.getForce() * 0.98f);
		
		car.setAcc(dampen[0] * car.getAccX(), dampen[1] * car.getAccY());
		car.setVel(dampen[0] * car.getVelX(), dampen[1] * car.getVelY());
	}

	Point2D.Float[] getAxes(Car car) {
		Point2D.Float[] axes = new Point2D.Float[4];
		for (int i = 0; i < car.getEdges().length; i++) {
			Edge edge = car.getEdges()[i];

			float dx = edge.x2 - edge.x1;
			float dy = edge.y2 - edge.y1;

			float x = (dx) / (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
			float y = (dy) / (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
			Point2D.Float axis = new Point2D.Float(-x, y);
			axes[i] = axis;
		}

		return axes;
	}

	Point2D.Float[] getAxes(Building bldg) {
		Point2D.Float[] axes = new Point2D.Float[4];
		for (int i = 0; i < bldg.getEdges().length; i++) {
			Edge edge = bldg.getEdges()[i];

			float dx = edge.x2 - edge.x1;
			float dy = edge.y2 - edge.y1;

			float x = (dx) / (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
			float y = (dy) / (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
			Point2D.Float axis = new Point2D.Float(-x, y);
			axes[i] = axis;
		}

		return axes;
	}

	void boundaryCollision(Car car, float angle) {
		// Corner based	
		for (int i = 0; i < 4; i++) {
			switch (i) {
			case 0:
				float coordNE[] = car.getNE(angle);
				if (coordNE[0] >= COURSE_SIZE)
					car.setPosX(car.getPosX() - (coordNE[0] - COURSE_SIZE));
				if (coordNE[0] <= 0)
					car.setPosX(car.getPosX() + (0 - coordNE[0]));
				if (coordNE[1] >= COURSE_SIZE)
					car.setPosY(car.getPosY() - (coordNE[1] - COURSE_SIZE));
				if (coordNE[1] <= 0 )
					car.setPosY(car.getPosY() + (0 - coordNE[1]));
				break;

			case 1:
				float coordNW[] = car.getNW(angle);
				if (coordNW[0] >= COURSE_SIZE)
					car.setPosX(car.getPosX() - (coordNW[0] - COURSE_SIZE));
				if (coordNW[0] <= 0)
					car.setPosX(car.getPosX() + (0 - coordNW[0]));
				if (coordNW[1] >= COURSE_SIZE)
					car.setPosY(car.getPosY() - (coordNW[1] - COURSE_SIZE));
				if (coordNW[1] <= 0 )
					car.setPosY(car.getPosY() + (0 - coordNW[1]));
				break;
				
			case 2:
				float coordSW[] = car.getSW(angle);
				if (coordSW[0] >= COURSE_SIZE)
					car.setPosX(car.getPosX() - (coordSW[0] - COURSE_SIZE));
				if (coordSW[0] <= 0)
					car.setPosX(car.getPosX() + (0 - coordSW[0]));
				if (coordSW[1] >= COURSE_SIZE)
					car.setPosY(car.getPosY() - (coordSW[1] - COURSE_SIZE));
				if (coordSW[1] <= 0 )
					car.setPosY(car.getPosY() + (0 - coordSW[1]));
				break;
				
			case 3:
				float coordSE[] = car.getSE(angle);
				if (coordSE[0] >= COURSE_SIZE)
					car.setPosX(car.getPosX() - (coordSE[0] - COURSE_SIZE));
				if (coordSE[0] <= 0)
					car.setPosX(car.getPosX() + (0 - coordSE[0]));
				if (coordSE[1] >= COURSE_SIZE)
					car.setPosY(car.getPosY() - (coordSE[1] - COURSE_SIZE));
				if (coordSE[1] <= 0 )
					car.setPosY(car.getPosY() + (0 - coordSE[1]));
				break;
			}
		}
	}
	
	/**
	 * Tree Drawing
	 */
	public void drawTree(GL2 gl, float x, float y) {
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL2.GL_ALPHA_TEST);
		gl.glAlphaFunc(GL.GL_GREATER, 0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, tree_tex[0]);
		
		gl.glPushMatrix();
		gl.glTranslatef(x, y, 0);
		float modelview[]=new float[16];
		// save the current modelview matrix
		gl.glPushMatrix();

		// get the current modelview matrix
		gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX , modelview,0);

		// undo all rotations
		// beware all scaling is lost as well 
		for(int i=0; i<3; i++ ) 
			for(int j=0; j<3; j++ ) {
				if ( i==j )
					modelview[i*4+j] = 1.0f;
				else
					modelview[i*4+j] = 0.0f;
			}

		// set the modelview with no rotations
		gl.glLoadMatrixf(modelview,0);
		
		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(0,0);gl.glVertex3f(-7, 0, 0);
		gl.glTexCoord2f(1,0);gl.glVertex3f(7, 0, 0);
		gl.glTexCoord2f(1,1);gl.glVertex3f(7, 20, 0);
		gl.glTexCoord2f(0,1);gl.glVertex3f(-7, 20, 0);
		gl.glEnd();
		gl.glPopMatrix();
		gl.glPopMatrix();
		gl.glDisable(GL.GL_BLEND);
		gl.glDisable(GL2.GL_ALPHA_TEST);
	}
	
	public void drawAllTrees(GL2 gl) {
		// Not very elegant
		drawTree(gl, 22, 635);
		drawTree(gl, 24, 655);
		drawTree(gl, 27, 675);
		drawTree(gl, 35, 700);
		drawTree(gl, 42, 725);
		drawTree(gl, 60, 750);
		drawTree(gl, 80, 770);
		drawTree(gl, 70, 770);
		drawTree(gl, 50, 770);
		drawTree(gl, 110, 660);
		drawTree(gl, 170, 695);
		drawTree(gl, 330, 725);
		for (int i = 200; i < 280; i += 15)
			drawTree(gl, i, 790);
		for (int i = 240; i < 280; i += 15)
			drawTree(gl, i, 775);
		for (int i = 260; i < 280; i += 15)
			drawTree(gl, i, 760);
		
		drawTree(gl, 390, 690);
		drawTree(gl, 515, 640);
		drawTree(gl, 515, 660);
		drawTree(gl, 630, 575);
		
		for (int i = 415; i < 540; i += 15)
			drawTree(gl, 782, i);
		drawTree(gl, 752, 540);
		
		for (int i = 425; i > 165; i -= 15)
			drawTree(gl, 692, i);
		
		for (int i = 185; i > 55; i -= 15)
			drawTree(gl, 770, i);
		
		drawTree(gl, 500, 97);
		for (int i = 400; i < 435; i += 15) {
			for (int j = 200; j < 245; j += 15)
				drawTree(gl, i, j);
		}
		drawTree(gl, 485, 321);
		drawTree(gl, 460, 365);
		drawTree(gl, 357, 286);
		drawTree(gl, 338, 382);
		drawTree(gl, 298, 355);
		drawTree(gl, 54, 182);
		drawTree(gl, 100, 170);
		drawTree(gl, 130, 275);
		drawTree(gl, 130, 175);
		drawTree(gl, 175, 195);
		drawTree(gl, 218, 310);
		drawTree(gl, 234, 229);
		drawTree(gl, 265, 249);
		for (int i = 65; i < 210; i += 25) {
			for (int j = 100; j < 151 ; j += 25)
				drawTree(gl, i, j);
		}
	}


	/**
	 * Event Listeners
	 */
	@Override
	public void dispose( GLAutoDrawable arg0 ) {
	}

	@Override
	public void keyTyped( KeyEvent e ) {
		char key = e.getKeyChar();

		switch (key) {
			case KeyEvent.VK_T:
			case 't':
				mode++;
				if (mode == 3) mode = 0;
				break;
	
			case KeyEvent.VK_SPACE:
				pause ^= true;
				break;
		}
	}

	@Override
	public void keyPressed( KeyEvent e ) {
		keys[ e.getKeyCode() ] = true;
	}

	@Override
	public void keyReleased( KeyEvent e ) {
		keys[ e.getKeyCode() ] = false;
	}

	@Override
	public void mouseDragged( MouseEvent e ) {
		// Mouse Rotation
		if (pause) {
			int x = e.getX();
			int y = e.getY();

			final float throttle_rot = 128.0f;

			float dx = ( x - mouse_x0 );
			float dy = ( y - mouse_y0 );

			if ( MOUSE_MODE_ROTATE == mouse_mode ) {
				float phi = (float) Math.acos( scene_look_z );
				float theta = (float) Math.atan2( scene_look_y, scene_look_x );

				theta -= dx / throttle_rot;
				phi += dy / throttle_rot;

				if ( theta >= Math.PI * 2.0 )
					theta -= Math.PI * 2.0;
				else if ( theta < 0 )
					theta += Math.PI * 2.0;

				if ( phi > Math.PI - 0.1 )
					phi = (float)( Math.PI - 0.1 );
				else if ( phi < 0.1f )
					phi = 0.1f;

				scene_look_x = (float)( Math.cos( theta ) * Math.sin( phi ) );
				scene_look_y = (float)( Math.sin( theta ) * Math.sin( phi ) );
				scene_look_z = (float)( Math.cos( phi ) );
			}

			mouse_x0 = x;
			mouse_y0 = y;
		}

	}

	@Override
	public void mouseMoved( MouseEvent e ) {
	}

	@Override
	public void mouseClicked( MouseEvent e ) {
	}

	@Override
	public void mousePressed( MouseEvent e ) {
		mouse_x0 = e.getX();
		mouse_y0 = e.getY();

		if ( MouseEvent.BUTTON1 == e.getButton() ) {
			mouse_mode = MOUSE_MODE_ROTATE;
		} else {
			mouse_mode = MOUSE_MODE_NONE;
		}
	}

	@Override
	public void mouseReleased( MouseEvent e ) {
	}

	@Override
	public void mouseEntered( MouseEvent e ) {
	}

	@Override
	public void mouseExited( MouseEvent e ) {
	}
}