package casa.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * <code>PropertiesMapXML</code> is an extension of <code>PropertiesMap</code>
 * that can transform the map into a valid XML document.
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
 * @author Jason Heard
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class PropertiesMapXML extends PropertiesMap {
  /**
   * The properties stored in their XML format.
   */
  private Document propertiesXML;

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
  public static final String BOOLEAN_TYPE = "boolean";
  /**
   * Used to denote a String property type in the property type attribute of a
   * property.
   */
  public static final String STRING_TYPE = "String";
  /**
   * Used to denote an integer property type in the property type attribute of
   * a property.
   */
  public static final String INTEGER_TYPE = "int";
  /**
   * Used to denote a long integer property type in the property type attribute
   * of a property.
   */
  public static final String LONG_TYPE = "long";
  /**
   * Used to denote a floating point property type in the property type
   * attribute of a property.
   */
  public static final String FLOAT_TYPE = "float";
  /**
   * Used to denote a double precision floating point property type in the
   * property type attribute of a property.
   */
  public static final String DOUBLE_TYPE = "double";

  /**
   * Creates a new empty instance of the PropertiesMapXML.  Initializes the map
   * of the properties to an empty <code>Hashtable</code>, and initializes the
   * XML version.
   */
  public PropertiesMapXML () {
    super ();

    propertiesXML = new Document (new Element ("properties"));
    resetModified ();
  }

  /**
   * Creates a new instance of the <code>PropertiesMapXML</code> from another
   * <code>PropertiesMap</code>.  Initializes the map of the properties
   * to a clone of the map of the given <code>PropertiesMap</code> unless it
   * is <code>null</code>.  If it is <code>null</code>, it initializes the map
   * of the properties to an empty <code>Hashtable</code>.  Finally, it
   * initializes the XML version to match.
   *
   * @param map A <code>PropertiesMap</code> that will be used to initialize
   * the new object if non-<code>null</code>.
   */
  public PropertiesMapXML (PropertiesMap map) {
    super (map);

    updateXMLFromMap ();
  }

  /**
   * Creates a new instance of the <code>PropertiesMapXML</code> from a
   * <code>Map</code>.  Initializes the map of the properties to the given
   * <code>Map</code> unless it is <code>null</code>.  If it is
   * <code>null</code>, it initializes the map of the properties to an empty
   * <code>Hashtable</code>.  Finally, it initializes the XML version to match.
   *
   * @param map A <code>Hashtable</code> that will be used to initialize the
   * new object if non-<code>null</code>.
   */
  public PropertiesMapXML (Map map) {
    super (map);

    updateXMLFromMap ();
  }

  /**
   * Creates a new instance of the <code>PropertiesMapXML</code> from a
   * <code>String</code>.  Initializes the XML verion of the properties from
   * the given <code>String</code>, and then initializes the map to match.
   *
   * @param xMLString A <code>String</code> containing the XML properties that
   * will be used to initialize the new object.
   */
  public PropertiesMapXML (String xMLString) {
    SAXBuilder reader = new SAXBuilder ();

    StringReader stringReader = new StringReader (xMLString);

    try {
      // open the file and a stream to read
      propertiesXML = reader.build (stringReader);
    } catch (JDOMException e) {
      propertiesXML = new Document (new Element ("properties"));
    } catch (IOException e) {
      propertiesXML = new Document (new Element ("properties"));
    }

    if (!propertiesXML.getRootElement ().getName ().equals ("properties")) {
      propertiesXML = new Document (new Element ("properties"));
    }

    updateMapFromXML ();
  }

  /**
   * Creates a new instance of the <code>PropertiesMapXML</code> from a
   * <code>Reader</code>.  Initializes the XML verion of the properties from
   * the given <code>Reader</code>, and then initializes the map to match.
   *
   * @param xMLReader The <code>Reader</code> that the XML properties will
   * be read from.
   */
  public PropertiesMapXML (Reader xMLReader) {
    SAXBuilder reader = new SAXBuilder ();

    try {
      // open the file and a stream to read
      propertiesXML = reader.build (xMLReader);
    } catch (JDOMException e) {
      propertiesXML = new Document (new Element ("properties"));
    } catch (IOException e) {
      propertiesXML = new Document (new Element ("properties"));
    }

    if (!propertiesXML.getRootElement ().getName ().equals ("properties")) {
      propertiesXML = new Document (new Element ("properties"));
    }

    updateMapFromXML ();
  }

  /**
   * Creates a new instance of the <code>PropertiesMapXML</code> from an
   * <code>InputStream</code>.  Initializes the XML verion of the properties
   * from the given <code>InputStream</code>, and then initializes the map to
   * match.
   *
   * @param xMLInputStream The <code>InputStream</code> that the XML
   * properties will be read from.
   */
  public PropertiesMapXML (InputStream xMLInputStream) {
    SAXBuilder reader = new SAXBuilder ();

    try {
      // open the file and a stream to read
      propertiesXML = reader.build (xMLInputStream);
    } catch (JDOMException e) {
      propertiesXML = new Document (new Element ("properties"));
    } catch (IOException e) {
      propertiesXML = new Document (new Element ("properties"));
    }

    if (!propertiesXML.getRootElement ().getName ().equals ("properties")) {
      propertiesXML = new Document (new Element ("properties"));
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

    output = writer.outputString (propertiesXML);

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

    writer.output (propertiesXML, xMLWriter);

    resetModified ();
  }

  /**
   * Writes the XML properties to the given <code>OutputStream</code>.
   *
   * @param xMLOutputStream The <code>OutputStream</code> that the XML
   * properties will be written to.
   * @throws IOException If there is a problem writing the XML to the
   * <code>OutputStream</code>.
   */
  public void write (OutputStream xMLOutputStream) throws IOException {
    XMLOutputter writer = new XMLOutputter ();

    writer.output (propertiesXML, xMLOutputStream);

    resetModified ();
  }

  /**
   * Redefines all of the properties to the given <code>Map</code> unless it is
   * <code>null</code>.  Finally, it modifies the XML version of the properties
   * to match the new properties.  All current properties will be lost if the
   * <cade>Map</code> is non-<code>null</code>.
   *
   * @param map The <code>Map</code> that will be used to set the properties if
   * non-<code>null</code>.
   */
  public void setAllProperties (Map map) {
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
      propertiesXML = reader.build (stringReader);
    } catch (JDOMException e) {
      propertiesXML = new Document (new Element ("properties"));
    } catch (IOException e) {
      propertiesXML = new Document (new Element ("properties"));
    }

    if (!propertiesXML.getRootElement ().getName ().equals ("properties")) {
      propertiesXML = new Document (new Element ("properties"));
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
      propertiesXML = reader.build (xMLReader);
    } catch (JDOMException e) {
      propertiesXML = new Document (new Element ("properties"));
    } catch (IOException e) {
      propertiesXML = new Document (new Element ("properties"));
    }

    if (!propertiesXML.getRootElement ().getName ().equals ("properties")) {
      propertiesXML = new Document (new Element ("properties"));
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
      propertiesXML = reader.build (xMLInputStream);
    } catch (JDOMException e) {
      propertiesXML = new Document (new Element ("properties"));
    } catch (IOException e) {
      propertiesXML = new Document (new Element ("properties"));
    }

    if (!propertiesXML.getRootElement ().getName ().equals ("properties")) {
      propertiesXML = new Document (new Element ("properties"));
    }

    updateMapFromXML ();
  }

  /**
   * Updates the map, assuming that the XML is initialized and correct.
   * Destroys any data currently in the map that is not contained in the XML.
   */
  private void updateMapFromXML () {
    Element propertyElement;
    String propertyName;
    String propertyValue;
    String propertyType;

    clearProperties ();

    Iterator i = propertiesXML.getRootElement ().getChildren ().listIterator ();

    while (i.hasNext ()) {
      propertyElement = (Element) i.next ();

      propertyName = propertyElement.getName ();
      propertyValue = propertyElement.getText ();
      propertyType = propertyElement.getAttribute (PROPERTY_TYPE).getValue ();

      if (propertyType.equals (BOOLEAN_TYPE)) {
        super.setProperty (propertyName,
                           BooleanProperty.fromString (propertyValue));
      } else if (propertyType.equals (STRING_TYPE)) {
        super.setProperty (propertyName,
                           StringProperty.fromString (propertyValue));
      } else if (propertyType.equals (INTEGER_TYPE)) {
        super.setProperty (propertyName,
                           IntegerProperty.fromString (propertyValue));
      } else if (propertyType.equals (LONG_TYPE)) {
        super.setProperty (propertyName,
                           LongProperty.fromString (propertyValue));
      } else if (propertyType.equals (FLOAT_TYPE)) {
        super.setProperty (propertyName,
                           FloatProperty.fromString (propertyValue));
      } else if (propertyType.equals (DOUBLE_TYPE)) {
        super.setProperty (propertyName,
                           DoubleProperty.fromString (propertyValue));
      }
    }

    resetModified ();
  }

  /**
   * Updates the XML, asssuming that the map is initialized and correct.
   * Destroys any data currently in the XML that is not contained in the map.
   */
  private void updateXMLFromMap () {
    propertiesXML = new Document (new Element ("properties"));

    Element rootElement = propertiesXML.getRootElement ();
    Element propertyElement;
    String propertyName;
    Property property;

    Enumeration e = getProperties ();

    while (e.hasMoreElements ()) {
      propertyName = (String) e.nextElement ();
      property = getProperty (propertyName);

      propertyElement = new Element (propertyName);
      switch (property.getType ()) {
        case (Property.BOOLEAN):
          propertyElement.setAttribute (PROPERTY_TYPE, BOOLEAN_TYPE);
          break;
        case (Property.STRING):
          propertyElement.setAttribute (PROPERTY_TYPE, STRING_TYPE);
          break;
        case (Property.INTEGER):
          propertyElement.setAttribute (PROPERTY_TYPE, INTEGER_TYPE);
          break;
        case (Property.LONG):
          propertyElement.setAttribute (PROPERTY_TYPE, LONG_TYPE);
          break;
        case (Property.FLOAT):
          propertyElement.setAttribute (PROPERTY_TYPE, FLOAT_TYPE);
          break;
        case (Property.DOUBLE):
          propertyElement.setAttribute (PROPERTY_TYPE, DOUBLE_TYPE);
          break;
      }

      propertyElement.setText (property.toString ());

      rootElement.addContent (propertyElement);
    }

    setModified ();
  }

  /**
   * Stores a property in the map.  If there was a property with the same name
   * previously, it is overwritten, even if the type is different.
   *
   * @param propertyName The name of the property to store.
   * @param property The property that matches the name given.
   */
  public void setProperty (String propertyName, Property property) {
    Element propertyElement;

    if (hasProperty (propertyName) &&
        property.equals (getProperty (propertyName))) {
      // The property is not changing.
      return;
    }

    propertyElement = new Element (propertyName);

    switch (property.getType ()) {
      case (Property.BOOLEAN):
        propertyElement.setAttribute (PROPERTY_TYPE, BOOLEAN_TYPE);
        break;
      case (Property.STRING):
        propertyElement.setAttribute (PROPERTY_TYPE, STRING_TYPE);
        break;
      case (Property.INTEGER):
        propertyElement.setAttribute (PROPERTY_TYPE, INTEGER_TYPE);
        break;
      case (Property.LONG):
        propertyElement.setAttribute (PROPERTY_TYPE, LONG_TYPE);
        break;
      case (Property.FLOAT):
        propertyElement.setAttribute (PROPERTY_TYPE, FLOAT_TYPE);
        break;
      case (Property.DOUBLE):
        propertyElement.setAttribute (PROPERTY_TYPE, DOUBLE_TYPE);
        break;
    }

    propertyElement.setText (property.toString ());

    propertiesXML.getRootElement ().removeChild (propertyName);
    propertiesXML.getRootElement ().addContent (propertyElement);

    setModified ();
    super.setProperty (propertyName, property);
  }

  /**
   * Stores a boolean property in the map and the XML.  If there was a property
   * with the same name previously, it is overwritten, even if the type is
   * different.
   *
   * @param propertyName The name of the property to store.
   * @param propertyValue The value of the property that matches the name
   * given.
   */
  public void setBoolean (String propertyName, boolean propertyValue) {
    Element propertyElement;
    Property property;

    if (hasProperty (propertyName)) {
      // Possibly modifying an existing property.
      property = getProperty (propertyName);

      try {
        if (property.getBoolean () == propertyValue) {
          // The property is not changing.
          return;
        }
      } catch (PropertyException e) {
      }

      propertyElement = propertiesXML.getRootElement ().getChild (propertyName);
      propertyElement.getAttribute (PROPERTY_TYPE).setValue (BOOLEAN_TYPE);
      propertyElement.setText (Boolean.toString (propertyValue));
    } else {
      // Adding a new property.
      propertyElement = new Element (propertyName);
      propertyElement.setAttribute (PROPERTY_TYPE, BOOLEAN_TYPE);
      propertyElement.setText (Boolean.toString (propertyValue));

      propertiesXML.getRootElement ().addContent (propertyElement);
    }

    setModified ();
    super.setBoolean (propertyName, propertyValue);
  }

  /**
   * Stores a String property in the map and the XML.  If there was a property
   * with the same name previously, it is overwritten, even if the type is
   * different.
   *
   * @param propertyName The name of the property to store.
   * @param propertyValue The value of the property that matches the name
   * given.
   */
  public void setString (String propertyName, String propertyValue) {
    Element propertyElement;
    Property property;

    if (hasProperty (propertyName)) {
      // Possibly modifying an existing property.
      property = getProperty (propertyName);

      try {
        if (property.getString ().equals (propertyValue)) {
          // The property is not changing.
          return;
        }
      } catch (PropertyException e) {
      }

      propertyElement = propertiesXML.getRootElement ().getChild (propertyName);
      propertyElement.getAttribute (PROPERTY_TYPE).setValue (STRING_TYPE);
      propertyElement.setText (propertyValue);
    } else {
      // Adding a new property.
      propertyElement = new Element (propertyName);
      propertyElement.setAttribute (PROPERTY_TYPE, STRING_TYPE);
      propertyElement.setText (propertyValue);

      propertiesXML.getRootElement ().addContent (propertyElement);
    }

    setModified ();
    super.setString (propertyName, propertyValue);
  }

  /**
   * Stores an integer property in the map and the XML.  If there was a
   * property with the same name previously, it is overwritten, even if the
   * type is different.
   *
   * @param propertyName The name of the property to store.
   * @param propertyValue The value of the property that matches the name
   * given.
   */
  public void setInteger (String propertyName, int propertyValue) {
    Element propertyElement;
    Property property;

    if (hasProperty (propertyName)) {
      // Possibly modifying an existing property.
      property = getProperty (propertyName);

      try {
        if (property.getInteger () == propertyValue) {
          // The property is not changing.
          return;
        }
      } catch (PropertyException e) {
      }

      propertyElement = propertiesXML.getRootElement ().getChild (propertyName);
      propertyElement.getAttribute (PROPERTY_TYPE).setValue (INTEGER_TYPE);
      propertyElement.setText (Integer.toString (propertyValue));
    } else {
      // Adding a new property.
      propertyElement = new Element (propertyName);
      propertyElement.setAttribute (PROPERTY_TYPE, INTEGER_TYPE);
      propertyElement.setText (Integer.toString (propertyValue));

      propertiesXML.getRootElement ().addContent (propertyElement);
    }

    setModified ();
    super.setInteger (propertyName, propertyValue);
  }

  /**
   * Stores a long integer property in the map and the XML.  If there was a
   * property with the same name previously, it is overwritten, even if the
   * type is different.
   *
   * @param propertyName The name of the property to store.
   * @param propertyValue The value of the property that matches the name
   * given.
   */
  public void setLong (String propertyName, long propertyValue) {
    Element propertyElement;
    Property property;

    if (hasProperty (propertyName)) {
      // Possibly modifying an existing property.
      property = getProperty (propertyName);

      try {
        if (property.getLong () == propertyValue) {
          // The property is not changing.
          return;
        }
      } catch (PropertyException e) {
      }

      propertyElement = propertiesXML.getRootElement ().getChild (propertyName);
      propertyElement.getAttribute (PROPERTY_TYPE).setValue (LONG_TYPE);
      propertyElement.setText (Long.toString (propertyValue));
    } else {
      // Adding a new property.
      propertyElement = new Element (propertyName);
      propertyElement.setAttribute (PROPERTY_TYPE, LONG_TYPE);
      propertyElement.setText (Long.toString (propertyValue));

      propertiesXML.getRootElement ().addContent (propertyElement);
    }

    setModified ();
    super.setLong (propertyName, propertyValue);
  }

  /**
   * Stores a floating point property in the map and the XML.  If there was a
   * property with the same name previously, it is overwritten, even if the
   * type is different.
   *
   * @param propertyName The name of the property to store.
   * @param propertyValue The value of the property that matches the name
   * given.
   */
  public void setFloat (String propertyName, float propertyValue) {
    Element propertyElement;
    Property property;

    if (hasProperty (propertyName)) {
      // Possibly modifying an existing property.
      property = getProperty (propertyName);

      try {
        if (property.getFloat () == propertyValue) {
          // The property is not changing.
          return;
        }
      } catch (PropertyException e) {
      }

      propertyElement = propertiesXML.getRootElement ().getChild (propertyName);
      propertyElement.getAttribute (PROPERTY_TYPE).setValue (FLOAT_TYPE);
      propertyElement.setText (Float.toString (propertyValue));
    } else {
      // Adding a new property.
      propertyElement = new Element (propertyName);
      propertyElement.setAttribute (PROPERTY_TYPE, FLOAT_TYPE);
      propertyElement.setText (Float.toString (propertyValue));

      propertiesXML.getRootElement ().addContent (propertyElement);
    }

    setModified ();
    super.setFloat (propertyName, propertyValue);
  }

  /**
   * Stores a double precision floating point property in the map and the XML.
   * If there was a property with the same name previously, it is overwritten,
   * even if the type is different.
   *
   * @param propertyName The name of the property to store.
   * @param propertyValue The value of the property that matches the name
   * given.
   */
  public void setDouble (String propertyName, double propertyValue) {
    Element propertyElement;
    Property property;

    if (hasProperty (propertyName)) {
      // Possibly modifying an existing property.
      property = getProperty (propertyName);

      try {
        if (property.getDouble () == propertyValue) {
          // The property is not changing.
          return;
        }
      } catch (PropertyException e) {
      }

      propertyElement = propertiesXML.getRootElement ().getChild (propertyName);
      propertyElement.getAttribute (PROPERTY_TYPE).setValue (DOUBLE_TYPE);
      propertyElement.setText (Double.toString (propertyValue));
    } else {
      // Adding a new property.
      propertyElement = new Element (propertyName);
      propertyElement.setAttribute (PROPERTY_TYPE, DOUBLE_TYPE);
      propertyElement.setText (Double.toString (propertyValue));

      propertiesXML.getRootElement ().addContent (propertyElement);
    }

    setModified ();
    super.setDouble (propertyName, propertyValue);
  }

  /**
   * Removes the property from the map and removes the corresponding XML
   * element.
   *
   * @param propertyName The name of the property to be removed.
   */
  public void removeProperty (String propertyName) {
    if (hasProperty (propertyName)) {
      propertiesXML.getRootElement ().removeChild (propertyName);
      super.removeProperty (propertyName);
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
    PropertiesMapXML pXML = (PropertiesMapXML) this.clone();
    pXML.propertiesXML = (Document)pXML.propertiesXML.clone();

    return pXML;
//    return new PropertiesMapXML (this);
  }
}
