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

package jade.semantics.kbase.filters.std.builtins;

import jade.semantics.kbase.KBase;
import jade.semantics.kbase.QueryResult;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.tools.SL;

import java.util.Set;
import java.util.Vector;


/**
 * @author Vincent Louis - France Telecom
 *
 */
public abstract class TernaryPredicate extends BuiltInPredicate {

	/**
	 * @param id
	 */
	public TernaryPredicate(String id) {
		super(id);
	}
	
	/***************************************************************************
	 * PUBLIC METHODS
	 **************************************************************************/

	// ASSERTING VALUES
	
	public Formula assertTrue(Term v1, Term v2, Term v3, KBase kb) {
//		if (!(v1 instanceof MetaTermReferenceNode)
//				&& !(v2 instanceof MetaTermReferenceNode)
//				&& !(v3 instanceof MetaTermReferenceNode)) {
		if (checkDomainV1V2V3(v1, v2, v3)) {
			return doAssertTrue(v1, v2, v3, kb);
		}
		return SL.TRUE;
	}
	
	public Formula assertFalse(Term v1, Term v2, Term v3, KBase kb) {
//		if (!(v1 instanceof MetaTermReferenceNode)
//				&& !(v2 instanceof MetaTermReferenceNode)
//				&& !(v3 instanceof MetaTermReferenceNode)) {
		if (checkDomainV1V2V3(v1, v2, v3)) {
			return doAssertFalse(v1, v2, v3, kb);
		}
		return SL.TRUE;
	}
	
	// RETRACTING VALUES
	
	public Formula retract(Term v1, Term v2, Term v3, KBase kb) {
		if (v1 instanceof MetaTermReferenceNode) {
			if (v2 instanceof MetaTermReferenceNode) {
				if (v3 instanceof MetaTermReferenceNode) {
					return doRetractV1V2V3Values(kb);
				}
				else if (checkDomainV3(v3)) {
					return doRetractV1V2Values(v3, kb);
				}
			}
			else {
				if (v3 instanceof MetaTermReferenceNode) {
					if (checkDomainV2(v2)) {
						return doRetractV1V3Values(v2, kb);
					}
				}
				else if (checkDomainV2V3(v2, v3)) {
					return doRetractV1Values(v2, v3, kb);
				}
			}
		}
		else {
			if (v2 instanceof MetaTermReferenceNode) {
				if (v3 instanceof MetaTermReferenceNode) {
					if (checkDomainV1(v1)) {
						return doRetractV2V3Values(v1, kb);
					}
				}
				else if (checkDomainV1V3(v1, v3)) {
					return doRetractV2Values(v1, v3, kb);
				}
			}
			else {
				if (v3 instanceof MetaTermReferenceNode) {
					if (checkDomainV1V2(v1, v2)) {
						return doRetractV3Values(v1, v2, kb);
					}
				}
				else if (checkDomainV1V2V3(v1, v2, v3)) {
					return doRetract(v1, v2, v3, kb);
				}
			}
		}
		return SL.TRUE;
	}
	
	// QUERYING VALUES

	public QueryResult query(Term v1, Term v2, Term v3, KBase kb) {
		if (v1 instanceof MetaTermReferenceNode) {
			if (v2 instanceof MetaTermReferenceNode) {
				if (v3 instanceof MetaTermReferenceNode) {
					return queryTupleValues(new MetaTermReferenceNode[] {
							(MetaTermReferenceNode)v1, (MetaTermReferenceNode)v2, (MetaTermReferenceNode)v3},
							doQueryV1V2V3Values(kb));
				}
				else if (checkDomainV3(v3)) {
					return queryTupleValues(new MetaTermReferenceNode[] {
							(MetaTermReferenceNode)v1, (MetaTermReferenceNode)v2},
							doQueryV1V2Values(v3, kb));
				}
			}
			else {
				if (v3 instanceof MetaTermReferenceNode) {
					if (checkDomainV2(v2)) {
						return queryTupleValues(new MetaTermReferenceNode[] {
								(MetaTermReferenceNode)v1, (MetaTermReferenceNode)v3},
								doQueryV1V3Values(v2, kb));
					}
				}
				else if (checkDomainV2V3(v2, v3)) {
					return querySingleValues((MetaTermReferenceNode)v1,
							doQueryV1Values(v2, v3, kb));
				}
			}
		}
		else {
			if (v2 instanceof MetaTermReferenceNode) {
				if (v3 instanceof MetaTermReferenceNode) {
					if (checkDomainV1(v1)) {
						return queryTupleValues(new MetaTermReferenceNode[] {
								(MetaTermReferenceNode)v2, (MetaTermReferenceNode)v3},
								doQueryV2V3Values(v1, kb));
					}
				}
				else if (checkDomainV1V3(v1, v3)) {
					return querySingleValues((MetaTermReferenceNode)v2,
							doQueryV2Values(v1, v3, kb));
				}
			}
			else {
				if (v3 instanceof MetaTermReferenceNode) {
					if (checkDomainV1V2(v1, v2)) {
						return querySingleValues((MetaTermReferenceNode)v3,
								doQueryV3Values(v1, v2, kb));
					}
				}
				else if (checkDomainV1V2V3(v1, v2, v3)
						&& doQuery(v1, v2, v3, kb)) {
					return QueryResult.KNOWN;
				}
			}
		}
		return QueryResult.UNKNOWN;
	}
	
	/***************************************************************************
	 * METHODS TO OVERRIDE
	 **************************************************************************/
	
	protected boolean checkDomainV1V2V3(Term i1, Term i2, Term i3) {
		return checkDomainV1(i1) && checkDomainV2(i2) && checkDomainV3(i3);
	}
	
	protected boolean checkDomainV1V2(Term i1, Term i2) {
		return checkDomainV1(i1) && checkDomainV2(i2);
	}

	protected boolean checkDomainV1V3(Term i1, Term i3) {
		return checkDomainV1(i1) && checkDomainV3(i3);
	}

	protected boolean checkDomainV2V3(Term i2, Term i3) {
		return checkDomainV2(i2) && checkDomainV3(i3);
	}

	protected boolean checkDomainV1(Term i1) {
		return false;
	}
	
	protected boolean checkDomainV2(Term i2) {
		return false;
	}
	
	protected boolean checkDomainV3(Term i3) {
		return false;
	}

	
	protected boolean doQuery(Term i1, Term i2, Term i3, KBase kb) {
		return false;
	}
	
	protected Set<Vector<Term>> doQueryV1V2V3Values(KBase kb) {
		return null;
	}

	protected Set<Vector<Term>> doQueryV1V2Values(Term i3, KBase kb) {
		return null;
	}
	
	protected Set<Vector<Term>> doQueryV1V3Values(Term i2, KBase kb) {
		return null;
	}

	protected Set<Vector<Term>> doQueryV2V3Values(Term i1, KBase kb) {
		return null;
	}

	protected Set<Term> doQueryV1Values(Term i2, Term i3, KBase kb) {
		return null;
	}

	protected Set<Term> doQueryV2Values(Term i1, Term i3, KBase kb) {
		return null;
	}

	protected Set<Term> doQueryV3Values(Term i1, Term i2, KBase kb) {
		return null;
	}

	

	protected Formula doAssertTrue(Term i1, Term i2, Term i3, KBase kb) {
		return SL.TRUE;
	}

	protected Formula doAssertFalse(Term i1, Term i2, Term i3, KBase kb) {
		return retract(i1, i2, i3, kb);
	}

	protected Formula doRetract(Term i1, Term i2, Term i3, KBase kb) {
		return SL.TRUE;
	}
	
	protected Formula doRetractV1Values(Term i2, Term i3, KBase kb) {
		return SL.TRUE;
	}

	protected Formula doRetractV2Values(Term i1, Term i3, KBase kb) {
		return SL.TRUE;
	}

	protected Formula doRetractV3Values(Term i1, Term i2, KBase kb) {
		return SL.TRUE;
	}

	protected Formula doRetractV1V2Values(Term i3, KBase kb) {
		return SL.TRUE;
	}

	protected Formula doRetractV1V3Values(Term i2, KBase kb) {
		return SL.TRUE;
	}

	protected Formula doRetractV2V3Values(Term i1, KBase kb) {
		return SL.TRUE;
	}

	protected Formula doRetractV1V2V3Values(KBase kb) {
		return SL.TRUE;
	}
}
