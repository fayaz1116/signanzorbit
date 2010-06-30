package nu.shacknet.poseidon.pn.signanzorbit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import nu.shacknet.poseidon.pn.signanzorbit.R;
import nu.shacknet.poseidon.pn.signanzorbit.camera.Camera;
import nu.shacknet.poseidon.pn.signanzorbit.config.Config;
import nu.shacknet.poseidon.pn.signanzorbit.scene.Scene;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.CollisionManager;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.GameManager;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.LogManager;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.MotionManager;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.SoundManager;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.TimeManager;

/**
 * The Class LevelActivity.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class LevelActivity extends Activity
{
	static
	{
		System.loadLibrary("signanzorbit");
	}
	
	/** The Constant SCENE_STATE_KEY. */
	public static final String SCENE_STATE_KEY = "state";
	
	/** The current version of the scene state file */
	public static final int SCENE_STATE_VERSION = 1;
	
	/** The Loglevel Dialog id. */
	private static final int DIALOG_ID_LOGLEVEL = 0;
	
	/** The instance. */
	private static LevelActivity instance;
	
	/** The Constant sessionState. */
//	private static final SessionState sessionState = new SessionState();
	
	/** The render view. */
	private RenderView renderView;
	
	/** The time TextView. */
	private TextView time;
	
	/** The fps TextView. */
	private TextView fps;
	
	/** The score progress. */
	private ProgressBar scoreProgress;
	
	/** The time progress. */
	private ProgressBar timeProgress;
	
	/** The handler. */
	public final Handler handler;
	
	/** The fps update runnable. */
	public final Runnable fpsUpdateRunnable;
	
	/** The score update runnable. */
	public final Runnable scoreUpdateRunnable;
	
	/** The remaining game time runnable. */
	public final Runnable remainingGameTimeRunnable;
	
	/** The time manager. */
	private final TimeManager timeManager = TimeManager.instance;
	
	/** The sound manager. */
	private final SoundManager soundManager = SoundManager.instance;
	
	/** Whether sound is enabled or not. */
	public static boolean SOUND_ENABLED = true;
	
	/** The vibrator. */
	private Vibrator vibrator;
	
	/** The PowerManagers Wake Lock. */
	private PowerManager.WakeLock wakeLock;
	
	/** The Milliseconds since the game was considered complete. */
	private float timeSinceComplete;
	
	/** indicating that the level has been completed. */
	private boolean levelComplete;
	
	/** The loglevel dialog. */
	private Dialog dialog_Loglevel;
	
	/**
	 * Instantiates a new level activity.
	 */
	public LevelActivity()
	{
		super();
		instance = this;
		handler = new Handler();
		
		timeSinceComplete = 0;
		levelComplete = false;		
				
		fpsUpdateRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				fps.setText(timeManager.getFPS() + " fps");
			}
		};
		
		scoreUpdateRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				float scorePercent = GameManager.instance.getScorePercent();
				scoreProgress.setProgress((int)Math.ceil(100-scorePercent));
//				sessionState.setProgress((int)Math.ceil(scorePercent)); 
//				setResult(Activity.RESULT_OK, sessionState.asIntent());
				if(GameManager.instance.isComplete()){
					timeSinceComplete = 0;
					levelComplete = true;
					soundManager.playSound(Config.SOUND_YEAH);
				}
			}
		};
		
		remainingGameTimeRunnable = new Runnable()
		{
			float totalSeconds = Config.GAMETIME/1000;
			
			@Override
			public void run()
			{
				int dt = (int)timeManager.getDeltaTmillis();
				int remainingSeconds = (int)(timeManager.getRemainingGameTime()/1000);
				timeProgress.setProgress((int)((remainingSeconds*100)/(totalSeconds)));
				
				if(remainingSeconds <= 0)
				{
					time.setText("Time's up!");
					finish();
					return;
				}
				if(levelComplete){
					if(timeSinceComplete>=Config.GAMETIME_WAIT_AFTER_COMPLETE){
						finish();
						return;
					}
					timeSinceComplete+=dt;
				}
								
				int remainingMinutes = remainingSeconds/60;
				remainingSeconds -= remainingMinutes*60;
				
				String remainingSecondsString = (remainingSeconds < 10 ? "0" : "") + remainingSeconds;
				String remainingMinutesString = (remainingMinutes < 10 ? "0" : "") + remainingMinutes;
				
				time.setText(remainingMinutesString + ":" + remainingSecondsString);
			}
		};
	}
	
	/**
	 * triggers the Vibrator.
	 *
	 * @param millis The Milliseconds the Vibrator will be turned on for
	 */
	public void vibrate(long millis)
	{
		if(Config.VIBRATE)
			vibrator.vibrate(millis);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		LogManager.v("onCreate(" + savedInstanceState + ")");
		
		initLoglevelDialog();
		
		System.gc();
		
		SOUND_ENABLED = true; /*getIntent().getBooleanExtra(MenuActivity.PREFERENCE_MUSIC, false);*/
		
		if(savedInstanceState == null)	// first start or restart
			timeManager.reset(true);
		
		soundManager.onCreate(this);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	 	Window window = getWindow();
	 	window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
	 	setContentView(R.layout.level);
		renderView = (RenderView)findViewById(R.id.RenderView);
		scoreProgress = (ProgressBar)findViewById(R.id.scoreProgressbar);
		time = (TextView)findViewById(R.id.timeTextField);
		timeProgress = (ProgressBar)findViewById(R.id.timeProgressbar);
		fps = (TextView)findViewById(R.id.fpsTextField);
		fps.setVisibility(Config.SHOW_FPS ? TextView.VISIBLE : TextView.INVISIBLE);
		
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, LogManager.TAG);

	 	//set dedicated volume buttons to control music volume
	 	setVolumeControlStream(AudioManager.STREAM_MUSIC);
        
//        sessionState.setProgress(0); 
//		setResult(Activity.RESULT_OK, sessionState.asIntent());
		
		renderView.setFocusable(true);
		renderView.setFocusableInTouchMode(true);
		renderView.requestFocus();
	}
	
	private void initLoglevelDialog()
	{
		final CharSequence[] items = {"Verbose", "Debug", "Info", "Warn", "Error", getString(R.string.Loglevel_none)};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.Select_Loglevel);
		builder.setSingleChoiceItems(items, Config.LOGLEVEL, new DialogInterface.OnClickListener()
		{
		    public void onClick(DialogInterface dialog, int item)
		    {
		    	Config.LOGLEVEL = item;
		    	dialog_Loglevel.dismiss();
		    }
		});
		dialog_Loglevel = builder.create();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart()
	{
		super.onStart();
		LogManager.v("onStart()");
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart()
	{
		super.onRestart();
		LogManager.v("onRestart()");
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		LogManager.v("onResume()");
		
		// try to read settings from file
		DataInputStream dis = null;
		try
		{
			dis = new DataInputStream(openFileInput(SCENE_STATE_KEY));
			
			boolean SHOW_FPS = Config.SHOW_FPS;
			boolean SHOW_SCENEENTITY_BOUNDING_SPHERES = Config.SHOW_SCENEENTITY_BOUNDING_SPHERES;
			boolean SHOW_MODEL_BOUNDING_SPHERES = Config.SHOW_MODEL_BOUNDING_SPHERES;
			boolean EASY_MODE = Config.EASY_MODE;
			boolean VIBRATE = Config.VIBRATE;
			int LOGLEVEL = Config.LOGLEVEL;
			
			// switch by version
			switch(dis.readInt())
			{
			case 1:
				SHOW_FPS = dis.readBoolean();
				SHOW_SCENEENTITY_BOUNDING_SPHERES = dis.readBoolean();
				SHOW_MODEL_BOUNDING_SPHERES = dis.readBoolean();
				EASY_MODE = dis.readBoolean();
				VIBRATE = dis.readBoolean();
				LOGLEVEL = dis.readInt();
				break;
			}
			
			// if we came so far without exception, we can safely write the settings
			// the config object
			// special case fps:
			if(Config.SHOW_FPS != SHOW_FPS)
				fps.setVisibility(SHOW_FPS ? TextView.VISIBLE : TextView.INVISIBLE);
			Config.SHOW_FPS = SHOW_FPS;
			Config.SHOW_SCENEENTITY_BOUNDING_SPHERES = SHOW_SCENEENTITY_BOUNDING_SPHERES;
			Config.SHOW_MODEL_BOUNDING_SPHERES = SHOW_MODEL_BOUNDING_SPHERES;
			Config.EASY_MODE = EASY_MODE;
			Config.VIBRATE = VIBRATE;
			Config.LOGLEVEL = LOGLEVEL;
			
			dis.close();
		}
		catch (Throwable t)
		{
			LogManager.w("Could not read config file, defaults are used", t);
		}
		finally
		{
			if(dis != null)
			{
				try
				{
					dis.close();
				}
				catch(Throwable t) {}
			}
		}
		
		wakeLock.acquire();
		soundManager.onResume();
		renderView.synchronizer.setActive(true);
		renderView.onResume();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
		LogManager.v("onPause()");
		
		// try to write settings to file
		DataOutputStream dos = null;
		try
		{
			dos = new DataOutputStream(openFileOutput(SCENE_STATE_KEY, MODE_PRIVATE));
			
			// version
			dos.writeInt(SCENE_STATE_VERSION);
			dos.writeBoolean(Config.SHOW_FPS);
			dos.writeBoolean(Config.SHOW_SCENEENTITY_BOUNDING_SPHERES);
			dos.writeBoolean(Config.SHOW_MODEL_BOUNDING_SPHERES);
			dos.writeBoolean(Config.EASY_MODE);
			dos.writeBoolean(Config.VIBRATE);
			dos.writeInt(Config.LOGLEVEL);
			
			dos.close();
		}
		catch (Throwable t)
		{
			LogManager.w("Could not write config file, defaults will be used on next start", t);
		}
		finally
		{
			if(dos != null)
			{
				try
				{
					dos.close();
				}
				catch(Throwable t) {}
			}
		}
		
		renderView.synchronizer.setActive(false);
		renderView.onPause();
		soundManager.onPause();
		wakeLock.release();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop()
	{
		super.onStop();
		LogManager.v("onStop()");
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		LogManager.v("onDestroy()");
		soundManager.onDestroy();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		LogManager.v("onSaveInstanceState(" + outState + ")");
		
		//this waits for the logic thread to shut down
		renderView.synchronizer.setActive(false);
		
		Scene scene = renderView.scene;
		Camera cam = renderView.cam;
		CollisionManager collManager = CollisionManager.instance;
		GameManager gameManager = GameManager.instance;

		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
			DataOutputStream dos = new DataOutputStream(baos);
			cam.persist(dos);
			scene.persist(dos);
			collManager.persist(dos);
			gameManager.persist(dos);
			dos.close();
			baos.close();
			byte[] state = baos.toByteArray();
			outState.putByteArray(SCENE_STATE_KEY, state);
		}
		catch (Throwable t)
		{
			LogManager.e("Failed to persist Scene state: ",t);
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		LogManager.v("onRestoreInstanceState(" + savedInstanceState + ")");
		
		byte[] state = savedInstanceState.getByteArray(SCENE_STATE_KEY);
		if(state != null)
		{
			try
			{
				ByteArrayInputStream bais = new ByteArrayInputStream(state);
				DataInputStream dis = new DataInputStream(bais);
				
				Scene scene = renderView.scene;
				Camera cam = renderView.cam;

				MotionManager.instance.reset();
				cam.restore(dis);
				scene.restore(dis);
				
				
				CollisionManager collManager = CollisionManager.instance;
				collManager.init(scene);
				collManager.restore(dis);
				
				GameManager gameManager = GameManager.instance;
				gameManager.restore(dis);
				
				dis.close();
				bais.close();
			}
			catch (Throwable t)
			{
				LogManager.e("Failed to restore Scene state: ",t);
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch(id)
		{
		case DIALOG_ID_LOGLEVEL: return dialog_Loglevel;
		}
		return super.onCreateDialog(id);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.Menu_ShowFPS:
			Config.SHOW_FPS ^= true;
			fps.setVisibility(Config.SHOW_FPS ? TextView.VISIBLE : TextView.INVISIBLE);
			return true;
		case R.id.Menu_BoundingSphereSE:
			Config.SHOW_SCENEENTITY_BOUNDING_SPHERES ^= true;
			return true;
		case R.id.Menu_BoundingSphereModel:
			Config.SHOW_MODEL_BOUNDING_SPHERES ^= true;
			return true;
		case R.id.Menu_EasyMode:
			Config.EASY_MODE ^= true;
			return true;
		case R.id.Menu_Vibrate:
			Config.VIBRATE ^= true;
			return true;
		case R.id.Menu_Loglevel:
			showDialog(DIALOG_ID_LOGLEVEL);
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		int size = menu.size();
		for(int i=0; i<size; i++)
		{
			MenuItem item = menu.getItem(i);
			switch(item.getItemId())
			{
			case R.id.Menu_ShowFPS:
				item.setTitle(Config.SHOW_FPS ? 
						R.string.Menu_ShowFPS_off : 
						R.string.Menu_ShowFPS_on);
				break;
			case R.id.Menu_BoundingSphereSE:
				item.setTitle(Config.SHOW_SCENEENTITY_BOUNDING_SPHERES ? 
						R.string.Menu_BoundingSphereSE_hide : 
						R.string.Menu_BoundingSphereSE_show);
				break;
			case R.id.Menu_BoundingSphereModel:
				item.setTitle(Config.SHOW_MODEL_BOUNDING_SPHERES ? 
						R.string.Menu_BoundingSphereModel_hide : 
						R.string.Menu_BoundingSphereModel_show);
				break;
			case R.id.Menu_EasyMode:
				item.setTitle(Config.EASY_MODE ?
						R.string.Menu_EasyMode_off : 
						R.string.Menu_EasyMode_on);
				break;
			case R.id.Menu_Vibrate:
				item.setTitle(Config.VIBRATE ?
						R.string.Menu_Vibrate_off : 
						R.string.Menu_Vibrate_on);
				break;
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
	
	
	/**
	 * Gets the single instance of LevelActivity.
	 *
	 * @return single instance of LevelActivity
	 */
	public static LevelActivity getInstance()
	{
		return instance;
	}
}
