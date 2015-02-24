package casa.ontology;

import casa.Status;
import casa.StatusObject;
import casa.TransientAgent;
import casa.abcl.CasaLispOperator;
import casa.abcl.LispException;
import casa.abcl.ParamsMap;
import casa.exceptions.DuplicateNodeException;
import casa.exceptions.IllegalOperationException;
import casa.exceptions.IncompatableTypeHierarchiesException;
import casa.exceptions.ParentNodeNotFoundException;
import casa.ontology.owl2.OWLOntology;
import casa.ui.AgentUI;
import casa.util.CASAUtil;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Set;
import java.util.TreeSet;

import org.armedbear.lisp.Cons;
import org.armedbear.lisp.ControlTransfer;
import org.armedbear.lisp.Environment;
import org.armedbear.lisp.LispObject;
import org.armedbear.lisp.SimpleString;
import org.coode.owl.krssparser.KRSSOntologyFormat;
import org.coode.owlapi.latex.LatexAxiomsListOntologyFormat;
import org.coode.owlapi.latex.LatexOntologyFormat;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.coode.owlapi.obo.parser.OBOOntologyFormat;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxHTMLOntologyFormat;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxOntologyFormat;
import uk.ac.manchester.owl.owlapi.tutorialowled2011.OWLTutorialSyntaxOntologyFormat;
import de.uulm.ecs.ai.owlapi.krssparser.KRSS2OntologyFormat;

public interface Ontology {
	
	public String getDefaultFileExtension();

	/**
	 * Adds <em>name</em> to the hierarchy with super-type links to each of the
	 * nodes in <em>parents</em>.  Each member of <em>parents</em> <b>must</b>
	 * already exist in the hierarchy.
	 * @param name the name of new type
	 * @param parents the super-types of <em>name</em>.  Each member of <em>parents</em>
	 * <b>must</b> already exist in the hierarchy.
	 * @return returns the Node added to the hierarchy.
	 * @throws ParentNodeNotFoundException if any of the members of <em>parents</em>
	 * isn't in the hierarchy.
	 * @throws IllegalOperationException 
	 * @throws DuplicateNode if <em>name</em> already exists in the hierarchy
	 */
	public abstract void addType(String name, String... parents)
			throws ParentNodeNotFoundException, DuplicateNodeException, IllegalOperationException;

	/**
	 * Equivalent to <code>add(name,new String[]{parent})</code>.  Use for types
	 * that only have a single super-type.
	 * @param name the name of new type
	 * @param parent the super-type of <em>name</em>.
	 * @throws ParentNodeNotFoundException if <em>parent</em>
	 * isn't in the hierarchy.
	 * @throws IllegalOperationException 
	 * @throws DuplicateNode if <em>name</em> already exists in the hierarchy
	 */
	public abstract void addType(String name, String parent)
			throws ParentNodeNotFoundException, DuplicateNodeException, IllegalOperationException;

	/**
	 * Equivalent to <code>add(new TypeHierarchy(description))</code>.
	 * @param description
	 * @throws DuplicateNode
	 * @throws IncompatableTypeHierarchiesException
	 * @throws ParentNodeNotFoundException
	 * @throws ParseException
	 * @return the number of types added to the hierarchy
	 */
	public abstract int add(String description) throws DuplicateNodeException,
			IncompatableTypeHierarchiesException, ParentNodeNotFoundException,
			ParseException;

	/**
	 * Determines if an element is related to another element by the relation.  In terms 
	 * of description logics, whether <code>domain</code> has a role of
	 * type <code>range</code>.
	 * @param domain the thing that is a the "source" of the relation
	 * @param range the thing that is the "destination" of the relation
	 * @return true iff <code>domain</code> is related to <code>range</code>
	 */
	public abstract boolean relatedTo(String relation, String domain, String range) throws UnsupportedOperationException, IllegalOperationException;
	
	/**
	 * Determines if <em>parent</em> is an ancestor of <em>child</em>.  This should
	 * be equivalent to relatedTo("isa",child,parent) if relatedTo() is supported.
	 * @param child the name if a node in the TypeHierarchy
	 * @param parent the name of a node in the TypeHierarchy
	 * @return true iff <em>parent</em> is an ancestor of <em>child</em>
	 */
	public abstract boolean isa(String child, String parent) throws IllegalOperationException;

	/**
	 * Finals all ancestors of <em>child</em>.  This should
	 * be equivalent to relatedTo("isa",child) if relatedTo() is supported.
	 * @param child the name if a node in the TypeHierarchy
	 * @return all ancestors of <em>child</em>
	 */
	public abstract Set<String> isa(String child) throws IllegalOperationException;

	/**
	 * Finals all parents (direct ancestors) of <em>child</em>.  This should
	 * be equivalent to relatedTo("isa-parent",child) if relatedTo() is supported.
	 * @param child the name if a node in the TypeHierarchy
	 * @return all parents (direct ancestors) of <em>child</em>
	 */
	public abstract Set<String> isParent(String child) throws IllegalOperationException;

	/**
	 * Create a string containing the persistent representation of this object.
	 * See the introduction to this entry for the details of the format.
	 * @return a string containing the persistent representation of this TypeHierarchy
	 */
	public abstract String toString();

	/**
	 * Returns true if the agent "knows" about the token string in the parameter: it's either a type or an individual.
	 * @param name the name of a node in the TypeHiearchy
	 * @return true iff the node is in the TypeHiearchy as either a type or an individual.
	 * @throws IllegalOperationException if the qualified syntax explicitly specifies a dictionary that is not the owner.
	 */
	public abstract boolean isObject(String name) throws IllegalOperationException;

	/**
	 * Returns true if the agent "knows" about the token string in the parameter and it's a type.
	 * @param name the name of a node in the TypeHiearchy
	 * @return true iff the node is in the TypeHiearchy and it is a type.
	 * @throws IllegalOperationException if the qualified syntax explicitly specifies a dictionary that is not the owner.
	 */
	public abstract boolean isType(String name) throws IllegalOperationException;

	/**
	 * Returns true if the agent "knows" about the token string in the parameter and it's an individual.
	 * @param name the name of a node in the TypeHiearchy
	 * @return true iff the node is in the TypeHiearchy and it is an individual.
	 * @throws IllegalOperationException if the qualified syntax explicitly specifies a dictionary that is not the owner.
	 */
	public abstract boolean isIndividual(String name) throws IllegalOperationException;

	/**
	 * Returns true if the agent "knows" about the token string in the parameter and it's an relation.
	 * @param name the name of the relation
	 * @return true iff the node is a relation in the ontology.
	 * @throws IllegalOperationException if the qualified syntax explicitly specifies a dictionary that is not the owner.
	 */
	public abstract boolean isRelation(String name) throws IllegalOperationException;

	/**
	 * Print to the return String the complete path (in persistent form) from the
	 * root node to the node named <em>name</em>.  Every possible branch is (multiple
	 * inheritance) is included, and no nodes are included more than once.
	 * @param name the name of a node in the TypeHiearchy
	 * @return the complete path (in persistent form) from the
	 * root node to the node named <em>name</em>, or <em>null</em> if <em>name</em>
	 * isn't the name of a node in the TypeHiearchy.
	 * @throws IllegalOperationException TODO
	 */
	public abstract String describe(String name) throws IllegalOperationException;

	/**
	 * Determines if <em>other</em> is a compatible description (see {@link #describe(TypeNode)})
	 * to this TypeHierarchy.
	 * Node A is compatible with node B iff all nodes in A that have matched names
	 * in B are exactly the same as the corresponding node in B (that is, they have
	 * the same name, and each of the parents [recursively] are exactly the same).
	 * forall i:Node | i member B . exists j:Node | j member A . i.name = j.name
	 * @param other the TypeHierarchy ot compare to this one for compatability
	 * @return true iff <em>other</em> is compatable
	 */
	public abstract boolean isCompatable(Ontology other);

	/**
	 * Determines if <em>other</em> is a compatible description (see {@link #describe(TypeNode)})
	 * to this TypeHierarchy.
	 * Node A is compatible with node B iff all nodes in A that have matched names
	 * in B are exactly the same as the corresponding node in B (that is, they have
	 * the same name, and each of the parents [recursively] are exactly the same).
	 * forall i:Node | i member B . exists j:Node | j member A . i.name = j.name<br>
	 * Use this method instead of isCompatable() to get more detailed information
	 * (in the Exception thrown) about what the error was.
	 * @param other the TypeHierarchy ot compare to this one for compatability
	 * @throws IncompatableTypeHierarchiesException iff <em>other</em> is incompatable
	 */
	public abstract void isCompatableThrow(Ontology other)
			throws IncompatableTypeHierarchiesException;
	
  /**
   * Add the persistent Ontology data in <em>spec</em> to this
   * Ontology.
   * @param spec A String containing the persistent for of an
   *          Ontology
   * @return a Status object indicating:
   *         <ul>
   *         <li>0: success
   *         <li>-1: Attempted insertion of duplicate type node name into
   *         performatives type hierarchy
   *         <li>-2: Malformed content field: Parent node not found in
   *         performatives type hierarchy
   *         <li>-3: Malformed content field: Parse exception
   *         <li>-4: Type information is incompatible with existing
   *         performatives hierarchy
   *         <li>-5: Input spec contains duplicate type node name
   *         </ul>
   */
	public abstract Status extendWith(String spec);

	public void addSuperOntologies(Ontology... parentOntologies) throws IllegalOperationException;
	public void addSuperOntologies(String... parentOntologies) throws IllegalOperationException;
	
	public abstract String getName();

	public abstract void addIndividual(String name, String parent)
			throws ParentNodeNotFoundException, DuplicateNodeException,
			IllegalOperationException;

	abstract void addIndividual(String name, String... parents)
			throws ParentNodeNotFoundException, DuplicateNodeException,
			IllegalOperationException;

	public abstract boolean instanceOf(String child, String parent)
			throws IllegalOperationException;
	
	static final CasaLispOperator ONTOLOGY =
  	new CasaLispOperator("ONTOLOGY", "\"!Declare a new Ontology or extends an existing Ontology.\" "+
  			"NAME \"@java.lang.String\" \"!The name of the  ontology.\" "+
  			"SUPER-ONTOLOGIES \"!A list of existing ontology names to append to the super-ontologies of this ontology.\" " +
  			"RELS-AND-TYPES \"!A list of decl* type declarations.\" "+
  			"&KEY VERBOSE \"!echo the command if verbose/=NIL\""
  			, TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "DECLONTOLOGY", "WITH-ONTOLOGY")
  {
  	@Override
  	public Status execute(final TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {
  		//assert agent!=null;
  		final String eng = agent.getOntologyEngine();
			Class<? extends Ontology> cls;
			Method method;
			try {
				cls = (Class<? extends Ontology>)Class.forName(eng);
			
				
				method = cls.getMethod("ontology_lispImpl", TransientAgent.class, ParamsMap.class, AgentUI.class, Environment.class);
				if (!Status.class.isAssignableFrom(method.getReturnType()))
						throw new Exception();
				return (Status)method.invoke(null, agent, params, ui, env);
			} catch (final Throwable e) {
				throw new LispException("Failed to find ontology engine '"+eng+"' or invoke ontology_lispImpl() method.", e);
			}		
  	}
  };

	static final CasaLispOperator ONT__IS_OBJECT =
  	new CasaLispOperator("ONT.IS-OBJECT", "\"!Return true iff the parameter is a type or an individual in the ontology.\" "+
  			"NAME \"@java.lang.String\" \"!The name of the item.\" "+
  			"&KEY ONTOLOGY \"@java.lang.String\" \"!The ontology to look in (default is the agent current ontology)\" "
  			, TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "DECLINDIVIDUAL")
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {
  		try {
  			String name = ((String)params.getJavaObject("NAME"));//.toUpperCase();
      	Ontology ont = CASAUtil.findOntology(agent, params, ui, env);
      	if (ont==null) return new Status(-15,"Can't find ontology to apply operator (ONT.IS-OBJECT "+name+" ...) to.");
      	
      	return new StatusObject<Boolean>(0, name==null?false:ont.isObject(name));
			} catch (Throwable e) {
				return new Status(-1,CASAUtil.printui(e.toString(),ui, params, agent));
			}
  	}
  };

	static final CasaLispOperator ONT__IS_TYPE =
  	new CasaLispOperator("ONT.IS-TYPE", "\"!Return true iff the parameter is a type (not an individual) in the ontology.\" "+
  			"NAME \"@java.lang.String\" \"!The name of the item.\" "+
  			"&KEY ONTOLOGY \"@java.lang.String\" \"!The ontology to look in (default is the agent current ontology)\" "
  			, TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "DECLINDIVIDUAL")
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {
  		try {
  			String name = ((String)params.getJavaObject("NAME"));//.toUpperCase();
      	Ontology ont = CASAUtil.findOntology(agent, params, ui, env);
      	if (ont==null) return new Status(-15,"Can't find ontology to apply operator (ONT.IS-TYPE "+name+" ...) to.");
      	
      	return new StatusObject<Boolean>(0, name==null?false:ont.isType(name));
			} catch (Throwable e) {
				return new Status(-1,CASAUtil.printui(e.toString(),ui, params, agent));
			}
  	}
  };
  
	static final CasaLispOperator ONT__IS_INDIVIDUAL =
  	new CasaLispOperator("ONT.IS-INDIVIDUAL", "\"!Return true iff the parameter is an individual in the ontology.\" "+
  			"NAME \"@java.lang.String\" \"!The name of the item.\" "+
  			"&KEY ONTOLOGY \"@java.lang.String\" \"!The ontology to look in (default is the agent current ontology)\" "
  			, TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "DECLINDIVIDUAL")
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {
  		try {
  			String name = ((String)params.getJavaObject("NAME"));//.toUpperCase();
      	Ontology ont = CASAUtil.findOntology(agent, params, ui, env);
      	if (ont==null) return new Status(-15,"Can't find ontology to apply operator (ONT.IS-INDIVIDUAL "+name+" ...) to.");
      	
      	return new StatusObject<Boolean>(0, name==null?false:ont.isIndividual(name));
			} catch (Throwable e) {
				return new Status(-1,CASAUtil.printui(e.toString(),ui, params, agent));
			}
  	}
  };
  
	static final CasaLispOperator ONT__GET_RESIDENT =
  	new CasaLispOperator("ONT.GET-RESIDENT", "\"!Retrieves a list of the names of the ontologies in shared memory.\" "+
  			"&KEY VERBOSE \"!echo the command if verbose/=NIL\""
  			, TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "GET-RESIDENT-ONTOLOGIES")
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {

  		try {
				Cons ptr=null, last;
				//if (allOntologies==null) return new StatusObject<LispObject>(org.armedbear.lisp.Lisp.NIL);
				LispObject list = org.armedbear.lisp.Lisp.NIL;
				String[] onts = (String[])CASAUtil.callMethod(agent==null?OWLOntology.class:Class.forName(agent.getOntologyEngine()), "getResident", null, null, null);
				for (String o: onts) {
					if (ptr==null) list = ptr = new Cons(new SimpleString(o));
					else {
					  last = ptr;
						ptr = new Cons(new SimpleString(o));
						last.cdr = ptr;
					}
				}
				ui.println(list.writeToString());
				return new StatusObject<LispObject>(list);
			} catch (Throwable e) {
				throw new LispException("(ONT.GET-RESIDENT)", e);
			}
  		}
  };
  
	static final CasaLispOperator ONT__SET_DEFAULT =
  	new CasaLispOperator("ONT.SET-DEFAULT", "\"!Sets the agent's default ontology either from the shared memory or from a file of the same name ([name].ont.lisp).\" "+
  			"NAME \"@java.lang.String\" \"!The name of the ontology.\" ", TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "SET-DEFAULT-ONTOLOGY")
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {
  		
  		String ontName = ((String)params.getJavaObject("NAME"))/*.toUpperCase()*/;
  		
  		Ontology ont = null;
  		ont = CASAUtil.findOntology(agent, params, ui, env);
  		if (ont==null) {
  			return new Status(-1,"Ontology "+ontName+" does not exist");
  		}
  		else {
  			agent.setOntology(ont);
  			return new Status(0);
  		}
  	}
  };

  @SuppressWarnings("unused")
	static final CasaLispOperator ONT__GET =
  	new CasaLispOperator("ONT.GET", "\"!Retrieves ontology either from the shared memory or from a file of the same name ([name].ont.lisp). If none of :relation, :type, or :individual is specified the ontology is printed and returned.\" "
  			+"&OPTIONAL ONTOLOGY \"@java.lang.String\" \"!The name of the ontology.\" "
  			+"&KEY VERBOSE \"!echo the command if verbose/=NIL\""
  			+"INDIVIDUAL \"!print out a lisp description of an individual in the ontology, returning the individual object.\""
  			+"TYPE       \"!print out a lisp description of a type in the ontology, returning the type object.\""
  			+"RELATION   \"!print out a lisp description of a relation in the ontology, returning the relation object.\""
  			+"(IMPORTS NIL) \"@java.lang.Boolean\" \"!print out the imported ontologies (ignored if INDIVIDUAL, TYPE, or RELATION is present).\""
  			, TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "GET-ONTOLOGY")
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {
  		
  		String ontName = null;
  		
  		if (params.containsKey("ONTOLOGY")) 
  			ontName = ((String)params.getJavaObject("ONTOLOGY"));
  		
  		Ontology ont = null;
  		if (ontName==null) {
 				try {
 					if (ui!=null)
 						ui.println("Retrieving agent's default ontology...");
					ont = (Ontology)agent.getOntology();
				} catch (Throwable e) {
					return new Status(-2,"Failed (ONT.GET) for current agent",e);
				}
  		}
  		else {
  			params.put("ONTOLOGY", ontName, new SimpleString(ontName), false);
    		ont = CASAUtil.findOntology(agent, params, ui, env);
  		}
  		if (ont==null) {
  			return new Status(-1,"Ontology "+ontName+" does not exist");
  		}

  			String relName = (String)params.getJavaObject("RELATION"); 
  			String typeName = (String)params.getJavaObject("TYPE"); 
  			String indName = (String)params.getJavaObject("INDIVIDUAL");
  			int c = 0;
  			if (relName!=null)  c |= 0x1; 
  			if (typeName!=null) c |= 0x2; 
  			if (indName!=null)  c |= 0x4; 
  			if (Integer.bitCount(c)>1) {
					return new Status(-3,"(ONT.GET ...): Only one of :RELATION, :TYPE, or :INDIVIDUAL allowed");  			  
  			}
  			switch (c) {
  			case 0:
  				if (ont instanceof org.semanticweb.owlapi.model.OWLOntology && (Boolean)params.getJavaObject("IMPORTS"))
    			  ui.println(OWLOntology.toStringPlusImports((org.semanticweb.owlapi.model.OWLOntology)ont, null));
  				else
  					ui.println(ont.toString());
    			return new StatusObject<Ontology>(ont);
  			case 1:
					try {
						String rel = ont.describeRelation(relName);
						ui.println(rel);
						return new StatusObject<String>(rel);
					} catch (IllegalOperationException e) {
						return new Status(-6,"(ONT.GET ... :RELATION "+relName+"): "+e.getMessage()==null?"IllegalOperationException":e.getMessage());
					}
  			case 2:
  				try {
  					String type = ont.describeType(typeName);
  					ui.println(type);
						return new StatusObject<String>(type);
					} catch (IllegalOperationException e) {
						return new Status(-3,"(ONT.GET ... :TYPE "+typeName+"): "+e.getMessage()==null?"IllegalOperationException":e.getMessage());
					}
  			case 4:
  				try {
  					String ind = ont.describeIndividual(indName);
  					ui.println(ind);
						return new StatusObject<String>(ind);
					} catch (IllegalOperationException e) {
						return new Status(-4,"(ONT.GET ... :INDIVIDUAL "+indName+"): "+e.getMessage()==null?"IllegalOperationException":e.getMessage());
					}
  			default:
  				return new Status(-5, "(ONT.GET ...): Internal error");
  			}
  	}
  };

  @SuppressWarnings("unused")
	static final CasaLispOperator ONT__DESCRIBE =
  	new CasaLispOperator("ONT.DESCRIBE", "\"!Prints out a description of the item.\" "
  			+"ITEM      \"@java.lang.String\" \"!The item to describe.\" "
  			+"&KEY VERBOSE \"!echo the command if verbose/=NIL\""
  			+"SYNTAX   \"@java.lang.String\" \"!manchester, functional, tutorial, XML, DLHTML, DL, KRSS2, KRSS, LatexAxiomsList, latex, OBO, prefix ...\""
  			+"INDIVIDUAL \"!print out a lisp description of an individual in the ontology, returning the individual object.\""
  			+"TYPE       \"!print out a lisp description of a type in the ontology, returning the type object.\""
  			+"RELATION   \"!print out a lisp description of a relation in the ontology, returning the relation object.\""
  			, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {
  		
  		String itemName = null;
  		String syntax = null;
  		
  		if (params.containsKey("ITEM")) 
  			itemName = ((String)params.getJavaObject("ITEM"));
  		if (params.containsKey("SYNTAX")) 
  			syntax = ((String)params.getJavaObject("SYNTAX"));
  		
  		try {
  			String ret;
  			if (syntax==null)
  				ret = agent.getOntology().describe(itemName);
  			else {
  				OWLOntologyFormat form = null; 
  				if (syntax.equalsIgnoreCase("manchester"))
  					form = new ManchesterOWLSyntaxOntologyFormat();
  				else if (syntax.equalsIgnoreCase("functional"))
  					form = new OWLFunctionalSyntaxOntologyFormat();
  				else if (syntax.equalsIgnoreCase("tutorial"))
  					form = new OWLTutorialSyntaxOntologyFormat();
  				else if (syntax.equalsIgnoreCase("XML"))
  					form = new OWLXMLOntologyFormat();
  				else if (syntax.equalsIgnoreCase("DLHTML"))
  					form = new DLSyntaxHTMLOntologyFormat();
  				else if (syntax.equalsIgnoreCase("DL"))
  					form = new DLSyntaxOntologyFormat();
  				else if (syntax.equalsIgnoreCase("KRSS2"))
  					form = new KRSS2OntologyFormat();
  				else if (syntax.equalsIgnoreCase("KRSS"))
  					form = new KRSSOntologyFormat();
  				else if (syntax.equalsIgnoreCase("LatexAxiomsList"))
  					form = new LatexAxiomsListOntologyFormat();
  				else if (syntax.equalsIgnoreCase("Latex"))
  					form = new LatexOntologyFormat();
  				else if (syntax.equalsIgnoreCase("OBO"))
  					form = new OBOOntologyFormat();
  				else if (syntax.equalsIgnoreCase("Prefix"))
  					form = new PrefixOWLOntologyFormat();
  				if (form==null) 
  					throw new LispException("ONT.DESCRIBE: Don't know SYNTAX "+syntax+". Use one of: manchester, functional, tutorial, XML, DLHTML, DL, KRSS2, KRSS, LatexAxiomsList, latex, OBO, prefix.");
  				ret = ((OWLOntology)agent.getOntology()).describe(itemName, form);
  			}
				return new StatusObject<SimpleString>(new SimpleString(ret));
			} catch (Throwable e) {
				throw new LispException(e.getMessage(), e);
			}
  	}
  };

  public String describeRelation(String relation) throws IllegalOperationException;

	public String describeType(String type) throws IllegalOperationException;

	public String describeIndividual(String type) throws IllegalOperationException;

  public abstract void declMaplet(String relationName, String domainName, String rangeName)
			throws IllegalOperationException;

	public abstract void declRelation(final String name, String basedOn, Set<Relation.Property> properties,
			Constraint domConstraint, Constraint ranConstraint, Object... otherParams) throws IllegalOperationException;

	public abstract Set<String> relatedTo(String relation, String domain)
			throws UnsupportedOperationException, IllegalOperationException;

	@SuppressWarnings("unused")
	static final CasaLispOperator ONT__RELATED_TO =
  	new CasaLispOperator("ONT.RELATED-TO", "\"!If RANGE is specified, return T iff the DOMAIN is related to the RANGE by the specified RELATION, otherwise return the set of elements in range of DOMAIN by RELATION.\" "+
  			"RELATION \"@java.lang.String\" \"!The name of the relation.\" "+
  			"DOMAIN \"@java.lang.String\" \"!An element in the domain of this relation.\" "+
  			"&OPTIONAL RANGE \"@java.lang.String\" \"!A element in the range in this relation.\" "+
  			"&KEY ONTOLOGY \"@java.lang.String\" \"!The ontology in which to check (default is the agent current ontology)\" "+
  			"VERBOSE \"!echo the command if verbose/=NIL\""
  			, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
  		String rel = (String)params.getJavaObject("RELATION");
  		String dom = (String)params.getJavaObject("DOMAIN");
  		String ran = (String)params.getJavaObject("RANGE");
    	Ontology ont = CASAUtil.findOntology(agent, params, ui, env);
    	if (ont==null) return new Status(-15,"Can't find ontology to apply operator (ONT.ASSERT "+rel+" ...) to.");
  		try {
				if (ran==null) {
					return new StatusObject<LispObject>(CASAUtil.toCons(ont.relatedTo(rel, dom).toArray(new String[1])));
				}
				else {
					return new StatusObject<Boolean>(ont.relatedTo(rel, dom, ran));
				}
			} catch (Throwable e) {
				throw new LispException("(ONT.RELATED-TO "+rel+" "+dom+(ran==null?"":" "+ran)+"): ", e);
			}
  	}
  };
  

	@SuppressWarnings("unused")
	static final CasaLispOperator ONT__ASSERT =
  	new CasaLispOperator("ONT.ASSERT", "\"!set a type-to-type relationship in the specified relation.\" "+
  			"RELATION-NAME \"@java.lang.String\" \"!The name of the relation.\" "+
  			"DOMAIN \"@java.lang.String\" \"!An element in the domain of this relation.\" "+
  			"RANGE \"!A element or list of elements to be set as the range in this relation.\" "+
  			"&KEY ONTOLOGY \"@java.lang.String\" \"!The ontology to put the new type in (default is the agent current ontology)\" "+
  			"VERBOSE \"!echo the command if verbose/=NIL\""
  			, TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "DECLMAPLET")
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {

  		//get the relation param
			String relName = ((String)params.getJavaObject("RELATION-NAME"))/*.toUpperCase()*/;
			
    	Ontology ont = null;
			try {
      	ont = CASAUtil.findOntology(agent, params, ui, env);
      	if (ont==null) return new Status(-15,"Can't find ontology to apply operator (ONT.ASSERT "+relName+" ...) to.");
	  			  			
  			//get the relation param
				relName = ((String)params.getJavaObject("RELATION-NAME"))/*.toUpperCase()*/;
//				Relation rel = ont.getRelation(relName);
//				if (rel==null) return new Status(-1,CASAUtil.printui("Undefined relation: "+relName,ui, params, agent));
//				if (!rel.isAssignable()) return new Status(-2,CASAUtil.printui("Relation "+relName+" is not assignable",ui, params, agent));
//				if (!rel.isVisible()) return new Status(-3,CASAUtil.printui("Relation "+relName+" is not visible",ui, params, agent));

				//get the domain type param
				String domNameOrig = ((String)params.getJavaObject("DOMAIN"));
				String domName = domNameOrig/*.toUpperCase()*/;
				if (!ont.isType(domName) && !ont.isIndividual(domName)) {
					ont.addType(domName);
				}

				int i=2;
				LispObject obj = params.getLispObject("RANGE");
				if (obj == null || obj==org.armedbear.lisp.Lisp.NIL) {
					return new Status(-7,CASAUtil.printui("Invalid range",ui, params, agent));
				} 
				else if (obj instanceof org.armedbear.lisp.Cons) {
					StringBuilder b = new StringBuilder("(ONT.ASSERT ").append(relName).append(" \"").append(domName).append("\" '(");
					int count = 0;
					while (obj!=null && obj!=org.armedbear.lisp.Lisp.NIL) {
						count++;
						String ranName = obj.car().writeToString()/*.toUpperCase()*/;
//						BaseType ran = (BaseType)ont.getType(ranName);
//						if (ran == null) //ran = ont.declType(ranName); //could recover here, but that doesn't seem right
//							return new Status(-2,CASAUtil.printui("(ONT.ASSERT "+relName+" \""+domName+"\" "+ranName+"): range '"+ranName+" type does not exist.",ui, params, agent));
						try {
							ont.declMaplet(relName,domName,ranName);
							b.append(ranName).append(' ');
						} catch (Throwable e) {
							return new Status(-1,CASAUtil.printui("(ONT.ASSERT "+relName+" \""+domName+"\" "+ranName+"): "+e.toString(),ui, params, agent));
						}
						obj = obj.cdr();
					}
					if (count>0) b.setLength(b.length()-1);
					b.append("))");
					if (params.getJavaObject("VERBOSE")!=null)
					  ui.println(b.toString());

				} 
				else {
					String ranName = obj.writeToString().trim()/*.toUpperCase()*/;
					if (ranName.length()>0 && ranName.charAt(0)=='"') ranName = CASAUtil.fromQuotedString(ranName);
//					BaseType ran = (BaseType)ont.getType(ranName);
//					if (ran == null) //ran = ont.declType(ranName); //could recover here, but that doesn't seem right
//						return new Status(-2,CASAUtil.printui("(ONT.ASSERT "+relName+" \""+domName+"\" "+ranName+"): range '"+ranName+"' type does not exist.",ui, params, agent));
					try {
						ont.declMaplet(relName,domName,ranName);
						if (params.getJavaObject("VERBOSE")!=null)
						  ui.println("(ONT.ASSERT "+relName+" \""+domName+"\" "+ranName+")");
					} catch (Throwable e) {
						return new Status(-1,CASAUtil.printui(e.toString(),ui, params, agent));
					}
				}
				
				return new StatusObject<LispObject>(0,org.armedbear.lisp.Lisp.T);
			} catch (Throwable e) {
				return new Status(-1,CASAUtil.printui(e.toString(),ui, params, agent));
			}
  	}
  };
	
  @SuppressWarnings("unused")
	 static final CasaLispOperator ONT__INDIVIDUAL =
  	new CasaLispOperator("ONT.INDIVIDUAL", "\"!Declare an individual in the A-box.\" "+
  			"NAME \"@java.lang.String\" \"!The name of the new individual.\" "+
  			"&OPTIONAL TYPE \"@java.lang.String\" \"The type of the invidiual.\" " +
  			"&KEY ONTOLOGY \"@java.lang.String\" \"!The ontology to put the new individual in (default is the agent current ontology)\" "+
  			"VERBOSE \"!echo the command if verbose/=NIL\""
  			, TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "DECLINDIVIDUAL")
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {
  		try {
  			String name = ((String)params.getJavaObject("NAME"));//.toUpperCase();
      	Ontology ont = CASAUtil.findOntology(agent, params, ui, env);
      	if (ont==null) return new Status(-15,"Can't find ontology to apply operator (ONT.INDIVIDUAL "+name+" ...) to.");
      	String type = ((String)params.getJavaObject("TYPE"));
      	if (type==null)
      		ont.addIndividual(name);
      	else
      		ont.addIndividual(name, type);
				//PACKAGE_CL_USER.internAndExport(name).setDocumentation(Symbol.FUNCTION, new SimpleString("A CASA type symbol"));
				if (params.getJavaObject("VERBOSE")!=null)
				  ui.println("(ONT.INDIVIDUAL \""+name+"\")");
				return new StatusObject<LispObject>(0,org.armedbear.lisp.Lisp.T);
			} catch (Throwable e) {
				return new Status(-1,CASAUtil.printui(e.toString(),ui, params, agent));
			}
  	}
  };
  
  @SuppressWarnings("unused")
	static final CasaLispOperator ONT__TYPE =
  	new CasaLispOperator("ONT.TYPE", "\"!Declare a type in the T-box.\" "+
  			"NAME \"@java.lang.String\" \"!The name of the new type.\" "+
  			"&OPTIONAL TYPE \"@java.lang.String\" \"The type of the invidiual.\" " +
  			"&KEY ONTOLOGY \"@java.lang.String\" \"!The ontology to put the new type in (default is the agent current ontology)\" "+
  			"VERBOSE \"!echo the command if verbose/=NIL\""
  			, TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "DECLTYPE")
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {
  		try {
  			String name = ((String)params.getJavaObject("NAME"));//.toUpperCase();
      	Ontology ont = CASAUtil.findOntology(agent, params, ui, env);
      	if (ont==null) return new Status(-15,"Can't find ontology to apply operator (ONT.TYPE "+name+" ...) to.");
      	String type = ((String)params.getJavaObject("TYPE"));
      	if (type==null)
      		ont.addType(name);
      	else
      		ont.addType(name, type);
				//PACKAGE_CL_USER.internAndExport(name).setDocumentation(Symbol.FUNCTION, new SimpleString("A CASA type symbol"));
				if (params.getJavaObject("VERBOSE")!=null)
				  ui.println("(decltype \""+name+"\")");
				return new StatusObject<LispObject>(0,org.armedbear.lisp.Lisp.T);
			} catch (Throwable e) {
				return new Status(-1,CASAUtil.printui(e.toString(),ui, params, agent));
			}
  	}
  };
  
  @SuppressWarnings("unused")
	static final CasaLispOperator ONT__IMPORT =
  	new CasaLispOperator("ONT.IMPORT", "\"!Import another ontology (ie: make the other ontology a superontology of the agent's ontology).\" "+
  			"NAME \"@java.lang.String\" \"!The name super (imported) ontology.\" "+
  			"&KEY ONTOLOGY \"@java.lang.String\" \"!The ontology to import the new onotlogy into (default is the agent's ontology)\" "+
  			"VERBOSE \"!echo the command if verbose/=NIL\""
  			, TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "DECLTYPE")
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {
  		try {
  			String name = ((String)params.getJavaObject("NAME"));//.toUpperCase();
  			String ontName = (String)params.getJavaObject("ONTOLOGY");
      	Ontology ont = CASAUtil.findOntology(agent, params, ui, env);
      	if (ont==null) return new Status(-15,"Can't find ontology to apply operator (ont.import \""+name+"\""+(ontName==null?"":(" :ontology "+ontName))+") to.");
      	
				ont.addSuperOntologies(name);
				//PACKAGE_CL_USER.internAndExport(name).setDocumentation(Symbol.FUNCTION, new SimpleString("A CASA type symbol"));
				if (params.getJavaObject("VERBOSE")!=null)
				  ui.println("(ont.import \""+name+"\""+(ontName==null?"":(" :ontology "+ontName))+")");
				return new StatusObject<LispObject>(0,org.armedbear.lisp.Lisp.T);
			} catch (Throwable e) {
				return new Status(-1,CASAUtil.printui(CASAUtil.log("error", "", e, true),ui, params, agent));
			}
  	}
  };
  
	/**
   * Lisp operator: (DEFTBOXREL name {keys {rel}}*)<br>
   * <pre>
   * The function's lambda list is:
   *  (RELATION-NAME &KEY BASE INVERSE REFLEXIVE SYMMETRIC TRANSITIVE
   *   ASSIGNABLE)
   * Function documentation:
   *  Define a relation in the agent's casa ontology.
   *  Lambda List:  RELATION-NAME  &KEY BASE  INVERSE  REFLEXIVE  SYMMETRIC  TRANSITIVE  ASSIGNABLE 
   *    RELATION-NAME   The name of the relation.
   *    :BASE           The relation on which this is based.
   *    :INVERSE        This relation is to be an inverse relation (x->y => y->x) of the base relation.
   *    :REFLEXIVE      This relation is a reflexive (x->x) version of the base relation.
   *    :SYMMETRIC      This relation is a symmetric (x->y => y->x) version of the base relation.
   *    :TRANSITIVE     This relation is a transitive (x->y & y->z => x->y) version of the base relation.
   *    :ASSIGNABLE     This relation is assignable (that is one can use it as first argument in a (declMaplet ...) operator
   * </pre>
   */
  @SuppressWarnings("unused")
	static final CasaLispOperator ONT__RELATION =
    new CasaLispOperator("ONT.RELATION", "\"!Define a relation in the agent's casa ontology.\" "+
    		"RELATION-NAME \"!The name of the relation.\" "+
    	  "&KEY "+
    	  "(DOMAIN-CONSTRAINT NIL) \"@casa.ontology.Constraint\" \"!The constraint for the domain elements of this relation.\" "+
    	  "(RANGE-CONSTRAINT NIL) \"@casa.ontology.Constraint\" \"!The constraint for the range elements of this relation.\" "+
    	  "BASE \"!The relation on which this is based.\" "+
    	  "INVERSE \"!This relation is to be an inverse relation (x->y => y->x) of the base relation.\" "+
    	  "REFLEXIVE \"!This relation is a reflexive (x->x) version of the base relation.\" "+
    	  "SYMMETRIC \"!This relation is a symmetric (x->y => y->x) version of the base relation.\" "+
    	  "ASYMMETRIC \"!This relation is an asymmetric (x->y => ~(y->x)) version of the base relation.\" "+
    	  "TRANSITIVE \"!This relation is a transitive (x->y & y->z => x->y) version of the base relation.\" "+
  			"USES \"@java.lang.String\" \"!This relation uses relation [arg] as an equivalence relation.\" "+
    	  "ASSIGNABLE \"!This relation is assignable (that is one can use it as first argument in a (declMaplet ...) operator\" "+
  			"ONTOLOGY \"@java.lang.String\" \"!The ontology to put the new type in (default is the agent current ontology)\" "+
  			"VERBOSE \"!echo the command if verbose/=NIL\""
    		, TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "DECLRELATION")
    {
      @Override
			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {

      	Object obj = params.getJavaObject("RELATION-NAME");
  			if (!(obj instanceof String)) 
  				return new Status(-1,CASAUtil.printui("The first parameter of (ONT.RELATION relation-name ...), must be a String or a Symbol.",ui, params, agent));
  			final String relName = ((String)obj)/*.toUpperCase()*/;
  			
      	Ontology ont = CASAUtil.findOntology(agent, params, ui, env);
      	if (ont==null) return new Status(-15,"Can't find ontology to apply operator (ONT.RELATION "+relName+" ...) to.");
      	
      	Constraint domConstraint = (Constraint)params.getJavaObject("DOMAIN-CONSTRAINT"); 
      	Constraint ranConstraint = (Constraint)params.getJavaObject("RANGE-CONSTRAINT"); 
      	
//      	ConcreteRelation base = null;
      	String baseName = null;
  			if (params.containsKey("BASE")) {
  			  obj = params.getJavaObject("BASE");
  			  if (!(obj instanceof String)) 
  			  	return new Status(-2,CASAUtil.printui("The value of key :BASE of (ONT.RELATION relation-name :BASE x), must be a String or a Symbol.",ui, params, agent));
  			  else
  			  	baseName = (String)obj;
//  			  try {
//						base = obj==null?null:(ConcreteRelation)ont.getRelation(((String)obj)/*.toUpperCase()*/);
//					} catch (IllegalOperationException e) {}//prop.get("deftboxrel",":base",Relation.class,0/*lineno*/);
//  			  if (base==null) 
//  			  	return new Status(-6, CASAUtil.printui("Base relation "+obj+" not found",ui, params, agent));
  			}

  			Set<Relation.Property> relProp = new TreeSet<Relation.Property>();
  			String usesRelation = null;
  		  if (params.containsKey("INVERSE"))    relProp.add(Relation.Property.INVERSE);
  		  if (params.containsKey("REFLEXIVE"))  relProp.add(Relation.Property.REFLEXIVE);
  		  if (params.containsKey("SYMMETRIC"))  relProp.add(Relation.Property.SYMMETRIC);
  		  if (params.containsKey("ASYMMETRIC"))  relProp.add(Relation.Property.ASYMMETRIC);
  		  if (params.containsKey("TRANSITIVE")) relProp.add(Relation.Property.TRANSITIVE);
  		  if (params.containsKey("ASSIGNABLE")) relProp.add(Relation.Property.ASSIGNABLE);
  		  if (params.containsKey("USES")) {
  		  	relProp.add(Relation.Property.USES);
//  		  	String s = (String)params.getJavaObject("USES");
//  		  	try {
//						usesRelation = ont.getRelation(s);
//					} catch (IllegalOperationException e) {
//						throw new LispException("Can't find 'uses' relation "+s+" in ontology "+ont.getName(), e);
//					}
  		  }
  		  
  		  boolean warning = false;
  		  try {
  		  	if (ont.isRelation(relName)) 
  		  		warning = true;
  		    ont.declRelation(relName, baseName, relProp, domConstraint, ranConstraint, usesRelation);
  			} catch (Throwable e) {
  				return new Status(-3, CASAUtil.printui(e.toString(),ui, params, agent));
  			}
  			return new StatusObject<Relation>(warning?1:0,warning?"Overwriting previous definition of relation "+relName:null);//new DataDesc(rel, Cons.Type.OBJECT);
      	
    }
  };

	Set<String> isAncestor(String child) throws IllegalOperationException;

	Set<String> isChild(String parent) throws IllegalOperationException;

	Set<String> isDescendant(String parent) throws IllegalOperationException;
  
}