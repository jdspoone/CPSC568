/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.util;

import java.lang.ref.WeakReference;
import java.util.TreeMap;
import java.util.WeakHashMap;

/**
 * This static class is used to track instances of tagged classes to track
 * their allocation and deallocation via the GC.  This is done through the 
 * {@link WeakReference} system and the {@link WeakHashMap} class in Java.
 * This will hopefully be useful in debugging memory leaks.  The class records
 * all the objects instantiated (but removes them when the GC deallocates them),
 * but only reports the counts (allocated, and deallocated -- the difference 
 * between these two counts is the number still in memory).  One can use the
 * debugger to actually LOOK at the objects still in memory.  Or one might
 * extend this class to report on the in-memory objects in more detail.
 * <p>
 * To use this class, you simple insert the line:   
 * <pre>
 *   InstanceCounter.add(this);
 * </pre>
 * in ALL of its constructors (except ones that call move primitive constructors, of course)
 * of the classes you wish to track.  Tracking of all subclasses will automatically
 * occur.
 *
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class InstanceCounter {
	
  // Adds a shutdown hook to write out a report whenever the application shuts down.
	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
			  InstanceCounter.report();
			}
		});
	}
	
	/**
	 * A {@link WeakReference} structure to keep pointers objects added and a 
	 * call-count.  There is one of these for each class being tracked. 
	 * @param <T>
	 */
	static class Data<T extends Comparable<T>> {
		public Data(T obj) {
			instantiated = 1;
			inMemory = new WeakHashMap<T, String>();
			inMemory.put(obj,"");
		}
		public void add(T obj) {
			instantiated++;
			inMemory.put(obj,"");
		}
		int instantiated = 1;
		WeakHashMap<T, String> inMemory;
	}
	
	/**
	 * The "database" of registered objects to keep track of.  This is nothing
	 * but a dictionary from Class names to the collection of {@link WeakReference} pointers
	 * the objects we're tracking. 
	 */
	@SuppressWarnings("rawtypes")
	static private TreeMap <String, Data> counts = new TreeMap<String, Data>(); 
	
	/** 
	 * This constructor is private to assure that this class is a Singleton (only
	 * one instantiation per process).
	 */
	private InstanceCounter() {
		add(this);
	}
	
	/**
	 * Adds an object to InstanceCounter's record. The object will automatically
	 * be tracked for it's eventual deallocation via a {@link WeakReference}.
	 * For any object to be tracked using InstanceCounter, you should place the line
	 * <pre>
	 *   InstanceCounter.add(this);
	 * </pre>
	 * in ALL of its constructors (except ones that call move primitive constructors, of course).
	 * @param o The object to be tracked.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public void add(Object o) {
		String cls = getTheClassName(o);
		Data i = counts.get(cls);
		try {
			if (i==null) {
				if (o instanceof Comparable<?>)	
					counts.put(cls, new Data((Comparable) o));
				else
					assert false;
			}
			else {
				if (o instanceof Comparable<?>)	
					i.add((Comparable) o);
				else
					assert false;
				counts.put(cls, i);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			assert false;
		}
	}

	/**
	 * Produces a report to standard output.
	 * @see #getReport()
	 */
	public static void report() {
		System.out.println(getReport());
	}
	
	/**
	 * @return The String table of Objects Created / Objects Erased by their class.
	 */
	public static String getReport() {
		StringBuilder b = new StringBuilder();
		b.append("Objects instantiated:\n Exist Erased Class\n");
		for (String cls: counts.keySet()) {
			@SuppressWarnings("rawtypes")
			Data pr = counts.get(cls); 
			String c = Integer.toString(pr.instantiated);
			for (int i=6-c.length(); i>0; i--)
				b.append(' ');
			b.append(c).append(' ');
			c = Integer.toString(pr.instantiated - pr.inMemory.size());
			for (int i=6-c.length(); i>0; i--)
				b.append(' ');
			b.append(c)
			.append(' ').append(cls.substring(6)).append('\n');
		}
		return b.toString();
	}
	
	private static String getTheClassName(Object o) {
		return o.getClass().toString();
	}

}
