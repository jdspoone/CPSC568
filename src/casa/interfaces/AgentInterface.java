package casa.interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import casa.DataStorageDescriptor;
import casa.Status;
import casa.StatusString;
import casa.util.PropertyException;

/**
 * <code>Agent</code> is an extension of <code>TransientAgent</code> that adds the posibility to store properties and data.  These can be stored persistently or can be temporary for each instance. <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @see TransientAgent
 * @author  Jason Heard
 */

public interface AgentInterface extends TransientAgentInterface {
  /**
   * Returns the filename of the <code>CASAFile</code> that will be used by
   * this agent to store its properties and data either temporarily or
   * persistently.
   *
   * @return The filename of the <code>CASAFile</code> that will be used by
   * this agent to store its properties and data.
   * @throws FileNotFoundException 
   */
  public String getCASAFilename () throws FileNotFoundException;

  /**
	 * Sets whether this <code>Agent</code>'s data should be persistent between instances.
	 * @param persistent  Whether this <code>Agent</code>'s data should be  persistent between instances.
	 */
  public void setPersistent (boolean persistent);

  /**
	 * Returns whether this agent's data will be persistent between instances.
	 * @return  <code>true</code> if this agent's data will be persistent between  instances; <code>false</code> otherwise.
	 */
  public boolean isPersistent ();

  /**
   * Every <code>Agent</code> that is secured by the CSM (Casa Security Mechanism) will
   * communicate the
   */
//  public abstract void sendListOfAuthorizedUsersByProtectedAgent ();

  /**
   * Destroys the specified data object, deleting the corresponding node in the
   * <code>CASAFile</code>.  A boolean value is returned as to whether the data
   * object existed in the first place.
   *
   * @param dataObjectName The name of the data object that we wish to destroy.
   * @return <code>true</code> if the data object existed and was successfully
   * destroyed; <code>false</code> otherwise.
   * @throws IOException If there is a problem removing the node from the
   * <code>CASAFile</code>, such as a non-existent file.
   */
  public boolean destroyDataObject (String dataObjectName) throws IOException;

  /**
   * Stores some data for this agent based on the specified
   * <code>DataStorageDescriptor</code>. The <code>Status</code> is returned
   * indicating the success of the operation.
   *
   * @param dsd The <code>DataStorageDescriptor</code> that specifies what data
   * is to be written as well as to which dataobject it should be written.
   * @return A <code>Status</code> object indicating the result of the attempt.
   */
  public Status putDataObject (DataStorageDescriptor dsd);

  /**
   * Stores a <code>DataDescriptor</code> for this agent to the specified data
   * object.  The <code>Status</code> is returned indicating the success of the
   * operation.
   *
   * @param data The <code>DataDescriptor</code> to be stored.
   * @param dataObjectName The name of the data object that will be written to
   * or written over.
   * @param append Sets whether the data object should be appended to, or
   * overwritten.
   * @return A <code>Status</code> object indicating the result of the attempt.
   */
  public Status putDataObject (String data,
                               String dataObjectName,
                               boolean append);

  /**
   * Returns an <code>OutputStream</code> that can be used to write to the
   * specified data object.
   *
   * @param dataObjectName The name of the data object that the
   * <code>OutputStream</code> will write to.
   * @param append Sets whether the data object will be appended to, or
   * overwritten by the <code>OutputStream</code>.
   * @return An <code>OutputStream</code> that can be used to write to the
   * specified data object.
   * @throws IOException If there is a problem creating the
   * <code>OutputStream</code> for the specified data object.
   */
  public OutputStream getDataObjectOutputStream (String dataObjectName,
                                                 boolean append) throws
      IOException;

  /**
   * Returns an <code>InputStream</code> that can be used to read from the
   * specified data object.
   *
   * @param dataObjectName The name of the data object that the
   * <code>InputStream</code> will read from.
   * @return An <code>InputStream</code> that can be used to read from the
   * specified data object.
   * @throws IOException If there is a problem creating the
   * <code>InputStream</code> for the specified data object.
   */
  public InputStream getDataObjectInputStream (String dataObjectName) throws
      IOException;

  /**
   * Retreives a <code>StatusDataDescriptor</code> for this agent from the
   * specified data object.  The <code>StatusDataDescriptor</code> contains the
   * status of the attempt and the data of the specified data object.
   *
   * @param dataObjectName The name of the data object that will be read from.
   * @return A <code>StatusDataDescriptor</code> containing the status of the
   * attempt and the data of the specified data object.
   */
  public StatusString getDataObject (String dataObjectName);

  /**
   * Stores a boolean property.  If there was a property with the same name
   * previously, it is overwritten, even if the type is different.
   *
   * @param name The name of the property to store.
   * @param value The value of the property that matches the name given.
   */
  public void setBooleanProperty (String name, boolean value);

  /**
   * Retrieves the boolean property, returning it to the user.
   *
   * @param name The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not a boolean property.
   */
  public boolean getBooleanProperty (String name) throws PropertyException;

  /**
   * Stores a String property.  If there was a property with the same name
   * previously, it is overwritten, even if the type is different.
   *
   * @param name The name of the property to store.
   * @param value The value of the property that matches the name given.
   */
  public void setStringProperty (String name, String value);

  /**
   * Retrieves the String property, returning it to the user.
   *
   * @param name The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not a String property.
   */
  public String getStringProperty (String name) throws PropertyException;

  /**
   * Stores an integer property.  If there was a property with the same name
   * previously, it is overwritten, even if the type is different.
   *
   * @param name The name of the property to store.
   * @param value The value of the property that matches the name given.
   */
  public void setIntegerProperty (String name, int value);

  /**
   * Retrieves the integer property, returning it to the user.
   *
   * @param name The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not an integer
   * property.
   */
  public int getIntegerProperty (String name) throws PropertyException;

  /**
   * Stores a long integer property.  If there was a property with the same
   * name previously, it is overwritten, even if the type is different.
   *
   * @param name The name of the property to store.
   * @param value The value of the property that matches the name given.
   */
  public void setLongProperty (String name, long value);

  /**
   * Retrieves the long integer property, returning it to the user.
   *
   * @param name The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not a long integer
   * property.
   */
  public long getLongProperty (String name) throws PropertyException;

  /**
   * Stores a floating point property.  If there was a property with the same
   * name previously, it is overwritten, even if the type is different.
   *
   * @param name The name of the property to store.
   * @param value The value of the property that matches the name given.
   */
  public void setFloatProperty (String name, float value);

  /**
   * Retrieves the floating point property, returning it to the user.
   *
   * @param name The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not a floating point
   * property.
   */
  public float getFloatProperty (String name) throws PropertyException;

  /**
   * Stores a double precision floating point property.  If there was a
   * property with the same name previously, it is overwritten, even if the
   * type is different.
   *
   * @param name The name of the property to store.
   * @param value The value of the property that matches the name given.
   */
  public void setDoubleProperty(String name, double value);

  /**
   * Retrieves the double precision floating point property, returning it to
   * the user.
   *
   * @param name The name of the property to retrieve.
   * @return The value of the property that matches the name given.
   * @throws PropertyException If the given property is not a double precision
   * floating point property.
   */
  public double getDoubleProperty(String name) throws PropertyException;

  /**
   * Returns whether the specified property is contained in the properties.
   *
   * @param propertyName The name of the property that we are checking for.
   * @return <code>true</code> if there exists a property with the specified
   * name; <code>false</code> otherwise.
   */
  public boolean hasProperty (String propertyName);

  /**
   * Removes the property from the propertiesp.
   *
   * @param propertyName The name of the property to be removed.
   */
  public void removeProperty (String propertyName);

  /**
   * Determines if the agent has been fully initialized: registered with the
   * LAC and initialized its data file and properties.
   * 
   * @return true iff initialization is complete.
   */
  public boolean ready();
}
