package casa.util;

/**
 * <p>Title: ShortProperty</p>
 * <p>Description: A short property.</p>
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

public class ShortProperty extends Property {
  /**
   * The value of this property.
   */
  private short value;

  /**
   * Initializes the property to have the value indicated.
   *
   * @param value The value to set the property at.
   */
  public ShortProperty (short value) {
    this.value = value;
  }

  /**
   * Retrieves the type of property this is.
   *
   * @return The type of property this is, <code>Property.BOOLEAN</code>.
   */
  public int getType () {
    return BOOLEAN;
  }

  /**
   * Retreives the <code>String</code> representation of the property.
   *
   * @return The <code>String</code> representation of the property.
   */
  public String toString () {
    return Short.toString (value);
  }

  /**
   * Determines if the current property is equal to the given object.
   *
   * @param object The object to compare to the current property.
   * @return <code>true</code> if the object is a <code>ShortProperty</code>
   * and its value is the same as the current property.
   */
  public boolean equals (Object object) {
    if (ShortProperty.class.isInstance (object)) {
      ShortProperty otherProperty = (ShortProperty) object;
      if (otherProperty.value == value) {
        return true;
      }
    }

    return false;
  }

  /**
   * Creates a <code>ShortProperty</code> that is decoded from the given
   * <code>String</code> and returns it.
   *
   * @param property The property in <code>String</code> form.
   * @return The property in <code>ShortProperty</code> form.
   */
  public static Property fromString (String property) {
    short value = Short.valueOf (property).shortValue ();
    return new ShortProperty (value);
  }

  /**
   * Retrieves the value of the property.
   *
   * @return The value of the property.
   */
  public short getShort () {
    return value;
  }

  /**
   * Sets the value of the property.
   *
   * @param value The new value of the property.
   */
  public void setShort (short value) {
    this.value = value;
  }
}