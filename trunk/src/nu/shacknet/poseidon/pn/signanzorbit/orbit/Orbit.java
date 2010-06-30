package nu.shacknet.poseidon.pn.signanzorbit.orbit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import nu.shacknet.poseidon.pn.signanzorbit.config.Config;
import nu.shacknet.poseidon.pn.signanzorbit.math.Constants;
import nu.shacknet.poseidon.pn.signanzorbit.math.Ellipse;
import nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44;
import nu.shacknet.poseidon.pn.signanzorbit.math.Vector3;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.LogManager;


/**
 * The Class Orbit represents a elliptic motion.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class Orbit extends Motion
{
	
	/** The iteration speed. */
	private float 	speed;
	/** The ellipse parameter */
	private float  	u;
	/** The step size */
	private float  	step;
 
	/** The direction vector rotation difference resp. to the final angle */
	private float  	dirVecRotationDiff;
	/** The stepsize for the direction vector rotation */
	private float  	dirVecRotationDiffStep;	
				
	/** The scaling speed for the main axis */
	private float  	aScalingMorphSpeed;
	/** The scaling speed for the secondary axis */
	private float  	bScalingMorphSpeed;
	
	/** The final main axis factor */
	private float  	centerDiffFactor;
	/** The main axis scaling stepsize */
	private float  	centerDiffStep;
	/** The current main axis factor */
	private float  	centerDiff;
	
	/** The final sec. axis factor */
	private float  	directionDiffFactor;
	/** The sec. axis scaling stepsize */
	private float  	directionDiffStep;
	/** The current sec. axis factor */
	private float  	directionDiff;
	
	/** The main axis cap/limit */
	private float  	centerVecCap;
	/** The sec. axis cap/limit */
	private float  	directionVecCap;
					

	/** The new reference speed */
	private float  	newSpeed;
	/** The stepsize for speed morphing */
	private float  	speedMorphStep;
	/** The difference between current and reference value */
	private float  	speedMorphDifference;
	/** The dynamic morph speed factor */
	private float  	dynamicMorphSpeed;
	
	/** The position on the ellipse. */
	public final Vector3 position;
	
	/** The entity position is equivalent to the initial position (u=0). */
	public final Vector3 entityPos;
	
	/** The center position of the orbit. */
	public final Vector3 centerPos;
					
	/** Represents the vector from the initial position (u=0) to the center of the orbit (a - axis). */
	public final Vector3 centerVec;

	/** The direction vector encodes the iteration direction (cw,ccw) and the b axis. */
	public final Vector3 directionVec;

	private final Vector3 normalVec;
	
	/** temp vector for the approximation of the current direction */
	private final Vector3 currtDirApproximation;
	/** temp direction vector */
	private final Vector3 tempDirectionVec;
	/** temp center vector */
	private final Vector3 tempCenterVec;
	
	/** This vector stores the original direction/sec. vector during axis scaling */
	private final Vector3 refDirectionVec;
	/** This vector stores the original center/main vector during axis scaling */
	private final Vector3 refCenterVec;

	/** The generated transformation matrix. */
	private Matrix44 transform;

	/** The rotation matrix for the direction/sec vector */
	private Matrix44 dirRotationMatrix;
	
	/** The basic orientation of the object. */
	private final Matrix44  basicOrientation;

	/** The satellite transformation of the object. */
	private SatelliteTransformation satTrans;
	
	/** The mathematical basis of the orbit. */
	private Ellipse ellipse;

	/**
	 * Instantiates a new orbit.
	 */
	public Orbit()
	{	
		//init
		position = new Vector3();
		entityPos = new Vector3();
		centerPos = new Vector3();
		normalVec = new Vector3();
		
		centerVec = new Vector3();
		directionVec = new Vector3();
		currtDirApproximation = new Vector3();
		tempDirectionVec = new Vector3();
		tempCenterVec = new Vector3();
		refDirectionVec = new Vector3();
		refCenterVec = new Vector3();
		
		transform = new Matrix44();
		basicOrientation = new Matrix44();
		dirRotationMatrix = new Matrix44();

		u = 0;
		
		dirVecRotationDiff = 0;
		dirVecRotationDiffStep = 0;
		
		speedMorphDifference = 0;
	}

	/**
	 * Instantiates a new orbit.
	 * 
	 * @param entityPos
	 *            the entity position is equivalent to the initial position (u=0)
	 * @param centerPos
	 *            the center position of the orbit
	 * @param directionVec
	 *            the direction vector encodes the iteration direction (cw,ccw) and the b axis
	 * @param speed
	 *            the iteration speed
	 * @param basicOrientation
	 *            the basic orientation of the object
	 */
	public Orbit(	Vector3 entityPos,Vector3 centerPos,
					Vector3 directionVec,
					float speed, 
					Matrix44 basicOrientation
				)
	{
		
		//init fields
		this();
		
		this.speed = speed;
		this.newSpeed = speed;
		
		this.entityPos.set(entityPos);
		this.centerPos.set(centerPos);
		this.directionVec.set(directionVec);
		
		this.centerVec.set(this.entityPos);
		this.centerVec.subtract(this.centerPos);

		if(basicOrientation!=null)
			this.basicOrientation.copy(basicOrientation);

		Vector3.crossProduct(this.centerVec, this.directionVec, this.normalVec);
		this.normalVec.normalize();
		
		/**
		 * INFO: the given vectors are used per reference!
		 */
		this.ellipse = new Ellipse(this.centerPos,this.centerVec,this.directionVec);
		
		//stepsize relative so perimeter
		this.step = Constants.TWOPI / ellipse.perimeter;
		
		//check the size and speed of the orbit
		limitUniverse();
		
	}
	
	/**
	 * Iterate over the orbit.
	 *
	 * @param dt delta time between frames for a frame-independent motion
	 */
	public void update(float dt)
	{	
		//inc parameter
		u+=(speed*step*dt);
		if(u>=Constants.TWOPI)
			u-=Constants.TWOPI;

		//update sat transformation
		if(satTrans!=null)
			satTrans.update(dt);
		
		//update the morphing for the direction vector length
		updateSpeedMorphing(dt);
		
		//update the morphing for the axis scaling
		updateAxisScaling(dt);
		
		//update the rotation of the direction vector
		updateDirVecRotation(dt);
		
		//calc position on ellipse - build transformation matrix
		evaluatePos();
		
//		//check the limitations
//		if(this.centerVec.length()<Config.FORCEFIELD_CENTERLENGTH_LIMIT || 
//		          this.directionVec.length()<Config.FORCEFIELD_DIRLENGTH_LIMIT)
//			Log.d(LevelActivity.TAG,"OUT centerVec="+centerVec.length()+" directionVec="+directionVec.length()+" speed="+speed);

	}
	
	/**
	 * Update the rotation of the direction vector
	 *
	 * @param dt delta time between frames for a frame-independent transition
	 */
	private void updateDirVecRotation(float dt)
	{
		if(dirVecRotationDiff > 0)
		{
			float dirVecRotationDiffIteration = dirVecRotationDiffStep*dt;
			
			if(dirVecRotationDiff-Math.abs(dirVecRotationDiffIteration)<0)
				dirVecRotationDiffIteration = Math.signum(dirVecRotationDiffStep)*dirVecRotationDiff;
			
			dirRotationMatrix.setRotate(normalVec, dirVecRotationDiffIteration);
			dirRotationMatrix.transformPoint(directionVec);
			//for axis scaling..
			dirRotationMatrix.transformPoint(refDirectionVec);
			dirVecRotationDiff -= (float)Math.abs(dirVecRotationDiffIteration);
			
			//Log.d(LevelActivity.TAG,"ROTATE ANGLE - diff="+dirVecRotationDiff+" iteration(degree)="+Math.toDegrees(dirVecRotationDiffIteration));
		}
	}
	
	/**
	 * Update the linear transition of the speed.
	 *
	 * @param dt delta time between frames for a frame-independent transition
	 */
	private void updateSpeedMorphing(float dt)
	{
		if(speedMorphDifference>0)
		{
			float speedMorphIteration = speedMorphStep * dt * dynamicMorphSpeed;
			
			if(speedMorphStep<0)
			{
				if(speed + speedMorphIteration < newSpeed){
					//speedMorphIteration = newSpeed - speed;
					speed=newSpeed;
				}else{
					speed+=speedMorphIteration;
				}
			}else{
				if(speed + speedMorphIteration > newSpeed){
					//speedMorphIteration = newSpeed - speed;
					speed=newSpeed;
				}else{
					speed+=speedMorphIteration;
				}
			}
			
			speedMorphDifference -= Math.abs(speedMorphIteration);
			//avoid numerical problems
			speedMorphDifference = (speedMorphDifference<0.01f)?0:speedMorphDifference;
			
			//Log.d(LevelActivity.TAG,"MORPH SPEED - diff="+speedMorphDifference+" step="+speedMorphStep+" iteration="+speedMorphIteration+" curr="+speed+" newspeed="+newSpeed + " dynSpeed="+dynamicMorphSpeed);
		}
	}
	
	/**
	 * Update the linear axis scaling.
	 *
	 * @param dt delta time between frames for a frame-independent transition
	 */
	private void updateAxisScaling(float dt)
	{
		boolean doCenterVecScaling = false;
		boolean doDirectionVecScaling = false;
		float centerDiffIteration = 0;
		float directionDiffIteration = 0;
		
		if(centerDiffStep<0){
			if(centerDiffFactor<centerDiff){
				doCenterVecScaling = true;
				centerDiffIteration = centerDiffStep * dt * aScalingMorphSpeed;
				
				if(centerDiff+centerDiffIteration<centerDiffFactor)
					centerDiffIteration = centerDiffFactor-centerDiff;
			}
		}else{
			if(centerDiffFactor>centerDiff){
				doCenterVecScaling = true;
				centerDiffIteration = centerDiffStep * dt * aScalingMorphSpeed;
			
				if(centerDiff+centerDiffIteration>centerDiffFactor)
					centerDiffIteration = centerDiffFactor-centerDiff;
			}
		}

		if(directionDiffStep<0){
			if(directionDiffFactor<directionDiff){
				doDirectionVecScaling = true;
				directionDiffIteration = directionDiffStep * dt * bScalingMorphSpeed;
				
				if(directionDiff+directionDiffIteration<directionDiffFactor)
					directionDiffIteration = directionDiffFactor-directionDiff;
				
			}
		}else{
			if(directionDiffFactor>directionDiff){
				doDirectionVecScaling = true;
				directionDiffIteration = directionDiffStep * dt * bScalingMorphSpeed;
				
				if(directionDiff+directionDiffIteration>directionDiffFactor)
					directionDiffIteration = directionDiffFactor-directionDiff;

			}
		}
		
		if(doCenterVecScaling){
			centerDiff += centerDiffIteration;
			
			centerVec.set(refCenterVec);
			centerVec.multiply(centerDiff);
			
			//Log.d(LevelActivity.TAG,"AXIS CENTER SCALING - centerDiff "+centerDiff +" centerDiffFactor" +centerDiffFactor+" centerDiffIteration "+centerDiffIteration );
		}
		
		if(doDirectionVecScaling){
			directionDiff += directionDiffIteration;
			
			directionVec.set(refDirectionVec);
			directionVec.multiply(directionDiff);
			
			//Log.d(LevelActivity.TAG,"AXIS DIR SCALING - directionDiff "+directionDiff +" directionDiffFactor" +directionDiffFactor+" directionDiffIteration "+directionDiffIteration );
		}
		
		if(doCenterVecScaling||doDirectionVecScaling){
			//change the stepsize relative to the new orbitsize
			updateStepSize();
		}

	}
	
	/**
	 * Rotate direction vector using the normal vector as rotationaxis
	 *
	 * @param angle the rotation angle in rad
	 */
	public void rotateDirectionVec(float angle){
		
		this.dirVecRotationDiff = angle;
		
		/**
		 * the rotation of the direction vector should be as fast as
		 * the satellite needs for a quarter of the orbit
		 */
		this.dirVecRotationDiffStep = 0.25f;
		if(Math.signum(angle)!=0)
			this.dirVecRotationDiffStep *= Math.signum(angle);

		
		this.dirVecRotationDiff = (float)Math.abs(this.dirVecRotationDiff);
	}
	
	/**
	 * Evaluate the current position along the ellipse.
	 */
	private void evaluatePos()
	{
		//evaluate ellipse
		position.set(ellipse.getPoint(u));
				
		//reset transformation to basic orientation
		transform.set(basicOrientation.m);
						
		//object transformation
		if(satTrans!=null)
			transform.mult(satTrans.getTransform());
		
		//transformation of the ellipsoid
		transform.addTranslate(position.v[0],position.v[1],position.v[2]);
	}



	/**
	 * Provides a linear scaling of the orbit`s axis.
	 *
	 * @param aAxisFactor the scaling factor for the a axis - center vector (1 = no change)
	 * @param bAxisFactor the scaling factor for the b axis - direction vector (1 = no change)
	 * @param aAxisMorphSpeed the transition speed
	 */
	public void morphAxisScale(float aAxisFactor,float bAxisFactor,float aAxisMorphSpeed,float bAxisMorphSpeed)
	{
		aScalingMorphSpeed = aAxisMorphSpeed;
		bScalingMorphSpeed = bAxisMorphSpeed;
		refCenterVec.set(centerVec);
		refDirectionVec.set(directionVec);
		centerDiffFactor = aAxisFactor;
		centerDiffStep = (centerDiffFactor-1)/100;
		centerDiff = 1;
		
		directionDiffFactor = bAxisFactor;
		directionDiffStep = (directionDiffFactor-1)/100;
		directionDiff = 1;
	}
	
	/**
	 * Limit universe according to the limits set in the @see Config.
	 */
	public void limitUniverse()
	{
		this.centerVecCap = 1;
		this.directionVecCap = 1;
		
		if(this.centerVec.length()>Config.UNIVERSE_CENTERLENGTH_LIMIT || 
				this.centerVec.length()<Config.FORCEFIELD_CENTERLENGTH_LIMIT)
			this.centerVecCap = Config.FORCEFIELD_NEW_CENTERLENGTH/this.centerVec.length();
		
		if(this.directionVec.length()>Config.UNIVERSE_DIRLENGTH_LIMIT ||
				this.directionVec.length()<Config.FORCEFIELD_DIRLENGTH_LIMIT)
			this.directionVecCap = Config.FORCEFIELD_NEW_DIRLENGTH/this.directionVec.length();
		
		//Log.d(LevelActivity.TAG,"LIMIT - centerVecCap ="+centerVecCap+" length="+this.centerVec.length() +" directionVecCap="+directionVecCap+" length="+this.directionVec.length()+" speed="+this.speed +" newspeed="+this.newSpeed);
		
		if((this.centerVecCap!=1 || this.directionVecCap != 1)){
			morphAxisScale(	centerVecCap, 
							directionVecCap, 
							Config.FORCEFIELD_CENTERLENGTH_SCALESPEED,
							Config.FORCEFIELD_DIRLENGTH_SCALESPEED);
		}
		
		if(this.newSpeed > Config.UNIVERSE_SPEED_LIMIT){
			this.dynamicMorphSpeed = 1;
			morphSpeed(Config.UNIVERSE_SPEED_LIMIT, Config.ORBIT_DYNAMIC_SPEEDFACTOR);
		}
	}
	
	/**
	 * Change the step size relative to the new perimeter
	 * <code>oldPerimeter</code> and <code>oldStepSize</code>
	 * has to be set before calling this method.
	 */
	private void updateStepSize()
	{
		//!!!oldPerimeter and oldStepSize has to be already set
		
		this.ellipse.calcPerimeter();
		//stepsize should be the same relative to the new perimeter
		this.step = Constants.TWOPI/ellipse.perimeter;
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion#morph(nu.shacknet.poseidon.pn.signanzorbit.math.Vector3)
	 */
	public void morph(Vector3 pushVec)
	{
		
		LogManager.d("MORPH OPERATION "+pushVec.toString());
		
		//stop scale morphing
		directionDiffFactor = directionDiff;
		centerDiffFactor = centerDiff;
		
		//check if there is dir vec rotation in progress
		boolean dirVecRotationInProgress = false;
		if(dirVecRotationDiff!=0)
			dirVecRotationInProgress = true;
			
		//approx current direction vec
		currtDirApproximation.set(ellipse.getPoint(u+step));
		currtDirApproximation.subtract(position);
		currtDirApproximation.normalize();
		
		//new speed evaluation
		tempDirectionVec.set(currtDirApproximation);
		tempDirectionVec.multiply(speed);
		tempDirectionVec.add(pushVec);
		newSpeed = tempDirectionVec.length();
		
		//new direction
		currtDirApproximation.multiply(this.directionVec.length());
		currtDirApproximation.add(pushVec);

		//update ellipse
		this.entityPos.set(this.position);
		this.directionVec.set(currtDirApproximation);
		this.centerVec.set(this.entityPos);
		this.centerVec.subtract(this.centerPos);
		
		//cap size and speed of orbit
		//-> get the new speed value
		limitUniverse();
				
		//continue dir vec rotation
		if(dirVecRotationInProgress){
			tempDirectionVec.set(directionVec).normalize();
			tempCenterVec.set(centerVec).normalize();
			float angle = Vector3.getAngle(tempCenterVec, tempDirectionVec);
			if(Float.isNaN(angle))
				angle = 0;
			else
				angle = Constants.PIHALF-angle;
			
			rotateDirectionVec(angle);
			LogManager.d("DIRVECROT UPDATE angle="+(float)Math.toDegrees(dirVecRotationDiff));
		}
		
		
		//change the stepsize relative to the new orbitsize
		updateStepSize();
		
		//restart ellipse
		this.u = 0;

		//add dynamic speed boost :)
		if(newSpeed<speed){
			morphSpeed(this.speed - (pushVec.length()*Config.ORBIT_SPEEDMORPH_PUSHVECFACTOR),
						(newSpeed/speed)*Config.ORBIT_DYNAMIC_SPEEDFACTOR);
		}else{
			morphSpeed(	this.speed+ (pushVec.length()*Config.ORBIT_SPEEDMORPH_PUSHVECFACTOR),
						(speed/newSpeed)*Config.ORBIT_DYNAMIC_SPEEDFACTOR);
		}
	}
	
	
	/**
	 * Morph the motion speed
	 *
	 * @param newSpeed the new reference speed
	 * @param dynamicMorphSpeed should be the ratio between the current and the
	 * new speed
	 */
	public void morphSpeed(float newSpeed,float dynamicMorphSpeed){

		//CAP
		float tempMaxSpeedLimit = Config.UNIVERSE_SPEED_LIMIT*Config.ORBIT_TEMP_MAXSPEED_FACTOR;
		if(newSpeed>tempMaxSpeedLimit)
			newSpeed = tempMaxSpeedLimit;
		if(newSpeed<=0)
			newSpeed = Config.ORBIT_MIN_SPEED;
		
		//speedmorphing parameters
		this.newSpeed = newSpeed;
		this.dynamicMorphSpeed = dynamicMorphSpeed;
		this.speedMorphStep = (this.newSpeed-this.speed)/100;
		this.speedMorphDifference = Math.abs(newSpeed - speed);
		
		
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion#persist(java.io.DataOutputStream)
	 */
	@Override
	public void persist(DataOutputStream dos) throws IOException
	{
		dos.writeFloat(speed);
		dos.writeFloat(u);
		dos.writeFloat(step);

		//scale morphing
		dos.writeFloat(aScalingMorphSpeed);
		dos.writeFloat(bScalingMorphSpeed);
		dos.writeFloat(centerDiffFactor);
		dos.writeFloat(centerDiffStep);
		dos.writeFloat(centerDiff);
		dos.writeFloat(directionDiffFactor);
		dos.writeFloat(directionDiffStep);
		dos.writeFloat(directionDiff);
		refCenterVec.persist(dos);
		refDirectionVec.persist(dos);
		
		//speed morphing
		dos.writeFloat(newSpeed); 
		dos.writeFloat(speedMorphStep);
		dos.writeFloat(speedMorphDifference); 
		dos.writeFloat(dynamicMorphSpeed);
	
		//dir vec rotation
		dos.writeFloat(dirVecRotationDiff);
		dos.writeFloat(dirVecRotationDiffStep);
		
		centerPos.persist(dos);
		centerVec.persist(dos);
		directionVec.persist(dos);
		basicOrientation.persist(dos);
		
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
		speed = dis.readFloat();
		u = dis.readFloat();
		step = dis.readFloat();

		//scale morphing
		aScalingMorphSpeed = dis.readFloat();
		bScalingMorphSpeed = dis.readFloat();
		centerDiffFactor = dis.readFloat();
		centerDiffStep = dis.readFloat();
		centerDiff = dis.readFloat();
		directionDiffFactor = dis.readFloat();
		directionDiffStep = dis.readFloat();
		directionDiff = dis.readFloat();
		refCenterVec.restore(dis);
		refDirectionVec.restore(dis);		
	
		//speed morphing
		newSpeed  = dis.readFloat(); 
		speedMorphStep = dis.readFloat();
		speedMorphDifference = dis.readFloat(); 
		dynamicMorphSpeed = dis.readFloat();

		//dir vec rotation
		dirVecRotationDiff = dis.readFloat(); 
		dirVecRotationDiffStep = dis.readFloat(); 
		
		centerPos.restore(dis);
		centerVec.restore(dis);
		directionVec.restore(dis);
		basicOrientation.restore(dis);
		
		isInsidePlanet = dis.readBoolean();
		filterPlanetColl = dis.readBoolean();
		playedCollSound = dis.readBoolean();
		
		if(dis.readBoolean()){
			String className = dis.readUTF();
			satTrans = SatelliteTransformation.restore(dis,className);
		}
		
		ellipse = new Ellipse(centerPos,centerVec,directionVec);
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
		//approx current direction vec
		currtDirApproximation.set(ellipse.getPoint(u+step));
		currtDirApproximation.subtract(position);
		return currtDirApproximation;
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
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion#setSatTrans(nu.shacknet.poseidon.pn.signanzorbit.orbit.SatelliteTransformation)
	 */
	public void setSatTrans(SatelliteTransformation satTrans) {
		this.satTrans = satTrans;
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion#getTransform()
	 */
	public Matrix44 getTransform() {
		return transform;
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
