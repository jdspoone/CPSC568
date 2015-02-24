package casa.io.tools;

import java.io.IOException;

import casa.io.CASAFile;
import casa.io.CASAFileIndexEntry;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @author  Jason Heard
 */

public class CASAFileTreeIndexEntry {
  private String nodeName;
  private long offset;
  /**
	 */
  private CASAFile file;

  public CASAFileTreeIndexEntry(CASAFileIndexEntry index, CASAFile file){
    this.nodeName = index.getName ();
    this.offset = index.getOffset ();
    this.file = file;
  }

  public String getName () {
    return nodeName;
  }

  /**
	 * @return
	 */
  public long getOffset () {
    return offset;
  }

  public long getLength () {
    try {
      return file.getNodeLength (nodeName);
    }
    catch (IOException ex) {
      return 0L;
    }
  }

  public int size () {
    return 3;
  }

  public Object get (int index) {
    if (index == 0) {
      return "Offset: " + offset;
    } else if (index == 1) {
      return "Length: " + getLength ();
    } else if (index == 2) {
      return new CASAFileTreeData (file, nodeName);
    }

    return null;
  }

  public int indexOf (Object child) {
    if (String.class.isInstance (child)) {
      String tempStr = (String) child;
      if (tempStr.charAt(0) == 'O') {
        return 0;
      } else if (tempStr.charAt(0) == 'L') {
        return 1;
      } else if (tempStr.charAt(0) == 'D') {
        return 2;
      }
    }

    return -1;
  }

  public String toString() {
    return "Name: \"" + nodeName + "\"";
  }
}