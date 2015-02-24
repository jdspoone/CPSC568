package casa.service;

import casa.Agent;
import casa.ML;
import casa.MLMessage;
import casa.Status;
import casa.TransientAgent;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
//import casa.conversation.JoinCD.ServiceAgent.JoinCDServiceAgentProposeDischarge;
//import casa.conversation.JoinCD.ServiceAgent.JoinCDServiceAgentRequestComposite;
import casa.event.Event;
import casa.event.MessageEvent;
import casa.exceptions.IllegalOperationException;
import casa.exceptions.URLDescriptorException;
import casa.ui.AgentUI;


/**
 * A ServiceAgent offers some kind of service to other agents.<p> The following 
 * table describes a conversation when the ServiceAgent registers its service: 
 * <table border=1> 
 * <tr> <th>ServiceAgent</th> <th>LAC</th> <th>YellowPages</th> </tr> 
 * <tr> <td>:performative request<br> :act lookup-agent-type<br> :to LAC<br> :language ??<br> :content casa/YellowPages </td> <td></td><td></td> </tr> 
 * <tr> <td></td> <td>:performative reply<br> :act lookup-agent-type<br> :to ServiceAgent<br> :language casa.agentCom.URLDescriptor*<br> :content {URLDescriptor} </td> <td></td> </tr> 
 * <tr> <td>:performative request<br> :act advertise<br> :to YellowPages<br> :language casa.AdvertisementDescriptor<br> :content [service-name]: &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;properties&gt; { &lt;[property-name] propertyType="[propety-type]"&gt; [value] &lt;/[property-name]&gt; }* &lt;/properties&gt; </td> <td></td><td></td> </tr> <tr> <td></td><td></td> <td>:performative reply<br> :act advertise<br> :to ServiceAgent<br> :language casa.Status<br> :content [status] </td> </tr> </table> THe following table describes a conversation when a client agent requests a service set (in the form of menus) from a ServiceAgent: <table border=1> <tr> <th>client-agent</th> <th>ServiceAgent</th> </tr> <tr> <td>:performative request<br> :act get-menu-commands<br> :to ServiceAgent<br> </td> <td></td> </tr> <tr> <td></td> <td>:performative reply<br> :act get-menu-commands<br> :to client-agent<br> :language menu command<br> :content { "^" object-type } [menu-item] { "|" [menu-item>] }* { "(" [parameter-descriptor] { "," [parameter-descriptor] }* ")" } <hr>where:<br> object-type ::= <em>fully qualified class name</em><br> menu-item ::= <em>non-whitespace string</em><br> parameter-descriptor ::= type parameter-name<br> type ::= <em>fully qualified class name</em> | "int" | ...<br> parameter-name ::= <em>non-whitespace string</em><br> </td> </tr> <tr><td colspan="2">...</td></tr> <tr> <td>:performative inform<br> :act gui-action<br> :to ServiceAgent<br> :language menu command<br> :content { "^" object-id } [menu-item] { "|" [menu-item>] }* { "(" [parameter-value] { "," [parameter-value] }* ")" } </td> <td></td> </tr> </table> <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public abstract class ServiceAgent extends Agent {

  public ServiceAgent(ParamsMap params, AgentUI ui) throws Exception {
    super(params, ui);
    
//    addConversationSupported(ML.JOIN_CD, JoinCDServiceAgentRequestComposite.class, true);
  }

  /**
	 */
  ServiceDescriptor[] serviceDescriptors = getServiceDescriptors();
  
  public int getServiceDescriptorsLength(){
  	return serviceDescriptors.length;
  }
  public void executeServiceDescriptor(int index, URLDescriptor cd){
  	serviceDescriptors[index].offerCommand.execute(cd, "*");
  }

  /**
	 * @return
	 */
  protected abstract ServiceDescriptor[] getServiceDescriptors();

    /**
     * Handles join-cd replies from a CD.  This method overrides the super method
     * but first calls the super method.  It then broadcasts it's offer of service
     * to the members of the CD.  The expected format is:
     * <table border="1" bgcolor="orange" cellpadding="3">
     * <tr><td><a href="doc-files/performatives.gif">performative</a> </td><td>reply</td></tr>
     * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>join.cd</td></tr>
     * <tr><td>language     </td><td><a href="doc-files/contentLanguages.html#casa.Status">casa.Status</a></td></tr>
     * <tr><td>content      </td><td><em>a {@link Status} object</em></td></tr>
     * </table>
     * or standard error replies.     *
     * @param msg the incoming reply message
     * @return the Status object as described above. 0 for success.
     */
//    @Override
//    public PerformDescriptor release_join_cd (MLMessage msg) {
//      in("ServiceAgent.release_join_cd");
//      PerformDescriptor stat = super.release_join_cd(msg);
//
//      if (stat.getStatusValue()==0) {
//        try {
//          URLDescriptor cd = msg.getFrom ();
//          for (int i = 0, end = serviceDescriptors.length; i < end; i++) {
//            serviceDescriptors[i].offerCommand.execute(cd, "*");
//          }
//        }
//        catch (URLDescriptorException ex) {
//          println("error", "unexpeced bad URL in joinCD reply", ex);
//        }
//      }
//
//      out("ServiceAgent.release_join_cd");
//      return stat;
//    }

    /**
     * Handles observe.membership subscribe messages by first calling the
     * super method, and then sending this objects service offer to the newly
     * joined agent.<br>
     * The handled messages are of the form:
     * <table border="1" bgcolor="orange" cellpadding="3">
     * <tr><td><a href="doc-files/performatives.gif">performative</a> </td><td>notify</td></tr>
     * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>join.cd | withdraw.cd</td></tr>
     * <tr><td>language     </td><td><a href="doc-files/contentLanguages.html#casa.agentCom.URLDescriptor">casa.agentCom.URLDescriptor</a></td></tr>
     * <tr><td>content      </td><td><em>a {@link URLDescriptor} object</em></td></tr>
     * </table>
     * or standard error replies.
     * @param msg the incoming message
     * @return A Status descibing the success of the operation
     */
    public Status handleReplys_observeMembership (MLMessage msg) {
    	in("SerrviceAgent.doCDObserveMembership");
    	String language = msg.getParameter(ML.LANGUAGE);
    	String act = msg.getParameter (ML.ACT);

    	if (isAPerformative(msg, ML.INFORM)) {
    		try {
    			if (language.equals(URLDescriptor.class.getName())) {
    				URLDescriptor cd = msg.getFrom ();

    				if (isA(act, ML.JOIN_CD)) {
    					for (int i = 0, end = serviceDescriptors.length; i < end; i++) {
    						serviceDescriptors[i].offerCommand.execute(cd, "*");
    					}
    				}
    				else if (isA(act, ML.WITHDRAW_CD)) {
    				}

    			}
    			else {
    				out("SerrviceAgent.doCDObserveMembership");
    				throw new URLDescriptorException(
    				"Language does not equal 'URLDescriptor.class.getName ()'");
    			}
    		}
    		catch (URLDescriptorException ex) {
    			String m = "SerrviceAgent.handleReplys_CDGetNewMember: mangled or missing .content in messasge: \n" +
    			msg.toString(true);
    			println("warning", m, ex);
    			out("SerrviceAgent.doCDObserveMembership");
    			return new Status( -3, m);
    		}
    	}
    	else {
    		out("SerrviceAgent.doCDObserveMembership");
    		return handleErrorReplies(msg,
    		"Request to be notified of new CD members");
    	}

    	out("SerrviceAgent.doCDObserveMembership");
    	return new Status (0);
    }

    @Override
    public void handleEvent(Event event) {
      super.handleEvent(event); // have super handle message.
      if (event instanceof MessageEvent) {
      	MLMessage msg = ((MessageEvent)event).getMessage();
      //if (status.getStatus() > 0) { //super didn't handle it
        // try and interpret the message
        String performative = msg.getParameter(ML.PERFORMATIVE);
        //String act = msg.getParameter(ML.ACT);
        for (int i = 0, end=serviceDescriptors.length; i<end; i++) {
          try {
						if ( ontology.isa(performative,serviceDescriptors[i].expectedPerformative) /*&&
						     isAAct(act,serviceDescriptors[i].expectedAct)*/) {
//						    status = serviceDescriptors[i].serviceCommand.execute(msg);
						}
					} catch (IllegalOperationException e) {
					}
        }
      //}
      }
//      return status == null ? new Status(1) : status;
    }

    //protected abstract Status handleGUIActionRequest (MLMessage message);

    protected interface OffersCommand {
      public void execute(URLDescriptor cd, String to);
    }

    protected interface ServiceCommand {
      public Status execute(MLMessage msg);
    }

    /**
		 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
		 */
    protected class ServiceDescriptor {
      public String expectedPerformative = null;
      public String expectedAct = null;
      /**
			 */
      public OffersCommand offerCommand = null;
      /**
			 */
      public ServiceCommand serviceCommand = null;
      public ServiceDescriptor(String expectedPerformative, String expectedAct,
                        OffersCommand offerCommand, ServiceCommand serviceCommand) {
        this.expectedPerformative = expectedPerformative;
        this.expectedAct = expectedAct;
        this.offerCommand = offerCommand;
        this.serviceCommand = serviceCommand;
      }
    }

//  	/**
//     * Lisp operator: make-join-cd-service-agent-request-conversation<br>
//     * 
//     * Instantiates a join_cd conversation
//     * 
//     * @author  <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
//     */
//    @SuppressWarnings("unused")
//    private static final CasaLispOperator MAKE_JOIN_CD_SERVICE_AGENT_REQUEST_CONVERSATION =
//    	new CasaLispOperator("MAKE-JOIN-CD-SERVICE-AGENT-REQUEST-CONVERSATION", "\"!Instantiates a client-side join_cd conversation.\""
//    			, TransientAgent.class)
//    {
//    	@Override
//    	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui) {
//    		MLMessage msg = getMsgForThread();
//
//    		agent.println("info", agent.getAgentName() + ": trying to create JoinCDServiceAgentRequestComposite conversation");
//    		JoinCDServiceAgentRequestComposite conv = new JoinCDServiceAgentRequestComposite(agent, msg.getConversationID());
//
//    		if (conv != null){ 
//    			//conv.run();
//    			return new StatusObject<JoinCDServiceAgentRequestComposite>(0, conv);
//    		}
//  		  return new Status(-1, "Could not create conversation in ServiceAgent.");
//    	}
//    };

    
}