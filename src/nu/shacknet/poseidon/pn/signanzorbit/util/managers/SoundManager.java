package nu.shacknet.poseidon.pn.signanzorbit.util.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import nu.shacknet.poseidon.pn.signanzorbit.LevelActivity;
import nu.shacknet.poseidon.pn.signanzorbit.config.Config;
import nu.shacknet.poseidon.pn.signanzorbit.util.datastructures.Pair;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

/**
 * The Class manages audio output.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class SoundManager 
{
	
	/** Instance of this Manager. */
	public static final SoundManager instance = new SoundManager();
	
	/** The context. */
	private Context context;
			
	/** Currently ready MediaPlayers for each resID. */
	private HashMap<Integer, ArrayList<MediaPlayer>> players;
	
	/** The available players. */
	private ArrayList<Pair<MediaPlayer,Integer>> availablePlayers;
	
	/** The paused players. */
	private ArrayList<MediaPlayer> pausedPlayers;
	
	/**
	 * Default Constructor.
	 *
	 */
	private SoundManager()
	{
		
	}
	
	/**
	 * Prepare player for a given res id
	 *
	 * @param resID the res id
	 * @return the media player
	 */
	private synchronized MediaPlayer preparePlayer(int resID)
	{
		LogManager.i("Preparing player for " + resID);
		MediaPlayer myPlayer = null;
		
		/*
		 * Find a player
		 */
		ArrayList<Pair<MediaPlayer,Integer>> availablePlayers = this.availablePlayers;
		HashMap<Integer, ArrayList<MediaPlayer>> players = this.players;
		
		int totalNumberOfPlayers = availablePlayers.size();
		for(int i=0; i<totalNumberOfPlayers; i++)
		{
			Pair<MediaPlayer,Integer> p = availablePlayers.get(i);
			
			MediaPlayer currentPlayer = p.getFirst();
			Integer oldGroupObject = p.getSecond();
			int oldGroup = oldGroupObject != null ? oldGroupObject : -1;
			
			if(!currentPlayer.isPlaying() && oldGroup != resID)
			{
				myPlayer = currentPlayer;
				
				// move it to the back of the list, for faster finding of new players..
				availablePlayers.remove(i);
				availablePlayers.add(p);
				
				// remove it from its old group
				if(oldGroup != -1)
				{
					LogManager.i("Removing player from group " + oldGroup + " in favor of " + resID);
					players.get(oldGroup).remove(currentPlayer);
				}
				
				// put it into the new group
				p.setSecond(resID);
				ArrayList<MediaPlayer> playersForThisResID = players.get(resID);
				if(playersForThisResID == null)
				{
					playersForThisResID = new ArrayList<MediaPlayer>();
					players.put(resID, playersForThisResID);
				}
				playersForThisResID.add(myPlayer);
				
				break;
			}
		}
		
		/*
		 * If there was a free player, re-initialize it
		 */
		if(myPlayer != null)
		{
			myPlayer.reset();
			
			AssetFileDescriptor fd = context.getResources().openRawResourceFd(resID);
			try
			{
				try
				{
					myPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
				}
				catch(IllegalStateException e)
				{
					myPlayer.reset();
					myPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
				}
				myPlayer.prepare();
				
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				try { fd.close(); } catch (IOException e) {}
			}
		}
		
		return myPlayer;
	}
	
	/**
	 * Play sound.
	 *
	 * @param resID the res id
	 * @return the media player
	 */
	public synchronized MediaPlayer playSound(int resID)
	{
		if(!LevelActivity.SOUND_ENABLED)
			return null;
		
		MediaPlayer myPlayer = null;
		
		// Try to find a player for this resource
		ArrayList<MediaPlayer> playersForThisRes = players.get(resID);
		if(playersForThisRes != null)
		{
			int numberOfPlayersForThisRes = playersForThisRes.size();
			for(int i=0; i<numberOfPlayersForThisRes; i++)
			{
				MediaPlayer currentPlayer = playersForThisRes.get(i);
				if(!currentPlayer.isPlaying())
				{
					myPlayer = currentPlayer;
					break;
				}
			}
		}
		
		if(myPlayer == null)
			myPlayer = preparePlayer(resID);
		
		// check if we found a player
		if(myPlayer != null)
		{
			myPlayer.seekTo(0);
			myPlayer.start();
		}
		return myPlayer;
	}
	
	/**
	 * On create.
	 *
	 * @param context the context
	 */
	public synchronized void onCreate(Context context)
	{
		this.context = context;
		players = new HashMap<Integer, ArrayList<MediaPlayer>>();
		availablePlayers = new ArrayList<Pair<MediaPlayer,Integer>>();
		pausedPlayers = new ArrayList<MediaPlayer>();
		
		// create a fixed number of players
		for(int i=0; i<20; i++)
			availablePlayers.add(new Pair<MediaPlayer, Integer>(new MediaPlayer(),null));
		
		// prepare two players for each audio resource
		for(int j=0;j<Config.SOUND_LIST.length;j++)
		{
			preparePlayer(Config.SOUND_LIST[j]);
			preparePlayer(Config.SOUND_LIST[j]);
		}
	}
	
	/**
	 * On resume.
	 */
	public synchronized void onResume()
	{
		ArrayList<MediaPlayer> pausedPlayers = this.pausedPlayers;
	
		int size = pausedPlayers.size();
		for(int i=0; i<size; i++)
			pausedPlayers.get(i).start();
	}
	
	/**
	 * On pause.
	 */
	public synchronized void onPause()
	{
		ArrayList<Pair<MediaPlayer, Integer>> availablePlayers = this.availablePlayers;
		ArrayList<MediaPlayer> pausedPlayers = this.pausedPlayers;
		pausedPlayers.clear();
		
		int size = availablePlayers.size();
		for(int i=0;i<size;i++)
		{
			MediaPlayer currentPlayer = availablePlayers.get(i).getFirst();
			if(currentPlayer.isPlaying())
			{
				currentPlayer.pause();
				pausedPlayers.add(currentPlayer);
			}
		}
	}
	
	/**
	 * On destroy.
	 */
	public synchronized void onDestroy()
	{
		ArrayList<Pair<MediaPlayer, Integer>> availablePlayers = this.availablePlayers;
		int size = availablePlayers.size();
		for(int i=0;i<size;i++)
		{
			MediaPlayer currentPlayer = availablePlayers.get(i).getFirst();
			if(currentPlayer.isPlaying())
				currentPlayer.stop();
			currentPlayer.release();
		}
	}
}
