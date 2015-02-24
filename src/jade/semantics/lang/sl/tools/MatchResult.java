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

package jade.semantics.lang.sl.tools;

import java.util.Iterator;

import jade.semantics.lang.sl.grammar.ByteConstantNode;
import jade.semantics.lang.sl.grammar.ContentExpression;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.ListOfNodes;
import jade.semantics.lang.sl.grammar.MetaContentExpressionReferenceNode;
import jade.semantics.lang.sl.grammar.MetaFormulaReferenceNode;
import jade.semantics.lang.sl.grammar.MetaSymbolReferenceNode;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.MetaVariableReferenceNode;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.Symbol;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.Variable;
import jade.semantics.lang.sl.tools.SL.WrongTypeException;

/**
 * This class holds the result of a matching operation.
 */
public class MatchResult extends ListOfNodes {
	
	
	/**
	 * Return the formula assigned to the named meta variable
	 * 
	 * @param name
	 *            the name of the meta variable to get its value
	 * @return the formula which is the value of the meta variable named
	 *         with the given <b><code>name</code></b>, or <b><code>null</code></b>
	 *         if no formula is assigned to this meta variable, meaning the
	 *         last matching operation has failed, or no matching operation
	 *         has been performed on this pattern.
	 * @exception WrongTypeException
	 *                Thrown if the value of the variable is not a formula
	 */
	public Formula getFormula(String name) throws WrongTypeException {
		try {
			return (Formula)getMetaReference(name);
		}
		catch (ClassCastException e) {
			throw new WrongTypeException();
		}
	}

	/**
	 * Return the formula assigned to the named meta variable
	 * 
	 * @param name
	 *            the name of the meta variable to get its value
	 * @return the formula which is the value of the meta variable named
	 *         with the given <b><code>name</code></b>, or <b><code>null</code></b>
	 *         if no formula is assigned to this meta variable, meaning the
	 *         last matching operation has failed, or no matching operation
	 *         has been performed on this pattern.
	 */
	public Formula formula(String name) {
		try {
			return getFormula(name);
		}
		catch(WrongTypeException wte) {wte.printStackTrace();}
		return null;
	}
	
	/**
	 * Return the term assigned to the named meta variable.
	 * 
	 * @param name
	 *            the name of the meta variable to get its value
	 * @return the term which is the value of the meta variable named with
	 *         the given <b><code>name</code></b>, or <b><code>null</code></b>
	 *         if no term is assigned to this meta variable, meaning the
	 *         last matching operation has failed, or no matching operation
	 *         has been performed on this pattern.
	 * @exception WrongTypeException
	 *                Thrown if the value of the variable is not a term.
	 */
	public Term getTerm(String name) throws WrongTypeException {
		try {
			return (Term)getMetaReference(name);
		}
		catch (ClassCastException e) {
			throw new WrongTypeException();
		}
	}

	/**
	 * Return the term assigned to the named meta variable.
	 * 
	 * @param name
	 *            the name of the meta variable to get its value
	 * @return the term which is the value of the meta variable named with
	 *         the given <b><code>name</code></b>, or <b><code>null</code></b>
	 *         if no term is assigned to this meta variable, meaning the
	 *         last matching operation has failed, or no matching operation
	 *         has been performed on this pattern.
	 */
	public Term term(String name) {
		try {
			return getTerm(name);
		}
		catch(WrongTypeException wte) {wte.printStackTrace();}
		return null;
	}
	
	/**
	 * Return the variable assigned to the named meta variable.
	 * 
	 * @param name
	 *            the name of the meta variable to get its value
	 * @return the variable which is the value of the meta variable named
	 *         with the given <b><code>name</code></b>, or <b><code>null</code></b>
	 *         if no variable is assigned to this meta variable, meaning the
	 *         last matching operation has failed, or no matching operation
	 *         has been performed on this pattern.
	 * @exception WrongTypeException
	 *                Thrown if the value of the variable is not a variable.
	 */
	public Variable getVariable(String name) throws WrongTypeException {
		try {
			return (Variable)getMetaReference(name);
		}
		catch (ClassCastException e) {
			throw new WrongTypeException();
		}
	}

	/**
	 * Return the variable assigned to the named meta variable.
	 * 
	 * @param name
	 *            the name of the meta variable to get its value
	 * @return the variable which is the value of the meta variable named
	 *         with the given <b><code>name</code></b>, or <b><code>null</code></b>
	 *         if no variable is assigned to this meta variable, meaning the
	 *         last matching operation has failed, or no matching operation
	 *         has been performed on this pattern.
	 */
	public Variable variable(String name) {
		try {
			return getVariable(name);
		}
		catch(WrongTypeException wte) {wte.printStackTrace();}
		return null;
	}
	
	/**
	 * Return the symbol assigned to the named meta variable.
	 * 
	 * @param name
	 *            the name of the meta variable to get its value
	 * @return the symbol which is the value of the meta variable named with
	 *         the given <b><code>name</code></b>, or <b><code>null</code></b>
	 *         if no symbol is assigned to this meta variable, meaning the
	 *         last matching operation has failed, or no matching operation
	 *         has been performed on this pattern.
	 * @exception WrongTypeException
	 *                Thrown if the value of the variable is not a symbol.
	 */
	public Symbol getSymbol(String name) throws WrongTypeException {
		try {
			return (Symbol)getMetaReference(name);
		}
		catch (ClassCastException e) {
			throw new WrongTypeException();
		}
	}

	/**
	 * Return the symbol assigned to the named meta variable.
	 * 
	 * @param name
	 *            the name of the meta variable to get its value
	 * @return the symbol which is the value of the meta variable named with
	 *         the given <b><code>name</code></b>, or <b><code>null</code></b>
	 *         if no symbol is assigned to this meta variable, meaning the
	 *         last matching operation has failed, or no matching operation
	 *         has been performed on this pattern.
	 */
	public Symbol symbol(String name) {
		try {
			return getSymbol(name);
		}
		catch(WrongTypeException wte) {wte.printStackTrace();}
		return null;
	}
	
	/**
	 * Return the content expression assigned to the named meta variable.
	 * 
	 * @param name
	 *            the name of the meta variable to get its value
	 * @return the content expression which is the value of the meta
	 *         variable named with the given <b><code>name</code></b>,
	 *         or <b><code>null</code></b> if no content expression is
	 *         assigned to this meta variable, meaning the last matching
	 *         operation has failed, or no matching operation has been
	 *         performed on this pattern.
	 * @exception WrongTypeException
	 *                Thrown if the value of the variable is not a content
	 *                expression.
	 */
	public ContentExpression getContentExpression(String name) throws WrongTypeException {
		try {
			return (ContentExpression)getMetaReference(name);
		}
		catch (ClassCastException e) {
			throw new WrongTypeException();
		}
	}

	/**
	 * Return the content expression assigned to the named meta variable.
	 * 
	 * @param name
	 *            the name of the meta variable to get its value
	 * @return the content expression which is the value of the meta
	 *         variable named with the given <b><code>name</code></b>,
	 *         or <b><code>null</code></b> if no content expression is
	 *         assigned to this meta variable, meaning the last matching
	 *         operation has failed, or no matching operation has been
	 *         performed on this pattern.
	 */
	public ContentExpression contentExpression(String name) {
		try {
			return getContentExpression(name);
		}
		catch(WrongTypeException wte) {wte.printStackTrace();}
		return null;
	}
	
	/**
	 * This method return a display of the matching result.
	 */
	@Override
	public String toString() {
		String result = "[";
		for (int i = 0; i < size(); i++) {
			Node var = get(i);
			result += (i == 0 ? "??"+SL.getMetaReferenceName(var) + " = " 
						      : ", ??"+SL.getMetaReferenceName(var) + " = ");
			Node value = SL.getMetaReferenceValue(var);
			if (value instanceof ByteConstantNode) {
				result += "ByteConstantNode(" + ((ByteConstantNode)value).lx_value().length + ")";
			}
			else if (value == null) {
				result += "*not bound*";
			}
			else {
				result += value.toString();
			}
		}
		return result+"]";
	}
	
	/**
	 * 
	 * @param object The match result object to compare with
	 * @return true is this match result object equals the one given as an argument. "Equal" means that
	 * the 2 objects holds the same meta variables (name and value) regardless their rank in the list.  
	 */
	@Override
	public boolean equals(Object object)
	{
		boolean isEqual = object instanceof MatchResult && ((MatchResult)object).size() == size();
		if ( isEqual && size() != 0 ) {
			//ListOfNodes l1 = new ListOfNodes(children());
			ListOfNodes l2 = new ListOfNodes(((MatchResult)object).children());
			
			for (Iterator ite = iterator(); ite.hasNext() ;) {
				Node n1 = (Node)ite.next();
				isEqual=false;
				for (Iterator ite2 = l2.iterator(); ite2.hasNext(); ) {
					Node n2 = (Node)ite2.next();
					if (SL.getMetaReferenceName(n1).equals(SL.getMetaReferenceName(n2))) {
						if (SL.getMetaReferenceValue(n1).equals(SL.getMetaReferenceValue(n2))) {
							l2.remove(n2);
							isEqual = true;
							break;
						}
						else {
							return false;
						}
					}
				}
				if (!isEqual) return false;
			}
			return l2.isEmpty();
//			do {
//				Node n1 = l1.getFirst();
//				ListOfNodes lo = new ListOfNodes();
//				// FIXME assume MetaTermReferenceNode.lx_name_ID equals any XXX.lx_name_ID
//				isEqual = l2.find(n1.getClass(), MetaTermReferenceNode.lx_name_ID, SL.getMetaReferenceName(n1), lo, false);
//				if ( isEqual ){
//					Node n2 = lo.getFirst();
//					isEqual = SL.getMetaReferenceValue(n1).equals(SL.getMetaReferenceValue(n2));
//					l1.remove(n1);
//					l2.remove(n2);
//				}
//			} while (isEqual && l1.size() > 0);
		}
		return isEqual;
	}
	
	/**
	 * 
	 * @param other the other match result to compute the intersection between
	 * @return the intersection between this match result and the one given as an argument or null if
	 *         the 2 match results are incompatible, i.e., they hold the same variable with different 
	 *         values.
	 */
	public MatchResult intersect(MatchResult other)
	{
		MatchResult result = new MatchResult();
		for (int i = 0; i < size(); i++) {
			Node m = get(i);
			ListOfNodes lo = new ListOfNodes();
			// FIXME assume MetaTermReferenceNode.lx_name_ID equals any XXX.lx_name_ID
			if ( other.find(m.getClass(), MetaTermReferenceNode.lx_name_ID, SL.getMetaReferenceName(m), lo, false) ) {
				if ( SL.getMetaReferenceValue(m).equals(SL.getMetaReferenceValue(lo.getFirst())) ) {
					result.add(m.getClone());
				}
				else {
					// the 2 match results are incompatible
					return null;
				}
			}
		}
		return result;
	}

	/**
	 * 
	 * @param other the other match result to join with
	 * @return the union between this match result and the one given as an argument or null if
	 *         the 2 match results are incompatible, i.e., they hold the same variable with different 
	 *         values.
	 */
	public MatchResult join(MatchResult other)
	{
		MatchResult result = intersect(other);
		if ( result != null ) {
			for (int i = 0; i < size(); i++) {
				Node m = get(i);
				if ( !result.contains(m) ) {
					result.add(m.getClone());
				}
			}
			for (int i = 0; i < other.size(); i++) {
				Node m = other.get(i);
				if ( !result.contains(m) ) {
					result.add(m.getClone());
				}
			}
		}
		return result;
	}

    /* (non-Javadoc)
     * @see jade.semantics.lang.sl.grammar.ListOfNodes#getClone()
     */
    @Override
	public Node getClone()
    {
        Node clone = new MatchResult();
        clone.copyValueOf(this);
        return clone;
    }	
    
    /* (non-Javadoc)
     * @see jade.semantics.lang.sl.grammar.ListOfNodes#copyValueOf(jade.semantics.lang.sl.grammar.Node)
     */
    @Override
	public void copyValueOf(Node n) {
        if (n instanceof MatchResult) {
        	super.copyValueOf(n);
			for (int i = 0; i < size(); i++) {
				Node meta = get(i);
				Node value = SL.getMetaReferenceValue(meta);
				if (value instanceof MetaContentExpressionReferenceNode ||
					value instanceof MetaFormulaReferenceNode || 
					value instanceof MetaSymbolReferenceNode || 
					value instanceof MetaTermReferenceNode ||
					value instanceof MetaVariableReferenceNode) {
					// The value of m is also a metareference
					String valuename = SL.getMetaReferenceName(value);
					ListOfNodes othermeta = new ListOfNodes();
					// FIXME assume MetaTermReferenceNode.lx_name_ID equals any XXX.lx_name_ID
					if ( find(SL.META_REFERENCE_CLASSES, MetaTermReferenceNode.lx_name_ID, valuename, othermeta, false) ) {
						try {
							SL.setMetaReferenceValue(meta, othermeta.get(0));
						}
						catch(Exception e) {e.printStackTrace();}
					}
				}
			}
        }
    }

    /**
     * Assigns the value of all occurrences of the meta-reference meta within exp
     * with the value of meta 
     */
    public void completeClosure() throws WrongTypeException {
        ListOfNodes metaReferences = new ListOfNodes();
                
        for (int i=0; i<size(); i++) {
        	Node value = SL.getMetaReferenceValue(get(i));
        	if (value!=null) {
        		value.childrenOfKind(SL.META_REFERENCE_CLASSES, metaReferences);
        	}
        }
        for (int i=0; i<metaReferences.size(); i++) {
        	if (!contains(metaReferences.get(i))) {
        		add(metaReferences.get(i));
        	}
        }
        for (int i=0; i<metaReferences.size(); i++) {
        	Node metaToAssign = metaReferences.get(i);
        	if (SL.getMetaReferenceValue(metaToAssign) == null) {
        		ListOfNodes result = new ListOfNodes();
    			// FIXME assume MetaTermReferenceNode.lx_name_ID equals any XXX.lx_name_ID
        		find(SL.META_REFERENCE_CLASSES, MetaTermReferenceNode.lx_name_ID, SL.getMetaReferenceName(metaToAssign), result, false);
				SL.setMetaReferenceValue(metaToAssign, SL.getMetaReferenceValue(result.get(0)));
        	}
        }
    }

	 
    /**
     * This method returns true if a MetaVariable has the given varName in the 
     * given MatchResult and if it succeeds in giving it the given value.  
     * @param varName the name of a metavariable
     * @param value the value of the metavariable
     * @return true if a MetaVariable has the given varName in the 
     * given MatchResult and if it succeeds in giving it the given value, false
     * if not.
     */
	public boolean set(String varName, Node value) {
        try {
			for (int i = 0; i < size(); i++) {
				Node meta = get(i);
				if ( SL.getMetaReferenceName(meta).equals(varName)) {
					// The meta reference to be assigned has been found.
					Node metaValue = SL.getMetaReferenceValue(meta);
					if ( metaValue == null ) {
						SL.setMetaReferenceValue(meta, value);
						completeClosure();
						return true;
					}
					else {
						// Assuming the meta variable already has a value
						MatchResult matchValue = SL.match(metaValue, value);
						if ( matchValue == null ) {
							// The two values are not compatible
							return false;
						}
						else {
							for (int j=0; j<matchValue.size(); j++) {
								boolean toAdd = true;
								for (int k=0; k<size(); k++) {
									if (get(k).equals(matchValue.get(j))) {
										SL.setMetaReferenceValue(get(k), SL.getMetaReferenceValue(matchValue.get(j)));
										toAdd = false;
										break;
									}
								}
								if (toAdd) {
									add(matchValue.get(j));
								}
							}
							completeClosure();
							return true;
						}
					}
                }
            }
        } catch (SL.WrongTypeException wte) {
            wte.printStackTrace();
        }
        return false;
    }
	
	/**
	 * Restore a previous size of the MatchResult by removing the last elements until the size
	 * equals the one given as an argument.
	 * @param size
	 * @return the removed elements
	 */
	protected void restore(int size) {
		int nbtoremove = (size()-size);
		for (int i = 0; i < nbtoremove; i++) {
			Node mr = getLast();
			mr.setAttribute(MetaVariableReferenceNode.sm_value_ID, null);
			remove(mr);
		}
	}

	// ===============================================
	// Package private implementation
	// ===============================================
	String nameOf(String name) {
		return name.startsWith("??") ?
				name.substring(2) :
					name;
	}
	
	private Node getMetaReference(String name) {
		for (Iterator it = iterator(); it.hasNext(); ) {
			Node n = (Node)it.next();
			if ( n.getAttribute(Variable.lx_name_ID).equals(nameOf(name)) ) {
				return (Node)n.getAttribute(MetaVariableReferenceNode.sm_value_ID);
			}
		}
		return null;
	}
	
//	void instantiate() {
//		for (int i = 0; i < size(); i++) {
//			replace(i, get(i).getClone());
//			if (SL.getMetaReferenceValue(get(i)) != null) {
//				try {
//					SL.setMetaReferenceValue(get(i), SL.instantiate(SL.getMetaReferenceValue(get(i))));
//				} catch (WrongTypeException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}

}