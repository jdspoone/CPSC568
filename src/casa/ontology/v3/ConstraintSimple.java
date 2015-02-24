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
package casa.ontology.v3;

import casa.Status;
import casa.StatusObject;
import casa.TransientAgent;
import casa.abcl.CasaLispOperator;
import casa.abcl.LispException;
import casa.abcl.ParamsMap;
import casa.exceptions.IllegalOperationException;
import casa.ontology.Constraint;
import casa.ontology.Ontology;
import casa.ontology.Type;
import casa.ui.AgentUI;
import casa.ui.BufferedAgentUI;
import casa.util.CASAUtil;

import java.util.Map;
import java.util.TreeMap;

import org.armedbear.lisp.Cons;
import org.armedbear.lisp.Environment;
import org.armedbear.lisp.LispObject;
import org.armedbear.lisp.SimpleString;

/**
 * A constraint that handles specifying constraints in the the context of a relation.
 * This constraint handles individuals vs types, the supertype of the parameter,
 * and an aribiraty Lisp expression to be evaluations.
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @see #ConstraintSimple(Individual, Type, Cons, CASAOntology, TransientAgent)
 */
public class ConstraintSimple extends Constraint {
	enum Individual {
		Individual,
		Type,
		Either
	}
	
	Individual ind;
	Type type;
	Cons exp;
	Ontology ont;
	TransientAgent agent;

  /**
   * Constructs a SimpleConstraint.  To later be evaluated by {@link #validate(Type, Type...)}.
   * Although any of the parameters may be null, at least one of <em>individual, type</em>, 
   * and <em>exp</em> must not be null.
   * @param individual If the value is:
   *   <ul>
   *   <li> {@link Individual#Individual}: The validating type must be an Individual.
   *   <li> {@link Individual#Type}: The validating type must be an Type.
   *   <li> {@link Individual#Either}: The validating type can be either an Individual or a Type.
   *   </ul>
   * @param type The <em>supertype</em> that the validating type must be a subtype (isa) of 
   *   in order for evalutation to pass . May be null.
   * @param exp A valid Lisp expression that must be return true for the evaluation to pass. 
   *   May be null. The expression may contain the following meta variables 
   *   (question marks are part of the meta variable names):
   *   <ul>
   *   <li>?type The typename under investigation.
   *   <li>?0 The domain of the relation.
   *   <li>?1 The range of the relation.
   *   <li>?[n] If this is a relation of arity greater than binary, the n'th indici of the relation.
   *   </ul>
   * @param ont The ontology in which to interpret the constraint.  May be null.
   * @param agent The agent context in which to interpret the constraint.  May be null.
   */
	public ConstraintSimple(Individual individual, Type type, Cons exp, Ontology ont, TransientAgent agent) {
		ind = individual;
		this.type = type;
		this.exp = exp;
		this.ont = ont;
		this.agent = agent;
	}
	
  /**
   * Validate the type in the context of a relation.  
   * @see Constraint#validate(Type, Type...)
   * @see #ConstraintSimple(Individual, Type, Cons, CASAOntology, TransientAgent)
   */
	@Override
	public boolean validate(Type type, Type... types) {
		//check if Individual
		if (ind==Individual.Individual && !type.isIndividual())
			return false;
		//check if Type
		if (ind==Individual.Type && type.isIndividual())
			return false;
		try {
			//get an ontology
			Ontology tempOnt = ont;
			if (tempOnt==null) {
				TransientAgent tempAgent = agent;
				if (tempAgent==null) tempAgent = TransientAgent.getAgentForThread();
				if (tempAgent!=null)
					tempOnt = (Ontology)tempAgent.getOntology();
			}
			if (tempOnt==null) {
				tempOnt = (Ontology)type.getOntology();
			}
			//check if type is correct
			if (tempOnt!=null && this.type!=null && !tempOnt.isa(type.getName(), this.type.getName())) 
				return false;
			//check expression
			if (tempOnt!=null && exp!=null && !validateExp(type, types))
				return false;
		} catch (IllegalOperationException e) {
			CASAUtil.log("error", "ConstraintSimple.validate", e, true);
		}
		return true;
	}
	
	/**
	 * Validate an expression.  This assumes that class variable <em>exp</em> is NOT null: do not 
	 * call this method if <em>exp</em> is null.
	 * @param type
	 * @param types
	 * @return
	 */
	private boolean validateExp(Type type, Type... types) {
		assert exp!=null;
		BufferedAgentUI buf = new BufferedAgentUI();
		Map<String,LispObject> map = null;
		if (types!=null) {
			map = new TreeMap<String,LispObject>();
			int i = 0;
			for (Type t: types) {
				map.put("?"+(i++), new SimpleString(t.getName()));
			}
		}
		map.put("?type", new SimpleString(type.getName()));
		TransientAgent tempAgent = agent;
		if (tempAgent==null) tempAgent = TransientAgent.getAgentForThread();
		Status stat = casa.abcl.Lisp.abclEval(tempAgent, null, map, exp.writeToString(), buf);
		if (stat.getStatusValue()==0) {
			if (stat instanceof StatusObject<?>) { 
				Object so = ((StatusObject<?>)stat).getObject();
				if (so==null || so==org.armedbear.lisp.Lisp.NIL) {
					return false;
				}
				else {
					return true;
				}
			}
			else { 
				return true;
			}
		}
		else {
			return false;
		}
	}

	
  /**
   * Java function definition to construct a SimpleConstraint.<br>
   * CONSTRAINT <br>
   * &OPTIONAL <br>TYPE-NAME java.lang.String; The type to constrain to.<br>
   * &KEY <br>INDIVIDUAL-ONLY; java.lang.Boolean Constraint to being an individual only and not a type.<br>
   * TYPE-ONLY java.lang.Boolean; Constrain to being a type only and not an individual.
   * EXP org.armedbear.lisp.Cons; The expression that must evaluate to true for the validation to pass. The expression is evaluation of an environment with variables: ?type (the type under consideration), ?0 (the domain), ?1 (the range), etc.<br>
   * ONTOLOGY java.lang.String or casa.ontology.v3.CASAOntology; The ontology in which this constraint resides.
   */
	@SuppressWarnings("unused")
	private static final CasaLispOperator CONSTRAINT =
  	new CasaLispOperator("CONSTRAINT", "\"!set a type-to-type relationship in the specified relation.\" "+
  			"&OPTIONAL TYPE-NAME \"@java.lang.String\" \"!The type to constrain to.\" "+
  			"&KEY INDIVIDUAL-ONLY \"@java.lang.Boolean\" \"!Constraint to being an individual only and not a type.\" "+
  			"TYPE-ONLY \"@java.lang.Boolean\" \"!Constrain to being a type only and not an individual.\" "+
  			"EXP \"@org.armedbear.lisp.Cons\" \"!The expression that must evaluate to true for the validation to pass. The expression is evaluation of an environment with variables: ?type (the type under consideration), ?0 (the domain), ?1 (the range), etc.\" "+
  			"ONTOLOGY \"The ontology in which this constraint resides.\" "
  			, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
  		Object o = params.getJavaObject("ONTOLOGY");
  		Ontology ontology = null;
  		if (o instanceof Ontology)
  			ontology = (Ontology)o;
  		else if (o instanceof String)
  			ontology = CASAOntology.getOntology((String)o);
  		if (ontology==null)
  			ontology = CASAUtil.findOntology(agent, params, ui, env);
  		o = params.getJavaObject("INDIVIDUAL-ONLY");
  		boolean ind = o==null?false:(Boolean)o;
  		o = params.getJavaObject("TYPE-ONLY");
  		boolean typ = o==null?false:(Boolean)o;
  		if (ind && typ) throw new LispException("(CONSTRAINT :INDIVIDUAL T :TYPE-ONLY T): Key :INDIVIDUAL-ONLY and key :TYPE-ONLY are disallowed together");
  		o = params.getJavaObject("TYPE-NAME");
  		String type = o==null?null:(String)o;
			Cons exp = params.getJavaObject("EXP",org.armedbear.lisp.Cons.class);
  		try {
  			if (type!=null || ind || typ || exp!=null)
				  return new StatusObject<ConstraintSimple>(0,
						new ConstraintSimple(
								    ind?ConstraintSimple.Individual.Individual:typ?ConstraintSimple.Individual.Type:ConstraintSimple.Individual.Either,
								    type==null?null:new SimpleType(type, ontology),
										exp,
										ontology,
										agent));
  			else
  				return null;
			} catch (IllegalOperationException e) {
				throw new LispException("(CONSTRAINT ...): Unexpected exception",e);
			}
  	}
  };
  
  /**
   * @return A string in the form "(constraint [type] [:type-only T] [:individual-only T] [:exp {exp}] [:ontology {ontology-name}])"
   */
  @Override
	public String toString() {
  	StringBuilder s = new StringBuilder("(constraint");
  	if (type != null)
  		s.append(' ').append(type.getName());
  	if (ind==Individual.Type)
  		s.append(" :type-only T");
  	else if (ind==Individual.Individual)
  		s.append(" :individual-only T");
  	if (exp!=null)
  		s.append(" :exp `").append(exp.writeToString());
  	if (ont!=null)
  		s.append(" :ontology \"").append(ont.getName()).append('\"');
  	s.append(")");
  	return s.toString();
  }
}

