package casa.util;

/**
 * <p>Title: Property</p>
 * <p>Description: An abstract property.</p>
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

public abstract class Property {
  /**
   * Constant used to indicate a boolean property type.
   */
  public static final int BOOLEAN = 0;
  /**
   * Constant used to indicate a string property type.
   */
  public static final int STRING = 1;
  /**
   * Constant used to indicate an integer property type.
   */
  public static final int INTEGER = 2;
  /**
   * Constant used to indicate a floating point property type.
   */
  public static final int LONG = 3;
  /**
   * Constant used to indicate a floating point property type.
   */
  public static final int FLOAT = 4;
  /**
   * Constant used to indicate a floating point property type.
   */
  public static final int DOUBLE = 5;
  /**
   * Constant used to indicate a boolean property type.
   */
  public static final int CHAR = 6;
  /**
   * Constant used to indicate a boolean property type.
   */
  public static final int BYTE = 7;
  /**
   * Constant used to indicate a boolean property type.
   */
  public static final int SHORT = 8;

  /**
   * Retrieves the type of property this is.
   *
   * @return The type of property this is.
   * @see casa.util.Property#BOOLEAN
   * @see casa.util.Property#STRING
   * @see casa.util.Property#INTEGER
   * @see casa.util.Property#LONG
   * @see casa.util.Property#FLOAT
   * @see casa.util.Property#DOUBLE
   * @see casa.util.Property#CHAR
   * @see casa.util.Property#BYTE
   * @see casa.util.Property#FLOAT
   */
  public abstract int getType ();

  /**
   * Retreives the <code>String</code> representation of the property.
   *
   * @return The <code>String</code> representation of the property.
   */
  @Override
	public abstract String toString ();

  /**
   * Determines if the current property is equal to the given object.
   *
   * @param object The object to compare to the current property.
   * @return <code>true</code> if the object is the same property type and its
   * value is the same as the current property.
   */
  @Override
	public abstract boolean equals (Object object);

  /**
   * Creates a <code>Property</code> that is decoded from the given
   * <code>String</code> and returns it.
   *
   * @param property The property in <code>String</code> form.
   * @return The decoded property.
   * @throws PropertyException If the function has not been overridden.
   */
  public static Property fromString (String property) throws PropertyException {
    throw new PropertyException ("fromString() not implimented.");
  }

  /**
   * Retrieves the char value of the property.
   *
   * @return The char value of the property.
   * @throws PropertyException If the property is not a
   * <code>CharProperty</code>.
   */
  public char getChar () throws PropertyException {
    throw new PropertyException ("Not a CharProperty");
  }

  /**
   * Retrieves the byte value of the property.
   *
   * @return The byte value of the property.
   * @throws PropertyException If the property is not a
   * <code>ByteProperty</code>.
   */
  public byte getByte () throws PropertyException {
    throw new PropertyException ("Not a ByteProperty");
  }

  /**
   * Retrieves the short value of the property.
   *
   * @return The short value of the property.
   * @throws PropertyException If the property is not a
   * <code>ShortProperty</code>.
   */
  public short getShort () throws PropertyException {
    throw new PropertyException ("Not a ShortProperty");
  }

  /**
   * Retrieves the boolean value of the property.
   *
   * @return The boolean value of the property.
   * @throws PropertyException If the property is not a
   * <code>BooleanProperty</code>.
   */
  public boolean getBoolean () throws PropertyException {
    throw new PropertyException ("Not a BooleanProperty");
  }

  /**
   * Sets the boolean value of the property.
   *
   * @param value The new boolean value of the property.
   * @throws PropertyException If the property is not a
   * <code>BooleanProperty</code>.
   */
  public void setBoolean (boolean value) throws PropertyException {
    throw new PropertyException ("Not a BooleanProperty");
  }

  /**
   * Sets the char value of the property.
   *
   * @param value The new char value of the property.
   * @throws PropertyException If the property is not a
   * <code>CharProperty</code>.
   */
  public void setChar (char value) throws PropertyException {
    throw new PropertyException ("Not a CharProperty");
  }

  /**
   * Sets the byte value of the property.
   *
   * @param value The new byte value of the property.
   * @throws PropertyException If the property is not a
   * <code>ByteProperty</code>.
   */
  public void setBtye (byte value) throws PropertyException {
    throw new PropertyException ("Not a ByteProperty");
  }

  /**
   * Sets the short value of the property.
   *
   * @param value The new short value of the property.
   * @throws PropertyException If the property is not a
   * <code>ShortProperty</code>.
   */
  public void setShort (short value) throws PropertyException {
    throw new PropertyException ("Not a ShortProperty");
  }

  /**
   * Retrieves the string value of the property.
   *
   * @return The string value of the property.
   * @throws PropertyException If the property is not a
   * <code>StringProperty</code>.
   */
  public String getString () throws PropertyException {
    throw new PropertyException ("Not a StringProperty");
  }

  /**
   * Sets the string value of the property.
   *
   * @param value The new string value of the property.
   * @throws PropertyException If the property is not a
   * <code>StringProperty</code>.
   */
  public void setString (String value) throws PropertyException {
    throw new PropertyException ("Not a StringProperty");
  }

  /**
   * Retrieves the integer value of the property.
   *
   * @return The integer value of the property.
   * @throws PropertyException If the property is not a
   * <code>IntegerProperty</code>.
   */
  public int getInteger () throws PropertyException {
    throw new PropertyException ("Not an IntegerProperty");
  }

  /**
   * Sets the integer value of the property.
   *
   * @param value The new integer value of the property.
   * @throws PropertyException If the property is not a
   * <code>IntegerProperty</code>.
   */
  public void setInteger (int value) throws PropertyException {
    throw new PropertyException ("Not an IntegerProperty");
  }

  /**
   * Retrieves the long integer value of the property.
   *
   * @return The long integer value of the property.
   * @throws PropertyException If the property is not a
   * <code>LongProperty</code>.
   */
  public long getLong () throws PropertyException {
    throw new PropertyException ("Not an LongProperty");
  }

  /**
   * Sets the long integer value of the property.
   *
   * @param value The new long integer value of the property.
   * @throws PropertyException If the property is not a
   * <code>LongProperty</code>.
   */
  public void setLong (long value) throws PropertyException {
    throw new PropertyException ("Not an LongProperty");
  }

  /**
   * Retrieves the floating point value of the property.
   *
   * @return The floating point value of the property.
   * @throws PropertyException If the property is not a
   * <code>FloatProperty</code.
   */
  public float getFloat () throws PropertyException {
    throw new PropertyException ("Not a FloatProperty");
  }

  /**
   * Sets the floating point value of the property.
   *
   * @param value The new floating point value of the property.
   * @throws PropertyException If the property is not a
   * <code>FloatProperty</code>.
   */
  public void setFloat (float value) throws PropertyException {
    throw new PropertyException ("Not a FloatProperty");
  }

  /**
   * Retrieves the double precision floating point value of the property.
   *
   * @return The double precision floating point value of the property.
   * @throws PropertyException If the property is not a
   * <code>DoubleProperty</code.
   */
  public double getDouble () throws PropertyException {
    throw new PropertyException ("Not a DoubleProperty");
  }

  /**
   * Sets the double precision floating point value of the property.
   *
   * @param value The new double precision floating point value of the
   * property.
   * @throws PropertyException If the property is not a
   * <code>DoubleProperty</code>.
   */
  public void setDouble (double value) throws PropertyException {
    throw new PropertyException ("Not a DoubleProperty");
  }
}