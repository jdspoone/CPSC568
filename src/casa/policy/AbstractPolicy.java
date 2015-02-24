package casa.policy;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.armedbear.lisp.LispObject;

import casa.Act;
import casa.ML;
import casa.MLMessage;
import casa.PerfActTransformation;
import casa.Status;
import casa.StatusObject;
import casa.event.Event;
import casa.event.EventDescriptor;
import casa.event.MessageEvent;
import casa.interfaces.PolicyAgentInterface;
import casa.interfaces.Transformation;
import casa.util.Tristate;

public interface AbstractPolicy extends Cloneable, Comparable<AbstractPolicy> {

	/**
	 * Returns either the long or short form of the name.<br>
	 * <table>
	 * <tr><td>short form:</td><td><em>name</em></td></tr>
	 * <tr><td>long form: </td><td><em>name</em> ({<em>performative</em>|*}:{<em>act</em>|*})</td></tr>
	 * </table>
	 * @param longVersion <b>true</b> indicates to long form is returned
	 * @return Either a long or short form of the name of the policy
	 */
	public abstract String getName(boolean longVersion);

	/**
	 * @return the unique (to this policy set) id number of this policy (automatically generated).
	 */
	public abstract int getID();

	/**
	 * A ghost policy is a policy that shouldn't be counted as being applied.  For example, 
	 * the global policy that blindly tries to filfill a social commitment to send that message
	 * on receiving or sending that message is a ghost; it doesn't do much (and usually doesn't
	 * do anything at all), and would want to consider it the message unhandled if only
	 * this policy applied.  
	 * @return true if this policy is a ghost.
	 */
	public abstract boolean isGhost();

	/**
	 * Declares this policy a ghost.  It isn't by default.
	 * @see #isGhost()
	 */
	public abstract void setGhost();
	
	/**
	 * Equivalent to getName(false)
	 * @return the short name of the policy
	 */
	public abstract String getName();
	
	public abstract String getType();

  public abstract Status isApplicable(PolicyAgentInterface agent, Event event, Map<String, LispObject> map);

	/**
	 * if there is an unevaluated antecedent, and the evaluation is a {@link EventDescriptor}
	 * then place this {@link EventDescriptor} in the antecedent.  The return
	 * value is not particularly meaningful, but it can be used to avoid performing
	 * the evaluation multiple times as it is guaranteed to be the result of an
	 * evaluation of the unevaluated antecedent if there is one.
	 * @param agent
	 * @param map symbols to be placed in the lisp environment during any evaluation.
	 * @return null if there was no unevaluated antecedent or the object returned 
	 * from the unevaluated antecedent if there was one.
	 */
	public Object updateAntecedent(PolicyAgentInterface agent, Map<String, LispObject> map);
	
  public abstract StatusObject<List<Object>> apply(PolicyAgentInterface agent, Status status, Vector<AbstractPolicy> policyContext, Event event, Map<String, LispObject> map, Object eventInstanceInfo) throws Exception, Throwable;

	public abstract String toString();
	
	public abstract String toString(int indent);
	
	public abstract EventDescriptor getAntecedent();
	
	public void setSourceFile(String source);
	
	public String getSourceFile();

//	public void setTransformation(PerfActTransformation trans);
//	
//	public Transformation getTransformation();

	/**
	 * @return the documentation string for this object if there is one, otherwise null
	 */
	public String getDoc();

	public abstract AbstractPolicy clone();
	
}