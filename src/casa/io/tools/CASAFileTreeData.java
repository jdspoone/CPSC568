package casa.io.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Vector;

import casa.io.CASAFile;
import casa.io.CASAFilePropertiesMap;
import casa.io.CASAInputStream;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @author  Jason Heard
 */

public class CASAFileTreeData {
  /**
	 */
  private CASAFile file;
  private String nodeName;

  private boolean propsEntry = false;
  /**
	 */
  private CASAFilePropertiesMap props;
  private Vector propsList;

  public CASAFileTreeData (CASAFile file, String nodeName) {
    this.file = file;
    this.nodeName = nodeName;

    if (CASAFilePropertiesMap.PROPERTIES_ENTRY_NAME.equals (nodeName)) {
      props = new CASAFilePropertiesMap (file);
      propsList = new Vector ();
      for (Enumeration e = props.getProperties (); e.hasMoreElements (); ) {
        propsList.add (e.nextElement ());
      }
      propsEntry = true;
    }
  }

  public int size () {
    if (propsEntry) {
      return propsList.size ();
    } else {
      return 1;
    }
  }

  public Object get (int index) {
    if (index == 0 && !propsEntry) {
      return getData ();
    } else if (propsEntry) {
      if (propsList.size () > index) {
        return new CASAFileTreeProperty (props, (String) propsList.get (index));
      }
    }

    return null;
  }

  public int indexOf (Object child) {
    if (String.class.isInstance (child)) {
      String tempStr = (String) child;
      if (propsEntry) {
        return propsList.indexOf (tempStr.substring (13, tempStr.length () - 1));
      }
    }

    return -1;
  }

  public String toString () {
    return "Data";
  }

  public Object getData () {
    CASAInputStream inputStream = null;
    BufferedReader br = null;

    try {
      // open the file and a stream to read
      inputStream = new CASAInputStream (nodeName, file);
      br = new BufferedReader (new InputStreamReader (inputStream));
    } catch (IOException ex) {
      return "ERROR: unnable to open node for reading";
    }

    StringBuffer buffer = new StringBuffer ();

    try {
      // retrieve message
      char[] c = new char[128];

      int numRead = br.read (c);
      while (numRead != -1) { // stop if eof
        buffer.append (c, 0, numRead);
        numRead = br.read (c);
      }
    } catch (Exception e) {
      return "ERROR: exception thrown during read";
    }

    try {
      inputStream.close ();
    } catch (IOException e) {
    }

    try {
      br.close ();
    } catch (IOException e) {
    }

    boolean rawData = false;
    final int NUM_BYTES_CHECKED = 16;
    int i = 0;
    int length = buffer.length ();

    while ((i < length) && (i < NUM_BYTES_CHECKED) && ! rawData) {
      if ((buffer.charAt (i) == 0) || (buffer.charAt (i) > 127)) {
        rawData = true;
      }
      i++;
    }

    if (rawData) {
      StringBuffer hexData = new StringBuffer ();
      int hexOnLine = 0;
      String tempString;
      char tempChar;
      for (i = 0; i < length; i++) {
        tempString = Integer.toHexString (buffer.charAt (i));
        if (tempString.length () == 1) {
          hexData.append ('0');
        }
        hexData.append (tempString);
        hexData.append (' ');
        hexOnLine++;
        if (hexOnLine == 8) {
          hexData.append (' ');
        }
        if (hexOnLine >= 16) {
          hexData.append (' ');
          for (int j = i - 15; j <= i; j++) {
            tempChar = buffer.charAt (j);
            if (tempChar >= 32 && tempChar <= 126) {
              hexData.append (tempChar);
            } else {
              hexData.append ('.');
            }
          }
          hexData.append ('\n');
          hexOnLine = 0;
        }
      }

      if (hexOnLine != 0) {
        for (int j = 0; j < (16 - hexOnLine); j++) {
          hexData.append ("   ");
        }
        if (hexOnLine < 9) {
          hexData.append (' ');
        }
        hexData.append (' ');
        for (int j = i - hexOnLine; j < length; j++) {
          tempChar = buffer.charAt (j);
          if (tempChar >= 32 && tempChar <= 126) {
            hexData.append (tempChar);
          } else {
            hexData.append ('.');
          }
        }
      }

      return new CASAFileTreeDataRaw (hexData.toString ());
    } else {
      return new CASAFileTreeDataString (buffer.toString ());
    }
  }
}