package nu.shacknet.poseidon.pn.signanzorbit.math;

import java.util.ArrayList;

/**
 * The Class Sphere.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class Sphere
{
	
	/** The center. */
	public final Vector3 center;
	
	/** The radius. */
	public float radius;
	
	/**
	 * Instantiates a new sphere with zero radius.
	 */
	public Sphere()
	{
		this(new Vector3(0,0,0), 0);
	}
	
	/**
	 * Copy constructor.
	 *
	 * @param other the other
	 */
	public Sphere(Sphere other)
	{
		this(new Vector3(other.center), other.radius);
	}
	
	/**
	 * Instantiates a new sphere.
	 *
	 * @param center the center
	 * @param radius the radius as a Vector3
	 */
	public Sphere(Vector3 center, Vector3 radius)
	{
		this(center, radius.length());
	}
	
	/**
	 * Instantiates a new sphere.
	 *
	 * @param center the center
	 * @param radius the radius
	 */
	public Sphere(Vector3 center, float radius)
	{
		this.center = center;
		this.radius = radius;
	}
	
	/**
	 * copy method.
	 *
	 * @param other the other
	 */
	public void set(Sphere other)
	{
		this.center.set(other.center);
		this.radius = other.radius;
	}
	
	/**
	 * Checks if is point inside.
	 *
	 * @param center the center
	 * @param radius the radius
	 * @param point the point
	 * @return true, if point is inside
	 */
	private native boolean isPointInside(float[] center, float radius, float[] point);
	
	/**
	 * Checks if is point inside.
	 *
	 * @param point the point
	 * @return true, if is point inside
	 */
	public boolean isPointInside(Vector3 point)
	{
		return isPointInside(center.v, radius, point.v);
	}
	
	/**
	 * Sets the point set.
	 * uses the centroid as center
	 * 
	 * @param vertices the vertices
	 * @param center the calculated center is written back into this
	 * @return the calculated radius
	 */
	private native float setPointSet(float[] vertices, float[] center);
	
	/**
	 * Calculates this sphere from a set of Vector3s.
	 *
	 * @param vertices the point set
	 */
	public void setPointSet(ArrayList<Vector3> vertices)
	{
		int size = vertices.size();
		float[] verticesFloats = new float[size*3];
		for(int i=0; i<size; i++)
		{
			Vector3 v = vertices.get(i);
			for(int j=0; j<3; j++)
				verticesFloats[3*i + j] = v.v[j];
		}
		radius = setPointSet(verticesFloats, center.v);
	}
	
	/**
	 * Sets the sphere set.
	 *
	 * @param centers the centers
	 * @param radii the radii
	 * @param center the calculated center is written back into this
	 * @return the calculated radius
	 */
	private native float setSphereSet(float[] centers, float[] radii, float[] center);
	
	/**
	 * Sets the sphere set.
	 *
	 * @param sphereSet the new sphere set
	 */
	public void setSphereSet(ArrayList<Sphere> sphereSet)
	{
		int size = sphereSet.size();
		
		if(size == 1)
		{
			center.set(sphereSet.get(0).center);
			radius = sphereSet.get(0).radius;
			return;
		}
		
		float[] centers = new float[3*size];
		float[] radii = new float[size];
		for(int i=0; i<size; i++)
		{
			Sphere s = sphereSet.get(i);
			for(int j=0; j<3; j++)
				centers[3*i + j] = s.center.v[j];
			radii[i] = s.radius;
		}
		
		radius = setSphereSet(centers, radii, center.v);
	}
}
