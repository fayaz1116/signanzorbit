package nu.shacknet.poseidon.pn.signanzorbit.math;

/**
 * The Class Vector4.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class Vector4
{
	
	/** The v. */
	public float[] v = new float[4];
	
	/**
	 * Instantiates a new vector4.
	 *
	 * @param xyzw All four fields are set to xyzw
	 */
	public Vector4(float xyzw)
	{
		set(xyzw,xyzw,xyzw,xyzw);
	}
	
	/**
	 * Instantiates a new vector4.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param w the w
	 */
	public Vector4(float x, float y, float z, float w)
	{
		set(x,y,z,w);
	}
	
	/**
	 * Sets the new values.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param w the w
	 */
	private void set(float x, float y, float z, float w)
	{
		this.v[0] = x;
		this.v[1] = y;
		this.v[2] = z;
		this.v[3] = w;
	}
	
	/**
	 * Instantiates a new vector4, w will be set to 1.0f
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public Vector4(float x, float y, float z)
	{
		this(x,y,z,1);
	}
	
	/**
	 * copy constructor.
	 *
	 * @param other the other Vector4
	 */
	public Vector4(Vector4 other)
	{
		this(other.v[0], other.v[1], other.v[2], other.v[3]);
	}
	
	/**
	 * copy constructor, w will be set to 1.0f
	 *
	 * @param other a Vector3 to get x,y,z from
	 */
	public Vector4(Vector3 other)
	{
		this(other.v[0], other.v[1], other.v[3], 1);
	}

	/**
	 * Instantiates a new vector4(0,0,0,1).
	 */
	public Vector4()
	{
		this(0,0,0,1);
	}
	
	/**
	 * Instantiates a new vector4.
	 *
	 * @param arr must be a float[4]
	 */
	public Vector4(float[] arr)
	{
		this(arr[0],arr[1],arr[2],arr[3]);
	}
	
	/**
	 * native vector + vector.
	 *
	 * @param v the vector (result written back into this)
	 * @param otherV the other vector
	 */
	private native void add(float[] v, float[] otherV);
	
	/**
	 * native vector + vector.
	 *
	 * @param v the vector (result written back into this)
	 * @param otherV the other vector
	 */
	private native void add3(float[] v, float[] otherV);
	
	/**
	 * native vector + skalar.
	 *
	 * @param v the vector (result written back into this)
	 * @param s the skalar
	 */
	private native void add(float[] v, float s);
	
	/**
	 * Adds another Vector4 to this.
	 *
	 * @param other the other Vector4
	 * @return this
	 */
	public Vector4 add(Vector4 other)
	{
		add(v, other.v);
		return this;
	}
	
	/**
	 * Adds a skalar to this.
	 *
	 * @param s the skalar
	 * @return this
	 */
	public Vector4 add(float s)
	{
		add(v, s);
		return this;
	}
	
	/**
	 * Adds two Vector4.
	 *
	 * @param a the first Vector4
	 * @param b the second Vector4
	 * @return a new Vector4, set to a+b
	 */
	public static Vector4 add(Vector4 a, Vector4 b)
	{
		return new Vector4(a).add(b);
	}
	
	/**
	 * Adds a skalar to a Vector4.
	 *
	 * @param a the Vector4
	 * @param s the skalar
	 * @return a new Vector4, set to a+s
	 */
	public static Vector4 add(Vector4 a , float s)
	{
		return new Vector4(a).add(s);
	}
	
	/**
	 * Adds a Vector3 to this.
	 *
	 * @param other a Vector3
	 * @return this
	 */
	public Vector4 add(Vector3 other)
	{
		add3(v, other.v);
		return this;
	}
	
	/**
	 * Adds a Vector4 and a Vector3.
	 *
	 * @param a the Vector4
	 * @param b the Vector3
	 * @return a new Vector4, set to a+b
	 */
	public static Vector4 add(Vector4 a, Vector3 b)
	{
		return new Vector4(a).add(b);
	}
	
	/**
	 * native vector - vector.
	 *
	 * @param v the vector (result written back into this)
	 * @param otherV the other vector
	 */
	private native void subtract(float[] v, float[] otherV);
	
	/**
	 * native vector - vector.
	 *
	 * @param v the vector (result written back into this)
	 * @param otherV the other vector
	 */
	private native void subtract3(float[] v, float[] otherV);
	
	/**
	 * native vector - skalar.
	 *
	 * @param v the vector (result written back into this)
	 * @param s the skalar
	 */
	private native void subtract(float[] v, float s);
	
	/**
	 * Subtracts a Vector4 from this.
	 *
	 * @param other the other Vector4
	 * @return this
	 */
	public Vector4 subtract(Vector4 other)
	{
		subtract(v, other.v);
		return this;
	}
	
	/**
	 * Subtracts a skalar from this.
	 *
	 * @param s the skalar
	 * @return this
	 */
	public Vector4 subtract(float s)
	{
		subtract(v, s);
		return this;
	}
	
	/**
	 * Subtracts two Vector4.
	 *
	 * @param a the first Vector4
	 * @param b the second Vector4
	 * @return a new Vector4, set to a-b
	 */
	public static Vector4 subtract(Vector4 a, Vector4 b)
	{
		return new Vector4(a).subtract(b);
	}
	
	/**
	 * Subtracts a skalar from a Vector4.
	 *
	 * @param a the Vector4
	 * @param s the skalar
	 * @return a new Vector4, set to a-s
	 */
	public static Vector4 subtract(Vector4 a, float s)
	{
		return new Vector4(a).subtract(s);
	}
	
	/**
	 * Subtracts a Vector3 from this.
	 *
	 * @param other the Vector3
	 * @return this
	 */
	public Vector4 subtract(Vector3 other)
	{
		subtract3(v, other.v);
		return this;
	}
	
	/**
	 * Subtract a Vector3 from a Vector4.
	 *
	 * @param a the Vector4
	 * @param b the Vector3
	 * @return a new Vector4, set to a-b
	 */
	public static Vector4 subtract(Vector4 a, Vector3 b)
	{
		return new Vector4(a).subtract(b);
	}
	
	/**
	 * native vector * vector.
	 *
	 * @param v the vector (result written back into this)
	 * @param otherV the other vector
	 */
	private native void multiply(float[] v, float[] otherV);
	
	/**
	 * native vector * vector.
	 *
	 * @param v the vector (result written back into this)
	 * @param otherV the other vector
	 */
	private native void multiply3(float[] v, float[] otherV);
	
	/**
	 * native vector * skalar.
	 *
	 * @param v the vector (result written back into this)
	 * @param s the skalar
	 */
	private native void multiply(float[] v, float s);
	
	/**
	 * Multiply this with a Vector4.
	 *
	 * @param other the other Vector4
	 * @return this
	 */
	public Vector4 multiply(Vector4 other)
	{
		multiply(v, other.v);
		return this;
	}
	
	/**
	 * Multiply this with a factor.
	 *
	 * @param s the factor
	 * @return this
	 */
	public Vector4 multiply(float s)
	{
		multiply(v, s);
		return this;
	}
	
	/**
	 * Multiply two Vector4.
	 *
	 * @param a the first Vector4
	 * @param b the second Vector4
	 * @return a new Vector4, set to a*b
	 */
	public static Vector4 multiply(Vector4 a, Vector4 b)
	{
		return new Vector4(a).multiply(b);
	}
	
	/**
	 * Multiply a Vector4 with a skalar.
	 *
	 * @param a the first Vector4
	 * @param s the skalar
	 * @return a new Vector4, set to a*s
	 */
	public static Vector4 multiply(Vector4 a, float s)
	{
		return new Vector4(a).multiply(s);
	}
	
	/**
	 * Multiply this with a Vector3.
	 *
	 * @param other the Vector3
	 * @return this
	 */
	public Vector4 multiply(Vector3 other)
	{
		multiply3(v, other.v);
		return this;
	}
	
	/**
	 * Multiply a Vector4 and a Vector3.
	 *
	 * @param a the Vector4
	 * @param b the Vector3
	 * @return a new Vector4, set to a*b
	 */
	public static Vector4 multiply(Vector4 a, Vector3 b)
	{
		return new Vector4(a).multiply(b);
	}

	/**
	 * native vector / vector.
	 *
	 * @param v the vector (result written back into this)
	 * @param otherV the other vector
	 */
	private native void divide(float[] v, float[] otherV);
	
	/**
	 * native vector / vector.
	 *
	 * @param v the vector (result written back into this)
	 * @param otherV the other vector
	 */
	private native void divide3(float[] v, float[] otherV);
	
	/**
	 * native vector / skalar.
	 *
	 * @param v the vector (result written back into this)
	 * @param s the skalar
	 */
	private native void divide(float[] v, float s);
	
	/**
	 * Divide this by another Vector4.
	 *
	 * @param other the other Vector4
	 * @return this
	 */
	public Vector4 divide(Vector4 other)
	{
		divide(v, other.v);
		return this;
	}
	
	/**
	 * Divide this by a factor.
	 *
	 * @param s the the factor
	 * @return this
	 */
	public Vector4 divide(float s)
	{
		divide(v, s);
		return this;
	}
	
	/**
	 * Divide two Vector4.
	 *
	 * @param a the first Vector4
	 * @param b the second Vector4
	 * @return a new Vector4, set to a/b
	 */
	public static Vector4 divide(Vector4 a, Vector4 b)
	{
		return new Vector4(a).divide(b);
	}
	
	/**
	 * Divide a Vector4 by a skalar.
	 *
	 * @param a the Vector4
	 * @param s the skalar
	 * @return a new Vector4, set to a/s
	 */
	public static Vector4 divide(Vector4 a, float s)
	{
		return new Vector4(a).divide(s);
	}
	
	/**
	 * Divide this by a Vector3.
	 *
	 * @param other the Vector3
	 * @return this
	 */
	public Vector4 divide(Vector3 other)
	{
		divide3(v, other.v);
		return this;
	}
	
	/**
	 * Divide a Vector4 by a Vector3.
	 *
	 * @param a the Vector4
	 * @param b the Vector3
	 * @return a new Vector4, set to a/b
	 */
	public static Vector4 divide(Vector4 a, Vector3 b)
	{
		return new Vector4(a).divide(b);
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
	 * Calculates the dot product of two Vector3.
	 *
	 * @param a the first Vector4
	 * @param b the second Vector4
	 * @return the dot product of a and b
	 */
	public static float dotProduct (Vector4 a, Vector4 b)
	{
		return dotProduct(a.v, b.v);
	}
	
	/**
	 * native normalize.
	 *
	 * @param v the vector
	 */
	private native void normalize(float[] v);
	
	/**
	 * Normalizes this.
	 *
	 * @return this
	 */
	public Vector4 normalize()
	{
		normalize(v);
		return this;
	}
	
	/**
	 * Normalizes a Vector4.
	 *
	 * @param a the Vector4
	 * @return a new Vector4, set to a, normalized.
	 */
	public static Vector4 normalize(Vector4 a)
	{
		return new Vector4(a).normalize();
	}
	
	/**
	 * native homogenize.
	 *
	 * @param v the vector
	 */
	private native void homogenize(float[] v);
	
	/**
	 * Homogenizes this.
	 *
	 * @return this
	 */
	public Vector4 homogenize()
	{
		homogenize(v);
		return this;
	}
	
	/**
	 * Homogenizes a Vector4.
	 *
	 * @param a the Vector4
	 * @return a new Vector4, set to a, homogenized.
	 */
	public static Vector4 homogenize(Vector4 a)
	{
		return new Vector4(a).homogenize();
	}

	/**
	 * native invert
	 *
	 * @param v the vector
	 */
	private native void invert(float[] v);
	
	/**
	 * Inverts this.
	 *
	 * @return this
	 */
	public Vector4 invert()
	{
		invert(v);
		return this;
	}
	
	/**
	 * native length.
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
		return "(" + v[0] + "," + v[1] + "," + v[2] + "," + v[3] + ")";
	}
}
