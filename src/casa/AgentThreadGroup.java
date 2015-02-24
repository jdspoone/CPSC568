package casa;

public class AgentThreadGroup extends ThreadGroup {

	/**
	 * 
	 */
	private AbstractProcess agent=null;
	public AgentThreadGroup(String name) {
		super(name);
	}
	@Override public void uncaughtException(Thread t, Throwable e) {
			agent.println("error", "Unexpected exception caught in ThreadGroup", e);
  }
	/**
	 * @return the abstractProcess
	 */
	public AbstractProcess getAgent() {
		return agent;
	}
	/**
	 * @param agent the abstractProcess to set
	 */
	public void setAgent(AbstractProcess agent) {
		this.agent = agent;
	}
}