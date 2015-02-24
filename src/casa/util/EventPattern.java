/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its 
 * documentation for any purpose is hereby granted without fee, provided that the 
 * above copyright notice appear in all copies and that both that copyright notice 
 * and this permission notice appear in supporting documentation.  The  Knowledge 
 * Science Group makes no representations about the suitability of  this software 
 * for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.util;

import casa.Act;
import casa.ML;
import casa.Status;
import casa.agentCom.URLDescriptor;
import casa.event.Event;
import casa.exceptions.IllegalOperationException;
import casa.exceptions.URLDescriptorException;
import casa.interfaces.Describable;
import casa.interfaces.PolicyAgentInterface;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.armedbear.lisp.LispObject;

/**
 * Used to match ({@link #matches(PolicyAgentInterface, Describable)}) {@link Event}s
 * to an abstract pattern represented by the this class.  The abstract pattern is
 * a list of key/value pairs.  Keys are NOT case-sensitive. The match is performed by iterating over each
 * key/value pair in the pattern, and comparing it the corresponding key/value pair
 * in the {@link Describable} parameter of 
 * {@link #matches(PolicyAgentInterface, Describable)}. The comparison may be one
 * of 5 operators:
 * <table border=1>
 * <tr><th>OPERATOR</th><th>EXPLANATION</th></tr>
 * <tr><td>=</td><td>equal</td></tr>
 * <tr><td>!</td><td>not equal</td></tr>
 * <tr><td>&lt;</td><td>isa (as per the agent's ontology)</td></tr>
 * <tr><td>!&lt;</td><td>not isa</td></tr>
 * <tr><td>*</td><td>regular expression comparison (as per {@link java.util.regex.Pattern}) </td></tr>
 * </table>
 * The operators are specified by prefixing them (without spaces) to the first (key) parameter in the 
 * {@link #put(String, Object)} method. If an operator is not specified, the default
 * depends on key parameter:
 * <table border=1>
 * <tr><th>KEY</th><th>OPERATOR</th></tr>
 * <tr><td>performative</td><td>&lt;</td></tr>
 * <tr><td>act</td><td>&lt;</td></tr>
 * <tr><td>type</td><td>&lt;</td></tr>
 * <tr><td>-- all others --</td><td>=</td></tr>
 * </table>
 * In addition, you can specify that a pattern matches only if the cooresponding key is 
 * missing by put([key], "-"), that is, by passing the string "-" as the value of a
 * key/value pair.
 * Examples:
 * <table>
 * <tr><td>put("performative", "request")</td><td>matches if the Describle's performative key is a subtype of "request", according to the agent's ontology.</td></tr>
 * <tr><td>put("&lt;performative", "request")</td><td>matches if the Describle's performative key is a subtype of "request", according to the agent's ontology.</td></tr>
 * <tr><td>put("=performative", "request")</td><td>matches if the Describle's performative key is exactly "request".</td></tr>
 * <tr><td>put("!performative", "request")</td><td>matches if the Describle's performative key is NOT "request".</td></tr>
 * <tr><td>put("!&lt;performative", "request")</td><td>matches if the Describle's performative key is NOT a subtype of "request".</td></tr>
 * <tr><td>put("to", ":6700")</td><td>matches if the Describable's to key is exactly ":6700".</td></tr>
 * <tr><td>put("*to", "[^:]*:6700.*")</td><td>matches if the Describable's to key contains ":6700" and there are no leading colons.</td></tr>
 * <tr><td>put("content", "-")</td><td>matches if the Describable does NOT contain a content key.</td></tr>
 * </table>
 * 
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 */
public class EventPattern {
	public enum Operator {
		equal("="), isa("<"), regex("*"), not("!"), notIsa("!<");
		String printName;
		Operator(String printName){this.printName=printName;}
		@Override public String toString() {return printName;
		}
	};
		
	class Descriptor {
		Operator operator;
		Object object;
		public String toString() {
			return "["+operator.toString()+", "+object+"]";
		}
	}
	TreeMap<String,Descriptor> pattern = new TreeMap<String,Descriptor>();
	
	public EventPattern() {
	}
	
	private String getNormalizedName(String name) {
		if (name==null) name = "?";
		if (name.length()>1) {
			char c = name.charAt(0);
			if (c=='=' || c=='<' || c=='*') return name.substring(1).toLowerCase();
			if (c=='!') {
				if (name.length()>2 && name.charAt(1)=='<') return name.substring(2).toLowerCase();
				return name.substring(1).toLowerCase();
			}
		}
		return name.toLowerCase();
	}
	
	private Operator getOperatorFromName(String name) {
		if (name==null || name.length()==0) return Operator.equal;
		char op = name.charAt(0);
		if (op=='=') return Operator.equal;
		if (op=='<') return Operator.isa;
		if (op=='*') return Operator.regex;
		if (op=='!') {
			if (name.length()>2 && name.charAt(1)=='<') return Operator.notIsa;
			return Operator.not;
		}
		String realName = getNormalizedName(name);
		if (realName.equalsIgnoreCase(name)) { //default PERFORMATIVE, ACT, and TYPE to = (isa); all other default to =
			if ((ML.PERFORMATIVE.equalsIgnoreCase(name) || ML.ACT.equalsIgnoreCase(name) || "TYPE".equalsIgnoreCase(name)))
				return Operator.isa;
			else
				return Operator.equal;
		}
		return Operator.equal;
	}
	
	/**
	 * @param key The key for the pattern (may be prefixed by "=", "<", "!", "!<", or "*")
	 * @param val The value for the key (may be "-" to indicate must-be-missing or null)
	 * @return
	 */
	public Object put(String key, Object val) {
		String realkey = getNormalizedName(key);
		Descriptor d = new Descriptor();
		d.object = val;
		d.operator = getOperatorFromName(key);
		Descriptor old_d = pattern.put(realkey, d);
		return old_d==null?null:old_d.object;
	}
	
	public Object get(String key) {
		Descriptor d = pattern.get(key);
		if (d==null) return null;
		return d.object;
	}

	public Set<String> keySet() {
		return pattern.keySet();
	}
	
	/**
	 * @return the pattern in the form: ":[operator][key] [value] ...", where [operator] may be "<" or "=".
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (String key: pattern.keySet()) {
			Descriptor d = pattern.get(key);
			b.append(':')
			.append(d.operator.toString())
			.append(key)
			.append(' ');
			if (d.object instanceof LispObject)
			  b.append(((LispObject)d.object).writeToString());
			else if (d.object instanceof Act) {
				b.append("(act ");
				for (Iterator<String> i = ((Act)d.object).iterator(); i.hasNext(); ) {
					b.append(i.next());
					if (i.hasNext()) b.append(' ');
				}
				b.append(")");
			}
			else 
				b.append(d.object);
			b.append(' ');
		}
		if (b.length()>1) b.setLength(b.length()-1);
		return b.toString();
	}
		
//	private Transformation transformation=null;
//
//	public void setTransformation(Vector<String> from, Vector<String> to) {
//		transformation = new PerfActTransformation(from, to); 
//	}
//	
//	public void setTransformation(Transformation trans) {
//		transformation = trans;
//	}
	
//  /**
//   * Determines if the MLMessage matches the pattern given in <em>pattern</em>.  By matching,
//   * we mean:
//   * <ul>
//   * <li> if a key appears in <em>pattern</em>, it must appear in the message.
//   * <li> if "act" (ignore case) is a key in <em>pattern</em>, then the act field in the message
//   *   should be a subtype of the value of the act key in <em>pattern</em>.
//   * <li> if any of the values of keys in <em>pattern</em> can be interpreted as a type in the 
//   * agent's ontology, then the field value in the message should be a subtype of the value in 
//   * the corresponding key in <em>pattern</em>.
//   * <li> otherwise, the value in <em>pattern</em> should be "string-equal" to the value
//   * in the corresponding key in the message.
//   * <li> keys in the message that don't appear in <em>pattern</em> are ignored.
//   * </ul>
//   * @param agent an agent who's ontology is used to determine the isa relationship among types 
//   * @param pattern keys-and-values where the keys are expected to appear as keys in the message
//   * @msg the message
//   * and values are checked for a match according to the list above.
//   * @return true iff this message matches the pattern
//   * 
//   * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
//   */
//	public Status matches(PolicyAgentInterface agent, Event e) {
//		//Event event = transformation==null?e:(Event)transformation.transform(e);
//		Event event = (Event)agent.transform(e);
//  	for (String key: pattern.keySet()) {
//  		String val = event.getParameter(key);
//  		Descriptor d = pattern.get(key);
//  		Object obj = d.object;
//  		if (ML.ACT.equalsIgnoreCase(key)) {
//  			if (obj instanceof Act) {
//  				Act thisAct = (obj instanceof Act)?(Act)obj:new Act((String)obj);
//  				Act thatAct = new Act((String)val);
//  				if (!agent.isAAct(thatAct, thisAct))
//  					return new Status(-1,"act "+thatAct+" is not a subtype of act "+thisAct);
//  			}
//  		}
//  		else if ("type".equalsIgnoreCase(key)){
//  			if (!related(agent, event.getEventType(), d.operator, obj.toString())) 
//  				return new Status(-2,"event type "+event.getEventType()+" is not "+d.operator+" "+obj.toString());
//  		}
//  		else if (ML.SENDER.equalsIgnoreCase(key) || ML.RECEIVER.equalsIgnoreCase(key)
//  				     || obj instanceof URLDescriptor) {
//  			try {
//  				URLDescriptor url = new URLDescriptor(val);
//					if (!related(agent, url, d.operator, obj)) 
//						return new Status(-3, "For key "+key+", URL "+url+" is not "+d.operator+" "+obj.toString());
//				} catch (URLDescriptorException e1) {
//					agent.println("error", "EventPattern.matches(): Expected URLDescriptor, got "+val.getClass(), e1);
//				}
//  		}
//  		else { //for all other parameters, check against the message parameters
//  			if (!related(agent, val, d.operator, obj)) 
//					return new Status(-4, "For key "+key+", "+val+" is not "+d.operator+" "+obj.toString());
//  		}
//  	}
//  	return new Status(0,"match");
//  }
	
	/**
	 * 
	 * @param agent
	 * @param descr
	 * @return
	 */
	public Status matches(PolicyAgentInterface agent, Describable descr) {
		//Describable describable = transformation==null?descr:transformation.transform(descr);
		Describable describable = (Describable)agent.transform(descr);
		for (String patternKey: pattern.keySet()) {
			Descriptor patternValueDesc = pattern.get(patternKey);
			Object patternValue = patternValueDesc.object;
			if (patternValue==null || patternValue.equals(org.armedbear.lisp.Lisp.NIL))
				continue;
			String descrVal = describable.getParameter(patternKey);
			if ("type".equalsIgnoreCase(patternKey) && descrVal==null && describable instanceof Event){
				if (!related(agent, ((Event)describable).getEventType(), patternValueDesc.operator, patternValue.toString())) 
					return new Status(-2,"Event type "+((Event)describable).getEventType()+" is not "+patternValueDesc.operator+" "+(patternValue==null?"null":patternValue.toString()));
			}
			boolean patternKeyIsAType;
			try {
				patternKeyIsAType = agent.getOntology().isObject(patternKey);
			} catch (IllegalOperationException e1) {
				patternKeyIsAType = false;
			}
			// Handle the ACT field
			if (ML.ACT.equalsIgnoreCase(patternKey)) {
				Act patternAct = (patternValue instanceof Act)?(Act)patternValue:new Act((String)patternValue);
				if (patternAct.toString().equals("-") || patternAct.size()==0 || patternAct.toString().length()==0) {
					if (descrVal==null || descrVal.equals("-") || descrVal.length()==0)
						continue;
					else
					  return new Status(-4,"Act '"+descrVal+"' exists and is specified as to be missing by '-'");
				}
				else if (descrVal==null && !(patternAct.isEmpty() || patternAct.toString().equalsIgnoreCase("TOP") || patternAct.toString().equals("-") || patternAct.toString().equals(""))) { 
					return new Status(-3, "Act missing in target");
				}
				else {
					Act descrAct = new Act((String)descrVal);
					if (!agent.isAAct(descrAct, patternAct))
						return new Status(-1,"Act '"+descrAct+"' is not a subtype of act "+patternAct);
				}
			}
			// Handle a field that is a type or has a value that is a URLDescriptor
			else if (patternKeyIsAType && agent.isA(patternKey, ML.AGENT) || patternValue instanceof URLDescriptor) {
				try {
					if ("-".equals(patternValue)) {
						if (descrVal==null)
							continue;
						else
						  return new Status(-4,"Key '"+patternKey+"' exists and is specified as to be missing by '-'");
					}
					if (descrVal==null) 
						return new Status(-3, "Key '"+patternKey+"' missing in target");
					URLDescriptor url = URLDescriptor.make(descrVal);
					if (!related(agent, url, Operator.equal, patternValue)) 
						return new Status(-3, "For key "+patternKey+", URL "+url+" is not "+patternValueDesc.operator+" "+patternValue.toString());
				} catch (URLDescriptorException e) {
					assert false;
					return new Status(-2, "For key '"+patternKey+"', "+descrVal+" is not the a URL", e);
				}
			}
			// Handle all other cases
			else {
				if ("-".equals(patternValue)) {
					if (descrVal==null)
						continue;
					else
					  return new Status(-4,"Key '"+patternKey+"' exists and is specified as to be missing by '-'");
				}
				if (descrVal==null) 
					return new Status(-3, "Key '"+patternKey+"' missing in target");
				if (!related(agent, descrVal, patternValueDesc.operator, patternValue))
					return new Status(-1, "For key '"+patternKey+"', "+descrVal+" is not "+patternValueDesc.operator+" "+patternValue.toString());
			}
		}			
		return new Status(0,"match");
	}

	private boolean related(PolicyAgentInterface agent, Object domain, Operator op, Object range) {
		switch (op) {
		case equal:
			if (domain instanceof String && range instanceof String)
				return ((String)domain).equalsIgnoreCase((String)range);
			else
				return domain.equals(range);
		case regex:
			if (domain instanceof String && range instanceof String)
				return domain.toString().matches(range.toString());
			else
				return domain.equals(range);
		case not:
			if (domain instanceof String && range instanceof String)
				return !((String)domain).equalsIgnoreCase((String)range);
			else
				return !domain.equals(range);
		case notIsa:
			return !agent.isA(domain.toString(), range.toString());
		case isa:
		default:
			return agent.isA(domain.toString(), range.toString());
		}
	}

}
