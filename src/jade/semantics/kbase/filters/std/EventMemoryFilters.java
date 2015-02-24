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
 * EventMemoryFilters.java
 * Created on 4 janvier 2007
 * Author : Thierry Martinez & Vincent Pautret
 * 
 * Modified November 2007 by Carole Adam
 * Modification of parameter goOn in queryFilter to relax the query of 
 * done(action): now any action that was done at any instant can be returned,
 * even if it is not the last action performed. FIXME
 * 
 * Modified 18 December 2007 by Carole Adam & Vincent Louis
 * The apply method of the query filter now uses match instead of equals
 * and returns a MatchResult instead of a boolean.
 */

package jade.semantics.kbase.filters.std;

import jade.semantics.kbase.KBase;
import jade.semantics.kbase.QueryResult;
import jade.semantics.kbase.filters.FiltersDefinition;
import jade.semantics.kbase.filters.KBAssertFilter;
import jade.semantics.kbase.filters.KBQueryFilter;
import jade.semantics.lang.sl.grammar.ActionExpression;
import jade.semantics.lang.sl.grammar.BelieveNode;
import jade.semantics.lang.sl.grammar.DoneNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.SequenceActionExpressionNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.VariableNode;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;
import jade.semantics.lang.sl.tools.SL.LoopingInstantiationException;
import jade.util.leap.ArrayList;
import jade.util.leap.Set;

/**
 * 
 * @author Vincent Louis - France Telecom
 *
 * TODO process the reasons in the apply method
 */
public class EventMemoryFilters extends FiltersDefinition {
	
	
    /**
     * List of events already done
     */
    private ArrayList eventMemory;

    /**
     * The maximum size of the event memory list
     */
    private int event_memory_size = 10;

    
    /**
     * Adds a new event in the event memory
     * @param action an action expression of an done event
     */
    private void addEventInMemory(ActionExpression action) {
    	try {
            SL.substituteMetaReferences(action);
            SL.removeOptionalParameter(action);
        } catch (LoopingInstantiationException e) {
            e.printStackTrace();
        }
        eventMemory.add(0, action);
        
        if (eventMemory.size() == event_memory_size + 1) eventMemory.remove(event_memory_size);
    }

    /**
     * Returns the event memory
     * @return the event memory
     */
    private ArrayList getEventMemory() {
        return eventMemory;
    }
    
    /**
     * Sets the size of the event memory
     * @param size the new size of the event memory
     */
    public void setEventMemorySize(int size) {
        event_memory_size = size;
    }

    /**
     * Constructor of a event memory filters environment
     */
    public EventMemoryFilters() {
    	this.eventMemory = new ArrayList();
    	
    	//
    	// Define a KBAssertFilter
    	// ------------------------
    	defineFilter(new KBAssertFilter() {
        
    		/**
    		 * Pattern used to test the applicability of the filter
    		 */
    		private Formula pattern = SL.formula("(B ??agent (done ??action ??phi))");
        
    		/**
    		 * Asserts a formula.
    		 * If the formula given in parameter is a 
    		 * <code>SequenceActionExpressionNode</code>,
    		 * this method asserts all the elements of 
    		 * the sequence in the event list of the belief base. 
    		 * If the action expression given in parameter is an ActionExpressionNode,
    		 * the method asserts the action expression.
    		 * In these two cases, returns a <code>TrueNode</code>. Otherwise, does nothing,
    		 * and returns the formula given in parameter.
    		 * @param formula a formula to assert
    		 * @return <code>TrueNode</code> if the filter is applicable, the given formula in the 
    		 * other cases.
    		 */
    		@Override
			public Formula apply(Formula formula) {
    			MatchResult matchResult = SL.match(pattern, formula);
    			if (matchResult != null) {
    				try {
    					storeInBase((ActionExpression)matchResult.getTerm("action"));
    					myKBase.updateObservers(((BelieveNode)formula).as_formula());
    					return SL.TRUE; 
    				} catch (SL.WrongTypeException wte) {}
    			}
    			return formula;
    		} 
            
    		/**
    		 * If the action expression given in parameter is a 
    		 * SequenceActionExpressionNode, this method asserts all the elements of 
    		 * the sequence in the event list of the belief base. 
    		 * If the action expression given in parameter is an ActionExpressionNode,
    		 * the method asserts the action expression.
    		 * @param action an action expression
    		 */
    		private void storeInBase(ActionExpression action) {
    			if (action instanceof SequenceActionExpressionNode) {
    				addEventInMemory((ActionExpression)((SequenceActionExpressionNode)action).as_left_action());
    				storeInBase((ActionExpression)((SequenceActionExpressionNode)action).as_right_action());
    			} else {
    				addEventInMemory(action); 
    			}
    		}             
    	});
    	
    	//
    	// Define a KBQueryFilter
    	// ------------------------
    	defineFilter(new KBQueryFilter() {
    	    
    	    /**
    	     * Exist pattern
    	     */
    	    private Formula existPattern = SL.formula("(B ??agent (exists ??e (done ??act)))");
    	    
    	    /**
    	     * Done pattern
    	     */
    	    private Formula donePattern = SL.formula("(B ??agent (done ??act true))");
    	    
    	    /**
    	     * Applies to formulas matching one of the following patterns:
    	     * <ul>
    	     * <li>(B ??myself (exists ??e (done ??act)))
    	     * <li>(B ??myself (done ??act true))
    	     * <ul>
    	     * In this case, the returned {@link QueryResult} is different from 
    	     * {@link QueryResult.UNKNOWN} if the action recovered in the match result 
    	     * is a sequence of action already done by myself.
    	     * @param formula a formula on which the filter is tested
    	     * @param remoteAgent a term that represents the agent is trying to apply the filter
    	     * @return a QueryResult as described above.
    	     */
    	    @Override
			public QueryResult apply(Formula formula, ArrayList falsityReasons, QueryResult.BoolWrapper goOn) {
    	    	try {
    	            MatchResult applyResult = SL.match(existPattern,formula);
    	            if (applyResult != null && myKBase.getAgentName().equals(applyResult.getTerm("agent"))) { 
    	                return apply(applyResult);
    	            } 
    	            //else {
    	                applyResult = SL.match(donePattern,formula);
    	                if (applyResult != null && myKBase.getAgentName().equals(applyResult.getTerm("agent"))) {
    	                    return apply(applyResult);
    	                }
    	            //}
    	        } catch (SL.WrongTypeException wte) {
    	            wte.printStackTrace();
    	        }
    	        return QueryResult.UNKNOWN;
    	    } 
    	    
    	    /**
    	     * Returns a <code>QueryResult.KNOWN</code> if the action recovered in the match result is a
    	     * sequence of action already done by myself, <code>QueryResult.UNKNOWN</code> if not.
    	     * @param applyResult the MatchResult corresponding to the match between
    	     * the incoming formula and the pattern of the filter.
    	     * @return QueryResult.KNOWN or QueryResult.UNKNOWN.
    	     * FIXME should return new QueryResult(applyResult) instead of QueryResult.KNOWN
    	     * 
    	     * new version Carole Adam, 18 December 2007 - returns a QueryResult instead of a boolean
    	     */
    	    private QueryResult apply(MatchResult applyResult) {
    	    	try {
    	        	// FIXME called with goOn = false (last parameter) : why ?
    	            return (analyzeActionExpression(applyResult, applyResult.getTerm("act"), 0, false));
    	        } catch (SL.WrongTypeException wte) {
    	            wte.printStackTrace();
    	        }
    	        return QueryResult.UNKNOWN;
    	    } 
    	    
    	    /**
    	     * Tests if a sequence of actions (can be reduced to only one) is in the
    	     * event memory or not. If VariableNode appears that means that actions could
    	     * appear between them. For example, a1;a2;e;a3 , means that a2 must follow
    	     * a1 in the memory whereas there can be several actions between a2 and a3.
    	     * @param applyResult the MatchResult corresponding to the match between
    	     * the incoming formula and the pattern of the filter.
    	     * @param action the action expression to test
    	     * @param index current index
    	     * @param goOn true if a VariableNode is met
    	     * @return true if the action expression is in memory, false if not.
    	     */
    	    // FIXME the last parameter is false when called by query.apply
    	    // only the first element of the memory is checked ... FIXME
    	    private QueryResult analyzeActionExpression(MatchResult applyResult, Term action, int index, boolean goOn) {
    	        if (index == getEventMemory().size() && !(action instanceof VariableNode)) {
    	            return QueryResult.UNKNOWN;
    	        }
    	        if (action instanceof SequenceActionExpressionNode) {
    	            try {
    	            	MatchResult rightMatch = SL.match(((SequenceActionExpressionNode)action).as_right_action(),(Node)getEventMemory().get(index));
    	                if (((SequenceActionExpressionNode)action).as_right_action() instanceof VariableNode 
    	                        && ((SequenceActionExpressionNode)action).as_right_action().equals(applyResult.getTerm("e"))) {
    	                    return analyzeActionExpression(applyResult, ((SequenceActionExpressionNode)action).as_left_action(), index, true);    
    	                } else if (rightMatch != null) {
    	                	QueryResult leftQMatch = analyzeActionExpression(applyResult, ((SequenceActionExpressionNode)action).as_left_action(), index + 1, true);
    	                	MatchResult leftMatch;
    	                	if (leftQMatch != null) {
    	                		// FIXME: only keep first result ??
    	                    	leftMatch = leftQMatch.getResult(0);
    	                    	return new QueryResult(rightMatch.join(leftMatch));
    	                	}
    	                	leftMatch = null;
    	                	return null;
    	                } else {
    	                    if (goOn) {
    	                        return analyzeActionExpression(applyResult, action, index + 1, true);
    	                    } 
    	                    //else {
    	                        return QueryResult.UNKNOWN;
    	                    //}
    	                }    
    	            }catch (Exception wte) {
    	                wte.printStackTrace();
    	                return QueryResult.UNKNOWN;
    	            }
    	        } 
    	        //else {
    	        	try {
    	                if (action instanceof VariableNode && action.equals(applyResult.getTerm("e"))) return QueryResult.KNOWN;
    	            } catch (SL.WrongTypeException wte) {
    	                wte.printStackTrace();
    	                return QueryResult.UNKNOWN;
    	            } 
    	            // to debug BEGIN (CA 27/11/07)
    	            goOn = (index<getEventMemory().size());
    	            // debug END
    	            // FIXME : en fait il faudrait enlever goOn = ...
    	            // et plut�t faire une query(exists(e,done(action;e)))
    	            // o� e contient au minimum l'action de faire la query...
    	            MatchResult match = SL.match((Node)getEventMemory().get(index), action);
    	            if (match != null) {
    	                return new QueryResult(match);
    	            } else if (goOn) {
    	                return analyzeActionExpression(applyResult, action, index + 1, goOn);
    	            } else {
    	                return QueryResult.UNKNOWN;
    	            }
    	        //}
    	    } 
    	    
    	    /**
    	     * By default, this method does nothing. 
    	     * @param formula an observed formula
    	     * @param set set of patterns. Each pattern corresponds to a kind a formula
    	     * which, if it is asserted in the base, triggers the observer that
    	     * observes the formula given in parameter.
    	     */
    	    // modification CA 23 November 2007
    	    // correction by CA 02 April 2008: added a test if formula is null to prevent NullPointerException
    	    @Override
			public boolean getObserverTriggerPatterns(Formula formula, Set set) {
    	    	MatchResult match;
    	    	if (formula != null) {
    	    		match = formula.match(donePattern);
    	    	}
    	    	else {
    	    		match = null;
    	    	}
    	    	if (match != null) {
    	    		ActionExpression act = (ActionExpression)match.term("act");
    	    		Term agent = match.term("agent");
    	    		Formula newPattern = SL.formula("(B "+agent+" (done "+act+" ??phi))");
    	    		set.add(newPattern);
    	    	}
    	    	
    	    	return true;
    	    }
    	    
    	    @Override
			public ArrayList toStrings() {
    	    	ArrayList result = new ArrayList(eventMemory.size());
    	    	result.add("******* Event Memory Filter *******");
    	    	for (int i=0; i<eventMemory.size(); i++) {
    	    		// "Memory(" + i + ") - " +
    	    		result.add(new DoneNode((ActionExpression)eventMemory.get(i),SL.TRUE));
    	    	}
    	    	result.add("***");
    	    	return result;
    	    }

    	});
    }

    public void init(KBase kb) {
    	kb.addClosedPredicate(SL.formula(""));
    }
}
