package nu.shacknet.poseidon.pn.signanzorbit;

import static android.opengl.GLES10.*;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.util.LinkedList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import nu.shacknet.poseidon.pn.signanzorbit.R;
import nu.shacknet.poseidon.pn.signanzorbit.camera.Camera;
import nu.shacknet.poseidon.pn.signanzorbit.config.Config;
import nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44;
import nu.shacknet.poseidon.pn.signanzorbit.math.Vector3;
import nu.shacknet.poseidon.pn.signanzorbit.orbit.Orbit;
import nu.shacknet.poseidon.pn.signanzorbit.scene.AOMLoader;
import nu.shacknet.poseidon.pn.signanzorbit.scene.HUD;
import nu.shacknet.poseidon.pn.signanzorbit.scene.MaterialManager;
import nu.shacknet.poseidon.pn.signanzorbit.scene.Scene;
import nu.shacknet.poseidon.pn.signanzorbit.scene.SceneEntity;
import nu.shacknet.poseidon.pn.signanzorbit.util.CustomGestureDetector;
import nu.shacknet.poseidon.pn.signanzorbit.util.MathUtil;
import nu.shacknet.poseidon.pn.signanzorbit.util.Synchronizer;
import nu.shacknet.poseidon.pn.signanzorbit.util.CustomGestureDetector.CustomOnGestureListener;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.CollisionManager;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.GameManager;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.LogManager;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.MotionManager;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.OGLManager;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.SoundManager;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.TimeManager;

/**
 * The Class RenderView.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class RenderView extends GLSurfaceView implements Renderer
{
	
	/** The Constant LIGHT_AMBIENT. */
	private static final float LIGHT_AMBIENT[] = {0.6f,0.6f,0.6f,1.0f};
	
	/** The Constant LIGHT_DIFFUSE. */
	private static final float LIGHT_DIFFUSE[] = {1.0f,1.0f,1.0f,1.0f};
	
	/** The Constant LIGHT_POSITION. */
	private static final float LIGHT_POSITION[] = {-100.0f,100.0f,100.0f,1.0f};
	
	/** The context. */
	private final LevelActivity context;
	
	/** The scene. */
	public final Scene scene;
	
	/** The cam. */
	public final Camera cam;
	
	// Managers
	/** The timer. */
	private final TimeManager timer = TimeManager.instance;
	
	/** The Sound Manager. */
	private final SoundManager soundManager = SoundManager.instance;
	
	/** The motion manager. */
	private final MotionManager motionManager = MotionManager.instance;
	
	/** The game manager. */
	private final GameManager gameManager;
	
	/** The ogl manager. */
	private final OGLManager oglManager = OGLManager.instance;
	
	/** The material manager. */
	private final MaterialManager materialManager = MaterialManager.instance;
	
	/** The coll manager. */
	private CollisionManager collManager;
	
	// thread stuff
	/** The synchronizer. */
	public final Synchronizer synchronizer;
	
	// main thread stuff
	/** The gui thread handler. */
	private final Handler guiThreadHandler;
	
	/** The fps update runnable. */
	private final Runnable fpsUpdateRunnable;
	
	/** The score update runnable. */
	private final Runnable scoreUpdateRunnable;
	
	/** The remaining game time runnable. */
	private final Runnable remainingGameTimeRunnable;
	
	// event stuff
	/** The motion events. */
	private final LinkedList<MotionEvent> motionEvents;
	
	/** The key events. */
	private final LinkedList<KeyEvent> keyEvents;
	
	/** The gesture detector. */
	private final CustomGestureDetector gestureDetector;
	
	/** The HUD. */
	private final HUD hud;
	
	// temp vars
	/** The selection direction. */
	private final Vector3 selectionDirection;
	
	/** whether or not a screenshot should be created after the next frame. */
	private boolean screenshot = false;
	
	/** the number formatter for the numbering of the screenshots */
	NumberFormat screenshotFormatter;
	
	/**
	 * Instantiates a new render view.
	 *
	 * @param context the context
	 * @param attr the attr
	 */
	public RenderView(Context context, AttributeSet attr)
	{
		super(context, attr);
		setFocusable(true);
		requestFocus();
		setRenderer(this);
		
		this.context = (LevelActivity)context;
		
		cam = new Camera(Config.CAM_DISTANCE,-80.0f,80.0f,0.0f,0.0f,1.0f/60.0f,1.0f,200.0f);
		
		synchronizer = new Synchronizer();
		motionEvents = new LinkedList<MotionEvent>();
		keyEvents = new LinkedList<KeyEvent>();
		
		gestureDetector = new CustomGestureDetector(new GestureListener());
		
		hud = new HUD();
		
		//init temp vars
		selectionDirection = new Vector3();
		
		scene = AOMLoader.instance.readScene(Config.LEVELNAME);
		scene.setHud(hud);
		motionManager.generateRandomOrbits(scene,Config.UNIVERSE_SPEED_LIMIT/2,Config.UNIVERSE_SPEED_LIMIT,0,(float)Math.PI/4,0,(float)Math.PI/4,15,20,0.7f,1.3f);
		collManager = CollisionManager.instance;
		collManager.init(scene);
		gameManager = GameManager.instance;
		collManager.initAndSetGameManager(gameManager);

		guiThreadHandler = this.context.handler;
		fpsUpdateRunnable = this.context.fpsUpdateRunnable;
		scoreUpdateRunnable = this.context.scoreUpdateRunnable;
		remainingGameTimeRunnable = this.context.remainingGameTimeRunnable;
		
		screenshotFormatter = NumberFormat.getInstance();
		screenshotFormatter.setMinimumIntegerDigits(5);
		screenshotFormatter.setMaximumFractionDigits(0);
		screenshotFormatter.setGroupingUsed(false);
		screenshotFormatter.setParseIntegerOnly(true);
	}
	
	/**
	 * Inits the gl settings.
	 */
	private void initGLSettings()
	{
		glEnable(GL_CULL_FACE);
		glShadeModel(GL_SMOOTH);
		glEnable(GL_LIGHTING);
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
	
		glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 
		glEnable(GL_DEPTH_TEST);
		glClearDepthf(1.0f);
		glEnable(GL_TEXTURE_2D);
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		
		//setup light
		glEnable(GL_LIGHT0);
		glLightfv(GL_LIGHT0, GL_POSITION, LIGHT_POSITION,0);
		glLightfv(GL_LIGHT0, GL_AMBIENT, LIGHT_AMBIENT,0);
		glLightfv(GL_LIGHT0, GL_DIFFUSE, LIGHT_DIFFUSE,0);
	}

	/* (non-Javadoc)
	 * @see android.opengl.GLSurfaceView.Renderer#onSurfaceCreated(javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig)
	 */
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		LogManager.v("onSurfaceCreated(" + gl + ", " + config + ")");
		
		// reset everything
		oglManager.reset();
		materialManager.reset();
		timer.reset(false);
		scene.deInit();
		
		// check for GLES11
		boolean gles11 = (gl instanceof GL11);
		Config.GLES11 = gles11;
		LogManager.i("OpenGL ES " + (gles11 ? "1.1" : "1.0") + " found!");
		
		initGLSettings();
		
		/**
		 * The Logic Thread
		 */
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while(RenderView.this.synchronizer.running)
					update();
				synchronizer.logicThreadFinished();
			}
		}, "Logic Thread").start();
	}
	
	/**
	 * Update.
	 */
	private void update()
	{
		synchronizer.waitForPreRender();
		timer.update();
		
		/*
		 * process all events
		 */
		synchronized(motionEvents)
		{
			MotionEvent event;
			while((event = motionEvents.poll()) != null)
				gestureDetector.onTouchEvent(event);
		}
		
		synchronized(keyEvents)
		{
			KeyEvent event;
			while((event = keyEvents.poll()) != null)
			{
				int keyCode = event.getKeyCode();
				
				switch(keyCode)
				{
				case KeyEvent.KEYCODE_DPAD_UP:
					cam.setDistance(cam.getDistance() - 10.0f);
					break;
				case KeyEvent.KEYCODE_DPAD_DOWN:
					cam.setDistance(cam.getDistance() + 10.0f);
					break;
				case KeyEvent.KEYCODE_DPAD_LEFT:
					Orbit test = (Orbit) motionManager.getMotion(1).getMotion();
					test.morphAxisScale(0.4f, 0.5f,100,100);
					break;	
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					Orbit test1 = (Orbit) motionManager.getMotion(1).getMotion();
					test1.morphAxisScale(1.6f,1.5f,100,100);
					break;
				case KeyEvent.KEYCODE_CAMERA:
					screenshot = true;
					break;
				}
			}
		}
		
		motionManager.updateMotion(timer.getDeltaTsec());
		collManager.doCollisionDetection();
		cam.updatePosition(Config.UNIVERSE_CENTER, 1.0f);
		
		/*
		 * Update FPS
		 */
		if(timer.haveFPSchanged())
			guiThreadHandler.post(fpsUpdateRunnable);
		
		/*
		 * Update Time
		 */
		if(timer.hasRemainingGameTimeChanged())
			guiThreadHandler.post(remainingGameTimeRunnable);
		
		/*
		 * Update Score
		 */
		if(gameManager.scoreHasChanged())
			guiThreadHandler.post(scoreUpdateRunnable);
		
		synchronizer.logicDone(); 
	}
	
	/**
	 * The listener interface for receiving gesture events.
	 * The class that is interested in processing a gesture
	 * event implements this interface. When
	 * the gesture event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see GestureEvent
	 */
	private class GestureListener implements CustomOnGestureListener
	{
		
		/* (non-Javadoc)
		 * @see nu.shacknet.poseidon.pn.signanzorbit.util.CustomGestureDetector.CustomOnGestureListener#onDown(android.view.MotionEvent)
		 */
		@Override
		public boolean onDown(MotionEvent e)
		{
			LogManager.v("onDown(" + e + ")");
			hud.setCircle(e.getRawX(), e.getRawY());
			return true;
		}

		/* (non-Javadoc)
		 * @see nu.shacknet.poseidon.pn.signanzorbit.util.CustomGestureDetector.CustomOnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
		 */
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			LogManager.v("onScroll(" + e1 + ", " + e2 + ", " + distanceX + ", " + distanceY + ")");
			
			if(Config.EASY_MODE)
				hud.setCircle(e2.getRawX(), e2.getRawY());
			else
				hud.disableCircle();
			
			cam.setMouseDiff(distanceX, distanceY);
			return true;
		}

		/* (non-Javadoc)
		 * @see nu.shacknet.poseidon.pn.signanzorbit.util.CustomGestureDetector.CustomOnGestureListener#onTouchUp(android.view.MotionEvent, long)
		 */
		@Override
		public boolean onUp(MotionEvent e, boolean wasLongPress, long duration)
		{
			LogManager.v("onTouchUp(" + e + ", " + duration + ")");

			hud.disableCircle();

			if(wasLongPress || Config.EASY_MODE)
			{
				float powerPercent = (float)duration / (float)Config.LONG_PRESS_CYCLE_DURATION;
				
				float powerPercentPulsed = MathUtil.forcePulseFunktion(powerPercent);
				
				//force strength
				float power = powerPercentPulsed * Config.MAX_FORCE;
				
				Vector3 unprojectedPoint = oglManager.unProject((int)e.getRawX(), (int)e.getRawY());
				Vector3 rayDirection = Vector3.subtract(unprojectedPoint,cam.eyePosition).normalize();

				SceneEntity entity = collManager.intersectRay(cam.eyePosition, rayDirection);

				//entity selected
				if(entity!=null && !entity.getName().equals(Config.PLANET_NAME))
				{
					selectionDirection.set(cam.viewPosition);
					selectionDirection.subtract(cam.eyePosition);

					selectionDirection.normalize().multiply(power);

					LogManager.d("selectionDirection=" + selectionDirection + " power = "+selectionDirection.length());

					// vibrate according to the strength
					context.vibrate((long)(powerPercentPulsed * (float)Config.MAX_VIBRATION_LENGTH));

					// play sound
					soundManager.playSound(Config.SOUND_SHOOT);

					motionManager.applySelectionForce(entity, selectionDirection);				
					motionManager.changeSatelliteTransformation(entity, entity.getMotion().getCurrDirectionVec(), selectionDirection,Config.SATELLITE_SPEEDROTA_RATIO);
				}

				LogManager.d("unprojectedPoint=" + unprojectedPoint + ", eye=" + cam.eyePosition + ", ray=" + rayDirection);
			}
			return true;
		}
	}

	/**
	 * Pre_render. This Method is run in the GL Thread,
	 * but during the synchronized phase, when the Logic Thread
	 * is suspended.
	 * 
	 */
	private void pre_render()
	{
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//		glLoadIdentity(); // not needed, because cam.look() sets the modelview matrix completely new
		cam.look();
		glLightfv(GL_LIGHT0, GL_POSITION, LIGHT_POSITION,0);
	}
	
	/**
	 * Render.
	 */
	private void render()
	{
		scene.render();
		if(screenshot)
		{
			takeScreenshot();
			screenshot = false;
		}
	}
	
	/**
	 * Take screenshot.
	 */
	private void takeScreenshot()
	{
		FileOutputStream fos = null;
		try
		{
			File screenshotDir = new File(Environment.getExternalStorageDirectory(), context.getString(R.string.NameOfTheGame));
			if(!screenshotDir.exists())
				screenshotDir.mkdirs();
			int counter = 0;
			
			File screenshotFile = null;
			do
			{
				screenshotFile = new File(screenshotDir, "screenshot_" + screenshotFormatter.format(counter) + ".png");
				counter++;
			}
			while(screenshotFile.exists());
			
			fos = new FileOutputStream(screenshotFile);
			oglManager.saveScreenshot(fos);
			LogManager.i("Saving Screenshot to " + screenshotFile.getAbsolutePath());
		}
		catch(Throwable t)
		{
			LogManager.e("Error creating screenshot",t);
		}
	}
	
	/* (non-Javadoc)
	 * @see android.opengl.GLSurfaceView.Renderer#onDrawFrame(javax.microedition.khronos.opengles.GL10)
	 */
	@Override
	public void onDrawFrame(GL10 gl)
	{
		synchronizer.waitForLogic();
		// copies transformation matrizes
		scene.update();
		// sets light and camera
		pre_render();
		synchronizer.preRenderDone();
		render();
	}

	/* (non-Javadoc)
	 * @see android.opengl.GLSurfaceView.Renderer#onSurfaceChanged(javax.microedition.khronos.opengles.GL10, int, int)
	 */
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height)
	{
		LogManager.v("onSurfaceChanged(" + gl + ", " + width + ", " + height + ")");
		// Prevent a Divide By Zero by making height equal one
		if(height == 0)
			height = 1;

		// Reset the current viewport
		glViewport(0, 0, width, height);
		int[] viewport = oglManager.getViewport();
		viewport[0] = 0;
		viewport[1] = 0;
		viewport[2] = width;
		viewport[3] = height;
		
		// Select and reset the projection matrix
		glMatrixMode(GL_PROJECTION); 	
		glLoadIdentity();

		// Fill the projection Matrix
		float fovy = 45.0f;
		float aspect = (float)width / (float)height;
		float zNear = 0.1f;
		//float zFar = Float.MAX_VALUE;
		float zFar = 10000;
		
		float top = (float)(Math.tan(fovy*0.0087266463f) * zNear); // top = tan((fovy/2)*(PI/180))*zNear
		float bottom = -top;
		float left = aspect * bottom;
		float right = aspect * top;
		
		Matrix44 projection = oglManager.getProjection();
		
		oglManager.glFrustumInfinite(left, right, bottom, top, zNear, zFar, projection);

		glLoadMatrixf(projection.getArray16(), 0);
		
		// Select the modelview matrix again
		glMatrixMode(GL_MODELVIEW);
	}
	
	/* (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(final MotionEvent event)
	{
		synchronized(motionEvents)
		{
			motionEvents.addLast(event);
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see android.view.View#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
		synchronized(keyEvents)
		{
			keyEvents.addLast(event);
			if(event.getKeyCode() == KeyEvent.KEYCODE_CAMERA)
				return true;
		}
		return false;
	}
}