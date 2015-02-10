package fin;

public class Projection {
	protected float min;
	protected float max;

	public Projection(float min, float max) {
		this.min = min;
		this.max = max;
	}

	public boolean overlap(Projection p2) {
		if (min > p2.max || max < p2.min)
			return false;

		return true;
	}
	
	public float getOverlap(Projection p2) {
		if (max >= p2.min)
			return max - p2.min;
		else if (min <= p2.max)
			return min - p2.max;
		else
			return 20000;
	}
}