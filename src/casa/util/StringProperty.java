package casa.util;

/**
 * <p>Title: StringProperty</p>
 * <p>Description: A string property.</p>
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

public class StringProperty extends Property {
  /**
   * The value of this property.
   */
  private String value;

  /**
   * Initializes the property to have the value indicated.
   *
   * @param value The value to set the property at.
   */
  public StringProperty (String value) {
    this.value = value;
  }

  /**
   * Retrieves the type of property this is.
   *
   * @return The type of property this is, <code>Property.STRING</code>.
   */
  public int getType () {
    return STRING;
  }

  /**
   * Retreives the <code>String</code> representation of the property.
   *
   * @return The <code>String</code> representation of the property.
   */
  public String toString () {
    return value;
  }

  /**
   * Determines if the current property is equal to the given object.
   *
   * @param object The object to compare to the current property.
   * @return <code>true</code> if the object is a <code>StringProperty</code>
   * and its value is the same as the current property.
   */
  public boolean equals (Object object) {
    if (StringProperty.class.isInstance (object)) {
      StringProperty otherProperty = (StringProperty) object;
      if (otherProperty.value == value) {
        return true;
      }
    }

    return false;
  }

  /**
   * Creates a <code>StringProperty</code> that is decoded from the given
   * <code>String</code> and returns it.
   *
   * @param property The property in <code>String</code> form.
   * @return The property in <code>StringProperty</code> form.
   */
  public static Property fromString (String property) {
    return new StringProperty (property);
  }

  /**
   * Retrieves the value of the property.
   *
   * @return The value of the property.
   */
  public String getString () {
    return value;
  }

  /**
   * Sets the value of the property.
   *
   * @param value The new value of the property.
   */
  public void setString (String value) {
    this.value = value;
  }
}