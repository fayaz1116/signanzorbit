package nu.shacknet.poseidon.pn.signanzorbit.math;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import nu.shacknet.poseidon.pn.signanzorbit.util.Persistable;


/**
 * The Class Vector3.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class Vector3 implements Persistable
{
	
	/** The v. */
	public float[] v = new float[3];
	
	/**
	 * Instantiates a new vector3.
	 *
	 * @param xyz All three fields are set to xyz
	 */
	public Vector3(float xyz)
	{
		set(xyz,xyz,xyz);
	}
	
	/**
	 * Instantiates a new vector3.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public Vector3(float x, float y, float z)
	{
		set(x,y,z);
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.util.Persistable#persist(java.io.DataOutputStream)
	 */
	public void persist(DataOutputStream dos) throws IOException
	{
		for(int i=0; i<3; i++)
			dos.writeFloat(v[i]);
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.util.Persistable#restore(java.io.DataInputStream)
	 */
	public void restore(DataInputStream dis) throws IOException
	{
		for(int i=0; i<3; i++)
			v[i] = dis.readFloat();
	}
	
	/**
	 * Sets the new values.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return the vector3
	 */
	public Vector3 set(float x, float y, float z)
	{
		this.v[0] = x;
		this.v[1] = y;
		this.v[2] = z;
		return this;
	}
	
	/**
	 * Sets the new values.
	 *
	 * @param other contains the values to set
	 * @return the vector3
	 */
	public Vector3 set(Vector3 other)
	{
		return set(other.v[0], other.v[1], other.v[2]);
	}
	
	/**
	 * copy constructor.
	 *
	 * @param other the other Vector3
	 */
	public Vector3(Vector3 other)
	{
		this(other.v[0], other.v[1], other.v[2]);
	}

	/**
	 * Instantiates a new vector3(0,0,0).
	 */
	public Vector3()
	{
		this(0,0,0);
	}
	
	/**
	 * Instantiates a new vector3.
	 *
	 * @param arr must be an array[3]
	 */
	public Vector3(float[] arr)
	{
		this(arr[0],arr[1],arr[2]);
	}
	
	/**
	 * native vector + vector.
	 *
	 * @param v the vector (result written back into this)
	 * @param otherV the other vector
	 */
	private native void add(float[] v, float[] otherV);
	
	/**
	 * native vector + skalar.
	 *
	 * @param v the vector (result written back into this)
	 * @param s the skalar
	 */
	private native void add(float[] v, float s);
	
	/**
	 * Adds another Vector3 to this.
	 *
	 * @param other the Vector3 to add
	 * @return this
	 */
	public Vector3 add(Vector3 other)
	{
		add(v, other.v);
		return this;
	}
	
	/**
	 * Adds a skalar to this.
	 *
	 * @param s the skalar to add
	 * @return this
	 */
	public Vector3 add(float s)
	{
		add(v, s);
		return this;
	}
	
	/**
	 * Adds two Vector3.
	 *
	 * @param a the first Vector3
	 * @param b the second Vector3
	 * @return a new Vector3, set to a+b
	 */
	public static Vector3 add(Vector3 a, Vector3 b)
	{
		return new Vector3(a).add(b);
	}
	
	/**
	 * Adds a skalar to a Vector3.
	 *
	 * @param a the first Vector3
	 * @param s the skalar
	 * @return a new Vector3, set to a+b
	 */
	public static Vector3 add(Vector3 a, float s)
	{
		return new Vector3(a).add(s);
	}
	
	/**
	 * native vector - vector.
	 *
	 * @param v the vector (result written back into this)
	 * @param otherV the other vector
	 */
	private native void subtract(float[] v, float[] otherV);
	
	/**
	 * native vector - skalar.
	 *
	 * @param v the vector (result written back into this)
	 * @param s the s
	 */
	private native void subtract(float[] v, float s);
	
	/**
	 * native vector - skalar.
	 *
	 * @param other the other
	 * @return the vector3
	 */
	public Vector3 subtract(Vector3 other)
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
	public Vector3 subtract(float s)
	{
		subtract(v,s);
		return this;
	}
	
	/**
	 * Subtract a Vector3 from another.
	 *
	 * @param a the first Vector3
	 * @param b the second Vector3
	 * @return a new Vector3, set to a-b
	 */
	public static Vector3 subtract(Vector3 a, Vector3 b)
	{
		return new Vector3(a).subtract(b);
	}
	
	/**
	 * Subtract a skalar from a Vector3.
	 *
	 * @param a the Vector3
	 * @param s the skalar
	 * @return a new Vector3, set to a-s
	 */
	public static Vector3 subtract(Vector3 a, float s)
	{
		return new Vector3(a).subtract(s);
	}
	
	/**
	 * native vector + vector.
	 *
	 * @param v the vector (result written back into this)
	 * @param otherV the other vector
	 */
	private native void multiply(float[] v, float[] otherV);
	
	/**
	 * native vector + skalar.
	 *
	 * @param v the vector (result written back into this)
	 * @param s the skalar
	 */
	private native void multiply(float[] v, float s);
	
	/**
	 * Multiply this with another Vector3.
	 *
	 * @param other the Vector3 to multiply
	 * @return this
	 */
	public Vector3 multiply(Vector3 other)
	{
		multiply(v, other.v);
		return this;
	}

	/**
	 * Multiplies this with a factor.
	 *
	 * @param s the factor
	 * @return this
	 */
	public Vector3 multiply(float s)
	{
		multiply(v,s);
		return this;
	}
	
	/**
	 * Multiplies two Vector3.
	 *
	 * @param a the first Vector3
	 * @param b the second Vector3
	 * @return a new Vector3, set to a*b
	 */
	public static Vector3 multiply(Vector3 a, Vector3 b)
	{
		return new Vector3(a).multiply(b);
	}
	
	/**
	 * Multiplies a Vector3 with a factor.
	 *
	 * @param a the Vector3
	 * @param b the factor
	 * @return a new Vector3, set to a*b
	 */
	public static Vector3 multiply(Vector3 a, float b)
	{
		return new Vector3(a).multiply(b);
	}
	
	/**
	 * native vector / vector.
	 *
	 * @param v the vector (result written back into this)
	 * @param otherV the other vector
	 */
	private native void divide(float[] v, float[] otherV);
	
	/**
	 * native vector / skalar.
	 *
	 * @param v the vector (result written back into this)
	 * @param s ths skalar
	 */
	private native void divide(float[] v, float s);
	
	/**
	 * Divides this by another Vector3.
	 *
	 * @param other the Vector3
	 * @return this
	 */
	public Vector3 divide(Vector3 other)
	{
		divide(v, other.v);
		return this;
	}
	
	/**
	 * Divides this by a divisor.
	 *
	 * @param s the divisor
	 * @return this
	 */
	public Vector3 divide(float s)
	{
		divide(v, s);
		return this;
	}
	
	/**
	 * Divides two Vector3.
	 *
	 * @param a the first Vector3
	 * @param b the second Vector3
	 * @return a new Vector3, set to a/b
	 */
	public static Vector3 divide(Vector3 a, Vector3 b)
	{
		return new Vector3(a).divide(b);
	}
	
	/**
	 * Divides a Vector3 by a skalar.
	 *
	 * @param a the Vector3
	 * @param s the skalar
	 * @return a new Vector3, set to a/s
	 */
	public static Vector3 divide(Vector3 a, float s)
	{
		return new Vector3(a).divide(s);
	}

	/**
	 * native Dot product.
	 *
	 * @param a the first vector
	 * @param b the second vector
	 * @return the dot product
	 */
	private static native float dotProduct(float[] a, float[] b);
	
	/**
	 * Calculates the dot product of two Vector3.
	 *
	 * @param a the first Vector3
	 * @param b the second Vector3
	 * @return the dot product of a and b
	 */
	public static float dotProduct (Vector3 a, Vector3 b)
	{
		return dotProduct(a.v, b.v);
	}

	/**
	 * native cross product.
	 *
	 * @param a the first vector
	 * @param b the second vector
	 * @param result the result
	 */
	private static native void crossProduct(float[] a, float[] b, float[] result);
	
	/**
	 * Calculates the cross product of two Vector3 and writes it back into a parameter.
	 *
	 * @param a the first Vector3
	 * @param b the second Vector3
	 * @param result the result will be written into this parameter
	 */
	public static void crossProduct(Vector3 a, Vector3 b, Vector3 result)
	{
		crossProduct(a.v, b.v, result.v);
	}
	
	/**
	 * Calculates the cross product of two Vector3.
	 *
	 * @param a the first Vector3
	 * @param b the second Vector3
	 * @return a new Vector3, set to a x b
	 */
	public static Vector3 crossProduct (Vector3 a, Vector3 b)
	{
		Vector3 result = new Vector3();
		crossProduct(a, b, result);
		return result;
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
	public Vector3 normalize()
	{
		normalize(v);
		return this;
	}
	
	/**
	 * Normalizes a Vector3.
	 *
	 * @param a the Vector3
	 * @return a new Vector3, set to a, normalized
	 */
	public static Vector3 normalize(Vector3 a)
	{
		return new Vector3(a).normalize();
	}
	
	/**
	 * native gets the angle between two vectors
	 *
	 * @param a the first vector
	 * @param b the second vector
	 * @return the angle
	 */
	private native float getAngle(float[] a, float[] b);
	
	/**
	 * Gets the angle between this and another Vector3.
	 *
	 * @param other the second Vector3
	 * @return the angle between this and other
	 */
	public float getAngle(Vector3 other)
	{
		return getAngle(v, other.v);
	}
	
	/**
	 * Gets the angle between two Vector3.
	 *
	 * @param a the first Vector3
	 * @param b the second Vector3
	 * @return the angle between a and b
	 */
	public static float getAngle(Vector3 a, Vector3 b)
	{
		return a.getAngle(b);
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
	public Vector3 invert()
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
		return "(" + v[0] + "," + v[1] + "," + v[2] + ")";
	}
}
