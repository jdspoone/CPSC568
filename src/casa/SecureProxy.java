package casa;

import casa.abcl.ParamsMap;
import casa.exceptions.IPSocketException;
import casa.ui.AgentUI;
import casa.util.Trace;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <code>SecureProxy</code> is a simple subclass of <code>AgentProxy</code>
 * created with the sole purpose of acting as a proxy between the agent that
 * it protects and all other agents.  All messages that it recieves are first
 * filtered to determine if the message is to the proxy (based on the
 * <code>ML.RECEIVER</code> parameter).  If so, the message is processed by the
 * super class as a regular agent.  Otherwise, the message is passed through
 * <code>universallyAccepted()</code> and <code>universallyDenied()</code> in
 * turn to determine if the message should be accepted or denied unequivically.
 * If neither of these functions returns <code>true</code>, the message is
 * passed through the <code>isSenderVerified()</code> and
 * <code>isSenderAuthorized()</code> functions to determine if the message
 * should be passed along.  If it is determined that the message should be
 * passed along, the message is then signed by <code>signMessage()</code> (to
 * prove that the message really was from the proxy) and then passed along to
 * the protected agent by <code>forwardMessage()</code>.
 * <p>
 * The signature (as a <code>String</code>) for a given message is computed by
 * <code>createSignature()</code>.  The signature can then be checked by the
 * protected agent by calling the static function
 * <code>verifySignature()</code> which returns <code>true</code> or
 * <code>false</code> depending on whether the message was received.  The
 * actual signature method is described in the documentation for
 * <code>createSignature()</code> and <code>createHash()</code>.
 *
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
 *
 * @see AgentProxy
 * @author Jason Heard
 */

public class SecureProxy extends AgentProxy {
  /**
   * The private key that the proxy shares with the agent that it is
   * protecting.
   */
  private byte[] key;

  /**
   * The unique number that the next signature will use.
   */
  private long uniqueNumber = 1L;

  /**
   * The name of the agent the new proxy is protecting.
   */
  @SuppressWarnings("unused")
  private String protectedAgent = "";

  /**
   * Creates a new <code>SecureProxy</code> that protects the given agent, uses
   * the given port, and uses the given secret key to sign the messages that
   * this proxy authorizes.
   *
   * @param agentName The name of the agent the new proxy is protecting.
   * @param proxyPort The port that the new proxy should use for
   * communications.
   * @param agentPort The port that the agent uses for communications.
   * @param key The key that the new proxy will use to sign messages that it
   * authorizes for the agent.
   * @throws IPSocketException If an Agent attempts to bind to an IPSocket
   * (port) that doesn't exist or is in use.
   */
  public SecureProxy (ParamsMap params, AgentUI ui) throws Exception {
    super(params, ui);
    in ("SecureProxy.SecureProxy");

    this.protectedAgent = (String)params.getJavaObject("AGENTNAME", String.class);
    this.key = (byte[])params.getJavaObject("KEY",byte[].class);
    out ("SecureProxy.SecureProxy");
  }

  /**
   * Handles all messages that are directed to the protected agent.  For each
   * incoming message, the following procedure is executed:
   * <ol>
   * <li>The message is passed to <code>universallyAccepted()</code> to
   * determine if the message is one which should always be accepted.  If so,
   * the message is accepted and step 6 is executed.</li>
   * <li>If the message is not universally accepted, it is checked to see if it
   * is universally denied.  If so, the message is not accepted and step 7 is
   * executed.</li>
   * <li>If the message is not universally accepted or denied, the message is
   * passed to <code>isSenderVerified()</code> to determine if the sender has
   * been or can be verified as who he or she claims to be.</li>
   * <li>If the sender of the message was verified, it is then passed to
   * <code>isSenderAuthorized()</code> to determine if the specified agent is
   * allowed to send the message to the protected agent.</li>
   * <li>If the message passed both of the previous two function's tests, then
   * the message is accepted and the next step is executed.  Otherwise, step 7
   * is executed.</li>
   * <li>The message is passed along to the protected agent.  The message is
   * digitally signed by <code>signMessage()</code> and then forwarded to the
   * protected agent by <code>forwardMessage()</code>.  A status of 0 is then
   * returned.</li>
   * <li>The message is not passed along to the protected agent and a status of
   * -10 is returned.</li>
   * </ol>
   *
   * @param message The incoming message to be evaluated and possibly forwarded
   * to the protected agent.
   * @return The <code>Status</code> describing if the message was forwarded:
   * <li>0 if the message was forwarded to the protected agent, or</li>
   * <li>-10 if the message was not forwarded for any reason.</li>
   */
  @Override
	protected Status handleForwardMessage (MLMessage message) {
    in ("SecureProxy.handleForwardMessage");
    boolean forwardMessage = false;

    if (universallyAccepted(message)) {
      forwardMessage = true;
    }
    else if (!universallyDenied(message)) {
      if (isSenderVerified(message)) {
        if (isSenderAuthorized(message)) {
          forwardMessage = true;
        }
      }
    }

    if (forwardMessage) {
      signMessage(message);

      try {
        forwardMessage(message);
      }
      catch (Exception ex) {
      	Trace.log("error", "Error sending message", ex);
      }
    }
    else {
      out ("SecureProxy.handleForwardMessage");
      return new Status(-10);
    }

    out ("SecureProxy.handleForwardMessage");
    return new Status(0);
  }

  /**
   * Determines whether the given message is one that should be universally
   * accepted.  Returns <code>true</code> if the message should be universally
   * accepted; <code>false</code> otherwise.  There are no universally accepted
   * messages by default.
   *
   * @param message The message to check.
   * @return <code>true</code> if the message should be universally accepted;
   * <code>false</code> otherwise.
   */
  protected boolean universallyAccepted (MLMessage message) {
    in ("SecureProxy.universallyAccepted");
    out ("SecureProxy.universallyAccepted");
    return false;
  }

  /**
   * Determines whether the given message is one that should be universally
   * denied.  Returns <code>true</code> if the message should be universally
   * denied; <code>false</code> otherwise.  By default, any message with the
   * act "exit" are denied, since this message is sent by agents as they shut
   * down.
   *
   * @param message The message to check.
   * @return <code>true</code> if the message should be universally denied;
   * <code>false</code> otherwise.
   */
  protected boolean universallyDenied (MLMessage message) {
    in ("SecureProxy.universallyDenied");
    String act = message.getParameter (ML.ACT);

    if (act.equals (ML.EXIT)) {
      out ("SecureProxy.universallyDenied");
      return true;
    }
    out ("SecureProxy.universallyDenied");
    return false;
  }

  /**
   * Verifies the sender of the given message, returning <code>true</code> if
   * the sender is verified; <code>false</code> otherwise.  All senders are
   * automatically verified by default.
   *
   * @param message The message containing the sender that should be verified.
   * @return <code>true</code> if the sender is verified; <code>false</code>
   * otherwise.
   */
  protected boolean isSenderVerified (MLMessage message) {
    in ("SecureProxy.isSenderVerified");
    out ("SecureProxy.isSenderVerified");
    return true;
  }

  /**
   * Determines if the sender of the given message is authorized to send it to
   * the protected agent, returning <code>true</code> if the sender is
   * authorized to send the given message to the protected agent;
   * <code>false</code> otherwise.  All senders are authorized to send any
   * message by default.
   *
   * @param message The message containing the sender that should be
   * authorized.
   * @return <code>true</code> if the sender is authorized to send the given
   * message to the protected agent; <code>false</code> otherwise.
   */
  protected boolean isSenderAuthorized (MLMessage message) {
    in ("SecureProxy.isSenderAuthorized");
    out ("SecureProxy.isSenderAuthorized");
    return true;
  }

  /**
   * Digitally signs the given message by creating a unique signature for the
   * given message and then attaching that signature to the message.  The
   * signature is created by <code>createSignature()</code> and then attached
   * to the message as the<code>ML.SIGNATURE</code> parameter.
   *
   * @param message The message to sign.
   */
  protected void signMessage (MLMessage message) {
    in ("SecureProxy.signMessage");
    message.setParameter (ML.SIGNATURE, createSignature (message));
    out ("SecureProxy.signMessage");
  }

  /**
   * Creates an signature for the given message.  The signature is a collection
   * of 21 signed numbers, each as a <code>String</code> with a space between
   * each.  The first number is a positive <code>long</code> that should be
   * unique for each message that is sent with the same key.  In this
   * implimentation this is increased by one for each message that is sent.
   * The remaining 20 numbers are the byte array returned by
   * <code>createHash()</code> with each byte as a seperate number in the
   * <code>String</code>.
   *
   * @param message The message for which we are creating a signature.
   * @return The signature for the given message as a <code>String</code>.
   */
  private synchronized String createSignature (MLMessage message) {
    in ("SecureProxy.createSignature");
    StringBuffer output = new StringBuffer ();

    output.append (Long.toString (uniqueNumber));
    output.append (' ');

    byte[] hash = createHash (message, key, uniqueNumber);

    for (int i = 0; i < hash.length; i++) {
      output.append (Byte.toString (hash[i]));
      output.append (' ');
    }

    uniqueNumber++;

    out ("SecureProxy.createSignature");

    return output.toString ();
  }

  /**
   * Verifies the signature contained in the given message using the given key.
   * The signature is pulled from the given message (the
   * <code>ML.SIGNATURE</code> parameter).  If the signature does not exist in
   * the message, -1 is returned.  Next, the first number is converted from a
   * <code>String</code> to a <code>long</code> and is used as the unique
   * number for this signature.  Next, the rest of the signature is calculated
   * by <code>createHash()</code>.  Then each number (<code>byte</code>) in the
   * actual signature is compared to the expected signature.  If they match and
   * there is not extra data in the given message's signature, the unique
   * number is returned.  Otherwise, -1 is returned to indicate that the
   * message has an invalid signature.
   *
   * @param message The message containing the signature to verify.
   * @param key The secret key that the message should have been signed with.
   * @return The positive unique number that was used to sign the message if
   * the message was verified; negative otherwise (if the signature was
   * invalid).
   */
  public static long verifySignature (MLMessage message, byte[] key) {
    String signatureString = message.getParameter (ML.SIGNATURE);

    if (signatureString == null) {
      return -1;
    }

    TokenParser parser = new TokenParser (signatureString);

    String temp = parser.getNextToken ();
    long uniqueNumber = Long.parseLong (temp);

    byte[] hash = createHash (message, key, uniqueNumber);

    boolean ok = true;
    for (int i = 0; i < hash.length && ok; i++) {
      ok = parser.getNextToken ().equals (Byte.toString (hash[i]));
    }

    if (parser.getRemaining () != null) {
      return -1;
    }

    return ok ? uniqueNumber : -1;
  }

  /**
   * Creates a 20 byte hash from the given message, key, and unique number.
   * This is used as the basis of all of the signatures that the proxy uses.
   * The procedure for creating the hash is as follows:
   * <ol>
   * <li>Create a 4 byte array from the unique number.</li>
   * <li>Create a byte array for each of the following parameters:</li>
   * <ul>
   * <li><code>ML.PERFORMATIVE</code></li>
   * <li><code>ML.ACT</code></li>
   * <li><code>ML.SENDER</code></li>
   * </ul>
   * <li>Concatinate the unique number byte array, the key (which was given as
   * a byte array), and all of the parameters in order into one byte array.</li>
   * <li>Pass the final byte array through the SHA-1 message digest
   * algorithm.</li>
   * <li>Return the byte array returned by the message digest algorithm.</li>
   * </ol>
   *
   * @param message The message for which we are creating a signature.
   * @param key The secret key that the message should have been signed with.
   * @param uniqueNumber A positive <code>long</code> that should be unique for
   * each message that is sent with the same key.
   * @return The byte array as calculated above.
   */
  private static byte[] createHash (MLMessage message, byte[] key,
                                    long uniqueNumber) {
    byte[] longAsBytes = new byte[4];
    long tempLong = uniqueNumber;
    for (int i = 3; i > -1; i--) {
      longAsBytes[i] = (byte) (tempLong % 256);
      tempLong = tempLong / 256;
    }

    byte[][] parameters = new byte[3][];
    try {
      parameters[0] = message.getParameter (ML.PERFORMATIVE).getBytes ();
    } catch (NullPointerException e) {
      parameters[0] = new byte[0];
    }
    try {
      parameters[1] = message.getParameter (ML.ACT).getBytes ();
    } catch (NullPointerException e) {
      parameters[1] = new byte[0];
    }
    try {
      parameters[2] = message.getParameter (ML.SENDER).getBytes ();
    } catch (NullPointerException e) {
      parameters[2] = new byte[0];
    }

    int length = 4 + key.length;
    for (int i = 0; i < 3; i++) {
      length += parameters[i].length;
    }

    byte[] toBeHashed = new byte[length];
    int pos = 0;
    System.arraycopy (longAsBytes, 0, toBeHashed, pos, 4);
    pos += 4;
    System.arraycopy (key, 0, toBeHashed, pos, key.length);
    pos += key.length;
    for (int i = 0; i < 3; i++) {
      System.arraycopy (parameters[i], 0, toBeHashed, pos, parameters[i].length);
      pos += parameters[i].length;
    }

    // Show the bytes that will be hashed
//    casa.util.CASAUtil.printByteArrayAsHex(toBeHashed, "To be hashed:");

    MessageDigest hasher = null;
    try {
      hasher = MessageDigest.getInstance ("SHA");
    } catch (NoSuchAlgorithmException e) {
    	Trace.log("error", "SecureProxy.createHash()", e);
    }
    hasher.update (toBeHashed);
    byte[] hash = hasher.digest ();
    return hash;
  }

}
