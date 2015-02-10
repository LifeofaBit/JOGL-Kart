package fin;

public class Edge {
	public final float x1;
	public final float y1;
	public final float x2;
	public final float y2;
	
	public Edge(float[] p1, float[] p2) {
		this.x1 = p1[0];
		this.y1 = p1[1];
		this.x2 = p2[0];
		this.y2 = p2[1];
	}
	
	public Edge(float x, float y, float xx, float yy) {
		this.x1 = x;
		this.y1 = y;
		this.x2 = xx;
		this.y2 = yy;
	}
}