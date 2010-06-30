package nu.shacknet.poseidon.pn.signanzorbit.math;

/**
 * The Class Ellipse
 * Encapsulates the parametric form of the ellipse
 * 
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class Ellipse
{
	
	/** The center of the ellipse */
	public final Vector3 center;
	
	/** The major axis (t=0) */
	public final Vector3 a;
	
	/** The secondary axis */
	public final Vector3 b;
	
	/** The position along the ellipse */
	public final Vector3 pos;

	/** The perimeter of the ellipse */
	public float perimeter;

	/**
	 * Instantiates a new ellipse.
	 *
	 * @param center the center of the ellipse
	 * @param a the major axis (t=0)
	 * @param b the secondary axis
	 */
	public Ellipse(Vector3 center, Vector3 a, Vector3 b)
	{
		//init
		this.pos = new Vector3();
		
		this.center = center;
		this.a = a;
		this.b = b;
	
		calcPerimeter();
	}

	/**
	 * Compute a approximation for the perimeter of the ellipse
	 */
	private native float calcPerimeter(float[] a, float[] b);
	
	/**
	 * Compute a approximation for the perimeter of the ellipse
	 */
	public void calcPerimeter()
	{
		perimeter = calcPerimeter(a.v, b.v);
	}
	
	/**
	 * Gets the point on the ellipse for a given parameter t
	 *
	 * @param center the center of the ellipse
	 * @param a the major axis (t=0)
	 * @param b the secondary axis
	 * @param result the position/point
	 * @param t parameter t along the ellipse
	 */
	private native void getPoint(float[] center, float[] a, float[] b, float[] result, float t);
	
	/**
	 * Gets the point on the ellipse for a given parameter t
	 * @param t the parameter
	 * @return the corresponding point on the ellipse
	 */
	public Vector3 getPoint(float t)
	{
        //pos = center + a cos(t) + b sin(t) - thx to dr. math
		getPoint(center.v, a.v, b.v, pos.v, t);
		return pos;
	}
}