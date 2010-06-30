package nu.shacknet.poseidon.pn.signanzorbit.orbit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44;
import nu.shacknet.poseidon.pn.signanzorbit.math.Vector3;
import nu.shacknet.poseidon.pn.signanzorbit.util.Persistable;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.LogManager;



/**
 * This class represents an object motion
 * Subclasses of Motion *need* a default constructor!
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public abstract class Motion implements Persistable
{	
	
	/** flag used to determine if the object is inside the planet */
	boolean isInsidePlanet = false;
	
	/** flag used to avoid collisions of satellites */
	boolean filterPlanetColl = false;
	
	/** flag used to play the collision sound only once */
	boolean playedCollSound = false;
	
	/** The collision counter */
	int collCount = 0;
	
	/**
	 * Sets the collision counter.
	 * @param newCollCount the new collision count
	 */
	public void setCollCount(int newCollCount){
		collCount = newCollCount;
	}
	
	/**
	 * Gets the collision counter.
	 * @return the collision counter
	 */
	public int getCollCount(){
		return collCount;
	}
	
	/**
	 * Do the next iteration step 
	 * @param dt delta time between frames for a frame-independent motion
	 */
	public abstract void update(float dt);
	
	/**
	 * Gets the generated transformation matrix 
	 * @return the generated transformation matrix 
	 */
	public abstract Matrix44 getTransform();
	
	/**
	 * Sets the transformation matrix 
	 * @param transform the new transformation matrix
	 */
	public abstract void setTransform(Matrix44 transform);
	
	/**
	 * Gets the satellite transformation
	 * @return the satellite transformation
	 */
	public abstract SatelliteTransformation getSatTrans();
	
	/**
	 * Sets the satellite transformation
	 * @param satTrans the new satellite transformation
	 */
	public abstract void setSatTrans(SatelliteTransformation satTrans);
	
	/**
	 * Get an approximation for the direction vector at the current position
	 * @return approximation for the direction vector
	 */
	public abstract Vector3 getCurrDirectionVec();
	
	/**
	 * Gets the motion speed
	 * @return the speed of the motion
	 */
	public abstract float getSpeed();
	
	/**
	 * Sets the motion speed
	 * @param newSpeed the new speed
	 */
	public abstract void setSpeed(float newSpeed);
	
	
	/**
	 * Dynamic change of the motion by the given push vector
	 * @param pushVec the change of the motion
	 */
	public abstract void morph(Vector3 pushVec);
	
	/**
	 * Checks if is inside planet.
	 * @return true, if is inside planet
	 */
	public boolean isInsidePlanet(){
		return isInsidePlanet;		
	}
	
	/**
	 * Sets the inside planet flag
	 * @param isInsidePlanet the satellite is in/outside
	 */
	public void setInsidePlanet(boolean isInsidePlanet){
		this.isInsidePlanet = isInsidePlanet;
	}
	
	/**
	 * Gets the flag state used to avoid collisions of satellites.
	 * @return the flag used to avoid collisions of satellites
	 */
	public boolean getFilterPlanetColl(){
		return filterPlanetColl;		
	}
	
	/**
	 * Sets the flag used to avoid collisions of satellites with the planet.
	 * @param filterPlanetColl the new state for filtering collisions
	 */
	public void setFilterPlanetColl(boolean filterPlanetColl){
		this.filterPlanetColl = filterPlanetColl;		
	}
	
	/**
	 * Gets the state of the flag - used to play the collision sound only once.
	 * @return state if the sound was already played or not
	 */
	public boolean getPlayedCollSound(){
		return playedCollSound;		
	}

	/**
	 * Sets the state of the flag - used to play the collision sound only once.
	 * @param playedCollSound state if the sound was already played or not
	 */
	public void setPlayedCollSound(boolean playedCollSound){
		this.playedCollSound = playedCollSound;		
	}
	
	/**
	 * Gets the basic orientation of the satellite
	 * @return the basic orientation matrix
	 */
	public abstract Matrix44 getBasicOrientation();
	
	/**
	 * Sets the basic orientation of the satellite
	 * @param basicOrientation the new basic orientation matrix
	 */
	public abstract void setBasicOrientation(Matrix44 basicOrientation);
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.util.Persistable#persist(java.io.DataOutputStream)
	 */
	public abstract void persist(DataOutputStream dos) throws IOException;
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.util.Persistable#restore(java.io.DataInputStream)
	 */
	public abstract void restore(DataInputStream dis) throws IOException;

	/**
	 * Restore a Motion
	 * @param dis the data input stream
	 * @param className the name of the class that should get restored
	 * @return the restored motion
	 */
	public static Motion restore(DataInputStream dis, String className)
	{
		try
		{
			Class<?> c = Class.forName(className);
			Motion m = (Motion)c.newInstance();
			m.restore(dis);
			return m;
		}
		catch (Throwable t)
		{
			LogManager.e("Could not restore Motion (no default constructor?)", t);
		}
		return null;
	}
}
