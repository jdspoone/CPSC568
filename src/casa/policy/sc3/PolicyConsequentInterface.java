package casa.policy.sc3;

import casa.interfaces.PolicyAgentInterface;
import casa.ui.AgentUI;

import java.util.Map;
import org.armedbear.lisp.ControlTransfer;
import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.LispObject;

public interface PolicyConsequentInterface {

	/**
	 * Executes the {@link #code} as in the context of the agent and returns the object returned from
	 * the lisp execution.  If the returned object is a {@link JavaObject}, the referenced JavaObject 
	 * is returned, otherwise the {@link LispObject} is returned.
	 * @param lispBindings TODO
	 * @return The object returned from the lisp execution or null if it succeeded but no object was returned.
	 * @throws ControlTransfer 
	 * @throws {@link RuntimeException} if the java execution fails, or succeeds but fails to return an object.
	 */
	public abstract Object process(PolicyAgentInterface agent, AgentUI ui,
			Map<String, LispObject> lispBindings) throws Exception, ControlTransfer;

}