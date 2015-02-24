package casa;

import casa.abcl.CasaLispOperator;
import casa.abcl.ParamsMap;
import casa.ui.AgentUI;
import casa.util.CASAUtil;

import java.text.ParseException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.armedbear.lisp.Environment;

/**
 * <p>Title: CASA Agent Infrastructure</p> <p>Description: </p> PerformDescriptor is a meant to be a simple description 
 * consisting of key/value pairs. It is an extension of  {@link casa.Status} with {@link java.util.TreeMap} 
 * properties added. <p> 
 * 
 * PerformDescriptor is usually used to return information from an agent processing a message.  The policies
 * call callback methods to allow the agent to process the message appropriately (if at all), and use the information
 * returned in the PerformDescriptor to modify behaviour.  For example, if a <em>reply</em> is normally called for, 
 * the type of the reply will be modified depending on the Status object in the returned PerformDescriptor, but the 
 * key/value pairs always override whatever the "normative" key/value pairs in any reply message.
 * <comment> <p>
 * In addition you can add a  {@link casa.Condition}  which will indicate that the associated performance/action
 * should be delayed until the condition is satisfied (note that this is  newer addtion, so it might be neglected
 * by older code).</comment> <p>
 * Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, 
 * modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, 
 * provided that the above copyright notice appear in all copies and that both that copyright notice and this
 * permission notice appear in supporting documentation.  The  Knowledge Science Group makes no representations 
 * about the suitability of  this software for any purpose.  It is provided "as is" without express or implied 
 * warranty.</p> <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author  kremer
 * @version 0.9
 * @see java.util.TreeMap
 * @see  casa.Status
 */

public class PerformDescriptor extends Status implements Map<String,String> {
  /**
	 * This seems odd, but this allows PerformDescriptor to "absorb" a different subtype (eg: {@link StatusObject})
	 * as it's internal structure.
	 */
  private Status status;
	
	private TreeMap<String,String> params = new TreeMap<String,String>();

  //TODO need to add the condition information as per the description above
  
  /**
   * Equivalent to {@link PerformDescriptor#PerformDescriptor(Status) PerformDescriptor(null)}
   */
  public PerformDescriptor() {status=new Status();}

  /**
   * Constructs a new PerformDescriptor.
   * @param status a new Status, if status is null, then it will be taken as new {@link Status#Status(int) Status(0)} (a success status).
   */
  public PerformDescriptor(Status status) {
  	this.status = (status==null?new Status():status);
  }
  
  public PerformDescriptor(int value) {
  	status = new Status(value);
  }

  public PerformDescriptor(int value, String explanation) {
  	status = new Status(value, explanation);
  }

  /**
   * Copy Constructor.
   * @param other another Perform Descriptor, if status is null, then it will be taken as the default Constructor.} (a success status).
   */
  @SuppressWarnings("unchecked")
	public PerformDescriptor(PerformDescriptor other) {
  	super(other);
  	if (other==null) {
  		status = new Status();
  		return;
  	}
  	params = (TreeMap<String, String>)other.params.clone();
  	try {
			status = (Status)other.status.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }
  
  public PerformDescriptor(ParamsMap map) {
  	this();
  	for (String key: map.keySet()) {
  		if ("PD".equalsIgnoreCase(key))
  			continue;
  		Object o = map.getJavaObject(key);
  		String val = o==null?null:o.toString();
  		params.put(key, val);
  	}
  }
  
  @Override
	public Object clone() {
  	Object obj = null;
  	try {
			obj = super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PerformDescriptor ret = (PerformDescriptor)obj;
  	try {
			ret.status = (Status)status.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
  }

  /**
	 * @return  the internal Status object
	 */
  public Status getStatus() {
  	return status;
  	}
  
  /**
	 * @param status  object to become the new internal status object
	 * @return  int new internal status object
	 */
  public PerformDescriptor setStatus(Status status) {
  	this.status = status;
  	return this;
  	}
  
  /**
   * 
   * @param val a value to be used to create a new internal status object
   * @param explanation the explanation to be used to create a new internal status object
   * @return the new internal status object
   */
	public PerformDescriptor setStatus(int val, String explanation) {
  	status.setStatus(val, explanation);
  	return this;
  	}

  @Override
	public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("( ")
    .append(String.valueOf( status.getStatusValue() ));
    String exp = status.getExplanation();
    b.append((exp==null || exp.length()==0)
           ?" \"\""
           :(" "+CASAUtil.toQuotedString(exp)))
    .append(" ( ");
    for (String key: params.keySet()) {
    	String val = params.get(key);
    	if (val==null) val = "";
      b.append(CASAUtil.toQuotedString(key)).append(' ')
      .append(CASAUtil.toQuotedString(val)).append(' ');
    }
    b.append(") ")
    .append(toString_extension())
    .append(" )");
    return b.toString();
  }
  
	@Override
	public void fromString(TokenParser parser) throws Exception {
		if (status==null) status = new Status();
    String token = parser.getNextToken();
    if (!token.equals("("))
      throw new ParseException("expected '('", 0);
    try {
      status.setStatusValue(Integer.parseInt(parser.getNextToken()));
    }
    catch (NumberFormatException e) {
      setStatus(Status.BAD_CONTENT_FIELD, "Expected an integer");
      System.out.println(codeToString());
      e.printStackTrace();
      throw new ParseException("expected an integer", 1);
    }
    token = parser.getNextToken();
    status.setExplanation (token);
    
    token = parser.getNextToken();
    if (!token.equals("("))
      throw new ParseException("expected '('", 0);
    while (true) {
    	String key = parser.getNextToken();
    	if (")".equals(key)) break; // we're done with the list;
    	String val = parser.getNextToken();
    	params.put(key, val);
    }
    fromString_extension(parser);
    //if (!parser.getNextToken().equals(")")) throw new ParseException("Expected ')'",2);
	}

  @SuppressWarnings("unchecked")
	@Override
	public void fromString_extension (TokenParser parser) throws Exception {
    String str = parser.getNextToken ();
    Vector<Object> v = (Vector<Object>)CASAUtil.unserialize(str, null);
    status = (Status)v.elementAt(0);
    params = (TreeMap<String,String>)v.elementAt(1);
  }

  /**
   * overlay this PerformDescriptor over the argument PerformDescriptor conditionally.
   * @param other
   * @param agent the Agent to use in isa comparisons.
   * @return a new PerformDescritor that is the overlay result.
   */
  @SuppressWarnings("unchecked")
	public PerformDescriptor overlay(PerformDescriptor other, TransientAgent agent) {
		PerformDescriptor ret = new PerformDescriptor(other); // copy constructor

		//replace keys from the command line CONDITIONALLY
		for (String key : params.keySet()) {
			if (key.length()>1 && key.charAt(0)=='_' && key.charAt(1)=='_') 
				continue;
			if ("PD".equals(key)) 
				continue;

			Object obj = params.get(key);
			String keyVal = obj==null?null:obj.toString();
			if (keyVal!=null && keyVal.length()==0)
				keyVal = null;
			key = key.toLowerCase();
			if (key.length()>0 && key.charAt(0)=='*') {
				key = key.substring(1);
				if (ret.containsKey(key)) 
					continue;
			}
			if (other!=null) {
				String pdVal = other.get(key);
				if (ret.containsKey(key)) { // we need to decide if the this is permissible: leave it or not
					if (ML.PERFORMATIVE.equals(key)) {
						if (keyVal!=null && agent.isA(pdVal,keyVal)) { // it's OK let it ride if it's subsumed by the suggestion
							continue;
						}
						else {
							if (pdVal!=null && pdVal!=null)
								agent.println("warning", 
										"(PERFORMDESCRIPTOR.OVERLAY PD &allow-other-keys): The performative, \""+pdVal+"\", given by the PD is not a subtype of the parameter :PERFORMATIVE \""+keyVal+"\", using \""+keyVal+"\"");
						}
					}
					if (ML.ACT.equals(key)) {
						if (keyVal!=null && agent.isAAct(pdVal,keyVal))  // it's OK let it ride if it's subsumed by the suggestion
							continue;
						agent.println("warning", 
								"(PERFORMDESCRIPTOR.OVERLAY PD &allow-other-keys): The act, \""+pdVal+"\", given by the PD is not a subtype of the act :PERFORMATIVE \""+keyVal+"\", using \""+keyVal+"\"");
					}
				}
			}
			if (keyVal!=null)
			  ret.put(key, keyVal);
			else
				ret.remove(key);
		}

//		// if the CONTENT field is not filled out, fill it out with the Status object out of the PerformDescriptor
//		if (other!=null && !ret.containsKey(ML.CONTENT)) {
//			if (other.getStatus() instanceof StatusObject<?>)
//				ret.put(ML.CONTENT, CASAUtil.serialize(((StatusObject<?>)other.getStatus()).getObject()));
//			else
//				ret.put(ML.CONTENT, CASAUtil.serialize(other.getStatus()));
//		}

		return ret;
  }

  /**
   * Lisp operator: (PERFORMDESCRIPTOR ...)<br>
   * 
   * Sends the designated response to
   */
  @SuppressWarnings("unused")
  private static final CasaLispOperator PERFORMDESCRIPTOR =	  
	  new CasaLispOperator("PERFORMDESCRIPTOR", "\"!Creates a new PerformDescriptor with the status value if the parameter (def=0) and the fields specified in the keys on the parameter PD's feilds.\" "
			  + "&OPTIONAL (STATUSVALUE 0) \"@java.lang.Integer\" \"!The integer status value of the new PerformDescriptor.\" "
	  		+ "&ALLOW-OTHER-KEYS "
			  , TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
  	{
	  @Override
	  public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
	    
	  	int statVal = (Integer)params.getJavaObject("STATUSVALUE");
      PerformDescriptor ret = new PerformDescriptor(new Status(statVal));
	  	
      Set<String> keys = params.keySet();
      if (keys==null) return new StatusObject<PerformDescriptor>(0,ret);
      for (String key : keys) {
      	if (key.length()>1 && key.charAt(0)=='_' && key.charAt(1)=='_') continue;
      	if ("STATUSVALUE".equals(key)) continue;
        String val = params.getJavaObject(key).toString();
        ret.put(key.toLowerCase (), val);
      }
      return new StatusObject<PerformDescriptor>(0,ret);
	  }
  };

  /**
   * Lisp operator: (PERFORMDESCRIPTOR.OVERLAY PD &allow-other-keys)<br>
   * 
   * Conditionally overlays the {@link PerformDescriptor} PD by the other-keys.
   * "Conditionally" means:
   * <ol>
   * <li> If the key is prefixed by a star (eg: ":*PERFORMATIVE agree") then
   * it will only fill the key in PD if there is no such key in PD.
   * <li> Otherwise, for the :PERFORMATIVE and :ACT keys (only): if the value in the key
   * subsumes the value in PD then the PD key is left; otherwise the PD
   * key is replaced and a warning is issued to the agent's log.
   * <li> Otherwise the key value replaces the value in PD.  
   * </ol>
   */
  @SuppressWarnings("unused")
  private static final CasaLispOperator PERFORMDESCRIPTOR__OVERLAY =	  
  new CasaLispOperator("PERFORMDESCRIPTOR.OVERLAY", "\"!Overlays the fields specified in the keys on the parameter PD's fields.\" "
  		+ "PD \"@casa.PerformDescriptor\" \"!The PerformDescriptor whose fields are to be overlayed.\" "
  		+ "&ALLOW-OTHER-KEYS "
  		, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {

  		PerformDescriptor pd = (PerformDescriptor)params.getJavaObject("PD");
  		PerformDescriptor This = new PerformDescriptor(params);
  		PerformDescriptor ret = This.overlay(pd, agent);
  		if (pd!=null)
  			pd.putAll(ret);
  		return ret;

  	}
  };
  
  /**
   * Lisp operator: (PERFORMDESCRIPTOR.GET-STATUS-VALUE PD ...)<br>
   * 
   * Sends the designated response to
   */
  @SuppressWarnings("unused")
  private static final CasaLispOperator PERFORMDESCRIPTOR__GET_STATUS_VALUE =	  
	  new CasaLispOperator("PERFORMDESCRIPTOR.GET-STATUS-VALUE", "\"!Retrieves this PerformDescriptor's Status's value.\" "
			  + "PD \"@casa.PerformDescriptor\" \"!The PerformDescriptor from which the Status's value will be retrieved.\" "
			  , TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
  	{
	  @Override
	  public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
	  	PerformDescriptor pd = (PerformDescriptor)params.getJavaObject("PD");
	  	Integer ret = (pd==null ? 0 : pd.getStatus().getStatusValue());
      return new StatusObject<Integer>(0,ret);
	  }
  };
  
  /**
   * Lisp operator: (PERFORMDESCRIPTOR.GET-VALUE PD KEY ...)<br>
   * 
   * Sends the designated response to
   */
  @SuppressWarnings("unused")
  private static final CasaLispOperator PERFORMDESCRIPTOR__GET_VALUE =	  
	  new CasaLispOperator("PERFORMDESCRIPTOR.GET-VALUE", "\"!Retrieves this PerformDescriptor's value for KEY or nil if KEY is not defined.\" "
			  + "PD \"@casa.PerformDescriptor\" \"!The PerformDescriptor from which the value will be retrieved.\" "
			  + "KEY \"@java.lang.String\" \"!The key.\" "
			  , TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
  	{
	  @Override
	  public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
	  	PerformDescriptor pd = (PerformDescriptor)params.getJavaObject("PD");
	  	if (pd==null)
	  		return new Status(-1,"PD is null");
	  	String key = (String)params.getJavaObject("KEY");
	  	String ret = pd.get(key);
	  	if (ret==null)
	  		return new Status(-2,"No value for key "+key);
      return new StatusObject<String>(0,"success",ret);
	  }
  };
  
  /**
   * Lisp operator: (PERFORMDESCRIPTOR.GET-STATUS PD ...)<br>
   * 
   * Sends the designated response to
   */
  @SuppressWarnings("unused")
  private static final CasaLispOperator PERFORMDESCRIPTOR__GET_STATUS =	  
	  new CasaLispOperator("PERFORMDESCRIPTOR.GET-STATUS", "\"!Retrieves this PerformDescriptor's Status object.\" "
			  + "PD \"@casa.PerformDescriptor\" \"!The PerformDescriptor from which the Status object will be retrieved.\" "
			  , TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
  	{
	  @Override
	  public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
	  	PerformDescriptor pd = (PerformDescriptor)params.getJavaObject("PD");
	  	Status ret = (pd==null ? new Status(0) : pd.getStatus());
      return new StatusObject<Status>(0,ret); //seems a bit weird, but...
	  }
  };

	@Override
	public void clear() {
		params.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return params.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return params.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		return params.entrySet();
	}

	@Override
	public String get(Object key) {
		return params.get(key);
	}

	@Override
	public boolean isEmpty() {
		return params.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return params.keySet();
	}

	@Override
	public String put(String key, String value) {
		return params.put(key, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		params.putAll(m);
	}

	@Override
	public String remove(Object key) {
		return params.remove(key);
	}

	@Override
	public int size() {
		return params.size();
	}

	@Override
	public Collection<String> values() {
		return params.values();
	}

	@Override
	public String codeToString() {
		return status.codeToString();
	}

	@Override
	public String getExplanation() {
		return status.getExplanation();
	}

	@Override
	public int getStatusValue() {
		return status.getStatusValue();
	}

	@Override
	public void setExplanation(String explanation) {
		status.setExplanation(explanation);
	}

	@Override
	public void setStatusValue(int val) {
		status.setStatusValue(val);
	}
	

}