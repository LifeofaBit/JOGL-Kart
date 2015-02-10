package fin;

import java.awt.geom.Point2D;

public class MTV {
	public Boolean result;
	public Point2D.Float coords;
	public float mag;
	
	public MTV(Boolean r, Point2D.Float c, float m) {
		this.result = r;
		this.coords = c;
		this.mag = m;
	}
}