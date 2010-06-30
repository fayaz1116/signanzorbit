package nu.shacknet.poseidon.pn.signanzorbit.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Class Synchronizer.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class Synchronizer
{
	/** The lock. */
	private ReentrantLock lock = new ReentrantLock();
	
	/** The prerender Condition. */
	private Condition prerender = lock.newCondition();
	
	/** The logic Condition. */
	private Condition logic = lock.newCondition();
	
	/** The disable Condition */
	private Condition disableCond = lock.newCondition();
	
	/** whether the logic frame is finished */
	private boolean logicFinished = false;

	// init lastLogicFrame one lower than lastPreRenderFrame,
	// to let the logic frame run once before starting to render
	/** the last frame the logic thread processed */
	private int lastLogicFrame = -1;
	
	/** the last frame the render frame processed */
	private int lastPreRenderFrame = 0;
	
	/** whether the threads are running or not */
	public boolean running = true;

	/**
	 * Reset.
	 */
	public void reset()
	{
		lock.lock();
		prerender.signal();
		logic.signal();
		lastLogicFrame = -1;
		lastPreRenderFrame = 0;
		lock.unlock();
	}
	
	/**
	 * Wait for logic.
	 */
	public void waitForLogic()
	{
		if(!running)
			return;
		lock.lock();
		try
		{
			while(lastPreRenderFrame!=lastLogicFrame)
				logic.await();
		}
		catch(InterruptedException e)
		{
		}
		lock.unlock();
	}

	/**
	 * Wait for pre render.
	 */
	public void waitForPreRender()
	{
		if(!running)
			return;
		lock.lock();
		try
		{
			while(lastPreRenderFrame==lastLogicFrame)
				prerender.await();
		}
		catch(InterruptedException e)
		{
		}
		lock.unlock();
	}

	/**
	 * Pre render done.
	 */
	public void preRenderDone()
	{
		lock.lock();
		lastPreRenderFrame++;
		disableCond.signal();
		prerender.signal();
		lock.unlock();
	}

	/**
	 * Logic done.
	 */
	public void logicDone()
	{
		lock.lock();
		lastLogicFrame++;
		disableCond.signal();
		logic.signal();
		lock.unlock();
	}

	/**
	 * called from the logic thread just before it exists
	 */
	public void logicThreadFinished()
	{
		lock.lock();
		logicFinished = true;
		disableCond.signal();
		lock.unlock();
	}
	
	/**
	 * Toggles this Synchronizer active.
	 * waits for threads to finish on disable
	 *
	 * @param running the new active
	 */
	public void setActive(boolean running)
	{
		if(this.running == running)
			return;

		lock.lock();
		this.running = running;
		if(!running)
		{
			int targetPreRenderFrame = lastPreRenderFrame;
			try
			{
				while(!logicFinished || lastPreRenderFrame <= targetPreRenderFrame)
					disableCond.await();
			}
			catch(InterruptedException e)
			{
			}
		}
		else
		{
			logicFinished = false;
		}
		lock.unlock();
	}
}
