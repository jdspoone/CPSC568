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
package casa.exceptions;

import casa.agentCom.URLDescriptor;
import casa.util.Pair;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class AmbiguousURLException extends URLDescriptorException {
	URLDescriptor target;
	URLDescriptor url1;
	URLDescriptor url2;
	
	public AmbiguousURLException(URLDescriptor target, URLDescriptor url1, URLDescriptor url2) {
		super("Ambiguous URL match: "+target+" matches both "+url1+" and "+url2+" (and there may be others).");
		error = AMBIGUOUS_ERROR;
		this.target = target;
		this.url1 = url1;
		this.url2 = url2;
	}

	private static final long serialVersionUID = 1L;
	
	public URLDescriptor getTarget() { return target; }

	public Pair<URLDescriptor, URLDescriptor> getConflictingURLS() { return new Pair<URLDescriptor, URLDescriptor>(url1, url2); }
	
}
