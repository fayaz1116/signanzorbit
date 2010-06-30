package nu.shacknet.poseidon.pn.signanzorbit.scene;

import static android.opengl.GLES10.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import nu.shacknet.poseidon.pn.signanzorbit.config.Config;
import nu.shacknet.poseidon.pn.signanzorbit.math.AxisAlignedBox3;
import nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44;
import nu.shacknet.poseidon.pn.signanzorbit.math.Sphere;
import nu.shacknet.poseidon.pn.signanzorbit.math.Vector3;
import nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion;
import nu.shacknet.poseidon.pn.signanzorbit.orbit.Movable;
import nu.shacknet.poseidon.pn.signanzorbit.util.Persistable;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.LogManager;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.MotionManager;


/**
 * The Class SceneEntity.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class SceneEntity implements Movable,Persistable
{
	/** The name. */
	private String name;
	
	/** The transformation used by opengl. */
	private Matrix44 transformation;
	
	/** The transformation the logic thread writes to. is written to transformation in update(). */
	private Matrix44 transformation_temp;
	
	/** The basic orientation (needed for orbit). */
	private final Matrix44 basicOrientation;
	
	/** The bounding box in model space. */
	private final AxisAlignedBox3 boundingBox;
	
	/** The bounding sphere in model space. */
	private final Sphere boundingSphere;
	
	/** The bounding sphere world space, is updated in each update() call. */
	private final Sphere boundingSphereWorld;
	
	/** The motion. */
	private Motion motion;
	
	/** if this is initialized. */
	private boolean initialized;
	
	/** The models. */
	public final ArrayList<Model> models;
	
	/** The current position */
	private final Vector3 currentPos;
	
	/** A List for all the Bounding Spheres of this SceneEntity */
	private final ArrayList<Sphere> modelBoundingSpheres;
	
	/** whether this SceneEntity is disabled (not rendered, not updated) */
	private boolean disabled;
	
	/**
	 * Instantiates a new scene entity.
	 */
	public SceneEntity()
	{
		transformation = new Matrix44();
		transformation_temp = new Matrix44();
		basicOrientation = new Matrix44();
		models = new ArrayList<Model>();
		boundingBox = new AxisAlignedBox3();
		boundingSphere = new Sphere();
		boundingSphereWorld = new Sphere();
		initialized = false;
		currentPos = new Vector3();
		modelBoundingSpheres = new ArrayList<Sphere>();
		disabled = false;
	}
	
	/**
	 * Inits the Scene entity
	 */
	void init()
	{
		if(!initialized)
		{
			ArrayList<Model> models = this.models;
			int size = models.size();
			for(int i=0; i<size; i++)
				models.get(i).init();
			
			initialized = true;
		}
	}
	
	/**
	 * De init the Scene entity
	 */
	void deInit()
	{
		initialized = false;
		
		ArrayList<Model> models = this.models;
		int size = models.size();
		for(int i=0; i<size; i++)
			models.get(i).deInit();
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.util.Persistable#persist(java.io.DataOutputStream)
	 */
	public void persist(DataOutputStream dos) throws IOException
	{
		LogManager.d("writing scene entity " + name);
		ArrayList<Model> models = this.models;
		int size = models.size();
		for(int i=0; i<size; i++)
			models.get(i).persist(dos);
		transformation.persist(dos);
		dos.writeBoolean(disabled);
		
		if(motion != null)
		{
			dos.writeBoolean(true);
			String className = motion.getClass().getName();
			LogManager.d("writing motion " + className);
			dos.writeUTF(className);
			motion.persist(dos);
		}
		else
			dos.writeBoolean(false);
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.util.Persistable#restore(java.io.DataInputStream)
	 */
	public void restore(DataInputStream dis) throws IOException
	{
		LogManager.d("reading scene entity " + name);
		ArrayList<Model> models = this.models;
		int size = models.size();
		for(int i=0; i<size; i++)
			models.get(i).restore(dis);
		transformation_temp.restore(dis);
		disabled = dis.readBoolean();
		if(dis.readBoolean())
		{
			String className = dis.readUTF();
			LogManager.d("reading motion " + className);
			motion = Motion.restore(dis, className);
			if(motion != null)
				MotionManager.instance.addMotion(motion, this);
		}
		else
			motion = null;
	}
	
	/**
	 * Render.
	 *
	 * @param rendermode the rendermode
	 */
	public void render(int rendermode)
	{
		if(disabled)
			return;
		
		glPushMatrix();
		glMultMatrixf(transformation.getArray16(), 0);
		int numModels = models.size();
		for(int i=0; i<numModels; i++)
			models.get(i).render(rendermode);
		glPopMatrix();
	}
	
	/**
	 * Render bounding spheres.
	 */
	void renderBoundingSpheres(int rendermode)
	{
		if(disabled)
			return;
		
		if(Config.SHOW_MODEL_BOUNDING_SPHERES)
		{
			int numModels = models.size();
			for(int i=0; i<numModels; i++)
				models.get(i).renderBoundingSpheres(rendermode);
		}

		if(Config.SHOW_SCENEENTITY_BOUNDING_SPHERES)
		{
			Vector3 translation = boundingSphereWorld.center;
			float scale = boundingSphereWorld.radius;
			
			glPushMatrix();
			glTranslatef(translation.v[0], translation.v[1], translation.v[2]);
			glScalef(scale, scale, scale);
			Scene.SPHERE.render(rendermode);
			glPopMatrix();
		}
	}
	
	/**
	 * Update.
	 */
	public void update()
	{
		if(disabled)
			return;
		
		int numModels = models.size();
		for(int i=0; i<numModels; i++)
		{
			Model m = models.get(i);
			// Models need the SceneEntitie's transformation to transform their bounding sphere into world space
			m.update(transformation_temp);
			modelBoundingSpheres.add(m.getBoundingSphereSceneEntity());
		}

		transformation.copy(transformation_temp);

		boundingSphere.setSphereSet(modelBoundingSpheres);
		modelBoundingSpheres.clear();
		transformation.transformSphere(boundingSphere, boundingSphereWorld);
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Movable#getTransformation()
	 */
	public Matrix44 getTransformation()
	{
		return transformation_temp;
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Movable#setTransformation(nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44)
	 */
	public void setTransformation(Matrix44 transformation)
	{
		this.transformation_temp = transformation;
	}
	
	/**
	 * Adds a Model to this SceneEntity
	 *
	 * @param model the model
	 */
	public void add(Model model)
	{
		models.add(model);
		boundingBox.include(model.getBoundingBox());
	}
	
	/**
	 * Removes a Model from this SceneEntity
	 * @param model the model to remove
	 */
	public void remove(Model model)
	{
		models.remove(model);
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Movable#getName()
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * Gets the bounding box.
	 *
	 * @return the bounding box
	 */
	public AxisAlignedBox3 getBoundingBox()
	{
		return boundingBox;
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Movable#getBoundingSphereWorld()
	 */
	public Sphere getBoundingSphereWorld()
	{
		return boundingSphereWorld;
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Movable#getMotion()
	 */
	public Motion getMotion()
	{
		return motion;
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Movable#setMotion(nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion)
	 */
	public void setMotion(Motion motion)
	{
		this.motion = motion;
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Movable#getBasicOrientation()
	 */
	@Override
	public Matrix44 getBasicOrientation()
	{
		basicOrientation.copy(transformation);
		basicOrientation.addTranslate(	-basicOrientation.m[12],
										-basicOrientation.m[13],
										-basicOrientation.m[14]);
		return basicOrientation;
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Movable#getCurrentPosition()
	 */
	@Override
	public Vector3 getCurrentPosition() {
		currentPos.set( transformation.m[12],
						transformation.m[13],
						transformation.m[14]);
		
		return currentPos;
	}

	/**
	 * @return true if this is disabled
	 */
	public boolean isDisabled()
	{
		return disabled;
	}

	/**
	 * @param en/disables this
	 */
	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
	}
	
	
}
