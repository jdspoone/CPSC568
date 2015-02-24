package casa.util;

import java.util.Hashtable;
import java.util.Enumeration;

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

public class UserMap {
  /**
   * The knownUsers stored as a Hashtable
   */
  private Hashtable kownUsersMap;

  public UserMap () {
    this.kownUsersMap = new Hashtable ();
  }

  public UserMap (Hashtable map) {
    if (map != null) {
      this.kownUsersMap = map;
    } else {
      this.kownUsersMap = new Hashtable ();
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
  public UserMap (UserMap map) {
    if (map != null) {
      this.kownUsersMap = (Hashtable) map.kownUsersMap.clone ();
    } else {
      this.kownUsersMap = new Hashtable ();
    }
  }

  /**
   * Redefines all of the knownUser to the given <code>Hashtable</code> unless
   * it is <code>null</code>.  All current knownUser will be lost if the
   * <cade>Hashtable</code> is changed.
   *
   * @param map A <code>Hashtable</code> that will be used to set the
   * properties if non-<code>null</code>.
   */
  public void setAllProperties (Hashtable map) {
    if (map != null) {
      kownUsersMap = map;
    }
  }

  /**
   * Retrieves the property from the map, returning it to the user.
   *
   * @param knownUserName The name of the property to retrieve.
   * @return The property that matches the name given.
   */
  public Property getProperty (String knownUserName) {
    return (Property) kownUsersMap.get (knownUserName);
  }

  /**
   * Stores a property in the map.  If there was a property with the same name
   * previously, it is overwritten, even if the type is different.
   *
   * @param knownUsersName The name of the knownUsers to store.
   * @param property The property that matches the name given (in this case it is
   * a StringProperty)
   */
  public void setProperty (String knownUsersName, Property property) {
    kownUsersMap.put (knownUsersName, property);
  }

  /**
   * Retrieves the String knownUsers from the map, returning it to the user.
   *
   * @param knownUsersName The name of the property to retrieve.
   * @return The value of the knownUsers that matches the name given.
   * @throws PropertyException If the given property is not a String property.
   */
  public String getString (String knownUsersName) throws PropertyException {
    Property prop = (Property) kownUsersMap.get (knownUsersName);
    if (prop == null) {
      throw new PropertyException ("KnownUsers not found.");
    } else {
      return prop.getString ();
    }
  }

  /**
   * Stores a String knownUsers in the map.  If there was a knownUser with the
   * same name previously, it is overwritten, even if the type is different.
   *
   * @param knownUsersName The name of the knownUsers to store.
   * @param knownUsersValue The value of the knownUsers that matches the name
   * given.
   */
  public void setString (String knownUsersName, String knownUsersValue) {
    kownUsersMap.put (knownUsersName, new StringProperty (knownUsersValue));
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
    Property prop = (Property) kownUsersMap.get (propertyName);
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
    return kownUsersMap.containsKey (propertyName);
  }

  /**
   * Removes the property from the map.
   *
   * @param propertyName The name of the property to be removed.
   */
  public void removeProperty (String propertyName) {
    kownUsersMap.remove (propertyName);
  }

  /**
   * Returns a list of all of the property names.
   *
   * @return All of the property names as <code>String</code>s in an
   * <code>Enumeration</code>.
   */
  public Enumeration getProperties () {
    return kownUsersMap.keys ();
  }

  /**
   * Removes all properties from the map.
   */
  public void clearProperties () {
    kownUsersMap.clear ();
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
  public boolean equals (Object object) {
    if (object == this) {
      return true;
    } else if (object instanceof PropertiesMap) {
      UserMap map = (UserMap) object;

      return map.kownUsersMap.equals (this.kownUsersMap);
    }

    return false;
  }

  /**
   * Creates a copy of this <code>PropertiesMap</code>. All of the properties
   * in the original map are copied into another map.
   *
   * @return  A clone of this <code>PropertiesMap</code>.
   */
  protected Object clone () {
    return new UserMap (this);
  }

}