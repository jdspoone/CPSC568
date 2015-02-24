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
package casa;

import casa.interfaces.Describable;

import java.util.Set;
import java.util.TreeMap;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class AbstractDescribable implements Describable {
  protected TreeMap<String,String> params = new TreeMap<String,String>();
	@Override
	public String getParameter(String key) {
		return params.get(key);
	}

	@Override
	public Set<String> keySet() {
		return params.keySet();
	}

	@Override
	public void setParameter(String parameter, String value) {
		params.put(parameter, value);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public AbstractDescribable clone() {
		try {
			AbstractDescribable ret = (AbstractDescribable)super.clone();
			ret.params = (TreeMap<String,String>)params.clone();
			return ret;
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}
}
