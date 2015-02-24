package casa.util;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: Instances of this class maintain named objects under one or more
 * names as "equivalence sets".  ie: you can have several names for the same object.
 * <p>
 * The referenced object do not have to be unique and may be null.  Therefore you
 * can use this class to keep just names, which end up being simple synonym strings.
 * </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its 
 * documentation for any purpose is hereby granted without fee, provided that the 
 * above copyright notice appear in all copies and that both that copyright notice 
 * and this permission notice appear in supporting documentation.  The  Knowledge 
 * Science Group makes no representations about the suitability of  this software 
 * for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
public class EquivalenceSets<T> {
	private int nextID = 1;
	
	@SuppressWarnings("serial")
	private TreeMap<String, Integer> names = new TreeMap<String, Integer>() {
		@Override
		public Integer put(String key, Integer value) {
			Integer ret = super.put(key, value);
			if (ret==null) {
				
				Vector<String> v = ids.get(value);
				if (v==null) 
					v = new Vector<String>();
				v.add(key);
				ids.put(value, v);
			}
			else {
				Vector<String> v = ids.get(value);
				assert v!=null;
				if (!v.contains(key)) {
					v.add(key);
				}
			}
			return ret;
		};
		@Override
		public Integer get(Object key) {
			Integer ret = super.get(key);
			if (ret==null)
				return -1;
			return ret;
		}
	};
	private TreeMap<Integer, Vector<String>> ids = new TreeMap<Integer, Vector<String>>();
	private TreeMap<Integer, T> objects = new TreeMap<Integer, T>();

	/**
	 * Default constructor.
	 */
	public EquivalenceSets() {		
	}

	/**
	 * Initialize this EquivalenceSets object with the String/Object pairs in <em>map</em>.
	 */
	public EquivalenceSets(Map<String, T> map) {
		if (map!=null) {
			for (String key: map.keySet()) {
				put(key, map.get(key));
			}
		}
	}

	/**
	 * Initialize this EquivalenceSets object with the the names in <em>names</em>.  
	 * All of them will be in singleton equivalence sets and reference a null object.
	 */
	public EquivalenceSets(String[] names) {
		if (names!=null) {
			for (String name: names) {
				put(name);
			}
		}
	}

	/**
	 * Initialize this EquivalenceSets object with the the names in <em>names</em>.
	 * All of them will be in singleton equivalence sets and reference a null object.
	 */
	public EquivalenceSets(Collection<String> names) {
		if (names!=null) {
			for (String name: names) {
				put(name);
			}
		}
	}

	private int getNewID() {
		return nextID++;
	}
	
	private int getID(String name) {
		Integer ret = names.get(name);
		if (ret==null)
			return 0;
		else
			return ret;
	}

  /**
   * Places a name in the database, associated with a null object.
   * @param name
   * @return 0 if this is a new name in the database, -1 if the name was already there.
   * @see #put(String, Object)
   */
	public int put(String name) {
		if (names.get(name)<=0) {
			names.put(name, getNewID());
			return 0;
		}
		return -1;
	}
	
	/**
	 * Places a new name if the database associated with the object.
	 * @param name
	 * @param object
	 * @return Either null, or the object replaced if there was one.
	 */
	public T put(String name, T object) {
		int id = names.get(name);
	  if (id>0) {
	  	T ret = objects.get(id);
	  	objects.put(id, object);
	  	return ret;
	  }
		id = getNewID();
		names.put(name, id);
		if (object!=null)
			objects.put(id, object);
		return null;
	}
	
	/**
	 * Places the two names in the same equivalence class.
	 * @param name1
	 * @param name2
	 * @return The object associated with the new (or extended) equivalence class.
	 * @throws Exception If the two names are already associated with an non-null object that aren't equal(), or can't be compared with equal().
	 */
	public T setEqual(String name1, String name2) throws Exception {
  	int id1 = names.get(name1);
		int id2 = names.get(name2);
		if (id1<=0) {
			if (id2<=0) {
				int id = getNewID();
				names.put(name1, id);
				names.put(name2, id);
				return null;
			}
			else {
				names.put(name1, id2);
				return objects.get(id2);
			}
		}
		else {
			if (id2<=0) {
				names.put(name2, id1);
				return objects.get(id1);
			}
			else { // BOTH names are already there in the DB
				T o1 = objects.get(id1);
				T o2 = objects.get(id2);
				if (o1==null) {
					names.put(name1, id2); //replace the id in the DB
					return o2;
				}
				else {
					if (o2==null) {
						names.put(name2, id2); //replace the id in the DB
						return o1;
					}
					else { // BOTH names have objects in the DB
						boolean eq;
						try {
							eq = o1.equals(o2);
						} catch (Throwable e) {
							throw new Exception("Names "+name1+" and "+name2+" both refer to objects incomparable by equals()");
						}
						if (eq) {
							names.put(name2, id1); //replace the id in the DB
							return o1;
						}
						else {
							throw new Exception("Names "+name1+" and "+name2+" both refer to different objects (by equals())");
						}
					}
				}
			}
		}
	}
	
	/**
	 * @param name The name under which to retrieve the object.
	 * @return The object associated with <em>name</em>.
	 */
	public Object get(String name) {
	  int id = getID(name);
	  if (id==0)
	  	return null;
	  return objects.get(id);
	}
	
	/**
	 * @param name The name from which to retrieve the other equivalent names.
	 * @return The names in the same equivalence class as the given <em>name</em>
	 * (including <em>name</em>); or null if the given <em>name</em> is not in 
	 * any equivalence class in this object.
	 */
	public String[] getEqClass(String name) {
		int id = names.get(name);
		if (id<=0) {
			return null;
		}
		Vector<String> v = ids.get(id);
		return v.toArray(new String[v.size()]);
	}
	
	public boolean hasObject(String name) {
		return names.containsKey(name);
	}
	
	/**
	 * @param name1
	 * @param name2
	 * @return true iff both names are registered they have been {@link #setEqual(String, String)}.
	 */
	public boolean equal(String name1, String name2) {
		int id1 = names.get(name1);
		if (id1<=0)
			return false;
		int id2 = names.get(name2);
		if (id2<=0)
			return false;
		return id1==id2;
	}
	
}