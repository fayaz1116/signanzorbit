package nu.shacknet.poseidon.pn.signanzorbit.util.managers;

import nu.shacknet.poseidon.pn.signanzorbit.config.Config;
import android.util.Log;

/**
 * The Class LogManager.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class LogManager
{
	/** The Constant TAG. */
	public static final String TAG = "Signanzorbit";

	/**
	 * verbose message.
	 *
	 * @param msg the msg
	 */
	public static void v(Object msg)
	{
		if(Config.LOGLEVEL <= Config.VERBOSE)
			Log.v(TAG, msg.toString());
	}

	/**
	 * verbose message.
	 *
	 * @param msg the msg
	 * @param t the t
	 */
	public static void v(Object msg, Throwable t)
	{
		if(Config.LOGLEVEL <= Config.VERBOSE)
			Log.v(TAG, msg.toString(), t);
	}

	/**
	 * verbose message with tag.
	 *
	 * @param tag the tag
	 * @param msg the msg
	 */
	public static void vTag(String tag, Object msg)
	{
		if(Config.LOGLEVEL <= Config.VERBOSE)
			Log.v(tag, msg.toString());
	}

	/**
	 * verbose message with tag.
	 *
	 * @param tag the tag
	 * @param msg the msg
	 * @param t the t
	 */
	public static void vTag(String tag, Object msg, Throwable t)
	{
		if(Config.LOGLEVEL <= Config.VERBOSE)
			Log.v(tag, msg.toString(), t);
	}

	/**
	 * debug message.
	 *
	 * @param msg the msg
	 */
	public static void d(Object msg)
	{
		if(Config.LOGLEVEL <= Config.DEBUG)
			Log.d(TAG, msg.toString());
	}

	/**
	 * debug message.
	 *
	 * @param msg the msg
	 * @param t the t
	 */
	public static void d(Object msg, Throwable t)
	{
		if(Config.LOGLEVEL <= Config.DEBUG)
			Log.d(TAG, msg.toString(), t);
	}

	/**
	 * debug message with tag.
	 *
	 * @param tag the tag
	 * @param msg the msg
	 */
	public static void dTag(String tag, Object msg)
	{
		if(Config.LOGLEVEL <= Config.DEBUG)
			Log.d(tag, msg.toString());
	}

	/**
	 * debug message with tag.
	 *
	 * @param tag the tag
	 * @param msg the msg
	 * @param t the t
	 */
	public static void dTag(String tag, Object msg, Throwable t)
	{
		if(Config.LOGLEVEL <= Config.DEBUG)
			Log.d(tag, msg.toString(), t);
	}

	/**
	 * info message.
	 *
	 * @param msg the msg
	 */
	public static void i(Object msg)
	{
		if(Config.LOGLEVEL <= Config.INFO)
			Log.i(TAG, msg.toString());
	}

	/**
	 * info message.
	 *
	 * @param msg the msg
	 * @param t the t
	 */
	public static void i(Object msg, Throwable t)
	{
		if(Config.LOGLEVEL <= Config.INFO)
			Log.i(TAG, msg.toString(), t);
	}

	/**
	 * info message with tag.
	 *
	 * @param tag the tag
	 * @param msg the msg
	 */
	public static void iTag(String tag, Object msg)
	{
		if(Config.LOGLEVEL <= Config.INFO)
			Log.i(tag, msg.toString());
	}

	/**
	 * info message with tag.
	 *
	 * @param tag the tag
	 * @param msg the msg
	 * @param t the t
	 */
	public static void iTag(String tag, Object msg, Throwable t)
	{
		if(Config.LOGLEVEL <= Config.INFO)
			Log.i(tag, msg.toString(), t);
	}

	/**
	 * warning message.
	 *
	 * @param msg the msg
	 */
	public static void w(Object msg)
	{
		if(Config.LOGLEVEL <= Config.WARN)
			Log.w(TAG, msg.toString());
	}

	/**
	 * warning message.
	 *
	 * @param msg the msg
	 * @param t the t
	 */
	public static void w(Object msg, Throwable t)
	{
		if(Config.LOGLEVEL <= Config.WARN)
			Log.w(TAG, msg.toString(), t);
	}

	/**
	 * warning message with tag.
	 *
	 * @param tag the tag
	 * @param msg the msg
	 */
	public static void wTag(String tag, Object msg)
	{
		if(Config.LOGLEVEL <= Config.WARN)
			Log.w(tag, msg.toString());
	}

	/**
	 * warning message with tag.
	 *
	 * @param tag the tag
	 * @param msg the msg
	 * @param t the t
	 */
	public static void wTag(String tag, Object msg, Throwable t)
	{
		if(Config.LOGLEVEL <= Config.WARN)
			Log.w(tag, msg.toString(), t);
	}

	/**
	 * error message.
	 *
	 * @param msg the msg
	 */
	public static void e(Object msg)
	{
		if(Config.LOGLEVEL <= Config.ERROR)
			Log.e(TAG, msg.toString());
	}

	/**
	 * error message.
	 *
	 * @param msg the msg
	 * @param t the t
	 */
	public static void e(Object msg, Throwable t)
	{
		if(Config.LOGLEVEL <= Config.ERROR)
			Log.e(TAG, msg.toString(), t);
	}

	/**
	 * error message with tag.
	 *
	 * @param tag the tag
	 * @param msg the msg
	 */
	public static void eTag(String tag, Object msg)
	{
		if(Config.LOGLEVEL <= Config.ERROR)
			Log.e(tag, msg.toString());
	}

	/**
	 * error message with tag.
	 *
	 * @param tag the tag
	 * @param msg the msg
	 * @param t the t
	 */
	public static void eTag(String tag, Object msg, Throwable t)
	{
		if(Config.LOGLEVEL <= Config.ERROR)
			Log.e(tag, msg.toString(), t);
	}
}