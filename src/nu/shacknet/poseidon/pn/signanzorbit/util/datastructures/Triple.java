package nu.shacknet.poseidon.pn.signanzorbit.util.datastructures;

/**
 * The Class Triple.
 *
 * @param <A> the generic type
 * @param <B> the generic type
 * @param <C> the generic Type
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class Triple<A,B,C>
{
	
	/** The first. */
	private A first;
	
	/** The second. */
	private B second;
	
	/** The third. */
	private C third;
	
	/**
	 * Instantiates a new triple.
	 *
	 * @param first the first
	 * @param second the second
	 * @param third the third
	 */
	public Triple(A first, B second, C third)
	{
		this.first = first;
		this.second = second;
		this.third = third;
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

	/**
	 * @return the third
	 */
	public C getThird()
	{
		return third;
	}

	/**
	 * @param third the third to set
	 */
	public void setThird(C third)
	{
		this.third = third;
	}
	
	
}