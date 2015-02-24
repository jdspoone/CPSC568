package casa;

import org.armedbear.lisp.LispObject;

public class OntologyFilterArgument extends LispObject{
	
	private String name= "";
	private String subsumption = "";
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSubsumption() {
		return subsumption;
	}
	public void setSubsumption(String subsumption) {
		this.subsumption = subsumption;
	}
	
	

}
