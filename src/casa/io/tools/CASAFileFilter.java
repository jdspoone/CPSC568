package casa.io.tools;

import java.io.File;

import javax.swing.filechooser.FileFilter;

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
 * @author Jason Heard
 */

public class CASAFileFilter extends FileFilter {
	@Override
  public boolean accept(File pathname) {
    if (pathname.isDirectory ()) {
      return true;
    }

    String extension = getExtension(pathname);
    if (extension != null) {
      if (extension.equalsIgnoreCase ("casaCD") ||
          extension.equalsIgnoreCase ("casa")) {
        return true;
      } else {
        return false;
      }
    }

    return false;
  }

  private String getExtension (File pathname) {
    String extension = null;
    String s = pathname.getName ();
    int i = s.lastIndexOf ('.');

    if (i > 0 && i < s.length () - 1) {
      extension = s.substring (i + 1);
    }
    return extension;
  }

  @Override
  public String getDescription() {
    return "CASAFile (*.casa; *.casaCD)";
  }
}