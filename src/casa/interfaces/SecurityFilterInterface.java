package casa.interfaces;
/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary.
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  The
 * Knowledge Science Group makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is" without express or
 * implied warranty.</p>
 *
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

import java.util.List;

import casa.AbstractProcess;
import casa.MLMessage;

/**
 * Defines the interface used by security hooks to filter incoming
 * and outgoing messages for signing and encryption.
 * @author kremer
 */
public interface SecurityFilterInterface {
	/**
	 * @param msg The incoming or outgoing message
	 * @param agent The agent sending or recieving the message
	 * @param incoming true if the message is incoming, false if it's outgoing
	 * @return The encoded and signed (if outgoing) or decoded and verified (if incoming) message
	 */
	public MLMessage processMessage(MLMessage msg, PolicyAgentInterface agent, boolean incoming);
  /**
   * @param level 0=none; 1=signed; 2=contentEncrypted 3=fullyEncrypted
   * @return the original level
   */
  public int setSecurityLevel(int level);
  /**
   * Set weather or not to demand incomming messages should be signed to avoid rejection.
   * @param required 
   * @return the original setting
   */
  public boolean requireIncomingSigning(boolean required);
  /**
   * Sets the default signature algorithm to use on outgoing messages
   * @param name
   * @return true iff the algorithm is know and can be applied
   */
  public boolean setDefSignatureAlgorithm(String name);
  /**
   * Inserts the algorithm at location <em>location</em> in the prioritized list of
   * algorithms.
   * @param alg The algorthm
   * @param location the location: 0...
   * @return true iff the algorithm is know and can be used
   */
  public boolean insertEncryptionAlgorithm(EncryptionAlgorithm alg, int location);
  /**
   * Deletes the specificed encryption algorithm
   * @param alg
   * @return true iff it was deleted
   */
  public boolean deleteEncrytpionAlgorithm(EncryptionAlgorithm alg);
  /**
   * @return The list of algorithms
   */
  public List<EncryptionAlgorithm> getEncryptionAlgorithms();
  /**
   * @author kremer
   * A description of an encryption algorithm
   */
  public interface EncryptionAlgorithm {
  	/**
  	 * @return the key size
  	 */
  	public int getSize();
  	/**
  	 * @return The name of the algorithm or "none"
  	 */
  	public String getName();
 	  /**
 	   * @return The mode
 	   */
 	  public String getMode();
 	  /**
 	   * @return The padding scheme
 	   */
 	  public String getPaddingScheme();
  }
}
