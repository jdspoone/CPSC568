package casa.io.tools;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import casa.util.Property;

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

public class CASAFileTreeCellRenderer extends DefaultTreeCellRenderer {
  JTextArea rawDataTextArea;
  JTextArea multiLineStringTextArea;

  public CASAFileTreeCellRenderer () {
    rawDataTextArea = new JTextArea ();
    rawDataTextArea.setFont (new Font ("Monospaced", Font.PLAIN, 12));
    multiLineStringTextArea = new JTextArea ();
  }


  public Component getTreeCellRendererComponent (JTree tree, Object value,
                                                 boolean sel, boolean expanded,
                                                 boolean leaf, int row,
                                                 boolean hasFocus) {
    if (Property.class.isInstance (value)) {
      Property tempProperty = (Property) value;

      return super.getTreeCellRendererComponent (tree,
                                                 "Value: \"" +
                                                 tempProperty.toString () +
                                                 "\"", sel, expanded, leaf, row,
                                                 hasFocus);
    } else if (CASAFileTreeDataRaw.class.isInstance (value)) {
      CASAFileTreeDataRaw tempData = (CASAFileTreeDataRaw) value;
      rawDataTextArea.setText (tempData.getData ());
      return rawDataTextArea;
    } else if (CASAFileTreeDataString.class.isInstance (value)) {
      CASAFileTreeDataString tempString = (CASAFileTreeDataString) value;
      multiLineStringTextArea.setText (tempString.getString ());
      return multiLineStringTextArea;
    }
    return super.getTreeCellRendererComponent (tree, value, sel, expanded, leaf,
                                               row, hasFocus);
  }

}