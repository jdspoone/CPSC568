/* <p>Title: CASA Agent Infrastructure</p>
 * <p>Copyright: Copyright (c) 2003-2014, Knowledge Science Group, University of Calgary. 
 */
package casa.util;

import casa.MLMessage;
import casa.agentCom.SocketServerTCPIP;

import java.util.Vector;

/**
 *
 * <p><b>Title:</b> CASA Agent Infrastructure</p>
 * <p><b>Copyright: Copyright (c) 2003-2014, Knowledge Science Group, University of Calgary.</b> 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p><b>Company:</b> Knowledge Science Group, Department of Computer Science, University of Calgary</p>
 * @version 0.9
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
/**
 * Utility class to remember a few recent objects.  Used by {@link SocketServerTCPIP#queueMessage(MLMessage)}
 * to detect circular message sends.
 * @param <T>
 */
public class LimitedSet<T> {
	Vector<T> v;
	int size;
	int index = -1;
	public LimitedSet(int size) {
		v = new Vector<T>(size);
		this.size = size;
		for (int i=0; i<size; i++) 
			v.add(null);
	}
	public boolean contains(T obj) {
		return v.contains(obj);
	}
	public T push(T obj) {
		return v.set(index=(++index % size), obj);
	}
}