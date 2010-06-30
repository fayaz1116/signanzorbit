package nu.shacknet.poseidon.pn.signanzorbit.scene;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import nu.shacknet.poseidon.pn.signanzorbit.config.Config;
import nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44;
import nu.shacknet.poseidon.pn.signanzorbit.util.Persistable;
import nu.shacknet.poseidon.pn.signanzorbit.util.datastructures.Pair;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.MotionManager;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.OGLManager;


/**
 * The Class Scene.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class Scene implements Persistable
{
	/** The Constant RENDERMODE_VERTEXARRAY. */
	public static final int RENDERMODE_VERTEXARRAY = 0;
	
	/** The Constant RENDERMODE_VBO. */
	public static final int RENDERMODE_VBO = 1;
	
	/** A bounding Sphere */
	public static final Model SPHERE = new GreenSphere();

	/** The OpenGL Manager. */
	private final OGLManager oglManager = OGLManager.instance;
	
	/** The Motion Manager */
	private final MotionManager motionManager = MotionManager.instance;
	
	/** The scene entities. */
	public final ArrayList<SceneEntity> sceneEntities;
	
	/** The hud. */
	private Model hud;
	
	/** The rendermode. */
	private int rendermode;

	/** if this is initialized. */
	private boolean initialized;
	
	/** contains all Models that should be untied from their SceneEntities */
	private final LinkedList<Pair<SceneEntity,Model>> toUntie;
	
	/** contains all Models that have already been untied from their SceneEntities, written as their indices (SceneEntity first) */
	private final LinkedList<Pair<Integer,Integer>> untied;
	
	/**
	 * Instantiates a new scene.
	 */
	public Scene()
	{	
		sceneEntities = new ArrayList<SceneEntity>();
		initialized = false;
		toUntie = new LinkedList<Pair<SceneEntity,Model>>();
		untied = new LinkedList<Pair<Integer,Integer>>();
	}
	
	/**
	 * Inits the scene.
	 */
	public void init()
	{
		if(!initialized)
		{
			rendermode = Config.GLES11 ? RENDERMODE_VBO : RENDERMODE_VERTEXARRAY;

			ArrayList<SceneEntity> sceneEntities = this.sceneEntities;
			int size = sceneEntities.size();
			for(int i=0; i<size; i++)
				sceneEntities.get(i).init();
			
			hud.init();
			SPHERE.init();

			if(Config.GLES11)
				oglManager.init();
			
			initialized = true;
		}
	}
	
	/**
	 * De-inits the scene.
	 */
	public void deInit()
	{
		initialized = false;
		
		ArrayList<SceneEntity> sceneEntities = this.sceneEntities;
		int size = sceneEntities.size();
		for(int i=0; i<size; i++)
			sceneEntities.get(i).deInit();
		
		hud.deInit();
		SPHERE.deInit();
		
		if(Config.GLES11)
			oglManager.deInit();
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.util.Persistable#persist(java.io.DataOutputStream)
	 */
	public void persist(DataOutputStream dos) throws IOException
	{
		LinkedList<Pair<Integer, Integer>> untied = this.untied;
		int size = untied.size();
		dos.writeInt(size);
		for(int i=0; i<size; i++)
		{
			Pair<Integer, Integer> p = untied.get(i);
			dos.writeInt(p.getFirst());
			dos.writeInt(p.getSecond());
		}
		
		ArrayList<SceneEntity> sceneEntities = this.sceneEntities;
		size = sceneEntities.size();
		for(int i=0; i<size; i++)
			sceneEntities.get(i).persist(dos);
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.util.Persistable#restore(java.io.DataInputStream)
	 */
	public void restore(DataInputStream dis) throws IOException
	{
		int size = dis.readInt();
		for(int i=0; i<size; i++)
		{
			int first = dis.readInt();
			int second = dis.readInt();
			
			SceneEntity s = sceneEntities.get(first);
			Model m = s.models.get(second);
			SceneEntity new_s = new SceneEntity();
			new_s.setName(Config.SATELLITE_PREFIX + m.getName() + Config.PLANETPART_SUFFIX);
			new_s.add(m);
			m.getBasicOrientation().setIdentity();
			
			s.remove(m);
			sceneEntities.add(new_s);
			untied.add(new Pair<Integer, Integer>(first, second));
		}
		
		ArrayList<SceneEntity> sceneEntities = this.sceneEntities;
		size = sceneEntities.size();
		for(int i=0; i<size; i++)
			sceneEntities.get(i).restore(dis);
	}
	
	/**
	 * Render.
	 */
	public void render()
	{
		if(!initialized)
			init();
		
		int size = sceneEntities.size();
		
		for(int i=0;i<size;i++)
			sceneEntities.get(i).render(rendermode);
		
		renderBoundingSpheres();
		
		hud.render(rendermode);
	}
	
	/**
	 * Render bounding spheres.
	 */
	private void renderBoundingSpheres()
	{
		int size = sceneEntities.size();
		for(int i=0;i<size;i++)
			sceneEntities.get(i).renderBoundingSpheres(rendermode);
	}
	
	/**
	 * Update.
	 */
	public void update()
	{
		synchronized(toUntie)
		{
			if(!toUntie.isEmpty())
			{
				int size = toUntie.size();
				for(int i=0; i<size; i++)
				{
					Pair<SceneEntity, Model> p = toUntie.poll();
					SceneEntity s = p.getFirst();
					Model m = p.getSecond();
					int sceneEntityIndex = sceneEntities.indexOf(s);
					int modelIndex = s.models.indexOf(m);
					SceneEntity new_s = new SceneEntity();
					new_s.setName(Config.SATELLITE_PREFIX + m.getName() + Config.PLANETPART_SUFFIX);
					new_s.add(m);
					Matrix44 new_s_transformation = new_s.getTransformation();
					new_s_transformation.copy(s.getTransformation());
					new_s_transformation.mult(m.getTransformation());
					new_s.update();
					m.getTransformation().setIdentity();
					m.getBasicOrientation().setIdentity();
					motionManager.transferMotion(m, new_s);
					
					s.remove(m);
					sceneEntities.add(new_s);
					untied.add(new Pair<Integer, Integer>(sceneEntityIndex, modelIndex));
				}
			}
		}
		
		int size = sceneEntities.size();
		for(int i=0;i<size;i++)
			sceneEntities.get(i).update();
		
		if(hud != null)
			hud.update(null);
	}
	
	/**
	 * Adds a SceneEntity to the Scene.
	 *
	 * @param sceneEntity the scene entity
	 */
	public void add(SceneEntity sceneEntity)
	{
		sceneEntities.add(sceneEntity);
	}
	
	/**
	 * Gets the index'th scene entity.
	 *
	 * @param index the index
	 * @return the scene entity
	 */
	public SceneEntity getSceneEntity(int index)
	{
		return sceneEntities.get(index);
	}

	/**
	 * Sets the hud.
	 *
	 * @param hud the new hud
	 */
	public void setHud(HUD hud)
	{
		this.hud = hud;
	}
	
	/**
	 * schedules a Model to be untied from its scene entity
	 * @param se the scene entity of the model-to-untie
	 * @param m the model-to-untie
	 */
	public void unTie(SceneEntity se, Model m)
	{
		synchronized(toUntie)
		{
			toUntie.add(new Pair<SceneEntity, Model>(se, m));
		}
	}
}
