package casa;

import casa.exceptions.MLMessageFormatException;
import casa.exceptions.XMLMessageFormatException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Iterator;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Title:        CASA
 * Description:  Based on the KQMLmessage class.
 *               Similar to a KQMLmessage, the message is stored internally as a
 *               String performative, and a HashTable parameters.
 *               The document is not stored locally it is easiest to create the
 *               document (the DOM tree) by using the XMLParser, and because the
 *               document cannot be easily modified to add new parameters once
 *               it has been created.
 *
 *               @returns well formed XMLMessage corresponding to object's current state
 *
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
 */
public class XMLMessage extends MLMessage implements XML {
  /**
   * Constructs an empty XML message.
   */
  public XMLMessage () {
  }

  public XMLMessage (String... list) {
    super(list);
  }

  /**
   * Sets the performative and the parameters of the XMLMessage based on the
   * string input, using the JDOM SAXBuilder.   If the string is not a valid
   * message an exception is thrown.
   *
   * @param source message used to initialize parameters in XMLMessage.
   *
   * @throws MLMessageFormatException when an MLMessage is too poorly formatted
   *         to be parsed.
   */
  @Override
  public void fromStringLocal (String xMLString) throws MLMessageFormatException {
    Document messageXML;

    SAXBuilder reader = new SAXBuilder (false);
    reader.setEntityResolver (new EntityResolver () {
      public InputSource resolveEntity (String publicId, String systemId) {
        return new InputSource (new StringReader (""));
      }
    });

    StringReader stringReader = new StringReader (xMLString);

    try {
      // open the file and a stream to read
      messageXML = reader.build(stringReader, "http://localhost/crap/");
    } catch (JDOMException e) {
      throw new XMLMessageFormatException (e);
    } catch (IOException e) {
      throw new XMLMessageFormatException (e);
    }

    if (!messageXML.getRootElement ().getName ().equals (MESSAGE)) {
      throw new MLMessageFormatException (
          "Not an XML CASA Message, incorrect root node: \'" +
          messageXML.getRootElement ().getName () + "\'");
    }

    Element parameterElement;
    String parameterName;
    String parameterValue;

    super.reset ();

    Iterator i = messageXML.getRootElement ().getChildren ().listIterator ();

    while (i.hasNext ()) {
      parameterElement = (Element) i.next ();

      parameterName = parameterElement.getName ();
      parameterValue = parameterElement.getTextTrim ();

      setParameter (parameterName, parameterValue);
    }
  }


  /**
   * This method can be used to generate the completely well-formed string equivalent
   * of the message specified. The message will include the performative and
   * any parameters using proper XML syntax.
   *
   * @param prettyPrint set to true to add spaces and newlines out output
   * @returns String containing a well formed XML message corresponding to object's current state.
   */
  @Override
  public String toString (boolean prettyPrint) {
    Element rootElement = new Element (MESSAGE);
    rootElement.setAttribute(VERSION, CS_VERSION);
//    rootElement.addAttribute (XML.VERSION, XML.CS_VERSION);
    Document messageXML = new Document (rootElement);
    messageXML.setDocType (new DocType (MESSAGE, MESSAGE_DTD));

    Element parameterElement;
    String parameterName;
    String parameterValue;

    parameterElement = new Element (ML.PERFORMATIVE);
    parameterElement.setText (getParameter (ML.PERFORMATIVE));

    rootElement.addContent (parameterElement);

    Enumeration e = getSortedParameterList ().elements ();

    while (e.hasMoreElements ()) {
      parameterName = (String) e.nextElement ();
      parameterValue = getParameter (parameterName);
      if (prettyPrint)
      	parameterValue = prettyfy(parameterName, parameterValue);

      parameterElement = new Element (parameterName);

      parameterElement.setText (parameterValue);

      rootElement.addContent (parameterElement);
    }

    XMLOutputter writer = new XMLOutputter ();
    if (prettyPrint) {
      writer.setFormat(Format.getPrettyFormat());
//      writer.setIndent ("  ");
//      writer.setNewlines (true);
    } else {
      writer.setFormat(Format.getCompactFormat());
    }
//    writer.setTrimText (true);
    String output = null;

    output = writer.outputString (messageXML);

    return output;
  }

  // Main - for testing only.
  public static void main (String args[]) {

    XMLMessage x = new XMLMessage ();
    try {
      x.fromStringLocal ("<?xml version=\"1.0\"?><!DOCTYPE message SYSTEM \"xmlmessage.dtd\"><CASAmessage><performative>registerAgent</performative><content>24.67.113.98:1000 null</content><from>24.somewhere.76.4</from><in-reply-to>24.67.113.98:1000</in-reply-to><language>CAG-CASA</language><ontlogy>nothing</ontlogy><receiver>24.67.113.98:1000</receiver><reply-with>24.67.113.98:1000#1</reply-with><sender>24.67.113.98:1001</sender></CASAmessage> ");
    } catch (MLMessageFormatException e) {
      System.out.println ("error setting message: " + e);
    } catch (Exception e) {
      System.out.println ("error setting message: " + e);
    }
    System.out.println ("\nPerformative: \'" + x.getParameter (ML.PERFORMATIVE) +
                        "\'\n\n");
    System.out.println (x.toString (false) + "\n\n");
    //x.parser.printDOMTree ();

  }
} // XMLMessage