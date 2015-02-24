package casa.io;

import casa.util.CASAUtil;
import casa.util.UserMapXML;

import java.io.IOException;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class CASAFileLACKnownUsersMap extends UserMapXML {
  public static String PROPERTIES_SECURITY_ENTRY = "_*known_users";
  /**
	 * The CASAFile that we are writing these properties to.
	 */
  private CASAFile file;

  /**
   * Determines whther the file has been initialized to what we beleive to be a
   * correct value.  <code>true</code> if the file has not been set yet;
   * <code>false</code> otherwise.
   */
  private boolean initializing = true;

  /**
   * Determines whether the properties are automatically written when they
   * are changed.
   */
  private boolean keepFileUpdated = true;

  public CASAFileLACKnownUsersMap(CASAFile file) {
    super ();
    this.file = file;
    initializing = false;

    CASAInputStream inputStream = null;
    try {
      inputStream = new CASAInputStream (PROPERTIES_SECURITY_ENTRY, file);

      read (inputStream);
    } catch (NodeNotFoundException e) {
      // The node doesn't exist yet, so properties will be empty.
    } catch (IOException e) {
      // File could not be read to find the node, or the file is not a CASAFile
    	CASAUtil.log("error", "CASAFileLACKnownUsersMap.constructor: IOException opening file",e, true);
    }

    try {
      inputStream.close ();
    } catch (Exception e) {}
  }

  /**
   * Writes the propreties to the CASAFile.
   */
  public void writeSecurityProperties () {
    if (initializing) {
      // We are in initialization, so ignore write attempts (which may be
      // automatic due to super class efforts).
      return;
    }

    if (!file.isCASAFile ()) {
      file = new CASAFile (file.getAbsolutePath ());
      try {
        file.createNewFile ();
      } catch (IOException e) {
        /** @todo Handle exception in writeProperties (). */
        e.printStackTrace (System.err);
      }
    }

    CASAOutputStream outputStream = null;
    try {
      // open the file and a stream to read
      outputStream = new CASAOutputStream (PROPERTIES_SECURITY_ENTRY,
                                           CASAFile.MODE_OVERWRITE, file);

      write (outputStream);
    } catch (IOException e) {
      /** @todo Handle exception in writeProperties (). */
      e.printStackTrace (System.err);
    }

    try {
      outputStream.close ();
    } catch (Exception e) {
    }
  }


  /**
	 * Sets whether this object is to keep the file updated with every property change.
	 * @param keepUpdated  <code>true</code> if the object is set to keep the file  updated with every property change; <code>false</code> otherwise.
	 */
  public void setKeepFileUpdated (boolean keepUpdated) {
    keepFileUpdated = keepUpdated;
  }

  /**
	 * Returns whether this object is set to keep the file updated with every property change.
	 * @return  <code>true</code> if the object is set to keep the file updated  with every property change; <code>false</code> otherwise.
	 */
  public boolean getKeepFileUpdated () {
    return keepFileUpdated;
  }

  /**
   * Called when the XML is modified for any reason.  This overrides the
   * super-class method so that the properties are written automatically if
   * <code>keepFileUpdated</code> is set.
   */
  protected void setModified () {
    super.setModified ();

    if (keepFileUpdated) {
      writeSecurityProperties ();
    }
  }

  /**
   * Compares the specified <code>Object</code> with this
   * <code>CASAFilePropertiesMap</code> for equality.  The two are equal if the
   * <code>Object</code> is an instance of <code>PropertiesMap</code> and
   * all of their properties are equal, and furthermore, if the
   * <code>Object</code> is an instance of <code>CASAFilePropertiesMap</code>,
   * they must both use the same <code>CASAFile</code>.
   *
   * @param object The <code>Object</code> to be compared for equality with
   * this <code>CASAFilePropertiesMap</code>.
   * @return <code>true</code> if the specified <code>Object</code> is equal to
   * this <code>CASAFilePropertiesMap</code>; <code>false</code> otherwise.
   */
  public boolean equals (Object object) {
    if (object == this) {
      return true;
    } else if (super.equals (object)) {
      if (object instanceof CASAFileLACKnownUsersMap) {
        CASAFileLACKnownUsersMap map = (CASAFileLACKnownUsersMap) object;
        return map.file.equals (this.file);
      } else {
        return true;
      }
    }

    return false;
  }
}