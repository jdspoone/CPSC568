/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
JSA - JADE Semantics Add-on is a framework to develop cognitive
agents in compliance with the FIPA-ACL formal specifications.

Copyright 2003-2014, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

/*
* created on 22 ao�t 07 by Thierry Martinez
*/

package jade.semantics.kbase;

import jade.semantics.lang.sl.tools.MatchResult;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

/**
 * The QueryResult class reifies the result of a query done 
 * onto the {@link KBase} object of an agent. Such a result can be :
 *  - <code>null</code> if the queried fact is not believed by the agent,
 *  - <br>an empty QueryResult if the queried fact is believed by
 *    the agent and includes no meta-reference,</br>
 *  - <br>a QueryResult holding MatchResult if the queried fact is believed by
 *    the agent and includes meta-reference.</br> 
 * 
 * @author Thierry Martinez - France Telecom
 *
 */
public class QueryResult {
	
	public static final QueryResult UNKNOWN = null;
	public static final QueryResult KNOWN = new QueryResult();
	
	public static class BoolWrapper {
		private boolean bool;

		public BoolWrapper(boolean bool) {
			setBool(bool);
		}
		public void setBool(boolean bool) {
			this.bool = bool;
		}
		public boolean getBool() {
			return this.bool;
		}
	}

	public static ArrayList addReasons(ArrayList reasons, ArrayList list) {
		if (list != null) {
			for (int i=0; i<list.size(); i++) {
				reasons.add(list.get(i));
			}
		}
		return reasons;
	}
	
	private ArrayList results = new ArrayList();
	
	/**
	 * Constructor.
	 */
	public QueryResult() {
	}
	
	/**
	 * Constructor.
	 */
	public QueryResult(MatchResult result) {
		add(result);
	}
	
	/**
	 * Constructor.
	 */
	public QueryResult(ArrayList results) {
		addAll(results);
	}
	
	/**
	 * Constructor.
	 */
	public QueryResult(QueryResult queryResult) {
		addAll(queryResult.getResults());
	}

	/**
	 * @return An ArrayList of MatchResult
	 */
	public ArrayList getResults() {
		return results;
	}
	
	/**
	 * @return The index MatchResult
	 */
	public MatchResult getResult(int index) {
		return (MatchResult)results.get(index);
	}

	/**
	 * This method add a given result if not already in the results list.
	 * @param result The MatchResult to add.
	 */
	public void add(MatchResult result) {
		if ( !results.contains(result) ) {
			results.add(result);
		}
	}
	
	/**
	 * This method add all the results if not already in the results list.
	 * @param results The MatchResults to add.
	 */
	public void addAll(ArrayList results) {
		if ( results != null ) {
			for (Iterator it = results.iterator(); it.hasNext();) {
				add((MatchResult)it.next());
			}
		}
	}
	
	/**
	 * This method remove a given result from the results list.
	 * @param result The MatchResult to remove.
	 */
	public void remove(MatchResult result) {
		results.remove(result);
	}
	
	public MatchResult remove(int i) {
		return (MatchResult)results.remove(i);
	}
	
	/**
	 * @return the size of this result
	 */
	public int size() {
		return results.size();
	}
	
	/**
	 * @return true if it is an empt result (equivalent to size() == 0)
	 */
	public boolean isEmpty() {
		return results.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object qr) {
		boolean equals = qr != null && qr instanceof QueryResult && ((QueryResult)qr).size() == size();
		if ( equals ) {
			for (int i=0; i< size() && equals; i++) {
				equals = equals && ((QueryResult)qr).results.get(i).equals(results.get(i));
			}
		}
		return equals;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if ( isEmpty() ) {
			return "KNOWN";
		}
		//else {
			String result = "";
			for (Iterator it = results.iterator(); it.hasNext();) {
				if ( result != "" ) result += ", ";
				result += it.next().toString();
			}
			return result;
		//}
	}
	
	/**
	 * Returns the union between this list of match results and the other one given as an argument
	 * @param other
	 * @return the union between this list of match results and the other one given as an argument
	 */
	public QueryResult union(QueryResult other)
	{
		QueryResult result = new QueryResult(this);
        if (other != null) {
        	result.addAll(other.getResults());
    	}
        result.removeSubsumedMatchResult();
        return result;
	}
	
	// ===============================================
	// Package private implementation
	// ===============================================
	void removeSubsumedMatchResult()
	{
		for (int i=0; i<size(); i++) {
			for (int j=i+1; j<size(); j++) {
				MatchResult inters = getResult(i).intersect(getResult(j));
				if ( inters != null && inters.size() != 0 ) {
					// valid intersection exists
					if ( inters.equals(getResult(i)) ) {
						remove(getResult(j--));
					}
					else if ( inters.equals(getResult(j))) {
						remove(getResult(i--));
						break;
					}
				}
			}
		}
	}


}

