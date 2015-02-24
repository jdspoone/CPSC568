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
 * KBase.java
 * Created on 28 oct. 2004
 * Author : Vincent Pautret
 */
package jade.semantics.kbase;

import jade.semantics.interpreter.Finder;
import jade.semantics.kbase.observers.Observer;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.IdentifyingExpression;
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.Term;
import jade.util.leap.ArrayList;

/**
 * This interface defines belief base API.
 * The belief base is a central component of the JSA framework, which mainly
 * maintains the internal state of semantic agents, in terms of their explicit
 * symbolic representation of their environment. This symbolic representation
 * is expressed by FIPA-SL formulas.
 * <p>
 * This interface provides three main kinds of operations:
 * <ul>
 *     <li>update the semantic agent's internal state, by
 *         {@linkplain #assertFormula(Formula) asserting} or
 *         {@linkplain #retractFormula(Formula) retracting} formulas,</li>
 *     <li>retrieve beliefs from the semantic agent's internal state, by
 *         querying {@linkplain #query(Formula) formulas} or
 *         {@linkplain #queryRef(IdentifyingExpression) IREs} (Identifying
 *         Referential Expressions),</li>
 *     <li>notify changes of the semantic agent's internal state, by
 *         {@linkplain #addObserver(Observer) installing} or
 *         {@linkplain #removeObserver(Observer) uninstalling} observers.</li> 
 * </ul>
 * </p>
 * The {@link KBase} interface was primarily intended to be implemented by third
 * parties. However, as it is quite a difficult and very technical task, the
 * JSA frameworks provides a default {@linkplain jade.semantics.kbase.FilterKBase
 * filter-based implementation}, which offers a good trade-off between efficiency
 * and expressiveness.
 * 
 * @author Vincent Pautret - France Telecom
 * @version 0.9
 * @version 0.9
 * @since JSA 1.0
 */
public interface KBase {
    
    /**
     * The {@link KBase} mechanism follows the decoration design pattern.
     * A {@link KBase} instance can be enclosed within another {@link KBase}
     * instance, wich can in turn be enclosed in another {@link KBase} instance
     * and so on.
     *   
     * @return the most enclosing {@link KBase} instance, on which to proceed
     *         assertion and query operations. This also corresonds to the
     *         {@link KBase} instance attached to the semantic capabilities of
     *         an agent. It can be this {@link KBase} object.
     */
    public KBase getWrappingKBase();
    
    /**
     * Set the most enclosing {@link KBase}. This method is for internal use  
     * only, and should never be called by the JSA programmer.
     * @param  the most enclosing {@link KBase} instance, on which to proceed
     *         assertion and query operations. This also corresonds to the
     *         {@link KBase} instance attached to the semantic capabilities of
     *         an agent. It can be this {@link KBase} object.
     */
	public void setWrappingKBase(KBase kbase); 
	
	/**
     * Get the name of the agent (given as a FIPA-SL term representing his AID)
     * owning this {@link KBase} instance. Note that a {@link KBase} instance
     * stores the beliefs of <b>only one</b> agent, even if these beliefs may
     * concern beliefs on other agents.
     * 
     * @return the name of the agent believing the facts stored in this
     *         {@link KBase} object. 
     */
    public Term getAgentName();

    /**
     * Modify the agent (given as a FIPA-SL term representing his AID) the
     * beliefs stored in this {@link KBase} instance belong to.
     * 
     * @param agent the agent the beliefs of this {@link KBase} object belong to. 
     */
    public void setAgentName(Term agent);

	/**
     * Assert a fact (given as a FIPA-SL formula) believed by the owning agent
     * into this belief base. It is very important to understand that asserted
     * facts are necessarily facts <b>believed</b> by the agent, independently
     * from their truth value in the real world.
     * <p>
     * Asserting <code>p</code> actually means that <code>(B ??myself p)</code>
     * is true (where <code>??myself</code> represents the agent owning the belief
     * base), while asserting <code>(not p)</code> actually means that
     * <code>(B ??myself (not p))</code> is true.
     * <br>
     * Consequently, asserting <code>p</code> is equivalent to asserting
     * <code>(B ??myself p)</code> (because in the underlying formal model -
     * see the <a href="http://www.fipa.org/specs/fipa00008">FIPA SL specifications</a>,
     * <code>(B i (B i p))</code> is logically equivalent to <code>(B i p)</code>).
     * </br>
     * </p>
     * <p>
     * The {@link KBase} implementations are expected to maintain a logically
     * consistent set of beliefs. When a fact, which is inconsistent with an
     * already stored belief, is asserted, the implementation must explicitly
     * choose an option that ensures the global consistency (for example, keeping
     * the most recently asserted fact in the base).
     * </p>
     * <p>
     * Depending on their sophistication, the {@link KBase} implementations may
     * not be able to actually store all asserted facts. In this case, the reasoning
     * (or "intellectual") capabilities of the owning agent will simply be
     * considered as limited, which is a very common case. In any case, the
     * asserted facts that cannot be dealt with should not create any inconsistency
     * in the belief base. For example, the implementation provided with the
     * JSA framework does not deal with uncertainty modal operators, so that
     * asserting <code>(U ??myself p)</code> has no effect.  
     * </p>
     * 
     * @param formula the formula to assert.
     */
    public void assertFormula(Formula formula);
    
    /**
     * Retract a fact from the belief base. Actually, this method should be a
     * shortcut for the assertion of <code>(not (B ??myself formula))</code>.
     * In other words, retracting <code>p</code> actually means that
     * <code>(not (B ??myself p))</code> is true.
     * <p>
     * The retracted formula may include meta-references, in this case, all
     * stored facts that match the input formula are retracted. For example,
     * if <code>(age john 25)</code>, <code>(age mary 25)</code> and
     * <code>(age peter 20)</code> are believed by the agent, retracting
     * <code>(age ??person 25)</code> will result in dropping the first two facts
     * from the belief base.
     * </p>
     *  
     * @param formula the formula to retract.
     */
    public void retractFormula(Formula formula);
	
    /**
     * Removes from the belief base all formulas recognized by a given finder.
     * 
     * @param finder a finder to identify formulas to remove from the belief base.
     * @deprecated use {@link #retractFormula(Formula)} instead.
     */
    @Deprecated
	public void removeFormula(Finder finder);
    
    /**
     * Return a list of objects satisfying a given logical description (expressed
     * as an Identifying Referential Expression), which is believed by the agent
     * owning this {@link KBase} instance.
     * <p>
     * Formally, given an IRE <code>??IRE</code> quantified by either
     * <code>iota</code> or <code>any</code>, the method returns a list of one
     * single term ??o such that: <code>(B ??myself (= ??IRE ??o))</code> is true.
     * <br>
     * Given an IRE <code>??IRE</code> quantified by either <code>all</code> or
     * <code>some</code>, the method returns a set ??set of zero or more terms
     * (given as a {@link ListOfTerm} object) such that:
     * <code>(B ??myself (= ??IRE ??set))</code> is true.
     * </br> 
     * </p>
     * If no term satisfy the previous formulas, then the method returns
     * <code>null</code> (which actually means that either the agent does not
     * know the specified objects or there is no such objects).
     * 
     * @return a list of terms containing all the objects satisfying the input
     *         IRE according the the agent's beliefs, or <code>null</code> if
     *         there is no such object in the agent's beliefs.
     * @param expression an Identifying Referential Expression describing one or
     *                   more objects to find within the agent's beliefs.
     **/
    public ListOfTerm queryRef(IdentifyingExpression expression);
    
    /**
     * Return a term satisfying a given logical description (expressed
     * as an Identifying Referential Expression), which is believed by the agent
     * owning this {@link KBase} instance.
     * <p>
     * Formally, given an IRE <code>??IRE</code> quantified by either
     * <code>iota</code> or <code>any</code>, the method returns either a term 
     * ??o such that <code>(B ??myself (= ??IRE ??o))</code> is true
     * or null if <code>(not (exists ?o (B ??myself (= ??IRE ?o))))</code>.
     * <br>
     * Given an IRE <code>??IRE</code> quantified by either <code>all</code> or
     * <code>some</code>, the method returns a set ??set of zero or more terms
     * such that:
     * <code>(B ??myself (= ??IRE ??set))</code> is true.
     * </br> 
     * </p>
     * 
     * @see #queryRef(IdentifyingExpression)
     * @return a term satisfying the input
     *         IRE according the the agent's beliefs, or <code>null</code> if
     *         there is no such object according to the agent's beliefs.
     * @param expression an Identifying Referential Expression describing one or
     *                   more objects to find within the agent's beliefs.
     **/
    public Term queryRefSingleTerm(IdentifyingExpression expression);
    
    public Term queryRef(IdentifyingExpression expression, ArrayList falsityReasons);

    /**
     * Check wether a given fact is believed by the agent owning this {@link KBase}
     * instance.
     * <p>
     * If the fact is not believed by the agent, the method returns <code>null</code>.
     * </p>
     * <p>
     * The queried fact may include meta-references. In this case, the
     * method returns all bindings of these meta-references, for which the
     * corresponding instantiated fact is believed by the agent.
     * These bindings are returned using a {@link QueryResult} instance.
     * If the queried fact include no meta-reference and is actually believed,
     * the returned {@link QueryResult} object is an empty list equal 
     * to QueryResult.KNOWN.
     * </p>
     * For example, if <code>(age john 25)</code>, <code>(age mary 25)</code> and
     * <code>(age peter 20)</code> are believed by the agent, querying for
     * <code>(age ??who 25)</code> will return a list of two
     * {@link jade.semantics.lang.sl.tools.MatchResult} objects containing
     * respectively the bindings (<code>??who</code>=<code>john</code>) and
     * (<code>??who</code>=<code>mary</code>).
     * 
     * @return <code>null</code> (QueryResult.UNKNOWN) if the agent doesn't
     *         know about this fact,
     *         <br>an empty QueryResult (QueryResult.KNOWN) if the 
     *         queried fact is believed by the agent and includes no meta-reference,</br>
     *         <br>a not empty QueryResult, if the queried fact is believed by
     *             the agent and includes meta-reference.</br> 
     * @param formula a fact to retrieve in the belief base (may include meta-
     *                references)
     **/
    public QueryResult query(Formula formula);
    
    public QueryResult query(Formula formula, ArrayList reasons);
    
    /**
     * @param expression a term to evaluate
     * @return a term, which is logically equal to the given expression
     */
    public Term eval(Term expression);
    
    /**
     * Add an observer to this {@link KBase} instance (at the end of the list of
     * observers). Observers make it possible to monitor the truth value of a
     * given fact and trigger some action each time this fact becomes believed
     * by the agent owning the belief base. For example, this mechanism is used
     * by the SIPs that manage the interpretation of the <code>SUBSCRIBE</code>
     * or <code>REQUEST-WHEN*</code> messages.
     *  
     * @param o the observer to add
     */
    public void addObserver(Observer o);
    
    /**
     * Remove from this {@link KBase} instance all the observers that are
     * identified by a given finder.
     * 
     * @param finder a finder that identifies the observers to remove.
     */
    public void removeObserver(Finder finder);
    
    /**
     * Remove from this {@link KBase} instance a given observer.
     * 
     * @param obs the observer to remove. 
     */
    public void removeObserver(Observer obs);
	
	/**
	 * Update the status of all observers concerned by a given formula.
	 * Updating an observer means requesting it to check whether its monitored
	 * formula has become believed by the agent owning the belief base (and
	 * trigger the associated action if needed). An observer is concerned by
	 * a formula if asserting this formula may change the truth value of its
	 * monitored formula.
	 * <p>
	 * A <code>null</code> formula forces all observers to be updated.
	 * </p>
	 * <p>
	 * This operation is needed to trigger the observers even when an assertion
	 * does not lead to an actual assertion in the belief base.
	 * </p>
	 * 
	 * @param formula a formula that has just been asserted.
	 * @see Observer
	 */
	public void updateObservers(Formula formula);
	
    /**
     * Return <code>true</code> if a given pattern of formula is closed on a
     * given domain of values, and <code>false</code> otherwise.
     * <p>
     * A pattern of formula is considered as closed on a set of values if and
     * only if:
     * <ul>
     *     <li>its instantiation with each value is believed by the agent
     *         owning the belief base,</li>
     *     <li>the agent believes that its instantiation with any other value
     *         is necessarily false.</li>
     * </ul>
     * Closed formulas are particularly useful to deal with the <code>iota</code>
     * and the <code>all</code> IRE quantifiers. For example, the belief
     * <code>(B ??myself (= (all ?x (age ?x 25)) (set john mary)))</code>
     * is actually stored in the belief base as the formula
     * <code>(age ??x 25)</code> closed on the domain (<code>john</code>,
     * <code>mary</code>). This makes it possible to retrieve implicit entailments
     * such as <code>(B ??myself (not (age peter 25)))</code> (without the closed
     * formula mechanism, one could only entail
     * <code>(not (B ??myself (age peter 25)))</code>).
     * <p>
     * Consequent to the formal definition of the underlying logical model for
     * agents' beliefs, all patterns of mental attitudes of the agent owning
     * the belief base (that is all patterns logically equivalent to a formula
     * of the form <code>(B ??myself ??PHI)</code>) are necessarily closed. In
     * other words, agents know exactly what are all their mental attitudes, they
     * cannot not be aware of them.
     * </p> 
     * <p>
     * Closed formulas can be defined and undefined using the
     * {@link #addClosedPredicate(Formula)} and {@link #removeClosedPredicate(Finder)}
     * methods.
     * </p>
     * 
     * @param pattern the pattern of closed formula to check.
     * @param values the domain of values to used to check the closure.
     *               <code>null</code> value does not check the values, only
     *               the registration of the pattern as a closed formula.
     * @return <code>true</code> if the pattern is closed on the given domain,
     *         <code>false</code> otherwise.
     */
    public boolean isClosed(Formula pattern, QueryResult values);
	
    /**
     * Define a new pattern of closed formula.
     * <p>
     * If there is no instance of this pattern, which is already believed by
     * the agent owning the belief base, this is equivalent to assert
     * <code>(= (all ??VARS ??pattern) (set))</code>, where <code>??VAR</code>
     * is a sequence of all meta-references occurring in the pattern.
     * </p>
     *
     * @see #isClosed(Formula, QueryResult)
     * @param pattern the pattern of formula to declare as closed.
     */
    public void addClosedPredicate(Formula pattern);

	
    /**
     * Remove the definition of a pattern of closed formula. This pattern is
     * identified by a {@link Finder} object.
     * 
     * @see #isClosed(Formula, QueryResult)
     * @param finder a finder identifying the pattern of closed formula to remove.
     */
    public void removeClosedPredicate(Finder finder);
    
    
	/**
	 * Return the list of believed facts contained in this {@link KBase} instance
	 * as an array of {@link java.lang.String}. It is useful for debugging purposes.
	 * 
	 * @return the list of believed facts in this belief base.
	 */
	public ArrayList toStrings();
    
//	Logger getLogger();
} 
