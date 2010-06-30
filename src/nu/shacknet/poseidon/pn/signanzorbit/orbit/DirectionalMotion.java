package nu.shacknet.poseidon.pn.signanzorbit.orbit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import nu.shacknet.poseidon.pn.signanzorbit.config.Config;
import nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44;
import nu.shacknet.poseidon.pn.signanzorbit.math.Vector3;


/**
 * The Class DirectionalMotion represents a directed linear motion.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class DirectionalMotion extends Motion
{
	
	/** The direction of the movement */
	private final Vector3 directionVec;
	
	/** The current position of the object */
	public final Vector3 position;
	
	private final Vector3 tempDirectionVec;
	
	/** The motion speed. */
	private float speed;
	
	/** The satellite transformation */
	private SatelliteTransformation satTrans;
	
	/** The transformation matrix */
	private Matrix44 transform;
	
	/** The basic orientation of the object */
	private Matrix44 basicOrientation;
	
	/**
	 * Instantiates a new directional motion.
	 *
	 * @param startPos the initial position of the motion
	 * @param directionVec the direction of the movement
	 * @param speed the motion speed
	 * @param basicOrientation the basic orientation of the object
	 */
	public DirectionalMotion(Vector3 startPos,Vector3 directionVec,float speed,Matrix44 basicOrientation)
	{
		//init
		this();
		this.directionVec.set(directionVec.normalize());
		this.position.set(startPos);
		this.speed = speed;
		
		if(basicOrientation!=null)
			this.basicOrientation.copy(basicOrientation);
	}
	
	/**
	 * Reuse the directional motion via reconfiguration
	 *
	 * @param startPos the initial position of the motion
	 * @param directionVec the direction of the movement
	 * @param speed the motion speed
	 * @param basicOrientation the basic orientation of the object
	 */
	public void reconfigMotion(Vector3 startPos,Vector3 directionVec,float speed,Matrix44 basicOrientation){
		this.directionVec.set(directionVec.normalize());
		this.position.set(startPos);
		this.speed = speed;
		
		if(basicOrientation!=null)
			this.basicOrientation.copy(basicOrientation);
	}
	
	/**
	 * Instantiates a new directional motion.
	 */
	protected DirectionalMotion()
	{
		//init
		this.transform = new Matrix44();
		this.basicOrientation = new Matrix44();
		this.directionVec = new Vector3();
		this.position = new Vector3();
		this.tempDirectionVec = new Vector3();
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion#update(float)
	 */
	public void update(float dt)
	{
		tempDirectionVec.set(directionVec).normalize();
		tempDirectionVec.multiply(dt*speed);
		
		position.add(tempDirectionVec);
	
		transform.setIdentity();
		transform.mult(basicOrientation);
		
		if(satTrans!=null){
			satTrans.update(dt);
			transform.mult(satTrans.getTransform());
		}
		
		transform.addTranslate(position.v[0], position.v[1], position.v[2]);
	}

	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion#getTransform()
	 */
	public Matrix44 getTransform() {
		return transform;
	}

	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion#setSatTrans(nu.shacknet.poseidon.pn.signanzorbit.orbit.SatelliteTransformation)
	 */
	public void setSatTrans(SatelliteTransformation satTrans) {
		this.satTrans = satTrans;
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion#persist(java.io.DataOutputStream)
	 */
	@Override
	public void persist(DataOutputStream dos) throws IOException
	{
		this.directionVec.persist(dos);
		this.position.persist(dos);
		dos.writeFloat(this.speed);
		this.basicOrientation.persist(dos);
		
		dos.writeBoolean(isInsidePlanet);
		dos.writeBoolean(filterPlanetColl);
		dos.writeBoolean(playedCollSound);
		
		if(satTrans != null){
			dos.writeBoolean(true);
			dos.writeUTF(satTrans.getClass().getName());
			this.satTrans.persist(dos);
		}else
			dos.writeBoolean(false);
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion#restore(java.io.DataInputStream)
	 */
	@Override
	public void restore(DataInputStream dis) throws IOException
	{
		this.directionVec.restore(dis);
		this.position.restore(dis);
		this.speed = dis.readFloat();
		this.basicOrientation.restore(dis);
		
		this.isInsidePlanet = dis.readBoolean();
		this.filterPlanetColl = dis.readBoolean();
		this.playedCollSound = dis.readBoolean();
		
		if(dis.readBoolean()){
			String className = dis.readUTF();
			this.satTrans = SatelliteTransformation.restore(dis,className);
		}
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion#getSatTrans()
	 */
	@Override
	public SatelliteTransformation getSatTrans() {
		return satTrans;
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion#getCurrDirectionVec()
	 */
	@Override
	public Vector3 getCurrDirectionVec() {
		return directionVec;
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion#setTransform(nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44)
	 */
	@Override
	public void setTransform(Matrix44 transform) {
		this.transform = transform;
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion#getSpeed()
	 */
	@Override
	public float getSpeed() {
		return this.speed;
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion#setSpeed(float)
	 */
	@Override
	public void setSpeed(float newSpeed) {
		this.speed = newSpeed;
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion#morph(nu.shacknet.poseidon.pn.signanzorbit.math.Vector3)
	 */
	@Override
	public void morph(Vector3 pushVec) {
		this.directionVec.multiply(speed).add(pushVec);
		
		this.speed = this.directionVec.length();
		if(this.speed>Config.UNIVERSE_SPEED_LIMIT)
			this.speed = Config.UNIVERSE_SPEED_LIMIT;
		
		this.directionVec.normalize();
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion#getBasicOrientation()
	 */
	@Override
	public Matrix44 getBasicOrientation() {
		return basicOrientation;
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion#setBasicOrientation(nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44)
	 */
	@Override
	public void setBasicOrientation(Matrix44 basicOrientation){
		if(basicOrientation!=null)
			this.basicOrientation.set(basicOrientation.m);
	}
}
