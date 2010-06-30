package nu.shacknet.poseidon.pn.signanzorbit.util.managers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Vector;

import nu.shacknet.poseidon.pn.signanzorbit.config.Config;
import nu.shacknet.poseidon.pn.signanzorbit.math.Constants;
import nu.shacknet.poseidon.pn.signanzorbit.math.Vector3;
import nu.shacknet.poseidon.pn.signanzorbit.orbit.DirectionalMotion;
import nu.shacknet.poseidon.pn.signanzorbit.orbit.DirectionalPlanetMotion;
import nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion;
import nu.shacknet.poseidon.pn.signanzorbit.orbit.Movable;
import nu.shacknet.poseidon.pn.signanzorbit.orbit.Orbit;
import nu.shacknet.poseidon.pn.signanzorbit.scene.Model;
import nu.shacknet.poseidon.pn.signanzorbit.scene.Scene;
import nu.shacknet.poseidon.pn.signanzorbit.scene.SceneEntity;
import nu.shacknet.poseidon.pn.signanzorbit.util.Persistable;


/**
 * This Manager handles the selection and collisions between objects
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class CollisionManager implements Persistable{

	/** The scene reference */
	private Scene scene;
	
	/** list containing the current scene entities */
	private ArrayList<SceneEntity> entityList;
	
	/** The point on the line */
	private final Vector3 p;
	
	/** The direction vector of the line */
	private final Vector3 a;
	
	/** The point which distance should be evaluated */
	private final Vector3 q;
	
	/** The vector between p and q */
	private final Vector3 pq;
	
	/** The normal distance */
	private final Vector3 normalDistance;

	/** The distance vector between two center points. */
	private final Vector3 centerDistance;
	/** The distance vector between planet and sat. center points. */
	private final Vector3 planetCenterDistance;
	/** Temp var for the approximation of the current direction. */
	private final Vector3 objACurrDir;
	/** Temp var for the approximation of the current direction. */
	private final Vector3 objBCurrDir;
	/** Temp var for the push vector */
	private final Vector3 objAPushVec;
	/** Temp var for the push vector */
	private final Vector3 objBPushVec;
	/** Describes the planet part direction */
	private final Vector3 planetPushVec;
	/** The deflaction vector for the satellite */
	private final Vector3 morphPlanetPushVec,tempMorphPlanetPushVec;
	/** Temp var for the center vector */
	private final Vector3 toCenterVecA;
	/** Temp var for the center vector */
	private final Vector3 toCenterVecB;
		
	/** The gold planet. */
	private Movable goldPlanet;
	
	/** The skysphere. */
	private Movable skysphere;
	
	/** list containing the remaining parts of the planet -
	 * 	sorted by the distance to the planet center. */
	public Vector<Model> remainingPlanetParts;
	
	/** Singleton */
	public static CollisionManager instance = new CollisionManager();
	
	/** The comparator for sorting the remainingPlanetParts. */
	public static NearestEntityComperator comperator;
	
	/** The game manager. */
	public GameManager gameManager;
	
	/** The motion manager. */
	private MotionManager motionManager;
	
	/** The sound manager. */
	private SoundManager soundManager;
	
	/** The collision list offset / startpoint */
	private int collOffset;
	
	/** Proceed the collision up to this index */
	private int collOffsetLimit;
	
	/** Random Generator */
	private final Random rand;

	/**
	 * Instantiates a new collision manager.
	 */
	public CollisionManager()
	{
		this.scene = null;
		this.entityList = null;
		
		this.p = new Vector3();
		this.q = new Vector3();
		this.a = new Vector3();
		this.pq = new Vector3();
		this.normalDistance = new Vector3();
		
		this.centerDistance = new Vector3();
		this.planetCenterDistance = new Vector3();
		
		this.objACurrDir = new Vector3();
		this.objBCurrDir = new Vector3();
		
		this.objAPushVec = new Vector3();
		this.objBPushVec = new Vector3();
		this.planetPushVec = new Vector3();
		this.morphPlanetPushVec = new Vector3();
		this.tempMorphPlanetPushVec = new Vector3();
		
		this.toCenterVecA = new Vector3();
		this.toCenterVecB = new Vector3();

		this.remainingPlanetParts = new Vector<Model>();
		comperator = new NearestEntityComperator();
				
		this.motionManager = MotionManager.instance;
		this.soundManager = SoundManager.instance;
		
		this.rand = new Random();
	}
	
	/**
	 * Initialize the collision manager - call this method before using the manager!
	 * @param scene the scene
	 */
	public void init(Scene scene)
	{
		this.scene = scene;
		this.entityList = scene.sceneEntities;
		
		remainingPlanetParts.clear();
		
		for(int i=entityList.size()-1;i>=0;i--)
		{
			if(entityList.get(i).getName().equals(Config.PLANET_NAME)){
				goldPlanet = entityList.get(i);
				remainingPlanetParts.addAll(entityList.get(i).models);
			}
			if(entityList.get(i).getName().equals(Config.SKYSPHERE_NAME)){
				skysphere = entityList.get(i);
			}
		}
		Collections.sort(remainingPlanetParts, comperator);
		//printRemaingingPlanetParts();
		LogManager.d("PLANET BSPHERE RADIUS:"+goldPlanet.getBoundingSphereWorld().radius);
		
		this.collOffset = (int)(entityList.size()/2);
		this.collOffsetLimit = entityList.size();
	}
	
	/**
	 * Initialize the game manager
	 * @param gameManager the game manager
	 */
	public void initAndSetGameManager(GameManager gameManager)
	{
		gameManager.init(remainingPlanetParts.size());
		this.gameManager = gameManager;
	}
	
	/**
	 * Shoot a ray through the scene and detect nearest intersection with the 
	 * objects
	 *
	 * @param origin the origin of the ray
	 * @param direction the ray direction
	 * @return the nearest SceneEntity intersected by the ray
	 */
	public SceneEntity intersectRay(Vector3 origin,Vector3 direction)
	{
		a.set(direction);
		a.normalize();
		p.set(origin);
		
		//reset values
		float minDistance = Float.MAX_VALUE;
		SceneEntity nearestEntity = null;
		SceneEntity entity = null;
		
		//iterate over all scene entities and calc the hesse normal form
		for(int i=0;i<entityList.size();i++)
		{
			entity = entityList.get(i);
			
			//exclude inactive entities
			if(entity.isDisabled()||entity.equals(skysphere))
				continue;
			
			//get entity bounding sphere position
			q.set(entity.getBoundingSphereWorld().center);
			
			//calc vector from q -> p
			pq.set(p);
			pq.subtract(q);
			
			Vector3.crossProduct(pq,a,normalDistance);
			if(normalDistance.length() < (entity.getBoundingSphereWorld().radius+Config.SELECTION_BSPHERE_INCREMENT))
			{
				LogManager.d("found hit with " + entity.getName() + " distance="+normalDistance.length()+" sphereradius="+entity.getBoundingSphereWorld().radius);
				
				//only store the one with the min distance
				if(nearestEntity==null && entity.getName().startsWith(Config.SATELLITE_PREFIX))
					nearestEntity = entity;
				
				if(pq.length()<minDistance && entity.getName().startsWith(Config.SATELLITE_PREFIX));
				{
					nearestEntity = entity;
					minDistance = pq.length();
				}
			}
		}
		if(nearestEntity!=null)
			LogManager.d("ray intersected with "+nearestEntity.getName());
		else
			LogManager.d("no ray intersection found");
		return nearestEntity;
		
	}
	
	/**
	 * Detect the collision between <code>objA</code> and <code>objB</code> 
	 *
	 * @param objA the object a
	 * @param objB the object b
	 * @param penetrationDepth the depth that the objects has to penetrate 
	 * each other
	 * @param centerDistance the vector between the center of 
	 * the bounding spheres of each object
	 * @return true, if there is a collision otherwise false
	 */
	private static boolean collisionDetected(Movable objA, Movable objB, float penetrationDepth, Vector3 centerDistance)
	{
		if(centerDistance == null)
			centerDistance = new Vector3();
		
		//calc distance between center points
		centerDistance.set(objB.getBoundingSphereWorld().center);
		centerDistance.subtract(objA.getBoundingSphereWorld().center);
		
		if(centerDistance.length()+ penetrationDepth < 
					objA.getBoundingSphereWorld().radius + objB.getBoundingSphereWorld().radius)
			return true;
		
		
		return false;		
	}
	
	/**
	 * Do collision detection between all scene entities in the scene
	 */
	public void doCollisionDetection()
	{		
		int entityListSize = entityList.size();
		int listSize = remainingPlanetParts.size();
		
		Movable objA = null;
		Movable objB = null;

		boolean objAIsMoveable = true;
		boolean objBIsMoveable = true;
		
		float satSpeed = 0;
		
		if(collOffset==0){
			collOffset = (int)(entityListSize/2);
			collOffsetLimit = entityListSize;
		}else{
			collOffset = 0;
			collOffsetLimit = (int)(entityListSize/2);
		}
		
		//for each entity
		for(int i=collOffset; i<collOffsetLimit; i++) 
		{
			objA = entityList.get(i);	
			
			//only collide with active entities
			if(objA.isDisabled()||objA.equals(skysphere))
				continue;
			
			objAIsMoveable = (objA.equals(goldPlanet))?false:true;
			
			
			for(int j = i+1; j<entityListSize; j++)
			{
				objB = entityList.get(j);
				
				//only collide with active entities
				if(objB.isDisabled()||objB.equals(skysphere))
					continue;
				
				objBIsMoveable = (objB.equals(goldPlanet))?false:true;

				//check for contact
				if(collisionDetected(objA,objB,Config.COLLISION_PENETRATION_DEPTH,centerDistance))
				{
					//collision detected
					//LogManager.d("COLLISION DETECTED");
					
					//check for collision between satellite and planet
					if(!objAIsMoveable||!objBIsMoveable)
					{
						//distinguish entities
						SceneEntity planet = null;
						SceneEntity satellite = null;
						
						if(!objAIsMoveable){
							planet = (SceneEntity) objA;
							satellite = (SceneEntity) objB;
						}else{
							planet = (SceneEntity) objB;
							satellite = (SceneEntity) objA;
						}
						
						/**
						 * SPECIAL CASE
						 * planet part moves out of the planet -> avoid collision with planet
						 */
						Motion satMotion = satellite.getMotion();
						Model planetPart = null;
						//store sat speed for further processing
						satSpeed = satMotion.getSpeed();
						
						if(satMotion.getFilterPlanetColl())
							continue;
					
						//set planet entrance flag
						satMotion.setInsidePlanet(true);
						
						//find out which part of the planet got hit
						//only search in remaining planet parts
						listSize = remainingPlanetParts.size();
						for(int u = listSize-1; u >=0; u--)
						{
							planetPart = remainingPlanetParts.get(u);
									
							//check for contact
							if(collisionDetected(planetPart,satellite,Config.COLLISION_PENETRATION_DEPTH,planetCenterDistance))
							{
								if(!satMotion.getPlayedCollSound()){
									playCollSound(satellite);
									satMotion.setPlayedCollSound(true);
								}
								
								Motion planetPartMotion = planetPart.getMotion();
								
								if(planetPart.getMotion()==null)
								{
									planetPushVec.set(planetPart.getBoundingSphereWorld().center);
									planetPushVec.subtract(Config.UNIVERSE_CENTER);
									
									if(planetPushVec.length()==0)
										planetPushVec.set(Constants.DUMMY_INIT_VEC);
									
									planetPartMotion = new DirectionalPlanetMotion(	planetPart.getBoundingSphereWorld().center,
																					planetPushVec,
																					satMotion.getSpeed()*Config.PLANETCOLL_SPEED_FROM_SAT_FACTOR,
																					planetPart.getBasicOrientation());

									LogManager.d(satMotion.getSpeed());
									if(satMotion.getSpeed()<Config.MIN_SPEED_FOR_UNDAMPED_DIRECTIONAL){

										morphPlanetPushVec.set(satellite.getBoundingSphereWorld().center);
										tempMorphPlanetPushVec.set(morphPlanetPushVec);
										morphPlanetPushVec.subtract(planetPart.getBoundingSphereWorld().center);
										morphPlanetPushVec.normalize();
										centerDistance.normalize();
										
										morphPlanetPushVec.add(centerDistance).normalize();
										if(morphPlanetPushVec.length()<=0.1f){
											morphPlanetPushVec.set(tempMorphPlanetPushVec).normalize();
										}
																				
										morphPlanetPushVec.multiply(satMotion.getSpeed()*Config.PLANETPART_BOUNCE_FACTOR);
										satMotion.morph(morphPlanetPushVec);
										
										//COLLISION FILTER
										int collCount = satMotion.getCollCount();
										collCount++;
										satMotion.setCollCount(collCount);
										
										//check if there were to many collision with this satellite
										if(collCount >= Config.DAMPED_MAX_COLLISION_COUNT_FACTOR * satSpeed){
											LogManager.d("MAX COLLISION COUNT REACHED");
											satMotion.setFilterPlanetColl(true);
											satMotion.setCollCount(0);		
											//stop the collision detection for this satellite
											break;
										}
										
									}else{
										satMotion.setSpeed(satMotion.getSpeed()-Config.UNDAMPED_SPEED_DECREMENT);
										satSpeed = satMotion.getSpeed();
									}
									motionManager.addMotion(planetPartMotion,planetPart);
									planetPartMotion.setInsidePlanet(true);
									
									//LogManager.d("PLANET COLL - SAT speed="+satellite.getMotion().getSpeed()+" PLANET speed="+planetPart.getMotion().getSpeed());
									
									//report game manager
									gameManager.incScore();
								}
								
								//delete from aiming list
								remainingPlanetParts.remove(planetPart);
								
								//avoid inter-planet-part-coll
								if(remainingPlanetParts.size()>1){
									planetPartMotion.setFilterPlanetColl(true);
								}									
								
								// schedule for untie
								scene.unTie(planet, planetPart);
							}
						}
						
					}
					else
					{
						Motion objAMotion = objA.getMotion();
						Motion objBMotion = objB.getMotion();
						
						if(objAMotion==null)
						{
							
							objAMotion = new Orbit(	objA.getBoundingSphereWorld().center,
													Config.UNIVERSE_CENTER,
													Constants.DUMMY_INIT_VEC,
													0.1f,
													null);
							motionManager.addMotion(objAMotion,objA);
						}
						
	
						if(objBMotion==null)
						{
							objBMotion = new Orbit(	objB.getBoundingSphereWorld().center,
													Config.UNIVERSE_CENTER,
													Constants.DUMMY_INIT_VEC,
													0.1f,
													null);
							motionManager.addMotion(objBMotion,objB);
						}
						
						
						objACurrDir.set(objA.getMotion().getCurrDirectionVec()).normalize();
						objBCurrDir.set(objB.getMotion().getCurrDirectionVec()).normalize();
											
						//weight with current speed
						objACurrDir.multiply(objA.getMotion().getSpeed()*0.2f);
						objBCurrDir.multiply(objB.getMotion().getSpeed()*0.2f);
						
						toCenterVecA.set(centerDistance);
						toCenterVecA.normalize().multiply(-objA.getBoundingSphereWorld().radius);
						
						toCenterVecB.set(centerDistance);
						toCenterVecB.normalize().multiply(objB.getBoundingSphereWorld().radius);
						
						objAPushVec.set(objBCurrDir);
						objAPushVec.add(toCenterVecA);

						objBPushVec.set(objACurrDir);
						objBPushVec.add(toCenterVecB);

						if(objAPushVec.length()!=0){
							objA.getMotion().morph(objAPushVec);
							motionManager.changeSatelliteTransformation(objA, objACurrDir, objAPushVec,Config.INTERSATELLITE_SPEEDROTA_RATIO);
						}
						
						if(objBPushVec.length()!=0){
							objB.getMotion().morph(objBPushVec);
							motionManager.changeSatelliteTransformation(objB, objBCurrDir, objBPushVec,Config.INTERSATELLITE_SPEEDROTA_RATIO);					
						}
				}

				//END of contact detection
				}else{
					//special case: sat is not longer "inside the planet"
					Motion motion = objA.getMotion();
					float distance = objA.getCurrentPosition().length();
					
					if(motion !=null && motion.isInsidePlanet()){
				
						if(!motionManager.isPlanetPart(objA)){
							if(distance > Config.TRANSFORMATION_DISTANCE){
								
								if(motion instanceof DirectionalMotion){
									motionManager.transformDirMotionInOrbit(objA);
								}
								motion.setInsidePlanet(false);
								motion.setPlayedCollSound(false);
								motion.setFilterPlanetColl(false);
								motion.setCollCount(0);
							}
						}else{
							if(distance > goldPlanet.getBoundingSphereWorld().radius + Config.PLANETPART_REUSE_MINDISTANCE){
								motion.setInsidePlanet(false);
								motion.setFilterPlanetColl(false);
								motion.setPlayedCollSound(false);
								motion.setCollCount(0);
							}
						}						
					}
				}
				
			}
		}
	}
	
	/**
	 * Play a collision sound depending on the motion speed
	 * @param satellite the satellite with collision
	 */
	private void playCollSound(Movable satellite)
	{
		//play sound per sat - planet hit only once
		if(satellite.getMotion().getSpeed()>Config.MIN_SPEED_FOR_UNDAMPED_DIRECTIONAL)
			soundManager.playSound(Config.SOUND_HEAVYIMPACT);
		else
			soundManager.playSound(Config.SOUND_IMPACT);
	}
	
	/**
	 * Select randomly an entity from the <code>remainingPlanetParts</code> list
	 * @return a random selected entity
	 */
	public Movable getAutoAimEntity()
	{
		//printRemaingingPlanetParts();
		if(remainingPlanetParts.size()>0){	
			//pseudo random selection of next element
			int index = (int)(rand.nextFloat()*(remainingPlanetParts.size()-1));
			LogManager.d("RANDOM SELECTION INDEX:"+index);
			return remainingPlanetParts.get(index);
		}else 
			return null;
	}
	
	
	/**
	 * The Class NearestEntityComperator - Comparator used to sort the 
	 * <code>remainingPlanetParts</code> list
	 *
	 * @author Alex Druml
	 * @author Lukas Roessler
	 */
	protected class NearestEntityComperator implements Comparator<Movable>{

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Movable objA, Movable objB) {
			if(objA.getBoundingSphereWorld().center.length() <
					objB.getBoundingSphereWorld().center.length())
				return -1;
			
			if(objA.getBoundingSphereWorld().center.length() ==
					objB.getBoundingSphereWorld().center.length())
				return 0;
			
			return 1;
		}
		
	}

	/**
	 * Prints the <code>remainingPlanetParts</code> list
	 */
	private void printRemaingingPlanetParts()
	{
		LogManager.d("AUTOAIM ----");
		for(int i=0;i<remainingPlanetParts.size();i++)
			LogManager.d("AUTOAIM: i=" + i + " length=" + remainingPlanetParts.get(i).getBoundingSphereWorld().center.length());
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.util.Persistable#persist(java.io.DataOutputStream)
	 */
	@Override
	public void persist(DataOutputStream dos) throws IOException {
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.util.Persistable#restore(java.io.DataInputStream)
	 */
	@Override
	public void restore(DataInputStream dis) throws IOException {
		
		int size = remainingPlanetParts.size();
		for(int u = size-1; u >= 0; u--)
		{
			if(remainingPlanetParts.get(u).getMotion()!=null)
				remainingPlanetParts.remove(u);
		}
		Collections.sort(remainingPlanetParts, comperator);
		printRemaingingPlanetParts();
	}

}
