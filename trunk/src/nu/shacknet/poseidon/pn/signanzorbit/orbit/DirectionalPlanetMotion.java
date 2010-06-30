package nu.shacknet.poseidon.pn.signanzorbit.orbit;

import nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44;
import nu.shacknet.poseidon.pn.signanzorbit.math.Vector3;

/**
 * The Class DirectionalPlanetMotion represents a directed linear planet motion.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class DirectionalPlanetMotion extends DirectionalMotion
{
	
	/**
	 * Instantiates a new directional planet motion.
	 */
	public DirectionalPlanetMotion()
	{
		super();
	}
	
	/**
	 * Instantiates a new directional planet motion.
	 *
	 * @param startPos the initial position of the motion
	 * @param directionVec the direction of the movement
	 * @param speed the motion speed
	 * @param basicOrientation the basic orientation of the object
	 */
	public DirectionalPlanetMotion(Vector3 startPos,Vector3 directionVec,float speed,Matrix44 basicOrientation)
	{
		super(startPos,directionVec,speed,basicOrientation);
	}
}
