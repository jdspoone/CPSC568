package casa;

import casa.exceptions.DataDescriptorException;
import casa.util.CASAUtil;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @author  <a href="laf@cpsc.ucalgary.ca">Chad La Fournie</a>,<a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class DataStorageDescriptor {
  /**
	 */
  private String dataObjectName = null; //object or file to store the data
  /**
	 */
  private String data = null;
  private boolean append = true;

  public DataStorageDescriptor () {
  }

  public DataStorageDescriptor (String storageObjectName, String data,
                                boolean append) {
    this.dataObjectName = storageObjectName;
    this.data = data;
    this.append = append;
  }

  /**
	 * @return
	 */
  public String getDataObjectName () {
    return dataObjectName;
  }

  /**
	 * @param storageObjectName
	 */
  public void setDataObjectName (String storageObjectName) {
    this.dataObjectName = storageObjectName;
  }

  /**
	 * @param data
	 */
  public void setData (String data) {
    this.data = data;
  }

  /**
	 * @return
	 */
  public String getData () {
    return data;
  }

  public boolean willAppend () {
    return append;
  }

  /**
	 * @param append
	 */
  public void setAppend (boolean append) {
    this.append = append;
  }

  /**
   *
   * @return String formatted to conform to CASA message protocols
   */
  public String toString () {
    StringBuffer buffer = new StringBuffer (dataObjectName);

    buffer.append (ML.BLANK);
    if (append) {
      buffer.append ("append");
    } else {
      buffer.append ("overwrite");

    }
    buffer.append (ML.BLANK);
    if (data != null) {
      buffer.append (CASAUtil.toQuotedString (data));
    } else {
      buffer.append (ML.NULL);

    }
    return buffer.toString ();
  }

  /**
   * Initializes a DataStorageDescriptor from a string of the form:
   *  "Object1 Index1 Some data of any form with or without spaces and abstract characters"
   * @param parser TokenParser containing the string to be parsed. The string must be in
   *               the order Name, Index, DataDescriptor
   * @param data   Object to be initialized
   * @throws DataDescriptorException if an error occurs parsing the string
   */
  public static void fromString (TokenParser parser,
                                 DataStorageDescriptor data) throws
      DataDescriptorException {
    try {
      String tmp = parser.getNextToken ();
      if (tmp != null) {
        data.setDataObjectName (tmp);
      } else {
        throw new DataDescriptorException ("No data object name.");
      }

      tmp = parser.getNextToken ();
      if (tmp != null) {
        data.setAppend (tmp.equals ("append"));
      } else {
        throw new DataDescriptorException ("No append/overwrite flag.");
      }

      tmp = parser.getRemaining ();
      if (tmp != null) {
        data.setData (tmp);
      } else {
        data.setData ("");
      }
    } catch (Exception e) {
      e.printStackTrace ();
      throw new DataDescriptorException (e);
    }
  }

  public static DataStorageDescriptor fromString (TokenParser parser) throws
      DataDescriptorException {
    DataStorageDescriptor data = new DataStorageDescriptor ();
    fromString (parser, data);

    return data;
  }
}