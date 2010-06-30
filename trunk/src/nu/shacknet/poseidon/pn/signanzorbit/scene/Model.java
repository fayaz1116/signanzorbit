package nu.shacknet.poseidon.pn.signanzorbit.scene;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import nu.shacknet.poseidon.pn.signanzorbit.math.AxisAlignedBox3;
import nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44;
import nu.shacknet.poseidon.pn.signanzorbit.math.Sphere;
import nu.shacknet.poseidon.pn.signanzorbit.math.Vector3;
import nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion;
import nu.shacknet.poseidon.pn.signanzorbit.orbit.Movable;
import nu.shacknet.poseidon.pn.signanzorbit.scene.MaterialManager.Material;
import nu.shacknet.poseidon.pn.signanzorbit.util.Persistable;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.LogManager;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.MotionManager;


//static imports
import static android.opengl.GLES10.*;

/**
 * The Class Model.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class Model implements Movable,Persistable
{
	
	/** The name. */
	String name;
	
	/** The material manager. */
	final MaterialManager materialManager = MaterialManager.instance;

	/** The transformation used by opengl. */
	Matrix44 transformation;

	/** The transformation the logic thread writes to. is written to transformation in update(). */
	Matrix44 transformation_temp;

	/** The basic orientation (needed for orbit). */
	final Matrix44 basicOrientation;
	
	/** The bounding box in model space. */
	final AxisAlignedBox3 boundingBox;
	
	/** The bounding sphere in model space. */
	final Sphere boundingSphere;
	
	/** The bounding sphere in world space. */
	final Sphere boundingSphereWorld;
	
	/** The bounding sphere in scene entity space. */
	final Sphere boundingSphereSceneEntity;
	
	/** The motion. */
	Motion motion;

	/** if this is initialized. */
	boolean initialized;
	
	/** The geometries. */
	final ArrayList<Geometry> geometries;
	
	/** The materials (in the same order as the corresponding geometries). */
	final ArrayList<Material> materials;
	
	/** The current position */
	final Vector3 currentPos;
	
	/** whether this Model is disabled (not rendered, not updated) */
	boolean disabled;
	
	/**
	 * Instantiates a new model.
	 */
	public Model()
	{
		transformation = new Matrix44();
		basicOrientation = new Matrix44();
		geometries = new ArrayList<Geometry>();
		materials = new ArrayList<Material>();
		boundingBox = new AxisAlignedBox3();
		boundingSphere = new Sphere();
		boundingSphereWorld = new Sphere();
		boundingSphereSceneEntity = new Sphere();
		initialized = false;
		currentPos = new Vector3();
		disabled = false;
	}
	
	/**
	 * Copy Constructor
	 *
	 * @param other the other
	 */
	public Model(Model other)
	{
		geometries = new ArrayList<Geometry>();
		materials = new ArrayList<Material>();
		basicOrientation = new Matrix44();
		transformation = new Matrix44(other.transformation);
		boundingBox = new AxisAlignedBox3(other.boundingBox);
		boundingSphere = new Sphere(other.boundingSphere);
		boundingSphereWorld = new Sphere(other.boundingSphereWorld);
		boundingSphereSceneEntity = new Sphere(other.boundingSphereSceneEntity);
		currentPos = new Vector3();
		name = other.name;
		disabled = other.disabled;
		
		int numGeoms = other.geometries.size();
		
		for(int i=0; i<numGeoms; i++)
		{
			geometries.add(other.geometries.get(i));
			materials.add(other.materials.get(i));
		}
	}
	
	/**
	 * Inits this Model
	 */
	void init()
	{
		if(!initialized)
		{
			ArrayList<Geometry> geometries = this.geometries;
			int size = geometries.size();
			for(int i=0; i<size; i++)
			{
				geometries.get(i).init();
				materials.get(i).init();
			}
			
			initialized = true;
		}
	}
	
	/**
	 * De-init this Model
	 */
	void deInit()
	{
		initialized = false;
		
		ArrayList<Geometry> geometries = this.geometries;
		int size = geometries.size();
		for(int i=0; i<size; i++)
		{
			geometries.get(i).deInit();
			materials.get(i).deInit();
		}
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.util.Persistable#persist(java.io.DataOutputStream)
	 */
	public void persist(DataOutputStream dos) throws IOException
	{
		LogManager.d("writing model " + name);
		ArrayList<Geometry> geometries = this.geometries;
		int size = geometries.size();
		for(int i=0; i<size; i++)
		{
			geometries.get(i).persist(dos);
			materials.get(i).persist(dos);
		}
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
		LogManager.d("reading model " + name);
		ArrayList<Geometry> geometries = this.geometries;
		int size = geometries.size();
		for(int i=0; i<size; i++)
		{
			geometries.get(i).restore(dis);
			materials.get(i).restore(dis);
		}
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
		int numGeoms = geometries.size();
		for(int i=0; i<numGeoms; i++)
		{
			materialManager.bindMaterial(materials.get(i));
			geometries.get(i).render(rendermode);
		}
		glPopMatrix();
	}
	
	/**
	 * Render bounding spheres.
	 */
	void renderBoundingSpheres(int rendermode)
	{
		if(disabled)
			return;
		
		Vector3 translation = boundingSphereWorld.center;
		float scale = boundingSphereWorld.radius;
		
		glPushMatrix();
		glTranslatef(translation.v[0], translation.v[1], translation.v[2]);
		glScalef(scale, scale, scale);
		Scene.SPHERE.render(rendermode);
		glPopMatrix();
	}
	
	/**
	 * Update.
	 *
	 * @param sceneEntityTransformation the scene entity transformation
	 */
	public void update(Matrix44 sceneEntityTransformation)
	{
		if(disabled)
			return;
		
		int numGeoms = geometries.size();
		for(int i=0; i<numGeoms; i++)
			geometries.get(i).update();

		transformation.copy(transformation_temp);

		transformation.transformSphere(boundingSphere, boundingSphereSceneEntity);
		sceneEntityTransformation.transformSphere(boundingSphereSceneEntity, boundingSphereWorld);
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
	
	/**
	 * @return the boundingSphere
	 */
	public Sphere getBoundingSphere()
	{
		return boundingSphere;
	}

	/**
	 * @return the boundingSphereSceneEntity
	 */
	public Sphere getBoundingSphereSceneEntity()
	{
		return boundingSphereSceneEntity;
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
	 * Adds a Geometry and its Material to the Model
	 *
	 * @param geometry the geometry
	 * @param material the material
	 */
	public void add(Geometry geometry, Material material)
	{
		geometries.add(geometry);
		materials.add(material);
		boundingBox.include(geometry.getBoundingBox());
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Movable#getMotion()
	 */
	@Override
	public Motion getMotion()
	{
		return motion;
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Movable#setMotion(nu.shacknet.poseidon.pn.signanzorbit.orbit.Motion)
	 */
	@Override
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
		basicOrientation.copy(transformation_temp);
		basicOrientation.addTranslate(	-basicOrientation.m[12],
										-basicOrientation.m[13],
										-basicOrientation.m[14]);
		return basicOrientation;
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.orbit.Movable#getName()
	 */
	@Override
	public String getName()
	{
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public Vector3 getCurrentPosition() {
		currentPos.set( transformation.m[12],
						transformation.m[13],
						transformation.m[14]);
		
		return currentPos;
	}

	/**
	 * @return the disabled
	 */
	public boolean isDisabled()
	{
		return disabled;
	}

	/**
	 * @param disabled the disabled to set
	 */
	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
	}
}
