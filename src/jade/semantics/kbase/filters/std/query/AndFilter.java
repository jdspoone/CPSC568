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
* AndFilter.java
* Created on 23 nov. 2004
* Author : Vincent Pautret
*/
package jade.semantics.kbase.filters.std.query;

import jade.semantics.kbase.KBase;
import jade.semantics.kbase.QueryResult;
import jade.semantics.kbase.filters.KBQueryFilter;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;
import jade.util.leap.ArrayList;
import jade.util.leap.Set;


/**
* This filter applies when an "and formula" is asserted in the belief Base.
* @author Vincent Pautret - France Telecom
* @version 0.9
*/
public class AndFilter extends KBQueryFilter {
   
   
   /**
    * Pattern that must match to apply the filter
    */
   private Formula pattern;
   
   /*********************************************************************/
   /**                          CONSTRUCTOR                             **/
   /*********************************************************************/
   
   /**
    * Creates a new Filter on the pattern (and ??phi ??psi)
    */
   public AndFilter() {
       pattern = SL.formula("(and ??phi ??psi)");
   } // End of AndFilter/1
   
   /*********************************************************************/
   /**                          METHODS                                 **/
   /*********************************************************************/
   
   /** 
    * If the formula matches the pattern (B ??agent (and ??phi ??psi)) and
    * ??agent equals the given agent, this method returns a {@link QueryResult} holding
    * the joined MatchResults of ??phi and ??psi, that is the MatchResults
    * that satisfy ??phi as well as ??psi. Else it returns {@link QueryResult.UNKNOWN}
    * @param formula a formula on which the filter is tested
    * @param agent a term that represents the agent is trying to apply the filter
    * @return a QueryResult as described above. 
    */
   @Override
public QueryResult apply(Formula formula, ArrayList falsityReasons, QueryResult.BoolWrapper goOn) {
       MatchResult applyResult = SL.match(pattern,formula);
       if (applyResult != null) {
    	   goOn.setBool(false); // Further filters must not be applied
    	   return andPhiPsi(applyResult.formula("phi"), myKBase,
    			   applyResult.formula("psi"), myKBase, falsityReasons);
       }
       return null;
   } 

   /**
    * Adds in the set, the patterns for the formula phi and for the formula
    * psi.
    * @param formula an observed formula
    * @param set set of patterns. Each pattern corresponds to a kind a formula
    * which, if it is asserted in the base, triggers the observer that
    * observes the formula given in parameter.
    */
   @Override
public boolean getObserverTriggerPatterns(Formula formula, Set set) {
	   MatchResult match = SL.match(pattern,formula);
	   if (match != null) {
		   try {
			   myKBase.getObserverTriggerPatterns(match.getFormula("phi"), set);
			   myKBase.getObserverTriggerPatterns(match.getFormula("psi"), set);
			   return false;
		   } catch (SL.WrongTypeException wte) {
			   wte.printStackTrace();
		   }
	   }
	   return true;
   }
   
   private static QueryResult andPhiPsi(Formula phi, KBase phiKB, Formula psi, KBase psiKB, ArrayList falsityReasons) {
	   QueryResult result = QueryResult.UNKNOWN;
	   
	   QueryResult phiBindings = phiKB.query(phi, new ArrayList());
	   if ( phiBindings == null ) {
		   // We try the other way round
		   Formula tmp = phi;
		   KBase tmpKB = phiKB;
		   phi = psi;
		   phiKB = psiKB;
		   psi = tmp;
		   psiKB = tmpKB;
		   phiBindings = phiKB.query(phi, falsityReasons);
	   }
	   try {
		   if ( phiBindings != null ) {
			   if ( phiBindings.size() == 0 ) {
				   result = psiKB.query(psi, falsityReasons);
			   }
			   else {
				   ArrayList psiReasons = new ArrayList();
				   for (int i=0; i<phiBindings.size(); i++) {
					   MatchResult phiBinding = phiBindings.getResult(i);
					   Formula npsi = (Formula)psi.getClone();
					   for (int j=0; j<phiBinding.size(); j++) {
						   SL.set(npsi, 
								   SL.getMetaReferenceName(phiBinding.get(j)), 
								   SL.getMetaReferenceValue(phiBinding.get(j)));
					   }
					   SL.substituteMetaReferences(npsi);
					   
					   QueryResult psiBindings = psiKB.query(npsi, psiReasons);
					   if (psiBindings != QueryResult.UNKNOWN) {
						   // We will probably add something
						   if (result == null) {
							   result = new QueryResult();
						   }
						   if ( psiBindings.isEmpty() ) {
							   // This phiBinding is sufficient
							   result.add(phiBinding);
						   }
						   else {
							   // There are possibly several psiBindings for this phiBinding
							   for (int k=0; k<psiBindings.size(); k++) {
								   MatchResult mr = phiBinding.join(psiBindings.getResult(k));
								   if ( mr != null) {
									   result.add(mr);
								   }
							   }
						   }
					   }
				   }
				   if (result == QueryResult.UNKNOWN) {
					   QueryResult.addReasons(falsityReasons, psiReasons);
				   }
			   }
		   }
	   }
	   catch (Exception e) {
		   e.printStackTrace();
	   }
	   return result;
   }
} 