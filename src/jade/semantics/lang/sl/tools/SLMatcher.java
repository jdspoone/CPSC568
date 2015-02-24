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

import jade.semantics.lang.sl.grammar.ActionContentExpressionNode;
import jade.semantics.lang.sl.grammar.ActionExpressionNode;
import jade.semantics.lang.sl.grammar.AllNode;
import jade.semantics.lang.sl.grammar.AlternativeActionExpressionNode;
import jade.semantics.lang.sl.grammar.AndNode;
import jade.semantics.lang.sl.grammar.AnyNode;
import jade.semantics.lang.sl.grammar.BelieveNode;
import jade.semantics.lang.sl.grammar.ByteConstantNode;
import jade.semantics.lang.sl.grammar.ContentExpression;
import jade.semantics.lang.sl.grammar.ContentNode;
import jade.semantics.lang.sl.grammar.CountAsNode;
import jade.semantics.lang.sl.grammar.DateTimeConstantNode;
import jade.semantics.lang.sl.grammar.DoneNode;
import jade.semantics.lang.sl.grammar.EqualsNode;
import jade.semantics.lang.sl.grammar.EquivNode;
import jade.semantics.lang.sl.grammar.ExistsNode;
import jade.semantics.lang.sl.grammar.FactNode;
import jade.semantics.lang.sl.grammar.FalseNode;
import jade.semantics.lang.sl.grammar.FeasibleNode;
import jade.semantics.lang.sl.grammar.ForallNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.FormulaContentExpressionNode;
import jade.semantics.lang.sl.grammar.FunctionalTermNode;
import jade.semantics.lang.sl.grammar.FunctionalTermParamNode;
import jade.semantics.lang.sl.grammar.IdentifyingContentExpressionNode;
import jade.semantics.lang.sl.grammar.ImpliesNode;
import jade.semantics.lang.sl.grammar.InstitutionalFactNode;
import jade.semantics.lang.sl.grammar.IntegerConstantNode;
import jade.semantics.lang.sl.grammar.IntentionNode;
import jade.semantics.lang.sl.grammar.IotaNode;
import jade.semantics.lang.sl.grammar.ListOfContentExpression;
import jade.semantics.lang.sl.grammar.ListOfNodes;
import jade.semantics.lang.sl.grammar.ListOfParameter;
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.MetaContentExpressionReferenceNode;
import jade.semantics.lang.sl.grammar.MetaFormulaReferenceNode;
import jade.semantics.lang.sl.grammar.MetaSymbolReferenceNode;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.MetaVariableReferenceNode;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.NotNode;
import jade.semantics.lang.sl.grammar.ObligationNode;
import jade.semantics.lang.sl.grammar.OrNode;
import jade.semantics.lang.sl.grammar.Parameter;
import jade.semantics.lang.sl.grammar.ParameterNode;
import jade.semantics.lang.sl.grammar.PersistentGoalNode;
import jade.semantics.lang.sl.grammar.PredicateNode;
import jade.semantics.lang.sl.grammar.PropositionSymbolNode;
import jade.semantics.lang.sl.grammar.RealConstantNode;
import jade.semantics.lang.sl.grammar.RelativeTimeConstantNode;
import jade.semantics.lang.sl.grammar.ResultNode;
import jade.semantics.lang.sl.grammar.SequenceActionExpressionNode;
import jade.semantics.lang.sl.grammar.SomeNode;
import jade.semantics.lang.sl.grammar.StringConstantNode;
import jade.semantics.lang.sl.grammar.Symbol;
import jade.semantics.lang.sl.grammar.SymbolNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TermSequenceNode;
import jade.semantics.lang.sl.grammar.TermSetNode;
import jade.semantics.lang.sl.grammar.TrueNode;
import jade.semantics.lang.sl.grammar.UncertaintyNode;
import jade.semantics.lang.sl.grammar.Variable;
import jade.semantics.lang.sl.grammar.VariableNode;
import jade.semantics.lang.sl.grammar.VisitorBase;
import jade.semantics.lang.sl.grammar.WordConstantNode;
import jade.util.leap.ArrayList;

import java.util.Comparator;

/**
 * This class implement a SL pattern matcher.
 */
public class SLMatcher extends VisitorBase {
	
	//============================================================
	//                      PUBLIC METHOD
	//============================================================
    /**
     * Try to match to SL expressions
     * @param expression1 a SL expression
     * @param expression2 another SL expression
     * @return null if the 2 expressions doesn't match, or a MatchResult otherwise
     */
    public MatchResult match(Node expression1, Node expression2) {
    	Node expr1 = expression1.getClone();
    	Node expr2 = expression2.getClone();
     	if (match(expr1, expr2, 
     			  new MatchResult(), new MatchResult(),
     			  new VariableMappings(),
        		  new ListOfNodes(), new ListOfNodes(), new ListOfNodes(), new ListOfNodes())) {
            return _metaReferences1;
     	} 
     	//else {
            return null;
        //}
    }

	//============================================================
	//                      PRIVATE CLASS
	//============================================================
    static class VariableMappings {
    	
    	ArrayList _variables = new ArrayList();
    	
    	static class VariableMapping {
    		Variable source;
    		Variable destination;
    		public VariableMapping(Variable source, Variable destination) {
    			this.source = source;
    			this.destination = destination;
			}
    	}
    	
    	// Return TRUE if adding this variable mapping produces no conflict
    	boolean addMapping(Variable source, Variable destination) {
    		for(int i=0; i<_variables.size(); i++) {
    			VariableMapping vm = (VariableMapping)_variables.get(i);
    			if (vm.source == source ) {
    				return (vm.destination == destination);
    			}
    		}
    		// The variable has not been found.
    		_variables.add(new VariableMapping(source, destination));
    		return true;
    	}
    	
    	int size() {
    		return _variables.size();
    	}
    	
    	protected void restore(int size) {
    		int nbtoremove = (_variables.size()-size);
    		for (int i = 0; i < nbtoremove; i++) {
    			_variables.remove(_variables.get(_variables.size()-1));
    		}
    	}
    	
    }
    
	//============================================================
	//                      PRIVATE FIELDS
	//============================================================
    Node _expression1;        
    Node _expression2;
        
    boolean _match;
        
    MatchResult _metaReferences1;
    MatchResult _metaReferences2;
        
    VariableMappings _variables;
    
    ListOfNodes _firstAndResidue;        
    ListOfNodes _secondAndResidue;        
    ListOfNodes _firstOrResidue;
    ListOfNodes _secondOrResidue;
    
	//============================================================
	//                      PRIVATE METHODS
	//============================================================
    
    /**
     * This method is used inside the recursive matching
     * process.
     * @param expression1
     * @param expression2
     * @param metaReferences1
     * @param firstList
     * @param secondList
     * @param firstOrResidue
     * @param secondOrResidue
     * @return
     */
    private boolean match(Node expression1, 
    		              Node expression2,
    		              MatchResult metaReferences1, 
    		              MatchResult metaReferences2, 
    		              VariableMappings variables,
    		              ListOfNodes firstList, 
    		              ListOfNodes secondList,
    		              ListOfNodes firstOrResidue, 
    		              ListOfNodes secondOrResidue)
    {
    	_match = false;
    	_metaReferences1 = metaReferences1; 
    	_metaReferences2 = metaReferences2; 
    	_variables = variables;
    	_expression1 = expression1;
    	_expression2 = expression2;
    	_firstAndResidue = firstList;
    	_secondAndResidue = secondList;
    	_firstOrResidue = firstOrResidue;
    	_secondOrResidue = secondOrResidue;
    	
    	// Keep the old metareference size to restore it if !_match
    	int size1 = _metaReferences1.size();
    	int size2 = _metaReferences2.size();
    	int vsize = _variables.size();
    	
    	_expression1.accept(this);
    	
    	if ( !_match ) {
    		_metaReferences1.restore(size1);
    		_metaReferences2.restore(size2);
    		_variables.restore(vsize);
    	}
    	
    	return _match;
    }
        
   /**
     * Returns true if the two lists given in parameter matches, false if 
     * not.
     * @param ref the reference list
     * @param other the other list
     * @param type 0 for an AndNode, 1 for an OrNode
     */
    private boolean analyze(ListOfNodes ref, ListOfNodes other, int type, boolean refIsExpression1) {
    	for (int i = 0; i < ref.size(); i++) {
    		if (ref.get(i) instanceof MetaFormulaReferenceNode) {
    			if (i == (ref.size() -1)) {
    				if (type == 0) {
    					doPatternMatchingOnMetaReference(ref.get(i), createEndAndNode(other), refIsExpression1);
    				} else {
    					doPatternMatchingOnMetaReference(ref.get(i), createEndOrNode(other), refIsExpression1);
    				}
    				return _match;
    			} 
    			//else {
    				for (int j = 0; j < other.size(); j++) {
    					doPatternMatchingOnMetaReference(ref.get(i), other.get(j), refIsExpression1);
    					if (_match) {
    						other.remove(other.get(j));
    						break;
    					}
    				}
    				if (!_match) return false;
    			//}
    		} else {
    			boolean find = false;
    			for (int j = 0; j < other.size(); j++) {
     				find = new SLMatcher().match(ref.get(i),other.get(j),
     											 _metaReferences1, _metaReferences2,
     											 _variables,
     											 new ListOfNodes(),new ListOfNodes(),new ListOfNodes(),new ListOfNodes());
//     					|| new SLMatcher().match(other.get(j),ref.get(i), 
//     							                 _metaReferences1, _metaReferences2,
//     							                 _variables,
//     							                 new ListOfNodes(),new ListOfNodes(),new ListOfNodes(),new ListOfNodes());
     				
     				if ( find ) {
    					other.remove(other.get(j));
    					break;
    				}
    			}
    			if (!find) {
    				if (type == 0) {
    					_secondAndResidue.removeAll();
    					_firstAndResidue.removeAll();
    				} else {
    					_secondOrResidue.removeAll();
    					_firstOrResidue.removeAll();
    				}
    				return false;
    			}
    		}
    	}
    	return (other.size() == 0);
    }
        
    /**
     * Creates an And Formula with the nodes of the list. If the list contains
     * only one node, this one is returned. The list given in parameter is
     * cleared. 
     * @param l a list of nodes
     * @return a formula that is an AndNode formula if the list size if at
     * least 2, or the node in the list if the size equals 1, or null if the
     * list is empty. 
     */
    private Formula createEndAndNode(ListOfNodes l) {
    	Formula solution = buildAndNode(l);
    	if (solution != null && solution instanceof AndNode) {
    		solution = sort(solution);
    	} 
    	l.removeAll();
    	return solution;
    }
        
    /**
     * Creates an Or Formula with the nodes of the list. If the list contains
     * only one node, this one is returned. The list given in parameter is
     * cleared. 
     * @param l a list of nodes
     * @return a formula that is an OrNode formula if the list size if at
     * least 2, or the node in the list if the size equals 1, or null if the
     * list is empty. 
     */
    private Formula createEndOrNode(ListOfNodes l) {
    	Formula solution = buildOrNode(l);
    	if (solution != null && solution instanceof OrNode) {
    		solution = sort(solution);
    	}
    	l.removeAll();
    	return solution;
    }                

    /**
     * Sorts the list given in parameter. The comparison between nodes is 
     * used to sort the list. 
     * @param l the list of nodes to be sorted
     */
    private void quickSort(ListOfNodes l) {
    	int length=l.size();
    	quickSort(l,0,length-1);
    }
        
    /**
     * Returns the index of the pivot in the given list.
     * @param l a list to be sorted
     * @param begin the beginning index
     * @param end the ending index
     * @return the index of the pivot in the given list.
     */
    private int partition(ListOfNodes l,int begin,int end) {
    	int compt=begin;
    	Node pivot=l.get(begin);
    	for(int i=begin+1;i<=end;i++) {
    		if (l.get(i).compare(pivot) <0 && !(l.get(i) instanceof MetaFormulaReferenceNode) && (l.get(i).childrenOfKind(MetaFormulaReferenceNode.class, new ListOfNodes()))
    			&& !(pivot instanceof MetaFormulaReferenceNode) && !(pivot.childrenOfKind(MetaFormulaReferenceNode.class, new ListOfNodes()))) {
    			exchange(l,compt,i);
    			compt++;
    		} else if (pivot.compare(l.get(i)) <0 && !(pivot instanceof MetaFormulaReferenceNode) && (pivot.childrenOfKind(MetaFormulaReferenceNode.class, new ListOfNodes()))
    				&& !(l.get(i) instanceof MetaFormulaReferenceNode) && !(l.get(i).childrenOfKind(MetaFormulaReferenceNode.class, new ListOfNodes()))) {
    			compt++;
    		}                
    		else if (l.get(i).compare(pivot)< 0 && !(l.get(i) instanceof MetaFormulaReferenceNode)) {
    			compt++;
    			exchange(l,compt,i);
    		} else if (pivot instanceof MetaFormulaReferenceNode && 
    				(!(l.get(i) instanceof MetaFormulaReferenceNode) )) {
    			compt++;
    			exchange(l,compt,i);
    		}
    	}
    	exchange(l,begin,compt);
    	return(compt);
    }
        
    /**
     * Permutes the node at index i and the node at index j in the list l.
     * @param l a list of nodes
     * @param i an index in the list
     * @param j an index in the list
     */
    private void exchange(ListOfNodes l,int i,int j) {
    	Node memory=l.get(i);
    	l.replace(i, l.get(j));
    	l.replace(j, memory);
    }
        
    /**
     * Sort the nodes of the given list using the compare method of each kind
     * of nodes.
     * @param l the list to be sorted
     * @param beg the beginning index in the list to sort
     * @param end the ending index in the list to sort
     */
    private void quickSort(ListOfNodes l,int beg,int end) {
    	if(beg<end) {
    		int pivot=partition(l,beg,end);
    		quickSort(l,beg,pivot-1);
    		quickSort(l,pivot+1,end);
    	}
    }
        
    /**
     * Extracts all the elements of a AndNode and puts them into the list
     * of nodes given in parameter.
     * @param n an AndNode 
     * @param l the resulting list of nodes
     */
    private void getList(AndNode n, ListOfNodes l) {
    	if (n.as_left_formula() instanceof AndNode) {
    		getList((AndNode)n.as_left_formula(), l);
    	} else {
    		l.add(n.as_left_formula());
    	}
    	if (n.as_right_formula() instanceof AndNode) {
    		getList((AndNode)n.as_right_formula(), l);
    	} else {
    		l.add(n.as_right_formula());
    	}
    }
   
    /**
     * Extracts all the elements of a OrNode and puts them into the list
     * of nodes given in parameter.
     * @param n an OrNode 
     * @param l the resulting list of nodes
     */
    private void getList(OrNode n, ListOfNodes l) {
    	if (n.as_left_formula() instanceof OrNode) {
    		getList((OrNode)n.as_left_formula(), l);
    	} else {
    		l.add(n.as_left_formula());
    	}
    	if (n.as_right_formula() instanceof OrNode) {
    		getList((OrNode)n.as_right_formula(), l);
    	} else {
    		l.add(n.as_right_formula());
    	}
    }
        
    /**
     * If the given formula is a OrNode or a AndNode, this method returns sorts
     * the formula and returns the sorted formula. If not, returns the same
     * formula.
     * @param node a formula to be sorted
     * @return a sorted formula.
     */
    private Formula sort(Formula node) {
    	if (node instanceof OrNode || node instanceof AndNode) {
    		ListOfNodes l = new ListOfNodes();
    		if (node instanceof AndNode) {
    			getList((AndNode)node, l);
    		} else {
    			getList((OrNode)node, l);
    		}
    		quickSort(l);
    		if (node instanceof AndNode) {
    			return buildAndNode(l);
    		} 
    		//else {
    			return buildOrNode(l);
    		//}
    	} 
    	//else {
    		return node;
    	//}
    }
        
    /**
     * Tries to return a AndNode built with the Formulae of the given list. 
     * If the size of the list equals 1, it returns the only formula of the list.
     * If the size of the list equals 0 or if the list is null, returns null.
     * Else, it returns an AndFormula. 
     * @param l a list of formulae
     * @return a AndNode built with the Formulae of the given list, 
     * or a Formula, or null. 
     */
    private Formula buildAndNode(ListOfNodes l) {
    	Formula solution = null;
    	if (l != null) {
    		if (l.size() == 1) {
    			solution = (Formula)l.get(0);
    		} else if (l.size() >= 2) {
    			AndNode andNode = new AndNode((Formula)l.get(l.size()-2), (Formula)l.get(l.size()-1));
    			for (int i = l.size() - 3; i >= 0; i--) {
    				andNode = new AndNode((Formula)l.get(i), andNode);
    			}
    			solution = andNode;
    		}
    	}
    	return solution;
    }
        
    /**
     * Tries to return a OrNode built with the Formulae of the given list.
     * If the size of the list equals 1, it returns the only formula of the list.
     * If the size of the list equals 0 or if the list is null, returns null.
     * Else, it returns an OrFormula. 
     * @param l a list of formulae
     * @return a OrNode built with the Formulae of the given list, 
     * or a Formula, or null. 
     */
    private Formula buildOrNode(ListOfNodes l) {
    	Formula solution = null;
    	if (l != null) {
    		if (l.size() == 1) {
    			solution = (Formula)l.get(0);
    		} else if (l.size() >= 2) {
    			OrNode orNode = new OrNode((Formula)l.get(l.size()-2), (Formula)l.get(l.size()-1));
    			for (int i = l.size() - 3; i >= 0; i--) {
    				orNode = new OrNode((Formula)l.get(i), orNode);
    			}
    			solution = orNode;
    		}
    	}
    	return solution;
    }
        
    /**
     * @param expression1
     * @param expression2
     */
    private void doPatternMatchingOnChildren(Node expression1, Node expression2)
    {
    	if (expression1.getClass() == expression2.getClass()) {
    		Node[] nodes1 = expression1.children();
    		Node[] nodes2 = expression2.children();
    		_match = (nodes1.length == nodes2.length);
    		for (int i = 0; i < nodes1.length && _match; i++) {
    			_match = _match && matchExpressions(nodes1[i], nodes2[i]);
    		}
    	} else {
    		_match = false;
    	}
    }
        
    /**
     * @param expression1
     * @param expression2
     * @return
     */
    boolean matchExpressions(Node expression1, Node expression2)
    {
     	return new SLMatcher().match(expression1, expression2,
				   _metaReferences1, _metaReferences2,
				   _variables,
				   _firstAndResidue, _secondAndResidue, _firstOrResidue, _secondOrResidue);
    }
        
    /**
     * Try to find another reference with the same type and the same name.
     * If found then 
     *    If the two meta references match then assign the new one with the value of the other one.
     *    Else _match is set to false.
     * Else
     *    _match is set to true, the new meta reference value is exp, and the new meta reference is stored.
     * @param metaRef
     * @param exp
     * @param refIsExpression1 TODO
     */
    private void doPatternMatchingOnMetaReference(Node metaRef, Node exp, boolean refIsExpression1)
    {
    	Node assignedValue = (Node)metaRef.getAttribute(MetaVariableReferenceNode.sm_value_ID);
    	if ( assignedValue == null ) {
    		(refIsExpression1 ? _metaReferences1 : _metaReferences2).add(metaRef);
    		metaRef.setAttribute(MetaVariableReferenceNode.sm_value_ID, exp);
    		_match = true;
    	}
    	else {
    		_match = assignedValue == exp;
    		if (!_match) {
    			if (refIsExpression1) {
    				_match = matchExpressions(assignedValue, exp);
    			}
    			else {
    				_match = matchExpressions(exp, assignedValue);
    			}
    		}
    	}
    }
        
	//============================================================
	//                 VISITOR IMPLEMENTATION
	//============================================================
    @Override
	public void visitMetaFormulaReferenceNode(MetaFormulaReferenceNode node) {
    	if (_expression2 instanceof Formula) {
    		doPatternMatchingOnMetaReference(node, _expression2, true);
    	} else {
    		_match = false;
    	}
    }
        
    @Override
	public void visitMetaSymbolReferenceNode(MetaSymbolReferenceNode node) {
    	if (_expression2 instanceof Symbol) {
    		doPatternMatchingOnMetaReference(node, _expression2, true);
    	} else {
    		_match = false;
    	}
    }
        
    @Override
	public void visitMetaVariableReferenceNode(
    		MetaVariableReferenceNode node) {
    	if (_expression2 instanceof Variable) {
    		doPatternMatchingOnMetaReference(node, _expression2, true);
    	} else {
    		_match = false;
    	}
    }
        
    @Override
	public void visitMetaTermReferenceNode(MetaTermReferenceNode node) {
    	if (_expression2 instanceof Term) {
    		doPatternMatchingOnMetaReference(node, _expression2, true);
    	} else {
    		_match = false;
    	}
    }
        
    @Override
	public void visitMetaContentExpressionReferenceNode(
    		MetaContentExpressionReferenceNode node) {
    	if (_expression2 instanceof ContentExpression) {
    		doPatternMatchingOnMetaReference(node, _expression2, true);
    	} else {
    		_match = false;
    	}
    }
        
    @Override
	public void visitVariableNode(VariableNode node) {
    	if (_expression2 instanceof VariableNode) {
//    		_match = (((VariableNode) _expression2).lx_name().equals(node.lx_name()));
    		_match = _variables.addMapping(node, ((VariableNode) _expression2));
    	}
    	else if (_expression2 instanceof MetaVariableReferenceNode || _expression2 instanceof MetaTermReferenceNode) {
    		doPatternMatchingOnMetaReference(_expression2, node, false);
    	}
    	else {
    		_match = false;
    	}
    }
        
	@Override
	public void visitIntegerConstantNode(IntegerConstantNode node) {
		if (_expression2 instanceof IntegerConstantNode) {
			_match = ((IntegerConstantNode) _expression2).lx_value().equals(node.lx_value());
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
	
	/**
	 * {@inheritDoc}
	 * @see jade.semantics.lang.sl.grammar.VisitorBase#visitRelativeTimeConstantNode(jade.semantics.lang.sl.grammar.RelativeTimeConstantNode)
	 */
	@Override
	public void visitRelativeTimeConstantNode(RelativeTimeConstantNode node) {
		if (_expression2 instanceof RelativeTimeConstantNode) {
			_match = ((RelativeTimeConstantNode) _expression2).lx_value().equals(node.lx_value());
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
	}
        
    @Override
	public void visitRealConstantNode(RealConstantNode node) {
		if (_expression2 instanceof RealConstantNode) {
			_match = ((RealConstantNode) _expression2).lx_value().equals(node.lx_value());
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitStringConstantNode(StringConstantNode node) {
		if (_expression2 instanceof StringConstantNode) {
			_match = ((StringConstantNode) _expression2).lx_value() == node.lx_value();
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitWordConstantNode(WordConstantNode node) {
		if (_expression2 instanceof WordConstantNode) {
			_match = ((WordConstantNode) _expression2).lx_value() == node.lx_value();
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitByteConstantNode(ByteConstantNode node) {
		if (_expression2 instanceof ByteConstantNode) {
			_match = ((ByteConstantNode) _expression2).lx_value().equals(node.lx_value());
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitDateTimeConstantNode(DateTimeConstantNode node) {
		if (_expression2 instanceof DateTimeConstantNode) {
			_match = ((DateTimeConstantNode) _expression2).lx_value().equals(node.lx_value());
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitSymbolNode(SymbolNode node) {
		if (_expression2 instanceof SymbolNode) {
			_match = ((SymbolNode) _expression2).lx_value() == node.lx_value();
		}
		else if (_expression2 instanceof MetaSymbolReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
     }
        
    @Override
	public void visitParameterNode(ParameterNode node) {
    	if (_expression2 instanceof ParameterNode) {
    		_match = (node.lx_name().equals(
    				((ParameterNode) _expression2).lx_name()) && matchExpressions(
    						node.as_value(), ((ParameterNode) _expression2).as_value()));
    	} else {
    		_match = false;
    	}
    }
        
    @Override
	public void visitTrueNode(TrueNode node) {
		if (_expression2 instanceof TrueNode) {
			_match = true;
		}
		else if (_expression2 instanceof MetaFormulaReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitFalseNode(FalseNode node) {
		if (_expression2 instanceof FalseNode) {
			_match = true;
		}
		else if (_expression2 instanceof MetaFormulaReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitListOfContentExpression(ListOfContentExpression node) {
    	doPatternMatchingOnChildren(node, _expression2);
    }
        
    @Override
	public void visitListOfTerm(ListOfTerm node) {
    	doPatternMatchingOnChildren(node, _expression2);
    }
        
    @Override
	public void visitListOfParameter(ListOfParameter node) {
    	if (node.getClass() == _expression2.getClass()) {
    		node.sort(new Comparator() {
				public int compare(Object o1, Object o2) {
					return ((Parameter)o1).compare((Parameter)o2);
				}
				@Override
				public boolean equals(Object obj) {
					return super.equals(obj);
				}
			});
    		ListOfParameter node2 = (ListOfParameter)_expression2;
    		node2.sort(new Comparator() {
				public int compare(Object o1, Object o2) {
					return ((Parameter)o1).compare((Parameter)o2);
				}
				@Override
				public boolean equals(Object obj) {
					return super.equals(obj);
				}
			});;
    		int i = 0, j = 0;
    		_match = true;
    		
    		while (i < node.size() && j < node2.size()) {
    			ParameterNode p1 = (ParameterNode)node.element(i);
    			ParameterNode p2 = (ParameterNode)node2.element(j);
    			
    			_match = matchExpressions(p1, p2);
    			if ( _match ) {i++; j++;}
    			else if (p1.lx_optional().booleanValue()) {
    				if (!p1.lx_name().equals(p2.lx_name())) {_match = true; i++;}
    				else {_match = false; break;}
    			}
    			else {_match=true; j++;}
//    			else if (((ParameterNode)node2.element(j)).lx_optional().booleanValue()) {j++;}
//    			else {_match = false; break;}
    		}
    		// i == node.size() || j == node2.size()
                
    		while ( _match && i < node.size() ) {
    			_match = _match && ((ParameterNode)node.element(i++)).lx_optional().booleanValue();
    		}
//    		while ( _match && j < node2.size() ) {
//    			_match = _match && ((ParameterNode)node2.element(j++)).lx_optional().booleanValue();
//    		}
    	}
    }
        
    @Override
	public void visitContentNode(ContentNode node) {
		if (_expression2 instanceof ContentNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
    
    @Override
	public void visitActionContentExpressionNode(ActionContentExpressionNode node) {
		if (_expression2 instanceof ActionContentExpressionNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaContentExpressionReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitFormulaContentExpressionNode(FormulaContentExpressionNode node) {
		if (_expression2 instanceof FormulaContentExpressionNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaContentExpressionReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitIdentifyingContentExpressionNode(IdentifyingContentExpressionNode node) {
		if (_expression2 instanceof IdentifyingContentExpressionNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaContentExpressionReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitActionExpressionNode(ActionExpressionNode node) {
		if (_expression2 instanceof ActionExpressionNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
     }
        
    @Override
	public void visitAlternativeActionExpressionNode(AlternativeActionExpressionNode node) {
		if (_expression2 instanceof AlternativeActionExpressionNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitSequenceActionExpressionNode(SequenceActionExpressionNode node) {
		if (_expression2 instanceof SequenceActionExpressionNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
    
    @Override
	public void visitPropositionSymbolNode(PropositionSymbolNode node) {
		if (_expression2 instanceof PropositionSymbolNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaFormulaReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitResultNode(ResultNode node) {
		if (_expression2 instanceof ResultNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaFormulaReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitPredicateNode(PredicateNode node) {
		if (_expression2 instanceof PredicateNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaFormulaReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitEqualsNode(EqualsNode node) {
    	if (node.getClass() == _expression2.getClass()) {
    		int size1 = _metaReferences1.size();
    		int size2 = _metaReferences2.size();
    		int vsize = _variables.size();
    		boolean first = (matchExpressions(node.as_left_term(), ((EqualsNode)_expression2).as_left_term()) &&
    				         matchExpressions(node.as_right_term(), ((EqualsNode)_expression2).as_right_term()));
    		if (!first) {
    			_metaReferences1.restore(size1);
    			_metaReferences2.restore(size2);
    			_variables.restore(vsize);
//    			removeExpsAssignments(mrefs);
    			boolean second = (matchExpressions(node.as_right_term(), ((EqualsNode)_expression2).as_left_term()) &&
    					          matchExpressions(node.as_left_term(), ((EqualsNode)_expression2).as_right_term()));
    			_match = second;
    		} else {
    			_match = true;
    		}	
    	} else if (_expression2 instanceof MetaFormulaReferenceNode) {
    			doPatternMatchingOnMetaReference(_expression2, node, false);
    	} else {
    		_match = false;
    	}
    }
        
    @Override
	public void visitDoneNode(DoneNode node) {
		if (_expression2 instanceof DoneNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaFormulaReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitFeasibleNode(FeasibleNode node) {
		if (_expression2 instanceof FeasibleNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaFormulaReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitNotNode(NotNode node) {
		if (_expression2 instanceof NotNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaFormulaReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
    
    @Override
	public void visitAndNode(AndNode node) {
    	if (node.getClass() == _expression2.getClass()) {
    		AndNode firstPart = (AndNode)sort(node);
    		AndNode secondPart = (AndNode)sort((AndNode)_expression2);
    		if (firstPart.as_right_formula() instanceof AndNode && secondPart.as_right_formula() instanceof AndNode) {
    			if (matchExpressions(firstPart.as_left_formula(), secondPart.as_left_formula())) {
    				_match = matchExpressions(firstPart.as_right_formula(), secondPart.as_right_formula());
    			} else {
    				_firstAndResidue.add(firstPart.as_left_formula());
    				_secondAndResidue.add(secondPart.as_left_formula());
    				_match = matchExpressions(firstPart.as_right_formula(), secondPart.as_right_formula());
    			}
    		} else {
    			if (firstPart.as_right_formula() instanceof AndNode) {
    				_secondAndResidue.add(secondPart.as_left_formula());
    				_secondAndResidue.add(secondPart.as_right_formula());
    				getList(firstPart, _firstAndResidue);
    				_match = analyze(_secondAndResidue, _firstAndResidue, 0, false);
    			} else {
    				_firstAndResidue.add(firstPart.as_left_formula());
    				_firstAndResidue.add(firstPart.as_right_formula());
    				getList(secondPart, _secondAndResidue);
    				_match = analyze(_firstAndResidue, _secondAndResidue, 0, true);
    			}
    		}
    	} else if (_expression2 instanceof MetaFormulaReferenceNode) {
    		doPatternMatchingOnMetaReference(_expression2,node, false);
    	} else {
    		_match = false;
    	}
    }
        
    @Override
	public void visitOrNode(OrNode node) {
    	if (node.getClass() == _expression2.getClass()) {
    		OrNode firstPart = (OrNode)sort(node);
    		OrNode secondPart = (OrNode)sort((OrNode)_expression2);
    		if (firstPart.as_right_formula() instanceof OrNode && secondPart.as_right_formula() instanceof OrNode) {
    			if (matchExpressions(firstPart.as_left_formula(), secondPart.as_left_formula())) {
    				_match = matchExpressions(firstPart.as_right_formula(), secondPart.as_right_formula());
    			} else {
    				_firstOrResidue.add(firstPart.as_left_formula());
    				_secondOrResidue.add(secondPart.as_left_formula());
    				_match = matchExpressions(firstPart.as_right_formula(), secondPart.as_right_formula());
    			}
    		} else {
    			if (firstPart.as_right_formula() instanceof OrNode) {
    				_secondOrResidue.add(secondPart.as_left_formula());
    				_secondOrResidue.add(secondPart.as_right_formula());
    				getList(firstPart, _firstOrResidue);
    				_match = analyze(_secondOrResidue, _firstOrResidue, 1, false);
    			} else {
    				_firstOrResidue.add(firstPart.as_left_formula());
    				_firstOrResidue.add(firstPart.as_right_formula());
    				getList(secondPart, _secondOrResidue);
    				_match = analyze(_firstOrResidue, _secondOrResidue, 1, true);
    			}
    		}
    	} else if (_expression2 instanceof MetaFormulaReferenceNode) {
    			doPatternMatchingOnMetaReference(_expression2,node, false);
    	} else {
    			_match = false;
    	}
    }
        
    @Override
	public void visitImpliesNode(ImpliesNode node) {
		if (_expression2 instanceof ImpliesNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaFormulaReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitEquivNode(EquivNode node) {
		if (_expression2 instanceof EquivNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaFormulaReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitForallNode(ForallNode node) {
		if (_expression2 instanceof ForallNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaFormulaReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitExistsNode(ExistsNode node) {
		if (_expression2 instanceof ExistsNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaFormulaReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
    
    @Override
	public void visitObligationNode(ObligationNode node) {
    	if (_expression2 instanceof ObligationNode) {
    		doPatternMatchingOnChildren(node, _expression2);
    	}
    	else if (_expression2 instanceof MetaFormulaReferenceNode) {
    		doPatternMatchingOnMetaReference(_expression2, node, false);
    	}
		else {
			_match = false;
		}
    }
       
    @Override
	public void visitCountAsNode(CountAsNode node) {
    	if (_expression2 instanceof CountAsNode) {
    		doPatternMatchingOnChildren(node, _expression2);
    	}
    	else if (_expression2 instanceof MetaFormulaReferenceNode) {
    		doPatternMatchingOnMetaReference(_expression2, node, false);
    	}
		else {
			_match = false;
		}
    }
    
    @Override
	public void visitInstitutionalFactNode(InstitutionalFactNode node) {
    	if (_expression2 instanceof InstitutionalFactNode) {
    		doPatternMatchingOnChildren(node, _expression2);
    	}
    	else if (_expression2 instanceof MetaFormulaReferenceNode) {
    		doPatternMatchingOnMetaReference(_expression2, node, false);
    	}
		else {
			_match = false;
		}
    }
    
    @Override
	public void visitBelieveNode(BelieveNode node) {
		if (_expression2 instanceof BelieveNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaFormulaReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitUncertaintyNode(UncertaintyNode node) {
		if (_expression2 instanceof UncertaintyNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaFormulaReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitIntentionNode(IntentionNode node) {
		if (_expression2 instanceof IntentionNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaFormulaReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitPersistentGoalNode(PersistentGoalNode node) {
		if (_expression2 instanceof PersistentGoalNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaFormulaReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitFactNode(FactNode node) {
    	if (_expression2 instanceof FactNode) {
    		doPatternMatchingOnChildren(node, _expression2);
    	}
    	else if (_expression2 instanceof MetaTermReferenceNode) {
    		doPatternMatchingOnMetaReference(_expression2, node, false);
    	}
		else {
			_match = false;
		}
    }
    
    @Override
	public void visitTermSetNode(TermSetNode node) {
		if (_expression2 instanceof TermSetNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitTermSequenceNode(TermSequenceNode node) {
		if (_expression2 instanceof TermSequenceNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitFunctionalTermNode(FunctionalTermNode node) {
		if (_expression2 instanceof FunctionalTermNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    // FIXME to correct (see temporary correction in Tools.term2AID(Term term) ...)
    @Override
	public void visitFunctionalTermParamNode(FunctionalTermParamNode node) {
		if (_expression2 instanceof FunctionalTermParamNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitAnyNode(AnyNode node) {
		if (_expression2 instanceof AnyNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitSomeNode(SomeNode node) 
    {
		if (_expression2 instanceof SomeNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitIotaNode(IotaNode node) {
		if (_expression2 instanceof IotaNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
        
    @Override
	public void visitAllNode(AllNode node) {
		if (_expression2 instanceof AllNode) {
	    	doPatternMatchingOnChildren(node, _expression2);
		}
		else if (_expression2 instanceof MetaTermReferenceNode) {
			doPatternMatchingOnMetaReference(_expression2, node, false);
		}
		else {
			_match = false;
		}
    }
}
