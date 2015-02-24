package casa.util;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * PropertiesMap is a generic base class used to store properties mapped from
 * a String name to a value of type boolean, String, int, long, float, or
 * double.
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
 * @see Property
 * @see BooleanProperty
 * @see StringProperty
 * @see IntegerProperty
 * @see LongProperty
 * @see FloatProperty
 * @see DoubleProperty
 *
 * @author Jason Heard
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class PropertiesMap {
  /**
   * The properties stored in their Hashtable format.
   */
  private Map<String, Property> propertiesMap;

  /**
   * Creates a new <code>PropertiesMap</code> object with an empty map.
   * Initializes the map of the properties to an empty <code>Hashtable</code>.
   */
  public PropertiesMap () {
    this.propertiesMap = new Hashtable<String, Property> ();
  }

  /**
   * Creates a new <code>PropertiesMap</code> object initialized with the given
   * <code>Map</code>.  Initializes the map of the properties to the given
   * <code>Map</code> unless it is <code>null</code>.  If it is
   * <code>null</code>, it initializes the map of the properties to an empty
   * <code>Hashtable</code>.
   *
   * @param map The <code>Map</code> that will be used to initialize the the
   * new <code>PropertiesMap</code> if non-<code>null</code>.
   */
  public PropertiesMap (Map<String, Property> map) {
    if (map != null) {
//      if (map instanceof Hashtable) {
//        Hashtable<?, ?> tempHashtable = (Hashtable<?, ?>) map;
//        this.propertiesMap = (Hashtable<String, Property>) tempHashtable.clone ();
//      } else {
        this.propertiesMap = new Hashtable<String, Property> (map);
//      }
    } else {
      this.propertiesMap = new Hashtable<String, Property> ();
    }
  }

  /**
   * Creates a new <code>PropertiesMap</code> object initialized with the
   * given <code>PropertiesMap</code>.  Initializes the map of the properties
   * to a clone of the map of the given <code>PropertiesMap</code> unless it
   * is <code>null</code>.  If it is <code>null</code>, it initializes the map
   * of the properties to an empty <code>Hashtable</code>.
   *
   * @param map The <code>PropertiesMap</code> that will be used to initialize
   * the new <code>PropertiesMap</code> if non-<code>null</code>.
   */
  public PropertiesMap (PropertiesMap map) {
    if (map != null) {
      this.propertiesMap = new Hashtable<String, Property>(map.propertiesMap); //(Hashtable<String, Property>) map.propertiesMap.clone ();
    } else {
      this.propertiesMap = new Hashtable<String, Property> ();
    }
  }

  /**
   * Redefines all of the properties to the given <code>Map</code> unless it is
   * <code>null</code>.  All current properties will be lost if the
   * <cade>Map</code> is non-<code>null</code>.
   *
   * @param map A <code>Map</code> that will be used to set the properties if
   * non-<code>null</code>.
   */
  public void setAllProperties (Map<String, Property> map) {
    if (map != null) {
//      if (map instanceof Hashtable<?,?>) {
//        Hashtable<?, ?> tempHashtable = (Hashtable<?, ?>) map;
//        this.propertiesMap = (Hashtable<String, Property>) tempHashtable.clone ();
//      } else {
        this.propertiesMap = new Hashtable<String, Property> (map);
//      }
    }
  }

  /**
   * Retrieves the property from the map, returning it to the user.
   *
   * @param propertyName The name of the property to retrieve.
   * @return The property that matches the name given.
   */
  public Property getProperty (String propertyName) {
    return propertiesMap.get (propertyName);
  }

  /**
   * Stores a property in the map.  If there was a property with the same name
   * previously, it is overwritten, even if the type is different.
   *
   * @param propertyName The name of the property to store.
   * @param property The property that matches the name given.
   */
  public void setProperty (String propertyName, Property property) {
    propertiesMap.put (propertyName, property);
  }

  /**
   * Retrieves the boolean property from the map, returning it to the user.
   *
   * @param propertyName The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not a boolean property.
   */
  public boolean getBoolean (String propertyName) throws PropertyException {
    Property prop = propertiesMap.get (propertyName);
    if (prop == null) {
      throw new PropertyException ("Property not found: "+propertyName);
    } else {
      return prop.getBoolean ();
    }
  }

  /**
   * Stores a boolean property in the map.  If there was a property with the
   * same name previously, it is overwritten, even if the type is different.
   *
   * @param propertyName The name of the property to store.
   * @param propertyValue The value of the property that matches the name
   * given.
   */
  public void setBoolean (String propertyName, boolean propertyValue) {
    propertiesMap.put (propertyName, new BooleanProperty (propertyValue));
  }

  /**
   * Retrieves the short property from the map, returning it to the user.
   *
   * @param propertyName The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not a short property.
   */
  public short getShort (String propertyName) throws PropertyException {
    Property prop = propertiesMap.get (propertyName);
    if (prop == null) {
      throw new PropertyException ("Property not found: "+propertyName);
    } else {
      return prop.getShort ();
    }
  }

  /**
   * Stores a short property in the map.  If there was a property with the
   * same name previously, it is overwritten, even if the type is different.
   *
   * @param propertyName The name of the property to store.
   * @param propertyValue The value of the property that matches the name
   * given.
   */
  public void setShort (String propertyName, short propertyValue) {
    propertiesMap.put (propertyName, new ShortProperty (propertyValue));
  }

  /**
   * Retrieves the byte property from the map, returning it to the user.
   *
   * @param propertyName The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not a byte property.
   */
  public byte getByte (String propertyName) throws PropertyException {
    Property prop = propertiesMap.get (propertyName);
    if (prop == null) {
      throw new PropertyException ("Property not found: "+propertyName);
    } else {
      return prop.getByte ();
    }
  }

  /**
   * Stores a byte property in the map.  If there was a property with the
   * same name previously, it is overwritten, even if the type is different.
   *
   * @param propertyName The name of the property to store.
   * @param propertyValue The value of the property that matches the name
   * given.
   */
  public void setByte (String propertyName, byte propertyValue) {
    propertiesMap.put (propertyName, new ByteProperty (propertyValue));
  }

  /**
   * Retrieves the char property from the map, returning it to the user.
   *
   * @param propertyName The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not a char property.
   */
  public char getChar (String propertyName) throws PropertyException {
    Property prop = propertiesMap.get (propertyName);
    if (prop == null) {
      throw new PropertyException ("Property not found: "+propertyName);
    } else {
      return prop.getChar ();
    }
  }

  /**
   * Stores a char property in the map.  If there was a property with the
   * same name previously, it is overwritten, even if the type is different.
   *
   * @param propertyName The name of the property to store.
   * @param propertyValue The value of the property that matches the name
   * given.
   */
  public void setChar (String propertyName, char propertyValue) {
    propertiesMap.put (propertyName, new CharProperty (propertyValue));
  }

  /**
   * Retrieves the String property from the map, returning it to the user.
   *
   * @param propertyName The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not a String property.
   */
  public String getString (String propertyName) throws PropertyException {
    Property prop = propertiesMap.get (propertyName);
    if (prop == null) {
      throw new PropertyException ("Property not found: "+propertyName);
    } else {
      return prop.getString ();
    }
  }

  /**
   * Stores a String property in the map.  If there was a property with the
   * same name previously, it is overwritten, even if the type is different.
   *
   * @param propertyName The name of the property to store.
   * @param propertyValue The value of the property that matches the name
   * given.
   */
  public void setString (String propertyName, String propertyValue) {
    propertiesMap.put (propertyName, new StringProperty (propertyValue));
  }

  /**
   * Retrieves the integer property from the map, returning it to the user.
   *
   * @param propertyName The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not an integer
   * property.
   */
  public int getInteger (String propertyName) throws PropertyException {
    Property prop = propertiesMap.get (propertyName);
    if (prop == null) {
      throw new PropertyException ("Property not found: "+propertyName);
    } else {
      return prop.getInteger ();
    }
  }

  /**
   * Stores a integer property in the map.  If there was a property with the
   * same name previously, it is overwritten, even if the type is different.
   *
   * @param propertyName The name of the property to store.
   * @param propertyValue The value of the property that matches the name
   * given.
   */
  public void setInteger (String propertyName, int propertyValue) {
    propertiesMap.put (propertyName, new IntegerProperty (propertyValue));
  }

  /**
   * Retrieves the long integer property from the map, returning it to the
   * user.
   *
   * @param propertyName The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not a long integer
   * property.
   */
  public long getLong (String propertyName) throws PropertyException {
    Property prop = propertiesMap.get (propertyName);
    if (prop == null) {
      throw new PropertyException ("Property not found: "+propertyName);
    } else {
      return prop.getLong ();
    }
  }

  /**
   * Stores a long integer property in the map.  If there was a property with
   * the same name previously, it is overwritten, even if the type is
   * different.
   *
   * @param propertyName The name of the property to store.
   * @param propertyValue The value of the property that matches the name
   * given.
   */
  public void setLong (String propertyName, long propertyValue) {
    propertiesMap.put (propertyName, new LongProperty (propertyValue));
  }

  /**
   * Retrieves the floating point property from the map, returning it to the
   * user.
   *
   * @param propertyName The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not a floating point
   * property.
   */
  public float getFloat (String propertyName) throws PropertyException {
    Property prop = propertiesMap.get (propertyName);
    if (prop == null) {
      throw new PropertyException ("Property not found: "+propertyName);
    } else {
      return prop.getFloat ();
    }
  }

  /**
   * Stores a floating point property in the map.  If there was a property with
   * the same name previously, it is overwritten, even if the type is
   * different.
   *
   * @param propertyName The name of the property to store.
   * @param propertyValue The value of the property that matches the name
   * given.
   */
  public void setFloat (String propertyName, float propertyValue) {
    propertiesMap.put (propertyName, new FloatProperty (propertyValue));
  }

  /**
   * Retrieves the double precision floating point property from the map,
   * returning it to the user.
   *
   * @param propertyName The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not a double precision
   * floating point property.
   */
  public double getDouble (String propertyName) throws PropertyException {
    Property prop = propertiesMap.get (propertyName);
    if (prop == null) {
      throw new PropertyException ("Property not found: "+propertyName);
    } else {
      return prop.getDouble ();
    }
  }

  /**
   * Stores a double precision floating point property in the map.  If there
   * was a property with the same name previously, it is overwritten, even if
   * the type is different.
   *
   * @param propertyName The name of the property to store.
   * @param propertyValue The value of the property that matches the name
   * given.
   */
  public void setDouble (String propertyName, double propertyValue) {
    propertiesMap.put (propertyName, new DoubleProperty (propertyValue));
  }

  /**
   * Determines the type of property stored in the map, returning it to the
   * user.
   *
   * @param propertyName The name of the property of which we are curious about
   * the type.
   * @return The type of the property that matches the name given.  One of:
   * <ul>
   * <li><code>Property.BOOLEAN</code>
   * <li><code>Property.STRING</code>
   * <li><code>Property.INTEGER</code>
   * <li><code>Property.LONG</code>
   * <li><code>Property.FLOAT</code>
   * <li><code>Property.DOUBLE</code>
   * </ul>
   * @see Property
   */
  public int getType (String propertyName) {
    Property prop = propertiesMap.get (propertyName);
    return prop.getType ();
  }

  /**
   * Returns whether the given property is contained in the property map.
   *
   * @param propertyName The name of the property that we are checking for.
   * @return <code>true</code> if there exists a property with the given name
   * in the map; <code>false</code> otherwise.
   */
  public boolean hasProperty (String propertyName) {
    return propertiesMap.containsKey (propertyName);
  }

  /**
   * Removes the property from the map.
   *
   * @param propertyName The name of the property to be removed.
   */
  public void removeProperty (String propertyName) {
    propertiesMap.remove (propertyName);
  }

  /**
   * Returns a list of all of the property names.
   *
   * @return All of the property names as <code>String</code>s in an
   * <code>Enumeration</code>.
   */
  @SuppressWarnings("unchecked")
	public Enumeration<String> getProperties () {
    if (propertiesMap instanceof Dictionary) 
    	return ((Dictionary<String,Property>)propertiesMap).keys ();
    else
    	return (new Vector<String>(propertiesMap.keySet())).elements();
  }
  
  /**
   * @return The set of String keys (property names).
   */
  public Set<String> keySet() {
  	return propertiesMap.keySet();
  }

  /**
   * Removes all properties from the map.
   */
  public void clearProperties () {
    propertiesMap.clear ();
  }

  /**
   * Compares the specified <code>Object</code> with this
   * <code>PropertiesMap</code> for equality.  The two are equal if and only if
   * the <code>Object</code> is an instance of <code>PropertiesMap</code> and
   * all of their properties are equal.
   *
   * @param object The <code>Object</code> to be compared for equality with
   * this <code>PropertiesMap</code>.
   * @return <code>true</code> if the specified <code>Object</code> is equal to
   * this <code>PropertiesMap</code>; <code>false</code> otherwise.
   */
  @Override
	public boolean equals (Object object) {
    if (object == this) {
      return true;
    } else if (object instanceof PropertiesMap) {
      PropertiesMap map = (PropertiesMap) object;

      return map.propertiesMap.equals (this.propertiesMap);
    }

    return false;
  }

  /**
   * Creates a copy of this <code>PropertiesMap</code>. All of the properties
   * in the original map are copied into another map.
   *
   * @return  A clone of this <code>PropertiesMap</code>.
   */
  @Override
	protected Object clone () {
    return new PropertiesMap (this);
  }
}