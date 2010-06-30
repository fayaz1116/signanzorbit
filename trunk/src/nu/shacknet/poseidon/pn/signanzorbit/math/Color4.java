package nu.shacknet.poseidon.pn.signanzorbit.math;

/**
 * The Class Color4.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class Color4
{
	public final float r,g,b,a;
	public final float[] asArray;
	
	public static final Color4 BLACK = new Color4(0,0,0);
	public static final Color4 GRAY30 = new Color4(0.3f,0.3f,0.3f);
	public static final Color4 WHITE = new Color4(1,1,1);
	public static final Color4 RED = new Color4(1,0,0);
	public static final Color4 GREEN = new Color4(0,1,0);
	public static final Color4 BLUE = new Color4(0,0,1);
	public static final Color4 CYAN = new Color4(0,1,1);
	public static final Color4 MAGENTA = new Color4(1,0,1);
	public static final Color4 YELLOW = new Color4(1,1,0);
	
	/**
	 * Instantiates a new color4.
	 *
	 * @param r the r
	 * @param g the g
	 * @param b the b
	 * @param a the a
	 */
	public Color4(float r, float g, float b, float a)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		asArray = new float[] {r,g,b,a};
	}
	
	/**
	 * Instantiates a new color4, a=1
	 *
	 * @param r the r
	 * @param g the g
	 * @param b the b
	 */
	public Color4(float r, float g, float b)
	{
		this(r,g,b,1);
	}
	
	/**
	 * Copy Constructor
	 *
	 * @param other the other
	 */
	public Color4(Color4 other)
	{
		this(other.r, other.g, other.b, other.a);
	}

	/**
	 * Instantiates a new color4(0,0,0,1)
	 */
	public Color4()
	{
		this(0,0,0,1);
	}
	
	/**
	 * Instantiates a new color4.
	 *
	 * @param arr a float[4]
	 */
	public Color4(float[] arr)
	{
		this(arr[0],arr[1],arr[2],arr[3]);
	}
}
