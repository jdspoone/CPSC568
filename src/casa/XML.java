package casa;


/**
 * Title:        CASA
 * Description:  Provides the necessary constants constructing an XML message.
 *               The XML tags are designed based on those used in the implementation
 *               of KQML, and kept as close as possible to their KQML counterparts.
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
 * @author <a href="laf@cpsc.ucalgary.ca">Chad La Fournie</a>
 *
 * @todo Implementing the version attribute for each independant tag: casa Message
 *                                                                    language
 *                                                                    Ontology
 *       This may be difficult because the sender will have to mark each message
 *       with the version of the convsation structures being used and the reciever
 *       must then parse this out and process this information.
 *
 *       Implications :  This may mean that the sender, if he understands older conversation
 *                       structure, may need to translate the message into the older conversation
 *                       structure or request translation from another agent in order to  change a
 *                       message into an older or newer version of a conversation structure.
 *
 */

public interface XML extends ML {

  /**
   * The first line of an XML document. Tells the parser that this is an XML
   * document.
   * @deprecated Now JDOM creates the XML file for us.
   */
	@Deprecated
  public final static String HEADER  = "<?xml version=\"1.0\"?>";

  /**
   * The second line of an XML document. Points the XML parser to the DTD.
   * @deprecated Now JDOM creates the DOCTYPE part for us, for our DTD file
   * location, use XML.MESSAGE_DTD.
   */
	@Deprecated
  public final static String DOCTYPE = "<!DOCTYPE message SYSTEM \"xmlmessage.dtd\">";

  public final static String MESSAGE_DTD = "CASAMessage.dtd";


  /**
  * Characters necessary for XML encoding
  */

  /** Corresponds to a ascii space character */
  public final static char BLANK = ' ';
  /** Corresponds to an open bracket character and signifies the beginning of a XML tag. */
  public final static char OPENTAG  = '<';
  /** Corresponds to a close bracket character and signifies the end of a XML tag. */
  public final static char CLOSETAG = '>';
  /** Corresponds to a double quote character. */
  public final static char QUOTE = '"';
  /** Corresponds to an equals character. */
  public final static char EQUAL = '=';
  /** Corresponds to a forward slash character. */
  public final static char SLASH = '/';
  /** Corresponds to an endline character. */
  public final static char ENDLINE = '\n';


  /**
  * XML parameters: the text to be used for tags
  */

  /**
   * String designating the beginning of the portion of the XMLMessage NOT to
   * be parsed (the contents of the message).
   */
  public final static String OPEN_CDATA = "<![CDATA[";
  /**
   * String designating the end of the portion of the XMLMessage NOT to be
   * parsed.
   */
  public final static String CLOSE_CDATA = "]]>";

  /** Tag that encases entire message. */
  public final static String MESSAGE = "CASAmessage";

  //
  // Authentication Section - CASA user's information
  //
  /** Tag indicating the userName of the user to authenticate. */
  public final static String USERNAME = "userName";
  /** Tag indicating the MAC address of the user to authenticate. */
  public final static String MAC      = "mac";
  /** Tag indicating the password of the user to authenticate. */
  public final static String PASSWORD = "password";

  /**
   * constant for the version attribute.  This attribute is
   * part of casaMessage, language and ontology.
   * @ see xmlmessage
   */
  public final static String VERSION = "version";

  /**
   * Temporary constant for describing the versions of the various conversation
   * structures: casaMessage, language, ontology.
   */
  public final static String CS_VERSION = "1.0";
}// XML
