package nu.shacknet.poseidon.pn.signanzorbit.math;

/**
 * The Class Vector2.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class Vector2
{
	
	/** The v. */
	public float v[] = new float[2];
	
	/**
	 * Instantiates a new vector2.
	 *
	 * @param xy Both fields are set to xy
	 */
	public Vector2(float xy)
	{
		set(xy,xy);
	}
	
	/**
	 * Instantiates a new vector2.
	 *
	 * @param x the x
	 * @param y the y
	 */
	public Vector2(float x, float y)
	{
		set(x,y);
	}
	
	/**
	 * Sets the new values.
	 *
	 * @param x the x
	 * @param y the y
	 */
	private void set(float x, float y)
	{
		this.v[0] = x;
		this.v[1] = y;
	}
	
	/**
	 * Copy Constructor.
	 *
	 * @param other the other Vector2
	 */
	public Vector2(Vector2 other)
	{
		this(other.v[0], other.v[1]);
	}

	/**
	 * Instantiates a new vector2(0,0).
	 */
	public Vector2()
	{
		this(0,0);
	}
	
	/**
	 * Instantiates a new vector2.
	 *
	 * @param arr must be a float[2]
	 */
	public Vector2(float[] arr)
	{
		this(arr[0],arr[1]);
	}
	
	/**
	 * native vector + vector
	 *
	 * @param v the vector (result written back into this)
	 * @param otherV the other vector
	 */
	private native void add(float[] v, float[] otherV);
	
	/**
	 * native vector + skalar
	 *
	 * @param v the vector (result written back into this)
	 * @param s the skalar
	 */
	private native void add(float[] v, float s);
	
	/**
	 * Adds the another Vector2 to this.
	 *
	 * @param other the other Vector2
	 * @return this
	 */
	public Vector2 add(Vector2 other)
	{
		add(v, other.v);
		return this;
	}
	
	/**
	 * Adds a skalar to this.
	 *
	 * @param s the s
	 * @return this
	 */
	public Vector2 add(float s)
	{
		add(v, s);
		return this;
	}
	
	/**
	 * Adds two Vector2.
	 *
	 * @param a the first Vector2
	 * @param b the second Vector2
	 * @return a new Vector2, set to a+b
	 */
	public static Vector2 add(Vector2 a, Vector2 b)
	{
		return new Vector2(a).add(b);
	}
	
	/**
	 * Adds a skalar to a Vector2.
	 *
	 * @param a the first Vector2
	 * @param s the skalar
	 * @return a new Vector2, set to a+b
	 */
	public static Vector2 add(Vector2 a, float s)
	{
		return new Vector2(a).add(s);
	}
	
	/**
	 * native vector - vector
	 *
	 * @param v the vector (result written back into this)
	 * @param otherV the other vector
	 */
	private native void subtract(float[] v, float[] otherV);
	
	/**
	 * native vector - skalar
	 *
	 * @param v the vector (result written back into this)
	 * @param s the skalar
	 */
	private native void subtract(float[] v, float s);
	
	/**
	 * Subtracts the another Vector2 from this.
	 *
	 * @param other the other Vector2
	 * @return this
	 */
	public Vector2 subtract(Vector2 other)
	{
		subtract(v, other.v);
		return this;
	}
	
	/**
	 * Subtracts a skalar from this.
	 *
	 * @param s the s
	 * @return this
	 */
	public Vector2 subtract(float s)
	{
		subtract(v, s);
		return this;
	}
	
	/**
	 * subtracts two Vector2.
	 *
	 * @param a the first Vector2
	 * @param b the second Vector2
	 * @return a new Vector2, set to a-b
	 */
	public static Vector2 subtract(Vector2 a, Vector2 b)
	{
		return new Vector2(a).subtract(b);
	}
	
	/**
	 * subtracts a skalar from a Vector2.
	 *
	 * @param a the first Vector2
	 * @param s the skalar
	 * @return a new Vector2, set to a-b
	 */
	public static Vector2 subtract(Vector2 a, float s)
	{
		return new Vector2(a).subtract(s);
	}
	
	/**
	 * native vector * vector
	 *
	 * @param v the vector (result written back into this)
	 * @param otherV the other vector
	 */
	private native void multiply(float[] v, float[] otherV);
	
	/**
	 * native vector * skalar
	 *
	 * @param v the vector (result written back into this)
	 * @param s the skalar
	 */
	private native void multiply(float[] v, float s);
	
	/**
	 * Multiply this by another Vector2.
	 *
	 * @param other the other Vector2
	 * @return this
	 */
	public Vector2 multiply(Vector2 other)
	{
		multiply(v, other.v);
		return this;
	}
	
	/**
	 * Multiplies this by a factor.
	 *
	 * @param s the factor
	 * @return this
	 */
	public Vector2 multiply(float s)
	{
		multiply(v, s);
		return this;
	}
	
	/**
	 * Multiplies two Vector2.
	 *
	 * @param a the first Vector2
	 * @param b the second Vector2
	 * @return a new Vector2, set to a*b
	 */
	public static Vector2 multiply(Vector2 a, Vector2 b)
	{
		return new Vector2(a).multiply(b);
	}
	
	/**
	 * Multiply a Vector2 by a factor.
	 *
	 * @param a the Vector2
	 * @param b the factor
	 * @return a new Vector2, set to a*b
	 */
	public static Vector2 multiply(Vector2 a, float b)
	{
		return new Vector2(a).multiply(b);
	}
	
	/**
	 * native vector / vector
	 *
	 * @param v the vector (result written back into this)
	 * @param otherV the other vector
	 */
	private native void divide(float[] v, float[] otherV);
	
	/**
	 * native vector / skalar
	 *
	 * @param v the vector (result written back into this)
	 * @param s the skalar
	 */
	private native void divide(float[] v, float s);
	
	/**
	 * Divides this by another Vector2.
	 *
	 * @param other the other Vector2
	 * @return this
	 */
	public Vector2 divide(Vector2 other)
	{
		divide(v,other.v);
		return this;
	}
	
	/**
	 * Divides this by a divisor.
	 *
	 * @param s the divisor
	 * @return this
	 */
	public Vector2 divide(float s)
	{
		divide(v,s);
		return this;
	}
	
	/**
	 * Divides two Vector2.
	 *
	 * @param a the first Vector2
	 * @param b the second Vector2
	 * @return a new Vector2, set to a/b
	 */
	public static Vector2 divide(Vector2 a, Vector2 b)
	{
		return new Vector2(a).divide(b);
	}

	/**
	 * Divides a Vector2 by a factor.
	 *
	 * @param a the Vector2
	 * @param b the factor
	 * @return a new Vector2, set to a/b
	 */
	public static Vector2 divide(Vector2 a, float b)
	{
		return new Vector2(a).divide(b);
	}
	
	/**
	 * native dot product.
	 *
	 * @param a the first vector
	 * @param b the second vector
	 * @return the dot product
	 */
	private static native float dotProduct(float[] a, float[] b);
	
	/**
	 * Calculates the dot product of two Vector2.
	 *
	 * @param a the first Vector2
	 * @param b the second Vector2
	 * @return the dot product of a and b
	 */
	public static float dotProduct(Vector2 a, Vector2 b)
	{
		return dotProduct(a.v, b.v);
	}
	
	/**
	 * native Normalize.
	 *
	 * @param v the vector
	 */
	private native void normalize(float[] v);
	
	/**
	 * Normalizes this.
	 *
	 * @return this
	 */
	public Vector2 normalize()
	{
		normalize(v);
		return this;
	}
	
	/**
	 * Normalizes a Vector2.
	 *
	 * @param a the Vector2
	 * @return a new Vector2, set to a, normalized
	 */
	public static Vector2 normalize(Vector2 a)
	{
		return new Vector2(a).normalize();
	}

	/**
	 * native Invert.
	 *
	 * @param v the vector
	 */
	private native void invert(float[] v);

	/**
	 * Inverts this.
	 *
	 * @return this
	 */
	public Vector2 invert()
	{
		invert(v);
		return this;
	}

	/**
	 * native Length.
	 *
	 * @param v the vector
	 * @return the length
	 */
	private native float length(float[] v);

	/**
	 * Length.
	 *
	 * @return the length of this
	 */
	public float length()
	{
		return length(v);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "(" + v[0] + "," + v[1] + ")";
	}
}
