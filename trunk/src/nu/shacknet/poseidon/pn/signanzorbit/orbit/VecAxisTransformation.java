package nu.shacknet.poseidon.pn.signanzorbit.orbit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import nu.shacknet.poseidon.pn.signanzorbit.math.Constants;
import nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44;
import nu.shacknet.poseidon.pn.signanzorbit.math.Vector3;


/**
 * The Class VecAxisTransformation represents a satellite
 * transformation around a vector axis.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class VecAxisTransformation extends SatelliteTransformation{

	/** The rotation step size. */
	private float qvStep;
	
	/** The current rotation value */
	private float qvCurr;
	
	/** The rotation angle. */
	public float qv;
	
	/** The transformation speed. */
	public float speed;

	/** The transformation matrix. */
	private final Matrix44 transform;
	
	/** The basic orientation of the object. */
	private final Matrix44 basicOrientation;
	
	/** The axis. */
	public final Vector3 axis;

	/**
	 * Instantiates a new vector axis transformation.
	 */
	public VecAxisTransformation() 
	{
		this.transform = new Matrix44();
		this.basicOrientation = new Matrix44();
		this.axis = new Vector3();
	}
	
	/**
	 * Instantiates a new vector axis transformation.
	 *
	 * @param axis the axis used for the transformation
	 * @param qv the angle
	 * @param speed the transformation speed
	 * @param basicOrientation the basic orientation of the object
	 */
	public VecAxisTransformation(Vector3 axis, float qv, float speed, Matrix44 basicOrientation) 
	{
		this();
		
		this.qv = (float)Math.toRadians(qv);
		this.qvCurr = 0;
		this.speed = speed;
		
		this.axis.set(axis);

		if(basicOrientation!=null)
			this.basicOrientation.copy(basicOrientation);
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.SatelliteTransformation#update(float)
	 */
	public void update(float dt)
	{
		qvCurr += dt*qv*speed;
	
		if(qvCurr>=Constants.TWOPI)
			qvCurr-=Constants.TWOPI;
		
		
		transform.setIdentity();
		transform.mult(basicOrientation);
		transform.addRotate(axis,qvCurr);

		
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.SatelliteTransformation#getTransform()
	 */
	public Matrix44 getTransform() {
		return transform;
	}	
	
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.SatelliteTransformation#persist(java.io.DataOutputStream)
	 */
	public void persist(DataOutputStream dos) throws IOException
	{
		dos.writeFloat(qv);
		dos.writeFloat(qvStep);
		dos.writeFloat(qvCurr);
		dos.writeFloat(speed);
		axis.persist(dos);
		basicOrientation.persist(dos);
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.SatelliteTransformation#restore(java.io.DataInputStream)
	 */
	public void restore(DataInputStream dis) throws IOException
	{
		this.qv = dis.readFloat();
		this.qvStep = dis.readFloat();
		this.qvCurr = dis.readFloat();
		this.speed = dis.readFloat();
		axis.restore(dis);
		basicOrientation.restore(dis);
	}

	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.SatelliteTransformation#getBasicOrientation()
	 */
	@Override
	public Matrix44 getBasicOrientation() {
		return basicOrientation;
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.SatelliteTransformation#setBasicOrientation(nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44)
	 */
	@Override
	public void setBasicOrientation(Matrix44 basicOrientation) {
		this.qvCurr = 0;
		this.basicOrientation.copy(basicOrientation);
	}

	/**
	 * Sets a new angle for the transformation. The new angle is limited by the
	 * given <code>ratio</code> regarding the current speed and is used to 
	 * avoid a too high rotation speed
	 * @param angle the new transformation angle
	 * @param ratio the limiting ratio
	 */
	public void setAngle(float angle,float ratio) {
		this.qv = angle;
		//check the speed/rotation ratio
		if((speed/qv)<ratio)
			this.qv = this.speed/ratio;
		
		//LogManager.d("ratio="+(speed/qv) + " speed="+speed+" qv="+qv);
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.SatelliteTransformation#reset()
	 */
	@Override
	public void reset() {
		this.qvCurr = 0;
	}
}
