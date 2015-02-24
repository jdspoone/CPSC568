/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
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
package casa.abcl;

import casa.util.CASAUtil;
import casa.util.Pair;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.LispObject;

public class ParamsMap {
	
	private class ParamData extends Pair<Object, LispObject> {
		boolean defaulted;
		public ParamData(Pair<Object, LispObject> x, boolean defaulted) {
			super(x);
			this.defaulted = defaulted;
		}
		public ParamData(Object javaObj, LispObject LispObj, boolean defaulted) {
			super(javaObj,LispObj);
			this.defaulted = defaulted;
		}
	}
	
	private Map<String, ParamData> map = new TreeMap<String, ParamData>();
	public ParamsMap() {};
	public ParamsMap(ParamsMap other) {
		map = new TreeMap<String, ParamData>(other.map);
	}
	
	public ParamsMap (Map<String, Object> javaObjectMap) {
		for (String key : javaObjectMap.keySet ()) {
			Object o = javaObjectMap.get (key);
			
			LispObject lispObject = (o instanceof LispObject)?(LispObject)o:JavaObject.getInstance(o, true);
			
			this.put (key, o, lispObject, false);
		}
	}
	
	public ParamsMap (Pair<String, Object>... params) {
		for (Pair<String,Object> p : params) {
			String key = p.getFirst();
			Object o = p.getSecond();
			LispObject lo = (o instanceof LispObject)?(LispObject)o:JavaObject.getInstance(o, true);
			this.put (key, o, lo, false);
		}
	}
	
	public Object getJavaObject(String key) {
		Pair<Object, LispObject> p = map.get(key.toUpperCase());
		if (p==null) return null;
		return p.getFirst();
	}

	/**
	 * Returns the value for the key iff it's of type cls or null if there either is no such key or 
	 * the value is null.  If the object is a string and cls has a string constructor that
	 * successfully converts to an object of cls, then the string is converted and returned.  Otherwise,
	 * an Exception is thrown.
	 * @param key Name of the parameter to look up
	 * @param cls The class the object is expected to be
	 * @return an object of type cls
	 * @throws LispException if the object isn't of type cls or isn't a string that can be converted to an object of type cls by cls's string constructor.
	 */
	public <T> T getJavaObject(String key, Class<T> cls) throws LispException {
		Object obj = getJavaObject(key);
		if (obj==null) return null;
		String string;
		Exception e2;
		if (cls.isInstance (obj)) {
			return cls.cast (obj);
		}
		else if (obj instanceof String) {
			string = (String)obj;
			try {
//		    Constructor<T> constructor = cls.getConstructor(String.class);
//			  return constructor.newInstance(string);
			  return (T)CASAUtil.unserialize(string, cls.getCanonicalName());
			}
			catch (Exception e) {
				e2 = e;
			}
		}
		else {
			e2 = new LispException("Object isn't subsummed by "+cls+" and isn't a string that call be converted to "+cls);
		}
		throw new LispException("expecting a "+cls+" as the "+key+" parameter, but got \""+obj+"\" of type "+obj.getClass(),e2);
	}

	public LispObject getLispObject(String key) {
		Pair<Object, LispObject> p = map.get(key.toUpperCase());
		if (p==null) return null;
		return p.getSecond();
	}
	public boolean containsKey(Object key) {
		return map.containsKey(((String)key).toUpperCase());
	}
	public boolean isEmpty() {
		return map.isEmpty();
	}
	public Set<String> keySet() {
		return map.keySet();
	}
	public Pair<Object, LispObject> put(String key, Pair<Object, LispObject> value, boolean defaulted) {
		return map.put(key.toUpperCase(), new ParamData(value,defaulted));
	}
	public Pair<Object, LispObject> putJavaObject (String key, Object javaObject, boolean defaulted) {
		LispObject lispObject = JavaObject.getInstance (javaObject, true);
		
		return this.put (key, javaObject, lispObject, defaulted);
	}
	public Pair<Object, LispObject> putLispObject (String key, LispObject lispObject, boolean defaulted) {
		Object javaObject = lispObject.javaInstance ();
		
		return this.put (key, javaObject, lispObject, defaulted);
	}
	public Pair<Object, LispObject> put(String key, Object javaObj, LispObject lispObj, boolean defaulted) {
		return map.put(key.toUpperCase(), new ParamData(javaObj,lispObj,defaulted));
	}
	public int size() {
		return map.size();
	}
	@Override
	public String toString() {
		return map.toString();
	}
	/**
	 * Returns whether a default value was applied to this key. If the key is missing, return false.
	 * @param key
	 * @return true if the key value is defaulted.
	 */
	public boolean isDefaulted(String key) {
		ParamData p = map.get(key.toUpperCase());
		if (p==null) return true;
		return p.defaulted;
	}
	
	public void remove(String key) {
		map.remove(key);
	}
	
}