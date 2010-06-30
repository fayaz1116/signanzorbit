package nu.shacknet.poseidon.pn.signanzorbit.math;

/**
 * The Class Constants.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class Constants
{

	/** The Constant TWOPI = Two times PI. */
	public static final float TWOPI = (float)(Math.PI*2);
	/** PI/2 */
	public static final float PIHALF = (float)(Math.PI/2);
	/** The X Axis */
	public static final Vector3 X_AXIS = new Vector3(1,0,0); 
	/** The Y Axis. */
	public static final Vector3 Y_AXIS = new Vector3(0,1,0); 
	/** The Z Axis */
	public static final Vector3 Z_AXIS = new Vector3(0,0,1);
	/** The Zero vector */
	public static final Vector3 ZERO_VEC = new Vector3(0,0,0);
	/** A dummy deflaction vector */
	public static final Vector3 DUMMY_VARIATION_VEC = new Vector3(0.01f,0.01f,0.01f);
	/** A dummy initialization vector */
	public static final Vector3 DUMMY_INIT_VEC = new Vector3(0,0,0.1f);
}
