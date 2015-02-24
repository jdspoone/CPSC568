package casa.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 *
 * <p>Title: KnownUsersMapXML</p>
 * <p>Description: Extension of KnownUsersMap.   Can transform a map to a proper XML document.</p>
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
 * @version 0.9
 */
public class UserMapXML extends UserMap {
  /**
   * The properties stored in their XML format.
   */
  private Document knownUsersXML;

  /**
   * Set to <code>true</code> if the XML has been modified since the latest
   * <code>read</code>, <code>write</code>, or <code>toString</code> call;
   * <code>false</code> otherwise.
   */
  protected boolean modified;

  /**
   * Used to denote the property type attribute of a property.
   */
  public static final String PROPERTY_TYPE = "propertyType";
  /**
   * Used to denote a boolean property type in the property type attribute of a
   * property.
   */
  public static final String STRING_TYPE = "string";

  /**
   * Creates a new empty instance of the KnownUsersMapXML.  Initializes the map
   * of the properties to an empty <code>Hashtable</code>, and initializes the
   * XML version.
   */
  public UserMapXML () {
    super();

    knownUsersXML = new Document (new Element ("known-users"));
    resetModified ();
  }

  /**
   * Creates a new instance of the <code>KnownUsersMapXML</code> from another
   * <code>KnownUsersMap</code>.  Initializes the map of the properties
   * to a clone of the map of the given <code>KnownUsersMap</code> unless it
   * is <code>null</code>.  If it is <code>null</code>, it initializes the map
   * of the properties to an empty <code>Hashtable</code>.  Finally, it
   * initializes the XML version to match.
   *
   * @param map A <code>KnownUsersMap</code> that will be used to initialize
   * the new object if non-<code>null</code>.
   */
  public UserMapXML (UserMap map) {
    super (map);

    updateXMLFromMap ();
  }

  /**
   * Creates a new instance of the <code>KnownUsersMapXML</code> from a
   * <code>Hashtable</code>.  Initializes the map of the properties to the
   * given <code>Hashtable</code> unless it is <code>null</code>.  If it is
   * <code>null</code>, it initializes the map of the properties to an empty
   * <code>Hashtable</code>.  Finally, it initializes the XML version to match.
   *
   * @param map A <code>Hashtable</code> that will be used to initialize the
   * new object if non-<code>null</code>.
   */
  public UserMapXML (Hashtable map) {
    super (map);

    updateXMLFromMap ();
  }

  /**
   * Creates a new instance of the <code>KnownUsersMapXML</code> from a
   * <code>String</code>.  Initializes the XML verion of the properties from
   * the given <code>String</code>, and then initializes the map to match.
   *
   * @param xMLString A <code>String</code> containing the XML properties that
   * will be used to initialize the new object.
   */
  public UserMapXML (String xMLString) {
    SAXBuilder reader = new SAXBuilder ();

    StringReader stringReader = new StringReader (xMLString);

    try {
      // open the file and a stream to read
      knownUsersXML = reader.build (stringReader);
    } catch (JDOMException e) {
      knownUsersXML = new Document (new Element ("known-users"));
    } catch (IOException e) {
      knownUsersXML = new Document (new Element ("known-users"));
    }

    if (!knownUsersXML.getRootElement ().getName ().equals ("known-users")) {
      knownUsersXML = new Document (new Element ("known-users"));
    }

    updateMapFromXML ();
  }

  /**
   * Creates a new instance of the <code>KnownUsersMapXML</code> from a
   * <code>Reader</code>.  Initializes the XML verion of the properties from
   * the given <code>Reader</code>, and then initializes the map to match.
   *
   * @param xMLReader The <code>Reader</code> that the XML properties will
   * be read from.
   */
  public UserMapXML (Reader xMLReader) {
    SAXBuilder reader = new SAXBuilder ();

    try {
      // open the file and a stream to read
      knownUsersXML = reader.build (xMLReader);
    } catch (JDOMException e) {
      knownUsersXML = new Document (new Element ("known-users"));
    } catch (IOException e) {
      knownUsersXML = new Document (new Element ("known-users"));
    }

    if (!knownUsersXML.getRootElement ().getName ().equals ("known-users")) {
      knownUsersXML = new Document (new Element ("known-users"));
    }

    updateMapFromXML ();
  }

  /**
   * Creates a new instance of the <code>KnownUsersMapXML</code> from an
   * <code>InputStream</code>.  Initializes the XML verion of the properties
   * from the given <code>InputStream</code>, and then initializes the map to
   * match.
   *
   * @param xMLInputStream The <code>InputStream</code> that the XML
   * properties will be read from.
   */
  public UserMapXML (InputStream xMLInputStream) {
    SAXBuilder reader = new SAXBuilder ();

    try {
      // open the file and a stream to read
      knownUsersXML = reader.build (xMLInputStream);
    } catch (JDOMException e) {
      knownUsersXML = new Document (new Element ("known-users"));
    } catch (IOException e) {
      knownUsersXML = new Document (new Element ("known-users"));
    }

    if (!knownUsersXML.getRootElement ().getName ().equals ("known-users")) {
      knownUsersXML = new Document (new Element ("known-users"));
    }

    updateMapFromXML ();
  }

  /**
   * Writes the XML properties to a <code>String</code> and returns it.
   *
   * @return The XML properties as a <code>String</code>.
   */
  public String toString () {
    XMLOutputter writer = new XMLOutputter ();
    String output = null;

    output = writer.outputString (knownUsersXML);

    resetModified ();
    return output;
  }

  /**
   * Writes the XML properties to the given <code>Writer</code>.
   *
   * @param xMLWriter The <code>Writer</code> that the XML properties will be
   * written to.
   * @throws IOException If there is a problem writing the XML to the
   * <code>Writer</code>.
   */
  public void write (Writer xMLWriter) throws IOException {
    XMLOutputter writer = new XMLOutputter ();

    writer.output (knownUsersXML, xMLWriter);

    resetModified ();
  }

  /**
   * Writes the XML knownUsers to the given <code>OutputStream</code>.
   *
   * @param xMLOutputStream The <code>OutputStream</code> that the XML
   * properties will be written to.
   * @throws IOException If there is a problem writing the XML to the
   * <code>OutputStream</code>.
   */
  public void write (OutputStream xMLOutputStream) throws IOException {
    XMLOutputter writer = new XMLOutputter ();

    writer.output (knownUsersXML, xMLOutputStream);

    resetModified ();
  }

  /**
   * Redefines all of the properties to the given <code>Hashtable</code> unless
   * it is <code>null</code>.  Finally, it modifies the XML version of the
   * properties to match the new properties.  All current properties will be
   * lost if the <cade>Hashtable</code> is changed.
   *
   * @param map The <code>Hashtable</code> that will be used to set the
   * properties if non-<code>null</code>.
   */
  public void setAllProperties (Hashtable map) {
    super.setAllProperties (map);

    updateXMLFromMap ();
  }

  /**
   * Redefines the XML verion of the properties from the given
   * <code>String</code>, and then modifies the map to match the new
   * properties.  All current properties will be lost.
   *
   * @param xMLString The <code>String</code> containing the XML properties
   * that will be used to set the properties.
   */
  public void read (String xMLString) {
    SAXBuilder reader = new SAXBuilder ();

    StringReader stringReader = new StringReader (xMLString);

    try {
      // open the file and a stream to read
      knownUsersXML = reader.build (stringReader);
    } catch (JDOMException e) {
      knownUsersXML = new Document (new Element ("known-users"));
    } catch (IOException e) {
      knownUsersXML = new Document (new Element ("known-users"));
    }

    if (!knownUsersXML.getRootElement ().getName ().equals ("known-users")) {
      knownUsersXML = new Document (new Element ("known-users"));
    }

    updateMapFromXML ();
  }

  /**
   * Redefines the XML verion of the properties from the given
   * <code>Reader</code>, and then modifies the map to match the new
   * properties.  All current properties will be lost.
   *
   * @param xMLReader The <code>Reader</code> containing the XML properties
   * that will be used to set the properties.
   */
  public void read (Reader xMLReader) {
    SAXBuilder reader = new SAXBuilder ();

    try {
      // open the file and a stream to read
      knownUsersXML = reader.build (xMLReader);
    } catch (JDOMException e) {
      knownUsersXML = new Document (new Element ("known-users"));
    } catch (IOException e) {
      knownUsersXML = new Document (new Element ("known-users"));
    }

    if (!knownUsersXML.getRootElement ().getName ().equals ("known-users")) {
      knownUsersXML = new Document (new Element ("known-users"));
    }

    updateMapFromXML ();
  }

  /**
   * Redefines the XML verion of the properties from the given
   * <code>InputStream</code>, and then modifies the map to match the new
   * properties.  All current properties will be lost.
   *
   * @param xMLInputStream The <code>InputStream</code> containing the XML
   * properties that will be used to set the properties.
   */
  public void read (InputStream xMLInputStream) {
    SAXBuilder reader = new SAXBuilder ();

    try {
      // open the file and a stream to read
      knownUsersXML = reader.build (xMLInputStream);
    } catch (JDOMException e) {
      knownUsersXML = new Document (new Element ("known-users"));
    } catch (IOException e) {
      knownUsersXML = new Document (new Element ("known-users"));
    }

    if (!knownUsersXML.getRootElement ().getName ().equals ("known-users")) {
      knownUsersXML = new Document (new Element ("known-users"));
    }

    updateMapFromXML ();
  }

  /**
   * Updates the map, usssuming that the XML is initialized and correct.
   * Destroys any data currently in the map that is not contained in the XML.
   */
  private void updateMapFromXML () {
    Element userElement;
    String userName;
    String userValue;
    String userType;

    clearProperties ();

    Iterator i = knownUsersXML.getRootElement ().getChildren ().listIterator ();

    while (i.hasNext ()) {
      userElement = (Element) i.next ();

      userName = userElement.getName ();
      userValue = userElement.getText ();
      userType = userElement.getAttribute (PROPERTY_TYPE).getValue ();

      if (userType.equals (STRING_TYPE)) {
        super.setProperty (userName,
                           StringProperty.fromString (userValue));
      }
    }

    resetModified ();
  }

  /**
   * Updates the XML, asssuming that the map is initialized and correct.
   * Destroys any data currently in the XML that is not contained in the map.
   */
  private void updateXMLFromMap () {
    knownUsersXML = new Document (new Element ("known-users"));

    Element rootElement = knownUsersXML.getRootElement ();
    Element userElement;
    String userName;
    Property property;

    Enumeration e = getProperties ();

    while (e.hasMoreElements ()) {
      userName = (String) e.nextElement ();
      property = getProperty (userName);

      userElement = new Element (userName);
      switch (property.getType ()) {
        case (Property.STRING):
          userElement.setAttribute (PROPERTY_TYPE, STRING_TYPE);
          break;
      }

      userElement.setText (property.toString ());

      rootElement.addContent (userElement);
    }

    setModified ();
  }

  /**
   * Stores a kownUser in the map.  If there was a kownUser with the same name
   * previously, it is overwritten, even if the type is different.
   *
   * @param userName The name of the property to store.
   * @param property The property that matches the name given.
   */
  public void setProperty (String userName, Property property) {
    Element userElement;

    if (hasProperty (userName) &&
        property.equals (getProperty (userName))) {
      // The property is not changing.
      return;
    }

    userElement = new Element (userName);

    switch (property.getType ()) {
      case (Property.STRING):
        userElement.setAttribute (PROPERTY_TYPE, STRING_TYPE);
        break;
    }

    userElement.setText (property.toString ());

    knownUsersXML.getRootElement ().removeChild (userName);
    knownUsersXML.getRootElement ().addContent (userElement);

    setModified ();
    super.setProperty (userName, property);
  }

  /**
   * Stores a String knownUser in the map and the XML.  If there was a knownUser
   * with the same name previously, it is overwritten, even if the type is
   * different.
   *
   * @param userName The name of the property to store.
   * @param userValue The value of the property that matches the name
   * given.
   */
  public void setString (String userName, String userValue) {
    Element userElement;
    Property property;

    if (hasProperty (userName)) {
      // Possibly modifying an existing knownUser.
      property = getProperty (userName);

      try {
        if (property.getString ().equals (userValue)) {
          // The knownUser is not changing.
          return;
        }
      } catch (PropertyException e) {
      }

      userElement = knownUsersXML.getRootElement ().getChild (userName);
      userElement.getAttribute (PROPERTY_TYPE).setValue (STRING_TYPE);
      userElement.setText (userValue);
    } else {
      // Adding a new knownUser.
      userElement = new Element (userName);
      userElement.setAttribute (PROPERTY_TYPE, STRING_TYPE);
      userElement.setText (userValue);

      knownUsersXML.getRootElement ().addContent (userElement);
    }

    setModified ();
    super.setString (userName, userValue);
  }

  /**
   * Removes the knownUser from the map and removes the corresponding XML
   * element.
   *
   * @param userName The name of the property to be removed.
   */
  public void removeProperty (String userName) {
    if (hasProperty (userName)) {
      knownUsersXML.getRootElement ().removeChild (userName);
      super.removeProperty (userName);
      setModified ();
    }
  }

  /**
   * Called when the XML is modified for any reason.
   */
  protected void setModified () {
    this.modified = true;
  }

  /**
   * Called to reset the watch on the XML, called internally by
   * <code>read</code>, <code>write</code>, and <code>toString</code>;
   * <code>isModified</code> will now return <code>false</code> until the XML
   * is modified again.
   */
  public void resetModified () {
    this.modified = false;
  }

  /**
   * Determines if the XML has been modified since the latest call to
   * <code>resetModified ()</code>.
   *
   * @return <code>true</code> if the XML has been modified since the latest
   * <code>read</code>, <code>write</code>, or <code>toString</code> call;
   * <code>false</code> otherwise.
   */
  public boolean isModified () {
    return modified;
  }

  /**
   * Creates a copy of this <code>PropertiesMapXML</code>. All of the
   * properties in the original map are copied into another map, and the XML
   * version is recreated.
   *
   * @return  A clone of this <code>PropertiesMap</code>.
   */
  protected Object clone () {
    return new UserMapXML (this);
  }
}