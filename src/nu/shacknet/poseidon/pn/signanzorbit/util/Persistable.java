package nu.shacknet.poseidon.pn.signanzorbit.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The Interface Persistable.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public interface Persistable
{
	
	/**
	 * Persist.
	 *
	 * @param dos the dos
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void persist(DataOutputStream dos) throws IOException;
	
	/**
	 * Restore.
	 *
	 * @param dis the dis
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void restore(DataInputStream dis) throws IOException;
}
