package casa.ontology;

import casa.Status;
import casa.TransientAgent;
import casa.abcl.CasaLispOperator;
import casa.abcl.LispException;
import casa.abcl.ParamsMap;
import casa.exceptions.IllegalOperationException;
import casa.ui.AgentUI;

import org.armedbear.lisp.Cons;
import org.armedbear.lisp.Environment;

public abstract class Constraint {

	public Constraint() {
		super();
	}

	/**
	 * Validate the constraint against the <em>type</em> against this constraint in
	 * the context of a relation.  The
	 * <em>types</em> (...) parameter should contain the vector of parameters the relation to
	 * allow a complete context for the validation (e.g. for a binary relation, the first element
	 * if <em>types</em> should be the domain, and the second element should be the range. 
	 * @param type The type under examination.
	 * @param types The types involved in the relation (in order).
	 * @return
	 */
	public abstract boolean validate(Type type, Type... types);

//	@SuppressWarnings("unused")
//	private static final CasaLispOperator CONSTRAINT__VALIDATE =
//			new CasaLispOperator("CONSTRAINT.VALIDATE", "\"!Test the argument CONSTRAINT with respect to TYPE-NAME.\" "+
//					"CONSTRAINT \"@casa.ontology.Constraint\" \"!The constraint\" "+
//					"TYPE-NAME \"@java.lang.String\" \"!The type to constrain to.\" "
//					, TransientAgent.class)
//	{
//		@Override
//		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
//			ConstraintSimple cons = (ConstraintSimple)params.getJavaObject("CONSTRAINT");
//			if (cons==null) 
//				throw new LispException("Bad CONSTRAINT parameter (null)");
//			String typeName = (String)params.getJavaObject("TYPE-NAME");
//			if (typeName==null || typeName.length()==0) 
//				throw new LispException("Bad TYPE-NAME parameter (null or zero-length)");
//    	CASAOntology ont = CASAOntology.findOntology(agent, params, ui, env);
//			try {
//				if (!ont.isObject(typeName))
//					throw new LispException("(CONSTRAINT.VALIDATE ...): Cannot find type "+typeName);
//				SimpleType type=new SimpleType(typeName, ont);
//				
//				return new Status(cons.validate(type, null)?0:-1);
//			} catch (IllegalOperationException e) {
//				throw new LispException("(CONSTRAINT.VALIDATE ...): Unexpected error",e);
//			}
//		}
//	};

	
}