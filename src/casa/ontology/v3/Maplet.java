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
package casa.ontology.v3;

import casa.exceptions.IllegalOperationException;
import casa.util.Pair;
import casa.util.PairComparable;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class Maplet extends Pair<BaseType, BaseType> {
	public Maplet(BaseType newFirst, BaseType newSecond) {
		super(newFirst, newSecond);
	}
	
	public int compare(ConcreteRelation relation, Maplet other) throws IllegalOperationException {
		//check domains
		int domComp=0;
		boolean forward = relation.relatedTo(this.getFirst(), other.getFirst());
		boolean backward = relation.relatedTo(other.getFirst(), this.getFirst());
		if (forward && !backward) domComp = -1;
		if (backward && !forward) domComp = 1;

		//check ranges
		int ranComp=0;
		forward = relation.relatedTo(this.getSecond(), other.getSecond());
		backward = relation.relatedTo(other.getSecond(), this.getSecond());
		if (forward && !backward) ranComp = -1;
		if (backward && !forward) ranComp = 1;

		//combine the results
		if (domComp==0) {
			return ranComp;
		}
		else if (domComp<0) {
			if (ranComp <= 0) return domComp;
			throw new IllegalOperationException("Maplet.isMoreSpecificThan: domain is less than range, and range is less than domain");
		}
		else { // (domComp>0)
			if (ranComp >= 0) return domComp;
			throw new IllegalOperationException("Maplet.isMoreSpecificThan: domain is greater than range, and range is greater than domain");
		}
	}
	
	public Maplet getInverted() {
		return new Maplet(getSecond(), getFirst());
	}
	
	public String toString() {
		return "("+getFirst().getName()+" "+getSecond().getName()+")";
	}
}
