package nu.shacknet.poseidon.pn.signanzorbit.scene;

import static android.opengl.GLES10.*;
import nu.shacknet.poseidon.pn.signanzorbit.config.Config;
import nu.shacknet.poseidon.pn.signanzorbit.math.AxisAlignedBox3;
import nu.shacknet.poseidon.pn.signanzorbit.math.Color4;
import nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44;
import nu.shacknet.poseidon.pn.signanzorbit.math.Sphere;
import nu.shacknet.poseidon.pn.signanzorbit.math.Vector2;
import nu.shacknet.poseidon.pn.signanzorbit.math.Vector3;
import nu.shacknet.poseidon.pn.signanzorbit.scene.MaterialManager.Material;
import nu.shacknet.poseidon.pn.signanzorbit.util.MathUtil;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.OGLManager;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.TimeManager;


/**
 * The Class HUD.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class HUD extends Model
{
	/** The ogl manager. */
	final OGLManager oglManager = OGLManager.instance;
	
	/** The time manager */
	final TimeManager timeManager = TimeManager.instance;
	
	/** The height. */
	private float width,height;
	
	/** The aspect. */
	private float aspect;
	
	/** The center  of the current circle */
	private final Vector2 circleCenter;
	
	/** The timestamp of the current circles start */
	private long circleStartMillis;
	
	/** whether a circle should be drawn after the next update */
	private boolean circleActive;
	
	/** whether a circle should drawn now */
	private boolean circleRender;
	
	/**
	 * Instantiates a new HUD.
	 */
	public HUD()
	{
		super();
		name = "HUD";
		float[] vertices = new float[] {
			-0.5f,-0.5f, 0.0f,	// upper left
			-0.5f, 0.5f, 0.0f,	// lower left
			 0.5f,-0.5f, 0.0f,	// upper right
			-0.5f, 0.5f, 0.0f,	// lower left
			 0.5f, 0.5f, 0.0f,	// lower right
			 0.5f,-0.5f, 0.0f	// upper right
		};
		float[] texcoords = new float[] {
			0.0f, 0.0f,		// upper left
			0.0f, 1.0f,		// lower left
			1.0f, 0.0f,		// upper right
			0.0f, 1.0f,		// lower left
			1.0f, 1.0f,		// lower right
			1.0f, 0.0f		// upper right
		};
		int numVertices = 6;
		
		Geometry circle = new Geometry(
				AOMLoader.instance.arrayToBuffer(vertices),
				null,
				AOMLoader.instance.arrayToBuffer(texcoords),
				new AxisAlignedBox3(new Vector3(-0.5f, -0.5f, 0.0f), new Vector3(0.5f, 0.5f, 0.0f)),
				new Sphere(new Vector3(0,0,0), 0.707106781f),
				numVertices);
		
		circleCenter = new Vector2();
		
		circleActive = false;
		circleRender = false;
		Material material = materialManager.getMaterial("HUDMaterial", 
				new Color4(1.0f,1.0f,1.0f,1.0f), 
				new Color4(0.0f,0.0f,0.0f,0.0f), 
				new Color4(0.0f,0.0f,0.0f,0.0f), 
				new Color4(0.0f,0.0f,0.0f,1.0f), 
				0.5f, 
				"circle");
		
		add(circle, material);
	}
	
	/**
	 * Pre_render.
	 */
	private void pre_render()
	{	
		// switch to projection
		glMatrixMode(GL_PROJECTION);
		
		// push projection
		glPushMatrix();
		
		// reset projection
		glLoadIdentity();
		
		// make projection ortho (width = aspect, height = 1)
		glOrthof(0, aspect, 1, 0, 0, 1);
		
		// switch to modelview again
		glMatrixMode(GL_MODELVIEW);
		
		// reset modelview
		glLoadIdentity();
	}
	
	/**
	 * Render.
	 */
	public void render(int rendermode)
	{
		if(!circleRender)
			return;
		
		pre_render();

		super.render(rendermode);
		
		post_render();
	}
	
	/**
	 * Post_render.
	 */
	private void post_render()
	{
		// switch to projection
		glMatrixMode(GL_PROJECTION);
		
		// pop projection
		glPopMatrix();
		
		// switch to modelview
		glMatrixMode(GL_MODELVIEW);
	}
	
	/**
	 * Update.
	 */
	public void update(Matrix44 sceneEntityTransformation)
	{
		int[] viewport = oglManager.getViewport();
		width = viewport[2];
		height = viewport[3];
		aspect = width/height;
		circleRender = circleActive;
		
		if(circleRender)
		{
			long circleTime = timeManager.getTimeOfLastFrame()-circleStartMillis;
			float circlePercent = (float)circleTime / (float)Config.LONG_PRESS_CYCLE_DURATION;
			float scale = MathUtil.forcePulseFunktion(circlePercent) * Config.MAX_FORCE_VIS_SIZE;
			float angle = circleTime * Config.FORCE_VIS_ROTATION_SPEED;
			transformation.setScale(scale,scale,scale);
			transformation.addRotateZ(angle);
			transformation.addTranslate(circleCenter.v[0], circleCenter.v[1], 0);
		}
	}
	
	
	/**
	 * Sets the circle's position and size
	 *
	 * @param centerPixelsX the center_x in screen pixels
	 * @param centerPixelsY the center_y in screen pixels
	 */
	public void setCircle(float centerPixelsX, float centerPixelsY)
	{
		circleCenter.v[0] = ((float)centerPixelsX / width)*aspect;
		circleCenter.v[1] = (float)centerPixelsY / height;
		
		if(!circleActive)
		{
			circleStartMillis = timeManager.getTimeOfLastFrame();
			circleActive = true;
		}
	}
	
	/**
	 * @param circleActive the circleActive to set
	 */
	public void disableCircle()
	{
		this.circleActive = false;
	}
}
