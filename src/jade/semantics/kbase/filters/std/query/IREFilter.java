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
 * IREFilter.java
 * Created on 30 sept. 2005
 * Author : Vincent Pautret
 */
package jade.semantics.kbase.filters.std.query;

import jade.semantics.kbase.QueryResult;
import jade.semantics.kbase.filters.KBQueryFilter;
import jade.semantics.lang.sl.grammar.AllNode;
import jade.semantics.lang.sl.grammar.AnyNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.IdentifyingExpression;
import jade.semantics.lang.sl.grammar.IotaNode;
import jade.semantics.lang.sl.grammar.ListOfNodes;
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.SomeNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TermSetNode;
import jade.semantics.lang.sl.grammar.VariableNode;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;
import jade.semantics.lang.sl.tools.SL.WrongTypeException;
import jade.util.leap.ArrayList;
import jade.util.leap.Set;

/**
 * This filter applies when the query relates to the equality between an 
 * identifying expression and a term.
 * @author Vincent Pautret - France Telecom
 * @version 0.9
 * 
 * TODO process the reasons in the apply method
 * 
 */
public class IREFilter extends KBQueryFilter {

    /**
     * First part of the formula
     */
//    private IdentifyingExpression ire ;
    
    /**
     * Second part of the formula
     */
//    private Term term;
    
    /**
     * Pattern that must match to apply the filter
     */
    private Formula pattern;
    
    /**
     * Pattern that must match to apply the filter
     */
    private Formula notPattern;
    
    
    private ListOfNodes listOfNodes;
    private Term termPattern;
    private Formula formulaPattern;

    /*********************************************************************/
    /**                         CONSTRUCTOR                             **/
    /*********************************************************************/
    
    /**
     * Creates a new Filter on the patterns (B ??agent (= ??ire ??term)) and 
     * (B ??agent (not (= ??ire ??term))).
     */
    public IREFilter() {
        pattern = SL.formula("(B ??agent (= ??ire ??term))");
        notPattern = SL.formula("(B ??agent (not (= ??ire ??term)))");
        listOfNodes = new ListOfNodes();
        termPattern = null;
        formulaPattern = null;
    } 
    
    /*********************************************************************/
    /**                         METHODS                                 **/
    /*********************************************************************/
    
    /** 
     * If if the formula matches the pattern (B ??agent (= ??ire ??term)) 
     * or (B ??agent (not (= ??ire ??term)) and ??agent equals the given agent term,  
     * this method returns a {@link QueryResult} corresponding to the solution
     * to the question. Else it returns {@link QueryResult#UNKNOWN}.
     * @param formula a formula on which the filter is tested
     * @param agent a term that represents the agent is trying to apply the filter
     * @return a QueryResult as described above. 
     */
    @Override
	public QueryResult apply(Formula formula, ArrayList falsityReasons, QueryResult.BoolWrapper goOn) {
        ListOfNodes listOfNodes = null;
        Term termPattern = null;
        Formula formulaPattern = null;
        try {
        	IdentifyingExpression ire;
        	Term term;
            MatchResult applyResult = SL.match(pattern,formula);
            if (applyResult != null) {

                if (applyResult.getTerm("ire") instanceof IdentifyingExpression) {
                    ire = (IdentifyingExpression)applyResult.getTerm("ire");
                    term = applyResult.getTerm("term");
                } else if (applyResult.getTerm("term") instanceof IdentifyingExpression) {
                    ire = (IdentifyingExpression)applyResult.getTerm("term");
                    term = applyResult.getTerm("ire");
                } else {
                    return QueryResult.UNKNOWN;
                }

                getPattern(ire);
                listOfNodes = this.listOfNodes;
                termPattern = this.termPattern;
                formulaPattern = this.formulaPattern;
                
                return patternProcess(formula, listOfNodes, termPattern, formulaPattern, ire, term);
            } 
            //else {
                applyResult = SL.match(notPattern,formula);
                if (applyResult != null) {
                	// formula match (B ??agent (not (= ??ire ??term)))
                    if (applyResult.getTerm("ire") instanceof IdentifyingExpression) {
                        ire = (IdentifyingExpression)applyResult.getTerm("ire");
                        term = applyResult.getTerm("term");
                    } else if (applyResult.getTerm("term") instanceof IdentifyingExpression) {
                        ire = (IdentifyingExpression)applyResult.getTerm("term");
                        term = applyResult.getTerm("ire");
                    } else {
                        return QueryResult.UNKNOWN;
                    }

                    getPattern(ire);
                    listOfNodes = this.listOfNodes;
                    termPattern = this.termPattern;
                    formulaPattern = this.formulaPattern;

                     return notPatternProcess(formula, listOfNodes, termPattern, formulaPattern, ire, term);
                }
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return QueryResult.UNKNOWN;
    } 
    
    /**
     * Returns a QueryResult corresponding to the answer to the query. If
     * the ??term is a meta variable, returns a list of MatchResults with only one 
     * MatchResult containing the assignation between the meta varaible corresponding to
     * ??term and the solution. If ??term is a constant, returns an empty list
     * if ??term equals the answer to the query on formula, and null if not.
     * @param formula the formula to be queried
     * @return a QueryResult corresponding to the answer to the query.
     */
    private QueryResult patternProcess(Formula formula, ListOfNodes listOfNodes, Term termPattern, Formula formulaPattern, IdentifyingExpression ire, Term term) {
        QueryResult queryResult = myKBase.query(formulaPattern);
        if (queryResult == null) {
        	if ( myKBase.isClosed(formulaPattern, null)  ) {
                if (ire instanceof SomeNode || ire instanceof AllNode) {
                    return new QueryResult(SL.match(term, new TermSetNode()));
                }
                //else {
                	return QueryResult.KNOWN;
                //}
        	}
        	//else {
                if (ire instanceof SomeNode) {
                    return new QueryResult(SL.match(term, new TermSetNode()));
                }
                //else {
                	return QueryResult.UNKNOWN;
                //}       		
        	//}
        } 
        else if ((ire instanceof AnyNode && queryResult.size() >= 1) ||
                 (ire instanceof SomeNode && queryResult.size() >= 1) ||
                 (ire instanceof AllNode && myKBase.isClosed(formulaPattern, queryResult)) || 
                 (ire instanceof IotaNode && queryResult.size() == 1 && myKBase.isClosed(formulaPattern, queryResult))) {
            ListOfTerm terms = new ListOfTerm();
            for (int j = 0; j < queryResult.size(); j++) {
            	Term termResult = (Term)termPattern.getClone();
            	try {
            		for (int i=0; i<listOfNodes.size(); i++) {
            			if (termPattern instanceof MetaTermReferenceNode) {
            				termResult = queryResult.getResult(j).getTerm(((VariableNode)listOfNodes.get(i)).lx_name());
            			} 
            			else {
            				if (queryResult.getResult(j).getTerm(((VariableNode)listOfNodes.get(i)).lx_name()) == null) {
            					termResult = (Term)SL.instantiate(termResult, 
                                      							  ((VariableNode)listOfNodes.get(i)).lx_name(), 
                                                                  listOfNodes.get(i));
            				} 
            				else {
            					termResult = (Term)SL.instantiate(termResult, 
                                                                  ((VariableNode)listOfNodes.get(i)).lx_name(), 
                                                                  queryResult.getResult(j).getTerm(((VariableNode)listOfNodes.get(i)).lx_name()));
            				}
            			}
            		}
            		terms.add(termResult);
            	} catch(Exception e) {e.printStackTrace();}
            }

            if (term instanceof MetaTermReferenceNode) {
                MatchResult match = null;
                if (ire instanceof IotaNode) {
                    match = SL.match(term, terms.get(0));
                } else if (ire instanceof AnyNode) {
                	int idx = (int)Math.floor(Math.random() * terms.size());
                    match = SL.match(term, terms.get(idx));
                } else {
                    match = SL.match(term, new TermSetNode(terms));   
                }
                if (match != null) {

                    return new QueryResult(match);
                }
                return QueryResult.UNKNOWN;
            } 
            //else {
                if (ire instanceof IotaNode) {
                    if (terms.get(0).equals(term)) {
                        return QueryResult.KNOWN;
                    }
                } else if (ire instanceof AnyNode) {
                    if (terms.contains(term)) {
                        return QueryResult.KNOWN;
                    }
                } else {
                    if (term instanceof TermSetNode &&
                            ((TermSetNode)term).as_terms().size() == terms.size() 
                             ) {
                        ListOfTerm list = ((TermSetNode)term).as_terms();
                        for(int i = 0; i < list.size(); i++) {
                            if (!terms.contains(list.get(i))) {return null;}
                        }
                        return QueryResult.KNOWN;
                    } 
                }
            //}
        }
        return QueryResult.UNKNOWN;
    }


    /**
     * Returns a QueryResult corresponding to the answer to the query. If
     * the ??term is a meta variable, returns an empty list. 
     * If ??term is a constant, returns an empty list
     * if ??term does not equal the answer to the query on formula, and null if not.
     * @param formula the formula to be queried
     * @return a QueryResult corresponding to the answer to the query.
     */
    private QueryResult notPatternProcess(Formula formula, ListOfNodes listOfNodes, Term termPattern, Formula formulaPattern, IdentifyingExpression ire, Term term) {
        QueryResult queryResult = myKBase.query(formulaPattern);
        // FIXME The following line must be replaced as in patternProcess.
        if (queryResult == null) return new QueryResult();
        if ((ire instanceof AnyNode && queryResult.size() >= 1) ||
                (ire instanceof SomeNode && queryResult.size() >= 1) ||
                (ire instanceof AllNode && myKBase.isClosed(formulaPattern, queryResult)) || 
                (ire instanceof IotaNode && queryResult.size() == 1 && myKBase.isClosed(formulaPattern, queryResult))) {
            ListOfTerm terms = new ListOfTerm();
            for (int j = 0; j < queryResult.size(); j++) {
                    Term termResult = (Term)termPattern.getClone();
                    
                    try {
                        for (int i=0; i<listOfNodes.size(); i++) {
                            if (termPattern instanceof MetaTermReferenceNode) {
                                termResult = queryResult.getResult(j).getTerm(((VariableNode)listOfNodes.get(i)).lx_name());
                            } else {
                                if (queryResult.getResult(j).getTerm(((VariableNode)listOfNodes.get(i)).lx_name()) == null) {
                                    termResult = (Term)SL.instantiate(termResult, 
                                            ((VariableNode)listOfNodes.get(i)).lx_name(), 
                                            listOfNodes.get(i));
                                } else {
                                    termResult = (Term)SL.instantiate(termResult, 
                                            ((VariableNode)listOfNodes.get(i)).lx_name(), 
                                            queryResult.getResult(j).getTerm(((VariableNode)listOfNodes.get(i)).lx_name()));
                                }
                            }
                        }
                        terms.add(termResult);
                    } catch(Exception e) {e.printStackTrace();}
            }
            if (term instanceof MetaTermReferenceNode) {
                if (ire instanceof IotaNode && !term.equals(terms.get(0))) {
                    return QueryResult.KNOWN;
                } else if (ire instanceof AnyNode) {
                    return QueryResult.KNOWN;
                } else if (ire instanceof AllNode || ire instanceof SomeNode){
                    return QueryResult.KNOWN;
                }
            } else {
                if (ire instanceof IotaNode && !term.equals(terms.get(0))) {
                    return QueryResult.KNOWN;
                } else if (ire instanceof AnyNode) {
                    if (!terms.contains(term)) {
                        return QueryResult.KNOWN;
                    }
                } else if (ire instanceof AllNode || ire instanceof SomeNode) {
                    TermSetNode node = new TermSetNode(terms);
                    if (node.equals(term)) {
                        return QueryResult.UNKNOWN;
                    } 
                    //else {
                        return QueryResult.KNOWN;
                    //}
                }
            }
        } 
        return QueryResult.UNKNOWN;
    }
    
    /**
     * Creates the formulaPattern and the TermPattern by changing into meta variables 
     * all the variables that appear in the term of the identifying expression. 
     */
    private void getPattern(IdentifyingExpression ire) {
        listOfNodes = new ListOfNodes();
        termPattern = null;
        formulaPattern = null;
        if (ire.as_term().childrenOfKind(VariableNode.class, listOfNodes)) {
            termPattern = (Term)SL.toPattern(ire.as_term(), (VariableNode)listOfNodes.get(0), ((VariableNode)listOfNodes.get(0)).lx_name());
            formulaPattern = (Formula)SL.toPattern(ire.as_formula(), (VariableNode)listOfNodes.get(0), ((VariableNode)listOfNodes.get(0)).lx_name());
            for (int i = 1; i < listOfNodes.size(); i++) {
                formulaPattern = (Formula)SL.toPattern(formulaPattern, (VariableNode)listOfNodes.get(i), ((VariableNode)listOfNodes.get(i)).lx_name());
                termPattern = (Term)SL.toPattern(termPattern, (VariableNode)listOfNodes.get(i), ((VariableNode)listOfNodes.get(i)).lx_name());
            }
        } else {
            formulaPattern = ire.as_formula();
            termPattern = ire.as_term();
        }
    }
    /**
     * By default, this method does nothing. 
     * @param formula an observed formula
     * @param set set of patterns. Each pattern corresponds to a kind a formula
     * which, if it is asserted in the base, triggers the observer that
     * observes the formula given in parameter.
     */
    @Override
	public boolean getObserverTriggerPatterns(Formula formula, Set set) {
//    	System.err.println("ire filter, pattern="+pattern+" ; formula="+formula);
        
    	MatchResult applyResult = SL.match(pattern,formula);
    	if (applyResult != null) {
        	IdentifyingExpression ire;
//        	System.err.println("************** IRE FILTER !!! *************");
//        	System.err.println("applyResult="+applyResult);
        	try {
        		if (applyResult.getTerm("ire") instanceof IdentifyingExpression) {
        			ire = (IdentifyingExpression)applyResult.getTerm("ire");
        		} else if (applyResult.getTerm("term") instanceof IdentifyingExpression) {
        			ire = (IdentifyingExpression)applyResult.getTerm("term");
        		} else {
        			return true;
        		}
        		getPattern(ire);
				myKBase.getObserverTriggerPatterns(formulaPattern, set);
        		return false;
        	}
        	catch (WrongTypeException wte) {
        		wte.printStackTrace();
        	}
        }
        return true;
    }
}
