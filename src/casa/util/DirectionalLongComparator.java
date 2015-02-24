package casa.util;

/**
   Simple implementation of java.util.Iterator that automatically throws
   an exception if remove is attempted.
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

import java.util.Comparator;

/**
 * @deprecated (rck) No known uses of this class in CASA.
 */
@Deprecated
public class DirectionalLongComparator implements Comparator {

    public static final int ASCENDING = 0;
    public static final int DESCENDING = 1;

    private int direction;

    public DirectionalLongComparator(){ direction = ASCENDING;}

    public DirectionalLongComparator(int aDirection){
	if((aDirection == ASCENDING) || (aDirection == DESCENDING)) direction = aDirection;
	else throw new IllegalArgumentException("LongComparator() - invalid direction specified");
    }

    public int getDirection(){ return direction;}

    public int compare(Object o1, Object o2){
	if((!(o1 instanceof Long)) || (!(o2 instanceof Long)))
	    throw new ClassCastException("LongComparator can only compare instances of Long");

	// we have two non-null Long values
	long longA, longB;
	if(direction == ASCENDING){ // natural order for long values
	    longA = ((Long)o1).longValue();
	    longB = ((Long)o2).longValue();
	} else { // we're descending order, so just switch values to compare
	    longB = ((Long)o1).longValue();
	    longA = ((Long)o2).longValue();
	}

	int retVal = 1;
	if(longA == longB) retVal = 0;
	else if(longA < longB) retVal = -1;

	return retVal;
    }

    public boolean equals(Object obj){
	return ((obj instanceof DirectionalLongComparator) &&
		(direction == ((DirectionalLongComparator)obj).getDirection()));
    }
}
