package nu.shacknet.poseidon.pn.signanzorbit.util;

/**
 * The Class MathUtil.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class MathUtil
{
	/**
	 * Evaluates the following funktion:
	 * sin(sin(t*PI/2)*PI-PI/2)/2+0.5
	 * @param t the parameter of the function
	 * @return	the result of the function
	 */
	public static native float forcePulseFunktion(float t);
}
