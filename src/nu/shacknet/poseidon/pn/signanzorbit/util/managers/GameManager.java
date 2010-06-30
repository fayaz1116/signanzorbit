package nu.shacknet.poseidon.pn.signanzorbit.util.managers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import nu.shacknet.poseidon.pn.signanzorbit.util.Persistable;


/**
 * The Class GameManager.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class GameManager implements Persistable{

	/** The number of parts. */
	private int parts;
	
	/** The current score. */
	private int score;
	
	/** whether score has changed. */
	private boolean scoreHasChanged;
	
	/** The instance. */
	public static GameManager instance = new GameManager();
	
	/**
	 * Instantiates a new game manager.
	 */
	public GameManager(){
		this.score = 0;
		this.parts = 0;
		this.scoreHasChanged = false;
	}
	
	/**
	 * Inits this.
	 *
	 * @param parts the parts
	 */
	public void init(int parts){
		this.score = 0;
		this.parts = parts;
	
		this.scoreHasChanged = false;
	}
	
	/**
	 * Sets the score.
	 *
	 * @param scorePercent the new score
	 */
	public synchronized void setScore(float scorePercent){
		this.score = Math.round((scorePercent*parts)/100);
		scoreHasChanged=true;
	}
	
	/**
	 * Gets the score percent.
	 *
	 * @return the score percent
	 */
	public synchronized float getScorePercent(){
		return (score*100)/parts;
	}
	
	/**
	 * Gets the score.
	 *
	 * @return the score
	 */
	public synchronized int getScore(){
		return score;
	}
	
	/**
	 * Checks if is complete.
	 *
	 * @return true, if is complete
	 */
	public synchronized boolean isComplete(){
		return score==parts;
	}
	
	/**
	 * Increment score.
	 */
	public synchronized void incScore(){
		score++;
		scoreHasChanged=true;
	}
	
	/**
	 * whether the score has changed.
	 *
	 * @return true, if successful
	 */
	public synchronized boolean scoreHasChanged(){
		if(scoreHasChanged)
		{
			scoreHasChanged = false;
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.util.Persistable#persist(java.io.DataOutputStream)
	 */
	@Override
	public void persist(DataOutputStream dos) throws IOException {
		dos.writeInt(parts);
		dos.writeInt(score);
		dos.writeBoolean(scoreHasChanged);
	}

	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.util.Persistable#restore(java.io.DataInputStream)
	 */
	@Override
	public void restore(DataInputStream dis) throws IOException {
		parts = dis.readInt();
		score = dis.readInt();
		scoreHasChanged = dis.readBoolean();
	}
	
}
