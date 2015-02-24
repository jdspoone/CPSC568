package casa.ontology.v3;

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
import casa.ontology.Constraint;
import casa.ontology.Ontology;
import casa.ontology.Relation;
import casa.ontology.Relation.Property;
import casa.ontology.Type;
import casa.ontology.v3.BaseType.Role;
import casa.ontology.v3.ConstraintSimple.Individual;
import casa.ui.AgentUI;
import casa.util.CASAUtil;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.armedbear.lisp.Cons;
import org.armedbear.lisp.ControlTransfer;
import org.armedbear.lisp.Environment;
import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.Lisp;
import org.armedbear.lisp.LispError;
import org.armedbear.lisp.LispObject;
import org.armedbear.lisp.SimpleString;
import org.armedbear.lisp.Symbol;


/**
 * Implements an efficient ontology of terms and relations.  A primary feature of this ontology
 * is its ability to define relations based on other relations and maintain the integrity of
 * the relationships between these relations.  It's persistent form consists of lisp-like
 * sentences of the following form: <br> <pre> 
 * (declRelation <em>rel</em>) 
 * (declRelation <em>rel</em> :base <em>rel</em> { :inverse|:transitive|:reflexive|:symmetric
 *                             | :assignable
 *                             | :domain-constraint <em>constraint</em>
 *                             | :range-constraint <em>constraint</em>
 *                             | <b>:uses <em>rel</em></b>
 *                             }*)
 * (declType <em>type</em>)
 * (declIndividual <em>ind</em>) 
 * (declMaplet <em>rel type</em> {<em>type</em>}|({<em>type</em>}*))
 * (constraint <em>type</em>? {:type-only|:individual-only}?) 
 * (<em>rel id id</em>) 
 * (<em>rel id</em>) 
 * (describe '{<em>id</em>|<em>rel</em>}) 
 * <b>
 * (declSlot <em>id slot</em> { :type <em>type</em> | :value <em>val</em> | :uses <em>rel</em> }*) 
 * (<em>typeExp</em>) 
 * (<em>typeExp id</em>)
 * (slot <em>id</em> <em>slot</em>)
 * </b>
 * </pre> where 
 * <ul><dl> 
 * <dt><em>rel, type, ind</em> <dd> is either an identifier representing a relation, type, or individual.  However, if 
 * rel/type/ind is first being declared (such as in "(declType <em>type</em>)") then the type/relation/individual should be a quoted string
 * (since it isn't already a type/relation/individual).
 * <dt><em>id</em> <dd> is an identifier representing either a type or an indivudal (as above). 
 * </dl></ul> 
 * The functions are defined informally as follows: 
 * <ul><dl> 
 * <dt>declRelation<dd> define a base relation (which is essentially a list of pairs  of types 
 * (domain-element, range-element) or a relation based on another relation with certain additional 
 * properties as follows: 
 *   <dl>
 *   <dt>:inverse<dd> The relation is the inverse of the base relation (axb iff bxa in the base relation).  
 *   <dt>:transitive<dd> The relation is transitive (axb /\ bxc -> axc).  
 *   <dt>:reflexive<dd> The relation is reflexive (axa always holds).  
 *   <dt>:symmetric<dd> The relation is symmetric (axb -> bxa).
 *   <dt>:assignable<dd> The relation is assignable.  An assignable relation can appear  in 
 *   (<em>rel type type</em>) expression, but a non-assignable relation cannot.  
 *   By default relations based on other relations are NOT assignable, but base relations are 
 *   assignable.   Transitive relations cannot be assignable because the maplet in the base 
 *   relation is ambiguous.
 *   <dt>:domain-constraint <em>constraint</em><dd> A constraint on the type of the domain of the relation.
 *   <dt>:range-constraint <em>constraint</em><dd> A constraint on the type of the range of the relation.
 *   <dt>:uses <em>rel</em><dd> <b>This relation will apply relation <em>rel</em> before computing results 
 *     (eg: isa would normally use equal to allow synonyms in the type hierarchy).</b>
 *   </dl>
 * <dt>declType<dd> Declares an identifier to be a type
 * <dt>declIndividual<dd> Declares an identifier to be an individual
 * <dt>declMaplet<dd> define one or more maplets (domain, range) in 
 * relation.  If the relation is isa-parent (or isa-child)  <em>settbox</em> declares a type. 
 * <dt>constraint<dd>Creates a constraint by declaring the most general type and/or weather the object
 * must be an individual only or a type only.
 * <dt><em>rel</em><dd>  
 *   <ul>
 *   <li>2-parameter form: returns true if the relation exists between the two arguments. 
 *   <li>1-parameter from: returns a list of types that appear in the range of the  relation, 
 *   given the domain as the first argument. 
 *   </ul>
 * <dt>describe<dd> returns a lisp description of the type including definitions of any types
 * and relations that the argument type is dependent on. 
 * </ul> 
 * <p> To facilitate the type lattice, there are 8 primitive relations and one type that pre-defined: 
 * <pre>
 * (deftboxrel "isa-parent" :domain-constraint (constraint :type type) :range-constraint (constraint :type type)) 
 * (deftboxrel "isa-ancestor"  :base isa-parent   :transitive) 
 * (deftboxrel "isa"           :base isa-ancestor :transitive :reflexive ) 
 * (deftboxrel "isa-child"     :base isa-parent   :inverse :assignable) 
 * (deftboxrel "isa-decendant" :base isa-child    :inverse :transitive ) 
 * (declRelation "isequal" :transitive :reflexive :symmetric :assignable)
 * (declType "TOP")  
 * </pre>
 * <p>Copyright: Copyright 2003-2014,Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class CASAOntology implements Ontology {

	public static final String DEFAULT_FILE_EXTENSION = ".ont.lisp";

	/** The primitive ontology */
	static CASAOntology primitiveOntology;
	/** The name of the primitive ontology */
	static final String PRIMITIVE_ONTOLOGY = "primitiveOntology";

	
	/** A primitive relation */
	static ConcreteRelation isaParent;
	/** A primitive relation */
	static ConcreteRelation isaAncestor;
	/** A primitive relation */
	static ConcreteRelation isa;
	/** A primitive relation */
	static ConcreteRelation isaChild;
	/** A primitive relation */
	static ConcreteRelation isaDescendant;
	/** A primitive relation */
	static ConcreteRelation isequal;
	/** A primitive relation */
	static ConcreteRelation properInstanceOf;
	/** A primitive relation */
	static ConcreteRelation instanceOf;
  /** The name of the primitive relation isaParent */
	public static final String ISAPARENT = "isa-parent";
  /** The name of the primitive relation isaAncestor */
	public static final String ISAANCESTOR = "isa-ancestor";
  /** The name of the primitive relation isa */
	public static final String ISA = "isa";
  /** The name of the primitive relation isaChild */
	public static final String ISACHILD = "isa-child";
  /** The name of the primitive relation isaDescendant */
	public static final String ISADESCENDANT = "isa-descendant";
  /** The name of the primitive relation isequal */
	public static final String ISEQUAL = "isequal";
  /** The name of the primitive relation isaDescendant */
	public static final String PROPERINSTANCEOF = "proper-instance-of";
  /** The name of the primitive relation isequal */
	public static final String INSTANCEOF = "instance-of";

	/** A primitive type */
  static Type top;
  /** The name of the primitive type top */
	public static final String TOP = "TOP";
	/** A primitive type */
  static Type bottom;
  /** The name of the primitive type bottom */
	public static final String BOTTOM = "BOTTOM";
	
	static {
		try {
			primitiveOntology = new CASAOntology(PRIMITIVE_ONTOLOGY);
		  top = primitiveOntology.declType(TOP);
		  isequal = primitiveOntology.declRelation(ISEQUAL, null, new Relation.Property[]{Relation.Property.TRANSITIVE, Relation.Property.REFLEXIVE, Relation.Property.SYMMETRIC, Relation.Property.ASSIGNABLE}, null, null);

		  isaParent = primitiveOntology.declRelation(ISAPARENT, null, new Relation.Property[]{Relation.Property.USES}, /*new Constraint(Individual.Type, null)*/ null, new ConstraintSimple(Individual.Type, null, null, primitiveOntology, null), isequal);
		  isaAncestor = primitiveOntology.declRelation(ISAANCESTOR, isaParent, new Relation.Property[]{Relation.Property.TRANSITIVE}, null, null);
		  isa = primitiveOntology.declRelation(ISA, isaAncestor, new Relation.Property[]{Relation.Property.REFLEXIVE, Relation.Property.ASSIGNABLE}, null, null);
		  isaChild = primitiveOntology.declRelation(ISACHILD, isaParent, new Relation.Property[]{Relation.Property.INVERSE}, null, null);
		  isaDescendant = primitiveOntology.declRelation(ISADESCENDANT, isaChild, new Relation.Property[]{Relation.Property.TRANSITIVE}, null, null);

		  properInstanceOf = primitiveOntology.declRelation(PROPERINSTANCEOF, isaParent, (Relation.Property[])null, new ConstraintSimple(Individual.Individual, null, null, primitiveOntology, null), new ConstraintSimple(Individual.Type, null, null, primitiveOntology, null));
		  //instanceOf = primitiveOntology.declRelation(INSTANCEOF, properInstanceOf, new Relation.Property[]{Relation.Property.TRANSITIVE}, null, null);
		  instanceOf = primitiveOntology.declRelation(INSTANCEOF, isa, (Relation.Property[])null, new ConstraintSimple(Individual.Individual, null, null, primitiveOntology, null), new ConstraintSimple(Individual.Type, null, null, primitiveOntology, null));

		} catch (IllegalOperationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The name of the ontology.
	 */
	private String name;
	
	/**
	 * The filename that this ontology was loaded from.  Note that this value cannot always
	 * be filed out accurately (especially when more than one ontology is loaded from
	 * the same file), so you can't assume that this value is going to be available.
	 */
	private String filePath;
	
	/**
	 * @return the name of this ontology
	 */
	@Override
	public String getName() {return name;}
	
	/**
	 * Records all "public" (within the process) ontologies.  Ontologies with no
	 * name (null or "") or with a name beginning with a dot (.) are NOT
	 * recorded here (these are considered "private"). 
	 */
	static Map<String, CASAOntology> allOntologies;

	/** The list of the super ontologies of this ontology */
	List<CASAOntology> superOntologies = new LinkedList<CASAOntology>();
  
	/**
	 * @return true iff the ontology has no
	 * name (null or "") or has a name beginning with a dot (.).
	 */
	public boolean isPrivate() {
		return name==null || name.length()==0 || name.charAt(0)=='.';
	}

	/**
	 * Find or read in a CASAOntology.  If the named ontology has been created already it is
	 * returned, otherwise it is instantiated from a file ("[ontology-name].ont.lisp"), recorded and returned.
	 * The ontology is NOT recorded if is private (see {@link #isPrivate()}).
	 * @param name the name of the ontology as specified in it's constructor and return by {@link #getName()}
	 * @return the found ontology
	 */
	public static CASAOntology getOntology(String name) {
		//name = name.toUpperCase();
		if (allOntologies!=null && allOntologies.containsKey(name)) { //we already have this ontology, just return that
			CASAOntology found = allOntologies.get(name);
			return found;
		}

		// we need to search for this as a file
		TransientAgent agent = null;
		if (Thread.currentThread() instanceof TransientAgent) {
			agent = (TransientAgent)Thread.currentThread();
		}
		String path;
		try {
			path = TransientAgent.findFileResourcePath(name+DEFAULT_FILE_EXTENSION);
		} catch (Throwable e) {
			CASAUtil.log("error", "CASAOntology.getOntology(\""+name+DEFAULT_FILE_EXTENSION+"\")", e, true);
			return null;
		}

		if (path!=null) {
			casa.abcl.Lisp.abclEval(agent, null, null, "(load \""+path.replaceAll("\\\\", "\\\\\\\\")+"\")", null); // the replaceAll() is for windows
		}
		CASAOntology found = allOntologies==null?null:allOntologies.get(name);
		if (found!=null) found.filePath = path;
		return found;
	}
	
	/**
	 * Constructs a new CASAOntology
	 * @param name
	 * @param superOntologies
	 * @return a newly constructed CASAOntology
	 * @throws IllegalOperationException If a CASAOntology with that <em>name</em> already exists.
	 */
	public static CASAOntology makeOntology(String name, Ontology... superOntologies) throws IllegalOperationException {
		if (getOntology(name)!=null)
			throw new IllegalOperationException("Ontology "+name+" already exists.");
		return new CASAOntology(name, (CASAOntology[])superOntologies);
	}
	
	public static CASAOntology makeOntology2(String name, String... superOntologies) throws IllegalOperationException {
		CASAOntology[] onts = null;
		if (superOntologies!=null) {
			onts = new CASAOntology[superOntologies.length];
			int i = 0;
			for (String n: superOntologies) {
				onts[i] = getOntology(n);
				if (onts[i++]==null)
					throw new IllegalOperationException("Can't find super ontology: "+n);
			}
		}
		return makeOntology(name,onts);
	}
	
	/**
	 * Instantiates a new ontology with parents specified by superOntologies.  The ontology
	 * is registered in {@link #allOntologies} unless it's name is null, empty, or begins with ".".
	 * @param name
	 * @param superOntologies
	 * @throws IllegalOperationException 
	 */
	private CASAOntology(String name, CASAOntology... superOntologies) throws IllegalOperationException {
		this.name = name;
		addSuperOntologies(superOntologies);


		if (name!=null && name.length()>0 && name.charAt(0)!='.') {
			if (allOntologies==null) 
				allOntologies = new TreeMap<String, CASAOntology>(new StringComp());
			allOntologies.put(name, this);
		}
	}

	@Override
	public void addSuperOntologies(Ontology... parentOntologies) throws IllegalOperationException {
		if (parentOntologies!=null && parentOntologies.length>0) {
			for (Ontology o: parentOntologies) {
				if (!(o instanceof CASAOntology))
					throw new IllegalOperationException("Can't add ontology "+o.getName()+" of type "+o.getClass().getCanonicalName()+"; must be a subtype of CASAOntology");
				CASAOntology co = (CASAOntology)o;
				if (co!=null) {
					if (co.isPrivate() && !isPrivate()) 
						throw new IllegalOperationException("Can't add private ontology "+o.getName()+" as a super ontology of public ontology "+getName());
					superOntologies.add(co);
				}
			}
			relations.invalidateSearchPathCache();
			tBox.invalidateSearchPathCache();
		}
		if (this.superOntologies.isEmpty() && !this.name.equals(PRIMITIVE_ONTOLOGY)) 
			addSuperOntologies(primitiveOntology);
	}
	
	@Override
	public void addSuperOntologies(String... superOntologyNames) throws IllegalOperationException {
		for (String o: superOntologyNames) {
			Ontology ont = getOntology(o);
			if (ont==null)
				throw new IllegalOperationException("Can't find ontology "+o);
			addSuperOntologies(o);
		}
	}

	/**
	 * A specialized {@link HierarchicalDictionary} used to contain type objects in this ontology.
	 */
	HierarchicalDictionary<BaseType> tBox = new HierarchicalDictionary<BaseType>(this, this) {
		@Override protected HierarchicalDictionary<BaseType> getDictionary(CASAOntology so) {return so.tBox;}
		};

	/**
	 * A specialized {@link HierarchicalDictionary} used to contain relation objects in this ontology.
	 */
	HierarchicalDictionary<Relation> relations = new HierarchicalDictionary<Relation>(this, this) {
		@Override protected HierarchicalDictionary<Relation> getDictionary(CASAOntology so) {return so.relations;}
		};

	
  /**
   * Convenience method that calls {@link #declRelation(String, ConcreteRelation, Property[], ConstraintSimple, ConstraintSimple, Object...)} after converting
   * the <em>properties</em> parameter.
   * @param name
   * @param basedOn
   * @param properties
   * @param otherParams any additional parameters
   * @return a new relation
   * @throws IllegalOperationException
   * @see #declRelation(String, ConcreteRelation, Property[], ConstraintSimple, ConstraintSimple, Object...)
   */
  public ConcreteRelation declRelation(String name, ConcreteRelation basedOn, Relation.Property[] properties, ConstraintSimple domConstraint, ConstraintSimple ranConstraint, Object... otherParams) throws IllegalOperationException {
  	TreeSet<Property> prop = new TreeSet<Property>();
  	if (properties!=null) {
  	  for (Property p: properties) {
  		  prop.add(p);
  	  }
  	}
  	return declRelation(name,basedOn,prop,domConstraint,ranConstraint,otherParams);
  }

  @Override
	public void declRelation(final String name, String basedOn, Set<Relation.Property> properties, Constraint domConstraint, Constraint ranConstraint, Object... otherParams) throws IllegalOperationException {
  	ConcreteRelation base = null;
	  try {
			base = basedOn==null?null:(ConcreteRelation)getRelation(basedOn);
		} catch (IllegalOperationException e) {}//prop.get("deftboxrel",":base",Relation.class,0/*lineno*/);
	  if (base==null) 
	  	throw new IllegalOperationException("Base relation "+basedOn+" not found");
	
  	declRelation(name, base, properties, domConstraint, ranConstraint, otherParams);
  }
  
  /**
   * Create and record a new relation in this ontology. This method automatically instantiates a
   * lisp operator with the same name as the relation to test a pair in the relation.
   * @param name The name of the new relation.
   * @param basedOn The relation on which to base this relation; if null indicates that this relation is a either a primitive
   * relation itself (no <em>properties</em> case), or (if there are <em>properties</em> specified)
   * we should generate an auxilliary primitive relation (named uniquely based on the relation name).
   * on which to base this relation.
   * @param properties the set of properties (INVERSE, SYMMETRIC, ASYMMETRIC, TRANSITIVE, REFLEXIVE, USES) for that the new relation should
   * exhibit.
   * @param otherParams TODO
   * @return the relation object recorded in this ontology.
   * @throws IllegalOperationException if the relation name attempts to override a reserved relation or you are attempting
   * base a primitive (no properties) relation on another relation (basedOn!=null & properties.isEmpty).
   */
  public ConcreteRelation declRelation(final String name, ConcreteRelation basedOn, Set<Relation.Property> properties, Constraint domConstraint, Constraint ranConstraint, Object... otherParams) throws IllegalOperationException {
  	if (primitiveOntology.getRelation(name)!=null) {
  		throw new IllegalOperationException("Cannot override reserved relation '"+name+"'");
  	}
  	if (properties==null)
  		properties = new TreeSet<Relation.Property>();
	
  	ConcreteRelation ret=null;
    if (canBeBase(properties)) {
    	if (basedOn==null) {
    		ret = basedOn = new PrimitiveRelation(name,this,domConstraint,ranConstraint);
    	}
    	else {
    		//throw new IllegalOperationException("Relation "+name+" is without properties, therefore should not be based-on other relations: "+basedOn.getName());
    	}
    }
    else { // we have properties to worry about
      boolean assignable = isAssignable(properties);
    	if (assignable) {
    		if (basedOn==null) {
    		  ret = basedOn = new PrimitiveRelation(name,this,domConstraint,ranConstraint);
    		}
    		else {
    			ret = basedOn;
    		}
    	} 
    	else { // !assignable
    		if (basedOn==null) {
    			assignable = true;
    			//we need to invent a new base
    			String baseName = name; //getUniqueRelationName(name);
    			basedOn = new PrimitiveRelation(baseName, this,domConstraint,ranConstraint);
    		}
    		else {
    		}
  			ret = basedOn;
    	}
     	if (properties.contains(Relation.Property.USES )) {
 		    if (otherParams==null || otherParams.length<1) {
 		    	throw new IllegalOperationException("for property USES, declRelation() requires the first otherParams argument to be a ConcreteRelations; found: "+(otherParams==null?"<empty>":(otherParams.length==0?"<missing>":otherParams[0].getClass())));
 		    }
 		    if (otherParams[0] instanceof String) {
  		  	try {
  		  		otherParams[0]= getRelation((String)otherParams[0]);
					} catch (IllegalOperationException e) {
						throw new LispException("Can't find 'uses' relation "+otherParams[0]+" in ontology "+getName(), e);
					}
 		    }
 		    if (otherParams[0] instanceof ConcreteRelation)
   			  ret = new UsesRelation(name,this,ret,(ConcreteRelation)otherParams[0],true,domConstraint,ranConstraint, null); 
     	}
    	if (properties.contains(Relation.Property.INVERSE   )                                             ) 
    		ret = new InverseRelation(name,this,ret,true,domConstraint,ranConstraint, null);
    	if (properties.contains(Relation.Property.SYMMETRIC ) && !basedOn.hasProperty(Property.SYMMETRIC )) 
    		ret = new SymmetricRelation(name,this,ret,false,domConstraint,ranConstraint);
    	if (properties.contains(Relation.Property.TRANSITIVE) && !basedOn.hasProperty(Property.TRANSITIVE)) 
    		ret = new TransitiveRelation(name,this,ret,false,domConstraint,ranConstraint);
     	if (properties.contains(Relation.Property.REFLEXIVE ) && !basedOn.hasProperty(Property.REFLEXIVE )) 
     		ret = new ReflexiveRelation(name,this,ret,false,domConstraint,ranConstraint);
    	if (properties.contains(Relation.Property.ASYMMETRIC ) && !basedOn.hasProperty(Property.ASYMMETRIC )) 
    		ret = new AsymmetricRelation(name,this,ret,false,domConstraint,ranConstraint);
   	  if (ret instanceof BasedRelation && (assignable || properties.contains(Relation.Property.ASSIGNABLE))) 
    		((BasedRelation)ret).assignable = true;
    }
 	  if (ret==null) ret = new MirrorRelation(name,this,basedOn,true,domConstraint,ranConstraint);

    // Sanity check
  	if (ret.hasProperty(Relation.Property.SYMMETRIC) && ret.hasProperty(Relation.Property.ASYMMETRIC)) {
  		throw new IllegalOperationException("Relation '"+name+"' cannot be both SYMMETRIC and ASYMMETRIC.");
  	}
		
    //ret.setName(name); //reset the name from the unique name we gave it back to it's originally intended name.
		ret.setVisible(true);

		relations.put(name, ret);
		
		final CASAOntology THIS = this; 

    new CasaLispOperator(new Name(this, name).toString(),"\"!Test weather items are related in the "+name+" relation\" "
    		+"DOMAIN \"@java.lang.String\" "
    		+"&OPTIONAL RANGE \"@java.lang.String\" "
  			+"&KEY ONTOLOGY \"@java.lang.String\" \"!The ontology use (default is the agent current ontology)\""
    		,TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), name)
    {
    	{
    		// make sure create operators for both the qualified and unqualified names
    		Name nameObj = new Name(name);
    	  if (nameObj.getOntology()==null) { // the parameter name is not fully-qualified
    	  	makeSynonym(new Name(THIS, name).toString());
    	  }
    	  else { // the parameter name is fully-qualified
    	  	makeSynonym(nameObj.getName());
    	  }
    	}
    	private String relName = name;
      @Override
			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {
      	try {
        	CASAOntology ont = null;
        	if (agent!=null) {
        		Ontology o = agent.getOntology();
        		if (o instanceof CASAOntology) {
        			ont = (CASAOntology)o;
        		}
        	}
        	if (ont==null) {
        		ont = THIS;
        	}
        	        	
	      	String domain = (String)params.getJavaObject("DOMAIN");
	      	if (params.containsKey("RANGE")) {
	      	  String range = (String)params.getJavaObject("RANGE");
	      	  Status stat = new StatusObject<org.armedbear.lisp.LispObject>(0,ont.relatedTo(relName, domain, range)?org.armedbear.lisp.Lisp.T:org.armedbear.lisp.Lisp.NIL);
					  return stat;
	      	}
	      	else {
	      		Set<String> related = ont.relatedTo(relName, domain);
	      		LispObject cons = Lisp.NIL;
	      		if (related!=null) {
	      			for (String type: related) {
	      				cons = new Cons(new SimpleString(type),cons);
	      			}
	      		}
	      	  Status stat = new StatusObject<org.armedbear.lisp.LispObject>(0,cons);
					  return stat;
	      	}
				} catch (Throwable e) {
					ui.print(e.toString());
					return new StatusObject<LispObject>(-1, new LispError(e.toString()));
				} 
			}
    };
		
		return ret;
  }
  
  /**
   * @param properties
   * @return true iff properties is null or empty.
   */
  private boolean canBeBase(Set<Relation.Property> properties) {
  	return (properties==null || properties.isEmpty());
  }

  /**
   * @param properties
   * @return true iff there is no property that is not assignable.
   */
  private boolean isAssignable(Set<Relation.Property> properties) {
  	if (properties!=null && !properties.isEmpty()) {
  		for (Relation.Property p: properties) {
  			if (!p.assignable())
  				return false;
  		}
  	}
  	return true;
  }
  
  /**
   * Adds a new type to this ontology.  Also defines the type as a symbol in the Lisp system.
   * @param name
   * @return the type that was added.
   * @throws IllegalOperationException if the name was illegal, or the type couldn't be added for some reason.
   */
  public BaseType declType(String name) throws IllegalOperationException {
  	BaseType ret;
		ret = new SimpleType(name,null,this);
  	tBox.put(name, ret);
  	setSymbol(name);
  	return ret;
  }
  
  /**
   * Adds a new individual to this ontology.  Also defines the individual as a symbol in the Lisp system.
   * @param name
   * @return the type that was added.
   * @throws IllegalOperationException if the name was illegal, or the type couldn't be added for some reason.
   */
  public BaseType declIndividual(String name) throws IllegalOperationException {
  	SimpleType ret = (SimpleType)declType(name);
		ret.setIndividual();
  	return ret;
  }
  
  /**
   * Set TWO symbols for the parameter <em>name</em>: one for the unqualified name, and 
   * another for the qualified name (ontology and name).
   * @param name the symbol to be defined; this may be the qualified or unqualified name.
   * @throws IllegalOperationException if the name was illegal.
   */
  private void setSymbol(String name) throws IllegalOperationException { 
  	// make sure create operators for both the qualified and unqualified names
  	Name nameObj = new Name(this, name);
  	String qualified = nameObj.toString();
  	String unqualified = nameObj.getName();
  	Symbol sym = org.armedbear.lisp.Lisp.PACKAGE_CL_USER.internAndExport(qualified.toUpperCase());
  	sym.setSymbolValue(new SimpleString(qualified));
  	sym.setDocumentation(Symbol.VARIABLE, new SimpleString("A CASA type symbol"));
  	sym = org.armedbear.lisp.Lisp.PACKAGE_CL_USER.internAndExport(unqualified.toUpperCase());
  	sym.setSymbolValue(new SimpleString(unqualified));
  	sym.setDocumentation(Symbol.VARIABLE, new SimpleString("A CASA type symbol"));
  }
  
  /**
   * Adds a new pair in <em>relationName</em> to this ontology.  After this call, the lisp 
   * operator (relationName domainName rangeName) should return true.
   * @param relationName
   * @param domainName
   * @param rangeName
   * @throws IllegalOperationException  if the name was illegal, or the maplet couldn't be added for some reason.
   * @see #declMaplet(Relation, BaseType, BaseType)
   */
  @Override
	@SuppressWarnings("javadoc")
	public void declMaplet(String relationName, String domainName, String rangeName) throws IllegalOperationException {
  	Relation relation = getRelation(relationName);
  	BaseType domain = getType(domainName);
  	if (domain==null)
  		domain = declType(domainName);
  	BaseType range = getType(rangeName);
  	declMaplet(relation, domain, range);
  }
  
  /**
   * Adds a new pair in <em>relationName</em> to this ontology.  After this call, the lisp 
   * operator (relationName domainName rangeName) should return true.
   * @param relation
   * @param domainName
   * @param rangeName
   * @throws IllegalOperationException  if the name was illegal, or the maplet couldn't be added for some reason.
   * @see #declMaplet(Relation, BaseType, BaseType)
   */
  @SuppressWarnings("javadoc")
	public void declMaplet(Relation relation, String domainName, String rangeName) throws IllegalOperationException {
  	BaseType domain = getType(domainName);
  	if (domain==null)
  		domain = declType(domainName);
  	BaseType range = getType(rangeName);
  	declMaplet(relation, domain, range);
  }
  
  /**
   * Adds a new pair in <em>relationName</em> to this ontology.  After this call, the lisp 
   * operator (relationName domainName rangeName) should return true.
   * @param relationName
   * @param domainName
   * @param rangeName
   * @throws IllegalOperationException  if the relation is non-assignable, or the maplet couldn't be added for some reason.
   */
  protected void declMaplet(Relation relation, BaseType domain, BaseType range) throws IllegalOperationException {
  	if (relation==null || domain==null || range==null) 
  		throw new IllegalOperationException("declMaplet("+relation.getName()+", "+domain+", "+range+"): illegal argument(s).");
  	if (!relation.isAssignable()) 
  		throw new IllegalOperationException("Cannot declare TBox maplet for non-assignable relation '"+relation.getName()+"'");
  	if (getType(domain.getName())==null) 
  		declType(domain.getName());
  	relation.add(domain, range);
  }
  
  /**
   * Determines if <em>domainName</em> and <em>rangeName</em> are related through relation <em>relationName</em> 
   * WITHOUT TAKING INTO ACCOUNT the isa relationships of the domain and range.
   * @param relationName
   * @param domainName
   * @param rangeName
   * @return true iff <em>domaionName</em> and <em>rangeName</em> are related through relation <em>relationName</em>
   * @throws IllegalOperationException if any of the parameters can't be found, etc
   * @see #queryMaplet(Relation, Type, Type)
   */
  @SuppressWarnings("javadoc")
	public boolean queryMaplet(String relationName, String domainName, String rangeName) throws IllegalOperationException {
  	Relation relation = getRelation(relationName);
  	Type domain = getType(domainName);
  	Type range = getType(rangeName);
  	return queryMaplet(relation, domain, range);
  }
  
  /**
   * Determines if <em>domainName</em> and <em>rangeName</em> are related through relation <em>relationName</em>
   * WITHOUT TAKING INTO ACCOUNT the isa relationships of the domain and range.
   * @param relation
   * @param domain
   * @param range
   * @return true iff <em>domaionName</em> and <em>rangeName</em> are related through relation <em>relationName</em>
   * @throws IllegalOperationException if any of the parameters can't be found, etc
   */
  protected boolean queryMaplet(Relation relation, Type domain, Type range) {
  	return relation.relatedTo(domain, range);
  }
  
	/**
	 * Returns a string describing the ontolog in the following form (for example):
	 * <pre>
	 * (declOntology "events" ;loaded from file /Apple/CASA/dataFiles/events.ont.lisp
	 *  ��'(isa) ;super ontologies (the search path is events, isa)
	 *  ��'( ;ontology declarations
	 *   ����(declType "event")
	 *   ����(declType "event_AdvertisementAdded")
	 *       ...
	 *       (declMaplet event isa:TOP)
	 *       (declMaplet event_advertisementEvent events:event)
	 *       ...
 ��*    )
   * ) 
	 * </pre>
	 * Note that some of the commented information might not be available.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		
		// print the ontology declaration with a comment about where it was loaded from
		b.append("(declOntology \"")
		.append(getName())
		.append("\"");
		if (filePath!=null) b.append(" ;loaded from file ").append(filePath);
		
		// print the super ontologies with a comment listing the search path if available
		int count = 0;
		b.append("\n  '(");
		for (Ontology o: superOntologies) {
			if (o!=primitiveOntology) {
				b.append(o.getName()).append(' ');
				count++;
			}
		}
		if (count>0) b.setLength(b.length()-1);
		b.append(") ;super ontologies");
		if (tBox.cachedSearchPath!=null && tBox.cachedSearchPath.size()>0) {
			b.append(" (the search path is: ");
			for (HierarchicalDictionary<BaseType> d: tBox.cachedSearchPath) b.append(d.owner.getName()).append(", ");
			b.setLength(b.length()-2);
			b.append(")");
		}
		
		// print the relation defs
		b.append("\n  '( ;ontology declarations");

		//print the relations in this ontology
		StringBuffer b2 = new StringBuffer();
		count = 0;
		int countInvAndVisible = 0;
		for (Relation r: relations.values()) r.setMark(false); //clear marks
		for (boolean someleft=true; someleft;) {
			someleft = false;
			for (Relation rel: relations.values()) {
					if (!rel.isMark()) {
					  if ((rel instanceof BasedRelation) && ((BasedRelation)rel).base !=null 
					  		&& ( ((BasedRelation)rel).base.isVisible() ? !((BasedRelation)rel).base.isMark() : false )) {
					  	someleft = true;
					  }
					  else {
						  if (rel.isVisible()) {
						  	b2.append("\n    ").append(rel);
						  	b2.append("\n      ;-its composition is: ").append(((ConcreteRelation)rel).toStringComment(this));
						  	count++;
						  }
						  countInvAndVisible++;
					  	rel.setMark(true);
					  }
					}
			}
		}
		b.append("\n    ; "+(count>0?count:"No")+" visible relations defined (of "+countInvAndVisible+" total relations)"+(count>0?":":".")).append(b2);

		//print all the DECLTYPEs
		b2.setLength(0);
		count = 0;
		for (BaseType t: tBox.values()) {
			b2.append("\n    (declType \"")
			.append(t.getUnqualifiedName())
			.append("\")");
			count++;
		}
		b.append("\n    ; "+(count>0?count:"No")+" types defined"+(count>0?":":".")).append(b2);
		
		
		//collect all the relations that any of the types in this ontology refer to
		Vector<Relation> allRelations = new Vector<Relation>();
		allRelations.add(isaParent); //we want to print the isa relations first
		for (BaseType t: tBox.values()) {
			for (Role r: t.roles.values()) {
			  if (!r.inDomain.isEmpty() && !allRelations.contains(r.rel)) {
			  	allRelations.add(r.rel);
			  }
			}
		}
		//remove any relations that are "below" us in the hierarchy
		top:
		for (int i = 0; i<allRelations.size(); i++) {
			Relation rel = allRelations.elementAt(i);
			for (Relation rel2: relations) {
				if (rel==rel2) {
					continue top;
				}
			}
			allRelations.remove(i--);
		}
		
		//print all the relation maplets for all the relations in this or superontologies.
		b2.setLength(0);
		count = 0;
		for (Relation rel: allRelations) {
			for (BaseType t: tBox.values()) t.resetMark(); //clear marks
			for (boolean someleft=true; someleft;) {
				someleft = false;
				for (BaseType t: tBox.values()) {
					if (t.mark==0) {
						//ready to print? are all parents that are in this ontology marked?
						boolean allParentsMarked = true;
						Set<Type> related = t.relatedTo(rel);
						if (related!=null) {
							for (Type p: related) {
								BaseType par = (BaseType)p;
								if (par.getOntology()==this) {
									if (par.mark==0) allParentsMarked = false;
									break;
								}
							}
						}
						if (!allParentsMarked) someleft = true;
						else {
							count += appendMapletDecl(b2, t, rel);
							t.mark++;
						}
					}
				}
			}
		}
		b.append("\n    ; "+(count>0?count:"No")+" relational maplets defined"+(count>0?":":".")).append(b2);
		
		//print all the relation maplets for relations in this ontology for types that are in superontologies
		b2.setLength(0);
		count = 0;
		for (BaseType t: tBox) {
			if (t.getOntology()==this)
				continue;
			for (BaseType.Role role: t.roles.values()) {
				for (Relation rel: relations.map.values()) {
					if (rel==role.rel || ((ConcreteRelation)rel).isBasedOn(role.rel)) {
						count += appendMapletDecl(b2, t, role.rel);
						break;
					}
				}
			}
		}
		b.append("\n    ; "+(count>0?count:"No")+" objects in superontologies have relations in this ontology"+(count>0?":":".")).append(b2);

		b.append("\n  )\n)");

		return b.toString();
	}

	private int appendMapletDecl(StringBuffer b, BaseType t, Relation rel) {
		Set<Type> parents = t.relatedTo(rel);
		int nParents = parents==null?0:parents.size();
		if (parents!=null && nParents>0) {
			b.append("\n    (declMaplet ")
			.append(((ConcreteRelation)rel).getRelativeName(this)).append(' ')
			.append(t.getRelativeName(this)).append(' ');
			if (nParents>1) b.append('(');
			for (Type p: parents) {
				b.append(((BaseType)p).getRelativeName(this)).append(' ');
			}
			b.setLength(b.length()-1);
			if (nParents>1) b.append(')');
			b.append(')');
		}
		return nParents==0?0:1;
	}

	/**
	 * Retrieves the relation object based on the name.
	 * @param name
	 * @return The relation object for <em>name</em>.
	 * @throws IllegalOperationException
	 */
	public Relation getRelation(String name) throws IllegalOperationException {
		Relation ret = relations.get(name);
		if (ret==null && this!=primitiveOntology) {
			ret = primitiveOntology.getRelation(name);
		}
		return ret;
	}
	
	/**
	 * Retrieves the type object based on the name.
	 * @param name
	 * @return The type object for <em>name</em>.
	 * @throws IllegalOperationException
	 */
	public BaseType getType(String name) throws IllegalOperationException {
		BaseType ret = tBox.get(name);
		if (ret==null && this!=primitiveOntology) {
			ret = primitiveOntology.getType(name);
		}
		return ret;
	}
	
	/**
	 * Adds an isa-parent relationship between <em>name</em> and each of the <em>parents</em> to the ontology.
	 * This method will also preform a behind-the-scenes {@link #declType(String)} for <em>name</em>
	 * if it doesn't already exist.
	 * @see casa.ontology.Ontology#addType(java.lang.String, java.lang.String)
	 */
	@Override
	public void addType(String name, String... parents)
			throws ParentNodeNotFoundException, DuplicateNodeException, IllegalOperationException {
		for (String s : parents) {
			addType (name, s);
		}
	}

	/**
	 * Adds an isa-parent relationship between <em>name</em> and <em>parent</em> to the ontology.
	 * This method will also preform a behind-the-scenes {@link #declType(String)} for <em>name</em>
	 * if it doesn't already exist.
	 * @see casa.ontology.Ontology#addType(java.lang.String, java.lang.String)
	 */
	@Override
	public void addType(String name, String parent)
			throws ParentNodeNotFoundException, DuplicateNodeException, IllegalOperationException {
		declMaplet(isa, name, parent);
	}

	/** 
	 * CASAOntolgy does not implement this method, and throws an {@link IncompatableTypeHierarchiesException}.
	 * Instead execute the lisp commands (declOntology ...) or (with-ontology ...).
	 * @see casa.ontology.Ontology#add(java.lang.String)
	 */
	@Override
	public int add(String description) throws DuplicateNodeException,
			IncompatableTypeHierarchiesException, ParentNodeNotFoundException,
			ParseException {
		throw new IncompatableTypeHierarchiesException("CASAOntolgy does not implement this method, instead execute the lisp commands (declOntology ...) or (with-ontology ...)");
	}

	/** 
	 * This method is not fully implemented in this implementation
	 * @see casa.ontology.Ontology#describe(java.lang.String)
	 */
	@Override
	public String describe(String name) throws IllegalOperationException {
		BaseType t = getType(name);
		if (t==null) throw new IllegalOperationException("Unknown type '"+name+"'");
    return describe(t);
	}

	/** 
	 * This method is not fully implemented in this implementation
	 * @see casa.ontology.Ontology#describe(String)
	 * @param type
	 * @return the String describing the <em>type</em>.
	 * @throws IllegalOperationException
	 */
	public String describe(BaseType type) throws IllegalOperationException {
		return describe2(type, new TreeSet<Type>(), new TreeSet<String>());
	}
	
	/** 
	 * This method is not fully implemented in this implementation
	 * @see casa.ontology.Ontology#describe(java.lang.String)
	 */
	private String describe2(Type theType, TreeSet<Type> doneTypes, TreeSet<String> doneRels) throws IllegalOperationException {
		assert theType instanceof BaseType;
		BaseType type = (BaseType)theType;
		StringBuffer buf = new StringBuffer();

		for (String relName: type.getRoles()) {
			Relation rel = getRelation(relName);
			buf.append(describeRel(rel, doneRels));
		}

		for (String relName: type.getRoles()) {
			for (Type t: type.relatedTo(relName)) {
				if (!doneTypes.contains(t) && t.getName()!=TOP) {
				  buf.append(describe2(t,doneTypes, doneRels));
				  doneTypes.add(t);
				}
			}
		}

		buf.append(type.toStringParent()).append('\n');
		String nonIsa = type.toStringNonIsa();
		if (nonIsa.length()>0) buf.append(nonIsa).append('\n');
		
    return buf.toString();
	}
	
	@Override
	public String describeRelation(String relation) throws IllegalOperationException {
		Relation	r = getRelation(relation);
		return describeRel(r, null);
	}
	
	@Override
	public String describeType(String type) throws IllegalOperationException {
		BaseType t = getType(type);
		return t.toString();
	}
	
	@Override
	public String describeIndividual(String type) throws IllegalOperationException {
		BaseType t = getType(type);
		return t.isIndividual()?t.toString():null;
	}
	
	/** 
	 * This method is not fully implemented in this implementation
	 * @see casa.ontology.Ontology#describe(java.lang.String)
	 */
	private String describeRel(Relation relation, TreeSet<String> doneRels) {
		if (doneRels==null) doneRels = new TreeSet<String>();
		StringBuffer b = new StringBuffer();
		if (relation instanceof BasedRelation) {
			String s = describeRel(((BasedRelation)relation).base,doneRels);
			if (s.length()>0) b.append(s);
		}
		String rName = relation.getName();
		if (relation.isVisible() && !primitiveRelation(rName) && !doneRels.contains(rName)) {
			b.append(relation.toString()).append('\n');
			doneRels.add(rName);
		}
		return b.toString();
	}
	
	/**
	 * @param name The name of a relation
	 * @return true iff the named relation is a primitive (system defined, non-overrideable, non-modifiable) relation
	 */
	private boolean primitiveRelation(String name) {
		try {
			return primitiveOntology.getRelation(name)!=null;
		} catch (IllegalOperationException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean isCompatable(Ontology other) {
		return other instanceof CASAOntology;
	}

	@Override
	public void isCompatableThrow(Ontology other)
			throws IncompatableTypeHierarchiesException {
		if (!(other instanceof CASAOntology)) throw new IncompatableTypeHierarchiesException();
	}

	/**
	 * @param name the name of the type
	 * @return true iff this type name is included in the ontology or one of the super ontologies.
	 * @throws IllegalOperationException if the qualified syntax explicitly specifies a dictionary that is not the owner.
	 * @see casa.ontology.Ontology#isObject(java.lang.String)
	 */
	@Override
	public boolean isObject(String name) throws IllegalOperationException {
		return tBox.containsKey(name);
	}
	
	@Override
	public boolean isType(String name) throws IllegalOperationException {
		BaseType t = tBox.get(name);
		return t==null?false:!t.isIndividual();
	}

	@Override
	public boolean isIndividual(String name) throws IllegalOperationException {
		BaseType t = tBox.get(name);
		return t==null?false:t.isIndividual();
	}



	/**
	 * Shortcut for the (isa child parent) lisp operator, or this.relatedTo(isa,child,parent).
	 * @see casa.ontology.Ontology#isa(java.lang.String, java.lang.String)
	 * @see #relatedTo(Relation, Type, Type)
	 */
	@SuppressWarnings("javadoc")
	@Override
	public boolean isa(String child, String parent) throws IllegalOperationException {
		Type dom = getType(child);
		if (dom==null) throw new IllegalOperationException("No type '"+child+"' found in tBox");
		Type ran = getType(parent);
		if (ran==null) throw new IllegalOperationException("No type '"+parent+"' found in tBox");
		
		//TODO: Why is this here? dsb
//		if (child.indexOf('|')!=-1 || parent.indexOf('|')!=-1) {
//			String[] childList = child.split("|"); 
//		}
		return relatedTo(isa, dom, ran);
	}

  /**
   * Determines if <em>domain</em> and <em>range</em> are related through relation <em>relation</em>.
   * This method takes into account the superOntologies and this isa relations (unlike {@link #queryMaplet(Relation, Type, Type)}
   * and {@link #queryMaplet(String, String, String)}).
   * @param relation
   * @param domain
   * @param rang
   * @return true iff <em>domaion</em> and <em>range</em> are related through relation <em>relation</em>
   * @throws IllegalOperationException if any of the parameters can't be found, etc
   * @see #queryMaplet(Relation, Type, Type)
   */
	@SuppressWarnings("javadoc")
	@Override
	public boolean relatedTo(String relation, String domain, String range)
			throws UnsupportedOperationException, IllegalOperationException {
		Relation rel = getRelation(relation);
		if (rel==null) throw new IllegalOperationException("No relation '"+relation+"' found");
		return relatedTo(rel, domain, range);
	}

  /**
   * Determines if <em>domain</em> and <em>range</em> are related through relation <em>relation</em>.
   * This method takes into account the superOntologies and this isa relations (unlike {@link #queryMaplet(Relation, Type, Type)}
   * and {@link #queryMaplet(String, String, String)}).
   * @param relation
   * @param domain
   * @param rang
   * @return true iff <em>domaion</em> and <em>range</em> are related through relation <em>relation</em>
   * @throws IllegalOperationException if any of the parameters can't be found, etc
   * @see #queryRelation(Relation, Type, Type)
   */
	protected boolean relatedTo(Relation relation, String domain, String range)
	throws UnsupportedOperationException, IllegalOperationException {
		Type dom = getType(domain);
		if (dom==null) throw new IllegalOperationException("No type '"+domain+"' found in tBox");
		Type ran = getType(range);
		if (ran==null) throw new IllegalOperationException("No type '"+range+"' found in tBox");
		return relatedTo(relation, dom, ran);
	}

  /**
   * Determines if <em>domain</em> and <em>range</em> are related through relation <em>relation</em>.
   * This method takes into account the superOntologies and this isa relations (unlike {@link #queryMaplet(Relation, Type, Type)}
   * and {@link #queryMaplet(String, String, String)}).
   * @param relation
   * @param domain
   * @param rang
   * @return true iff <em>domaion</em> and <em>range</em> are related through relation <em>relation</em>
   * @throws IllegalOperationException if any of the parameters can't be found, etc
   */
	protected boolean relatedTo(Relation relation, Type domain, Type range) {
		return relation.relatedTo(domain, range);
	}

	@Override
	public Set<String> relatedTo(String relation, String domain)
	throws UnsupportedOperationException, IllegalOperationException {
		Type dom = getType(domain);
		if (dom==null) throw new IllegalOperationException("No type '"+domain+"' found in tBox");
		Relation rel = getRelation(relation);
		if (rel==null) throw new IllegalOperationException("No relation '"+relation+"' found");
		Set<Type> set = relatedTo(rel, dom);
		Set<String> ret = new TreeSet<String>();
		for (Type t: set)
			ret.add(t.getName());
		return ret;
	}

	/**
	 * @param relation
	 * @param domain
	 * @return the set of Types <em>domain</em> is related to via <em>relation</em>.
	 */
	protected Set<Type> relatedTo(Relation relation, Type domain) {
		return relation.relatedTo(domain);
	}

	/**
	 * Not implemented.
	 * @see casa.ontology.Ontology#extendWith(java.lang.String)
	 */
	@Override
	public Status extendWith(String spec) {
		return new Status(-10,"Method unimplemented");
	}
	
	@Override
	public void addIndividual(String name, String parent) throws ParentNodeNotFoundException, DuplicateNodeException, IllegalOperationException {
		throw new IllegalOperationException("CASAOntology.addIndividual(): not implemented.");
//		Type child = getType(name);
//		if (child!=null && !child.isIndividual()) {
//			throw new IllegalOperationException("CASAOntology.addIndividual(): first argument is a non-Individual type.");
//		}
//		addType(name, parent);
//		if (child==null) {
//			child = getType(name);
//			child.
//		}
	}

//  @SuppressWarnings("unused")
//	private static final CasaLispOperator ONTOLOGY =
//  	new CasaLispOperator("ONTOLOGY", "\"!Declare a new Ontology or extends an existing Ontology.\" "+
//  			"NAME \"@java.lang.String\" \"!The name of the  ontology.\" "+
//  			"SUPER-ONTOLOGIES \"!A list of existing ontology names to append to the super-ontologies of this ontology.\" " +
//  			"RELS-AND-TYPES \"!A list of decl* type declarations.\" "+
//  			"&KEY VERBOSE \"!echo the command if verbose/=NIL\""
//  			, TransientAgent.class, "DECLONTOLOGY", "WITH-ONTOLOGY")
//  {
//  	@Override
//  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {
  public static Status ontology_lispImpl(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {
  	//assert agent!=null;
  	String ontName = ((String)params.getJavaObject("NAME"))/*.toUpperCase()*/;

  	CASAOntology ont = null;	
  	//ont = getOntology(ontName); will cause recursion on opening the same file if this is called from a file
  	ont = allOntologies==null?null:allOntologies.get(ontName);
  	if (ont==null) {
  		try {
  			ont = new CASAOntology(ontName);
  		} catch (IllegalOperationException e) {
  			// can't happen if we don't specify super-ontologies
  		}
  		allOntologies.put(ontName, ont);
  	}

  	LispObject obj = params.getLispObject("SUPER-ONTOLOGIES");
  	if (obj == null || obj==org.armedbear.lisp.Lisp.NIL) {} //don't need to do anything if there are no super ontologies 
  	else if (obj instanceof Cons) {
  		while (obj!=null && obj!=org.armedbear.lisp.Lisp.NIL) {
  			String superName = obj.car().writeToString()/*.toUpperCase()*/;
  			CASAOntology superOnt;
  			try {
  				if (superName.length()>0 && superName.charAt(0)=='"') superName = CASAUtil.fromQuotedString(superName);
  				superOnt = getOntology(superName);
  			} catch (Exception e) {
  				return new Status(-2,"Failed (Ontology "+ontName+" ...)",e);
  			}
  			if (superOnt == null) //ran = ont.declType(ranName); //could recover here, but that doesn't seem right
  				return new Status(-4,CASAUtil.printui("Failed (Ontology "+ontName+" ...), super ontoloy "+superName+" doesn't exist",ui, params, agent));
  			if (!ont.superOntologies.contains(superOnt)) {
  				try {
  					ont.addSuperOntologies(superOnt);
  				} catch (IllegalOperationException e) {
  					return new Status(-4,CASAUtil.printui("Failed (Ontology "+ontName+" ...): "+e.getMessage(),ui, params, agent));
  				}
  			}
  			obj = obj.cdr();
  		}
  	} 
  	else 
  		return new Status(-6,"Ontology "+ontName+" expected 2nd parameter as a list of existing super ontologies");

  	obj = params.getLispObject("RELS-AND-TYPES");
  	if (obj == null || obj==org.armedbear.lisp.Lisp.NIL) {} //don't need to do anything if there are no declarations 
  	else if (obj instanceof Cons) {
  		//set up an environment where the decl* operators can get back to this ont through the "ontology" symbol 
  		//				Environment env2 = new Environment(env, Lisp.PACKAGE_CL_USER.addInternalSymbol("ontology"), new JavaObject(ont));
  		Environment env2 = new Environment(env);
  		casa.abcl.Lisp.bind(env2,"ontology",new JavaObject(ont));
  		while (obj!=null && obj!=org.armedbear.lisp.Lisp.NIL) {
  			LispObject decl = obj.car();
  			if (decl instanceof Cons) {
  				//we are just assuming that this will add the declaration to ourself
  				//LispObject.eval(decl, env2, LispThread.currentThread());
  				casa.abcl.Lisp.abclEval(agent, env2, null, decl.writeToString(), ui);
  			}
  			else {
  				return new Status(-8,"Ontology "+ontName+" expected 3rd parameter as a list of relation and type expressions ((declType...), (declRelation...), (declIndividual...))");						
  			}
  			obj = obj.cdr();
  		}
  	} 
  	else 
  		return new Status(-10,"Ontology "+ontName+" expected 3rd parameter as a list of relation and type expressions ((declType...), (declRelation...), (declIndividual...))");


  	return new StatusObject<CASAOntology>(ont);
  }
//  };
  
	public static String[] getResident() {
		if (allOntologies==null) 
			return new String[]{};
		String[] ret = new String[allOntologies.size()];
		int i = 0;
		for (String o: allOntologies.keySet()) {
			ret[i++] = o;
		}
		return ret;
	}
  
	@Override
	public void addIndividual(String name, String... parents) throws ParentNodeNotFoundException, DuplicateNodeException, IllegalOperationException {
		for (String p: parents) {
			addIndividual(name, p);
		}
	}

	@Override
	public boolean instanceOf(String child, String parent) throws IllegalOperationException {
		Type c = getType(child);
		if (!c.isIndividual())
			throw new IllegalOperationException("CASAOntology.instanceOf(): first argument, '"+name+"' is not an individual");
		return isa(child,parent);
	}

	@Override
	public boolean isRelation(String name) throws IllegalOperationException {
		return getRelation(name)!=null;
	}

	@Override
	public Set<String> isa(String child) throws IllegalOperationException {
		return relatedTo("isa", child);
	}

	@Override
	public Set<String> isParent(String child) throws IllegalOperationException {
		return relatedTo("isa-parent", child);
	}

	@Override
	public Set<String> isAncestor(String child) throws IllegalOperationException {
		return relatedTo("isa-ancestor", child);
	}

	@Override
	public Set<String> isChild(String parent) throws IllegalOperationException {
		return relatedTo("isa-child", parent);
	}

	@Override
	public Set<String> isDescendant(String parent) throws IllegalOperationException {
		return relatedTo("isa-descendant", parent);
	}

	@Override
	public String getDefaultFileExtension() {
		// TODO Auto-generated method stub
		return null;
	}


}