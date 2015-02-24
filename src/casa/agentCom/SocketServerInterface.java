package casa.agentCom;

/**
 * An abstract socket server.
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public interface SocketServerInterface {

	/**
	 * Retrieves the address of the listening socket
	 *
	 * @returns address of the listening socket
	 */
	public abstract String getHostAddress();

	/**
	 * Retrieves the port of the listening socket
	 *
	 * @returns port of the listening socket
	 */
	public abstract String getLocalPortAsString();

	/**
	 * Retrieves the port of the listening socket
	 * @returns port of the listening socket
	 */
	public abstract int getLocalPort();

	public abstract void exit();

	/**
	 * Method to close the socket server's open port and return it to the system.
	 */
	public abstract void closePort();

	public abstract boolean isAlive();

	public abstract void interrupt();

}