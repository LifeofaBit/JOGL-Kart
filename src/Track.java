package fin;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import com.jogamp.opengl.util.gl2.GLUT;

public class Track {
	public static final String PATH_TO_TEXTURES = "/Users/mattdowns/Documents/workspace/fin/src/fin/course_textures/";
//	public static final String PATH_TO_TEXTURES = "c://Users/donovanjustice1/workspace/fin/course_textures/";
	protected TextureLoader loader = null;
	private int[] textures = new int[4];
	protected final int BLDG_TEX = 4;
	protected int[] bldg_tex = new int[BLDG_TEX];
	private int size;
	private Piece[][] grid = new Piece[4][4]; 
	protected final int CELL;
	private ArrayList checkPoints;
	private TGABuffer buffer;
	private float trackLength = 0;

	
	public Track(TextureLoader loader, int s) {
		this.loader = loader;
		this.size = s;
		this.CELL = size / 4;
		this.checkPoints = new ArrayList();
		loadTrackTextures();
		
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 4; x++)
				grid[y][x] = new Piece(y,x,2);
		}
		
		loadTrackCheckpoints(PATH_TO_TEXTURES+"checkpoints.txt");
	}
	
	protected void loadTrackTextures() {
		textures[0] = loader.generateTexture();
		textures[1] = loader.generateTexture();
		textures[2] = loader.generateTexture(); 
		textures[3] = loader.generateTexture();
		
		try {
			loader.loadTexture(textures[0], PATH_TO_TEXTURES + "grass.jpg", true);
			loader.loadTexture(textures[1], PATH_TO_TEXTURES + "bg.png", false);
			loader.loadTexture(textures[2], PATH_TO_TEXTURES + "track2.png", false);
		} catch ( Exception e ) {
			System.err.println( "Unable to load texture: " + e.getMessage() );
		}
		
		for (int i = 0; i < BLDG_TEX; i++) {
			bldg_tex[i] = loader.generateTexture();
			
			try {
				loader.loadTexture(bldg_tex[i], PATH_TO_TEXTURES + "bldg" + Integer.toString(i) + ".jpg", false);
			} catch ( Exception e ) {
				System.err.println( "Unable to load texture: " + e.getMessage() );
			}
		}
	}
	
	public Piece getCell(float x, float y) { 
		try {
			return grid[(int) y / CELL][(int) x / CELL];
		} catch (Exception e) {
			return new Piece(-1, -1, 0);
		}
	}
	

	public void calculuateTrackLength() {
		float[] checkPointCoords = new float[2];
		float[] prevPointCoords = new float[2];
		float dx;
		float dy;

		for (int i = 0; i < checkPoints.size(); i++) {
			checkPointCoords = (float[])(checkPoints.get(i));
			if (i == 0) {
				prevPointCoords = (float[])(checkPoints.get(checkPoints.size()-1));
			} else {
				prevPointCoords = checkPointCoords;
			}
			
			dx = checkPointCoords[0] - prevPointCoords[0];
			dy = checkPointCoords[1] - prevPointCoords[1];
			this.trackLength += (float) Math.sqrt((dx*dx) + (dy*dy));	
		}
	}
	
	public float getTrackLength() {
		return this.trackLength;
	}
	
	public float TrackSegmentLength(int index) {
		float[] checkPointCoords = new float[2];
		float[] prevPointCoords = new float[2];
		float dx;
		float dy;
		float trackSeg = 0.0f;
		
		for (int i = 0; i < index; i++) {
			checkPointCoords = (float[])(checkPoints.get(i));
			if (i == 0) { // finish
				prevPointCoords = (float[])(checkPoints.get(checkPoints.size()-1));
			} else {
				prevPointCoords = checkPointCoords;
			}
			
			dx = checkPointCoords[0] - prevPointCoords[0];
			dy = checkPointCoords[1] - prevPointCoords[1];
			trackSeg += (float) Math.sqrt((dx*dx) + (dy*dy));	
		}
		
		return trackSeg;
	}
	
	public void loadTrackCheckpoints(String checkpointFile) {
		FileReader reader;
		BufferedReader in;
		
		try {
			String newline;
			reader = new FileReader(checkpointFile);
			in = new BufferedReader(reader);
			while((newline = in.readLine()) != null){	
				if(newline.length() > 0){
                    newline = newline.trim();
                    float coords[] = new float[2];
                    newline = newline.substring(0, newline.length());
                    StringTokenizer st = new StringTokenizer(newline, " ");
                    for(int i = 0; st.hasMoreTokens(); i++)
                        coords[i] = Float.parseFloat(st.nextToken());
                    checkPoints.add(coords);
				}
			}	
			reader.close();
			in.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void drawCheckpoints(GL2 gl) {
		GLUT glu = new GLUT();
		float[] checkPointCoords;
		
		gl.glDisable(GL2.GL_TEXTURE_2D);
		//gl.glBegin(GL2.GL_QUADS);
		for (int i = 0; i < checkPoints.size(); i++) {
			checkPointCoords = (float[])(checkPoints.get(i));
			gl.glPushMatrix();
			gl.glTranslatef(checkPointCoords[0], checkPointCoords[1], 0.0f);
			glu.glutSolidSphere(32, 10, 10);
			gl.glPopMatrix();
		}
		//gl.glEnd();
		gl.glEnable(GL2.GL_TEXTURE_2D);
	}
		
	public float[] getCheckpoints(int checkIndex) {
		return (float[])(checkPoints.get(checkIndex));
	}
	
	public int findNearestCheckpoint(float x, float y) {
		float radius = 64.0f;
		float[] temp = new float[2];
		
		float currCP_x;
		float currCP_y;
		float dx;
		float dy;
		float r;
		
		int CPindex = 0;
		
		for (int i = 0; i < checkPoints.size(); i++) {
			temp = (float[]) checkPoints.get(i);
			currCP_x = temp[0];
			currCP_y = temp[1];
			
			dx = (x - currCP_x)*(x - currCP_x);
			dy = (y - currCP_y)*(y - currCP_y);
			r = (float) Math.sqrt(dx + dy);
			
			if (r < radius) {
				radius = r;
				CPindex = i;
			}
		}
		
		return CPindex;
	}
	
	public void draw(GL2 gl, float x, float y) {		
		// Course Texture
		gl.glBindTexture(GL.GL_TEXTURE_2D, textures[2]);
		gl.glBegin(GL2.GL_QUADS);
		
		gl.glTexCoord2f(1, 1); gl.glVertex3f(size, size, 0.2f);
		gl.glTexCoord2f(0, 1); gl.glVertex3f(0, size, 0.2f);
		gl.glTexCoord2f(0, 0); gl.glVertex3f(0, 0, 0.2f);
		gl.glTexCoord2f(1, 0); gl.glVertex3f(size, 0, 0.2f);
		
		gl.glEnd();
		
		final int row = (int) y / CELL;
		final int col = (int) x / CELL;
		
		// Try to draw current cell
		try { 
			grid[row][col].draw(gl);
		} catch (Exception e) {}
		
		// Try to draw all neighbors
		try {
			grid[row - 1][col - 1].draw(gl);
		} catch (Exception e) {}
		
		try {
			grid[row - 1][col].draw(gl);
		} catch (Exception e) {}
		
		try {
			grid[row - 1][col + 1].draw(gl);
		} catch (Exception e) {}
		
		try {
			grid[row][col - 1].draw(gl);
		} catch (Exception e) {}
		
		try {
			grid[row][col + 1].draw(gl);
		} catch (Exception e) {}
		
		try {
			grid[row + 1][col - 1].draw(gl);
		} catch (Exception e) {}
		
		try {
			grid[row + 1][col].draw(gl);
		} catch (Exception e) {}
		
		try {
			grid[row + 1][col + 1].draw(gl);
		} catch (Exception e) {}
	}
	
	public void drawBG(final GL2 gl) {
		gl.glBegin(GL2.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL2.GL_ALPHA_TEST);
		gl.glAlphaFunc(GL.GL_GREATER, 0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, textures[1]);
		gl.glBegin(GL2GL3.GL_QUADS);
			
		// North End
		gl.glTexCoord2f(0, 0);
		gl.glVertex3f(0, size, 0);
		gl.glTexCoord2f(0, 1);
		gl.glVertex3f(0, size, 150);
		gl.glTexCoord2f(1, 1);
		gl.glVertex3f(size, size, 150);
		gl.glTexCoord2f(1, 0);
		gl.glVertex3f(size, size, 0);
		
		// East End
		gl.glTexCoord2f(0, 0);
		gl.glVertex3f(size, size, 0);
		gl.glTexCoord2f(0, 1);
		gl.glVertex3f(size, size, 150);
		gl.glTexCoord2f(1, 1);
		gl.glVertex3f(size, 0, 150);
		gl.glTexCoord2f(1, 0);
		gl.glVertex3f(size, 0, 0);

		// South End
		gl.glTexCoord2f(0, 0);
		gl.glVertex3f(0, 0, 0);
		gl.glTexCoord2f(0, 1);
		gl.glVertex3f(0, 0, 150);
		gl.glTexCoord2f(1, 1);
		gl.glVertex3f(size, 0, 150);
		gl.glTexCoord2f(1, 0);
		gl.glVertex3f(size, 0, 0);
	
		// West End
		gl.glTexCoord2f(0, 0);
		gl.glVertex3f(0, 0, 0);
		gl.glTexCoord2f(0, 1);
		gl.glVertex3f(0, 0, 150);
		gl.glTexCoord2f(1, 1);
		gl.glVertex3f(0, size, 150);
		gl.glTexCoord2f(1, 0);
		gl.glVertex3f(0, size, 0);
		
		gl.glDisable(GL.GL_BLEND);
		gl.glDisable(GL2.GL_ALPHA_TEST);
		gl.glEnd();
	}
	
	protected class Piece {
		protected int num;
		protected int boundX;
		protected int boundY;
		protected int[] piece_tex = new int[1];
		protected Building[] bldgs;
		
		protected Piece(int row, int col, int num_bldg) {
			this.num = row * 4 + col;
			this.boundX = col * CELL;
			this.boundY = row * CELL;
			loadPieceTextures();
			
			bldgs = new Building[num_bldg];

			Random rand = new Random();		// Random textures applied			
			// Custom Drawn
			switch (num) {
				case 0:
					bldgs[0] = new Building(rand.nextInt(BLDG_TEX), boundX + 5, boundY + 5, 50, 160, 50);
					bldgs[1] = new Building(rand.nextInt(BLDG_TEX), boundX + 65, boundY + 5, 120, 80, 25);
					break;
				case 1:
					bldgs[0] = new Building(rand.nextInt(BLDG_TEX), boundX + 20, boundY + 10, 160, 100, 75);
					bldgs[1] = new Building(rand.nextInt(BLDG_TEX), boundX + 50, boundY + 120, 75, 70, 30);
					break;
				case 2:
					bldgs[0] = new Building(rand.nextInt(BLDG_TEX), boundX + 2, boundY + 2, 178, 50, 60);
					bldgs[1] = new Building(rand.nextInt(BLDG_TEX), boundX + 2, boundY + 60, 58, 130, 30);
					break;
				case 3:
					bldgs[0] = new Building(rand.nextInt(BLDG_TEX), boundX + 2, boundY + 2, 197, 41, 50);
					bldgs[1] = new Building(rand.nextInt(BLDG_TEX), boundX + 2, boundY + 150, 78, 49, 30);
					break;	
				case 4:
					bldgs[0] = new Building(rand.nextInt(BLDG_TEX), boundX + 2, boundY + 2, 23, 188, 60);
					bldgs[1] = new Building(rand.nextInt(BLDG_TEX), boundX + 100, boundY + 95, 95, 100, 70);
					break;
				case 5:
					bldgs[0] = new Building(rand.nextInt(BLDG_TEX), boundX + 85, boundY + 2, 100, 58, 40);
					bldgs[1] = new Building(rand.nextInt(BLDG_TEX), boundX + 2, boundY + 138, 85, 55, 30);
					break;
				case 6:
					bldgs[0] = new Building(rand.nextInt(BLDG_TEX), boundX + 50, boundY + 180, 140, 15, 50);
					bldgs[1] = new Building(rand.nextInt(BLDG_TEX), boundX + 115, boundY + 30, 80, 145, 30);
					break;
				case 7:
					bldgs[0] = new Building(rand.nextInt(BLDG_TEX), boundX + 2, boundY + 2, 78, 178, 50);
					bldgs[1] = new Building(rand.nextInt(BLDG_TEX), boundX + 165, boundY + 2, 34, 197, 60);
					break;
				case 8:
					bldgs[0] = new Building(rand.nextInt(BLDG_TEX), boundX + 5, boundY + 5, 20, 190, 30);
					bldgs[1] = new Building(rand.nextInt(BLDG_TEX), boundX + 96, boundY + 5, 100, 190, 80);
					break;
				case 9:
					bldgs[0] = new Building(rand.nextInt(BLDG_TEX), boundX + 2, boundY + 2, 193, 48, 50);
					bldgs[1] = new Building(rand.nextInt(BLDG_TEX), boundX + 2, boundY + 55, 193, 140, 30);
					break;
				case 10:
					bldgs[0] = new Building(rand.nextInt(BLDG_TEX), boundX + 2, boundY + 2, 197, 98, 20);
					bldgs[1] = new Building(rand.nextInt(BLDG_TEX), boundX + 2, boundY + 105, 58, 58, 70);
					break;
				case 11:
					bldgs[0] = new Building(rand.nextInt(BLDG_TEX), boundX + 50, boundY + 155, 145, 40, 50);
					bldgs[1] = new Building(rand.nextInt(BLDG_TEX), boundX + 2, boundY + 2, 73, 48, 30);
					break;
				case 12:
					bldgs[0] = new Building(rand.nextInt(BLDG_TEX), boundX + 5, boundY + 182, 190, 17, 50);
					bldgs[1] = new Building(rand.nextInt(BLDG_TEX), boundX + 130, boundY + 5, 50, 80, 30);
					break;
				case 13:
					bldgs[0] = new Building(rand.nextInt(BLDG_TEX), boundX + 85, boundY + 140, 100, 58, 50);
					bldgs[1] = new Building(rand.nextInt(BLDG_TEX), boundX + 150, boundY + 105, 45, 30, 30);
					break;
				case 14:
					bldgs[0] = new Building(rand.nextInt(BLDG_TEX), boundX + 2, boundY + 80, 197, 120, 80);
					bldgs[1] = new Building(rand.nextInt(BLDG_TEX), boundX + 130, boundY + 20, 65, 50, 40);
					break;
				case 15:
					bldgs[0] = new Building(rand.nextInt(BLDG_TEX), boundX, boundY, 60, 190, 20);
					bldgs[1] = new Building(rand.nextInt(BLDG_TEX), boundX + 90, boundY + 20, 100, 100, 60);
					break;
				default:
					bldgs[0] = new Building(rand.nextInt(BLDG_TEX), boundX + 5, boundY + 5, 10, 10, 50);
					bldgs[1] = new Building(rand.nextInt(BLDG_TEX), boundX + 25, boundY + 25, 10, 10, 30);
					break;
			}
			
		}
		
		protected void loadPieceTextures() {
			
		}
		
		public int getNum() {
			return num;
		}
		
		public Building getBuilding(int num) { 
			try {
				return bldgs[num]; 
			} catch (Exception e) {
				return null;
			}
		}
		
		public void draw(GL2 gl) {
			for (int i = 0; i < bldgs.length; i++) {
				bldgs[i].draw(gl);
			}
			
			gl.glBindTexture(GL.GL_TEXTURE_2D, textures[0]);
			gl.glBegin(GL2.GL_QUADS);
			
			gl.glTexCoord2f(0, 0); gl.glVertex3f(boundX, boundY, 0);
			gl.glTexCoord2f(0, 10); gl.glVertex3f(boundX, boundY + CELL, 0);
			gl.glTexCoord2f(10, 10); gl.glVertex3f(boundX + CELL, boundY + CELL, 0);
			gl.glTexCoord2f(10, 0); gl.glVertex3f(boundX + CELL, boundY, 0);
			
			gl.glEnd();
		}
		
		protected class Building {
			protected int texture;
			protected int[] dim = new int[3];
			protected float[][] corner = new float[4][2];
			public Edge[] edges;
			
			protected Building(int tex, float x, float y, int w, int d, int h) {
				this.texture = tex;
				dim[0] = w;
				dim[1] = d;
				dim[2] = h;
				corner[0][0] = x;		// Southwest
				corner[0][1] = y;
				corner[1][0] = x + w; 	// Southeast
				corner[1][1] = y;
				corner[2][0] = x + w;	// Northeast
				corner[2][1] = y + d;
				corner[3][0] = x;		// Northwest
				corner[3][1] = y + d;
			}
			
			public float[] getSW() { return new float[] { corner[0][0], corner[0][1] }; }
			public float[] getSE() { return new float[] { corner[1][0], corner[1][1] }; }
			public float[] getNE() { return new float[] { corner[2][0], corner[2][1] }; }
			public float[] getNW() { return new float[] { corner[3][0], corner[3][1] }; }
			public float[] getDim() { return new float[] { dim[0], dim[1], dim[2] }; }
			public float[][] getShape() { return new float[][] { getSW(), getSE(), getNE(), getNW() }; }
			public Edge[] getEdges() { 
				return new Edge[] {
						new Edge(getSW(), getSE()),
						new Edge(getSE(), getNE()),
						new Edge(getNE(), getNW()),
						new Edge(getNW(), getSW())
				};
			}
			
			public float[][] getNorth() {
				return new float[][] {
						{ corner[3][0], corner[3][1] },
						{ corner[2][0], corner[2][1] }
				};
			}
			public float[] getNorthN() { return new float[] { 0, 1 }; }
			
			public float[][] getEast() {
				return new float[][] {
						{ corner[1][0], corner[1][1] },
						{ corner[2][0], corner[2][1] }
				};
			}
			public float[] getEastN() { return new float[] { 1, 0 }; }
			
			public float[][] getSouth() {
				return new float[][] {
						{ corner[0][0], corner[0][1] },
						{ corner[1][0], corner[1][1] }
				};
			}
			public float[] getSouthN() { return new float[] { 0, -1 }; }
			
			public float[][] getWest() {
				return new float[][] {
						{ corner[0][0], corner[0][1] },
						{ corner[3][0], corner[3][1] }
				};
			}
			public float[] getWestN() { return new float[] { -1, 0 }; }
			
			
			public Boolean inBounds(float[] coord) {
				if (corner[0][0] <= coord[0] && coord[0] <= corner[1][0] && corner[1][1] <= coord[1] && coord[1] <= corner[2][1])
					return true;
				
				return false;
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
			
			public void draw(GL2 gl) {
				gl.glBindTexture(GL.GL_TEXTURE_2D, bldg_tex[texture]);
				gl.glBegin(GL2.GL_QUADS);

				// South Face
				gl.glTexCoord2f(0, 0); gl.glVertex3f(corner[0][0], corner[0][1], 0);
				gl.glTexCoord2f(0, 1); gl.glVertex3f(corner[0][0], corner[0][1], dim[2]);
				gl.glTexCoord2f(1, 1); gl.glVertex3f(corner[1][0], corner[1][1], dim[2]);
				gl.glTexCoord2f(1, 0); gl.glVertex3f(corner[1][0], corner[1][1], 0);

				// West Face
				gl.glTexCoord2f(0, 0); gl.glVertex3f(corner[0][0], corner[0][1], 0);
				gl.glTexCoord2f(0, 1); gl.glVertex3f(corner[0][0], corner[0][1], dim[2]);
				gl.glTexCoord2f(1, 1); gl.glVertex3f(corner[3][0], corner[3][1], dim[2]);
				gl.glTexCoord2f(1, 0); gl.glVertex3f(corner[3][0], corner[3][1], 0);
				
				// North Face
				gl.glTexCoord2f(0, 0); gl.glVertex3f(corner[3][0], corner[3][1], 0);
				gl.glTexCoord2f(0, 1); gl.glVertex3f(corner[3][0], corner[3][1], dim[2]);
				gl.glTexCoord2f(1, 1); gl.glVertex3f(corner[2][0], corner[2][1], dim[2]);
				gl.glTexCoord2f(1, 0); gl.glVertex3f(corner[2][0], corner[2][1], 0);
				
				// East Face
				gl.glTexCoord2f(0, 0); gl.glVertex3f(corner[2][0], corner[2][1], 0);
				gl.glTexCoord2f(0, 1); gl.glVertex3f(corner[2][0], corner[2][1], dim[2]);
				gl.glTexCoord2f(1, 1); gl.glVertex3f(corner[1][0], corner[1][1], dim[2]);
				gl.glTexCoord2f(1, 0); gl.glVertex3f(corner[1][0], corner[1][1], 0);
				
				// Top
				gl.glVertex3f(corner[0][0], corner[0][1], dim[2]);
				gl.glVertex3f(corner[1][0], corner[1][1], dim[2]);
				gl.glVertex3f(corner[2][0], corner[2][1], dim[2]);
				gl.glVertex3f(corner[3][0], corner[3][1], dim[2]);

				gl.glEnd();
			}	
		}
	}	
}