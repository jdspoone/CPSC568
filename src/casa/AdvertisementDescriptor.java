package casa;

import casa.agentCom.URLDescriptor;
import casa.exceptions.URLDescriptorException;
import casa.util.PropertiesMap;
import casa.util.PropertiesMapXML;
import casa.util.PropertyException;

import java.text.ParseException;

/**
 * An <code>AdvertisementDescriptor</code> object is used to describe an advertisement that an agent has placed about one of its services with a yellow pages agent.  It consists of a <code>URLDescriptor</code> that is used to locate the agent and a <code>PropertiesMap</code> that is used to describe the agent.  There is onlt one defined field at this time, and that is the "service" field.  It describes what service the agent is advertising in this advertisement. <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @see TransientAgent
 * @author  Jason Heard
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public final class AdvertisementDescriptor {
  /**
	 */
  URLDescriptor advertisor = null;
  /**
	 */
  PropertiesMapXML properties = new PropertiesMapXML ();

  public AdvertisementDescriptor(URLDescriptor newAdvertisor, String newService) {
    this.advertisor = newAdvertisor;
    setStringProperty ("service", newService);
  }

  public AdvertisementDescriptor(URLDescriptor newAdvertisor, PropertiesMap newProperties) {
    this.advertisor = newAdvertisor;

    properties = new PropertiesMapXML (newProperties);
  }

  public AdvertisementDescriptor(String serializedDescriptor) throws ParseException {
    int colonLocation = serializedDescriptor.indexOf(": ");

    try {
      advertisor = URLDescriptor.make (serializedDescriptor.substring (0,
          colonLocation));
    } catch (URLDescriptorException e) {
      throw new ParseException ("Error decoding URLDescriptor: " + e.toString (), 0);
    }

    properties.read (serializedDescriptor.substring(colonLocation + 2));
//    properties.read (CASAUtil.fromQuotedString (serializedDescriptor, colonLocation + 2));
  }

  public static AdvertisementDescriptor fromString (TokenParser parser) throws ParseException {
    String str = parser.getNextToken ();
    if (str == null || str.equals (ML.NULL)) {
      return null;
    } else {
      return new AdvertisementDescriptor (str);
    }
  }

  @Override
public String toString () {
    StringBuffer output = new StringBuffer ();

    output.append (advertisor.toString (null));
    output.append (": ");
    output.append (properties.toString ());
//    output.append (CASAUtil.toQuotedString (descriptors.toString ()));

    return output.toString ();
  }

  /**
	 * @return
	 */
  public URLDescriptor getAdvertisor () {
    return advertisor;
  }

  /**
	 * @param newAdvertisor
	 */
  public void setAdvertisor (URLDescriptor newAdvertisor) {
    this.advertisor = newAdvertisor;
  }

  /**
   * Stores a boolean property.  If there was a property with the same name
   * previously, it is overwritten, even if the type is different.
   *
   * @param name The name of the property to store.
   * @param value The value of the property that matches the name given.
   */
  public synchronized void setBooleanProperty (String name, boolean value) {
    properties.setBoolean (name, value);
  }

  /**
   * Retrieves the boolean property, returning it to the user.
   *
   * @param name The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not a boolean property.
   */
  public boolean getBooleanProperty (String name) throws PropertyException {
    if (properties==null) throw new PropertyException("not a persistent agent");
    return properties.getBoolean (name);
  }

  /**
   * Stores a String property.  If there was a property with the same name
   * previously, it is overwritten, even if the type is different.
   *
   * @param name The name of the property to store.
   * @param value The value of the property that matches the name given.
   */
  public synchronized void setStringProperty (String name, String value) {
    properties.setString (name, value);
  }

  /**
   * Retrieves the String property, returning it to the user.
   *
   * @param name The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not a String property.
   */
  public String getStringProperty (String name) throws PropertyException {
    if (properties==null) throw new PropertyException("not a persistent agent");
    return properties.getString (name);
  }

  /**
   * Stores an integer property.  If there was a property with the same name
   * previously, it is overwritten, even if the type is different.
   *
   * @param name The name of the property to store.
   * @param value The value of the property that matches the name given.
   */
  public synchronized void setIntegerProperty (String name, int value) {
    properties.setInteger (name, value);
  }

  /**
   * Retrieves the integer property, returning it to the user.
   *
   * @param name The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not an integer
   * property.
   */
  public int getIntegerProperty (String name) throws PropertyException {
    if (properties==null) throw new PropertyException("not a persistent agent");
    return properties.getInteger (name);
  }

  /**
   * Stores a long integer property.  If there was a property with the same
   * name previously, it is overwritten, even if the type is different.
   *
   * @param name The name of the property to store.
   * @param value The value of the property that matches the name given.
   */
  public synchronized void setLongProperty (String name, long value) {
    properties.setLong (name, value);
  }

  /**
   * Retrieves the long integer property, returning it to the user.
   *
   * @param name The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not a long integer
   * property.
   */
  public long getLongProperty (String name) throws PropertyException {
    if (properties==null) throw new PropertyException("not a persistent agent");
    return properties.getLong (name);
  }

  /**
   * Stores a floating point property.  If there was a property with the same
   * name previously, it is overwritten, even if the type is different.
   *
   * @param name The name of the property to store.
   * @param value The value of the property that matches the name given.
   */
  public synchronized void setFloatProperty (String name, float value) {
    properties.setFloat (name, value);
  }

  /**
   * Retrieves the floating point property, returning it to the user.
   *
   * @param name The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not a floating point
   * property.
   */
  public float getFloatProperty (String name) throws PropertyException {
    if (properties==null) throw new PropertyException("not a persistent agent");
    return properties.getFloat (name);
  }

  /**
   * Stores a double precision floating point property.  If there was a
   * property with the same name previously, it is overwritten, even if the
   * type is different.
   *
   * @param name The name of the property to store.
   * @param value The value of the property that matches the name given.
   */
  public synchronized void setDoubleProperty (String name, double value) {
    properties.setDouble (name, value);
  }

  /**
   * Retrieves the double precision floating point property, returning it to
   * the user.
   *
   * @param name The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not a double precision
   * floating point property.
   */
  public double getDoubleProperty (String name) throws PropertyException {
    if (properties==null) throw new PropertyException("not a persistent agent");
    return properties.getDouble (name);
  }

  /**
   * Returns whether the specified property is contained in the properties.
   *
   * @param propertyName The name of the property that we are checking for.
   * @return <code>true</code> if there exists a property with the specified
   * name; <code>false</code> otherwise.
   */
  public boolean hasProperty (String propertyName) {
    return properties.hasProperty (propertyName);
  }

  /**
   * Removes the property from the properties.
   *
   * @param propertyName The name of the property to be removed.
   */
  public void removeProperty (String propertyName) {
    properties.removeProperty (propertyName);
  }

  @Override
public boolean equals (Object object) {
    if (object == this) {
      return true;
    } else {
      if (object instanceof AdvertisementDescriptor) {
        AdvertisementDescriptor tempAdvertisement = (AdvertisementDescriptor) object;

        if (tempAdvertisement.advertisor.equals(this.advertisor) &&
            tempAdvertisement.properties.equals(this.properties)) {
          return true;
        }
      }
    }

    return false;
  }
}