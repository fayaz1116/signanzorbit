package nu.shacknet.poseidon.pn.signanzorbit.camera;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import nu.shacknet.poseidon.pn.signanzorbit.math.Constants;
import nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44;
import nu.shacknet.poseidon.pn.signanzorbit.math.Vector3;
import nu.shacknet.poseidon.pn.signanzorbit.util.Persistable;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.OGLManager;


//static imports
import static android.opengl.GLES10.*;

/**
 * The Class Camera represents a spherical camera model.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class Camera implements Persistable
{
	
	/** The ogl manager. */
	private final OGLManager oglManager = OGLManager.instance;
	
	/** The view position. */
	public final Vector3 viewPosition;
	
	/** The eye/cam position. */
	public final Vector3 eyePosition;
	
	/** The up vector. */
	public final Vector3 upVector;
	
	/** The inverse view vector (view position -> eye position). */
	public final Vector3 inverseViewVector;
	
	/** The right vector. */
	public final Vector3 rightVector;
	
	/** The max distance. */
	private float 	minAltitude,maxAltitude, azimuth, altitude, 
					currentAzimuth, currentAltitude, motionFactor,
					minDistance,maxDistance;

	/** The current distance. */
	private float distance,currentDistance;
	
	/** The last position. */
	private final float[] lastPosition;
	
	/** The qy. */
	private final Matrix44 qx,qy;
	
	//temp vectors
	/** The view vec xz projection. */
	private Vector3 qyAxis,tempInverseViewVec,viewVecXZProjection;
	
	/**
	 * Instantiates a new camera.
	 *
	 * @param distance the distance to the view position
	 * @param minAltitude the minimal altitude angle
	 * @param maxAltitude the maximal altitude angle
	 * @param initAzimuth the initial azimuth angle
	 * @param initAltitude the initial altitude angle
	 * @param motionFactor the motion speed factor
	 * @param minDistance the minimal distance
	 * @param maxDistance the maximal distance
	 */
	public Camera(	float distance,float minAltitude,float maxAltitude,
					float initAzimuth, float initAltitude, float motionFactor,
					float minDistance, float maxDistance)
	{

		this.distance = distance;
		this.currentDistance = distance;
		this.azimuth=initAzimuth;
		this.altitude=initAltitude;
		this.currentAzimuth=0;
		this.currentAltitude=0;
		this.minAltitude = minAltitude;
		this.maxAltitude = maxAltitude;
		this.motionFactor = motionFactor;
		this.minDistance = minDistance;
		this.maxDistance = maxDistance;
		
		this.qx = new Matrix44();
		this.qy = new Matrix44();
		this.qyAxis = new Vector3();
		this.viewVecXZProjection = new Vector3();
		this.tempInverseViewVec = new Vector3();
		this.rightVector = new Vector3();
		
		//init position
		eyePosition = new Vector3(0.0f,0.0f,distance);
		lastPosition = new float[2];
		lastPosition[0] = eyePosition.v[0];
		lastPosition[1] = eyePosition.v[1];
		viewPosition =  new Vector3(0.0f,0.0f,0.0f);
		inverseViewVector = Vector3.subtract(eyePosition,viewPosition);
		inverseViewVector.normalize();

		//right = forward x up = up x -forward
		Vector3.crossProduct(Constants.Y_AXIS, inverseViewVector, rightVector);
		rightVector.normalize();
		
		upVector = Vector3.crossProduct(inverseViewVector, rightVector);
		upVector.normalize();
	}
	
	/**
	 * Process the camera transformation.
	 */
	public void look()
	{
		Matrix44 modelview = oglManager.getModelview();
		
		oglManager.gluLookAt(eyePosition, inverseViewVector, rightVector, upVector, modelview);
		
		glLoadMatrixf(modelview.getArray16(), 0);
	}
	
	
	/**
	 * Change the camera position by detecting the relative horizontal and
	 * vertical movement of the input.
	 *
	 * @param xDiff the relative horizontal movement
	 * @param yDiff the relative vertical movement
	 */
	public void setMouseDiff(float xDiff, float yDiff)
	{
		//set new desired angle 
		azimuth += motionFactor * 15 * xDiff;
		altitude += motionFactor * 15 * yDiff;
		
		if(altitude>=maxAltitude)
			altitude = maxAltitude;
		if(altitude<=minAltitude)
			altitude = minAltitude;
	}
	
	/**
	 * Change the camera position by detecting the relative horizontal and
	 * vertical movement of the input.
	 *
	 * @param x the absolute x value
	 * @param y the absolute y value
	 */
	public void setMousePosition(float x, float y)
	{
		//calc difference to last position
		float xDiff = lastPosition[0]-x;
		float yDiff = lastPosition[1]-y;
		//store last position
		lastPosition[0] = x;
		lastPosition[1] = y;
		setMouseDiff(xDiff, yDiff);
	}
	
	
	/**
	 * Gets the distance between eye and viewpoint.
	 *
	 * @return the distance between eye and viewpoint
	 */
	public float getDistance()
	{
		return distance;
	}

	/**
	 * Sets the distance between eye and viewpoint limited by the values given
	 * by instantiation.
	 *
	 * @param newDistance the new distance between eye and viewpoint
	 */
	public void setDistance(float newDistance)
	{
		if(newDistance > maxDistance)
		{
			this.distance = maxDistance;
			return;
		}
		if(newDistance < minDistance)
		{
			this.distance = minDistance;
			return;
		}

		this.distance = newDistance;
	}
	

	/**
	 * Update the camera position and iteration of the camera motion.
	 *
	 * @param viewPos the view position
	 * @param dt the delta time between frames
	 */
	public void updatePosition(Vector3 viewPos, float dt)
	{
		//set view position
		viewPosition.set(viewPos);
		
		//iterate 
		float azimuthDiff = azimuth - currentAzimuth;
		float altitudeDiff = altitude - currentAltitude;
		float distanceDiff = distance - currentDistance;

		//evaluate only on change
		if(azimuthDiff!=0||altitudeDiff!=0||distanceDiff!=0)
		{
			
			// calc the iteration step
			azimuthDiff *= motionFactor * 30.0f;
			altitudeDiff *= motionFactor * 30.0f;
			distanceDiff *= motionFactor * 30.0f;
	
			//reset
			qx.setIdentity();
			//rotate around the y axis
			qx.setRotate(Constants.Y_AXIS,(float)Math.toRadians(azimuthDiff));
			qx.transformPoint(inverseViewVector);
	
			// create the axis for the vertical rotation (altitude)
			viewVecXZProjection.set(inverseViewVector);
			viewVecXZProjection.v[1] = 0.0f;
			viewVecXZProjection.normalize();
	
			// normal of the XZ projection and the y axis = rotation axis
			Vector3.crossProduct(Constants.Y_AXIS,viewVecXZProjection,qyAxis);
			qyAxis.normalize();
	
			// rotatate view vec (grad)
			qy.setIdentity();
			qy.setRotate(qyAxis,(float)Math.toRadians(altitudeDiff));
	
			qy.transformPoint(inverseViewVector);
			inverseViewVector.normalize();
			
			//right = forward x up = up x -forward
			Vector3.crossProduct(Constants.Y_AXIS, inverseViewVector, rightVector);
			rightVector.normalize();
			
			Vector3.crossProduct(inverseViewVector, rightVector, upVector);
			upVector.normalize();
	
			// update current angle data
			currentAzimuth += azimuthDiff;
			currentAltitude += altitudeDiff;
			currentDistance += distanceDiff;
			
			
			//eyePosition = viewPosition + inverseViewVec * currentDistance
			eyePosition.set(viewPosition);
			tempInverseViewVec.set(inverseViewVector);
			eyePosition.add(tempInverseViewVec.multiply(currentDistance));
		}
	}
	
	/**
	 * Sets the last position for the relative motion measurement.
	 *
	 * @param x the last absolute x position
	 * @param y the last absolute y position
	 */
	public void setLastPosition(int x, int y)
	{
		this.lastPosition[0] = x;
		this.lastPosition[1] = y;
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.util.Persistable#persist(java.io.DataOutputStream)
	 */
	public void persist(DataOutputStream dos) throws IOException
	{
		dos.writeFloat(azimuth);
		dos.writeFloat(altitude);
		dos.writeFloat(distance);
		
		dos.writeFloat(currentAzimuth);
		dos.writeFloat(currentAltitude);
		dos.writeFloat(currentDistance);
		
		dos.writeFloat(minAltitude);
		dos.writeFloat(maxAltitude);
		dos.writeFloat(minDistance);
		dos.writeFloat(maxDistance);
		
		dos.writeFloat(motionFactor);
		
		eyePosition.persist(dos);
		inverseViewVector.persist(dos);
		rightVector.persist(dos);
		upVector.persist(dos);
		viewPosition.persist(dos);
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.util.Persistable#restore(java.io.DataInputStream)
	 */
	public void restore(DataInputStream dis) throws IOException
	{
		azimuth = dis.readFloat();
		altitude = dis.readFloat();
		distance = dis.readFloat();
		
		currentAzimuth = dis.readFloat();
		currentAltitude = dis.readFloat();
		currentDistance = dis.readFloat();
		
		minAltitude = dis.readFloat();
		maxAltitude = dis.readFloat();
		minDistance = dis.readFloat();
		maxDistance = dis.readFloat();
		
		motionFactor = dis.readFloat();
		
		eyePosition.restore(dis);
		inverseViewVector.restore(dis);
		rightVector.restore(dis);
		upVector.restore(dis);
		viewPosition.restore(dis);
	}
}
