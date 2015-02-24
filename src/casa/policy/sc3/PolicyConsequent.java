package casa.policy.sc3;

import casa.Status;
import casa.StatusObject;
import casa.interfaces.PolicyAgentInterface;
import casa.ui.AgentUI;

import java.util.Map;

import org.armedbear.lisp.ControlTransfer;
import org.armedbear.lisp.Cons;
import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.LispObject;

public class PolicyConsequent implements PolicyConsequentInterface {
	
	/**
	 * The lisp code as a String
	 */
	private Cons code;
	
	/**
	 * @param lispCode the lisp code to be stored as this object's {@link #code}.
	 */
	public PolicyConsequent(Cons lispCode) {
		this.code = lispCode;
	}

	/* (non-Javadoc)
	 * @see casa.policy.sc3.PolicyConsequentInterface#process(casa.interfaces.PolicyAgentInterface, casa.ui.AgentUI, java.lang.Object)
	 */
	@Override
	public Object process(PolicyAgentInterface agent, AgentUI ui, Map<String, LispObject> bindings) throws Exception, ControlTransfer {
    Status stat = agent.abclEval(code.writeToString(), bindings, ui);
    if (stat==null) {
    	//throw new Exception("Lisp evaluation concluded with null status:\n"+code+'\n');
    	return null;
    }
    if (stat.getStatusValue()<0) {
    	throw new Exception("Lisp evaluation failed with status "+stat.getStatusValue()+": "+stat.getExplanation()+"\n"+code+'\n');
    }
    if (stat instanceof StatusObject<?>) {
    	Object obj = ((StatusObject<?>)stat).getObject();
    	if (obj instanceof JavaObject) {
    		return ((JavaObject)obj).getObject();
    	} 
    	else {
    		return obj;
    	}
    } 
    else {
    	//throw new Exception("PolicyConsequent.process "+toString()+": Lisp evaluation succeeded with status "+stat.getStatusValue()+", \""+stat.getExplanation()+"\", but no object returned:\n"+code+'\n');
    	return null;
    }
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return form: [code]
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		try {
			return code.writeToString();
		} catch (ControlTransfer e) {
			// TODO Auto-generated catch block
			return "<"+e+">";
		}
	}

}
