package nu.shacknet.poseidon.pn.signanzorbit.util.datastructures;

/**
 * The Class Pair.
 *
 * @param <A> the generic type
 * @param <B> the generic type
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class Pair<A,B>
{
	
	/** The first. */
	private A first;
	
	/** The second. */
	private B second;
	
	/**
	 * Instantiates a new pair.
	 *
	 * @param first the first
	 * @param second the second
	 */
	public Pair(A first, B second)
	{
		this.first = first;
		this.second = second;
	}

	/**
	 * Gets the first.
	 *
	 * @return the first
	 */
	public A getFirst()
	{
		return first;
	}

	/**
	 * Sets the first.
	 *
	 * @param first the new first
	 */
	public void setFirst(A first)
	{
		this.first = first;
	}

	/**
	 * Gets the second.
	 *
	 * @return the second
	 */
	public B getSecond()
	{
		return second;
	}

	/**
	 * Sets the second.
	 *
	 * @param second the new second
	 */
	public void setSecond(B second)
	{
		this.second = second;
	}
}