package nu.shacknet.poseidon.pn.signanzorbit.util.managers;

import java.util.ArrayList;
import java.util.Random;

import nu.shacknet.poseidon.pn.signanzorbit.config.Config;
import nu.shacknet.poseidon.pn.signanzorbit.math.Constants;
import nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44;
import nu.shacknet.poseidon.pn.signanzorbit.math.Vector3;
import nu.shacknet.poseidon.pn.signanzorbit.orbit.DirectionalMotion;
import nu.shacknet.poseidon.pn.signanzorbit.orbit.DirectionalSatelliteMotion;
import nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion;
import nu.shacknet.poseidon.pn.signanzorbit.orbit.Movable;
import nu.shacknet.poseidon.pn.signanzorbit.orbit.Orbit;
import nu.shacknet.poseidon.pn.signanzorbit.orbit.SatelliteTransformation;
import nu.shacknet.poseidon.pn.signanzorbit.orbit.VecAxisTransformation;
import nu.shacknet.poseidon.pn.signanzorbit.scene.Scene;
import nu.shacknet.poseidon.pn.signanzorbit.scene.SceneEntity;


/**
 * The MotionManager handles the linking between objects 
 * (entities) and their motion
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class MotionManager {
	
	/** The list containing the movable obj. with motions */
	private final ArrayList<Movable> list;
	
	/** Manager Singleton */
	public static final MotionManager instance = new MotionManager();
	
	/** Temp vector for the sat. transform axis. */
	private final Vector3 	satTransformAxis;
	/** Temp var for the direction vector. */
	private final Vector3 	tempDirectionVec;
	/** Temp var for the center vector. */
	private final Vector3 	tempCenterVec;
	/** Temp var for the push vector. */
	private final Vector3 	tempPushVec;
	/** Temp var for the force direction vector. */
	private final Vector3 	tempForceDirectionVec;
	/** Temp var for the deflaction vector. */
	private final Vector3 	deflactionDirVec;
	/** Temp var for the basic orientation matrix of an object */
	private final Matrix44 tempBasicOrientation;
	
	/**
	 * Instantiates a new motion manager.
	 */
	private MotionManager()
	{
		this.list = new ArrayList<Movable>();
		this.satTransformAxis = new Vector3();
		this.tempDirectionVec = new Vector3();
		this.tempCenterVec = new Vector3();
		this.tempForceDirectionVec = new Vector3();
		this.tempPushVec = new Vector3();
		this.tempBasicOrientation = new Matrix44();
		this.deflactionDirVec = new Vector3();
	}
	
	/**
	 * Register and link a movable object/entity to a motion
	 * @param motion the motion of the object
	 * @param entity the object
	 */
	public void addMotion(Motion motion,Movable entity)
	{
		int index = list.indexOf(entity);
		if(index==-1){
			entity.setMotion(motion);	
			if(motion.getSatTrans()==null)
				motion.setSatTrans(new VecAxisTransformation(Constants.Y_AXIS,(float)Math.PI,5,null));
			motion.setTransform(entity.getTransformation());
			list.add(entity);
		}
	}
	
	
	/**
	 * Change the motion for a given entity
	 * @param motion the new motion
	 * @param entity the entity
	 */
	public void setMotion(Motion motion,Movable entity)
	{
		int index = list.indexOf(entity);
		if(index==-1){
			addMotion(motion,entity);
		}else{
			Motion oldMotion = entity.getMotion();
			entity.setMotion(motion);	
			motion.setTransform(entity.getTransformation());
			motion.setSatTrans(oldMotion.getSatTrans());
		}
	}
	
	/**
	 * Transfer the motion between movable entities - <code>oldMoveable</code> 
	 * has no motion after the transfer!
	 *
	 * @param oldMovable references the motion to transfer
	 * @param newMovable transfer the motion to this entity
	 */
	public void transferMotion(Movable oldMovable, Movable newMovable)
	{
		Motion motion = oldMovable.getMotion();
		newMovable.setMotion(motion);
		oldMovable.setMotion(null);
		motion.setBasicOrientation(newMovable.getBasicOrientation());
		motion.setTransform(newMovable.getTransformation());
		list.remove(oldMovable);
		list.add(newMovable);
	}
	
	/**
	 * Apply selection/push force to the given entity
	 *
	 * @param entity the entity on which the force should be applyed 
	 * @param pushVec the force/push vector
	 */
	public void applySelectionForce(Movable entity, Vector3 pushVec)
	{
		Motion motion = entity.getMotion();
		
		//check for a change to directional motion
		if(pushVec.length()>= Config.MIN_STRENGTH_FOR_DIRECTIONAL){
				
			//determine aiming center
			Movable aimEntity = CollisionManager.instance.getAutoAimEntity();
			Vector3 aimCenter = null;
			
			if(aimEntity!=null)
				aimCenter = aimEntity.getBoundingSphereWorld().center;
			else
				aimCenter = Config.UNIVERSE_CENTER;
			
			tempForceDirectionVec.set(aimCenter);
			tempForceDirectionVec.subtract(entity.getCurrentPosition());		
			
			float motionSpeed = pushVec.length()*Config.SELECTION_FORCE_FACTOR;
			
			LogManager.d("SELECTION push force="+motionSpeed);
			
			if(motion instanceof DirectionalMotion){
				DirectionalMotion dirMotion = (DirectionalMotion)motion;
				dirMotion.reconfigMotion(	entity.getCurrentPosition(),
											tempForceDirectionVec,
											motionSpeed,
											motion.getBasicOrientation());
			}else{
				DirectionalSatelliteMotion dirMotion =  
					new DirectionalSatelliteMotion(	entity.getCurrentPosition(),
													tempForceDirectionVec,
													motionSpeed,
													motion.getBasicOrientation());
				//exchange motion
				setMotion(dirMotion, entity);
			}
			
		}else{
			motion.morph(pushVec);
		}
	}
	
	/**
	 * Gets the motion of an entity
	 *
	 * @param index the index in the list
	 * @return the linked motion
	 */
	public Movable getMotion(int index)
	{
		return list.get(index);
	}
	
	/**
	 * Gets the motion of an entity
	 *
	 * @param entity the entity reference
	 * @return the linked motion
	 */
	public Movable getMotion(Movable entity)
	{
		int index = list.indexOf(entity);
		if(index>=0)
			return list.get(index);
		
		return null;		
	}
	
	
	/**
	 * Update all registered motions (next iteration step)
	 * @param dt the delta time between frames for a frame-independent motion
	 */
	public void updateMotion(float dt)
	{
		Movable tempEntity;
		
		for(int i=0; i<list.size();i++)
		{
			tempEntity = list.get(i);
			tempEntity.getMotion().update(dt);
			
			//check the outer limits
			checkUniverseLimits(tempEntity);
		}
	}
	
	
	/**
	 * Check the universe limits and correct the motion parameter if necessary
	 * @param entity contains the motion to check
	 */
	private void checkUniverseLimits(Movable entity)
	{
		Motion motion = entity.getMotion();

		//check only for directional motions
		if(motion instanceof DirectionalSatelliteMotion){
			if(entity.getCurrentPosition().length()>Config.UNIVERSE_CENTERLENGTH_LIMIT){
				//change to orbit motion
				//LogManager.d("UNIVERSE LIMIT="+entity.getCurrentPosition().length());
				transformDirMotionInOrbit(entity);
			}
		}else{
			if(isPlanetPart(entity)){
				if(!entity.isDisabled()){
					if(entity.getCurrentPosition().length()>Config.PLANETPART_CULL_DISTANCE){
						LogManager.d("CULLING ENTITY:"+entity.getName());
						entity.setDisabled(true);
						removeMotion(entity);
					}
				}
			}	
		}
		
	}
	
	/**
	 * Convert a directional motion into an orbit
	 * @param obj entity that contains the motion to transform
	 */
	public void transformDirMotionInOrbit(Movable obj){
		
		if(obj.getMotion() instanceof DirectionalMotion){
			
			LogManager.d("TRANSFORM in ORBIT");
			
			DirectionalMotion oldDirMotion = (DirectionalMotion)obj.getMotion();
			
			//set to the actual direction and normalize
			deflactionDirVec.set(obj.getMotion().getCurrDirectionVec()).normalize();
			// * centerVec * 4
			deflactionDirVec.multiply(obj.getCurrentPosition().length()*Config.DIRORBITTRANSFORM_DIRVEC_FACTOR);
			// + small deflaction for cross prod
			deflactionDirVec.add(Constants.DUMMY_VARIATION_VEC);
									
			Orbit newOrbit = new Orbit(	obj.getCurrentPosition(),
										Config.UNIVERSE_CENTER,
										deflactionDirVec,
										oldDirMotion.getSpeed(),
										oldDirMotion.getBasicOrientation());

			tempCenterVec.set(newOrbit.centerVec).normalize();
			tempDirectionVec.set(newOrbit.directionVec).normalize();
			float angle = Vector3.getAngle(tempCenterVec, tempDirectionVec);

			LogManager.d("DIR TO ORBIT TRANSFORM - angle="+((float)Math.toDegrees(angle)));
		
			//dir = center 
			if(Float.isNaN(angle))
				angle = 0;
			
			newOrbit.rotateDirectionVec(Constants.PIHALF-angle);
			setMotion(newOrbit,obj);
		}
	}
	
	/**
	 * Clear the list of movable objects and their motions
	 */
	public void reset()
	{
		list.clear();
	}
	
	/**
	 * Change the satellite transformation
	 * @param entity object holding the satellite transformation
	 * @param directionVec the direction vector at the current position
	 * @param pushVec the representing change of the transformation
	 * @param speedRotationRatio the speed/rotation ratio for speed limiting
	 */
	public void changeSatelliteTransformation(	Movable entity,Vector3 directionVec,
												Vector3 pushVec, float speedRotationRatio)
	{
		//generate new rotation axis
		Vector3.crossProduct(directionVec, pushVec, satTransformAxis);
		satTransformAxis.normalize();
		
		
		SatelliteTransformation currSatTransform = (VecAxisTransformation)entity.getMotion().getSatTrans();
		VecAxisTransformation vecSatTransform;
		
		if(currSatTransform!=null && currSatTransform instanceof VecAxisTransformation)
		{
			vecSatTransform = (VecAxisTransformation)currSatTransform;
			tempBasicOrientation.copy(vecSatTransform.getTransform());
			
			//set current transformation (rotation) as basic orientation
			vecSatTransform.setBasicOrientation(tempBasicOrientation);
			//set new rotation axis
			vecSatTransform.axis.set(satTransformAxis);
	
			tempDirectionVec.set(directionVec).normalize();
			tempPushVec.set(pushVec).normalize();
		
			//set the new rotation angle
			vecSatTransform.setAngle(Vector3.getAngle(tempDirectionVec, tempPushVec),speedRotationRatio);
			
			//LogManager.d("satAngle="+vecSatTransform.qv);
		}
	}
	
	
	/**
	 * Generate random orbits for each satellite in the scene
	 *
	 * @param scene the hole scene
	 * @param minSpeed the minimal speed of the satellites
	 * @param maxSpeed the maximal speed of the satellites
	 * @param minQx the minimal x - axis rotation
	 * @param maxQx the maximal x - axis rotation
	 * @param minQz the minimal z - axis rotation
	 * @param maxQz the maximal z - axis rotation
	 * @param minDistance the minimal distance of the satellites
	 * @param maxDistance the maximal distance of the satellites
	 * @param minDistanceRatio the minimal distance ratio
	 * @param maxDistanceRatio the maximal distance ratio
	 */
	public void generateRandomOrbits(	Scene scene,
										float minSpeed,float maxSpeed,
										float minQx,float maxQx,
										float minQz,float maxQz,
										float minDistance,float maxDistance,
										float minDistanceRatio, float maxDistanceRatio)
	{
		Matrix44 rotation = new Matrix44();
		SceneEntity entity = null;
		Random rand = new Random();
		Vector3 a = new Vector3();
		Vector3 b = new Vector3();
		Vector3 center = Config.UNIVERSE_CENTER;
		Vector3 rotationAxis = new Vector3();
		Orbit generatedOrbit = null;
		
		int orbitCounter = 0;
		
		VecAxisTransformation satTransform = null;
		
		for(int i=0;i<scene.sceneEntities.size();i++){
			entity = scene.sceneEntities.get(i);
			if(entity.getName().startsWith(Config.SATELLITE_PREFIX)){
				
				//generate the orthonormal axis for the ellipse
				a.v[0] = ((float)rand.nextDouble()*(maxDistance-minDistance) + minDistance);
				a.v[0] = (rand.nextBoolean())? -a.v[0] : a.v[0];
				
				/** generate a similar value for b over the <code>distanceRatio</code> */
				b.v[2] = a.v[0] *((float)rand.nextDouble()*(maxDistanceRatio-minDistanceRatio) + minDistanceRatio);
				b.v[2] = (rand.nextBoolean())? -b.v[2]: b.v[2];
				
//				LogManager.d(" a.x="+a.x+" b.z="+b.z + " rand="+((float)rand.nextDouble()*(maxDistanceRatio-minDistanceRatio) + minDistanceRatio));
				
				rotation.setIdentity();
				rotation.addRotateY((float)rand.nextDouble()*Constants.TWOPI);
				rotation.addRotateX((float)rand.nextDouble()*(maxQx - minQx) + minQx);
				rotation.addRotateZ((float)rand.nextDouble()*(maxQz - minQz) + minQz);
				rotation.transformPoint(a);
				rotation.transformPoint(b);

//				LogManager.d(" 	qx="+(float)rand.nextDouble()*(maxQx - minQx) + minQx
//										+ " qz="+(float)rand.nextDouble()*(maxQz - minQz) + minQz);
							
				generatedOrbit = new Orbit(	a,center,b,
											(float)rand.nextDouble()*maxSpeed + minSpeed,
											entity.getBasicOrientation());
								
				//generate random satellite transformation
				rotationAxis.v[0] = (float)rand.nextDouble();
				rotationAxis.v[1] = (float)rand.nextDouble();
				rotationAxis.v[2] = (float)rand.nextDouble();
				rotationAxis.normalize();
				
				satTransform = new VecAxisTransformation(	rotationAxis,
															1,
															(float)rand.nextDouble() * 5 + 1, //empiric values :) 
															null );
				
				satTransform.setAngle((float)rand.nextDouble()+0.1f, Config.INTERSATELLITE_SPEEDROTA_RATIO);
				
				generatedOrbit.setSatTrans(satTransform);
				addMotion(generatedOrbit,entity);
				
				orbitCounter++;
			}
		}
		LogManager.d(orbitCounter+" RANDOM SAT ORBITS GENERATED");
	}
	
	/**
	 * Removes the motion of an entity
	 * @param entity references the motion to remove
	 */
	public void removeMotion(Movable entity){
		entity.setMotion(null);
		list.remove(entity);
	}
	
	/**
	 * Checks if the given entity is part of the planet
	 * @param entity the entity to check
	 * @return true, if it is part of the planet
	 */
	public boolean isPlanetPart(Movable entity){
		if(entity!=null)
			return entity.getName().endsWith(Config.PLANETPART_SUFFIX);
			
		return false;
	}
}
