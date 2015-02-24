package casa.util;

/**
 * <p>Title: IntegerProperty</p>
 * <p>Description: An integer property.</p>
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

public class IntegerProperty extends Property {
  /**
   * The value of this property.
   */
  private int value;

  /**
   * Initializes the property to have the value indicated.
   *
   * @param value The value to set the property at.
   */
  public IntegerProperty (int value) {
    this.value = value;
  }

  /**
   * Retrieves the type of property this is.
   *
   * @return The type of property this is, <code>Property.INTEGER</code>.
   */
  public int getType () {
    return INTEGER;
  }

  /**
   * Retreives the <code>String</code> representation of the property.
   *
   * @return The <code>String</code> representation of the property.
   */
  public String toString () {
    return Integer.toString (value);
  }

  /**
   * Determines if the current property is equal to the given object.
   *
   * @param object The object to compare to the current property.
   * @return <code>true</code> if the object is an <code>IntegerProperty</code>
   * and its value is the same as the current property.
   */
  public boolean equals (Object object) {
    if (IntegerProperty.class.isInstance (object)) {
      IntegerProperty otherProperty = (IntegerProperty) object;
      if (otherProperty.value == value) {
        return true;
      }
    }

    return false;
  }

  /**
   * Creates an <code>IntegerProperty</code> that is decoded from the given
   * <code>String</code> and returns it.
   *
   * @param property The property in <code>String</code> form.
   * @return The property in <code>IntegerProperty</code> form.
   */
  public static Property fromString (String property) {
    int value = Integer.valueOf (property).intValue ();
    return new IntegerProperty (value);
  }

  /**
   * Retrieves the value of the property.
   *
   * @return The value of the property.
   */
  public int getInteger () {
    return value;
  }

  /**
   * Sets the value of the property.
   *
   * @param value The new value of the property.
   */
  public void setInteger (int value) {
    this.value = value;
  }
}