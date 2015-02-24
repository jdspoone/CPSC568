package casa;

/**
 * Serves to provide a set of constants that correspond to various KQML
 * constructs. Most importantly this class provides a set of basic performatives and
 * their textual equivalents for use when constructing KQML messages. These
 * performatives may be used in either a proactive or reactive context. <p>
 *
 * Also provided are constants representing punctuation used for constructing KQML
 * messages. <p>
 *
 * <FONT SIZE="-1">(!!! need more extensive documentation on expected parameters and responses and so on)</FONT>
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
 */

public interface KQML extends ML
{
  /** corresponds to an open bracket character and signifies the beginning of a KQML message */
  public final static char OPEN  = '(';
  /** corresponds to a close bracket character and signifies the end of a KQML message */
  public final static char CLOSE = ')';
  /** corresponds to a double quote character */
  public final static char QUOTE = '"';
  /** corresponds to a back slash character */
  public final static char BACKSLASH = '\\';


  // *** KQML parameters *** //
  /** parameter indicating a value to act */
  public final static String ACT          = "act";
  /** parameter indicating the sender */
  public final static String SENDER       = "sender";
  /** parameter indicating the receiver */
  public final static String RECEIVER     = "receiver";
  public final static String RECEIVERS    = "receivers";
  /** parameter indicating the who the message is from */
  public final static String FROM         = "from";
  /** parameter indicating the who the message is to */
  public final static String TO           = "to";
  /** parameter indicating a value to reply with */
  public final static String REPLY_WITH   = "reply-with";
  /** parameter indicating a value of the agent */
  public final static String AGENT   = "agent";
  /** parameter indicating a value of the actor */
  public final static String ACTOR   = "actor";
  /**
   * parameter indicating some value that is being replied with,
   * this is usually the same as the REPLY_WITH value of the message that this
   * message is in response to
   */
  public final static String IN_REPLY_TO  = "in-reply-to";
  /** parameter indicating the language/protocol usd in the content of the message */
  public final static String LANGUAGE     = "language";
  /** parameter indicating the ongology used in the content of the message */
  public final static String ONTOLOGY      = "ontology";
  /** parameter indicating the content of the message */
  public final static String CONTENT      = "content";

}
