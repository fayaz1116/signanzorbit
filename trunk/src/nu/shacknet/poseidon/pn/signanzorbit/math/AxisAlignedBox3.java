package nu.shacknet.poseidon.pn.signanzorbit.math;

/**
 * The Class AxisAlignedBox3.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class AxisAlignedBox3
{
	
	/** The min and max. */
	public final Vector3 min,max;
	
	/**
	 * Instantiates a new axis aligned box3.
	 */
	public AxisAlignedBox3()
	{
		min = new Vector3();
		max = new Vector3();
	}

	/**
	 * Copy Constructor.
	 *
	 * @param other the other
	 */
	public AxisAlignedBox3(AxisAlignedBox3 other)
	{
		min = new Vector3(other.min);
		max = new Vector3(other.max);
	}
	
	/**
	 * Instantiates a new axis aligned box3.
	 *
	 * @param min the min
	 * @param max the max
	 */
	public AxisAlignedBox3(Vector3 min, Vector3 max)
	{
		this.min = min;
		this.max = max;
	}
	
	/**
	 * Gets the volume.
	 *
	 * @return the volume
	 */
	public float getVolume() 
	{
		return (max.v[0] - min.v[0]) * (max.v[1] - min.v[1]) * (max.v[2] - min.v[2]);
	}
	
	/**
	 * Resizes the box to include a Vector3.
	 *
	 * @param v the Vector3
	 */
	public void include(Vector3 v)
	{
		minimize(min, v);
		maximize(max, v);
	}
	
	/**
	 * Resizes the box to include box.
	 *
	 * @param box the box
	 */
	public void include(AxisAlignedBox3 box)
	{
		minimize(min, box.min);
		maximize(max, box.max);
	}
	
	/**
	 * Checks if a Point is inside.
	 *
	 * @param v the Point
	 * @return true, if Point is inside
	 */
	public boolean isInside(Vector3 v)
	{
		return !((v.v[0] < min.v[0]) ||
			     (v.v[0] > max.v[0]) ||
				 (v.v[1] < min.v[1]) ||
				 (v.v[1] > max.v[1]) ||
				 (v.v[2] < min.v[2]) ||
				 (v.v[2] > max.v[2]));
	}

	/**
	 * Checks if a Box is inside.
	 *
	 * @param box the box
	 * @return true, if box is inside
	 */
	public boolean includes(AxisAlignedBox3 box)
	{
		return (box.min.v[0] >= min.v[0] &&
				box.min.v[1] >= min.v[1] &&
				box.min.v[2] >= min.v[2] &&
				box.max.v[0] <= max.v[0] &&
				box.max.v[1] <= max.v[1] &&
				box.max.v[2] <= max.v[2]);
	}
	
	/**
	 * Sets min to the minimum of min and candidate.
	 *
	 * @param min will be set to the minimum
	 * @param candidate the candidate
	 */
	void minimize(Vector3 min, Vector3 candidate)
	{
		if (candidate.v[0] < min.v[0])
			min.v[0] = candidate.v[0];
		if (candidate.v[1] < min.v[1])
			min.v[1] = candidate.v[1];
		if (candidate.v[2] < min.v[2])
			min.v[2] = candidate.v[2];
	}

	/**
	 * Sets max to the maximum of max and candidate.
	 *
	 * @param max will be set to the maximum
	 * @param candidate the candidate
	 */
	void maximize(Vector3 max, Vector3 candidate)
	{
		if (candidate.v[0] > max.v[0])
			max.v[0] = candidate.v[0];
		if (candidate.v[1] > max.v[1])
			max.v[1] = candidate.v[1];
		if (candidate.v[2] > max.v[2])
			max.v[2] = candidate.v[2];
	}
	
	/**
	 * native center.
	 *
	 * @param minx the minx
	 * @param maxx the maxx
	 * @param miny the miny
	 * @param maxy the maxy
	 * @param minz the minz
	 * @param maxz the maxz
	 * @return a new Vector3
	 */
	private native Vector3 center(float minx, float maxx, float miny, float maxy, float minz, float maxz);
	
	/**
	 * Center.
	 *
	 * @return the center of this box
	 */
	public Vector3 center()
	{ 
		return center(min.v[0], max.v[0], min.v[1], max.v[1], min.v[2], max.v[2]); 
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "AxisAlignedBox3: min=" + min + ", max=" + max;
	}
}
