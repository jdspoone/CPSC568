package casa;

import casa.agentCom.URLDescriptor;
import casa.exceptions.IllegalOperationException;
import casa.exceptions.MLMessageFormatException;
import casa.exceptions.MLTypeException;
import casa.exceptions.URLDescriptorException;
import casa.interfaces.Describable;
import casa.interfaces.PolicyAgentInterface;
import casa.util.CASAUtil;
import casa.util.Pair;
import casa.util.Trace;

import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.ksg.casa.CASA;


/**
 * Title:        CASA Description:  Base class for XMLMessages and KQMLMessages. Contains the most basic, necessary functions
 * 				 for a markup message in the CASA package. Also contains functionality for interfacing with XML and KQML messages.
 *				 That is, the field markupLanguage determines the type of message (XML/KQML) to be returned by the getNewMLMessage function.
 *
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * 
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @author  <a href="laf@cpsc.ucalgary.ca">Chad La Fournie</a>
 */
public abstract class MLMessage implements Cloneable, Describable {
	
  /**
	 * String used to determine if the message being sent is using XML or strict KQML. Set to KQML by default.
	 */
  private static String markupLanguage;

	static {
		markupLanguage = CASA.getPreference("defaultMarkupLanguage", (String)null, 0);
		if (markupLanguage==null) {
			markupLanguage = ML.KQML;
			CASA.putPreference("defaultMarkupLanguage", ML.KQML, 0);
		}
	}

  /** Stores all of the parameters this message contains (including "performative"). */
  protected TreeMap<String,String> parameters;

  @Override
  public MLMessage clone() {
  	MLMessage ret = null;
  	try {
      ret = (MLMessage)super.clone();
  	} catch(Exception ex) {}
    ret.parameters = new TreeMap<String,String>(parameters);
    return ret;
  }
  
  private void swap(String tag1, String tag2) {
  	String temp1 = getParameter(tag1);
  	String temp2 = getParameter(tag2);
  	if (temp2==null) removeParameter(tag1); else setParameter(tag1,temp2);
  	if (temp1==null) removeParameter(tag2); else setParameter(tag2,temp1);
  }
  
  public MLMessage reverseDirection() {
  	swap(ML.RECEIVER,ML.SENDER);
    String temp = getParameter (ML.REPLY_TO);
    if (temp != null) {
      setParameter (ML.RECEIVER, temp);
      removeParameter (ML.REPLY_TO);
    }
  	return this;
  }

  /**
   * Constructs a new {@link ML#AGREE} reply {@link MLMessage} using the input {@link MLMessage}
   * <em>m</em>.  The new message is a copy of <em>m</em> with the following exceptions:
   * <ul>
   * <li> {@link ML#PERFORMATIVE} is removed -- you have to set it yourself.  (formally set to {@link ML#AGREE})
   * <li> {@link ML#ACT} set to m's {@link ML#PERFORMATIVE} pushed onto m's original {@link ML#ACT}
   * <li> {@link ML#SENDER} set to: if <em>originator<em> is not null then <em>originator</em>
   *      else the value of m's {@link ML#RECEIVER}
   * <li> {@link ML#RECEIVER} set to: if m's {@value ML#REPLY_TO} is not null, then m's 
   *      {@link ML#REPLY_TO} else m's {@link ML#SENDER}
   * <li> {@link ML#IN_REPLY_TO} set to: in m's {@link ML#REPLY_WITH} is set, then m's 
   *      {@link ML#REPLY_WITH} else it is left unfilled (absent)
   * <li> {@link ML#REPLY_WITH} set to: if <em>replyWith</em> is not null, then the value
   *      of <em>replyWith</em> else it is left unfilled (absent) 
   * </ul>
   * In addition, the following fields are ignored (left out of the return message):
   * <ul> 
   * <li> {@link ML#RECIPIENTS}
   * <li> {@link ML#REPLY_TO}
   * <li> {@link ML#FROM}
   * <li> resends
   * <li> methodCalled
   * </ul>
   * @param m The {@link MLMessage} to be used a model to construct the reply to
   * @param replyWith The value to put in the {@link ML#REPLY_WITH} feild if the return
   * @param originator The URL of the agent sending the return
   * @return a new {@link MLMessage} that is a proper {@link ML#AGREE} reply to <em>m</em>
   */
  public static MLMessage constructReplyTo (MLMessage m, String replyWith, URLDescriptor originator) {
    MLMessage reply = MLMessage.getNewMLMessage();
    //copy all the parameters
    for (Enumeration<String> i = m.parameters (); i.hasMoreElements (); ) {
      String s = (String) i.nextElement ();
      if (!(   ML.RECIPIENTS.equals(s) //don't copy certain fields
    		    || ML.REPLY_TO.equals(s)
    		    || ML.FROM.equals(s)
      		  || "resends".equals(s)
      		  || "methodCalled".equals(s)
      		 ))
      reply.setParameter (s, m.getParameter (s));
    }
    
    // performative and act
    //reply.setParameter (ML.PERFORMATIVE, ML.AGREE);
    reply.removeParameter(ML.PERFORMATIVE);
    reply.setParameter(ML.ACT,m.getAct().push(m.getParameter(ML.PERFORMATIVE)).toString());

    // sender and receiver
    String receiver = m.getParameter (ML.SENDER);
    String replyTo = m.getParameter (ML.REPLY_TO);
    if (replyTo != null) {
      receiver = replyTo;
    }
    String sender = originator==null ? (m.getParameter(ML.RECEIVER)) : originator.toString();
    setIf(reply, ML.RECEIVER, receiver);
    setIf(reply, ML.SENDER,   sender);

    //fix up REPLY_WITH and IN_REPLY_TO
    setIf (reply, ML.IN_REPLY_TO, m.getParameter (ML.REPLY_WITH));
    if (replyWith!=null) 
    	reply.setParameter(ML.REPLY_WITH, replyWith);
    else
    	reply.removeParameter(ML.REPLY_WITH);
    return reply;
  }
  
  /**
	 * Attempts to decode the proxy information from the string provided into a
	 * {@link URLDescriptor} and an {@link MLMessage}.
	 * 
	 * @param contents The string containing the encoded proxy information.
	 * @return A pair containing a {@link URLDescriptor} and an {@link MLMessage}
	 *         if extraction was successful; <code>null</code> otherwise.
	 */
	public static Pair<URLDescriptor, MLMessage> extractBasicProxyInformation (String contents) {
	  Pair<String, MLMessage> proxyInfo = extractProxyInformation (contents);
	  
	  if (proxyInfo != null) {
	    URLDescriptor receiver;
	    try {
	      receiver = URLDescriptor.make (proxyInfo.getFirst ());        
	    } catch (URLDescriptorException e) {
	      return null;
	    }
	    
	    return new Pair<URLDescriptor, MLMessage> (receiver, proxyInfo.getSecond ());
	  } else {
	    return null;
	  }
	}

	
	public static MLMessage constructProxyMessage (MLMessage message, URLDescriptor sender, URLDescriptor proxyReciever, String finalRecieverDescription) {
    
    if (message.getParameter(ML.REPLY_TO)==null) message.setParameter (ML.REPLY_TO, sender.toString ());    
    message.setParameter (ML.RECEIVER, finalRecieverDescription);
    message.setParameter(ML.FROM, sender.toString());
    
    MLMessage proxyMessage = MLMessage.getNewMLMessage();
    
    proxyMessage.setParameter (ML.PERFORMATIVE, ML.PROXY);
    proxyMessage.setParameter (ML.ACT, ML.PROXY);
    proxyMessage.setParameter (ML.RECEIVER, proxyReciever.toString (sender));
    
    Object[] contents = {finalRecieverDescription, message};
    
    proxyMessage.setParameter (ML.CONTENT, CASAUtil.serialize (contents));
    
    return proxyMessage;
  }

  public static MLMessage constructBasicProxyMessage (MLMessage message, URLDescriptor sender, URLDescriptor proxyReciever, URLDescriptor finalReciever) {
  	String finalRecieverString = finalReciever.toString (proxyReciever);
  	if (proxyReciever.getDataValue("directed")!=null) finalRecieverString = "+ "+finalRecieverString;
    return constructProxyMessage (message, sender, proxyReciever, finalRecieverString);
  }
  
  /**
   * Attempts to decode the proxy information from the string provided into a
   * {@link String} and an {@link MLMessage}.
   * 
   * @param contents The string containing the encoded proxy information.
   * @return A pair containing a {@link String} and an {@link MLMessage} if
   *         extraction was successful; <code>null</code> otherwise.
   */
  public static Pair<String, MLMessage> extractProxyInformation (String contents) {
    String first;
    MLMessage second;
    try {
      Object[] data = CASAUtil.unserializeArray (contents, null);

      MLMessage templateMessage = (MLMessage) data[1];
      second = templateMessage;

      String receiver = (String) data[0];
      first = receiver;
    } catch (ParseException e1) {
      return null;
    } catch (ClassCastException e2) {
      return null;
    }

    return new Pair<String, MLMessage> (first, second);
  }
  
  /**
   * Attempts to decode the proxy information from the content field into a
   * {@link String} and an {@link MLMessage}.
   * 
    * @return A pair containing a {@link String} and an {@link MLMessage} if
   *         extraction was successful; <code>null</code> otherwise.
   */
  public Pair<String, MLMessage> extractProxyInformation () {
  	return extractProxyInformation(getParameter(ML.CONTENT));
  }

 	/**
   * Attempts to decode the proxy information from the content field into a
   * {@link URLDescriptor} and an {@link MLMessage}.
   * 
   * @param contents The string containing the encoded proxy information.
   * @return A pair containing a {@link URLDescriptor} and an {@link MLMessage}
   *         if extraction was successful; <code>null</code> otherwise.
   */
  public Pair<URLDescriptor, MLMessage> extractBasicProxyInformation() {
  	return extractBasicProxyInformation(getParameter(ML.CONTENT));
  }
  
  /**
   * Sets the parameter of the message only if the given string is not <code>null</code>.
   *
   * @param m The message to conditionally modify.
   * @param key The name of tghe parameter to conditionally change.
   * @param newVal The new value of the parameter.
   */
  private static void setIf (MLMessage m, String key, String newVal) {
    if (newVal != null) {
      m.setParameter (key, newVal);
    }
  }

  /**
   * Constructor
   */
  public MLMessage () {
    parameters = new TreeMap<String,String> (new Comparator<String>(){
  		@Override
			public int compare(String o1, String o2) {
  			return o1.compareToIgnoreCase(o2);
  		}});
  	
  }

  /**
   * Constructs a message from an array of Strings, which is interpreted as key/value
   * pairs.  The even indexes are keys and the odd indexes are values.
   * @param list a String array of keys and values as alternating elements
   * @throws Exception if a key is null or there's an odd number of elements in the array
   */
  public MLMessage(String... list) {
    this();
    setParameters(list);
  }

  /**
   * @return true iff o is a MLMessage and every field of the two messages are {@link #equals(Object)}, EXCEPT the :REPLY-BY field (that is, the :REPLY-BY field is ignored).
   */
  @Override
	public boolean equals (Object o) {
    if (!(o instanceof MLMessage)) return false;
    return equals((MLMessage)o);
  }
  
  /**
   * @param o the "other" message.
   * @return true iff every field of the two messages are {@link #equals(Object)}, EXCEPT the :REPLY-BY field (that is, the :REPLY-BY field is ignored).
   */
  public boolean equals(MLMessage o) {
  	for (String key: parameters.keySet()) {
  		if (!key.equalsIgnoreCase("reply-by")) {
  			Object thisVal = parameters.get(key);
  			Object thatVal = o.parameters.get(key);
  			if (!(thisVal==null?thatVal==null:thisVal.equals(thatVal)))
  				return false;
  		}
  	}
  	return true;
  }

  @Override
	public int hashCode () {
    return parameters.hashCode ();
  }

  /**
	 * Simple accessor function for the markupLanguage field.
	 * @returns  markupLanguage  indicating the markup language to be used - KQML or XML.
	 */
  public static String getMarkupLanguage () {
    return markupLanguage;
  }

  /**
	 * Simple set function to change the markup language to be used.
	 * @param markup  contains the specified language to be used, either XML or  KQML.
	 * @throws MLTypeException   if markup is anything other than "XML" or a registered 
	 * markup language (from preferences or from a detected subtype of {@link MLMessage}.
	 */
  public static void setMarkupLanguage (String markup) throws MLTypeException {
		getNewMLMessageType(markup); //test if we can find the relevant class, throws if not.
    markupLanguage = markup;
  }

  /**
   * Method for creating a new MLMessage depending on the messageType.
   *
   * @param markupLanguage Determines if the MLMessage is an XMLMessage, or a KQMLMessage
   *
   * @return null if the markupLanguage is neither and XMLMessage nor a KQMLMessage.
   *         Else, it will return the specific type of message.
   * @throws IllegalOperationException 
   */
  public static MLMessage getNewMLMessageType (String markupLanguage) throws MLTypeException {
    String ml = markupLanguage;
    if (ml==null) ml = getMarkupLanguage();
    if (ml.equals (ML.XML)) {
      return new XMLMessage ();
    } else if (ml.equals (ML.KQML)) {
      return new KQMLMessage ();
    } else {
    	knownSubclasses = CASAUtil.fillSubclasses(knownSubclasses, MLMessage.class, markupLanguage, "Message");
    	Class<?> cls = knownSubclasses.get(markupLanguage);
    	if (cls!=null) {
    		try {
					Constructor<?> constructor = cls.getConstructor(String[].class);
					return (MLMessage)constructor.newInstance();
				} catch (Throwable e) {
					throw new MLTypeException("Class for "+markupLanguage+" found, but no String... constructor");
				}
    	}
    	else {
				throw new MLTypeException("No class for "+markupLanguage+" found; looking for class "+markupLanguage+"Message.");
    	}
    }
  }
  
  static Map<String, Class<?>> knownSubclasses = null;

  /**
   * Method for creating a new MLMessage depending on the messageType.
   *
   * @param markupLanguage Determines if the MLMessage is an XMLMessage, or a KQMLMessage
   *
   * @return null if the markupLanguage is neither and XMLMessage nor a KQMLMessage.
   *         Else, it will return the specific type of message.
   * @throws IllegalOperationException 
   */
  public static MLMessage getNewMLMessageType (String markupLanguage, String... list) throws MLTypeException {
    String ml = markupLanguage;
    if (ml==null) ml = getMarkupLanguage();
    if (ml.equals (ML.XML)) {
      return new XMLMessage (list);
    } else if (ml.equals (ML.KQML)) {
      return new KQMLMessage (list);
    } else {
    	knownSubclasses = CASAUtil.fillSubclasses(knownSubclasses, MLMessage.class, markupLanguage, "Message");
    	Class<?> cls = knownSubclasses.get(markupLanguage);
    	if (cls!=null) {
    		try {
					Constructor<?> constructor = cls.getConstructor(String[].class);
					return (MLMessage)constructor.newInstance((Object[])list);
				} catch (Throwable e) {
					throw new MLTypeException("Class for "+markupLanguage+" found, but no String... constructor");
				}
    	}
    	else {
				throw new MLTypeException("No class for "+markupLanguage+" found; looking for class "+markupLanguage+"Message.");
    	}
    	
    	
//      ml = getMarkupLanguage();
//      if (ml.equals (ML.XML)) {
//        return new XMLMessage ();
//      } else {
//        return new KQMLMessage ();
//      }
    }
  }
  
  /**
   * Method for creating a new MLMessage depending on the messageType.
   *
   * @param markupLanguage Determines if the MLMessage is an XMLMessage, or a KQMLMessage
   *
   * @return null if the markupLanguage is neither and XMLMessage nor a KQMLMessage.
   *         Else, it will return the specific type of message.
   * @throws IllegalOperationException 
   */
  public static MLMessage getNewMLMessage (String... list) {
    try {
			return getNewMLMessageType(getMarkupLanguage(), list);
		} catch (MLTypeException e) {
			// This should never happen since we check for this condition in setMarkupLanguage()
			CASAUtil.log("error", "Unexpected failure instantiating a default MLMessage, "+getMarkupLanguage()+".", e, true);
		}
    return null;
  }
  
  public void reset () {
    parameters.clear ();
  }

  /**
   * Method for creating a new MLMessage of the default type (see setMarkupLanguage()).
   *
   * @todo modify this function to throw an exception, and not return null
   *
   * @return null if the markupLanguage is neither and XMLMessage nor a KQMLMessage.
   *         Else, it will return the specific type of message.
   */
  public static MLMessage getNewMLMessage () {
    try {
			return getNewMLMessageType(getMarkupLanguage());
		} catch (MLTypeException e) {
			// This should never happen since we check for this condition in setMarkupLanguage()
			CASAUtil.log("error", "Unexpected failure instantiating a default MLMessage, "+getMarkupLanguage()+".", e, true);
		}
    return null;
  }


  /**
   * Sets a parameter in the message. If the parameter has not been defined then
   * there is a fault in the document - likely it was not parsed prior to calling
   * setParameter, so the document was not instantiated.
   * If the parameter has been previously defined it is updated to reflect the
   * value specified.
   *
   * @param parameter name of the parameter to set/add
   * @param value     string value to associate with the parameter
   */
  @Override
	public void setParameter (String parameter,
                            String value) {
    parameters.put (parameter, value==null?"":value);
  }

  /**
   * Adds a parameters to a MLMessage from an array of Strings, which is
   * interpreted as key/value pairs.  The even indexes are keys and the odd
   * indexes are values.
   * @param list a String array of keys and values as alternating elements
   * @throws Exception if a key is null or there's an odd number of elements in the array
   */
  public MLMessage setParameters (String... list) {
    if (list==null) return this;
    for (int i=0, len=list.length; i<len; i+=2) {
      if (list[i] == null) {
      	Trace.log("error", "MLMessage(String[]): Null keys are not allowed at even addresses");
        continue;
      }
      if ((i+1)>len) {
      	Trace.log("error", "MLMessage(String[]): Requires an even number of elements (key/value pairs)");
        break;
      }
      setParameter(list[i], list[i+1]);
    }
    return this;
  }

  public void setParameters (Map<?, ?> list, String performativeConstraint, String actConstraint, PolicyAgentInterface agent)
     throws IllegalArgumentException {
   if (list==null) return;
   Set<?> keys = list.keySet();

   //iterative looking for violations.  We have to do this separately because we
   //don't want to modify the list if it's a bad call.
   for (Iterator<?> i=keys.iterator(); i.hasNext(); ) {
     String key = (String)i.next();
     String value = (String)list.get(key);
     if (performativeConstraint!=null 
    		 && key.equals(ML.PERFORMATIVE) 
    		 && !agent.isA(value,performativeConstraint) 
    		 && !agent.isA(value,ML.NEGATIVE_REPLY)) {
       throw new IllegalArgumentException("MLMessage.setParameters: performative constraint '"+performativeConstraint+"' is not an ancestor of value '"+value+"'");
     }
     if (actConstraint         !=null 
    		 && key.equals(ML.ACT         ) 
    		 && !agent.isAAct(new Act(value),new Act(actConstraint))) {
       throw new IllegalArgumentException("MLMessage.setParameters: act constraint '"+actConstraint+"' is not an ancestor of value '"+value+"'");
     }
   }
   setParameters(list);
  }

  public void setParameters (Map<?, ?> list) {
   if (list==null) return;
   Set<?> keys = list.keySet();
   for (Iterator<?> i=keys.iterator(); i.hasNext(); ) {
     String key = (String)i.next();
     String value = (String)list.get(key);
     setParameter(key,value);
     
//     if (list.get(key) instanceof String)
//    	 setParameter(key, (String)list.get(key));
//     else
//    	 setParameter(key, CASAUtil.serialize(list.get(key)));
   }
  }

  /**
   * Retrieves a parameter of this message.  If the parameter is not found, returns null.
   *
   * @param key String indication which parameter to retrieve.
   *
   * @returns The parameter specified, or null if the parameter is not found.
   */
  @Override
	public String getParameter (String key) {
    return parameters.get (key);
  }

  public Object getParameter (String key, Class<?> cls)
                      throws ParseException {
    String s = parameters.get (key);
    if (s==null) return null;
    return CASAUtil.unserialize(s, null);
  }

  public URLDescriptor getURLParameter (String key)
                      throws ParseException, ClassCastException {
    URLDescriptor ret;
    try {
      ret = URLDescriptor.fromString(new TokenParser(getParameter(key)));
    }
    catch (URLDescriptorException ex) {
      ret = (URLDescriptor)getParameter(key,URLDescriptor.class);
    }
    return ret;
  }

  public URLDescriptor getURLParameter (String key, URLDescriptor defaultValue) {
    String temp = getParameter(key);
    if (temp==null) return null;
    try {return getURLParameter(getParameter(key));}
    catch (Exception ex) {return defaultValue;}
  }

  public Status getStatusParameter (String key)
                      throws Exception {
    Status ret = new Status(); //dummy, so the next lines work
    try {
      ret = (Status) getParameter(key, Status.class);
    }
    catch (Exception ex) {
      try {
        ret.fromString(new TokenParser(getParameter(key)));
      }
      catch (Exception ex1) {
        throw ex;
      }
    }
    return ret;
  }

  public Status getStatusParameter (String key, Status defaultValue) {
    String temp = getParameter(key);
    if (temp==null) return null;
    try {return getStatusParameter(getParameter(key));}
    catch (Exception ex) {return defaultValue;}
  }

  public PerformDescriptor getPerformDescriptorParameter (String key)
                      throws Exception {
    PerformDescriptor ret = new PerformDescriptor(); //dummy, so the next lines work
    try {
      ret = (PerformDescriptor) getParameter(key, PerformDescriptor.class);
    }
    catch (Exception ex) {
      //try {
      //  ret = PerformDescriptor.fromString(new TokenParser(getParameter(key)));
      //}
      //catch (Exception ex1) {
        throw ex;
      //}
    }
    return ret;
  }

  public PerformDescriptor getPerformDescriptorParameter (String key, PerformDescriptor defaultValue) {
    PerformDescriptor ret;
    try {ret = getPerformDescriptorParameter(getParameter(key));}
    catch (Exception ex) {ret = defaultValue;}
    return ret;
  }

  public int getIntParameter (String key, int defaultValue) {
    String temp = getParameter(key);
    if (temp==null) return defaultValue;
    try {return Integer.parseInt(temp);}
    catch (NumberFormatException ex) {return defaultValue;}
  }

  public long getLongParameter (String key, int defaultValue) {
    String temp = getParameter(key);
    if (temp==null) return defaultValue;
    try {return Long.parseLong(temp);}
    catch (NumberFormatException ex) {return defaultValue;}
  }

  /**
   * Removes the given parameter from the message.  If the parameter did not
   * exist, no error is reported.
   *
   * @param key String indicating which parameter to remove.
   */
  public void removeParameter (String key) {
    parameters.remove (key);
  }

  /**
   * Retrieve a list of all parameters specified in this message. This list can
   * be used with the getParameter method to retrieve these parameters' values.
   *
   * @returns An Enumeration of parameters.
   */
  public Enumeration<String> parameters () {
    class EnumWrapper<T> implements Enumeration<T> {
			Iterator<T> it;
    	public EnumWrapper(Iterator<T> iterator) {
				this.it = iterator;
			}
			@Override
			public boolean hasMoreElements() {
				return it.hasNext();
			}
			@Override
			public T nextElement() {
				return it.next();
			}
    }
    return new EnumWrapper<String>(parameters.keySet().iterator());
  }
  
  /**
   * @return the set of all the keys for this Message
   */
  @Override
	public Set<String> keySet() {
  	return parameters.keySet();
  }

  /**
   * Returns a list of all message parameters other than ML.PERFORMATIVE in
   * the correct order.  The correct order is defined as:
   * <ul>
   * <li>ML.ACT</li>
   * <li>ML.SENSE</li>
   * <li>ML.SENDER</li>
   * <li>ML.RECEIVER</li>
   * <li>ML.FROM</li>
   * <li>ML.TO</li>
   * <li>ML.REPLY_BY</li>
   * <li>ML.REPLY_WITH</li>
   * <li>ML.IN_REPLY_TO</li>
   * <li>ML.AGENT</li>
   * <li>ML.ACTOR</li>
   * <li>ML.CONVERSATION_ID</li>
   * <li>ML.LANGUAGE</li>
   * <li>ML.LANGUAGE_VERSION</li>
   * <li>ML.ONTOLOGY</li>
   * <li>ML.ONTOLOGY_VERSION</li>
   * <li>ML.CONTENT</li>
   * <li>ML.SIGNATURE</li>
   * <li>ML.ENCRYPTION_ALGORITHM</li>
   * </ul>
   * Followed by all other parameters in lexigraphical order.
   *
   * @returns A Vector containing the list of all message parameters other than
   * ML.PERFORMATIVE in the correct order.
   * @see MLMessage.toString()
   * @see xmlmessage.dtd
   */
  public Vector<String> getSortedParameterList () {
    Vector<String> parameters = new Vector<String> ();
    Vector<String> sortedParameters = new Vector<String> ();

    for (Enumeration<String> e = this.parameters () ; e.hasMoreElements() ;) {
      parameters.add(e.nextElement());
    }
    
    parameters.remove (ML.PERFORMATIVE);

    conditionalMove (parameters, sortedParameters, ML.ACT);
    conditionalMove (parameters, sortedParameters, ML.SENSE);
    conditionalMove (parameters, sortedParameters, ML.SENDER);
    conditionalMove (parameters, sortedParameters, ML.RECEIVER);
    conditionalMove (parameters, sortedParameters, ML.REPLY_TO);
//  conditionalMove (parameters, sortedParameters, ML.TO);
    conditionalMove (parameters, sortedParameters, ML.FROM);
    conditionalMove (parameters, sortedParameters, ML.CD);
    conditionalMove (parameters, sortedParameters, ML.RECIPIENTS);
    conditionalMove (parameters, sortedParameters, ML.REPLY_BY);
    conditionalMove (parameters, sortedParameters, ML.REPLY_WITH);
    conditionalMove (parameters, sortedParameters, ML.IN_REPLY_TO);
    conditionalMove (parameters, sortedParameters, ML.CONVERSATION_ID);
    conditionalMove (parameters, sortedParameters, ML.AGENT);
    conditionalMove (parameters, sortedParameters, ML.ACTOR);
    conditionalMove (parameters, sortedParameters, ML.LANGUAGE);
    conditionalMove (parameters, sortedParameters, ML.LANGUAGE_VERSION);
    conditionalMove (parameters, sortedParameters, ML.ONTOLOGY);
    conditionalMove (parameters, sortedParameters, ML.ONTOLOGY_VERSION);
    conditionalMove (parameters, sortedParameters, ML.CONTENT);
    conditionalMove (parameters, sortedParameters, ML.SIGNATURE);
    conditionalMove (parameters, sortedParameters, ML.ENCRYPTION_ALGORITHM);

    sortedParameters.addAll (new TreeSet<String> (parameters));

    return new Vector<String> (sortedParameters);
  } // getSortedParameterList

  /**
   * If the <code>parameter</code> given exists in <code>parameters</code>,
   * move it from <code>parameters</code> to <code>sortedParameters</code>.
   *
   * @param parameters The <code>Vector<code> to move the
   * <code>parameter</code> from.
   * @param sortedParameters The <code>Vector</code> to move the
   * <code>parameter</code> to if it was in <code>parameters</code>.
   * @param parameter The <code>String</code> that is to be moved from
   * <code>parameters</code> to <code>sortedParameters</code>.
   */
  private <T> void conditionalMove (Vector<T> parameters, Vector<T> sortedParameters,
                                T parameter) {
    if (parameters.contains (parameter)) {
      parameters.remove (parameter);
      sortedParameters.add (parameter);
    }
  }

  public static String longToTextDate(long val) {
    return CASAUtil.getDateAsString(val, "yyyy.MM.dd HH:mm:ss.SSS z");
  }

  /**
   * @Deprecated
   * @see #toString(boolean)
   */
  public String displayString() {
    StringBuffer buffer = new StringBuffer();

    buffer.append( "( " )
          .append( getParameter(ML.PERFORMATIVE) );

    for(Enumeration<?> e = getSortedParameterList().elements(); e.hasMoreElements(); ) {
        String key   = (String) e.nextElement(),
               value = getParameter( key );

        if (!key.equals(ML.PERFORMATIVE)) {
          if (key.equals(ML.REPLY_BY)) {
            long timeout = Long.parseLong(value);
            if (timeout != ML.TIMEOUT_NEVER) {
              value = longToTextDate(timeout);
            } else {
              value = "Never";
            }
          }
          buffer.append("\n :")
                .append (key)
                .append( " " )
                .append( TokenParser.makeFit( value ) );
        }
    }
    buffer.append("\n)");

    return buffer.toString();

  }

  /**
   * Abstract Methods
   */
  @Override
	public String toString () {
    return toString (false);
  }

  public abstract String toString (boolean prettyPrint);
  
  /**
   * Utility method to returns an appropriate field value for the given key and value for use
   * with pretty print format.  At present, this method will turn a <em>timeout</em> field in long
   * format into the default string representation of a date (dow mon dd hh:mm:ss zzz yyyy).
   * @param key
   * @param value
   * @return
   */
  public static String prettyfy(final String key, final String value) {
  	String ret = value.trim();
  	if (ret.length()>0) {
      try {
				if (ML.REPLY_BY.equals(key)) {
				  ret = new Date(Long.parseLong(ret)).toString();
				}
			} catch (Throwable e) {
				//DEBUG.PRINT(e);
			}
  	}
    return ret;
  }

  public abstract void fromStringLocal(String source) throws MLMessageFormatException;

//  public static MLMessage fromString(String source) {
//  	
//  }

  public static MLMessage fromString (String text) throws MLMessageFormatException {
  	MLMessage tempMessage = null;
  	try {
  		tempMessage = MLMessage.getNewMLMessageType(MLMessage.getMarkupLanguage());
  		tempMessage.fromStringLocal(text);
  	}
  	catch (MLTypeException ex1) {
  		if (knownSubclasses==null) {
  			knownSubclasses = CASAUtil.fillSubclasses(null, MLMessage.class, null, "Message");
  		}
  		if (knownSubclasses!=null) {
  			for (String name: knownSubclasses.keySet()) {
  				try {
  					tempMessage = MLMessage.getNewMLMessageType(name);
  					tempMessage.fromStringLocal(text);
  				}
  				catch (MLTypeException ex2) {
  					CASAUtil.log("error", "Message is invalid: '" + text + "'", ex1, true);
  					throw new MLMessageFormatException(ex2.getMessage());
  				}
  			}
  		}
  	}
  	return tempMessage;
  }

  // accessor methods - necessary because of the slight difference between these parameters for XML/KQML.

  /**
   * Returns a {@link casa.channels.URLDescriptor URLDescriptor} from the REPLY-TO field; but if the REPLY-TO field is empty or missing
   * returns the value from the SENDER field. 
   * @return a URLDescriptor constructed from either the FROM field or the SENDER field 
   * @throws URLDescriptorException if a URLDescriptor cannot be constructed from the one in the message
   * @see URLDescriptor
   * @see URLDescriptorException
   */
  public URLDescriptor getFrom() throws URLDescriptorException {
    URLDescriptor url = URLDescriptor.make(getFromString());
    return url;
  }
  
  /**
   * Returns the contents of the REPLY-TO field; but if the REPLY-TO field is empty or missing
   * returns the value from the SENDER field. 
   * @return the contents from either the FROM field or the SENDER field 
   */
  public String getFromString() {
  	String fromString = getParameter (ML.REPLY_TO);
  	if (fromString == null || fromString.length()==0)
  		fromString = getParameter (ML.SENDER);
  	return fromString;
  }
  
  /**
   * Returns the contents of the REPLY-TO field; but if the REPLY-TO field is empty or missing
   * returns the value from the SENDER field. 
   * @return a URLDescriptor constructed from either the REPLY-TO field or the SENDER field 
   * @throws URLDescriptorException if a URLDescriptor cannot be constructed from the one in the message
   * @see URLDescriptor
   * @see URLDescriptorException
   * 
   * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
   */
  public URLDescriptor getReplyTo() throws URLDescriptorException {
  	String fromString = getParameter (ML.REPLY_TO);
  	if (fromString == null)
  		fromString = getParameter (ML.SENDER);
  	return URLDescriptor.make(fromString);
  }
  
  /**
   * Returns a {@link casa.channels.URLDescriptor URLDescriptor} from the TO field; but if the TO field is empty or missing or contains a '*'
   * returns the value from the RECIEVER field. 
   * @return a URLDescriptor constructed from either the TO field or the RECIEVER field 
   * @throws URLDescriptorException if a URLDescriptor cannot be constructed from the one in the message
   * @see URLDescriptor
   * @see URLDescriptorException
   */
  public URLDescriptor getTo() throws URLDescriptorException {
    String toString = getParameter (ML.RECEIVER);
    URLDescriptor url = URLDescriptor.make(toString);
  	return url;
  }
  
  /**
   * Returns a {@link casa.channels.URLDescriptor URLDescriptor} from the SENDER field. 
   * @return a URLDescriptor constructed from the SENDER field 
   * @throws URLDescriptorException if a URLDescriptor cannot be constructed from the one in the message
   * @see URLDescriptor
   * @see URLDescriptorException
   */
  public URLDescriptor getSender() throws URLDescriptorException {
    String fromString = getParameter (ML.SENDER);
    URLDescriptor url = URLDescriptor.make(fromString);
  	return url;
  }
  
  /**
   * Returns a {@link casa.channels.URLDescriptor URLDescriptor} from the RECEIVER field. 
   * @return a URLDescriptor constructed from the RECIEVER field 
   * @throws URLDescriptorException if a URLDescriptor cannot be constructed from the one in the message
   * @see URLDescriptor
   * @see URLDescriptorException
   */
  public URLDescriptor getReceiver() throws URLDescriptorException {
    String fromString = getParameter (ML.RECEIVER);
    URLDescriptor url = URLDescriptor.make(fromString);
  	return url;
  }
  
  public enum Languages {
    CASA,
    TEXT,
    FIPA_SL
  }

  public Object getContent() throws ParseException, ClassNotFoundException {
    Languages lang;
  	String language = getParameter(ML.LANGUAGE);
  	String content = getParameter(ML.CONTENT);
  	if (content==null || content.length()==0)
  		return null;
  	String constraint = null;
  	
  	//casa
  	if (language==null || language.length()==0)
  		lang = Languages.CASA;
  	else if (language.length()>3 && language.substring(0, 4).equalsIgnoreCase("casa")) {
  		lang = Languages.CASA;
  		if (language.length()>4 && (language.charAt(4)!='.' || language.length()==5))
  			throw new ParseException("Bad :language field: "+language, 0);
  		constraint = language.substring(5);
  		if ("*".equals(constraint))
  			constraint = null;
  	}
  	else if (language.equalsIgnoreCase("text")) {
  		lang = Languages.TEXT;
  	}
  	else if (language.equalsIgnoreCase(ML.FIPA_SL)) {
  		lang = Languages.FIPA_SL;
  	}
  	else {
			throw new ParseException("Unknown language: "+language, 0);
  	}
  	
  	switch (lang) {
  	case CASA:
  		Object ret = CASAUtil.unserialize(content, null);
  		if (constraint==null)
  			return ret;
  		Class<?> cls = Class.forName(constraint);
  		if (cls.isAssignableFrom(ret.getClass())) 
  			return ret;
  		throw new ClassCastException("Can't cast object of type "+ret.getClass().getName()+" to type "+cls.getName());
  	case TEXT:
  		return CASAUtil.fromQuotedString(content);
  	case FIPA_SL:
//  		try {
//				return SLParser.getParser().parseTerm(content, true);//SL.term(content);//CASAUtil.fromQuotedString(content));
//  		} catch (Throwable e) {
//  			try {
//  				return SLParser.getParser().parseFormula(content, true);//SL.formula(content);
//  			}
//  			catch (Throwable e1) {
//    			try {
//    				return SLParser.getParser().parseContent(content, true);//SL.formula(content);
//    			}
//    			catch (Throwable e2) {
//    			e1.initCause(e);
//  				e2.initCause(e1);
//  				ParseException e3 = new ParseException("MLMessage.getContent(): Could not parse expression '"+content+"'",0);
//  				e3.initCause(e2);
//  				throw e3;
//    			}
//  			}
//			}
  		
  		return casa.jade.Util.parseExpression(content);
     default:
			throw new ParseException("Unknown language: "+language, 0);
  	}
  }
  
  public void setContent(Object object, Languages language) {
  	if (object==null)
  		return;
  	if (language==null)
  		language = Languages.CASA;
  	
  	switch (language) {
  	case CASA:
  		if (object instanceof Map<?,?> || object instanceof Collection<?> || object instanceof Object[])
    		setParameter(ML.LANGUAGE, "casa.*");
  		else
  		  setParameter(ML.LANGUAGE, "casa."+object.getClass().getName());
  		setParameter(ML.LANGUAGE_VERSION, "1.0");
  		setParameter(ML.CONTENT, CASAUtil.serialize(object));
  		break;
  	case TEXT:
  		setParameter(ML.LANGUAGE, "text");
  		setParameter(ML.LANGUAGE_VERSION, "1.0");
  		setParameter(ML.CONTENT, CASAUtil.toQuotedString(object));
  		break;
  	case FIPA_SL:
  		setParameter(ML.LANGUAGE, ML.FIPA_SL);
  		setParameter(ML.LANGUAGE_VERSION, "1.0");
  		setParameter(ML.CONTENT, (object instanceof URLDescriptor)?((URLDescriptor)object).toStringAgentIdentifier(false):CASAUtil.toQuotedString(object));
  		break;
  	default:
  	}
  }
  
  
  /**
   * Returns the value from the reply-by field of the message as a long.
   * @return the value of the reply-by field as a long; if the value can't be parsed or is missing or empty, returns 0.
   * @see #getTimeout(AbstractProcess)
   */
  public long getTimeout() {
  	String sVal = getParameter(ML.REPLY_BY);
  	if (sVal!=null) {
  		try {
  			return Long.parseLong(getParameter(sVal));
  		}
  		catch (NumberFormatException ex) {
  		}
  	}
		return 0L;
  }

  /**
   * Returns the value from the reply-by field of the message as a long.
   * @param agent
   * @return the value of the reply-by field as a long; if the value can't be parsed or is missing or empty, returns the agent's default timeout (adjusted to the current time).
   * @see #getTimeout()
   */
  public long getTimeout(AbstractProcess agent) {
  	String sVal = getParameter(ML.REPLY_BY);
  	if (sVal!=null) {
  		try {
  			return Long.parseLong(getParameter(sVal));
  		}
  		catch (NumberFormatException ex) {
  		}
  	}
		return agent.options.defaultTimeout + System.currentTimeMillis();
  }

  /**
   * Returns the value from the conversation-id field as a string
   * 
   * @return conversation-id
   * 
   * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
   * @date 2010-1-11
   */
  public String getConversationID(){
	  return getParameter(ML.CONVERSATION_ID);
  }
  
  
  /**
   * Returns the value from the act field of the message as an Act object.
   * @return the value of the act field as a Act object.
   */
  public Act getAct() {
    return new Act(getParameter(ML.ACT));
  }

  /**
   * @param the new act to "push" onto the existing act
   * @return a new act that is the result of pushing the new act onto the existing act
   */
  public Act pushAct(String token) {
    Act act = getAct();
    act.push(token);
    setParameter(ML.ACT,act.toString());
    return act;
  }

  /**
   * Pops off an act
   * @return The act that was popped off
   */
  public String popAct() {
    Act act = getAct();
    String ret = act.peek();
    setParameter(ML.ACT,act.toString());
    return ret;
  }

  /**
   * @return the top of the act "stack"
   */
  public String peekAct() {
    return getAct().peek();
  }
  
  public boolean isReplyTo(MLMessage original){
  	return (original == null
  			    || original.getParameter (ML.REPLY_WITH) == null
  			    || this.getParameter (ML.IN_REPLY_TO) == null
  			    || original.getParameter (ML.REPLY_WITH).equals
  			        (this.getParameter (ML.IN_REPLY_TO)));
  }
  
  public boolean hasPriority() {
	  return getParameter(ML.PRIORITY) != null;
  }
  
  public int getPriority() {
	  return Integer.parseInt(getParameter(ML.PRIORITY));
  }
  
  public void setPriority(int priority){
	  setParameter(ML.PRIORITY, Integer.toString(priority));
  }
  
  /**
   * Determines if this this mask is a broadcast message (designated
   * to all possible recipients, i.e. "*").
   * @return whether this is a broadcast message
   */
  public boolean isBroadcast() {
		String recipients = getParameter(ML.RECIPIENTS);
		if (recipients!=null && recipients.equals("*")) return true;
		String receiver = getParameter(ML.RECEIVER);
		return receiver!=null && receiver.equals("*");
  }
  
  public boolean isAddressedTo(URLDescriptor url) {
  	String recipients = getParameter(ML.RECIPIENTS);
  	if (recipients!=null) {
  		// if it's a broadcast it's to me
  		if (recipients.equals("*")) return true;
  	  
      //scan the recipients list to see if I'm in it
      TokenParser parser = new TokenParser(recipients);
      if (!parser.getNextToken().equals("+")) { // check for whisper mode (+syntax)
        parser.putback();
      }
      // scan through a space-separated list of URLs and put only those that are members into 'tempList'
      URLDescriptor listsURL;
      do {
        try {
          listsURL = URLDescriptor.fromString(parser);
      		// if I'm in the recipients list it's for me.
          if (listsURL!= null) {
          	if (listsURL.equals(url)) return true;
          }
          
        }
        catch (URLDescriptorException ex1) {
          listsURL = null;
        }
        

      } while (listsURL != null);

  	// if I'm NOT in the recipients list it's NOT for me.
 		return false;
  	}
  	
  	return true;
  }
  
//  private static void recordType(Ontology ontology, String type, String... superType) {
//  	try {
//			ontology.add(type,superType);
//		} catch (Exception e) {
//	    DEBUG.DISPLAY(e);
//		}
//  }
  

  

} // MLMessage