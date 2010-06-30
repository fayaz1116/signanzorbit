package nu.shacknet.poseidon.pn.signanzorbit.util.managers;

import nu.shacknet.poseidon.pn.signanzorbit.config.Config;

/**
 * The Class TimeManager.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class TimeManager
{
	/** The time of the last frame. */
	long t;
	
	/** Also the time of the last frame. */
	long t0;
	
	/** The duration of the last frame. */
	long dt;
	
	/** The time when the remainingGameTime was calculated the last time. */
	long remainingGameT0;
	
	/** The remaining game time. */
	long remainingGameTime;
	
	/** Whether the remaining game time changed in the current frame */
	boolean remainingGameTimeChanged;
	
	/** The time when the fps were calculated the last time. */
	long fpsT0;
	
	/** The fps. */
	float fps;
	
	/** Whether the fps changed. */
	boolean fpsChanged;
	
	/** The number of frames since the fps were calculated the last time. */
	int frames;
	
	/** The Constant instance. */
	public static final TimeManager instance = new TimeManager();
	
	/**
	 * Instantiates a new time manager.
	 */
	private TimeManager()
	{
		reset(true);
	}
	
	/**
	 * Reset.
	 *
	 * @param gameTimeAlso whether the remaining game time should be reset as well
	 */
	public synchronized void reset(boolean gameTimeAlso)
	{
		if(gameTimeAlso)
			remainingGameTime = Config.GAMETIME;
		
		t0 = System.currentTimeMillis();
		t = t0;
		dt = 0;
		fpsT0 = t0;
		frames = 0;
		fps = 0;
		fpsChanged = false;
	}
	
	/**
	 * Update.
	 */
	public synchronized void update()
	{
		t = System.currentTimeMillis();
		
		// delta time since last frame
		dt = t-t0;
		
		remainingGameTime -= dt;
		
		if((t-remainingGameT0) >= Config.GAMETIME_UPDATE_INTERVAL)
			remainingGameTimeChanged = true;
		else
			remainingGameTimeChanged = false;

		// calc fps if FPS_UPDATE_INTERVAL seconds have passed since the last calculation
		if((t-fpsT0) >= Config.FPS_UPDATE_INTERVAL)
		{
			// round to xxxx.xx
			fps = (float)((Math.ceil((((float)frames) / (((float)(t-fpsT0)) / 1000.0f))*100.0f)) / 100.0);
			frames = 0;
			fpsT0 = t;
			fpsChanged = true;
		}
		else
			fpsChanged = false;

		// increment frame counter (for fps calculation)
		frames++;
		
		// set the current time as the last time for the next frame
		t0 = t;
	}
	
	
	/**
	 * @return the duration of the last frame in Milliseconds
	 */
	public synchronized long getDeltaTmillis()
	{
		return dt;
	}
	
	/**
	 * @return the duration of the last frame in Seconds
	 */
	public synchronized float getDeltaTsec()
	{
		return ((float)dt)/1000.0f;
	}
	
	/**
	 * Gets the fPS.
	 *
	 * @return the fPS
	 */
	public synchronized float getFPS()
	{
		return fps;
	}
	
	/**
	 * Have fp schanged.
	 *
	 * @return true, if fps were calculated in this frame
	 */
	public synchronized boolean haveFPSchanged()
	{
		return fpsChanged;
	}
	
	/**
	 * Gets the remaining game time.
	 *
	 * @return the remaining game time
	 */
	public synchronized long getRemainingGameTime()
	{
		return remainingGameTime;
	}
	
	/**
	 * Checks for remaining game time changed.
	 *
	 * @return true, if game time was calculated in this frame
	 */
	public synchronized boolean hasRemainingGameTimeChanged()
	{
		return remainingGameTimeChanged;
	}

	/**
	 * Gets the time of last frame.
	 *
	 * @return the time of last frame
	 */
	public synchronized long getTimeOfLastFrame()
	{
		return t;
	}
		
}
