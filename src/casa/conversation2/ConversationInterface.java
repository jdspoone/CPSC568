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
package casa.conversation2;

import casa.Act;
import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.ProcessOptions;
import casa.TransientAgent;
import casa.event.Event;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 */
public interface ConversationInterface extends Runnable{
	
  /**
   * Used by consider_*(), verify_*(), perform_*(), release_*(), 
   * conclude_*(), etc-type methods to signal that the action is
   * not yet ready to be executed, and that the method should be
   * called again later. 
   */
  static final public int DEFER_ACTION = 8765;
	
  /**
   * Used by consider_*(), verify_*(), perform_*(), release_*(), 
   * conclude_*(), etc-type methods to signal that the action should
   * be discarded -- i.e.: no action should be taken and the message
   * should be allowed by timeout when the REPLY-BY expires. 
   */
  static final public int DROP_ACTION = 8766;
	
	//public String id = null;
	
	public final static String SERVER = "server";
	public final static String CLIENT = "client";
	
//	public enum Role{
//		SERVER,
//		CLIENT
//	};
	
	//public Role role = null;

//	public final static String CREATED = "created";
//	public final static String RUNNING = "running";
//	public final static String WAITING = "waiting";
//	public final static String TERMINATED = "terminated";
	
	public enum State {
		/**
		 */
		CREATED, 
		/**
		 */
		RUNNING, 
		/**
		 */
		WAITING, 
		/**
		 */
		TERMINATED
	};

	/**
	 */
	//public State state;// = null;

	/**
	 */
	//public TransientAgent agent = null;
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	//public void run();
	
	abstract void init();
	
//This is changed to private as users should just use callHandlerMethod to access methods -- rck
//	/**
//   * Find a method by first looking in the cache, and if it isn't there by deferring 
//   * to {@link #getMethodRobustly(Object, String, Class...)} to find it in <em>object</em>
//   * using reflection.
//   * @param object the Object to look for the method in
//   * @param methodName the name of the method to return
//   * @return the Method found, or null if not found.
//   */
//	public Method findMethod(Object object, String methodName);
	
//This is changed to private as users should use findMethod instead -- rck
//  /**
//	 * This method retrieves a method from <code>this</code> even if it is
//	 * protected. This means that the method may not be callable. If getMethod()
//	 * is used, only public methods are returned.
//   * @param object The object who's method is to be obtained (only used for it's class).
//   * @param methodName The name of the method to return.
//   * @param parameters The parameters that must exist in the returned method.
//	 * 
//	 * @return A Method object with the required parameters, or <code>null</code>
//	 *         if it does not exist.
//	 */
//  public Method getMethodRobustly (Object object, String methodName, Class<?> ... parameters);
	
  //Map<String,Method> methodMap = new TreeMap<String,Method>();
  /**
	 * Calls a method on <code>this</code> with the specified name, passing it
	 * the given MLMesssage, and returning a PerformDescriptor. Throws
	 * NoSuchMethodException if no such method exists.<br>
	 * In the event that the method is not addressed to this agent, we don't
	 * call the method, but we attempt to find a method with the same name concatenated
	 * with "_eavesdrop"; if we can't find such a method, then call
	 * {@link #eavesdrop(MLMessage)} as a catch-all.
	 * 
	 * @param message The message to pass.
	 * @param theMethodName The name of the method to call.
	 * @return The PerformDescriptor that the method returns with the details of the actual method called in the key 'methodCalled'.
	 * @throws NoSuchMethodException If any exception occurs during the call.
	 */
	public PerformDescriptor callHandlerMethod (MLMessage message, String methodName) throws NoSuchMethodException;
	
	/**
	 * This is the catch-all method called when {@link ProcessOptions#observeMessages} 
	 * is set true and the handler method (the regular method concatenated with
	 * "_evesdrop") is not found.  Subclasses should override this to implement
	 * the desired behaviour.  This implementation does nothing but return a
	 * {@link PerformDescriptor} with a {@link #DROP_ACTION} Status, which 
	 * indicates no action should be taken.
	 * @param msg An incoming message that is not addressed to this agent.
	 * @return null
	 */
	public PerformDescriptor eavesdrop(MLMessage msg);
	
  /**
   * This method is called (directly or indirectly) in response to a 
   * {@link ML#REQUEST request}-type message.  
   * This implementation uses the {@link sun.reflect} package to
   * look for a method with the signature consider_*(MLMessage) (where * is the
   * name of the performative in the message) and calls that method if it
   * exists, otherwise it calls {@link #considerDefault(Act, MLMessage)}.
   * If the message is just an {@link ML#INFORM inform} type and the called
   * method returns a performative in it's return of a sub-type of
   * {@link ML#NEGATIVE_REPLY negative-reply} then a {@link ML#REPLY_REQUEST reply}
   * message is constructed and returned to the sender (even though {@link ML#INFORM inform}
   * messages usually don't get a reply).<br>
   * Be sure to look at the <em>see</em> 
   * section for more detail on possible returns.
	 * @param act the Act object of the message (should be the same as that described in the {@link ML#ACT act} field of <em>message</em>).
	 * @param b the {@link ML#REQUEST request}-type performative message that precipitated the call to the method
	 * @return the result as described above.
   * @author kremer
   * @see casa.interfaces.PolicyAgentInterface#consider(casa.Act, casa.MLMessage)
   */
	public PerformDescriptor dispatchMsgHandlerMethod (String consider, Act act, MLMessage message);
	
  public PerformDescriptor dispatchMsgHandlerMethod (String consider, Act act, MLMessage message, boolean contextual);
	
//  /**
//   * This method examines the {@link SocialCommitments} associated with a conversation.
//   * If a conversation has unfulfilled {@link SocialCommitments}, then it returns
//   * true.  Otherwise it returns false.
//   * 
//   * @return boolean
//   * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
//   */
//  public boolean hasUnfulfilledCommitments();

//	/**
//	 * @return  the social commitments
//	 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
//	 */
//	public TreeSet<SocialCommitment> getCommitments();	

	
	
	/**
	 * 
	 * @param event
	 * @return a status with a code of 0 for 
	 */
//	public Status update(Event event) {
//		
//	}
	
	public void waitForEvent(Event event, long timeout);

	/**
	 * @return  the id
	 */
	public String getId();

	/**
	 * @param id  the id to set
	 */
	public void setId(String id);

	/**
	 * @return  the state
	 */
	public State getState();

	/**
	 * @return  the previousState
	 */
	public State getPreviousState();
	
	/**
	 * @return  the id
	 */
	public void setState(State s);
	

	/**
	 * @return  the agent
	 */
	public TransientAgent getAgent();
	
	/**
	 * @return  the role
	 */
//	public Role getRole();
	
}


