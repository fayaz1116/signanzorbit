package nu.shacknet.poseidon.pn.signanzorbit.orbit;

import nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44;
import nu.shacknet.poseidon.pn.signanzorbit.math.Sphere;
import nu.shacknet.poseidon.pn.signanzorbit.math.Vector3;

/**
 * The Interface Movable 
 * 
 * @author Alex Druml
 * @author Lukas Roessler
 */
public interface Movable { 
	
	/**
	 * Gets the motion.
	 *
	 * @return the motion
	 */
	public Motion getMotion(); 
	
	/**
	 * Sets the motion.
	 *
	 * @param motion the new object motion
	 */
	public void setMotion(Motion motion); 
	
	/**
	 * Gets the basic orientation of the object
	 *
	 * @return the basic orientation of the object
	 */
	public Matrix44 getBasicOrientation();
	
	/**
	 * Gets the current position.
	 *
	 * @return the current position of the object
	 */
	public Vector3 getCurrentPosition();
	
	/**
	 * Sets the transformation.
	 *
	 * @param transform the new transformation
	 */
	public void setTransformation(Matrix44 transform);
	
	/**
	 * Gets the transformation.
	 *
	 * @return the transformation
	 */
	public Matrix44 getTransformation();
	
	/**
	 * Gets the bounding sphere in world coordinates
	 *
	 * @return the bounding sphere in world coordinates
	 */
	public Sphere getBoundingSphereWorld();
	
	/**
	 * Gets the name.
	 *
	 * @return the name of the object
	 */
	public String getName();
	
	/**
	 * Checks if the object is disabled.
	 *
	 * @return true, if is disabled
	 */
	public boolean isDisabled();
	
	/**
	 * Sets the disabled flag
	 *
	 * @param disabled true, to disable the object
	 */
	public void setDisabled(boolean disabled);
	
}