package casa;

import java.util.*;

import casa.util.CASAUtil;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @todo  add doc
 */
public class StatusObjectList extends Status {
  /**
	 */
  private Vector<?> objects;
  //private Class<?> castableClass = null;
  //private Set<?> castableInterfaces = null;

  /**
   * Default constructor
   */
  public StatusObjectList () {
    super ();

    objects = new Vector<Object> ();
  }

  public StatusObjectList (int status, Vector<?> newObjects) {
    super (status);

    if (newObjects == null) {
      this.objects = new Vector<Object> ();
    } else {
      this.objects = newObjects;
    }
    updateCastables ();
  }

  public StatusObjectList (int status, String explanation) {
    super (status, explanation);

    this.objects = new Vector<Object> ();
  }

  public StatusObjectList (int status, String explanation, Vector<?> newObjects) {
    super (status, explanation);

    if (newObjects == null) {
      this.objects = new Vector<Object> ();
    } else {
      this.objects = newObjects;
    }
    updateCastables ();
  }

  public StatusObjectList (TokenParser p) throws Exception {
    this();
    fromString(p);
  }

  /**
	 * @param newObjects
	 */
  public void setObjects (Vector<?> newObjects) {
    if (newObjects == null) {
      this.objects = new Vector<Object> ();
    } else {
      this.objects = newObjects;
    }
    updateCastables ();
  }

  /**
	 * @return
	 */
  public Vector<?> getObjects () {
    return objects;
  }

  public boolean containsType (Class<?> testClass) {
//    return testClass.isInstance (object);
    return true;
  }

  private void updateCastables () {
    Class<?> tempClass = null;
    Set<?> tempInterfaces = null;

    Iterator<?> i = objects.iterator();
    while (i.hasNext ()) {
      Class<?> currentClass = i.next ().getClass ();

      if (tempClass == null) {
        tempClass = currentClass;
        tempInterfaces = completeInterfaceList (currentClass);
        continue;
      }

      if (! tempClass.equals (Object.class)) {
        // Start with this item's class
        Class<?> traverseClass = currentClass;

        // Search up the class heirarchy until you find the lowest common
        // subclass
        while (!traverseClass.isAssignableFrom (tempClass) &&
               !traverseClass.equals (Object.class)) {
          traverseClass = traverseClass.getSuperclass ();
        }
        tempClass = traverseClass;
      }

      if (tempInterfaces.size () > 0) {
        Set<Object> tempSet = new HashSet<Object> ();
        Set<?> tempSet2 = completeInterfaceList (currentClass);
        Iterator<?> j = tempSet2.iterator ();
        while (j.hasNext ()) {
          Object o = j.next ();
          if (tempInterfaces.contains(o)) {
            tempSet.add (o);
          }
        }
        tempInterfaces = tempSet;
      }
    }

//    castableClass = tempClass;
//    castableInterfaces = tempInterfaces;
  }

  private static Set<?> completeInterfaceList (Class<?> tempClass) {
    Set<Object> outputList = new HashSet<Object> ();
    Set<Class<?>> inputList = new HashSet<Class<?>> ();

    // initialize inputList
    Class<?>[] interfaceList = tempClass.getInterfaces ();
    for (int j = 0; j < interfaceList.length; j++) {
      inputList.add (interfaceList[j]);
    }

    while (! inputList.isEmpty()) {
      Class<?> tempInterface = inputList.iterator ().next ();
      inputList.remove (tempInterface);
      outputList.add (tempInterface);
      interfaceList = tempInterface.getInterfaces ();
      for (int j = 0; j < interfaceList.length; j++) {
        if (! outputList.contains(interfaceList[j])) {
          inputList.add (interfaceList[j]);
        }
      }
    }

    return outputList;
  }

  @Override
public String toString_extension () {
//    StringBuffer buffer = new StringBuffer ();
//
//    for (Iterator i = objects.iterator (); i.hasNext (); ) {
//      buffer.append (ML.BLANK);
//      buffer.append (TokenParser.makeFit (i.next ().toString ()));
//    }
//
//    return buffer.toString ();

    String str = CASAUtil.serialize(objects);
    return CASAUtil.toQuotedString (str);
  }

  @Override
public void fromString_extension (TokenParser parser) throws Exception {
//    try {
//      Vector vector = new Vector ();
//      String str;
//
//      for (str = parser.getNextToken (); str != null && !str.equals (")");
//          str = parser.getNextToken ()) {
//        vector.add (new URLDescriptor (str));
//      }
//      if (str.equals (")"))
//        parser.putback ();
//
//      setURLs (vector);
//    } catch (URLDescriptorException e) {
//      e.printStackTrace ();
//      throw new ParseException ("Cannot parse URLDescriptor", 0);
//    }

    String str = parser.getNextToken ();
    objects = (Vector<?>) CASAUtil.unserialize(str, null);
    updateCastables ();
  }

//  public static void main (String args[]) {
//    StatusObjectList s = new StatusObjectList (21, "Test2", null);
//    URLDescriptor temp = null;
//    URLDescriptor temp2 = null;
//    try {
//      temp = new URLDescriptor ("127.0.0.1", "9005");
//      temp2 = new URLDescriptor ("127.0.0.1", "9006");
//    } catch (casa.exceptions.URLDescriptorException ex) {
//      ex.printStackTrace ();
//    }
//
//    Vector v = new Vector ();
//    v.add (new Vector ());
//    v.add (new Vector ());
//    v.add (new java.util.ArrayList ());
//    v.add (new java.util.TreeSet ());
////    v.add (new java.util.Hashtable ());
//
//    String testString = s.toString ();
//
//    s = new StatusObjectList (0, v);
//
//    System.out.println (s.toString ());
//
//    try {
//      s.fromString (new TokenParser (testString));
//    } catch (Exception ex1) {
//      System.out.println("crap!");
//    }
//  }
}