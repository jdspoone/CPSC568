package casa.testAgents;

import casa.ML;
import casa.MLMessage;
import casa.Status;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.exceptions.URLDescriptorException;
import casa.service.ServiceAgent;
import casa.ui.AgentUI;
import casa.util.CASAUtil;

import java.util.TimeZone;

public class TimeAgent extends ServiceAgent {

	public TimeAgent(ParamsMap params, AgentUI ui) throws Exception {
    super (params, ui);
  }

  @Override
	protected ServiceDescriptor[] getServiceDescriptors() {
    ServiceDescriptor[] sds = {
        new ServiceDescriptor(
              ML.PERFORMATIVE,
              ML.GUI_ACTION_REQUEST,
              new ServiceAgent.OffersCommand() {
                @Override
								public void execute(URLDescriptor cd, String to) {
                  String myURL = getURL().toString();
                  String[] body = {
                      "get.time | ?(help=\"Requests the current time from " + myURL + "\")",
                      "send " + ML.INFORM + " " + ML.GUI_ACTION_REQUEST + " \"" + myURL + "\" receiver=\"" + cd + "\""};
                  String[] params = {
                      ML.LANGUAGE, "casa.*",
                      ML.CONTENT, CASAUtil.serialize((Object[])body)};
                  MLMessage outMsg = getNewMessage(ML.INFORM, ML.NEW_COMMAND, cd, params);
                  
                  sendMessage(MLMessage.constructProxyMessage (outMsg, getURL (), cd, to));
                }
              },
              new ServiceCommand() {@Override
							public Status execute(MLMessage message){
                try {
                	URLDescriptor.make(message.getParameter(ML.SENDER));
                } catch (URLDescriptorException ex) {}

                String zone = TimeZone.getDefault().getID();
                String info = CASAUtil.getDateAsString() + " " + zone;

                MLMessage msg = MLMessage.constructReplyTo (message, getUniqueRequestID(), getURL());
                String reply[] = {
                    ML.PERFORMATIVE, ML.INFORM,
                    ML.ACT, TimeAgentInterface.TIME_MESSAGE,
                    ML.LANGUAGE, String.class.getName (),
                    ML.CONTENT, info
                    };
                msg.setParameters(reply);

                Status ret = sendMessage (msg);

                return ret;}
              })
    };
    return sds;
  }

}
