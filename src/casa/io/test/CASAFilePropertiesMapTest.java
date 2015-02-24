package casa.io.test;

import java.util.Enumeration;

import casa.io.CASAFile;
import casa.io.CASAFilePropertiesMap;
import casa.io.CASAFileLACKnownUsersMap;
import casa.util.PropertyException;

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
 */
public class CASAFilePropertiesMapTest {
  public static void main (String[] args) {
    boolean keepUpdated = false;
    CASAFile file = new CASAFile ("testCD.casaCD");

    if (! file.isCASAFile ()) {
      try {
        file = new CASAFile ("testCD.casaCD");
        file.createNewFile ();
      } catch (Exception e) {
        e.printStackTrace (System.err);
      }
    }

    CASAFilePropertiesMap props = new CASAFilePropertiesMap (file);
    CASAFileLACKnownUsersMap props1 = new CASAFileLACKnownUsersMap (file);
    props.setKeepFileUpdated (keepUpdated);

    System.out.println ("---- Initial Properties");
    for (Enumeration e = props.getProperties (); e.hasMoreElements (); ) {
      String temp = (String) e.nextElement ();
      System.out.println (temp + ": '" + props.getProperty (temp) + "'");
    }
    System.out.println ("----");
    System.out.println ();

    System.out.print ("Setting \"one\" to integer 1:");
    props.resetModified ();
    props.setInteger ("one", 1);
    System.out.println (" modified = " + props.isModified ());

    System.out.print ("Setting \"two\" to integer 2:");
    props.resetModified ();
    props.setInteger ("two", 2);
    System.out.println (" modified = " + props.isModified ());

    System.out.print ("Setting \"three\" to float 3.0F:");
    props.resetModified ();
    props.setFloat ("three", 3.0F);
    System.out.println (" modified = " + props.isModified ());

    System.out.print ("Setting \"four\" to float 4.0F:");
    props.resetModified ();
    props.setFloat ("four", 4.0F);
    System.out.println (" modified = " + props.isModified ());

    System.out.print ("Setting \"nine\" to long 30L:");
    props.resetModified ();
    props.setLong ("nine", 30L);
    System.out.println (" modified = " + props.isModified ());

    System.out.print ("Setting \"ten\" to double 2.0:");
    props.resetModified ();
    props.setDouble ("ten", 2.0);
    System.out.println (" modified = " + props.isModified ());

    System.out.print ("Setting \"eleven\" to long 69L:");
    props.resetModified ();
    props.setLong ("eleven", 69L);
    System.out.println (" modified = " + props.isModified ());
    System.out.println ();

    System.out.println ("Writing and re-reading properties.");
    props.writeProperties ();
    props = new CASAFilePropertiesMap (file);
    props.setKeepFileUpdated (keepUpdated);
    System.out.println ();

    props1 = new CASAFileLACKnownUsersMap (file);
    props1.writeSecurityProperties();
    props1.setString("Gabriel", "sdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdf");
    props1.setKeepFileUpdated(keepUpdated);

    System.out.println ("---- Properties After Modifications");
    for (Enumeration e = props.getProperties (); e.hasMoreElements (); ) {
      String temp = (String) e.nextElement ();
      System.out.println (temp + ": '" + props.getProperty (temp) + "'");
    }
    System.out.println ("----");
    System.out.println ();

    System.out.print ("Setting \"six\" to integer 6:");
    props.resetModified ();
    props.setString ("six", "6");
    System.out.println (" modified = " + props.isModified ());

    System.out.print ("Setting \"seven\" to float 7.0F:");
    props.resetModified ();
    props.setFloat ("seven", 7.0F);
    System.out.println (" modified = " + props.isModified ());

    System.out.print ("Setting \"four\" to boolean true:");
    props.resetModified ();
    props.setBoolean ("four", true);
    System.out.println (" modified = " + props.isModified ());

//    System.out.print ("Removing \"two\":");
//    props.resetModified ();
//    props.removeProperty ("two");
//    System.out.println (" modified = " + props.isModified ());

    System.out.print ("Setting \"nine\" to double 5.2:");
    props.resetModified ();
    props.setDouble ("nine", 5.2);
    System.out.println (" modified = " + props.isModified ());

    System.out.print ("Removing \"ten\":");
    props.resetModified ();
    props.removeProperty ("ten");
    System.out.println (" modified = " + props.isModified ());
    System.out.println ();

    System.out.println ("Writing and re-reading properties.");
    props.writeProperties ();
    props = new CASAFilePropertiesMap (file);
    props.setKeepFileUpdated (keepUpdated);
    System.out.println ();

    System.out.println ("---- Properties After Further Modifications");
    for (Enumeration e = props.getProperties (); e.hasMoreElements (); ) {
      String temp = (String) e.nextElement ();
      System.out.println (temp + ": '" + props.getProperty (temp) + "'");
    }
    System.out.println ("----");
    System.out.println ();

    System.out.println ("---- Reading Non-Existant / Wrong-Type Properties");
    System.out.print ("six (as integer):");
    try {
      System.out.println (" '" + props.getInteger ("six") + "'");
    } catch (PropertyException e) {
      System.out.println (" " + e.getMessage ());
    }
    System.out.print ("fred (as boolean):");
    try {
      System.out.println (" '" + props.getBoolean ("fred") + "'");
    } catch (PropertyException e) {
      System.out.println (" " + e.getMessage ());
    }
    System.out.print ("nine (as float):");
    try {
      System.out.println (" '" + props.getFloat ("nine") + "'");
    } catch (PropertyException e) {
      System.out.println (" " + e.getMessage ());
    }
    System.out.print ("bob (as double):");
    try {
      System.out.println (" '" + props.getDouble ("bob") + "'");
    } catch (PropertyException e) {
      System.out.println (" " + e.getMessage ());
    }
    System.out.println ("----");
  }
}