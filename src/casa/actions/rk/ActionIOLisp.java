/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.actions.rk;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class ActionIOLisp extends ActionIO {

	/**
	 * 
	 */
	public ActionIOLisp() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see casa.actions.ActionIO#toString(casa.actions.CompositeAction)
	 */
	@Override
	public String toString(CompositeAction action) {
		StringBuilder b = new StringBuilder();
		b.append('(').append(action.getName());
		if (action.actions!=null) {
			for (Action a: action.actions) {
				b.append(' ')
				.append(toString(a));
			}
		}
		b.append(')');
		return b.toString();
	}

	/* (non-Javadoc)
	 * @see casa.actions.ActionIO#toString(casa.actions.SimpleAction)
	 */
	@Override
	public String toString(SimpleAction action) {
		StringBuilder b = new StringBuilder();
		b.append('(')
//		.append("#| ").append(action.getName()).append(" - toString(SimpleAction) not defined |#");
    .append(action.getName());
		if (action.params!=null) {
			for (Param p: action.params) {
				b.append(' ')
				.append(toString(p));
			}
		}
		b.append(')');
		return b.toString();
	}
	
	@Override
	public String toString(Param p) {
		StringBuilder b = new StringBuilder();
		b.append("(param \"")
    .append(p.name)
    .append("\" ")
    .append(p.type.getName())
    .append("\"");
		if (p.defValue!=null) {
			b.append(" :default ")
			.append(p.defValue.toString());
		}
		b.append(')');
		return b.toString();
		
	}
	
	public String paramsToString(Param[] params) {
		return null;
	}

	/* (non-Javadoc)
	 * @see casa.actions.ActionIO#toInstanceString(casa.actions.CompositeAction)
	 */
	@Override
	public String toInstanceString(CompositeAction action) {
		StringBuilder b = new StringBuilder('(');
		b.append(action.getName());
		if (action.actions!=null) {
			for (Action a: action.actions) {
				b.append(' ')
				.append(a.toString());
			}
		}
		b.append(')');
		return b.toString();
	}

	/* (non-Javadoc)
	 * @see casa.actions.ActionIO#toInstanceString(casa.actions.SimpleAction)
	 */
	@Override
	public String toInstanceString(SimpleAction action) {
		StringBuilder b = new StringBuilder();
		b.append('(')
//		.append("#| ").append(action.getName()).append(" - toString(SimpleAction) not defined |#");
    .append(action.getName());
		if (action.params!=null) {
			for (int i=0; i<action.params.length; i++) {
				b.append(' ')
				.append(action.getParamValue(i));
			}
		}
		b.append(')');
		return b.toString();
	}

	/* (non-Javadoc)
	 * @see casa.actions.ActionIO#fromString(java.lang.String)
	 */
	@Override
	public Action fromString(String serialized) {
		return null;
	}

	/* (non-Javadoc)
	 * @see casa.actions.ActionIO#fromInstanceString(java.lang.String)
	 */
	@Override
	public Action fromInstanceString(String serialized) {
		// TODO Auto-generated method stub
		return null;
	}

}
