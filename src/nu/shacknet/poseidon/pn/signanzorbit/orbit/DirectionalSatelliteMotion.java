package nu.shacknet.poseidon.pn.signanzorbit.orbit;

import nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44;
import nu.shacknet.poseidon.pn.signanzorbit.math.Vector3;

/**
 * The Class DirectionalSatelliteMotion represents a directed linear 
 * satellite motion.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class DirectionalSatelliteMotion extends DirectionalMotion
{
	
	/**
	 * Instantiates a new directional satellite motion.
	 */
	public DirectionalSatelliteMotion()
	{
		super();
	}
	
	/**
	 * Instantiates a new directional satellite motion.
	 *
	 * @param startPos the initial position of the motion
	 * @param directionVec the direction of the movement
	 * @param speed the motion speed
	 * @param basicOrientation the basic orientation of the object
	 */
	public DirectionalSatelliteMotion(Vector3 startPos,Vector3 directionVec,float speed,Matrix44 basicOrientation)
	{
		super(startPos,directionVec,speed,basicOrientation);
	}
}
