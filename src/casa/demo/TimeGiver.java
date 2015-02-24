package casa.demo;

import casa.*;
import casa.abcl.ParamsMap;
import casa.exceptions.IPSocketException;
import casa.ui.AgentUI;
import casa.util.CASAUtil;

import java.util.Date;

public class TimeGiver extends TimeSuper {
	public TimeGiver (ParamsMap params, AgentUI ui) throws Exception {
		super (params, ui);
	}

	public PerformDescriptor perform_getTime (MLMessage message) {
      println("warning","before making ret");
		in ("TimeGiver.performGetTime");

		PerformDescriptor ret = new PerformDescriptor ();

    ret.put (ML.LANGUAGE, Date.class.getName ());
		ret.put (ML.CONTENT, CASAUtil.getDateAsString());

		ret.setStatus (new Status (0));

		out ("TimeGiver.performGetTime");
		return ret;
	}
}