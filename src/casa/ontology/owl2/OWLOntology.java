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
package casa.ontology.owl2;


import casa.Status;
import casa.StatusObject;
import casa.TransientAgent;
import casa.abcl.CasaLispOperator;
import casa.abcl.ParamsMap;
import casa.exceptions.DuplicateNodeException;
import casa.exceptions.IllegalOperationException;
import casa.exceptions.IncompatableTypeHierarchiesException;
import casa.exceptions.ParentNodeNotFoundException;
import casa.ontology.Constraint;
import casa.ontology.Ontology;
import casa.ontology.Relation;
import casa.ui.AgentUI;
import casa.util.CASAUtil;
import casa.util.Trace;

import java.text.ParseException;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.armedbear.lisp.Cons;
import org.armedbear.lisp.ControlTransfer;
import org.armedbear.lisp.Environment;
import org.armedbear.lisp.Lisp;
import org.armedbear.lisp.LispError;
import org.armedbear.lisp.LispObject;
import org.armedbear.lisp.SimpleString;
import org.armedbear.lisp.Symbol;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import uk.ac.manchester.cs.owl.owlapi.OWLClassAssertionImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;

/**
 * A regular {link org.semanticweb.owlapi.model.OWLOntology} overlaid with the CASA-standard
 * {@link Ontology} interface.  Note that the interface entails several minor changes:
 * <ul>
 * <li> CASA uses the term "Type" instead of OWL's "Class".
 * <li> CASA Individuals as a specialization of Type, whereas in OWL Class and Individual are distinct;
 * therefore in CASA both isa(Class, Class) and isa(Individual, Class) are valid, wherewas in
 * OWL we would expect isa(Individual, Class) to be not well-formed.  instanceOf(?, Class) has the same
 * semantics in both CASA and OWL (ie: instanceOf(Class,Class) is not well-formed, 
 * but instanceOf(Individual,Class)is.
 * </ul>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class OWLOntology extends OWLOntologyImpl implements Ontology {
	
	private static final long serialVersionUID = -7794743478401898610L;
	
	public static final String DEFAULT_FILE_EXTENSION = ".owl";
	
	private static OWLOntologyManager manager = null;
	private Reasoner reasoner = null;
	
	/**
	 * Normally, one would not call this constructor; instead, you should use {@link #open(String)} or
	 * {@link #open(String, String)} to create an ontology.
	 * @param mgr
	 * @param id
	 */
	public OWLOntology(OWLOntologyManager mgr, OWLOntologyID id) {
		super(manager==null?(mgr==null?getOntologyManager():(manager=mgr)):manager, id);
		assert manager == mgr;    
	}
	
	public Reasoner getReasoner() {
		if (reasoner==null) {
			reasoner = new Reasoner(this);
	    commitOntToLisp();
		}
		return reasoner;
	}
	
	static public OWLOntologyManager getOntologyManager() {
		return manager==null?(manager=CASA_OWLManager.createOWLOntologyManager()):manager;
	}
	
	/**
	 * Defers to the reasoner.
	 * @return true if the ontology is consisitent.
	 */
	public boolean isConsistent() {
		return getReasoner().isConsistent();
	}
	
	/**
	 * Adds a new Class to this ontology with the specified parents.  Defers to {@link #addType(String, String)}.
	 * @param name
	 * @param parent
	 * @throws ParentNodeNotFoundException
	 * @throws DuplicateNodeException
	 * @throws IllegalOperationException
	 */
	@Override
	public void addType(String name, String... parents) throws ParentNodeNotFoundException, DuplicateNodeException, IllegalOperationException {
		if (parents==null || parents.length==0)
			parents = new String[]{"Thing"};
		for (String p: parents) 
			addType(name, p);
	}

	/* (non-Javadoc)
	 * @see casa.ontology.Ontology#add(java.lang.String, java.lang.String)
	 */
	@Override
	public void addType(String name, String parent) throws ParentNodeNotFoundException, DuplicateNodeException, IllegalOperationException {
		OWLClass p = findClassBySimpleName(parent);
		if (p==null)
			throw new ParentNodeNotFoundException(name, parent);
		addType(name, p);
	}
	
	/**
	 * Adds a new Class to this ontology with the specified parent.
	 * @param name
	 * @param parent
	 * @throws ParentNodeNotFoundException
	 * @throws DuplicateNodeException
	 * @throws IllegalOperationException
	 */
	public void addType(String name, OWLClass parent) throws ParentNodeNotFoundException, DuplicateNodeException, IllegalOperationException {
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass cls = findClassInClosureBySimpleName(name);
		if (cls==null) {
			cls = factory.getOWLClass(makeIRIFromSimpleName(name));
		}
		OWLAxiom axiom = factory.getOWLSubClassOfAxiom(cls, parent);
		addAxiom(axiom);
		setSymbol(name);
		flushReasoner();
  }
	
	/**
	 * Adds an axiom to this ontology and flushes the reasoner so it takes account
	 * of the new addition.
	 * @param axiom
	 */
	public void addAxiom(OWLAxiom axiom) {
		AddAxiom addAxiom = new AddAxiom(this, axiom);
		manager.applyChange(addAxiom);
		flushReasoner();
	}
	
	public void flushReasoner() {
	  // if we can't flush to the reasoner, it means the pending axioms are not compatible
		// with incremental a-box updates, so rebuild the reasoner.
		try { 
			getReasoner().flush();
		} catch (Exception e) {
			reasoner.dispose();
			reasoner = new Reasoner(this);
		}
	}

	/* (non-Javadoc)
	 * @see casa.ontology.Ontology#addIndividual(java.lang.String, java.lang.String[])
	 */
	@Override
	public void addIndividual(String name, String... parents) throws ParentNodeNotFoundException, DuplicateNodeException, IllegalOperationException {
		if (parents==null || parents.length==0)
			parents = new String[]{"Thing"};
		for (String p: parents) 
			addIndividual(name, p);
	}

	/* (non-Javadoc)
	 * @see casa.ontology.Ontology#addIndividual(java.lang.String, java.lang.String)
	 */
	@Override
	public void addIndividual(String name, String parent) throws ParentNodeNotFoundException, DuplicateNodeException, IllegalOperationException {
		OWLClass p = findClassBySimpleName(parent);
		if (p==null)
			throw new ParentNodeNotFoundException(name, parent);
		addIndividual(name, p);
	}
	
	/**
	 * Adds a new Individual to this ontology with the specified parent.
	 * @param name
	 * @param parent
	 * @throws ParentNodeNotFoundException
	 * @throws DuplicateNodeException
	 * @throws IllegalOperationException
	 */
	public void addIndividual(String name, OWLClass parent) throws ParentNodeNotFoundException, DuplicateNodeException, IllegalOperationException {
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLNamedIndividual ind = findIndividualInClosureBySimpleName(name);
		if (ind==null) {
			ind = factory.getOWLNamedIndividual(makeIRIFromSimpleName(name));
		}
		OWLAxiom axiom = factory.getOWLClassAssertionAxiom(parent, ind);
		addAxiom(axiom);
		setSymbol(name);
		flushReasoner();
	}

	/**
	 * Removes an Individual from this ontology.
	 * @param name
	 * @throws IllegalOperationException
	 */
	public void removeIndividual(String name) throws IllegalOperationException {
		removeEntity(name, OWLNamedIndividual.class);
	}
	
	/**
	 * Removes a Class fromt his ontology.
	 * @param name
	 * @throws IllegalOperationException
	 */
	public void removeType(String name) throws IllegalOperationException {
		removeEntity(name, OWLClass.class);
	}
		
	/**
	 * Removes an entity of class <em>cls</em> from this ontology.
	 * @param name
	 * @param cls
	 * @throws IllegalOperationException
	 */
	public <T extends OWLEntity> void removeEntity(String name, Class<T> cls) throws IllegalOperationException {
    OWLEntityRemover remover = new OWLEntityRemover(manager, Collections.singleton((org.semanticweb.owlapi.model.OWLOntology)this));
    T ent = findEntity(name, cls);
    if (ent!=null) {
    	ent.accept(remover);
      manager.applyChanges(remover.getChanges());
      flushReasoner();
    }
	}
		
	/**
	 * Builds a new full IRI for this ontology.  This will be of name:<br>
	 * [ontologyName]#<em>name</em>
	 * @param name
	 * @return the new IRI
	 */
	public IRI makeIRIFromSimpleName(String name) {
		return IRI.create(getName()+"#"+name);
	}

	/** 
	 * OWLOntolgy does not implement this method, and throws an {@link IncompatableTypeHierarchiesException}.
	 * Instead execute the lisp commands (declOntology ...) or (with-ontology ...).
	 * @see casa.ontology.Ontology#add(java.lang.String)
	 */
	@Override
	public int add(String description) throws DuplicateNodeException,
			IncompatableTypeHierarchiesException, ParentNodeNotFoundException,
			ParseException {
		throw new IncompatableTypeHierarchiesException("OWLOntolgy does not implement this method");
	}


	@Override
	public Set<String> relatedTo(String relation, String domain) throws UnsupportedOperationException, IllegalOperationException {
		if ("isa".equalsIgnoreCase(relation)) 
			return isa(domain);
		if ("isparent".equalsIgnoreCase(relation))
			return isParent(domain);
 		OWLObjectProperty rel	= findEntity(relation, OWLObjectProperty.class);
 		if (rel==null)
 			throw new IllegalOperationException("Can't find relation "+relation);
		OWLNamedIndividual dom = findIndividualInClosureBySimpleName(domain);//findClassBySimpleName(domain)
		if (dom!=null) {
		  return relatedTo(rel, dom);
		}
		else { // dom==null
			OWLClass domClass = findClassBySimpleName(domain);
			  return relatedTo(rel, domClass);
		}
	}
	
	@Override
	public String describeRelation(String relation) throws IllegalOperationException {
 		OWLObjectProperty rel	= findEntity(relation, OWLObjectProperty.class);
 		if (rel==null)
 			throw new IllegalOperationException("Can't find relation "+relation+" in ontology "+getName()+" or its imports.");
 		return rel.toString();
	}

	@Override
	public String describeType(String type) throws IllegalOperationException {
 		OWLClass cls	= findEntity(type, OWLClass.class);
 		if (cls==null)
 			throw new IllegalOperationException("Can't find type/class "+type+" in ontology "+getName()+" or its imports.");
 		return cls.toString();
	}

	@Override
	public String describeIndividual(String ind) throws IllegalOperationException {
 		OWLNamedIndividual ni	= findEntity(ind, OWLNamedIndividual.class);
 		if (ni==null)
 			throw new IllegalOperationException("Can't find individual "+ind+" in ontology "+getName()+" or its imports.");
 		return ni.toString();
	}

	public Set<String> relatedTo(OWLObjectProperty relation, OWLNamedIndividual domain) throws UnsupportedOperationException, IllegalOperationException {
		Set<String> ret = new TreeSet<String>();
		NodeSet<OWLNamedIndividual> vals = getReasoner().getObjectPropertyValues(domain, relation);
		for (OWLNamedIndividual ind: vals.getFlattened()) {
			ret.add(ind.getIRI().getFragment());
		}
		return ret;
	}
	
	public Set<String> relatedTo(OWLObjectProperty relation, OWLClass domain) throws UnsupportedOperationException, IllegalOperationException {
		Set<String> ret = new TreeSet<String>();
		NodeSet<OWLClass> set = getReasoner().getSuperClasses(domain, false);
		for (Node<OWLClass> node: set) {
			OWLClass c = node.getRepresentativeElement();
			IRI iri = c.getIRI();
			OWLNamedIndividual ind = findIndividualInClosureBySimpleName(iri.toString());
			if (ind!=null) {
				ret = relatedTo(relation, ind);
				if (!ret.isEmpty())
				  return ret;
			}
		}
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see casa.ontology.Ontology#relatedTo(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean relatedTo(String relation, String domain, String range) throws UnsupportedOperationException, IllegalOperationException {
		if ("isa".equalsIgnoreCase(relation)) 
			return isa(domain,range);
 		OWLObjectProperty rel	= findEntity(relation, OWLObjectProperty.class);
 		if (rel==null)
 			throw new IllegalOperationException("Can't find relation "+relation);
		OWLNamedIndividual dom = findIndividualInClosureBySimpleName(domain);//findClassBySimpleName(domain)
		OWLNamedIndividual ran = findIndividualInClosureBySimpleName(range);//findClassBySimpleName(range);
		if (dom!=null) {
			if (ran!=null) {
			  return relatedTo(rel, dom, ran);
			}
			else {
				OWLClass ranClass = findClassBySimpleName(range);
				if (ranClass!=null) {
				  return relatedTo(rel, dom, ranClass);
				}
				else {
		 			throw new IllegalOperationException("Can't find range "+range);
				}
			}
		}
		else { // dom==null
			OWLClass domClass = findClassBySimpleName(domain);
			if (ran!=null) {
			  return relatedTo(rel, domClass, ran);
			}
			else {
				OWLClass ranClass = findClassBySimpleName(range);
				if (ranClass!=null) {
				  return relatedTo(rel, domClass, ranClass);
				}
				else {
		 			throw new IllegalOperationException("Can't find range "+range);
				}
			}
		}
	}

	/**
	 * @param relation
	 * @param domain
	 * @param range
	 * @return true iff <em>domain</em> is related to <em>range</em> through relation <em>relation</em>.
	 * @throws UnsupportedOperationException
	 * @throws IllegalOperationException
	 */
	public boolean relatedTo(OWLObjectProperty relation, OWLNamedIndividual domain, OWLNamedIndividual range) throws UnsupportedOperationException, IllegalOperationException {
		return getReasoner().hasObjectPropertyRelationship(domain, relation, range);
	}

	/**
	 * @param relation
	 * @param domain
	 * @param range
	 * @return true iff <em>domain</em> is related to <em>range</em> through relation <em>relation</em>.
	 * @throws UnsupportedOperationException
	 * @throws IllegalOperationException
	 */
	public boolean relatedTo(OWLObjectProperty relation, OWLClass domain, OWLNamedIndividual range) throws UnsupportedOperationException, IllegalOperationException {
		NodeSet<OWLClass> set = getReasoner().getSuperClasses(domain, false);
		for (Node<OWLClass> node: set) {
			OWLClass c = node.getRepresentativeElement();
			IRI iri = c.getIRI();
			OWLNamedIndividual ind = findIndividualInClosureBySimpleName(iri.toString());
			if (ind!=null && relatedTo(relation, ind, range))
				return true;
		}
		return false;
	}

	/**
	 * @param relation
	 * @param domain
	 * @param range
	 * @return true iff <em>domain</em> is related to <em>range</em> through relation <em>relation</em>.
	 * @throws UnsupportedOperationException
	 * @throws IllegalOperationException
	 */
	public boolean relatedTo(OWLObjectProperty relation, OWLNamedIndividual domain, OWLClass range) throws UnsupportedOperationException, IllegalOperationException {
		NodeSet<OWLClass> set = getReasoner().getSuperClasses(range, false);
		for (Node<OWLClass> node: set) {
			IRI iri = node.getRepresentativeElement().getIRI();
			OWLNamedIndividual ind = findIndividualInClosureBySimpleName(iri.toString());
			if (ind!=null && relatedTo(relation, domain, ind))
				return true;
		}
		return false;
	}

	/**
	 * @param relation
	 * @param domain
	 * @param range
	 * @return true iff <em>domain</em> is related to <em>range</em> through relation <em>relation</em>.
	 * @throws UnsupportedOperationException
	 * @throws IllegalOperationException
	 */
	public boolean relatedTo(OWLObjectProperty relation, OWLClass domain, OWLClass range) throws UnsupportedOperationException, IllegalOperationException {
		NodeSet<OWLClass> set = getReasoner().getSuperClasses(domain, false);
		for (Node<OWLClass> node: set) {
			OWLClass c = node.getRepresentativeElement();
			IRI iri = c.getIRI();
			OWLNamedIndividual ind = findIndividualInClosureBySimpleName(iri.toString());
			if (ind!=null && relatedTo(relation, ind, range))
				return true;
		}
		return false;
	}

	/** 
	 * Setting this constant to true means isa's arguments are (type, type);
	 * setting to false means isa's arguments are ([type|individual], type).
	 */
	static final boolean isaStrict = false;
	
	/* (non-Javadoc)
	 * @see casa.ontology.Ontology#isa(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isa(String child, String parent) throws IllegalOperationException {
		
		//non-strict: if the two are the same string, we'll return true
		if (child.equals(parent))
			return true;
		
		OWLClass p = findClassInClosureBySimpleName(parent);
		if (p==null)
			return false;
		
		OWLClass c = findClassInClosureBySimpleName(child);
		if (c==null) {
			if (isaStrict)
		    return false;
			OWLNamedIndividual ind = findIndividualInClosureBySimpleName(child);
			if (ind==null)
				return false;
			return instanceOf(ind, p);
		}
		return isa(c,p);
	}

	/**
	 * @param child
	 * @return The set of ancestors to Class or Individual <em>child</em>.
	 * @throws IllegalOperationException
	 */
	@Override
	public Set<String> isa(String child) throws IllegalOperationException {
		return isa(child, false);
	}
	
	/**
	 * @param child
	 * @return The set of parents (direct ancestors) to Class <em>child</em>.
	 * @throws IllegalOperationException
	 */
	@Override
	public Set<String> isParent(String child) throws IllegalOperationException {
		return isa(child, true);
	}	
	
	/**
	 * @param child
	 * @return The set of ancestors to Class <em>child</em>.
	 * @throws IllegalOperationException
	 */
	@Override
	public Set<String> isAncestor(String child) throws IllegalOperationException {
		return isa(child, false);
	}	
	
	/**
	 * @param parent
	 * @return The set of children (direct decendants) to Class <em>parent</em>.
	 * @throws IllegalOperationException
	 */
	@Override
	public Set<String> isChild(String parent) throws IllegalOperationException {
		return isSubClass(parent, true);
	}	
	
	/**
	 * @param parent
	 * @return The set of descendants to Class <em>parent</em>.
	 * @throws IllegalOperationException
	 */
	@Override
	public Set<String> isDescendant(String parent) throws IllegalOperationException {
		return isSubClass(parent, false);
	}	
	
	/**
	 * @param child
	 * @param direct If true, return only direct ancestors (parents), otherwise return all ancestors.
	 * @return The set of ancestors to Class or Individual <em>child</em>.
	 * @throws IllegalOperationException
	 */
	private Set<String> isa(String child, boolean direct) throws IllegalOperationException {
		NodeSet<OWLClass> nodeSet;
		OWLClass c = findClassInClosureBySimpleName(child);
		if (c==null) {
			if (isaStrict)
		    return null;
			OWLNamedIndividual ind = findIndividualInClosureBySimpleName(child);
			if (ind==null)
				return null;
			nodeSet = instanceOf(ind);
		}
		else {
		  nodeSet = isa(c, direct);
		}
		TreeSet<String> ret = new TreeSet<String>();
		for (Node<OWLClass> node: nodeSet) {
			ret.add(node.getRepresentativeElement().getIRI().getFragment());
		}
		return ret;
	}
	
	/**
	 * @param parent
	 * @param direct If true, return only direct descendants (children), otherwise return all descendants.
	 * @return The set of descendants to Class or Individual <em>child</em>.
	 * @throws IllegalOperationException
	 */
	private Set<String> isSubClass(String parent, boolean direct) throws IllegalOperationException {
		NodeSet<OWLClass> nodeSet;
		OWLClass c = findClassInClosureBySimpleName(parent);
		if (c==null) {
		  return null;
		}
		else {
		  nodeSet = isSubClass(c, direct);
		}
		TreeSet<String> ret = new TreeSet<String>();
		for (Node<OWLClass> node: nodeSet) {
			ret.add(node.getRepresentativeElement().getIRI().getFragment());
		}
		return ret;
	}
	
	private NodeSet<OWLClass> isa(OWLClass child, boolean direct) throws IllegalOperationException {
		return getReasoner().getSuperClasses(child, direct);
	}	

	private NodeSet<OWLClass> isSubClass(OWLClass parent, boolean direct) throws IllegalOperationException {
		return getReasoner().getSubClasses(parent, direct);
	}	

	/**
	 * @param child
	 * @return The set of ancestors to Class <em>child</em>.
	 * @throws IllegalOperationException
	 */
	public NodeSet<OWLClass> isa(OWLClass child) throws IllegalOperationException {
		return isa(child, false);
	}	
	
	/**
	 * @param child
	 * @return The set of parents (direct ancestors) to Class <em>child</em>.
	 * @throws IllegalOperationException
	 */
	public NodeSet<OWLClass> isParent(OWLClass child) throws IllegalOperationException {
		return isa(child, true);
	}	
	
	/**
	 * @param child
	 * @return The set of parents (direct ancestors) to Class <em>child</em>.
	 * @throws IllegalOperationException
	 */
	public NodeSet<OWLClass> isAncestor(OWLClass child) throws IllegalOperationException {
		return isa(child, false);
	}	
	
	/**
	 * @param parent
	 * @return The set of parents (direct ancestors) to Class <em>child</em>.
	 * @throws IllegalOperationException
	 */
	public NodeSet<OWLClass> isChild(OWLClass parent) throws IllegalOperationException {
		return isSubClass(parent, true);
	}	
	
	/**
	 * @param parent
	 * @return The set of parents (direct ancestors) to Class <em>child</em>.
	 * @throws IllegalOperationException
	 */
	public NodeSet<OWLClass> isDescendant(OWLClass parent) throws IllegalOperationException {
		return isSubClass(parent, false);
	}	
	


  /**
   * @param child
   * @param parent
   * @return True iff <em>child</em> is a subclass of <em>parent</em>.
   * @throws IllegalOperationException
   */
  public boolean isa(OWLClass child, OWLClass parent) throws IllegalOperationException {
		NodeSet<OWLClass> set = getReasoner().getSuperClasses(child, false);
		return set.containsEntity(parent);
	}

	/* (non-Javadoc)
	 * @see casa.ontology.Ontology#instanceOf(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean instanceOf(String child, String parent) throws IllegalOperationException {
		OWLClass p = findClassInClosureBySimpleName(parent);
		if (p==null)
			return false;
		OWLNamedIndividual c = findIndividualInClosureBySimpleName(child);
		if (c==null)
		  return false;
		return instanceOf(c,p);
	}

	/**
	 * @param child
	 * @param parent
	 * @return True iff <em>child</em> is an instance of <em>parent</em>.
	 * @throws IllegalOperationException
	 */
	public boolean instanceOf(OWLNamedIndividual child, OWLClass parent) throws IllegalOperationException {
		NodeSet<OWLClass> set = getReasoner().getTypes(child, false);
		return set.containsEntity(parent);
	}

	/**
	 * @param child
	 * @return the set of all ancestors of <em>child</em>.
	 * @throws IllegalOperationException
	 */
	public Set<String> instanceOf(String child) throws IllegalOperationException {
		OWLNamedIndividual c = findIndividualInClosureBySimpleName(child);
		if (c==null)
		  return null;
		NodeSet<OWLClass> nodeSet = instanceOf(c);
		TreeSet<String> ret = new TreeSet<String>();
		for (Node<OWLClass> node: nodeSet) {
			ret.add(node.getRepresentativeElement().toString());
		}
		return ret;
	}
	
	/**
	 * @param child
	 * @return the set of all ancestors of <em>child</em>.
	 * @throws IllegalOperationException
	 */
	public NodeSet<OWLClass> instanceOf(OWLNamedIndividual child) throws IllegalOperationException {
		return getReasoner().getTypes(child, false);
	}	

	/* (non-Javadoc)
	 * @see casa.ontology.Ontology#isObject(java.lang.String)
	 */
	@Override
	public boolean isObject(String name) throws IllegalOperationException {
		return findEntity(name) != null;
	}

	/* (non-Javadoc)
	 * @see casa.ontology.Ontology#isType(java.lang.String)
	 */
	@Override
	public boolean isType(String name) throws IllegalOperationException {
		OWLEntity e = findEntity(name);
		return e!=null && e instanceof OWLClass;
	}

	/* (non-Javadoc)
	 * @see casa.ontology.Ontology#isIndividual(java.lang.String)
	 */
	@Override
	public boolean isIndividual(String name) throws IllegalOperationException {
		OWLNamedObject e = findEntity(name, OWLNamedIndividual.class);
		return e!=null && e instanceof OWLIndividual;
	}

	@Override
	public boolean isRelation(String name) throws IllegalOperationException {
		if (findEntity(name, OWLDataProperty.class)!=null)
			return true;
		if (findEntity(name, OWLObjectProperty.class)!=null)
			return true;
		return false;
	}
	
	public String describe(String name, OWLOntologyFormat outputFormat) throws IllegalOperationException {
		OWLOntologyFormat originalFormat = manager.getOntologyFormat(this);
		if (outputFormat==null) outputFormat = originalFormat;

		OWLOntology ont = findOntologyOfSimpleName(name);
		if (ont==null)
			throw new IllegalOperationException("Can't find class '"+name+"' in ontology "+getName()+" or it's import closure.");

		StringDocumentTarget documentTarget = new StringDocumentTarget();

		//OWLOntologyFormat outputFormat = new ManchesterOWLSyntaxOntologyFormat();
		if(originalFormat.isPrefixOWLOntologyFormat()) {
			((PrefixOWLOntologyFormat)outputFormat).copyPrefixesFrom(originalFormat.asPrefixOWLOntologyFormat());
		}
		try {
			manager.saveOntology(ont, outputFormat, documentTarget);
		} catch (OWLOntologyStorageException e) {
			throw new IllegalOperationException(e);
		}
		return documentTarget.toString();
	}
	
	/* (non-Javadoc)
	 * @see casa.ontology.Ontology#describe(java.lang.String)
	 */
	@Override
	public String describe(String name) throws IllegalOperationException {
		return describe(name, null);
	}
	
	public String getClassHierarchyString(OWLClassExpression cls) {
		return getClassHierarchyString(cls, manager.getImportsClosure(this), " ", new Vector<String>());
	}
	
	/**
	 * @param name
	 * @return the Class corresponding to the simple name; or null if the name isn't found.
	 * @throws IllegalOperationException
	 */
	public OWLClass findClassBySimpleName(String name) throws IllegalOperationException {
		OWLEntity entity = findEntity(name, OWLClass.class);
		if (entity instanceof OWLClass)
			return (OWLClass)entity;
		return null;
	}

	/**
	 * @param name
	 * @return the Ontology in which the simple name is found; or null if the name isn't found.
	 * @throws IllegalOperationException
	 */
	public OWLOntology findOntologyOfSimpleName(String name) throws IllegalOperationException {
		IRI iri = findIRI(name);
		if (iri==null)
			throw new IllegalOperationException("Can't find IRI for name '"+name+"'.");
		String start = iri.getStart();
		if (start.endsWith("#")) {
			start = start.substring(0, start.length()-1);
		}
		iri = IRI.create(start);
		OWLOntology ret = (OWLOntology)OWLOntology.manager.getOntology(iri);
		return ret;
	}
	
	/**
	 * @param name
	 * @return the IRI (including the ontology spec) os the simple name, or null if the name isn't found.
	 * @throws IllegalOperationException
	 */
	public IRI findIRI(String name) throws IllegalOperationException {
		OWLEntity ent = findEntity(name);
		if (ent==null)
			return null;
		return ent.getIRI();
	}
	
	/**
	 * @param name
	 * @return the entity corresponding to the simple name.
	 * @throws IllegalOperationException
	 */
	public OWLEntity findEntity(String name) throws IllegalOperationException {
		return findEntity(name, OWLEntity.class);
	}

	/**
	 * @param name
	 * @param cls
	 * @return The entity corresponding to the simple name of type <em>cls</em>; or null if
	 * there is not such name or the name is not of the specified type.
	 * @throws IllegalOperationException
	 */
	@SuppressWarnings("unchecked")
	public <T extends OWLNamedObject> T findEntity(String name, Class<T> cls) throws IllegalOperationException {
		name = name.trim();
		Set<OWLEntity> entitySet = null;
		if (name.contains("://")) { // we have a complete IRI
			entitySet = getEntitiesInSignature(IRI.create(name),true);
		}
		else {
			if (name.startsWith("#")) {
				name = name.substring(1);
			}
			if (name.equals("Thing") && OWLClass.class.isAssignableFrom(cls)) {
				OWLDataFactory factory = manager.getOWLDataFactory();
				return (T)factory.getOWLThing();
			}
			else if (name.equals("Nothing") && OWLClass.class.isAssignableFrom(cls)) {
				OWLDataFactory factory = manager.getOWLDataFactory();
				return (T)factory.getOWLNothing();
			}
			else {
				Set<org.semanticweb.owlapi.model.OWLOntology> set = manager.getImportsClosure(this);
				for (org.semanticweb.owlapi.model.OWLOntology o: set) {
					OWLOntologyID id = o.getOntologyID();
					assert id!=null;
					IRI ontIRI = id.getOntologyIRI();
					IRI iri = IRI.create((ontIRI==null?"":ontIRI.toString()+"#")+name); //in the case of an anonymous ontology just use the name
					Set<OWLEntity> tempSet = o.getEntitiesInSignature(iri);
					if (tempSet!=null && tempSet.size()>0) {
						if (entitySet == null)
							entitySet = tempSet;
						else
							entitySet.addAll(tempSet);
					}
				}
			}
		}
		
		TreeSet<T> validReturns = new TreeSet<T>();
		if (entitySet!=null) {
			for (OWLEntity i: entitySet) {
				if (cls==null || cls.isAssignableFrom(i.getClass())) {
					validReturns.add((T)i);
				}
			}
		}
		
		switch (validReturns.size()) {
		case 0:
			return null;
		case 1:
			return validReturns.first();
		default:
			String list = "";
      String first = null;
      boolean error = false;
			for (T i: validReturns) {
				String iri = i.getIRI().toString();
				if (first==null) first = iri;
				if (!first.equals(iri)) error = true;
				list += "\n  " + iri;
			}
			if (error)
			  throw new IllegalOperationException("Name "+name+" refers to "+entitySet.size()+" entities:"+list);
			else
				return validReturns.first();
		}
	}

	/**
	 * @param name
	 * @return the Class corresponding to the simple name, or null if the name (as a Class) is not found.
	 * @throws IllegalOperationException  
	 */
	public OWLClass findClassInClosureBySimpleName(String name) throws IllegalOperationException {
		return findEntity(name, OWLClass.class);
	}
	
	/**
	 * @param name
	 * @return the Individual corresponding to the simple name, or null if the name (as an Individual) is not found.
	 * @throws IllegalOperationException  
	 */
	public OWLNamedIndividual findIndividualInClosureBySimpleName(String name) throws IllegalOperationException {
		return findEntity(name, OWLNamedIndividual.class);
	}
	
	static private String getClassHierarchyString(OWLClassExpression cls, Set<org.semanticweb.owlapi.model.OWLOntology> onts, String prefix, Vector<String> map) {
		assert map!=null;
		StringBuilder ret = new StringBuilder();
		if (cls instanceof OWLClass) {
			Set<OWLClassExpression> subs = ((OWLClass)cls).getSubClasses(onts);
			String frag = cls.asOWLClass().getIRI().getFragment();
			ret.append(String.format("%5d", map.size()+1)).append(prefix).append(frag);
			if (map.indexOf(frag)<0) {
				ret.append('\n');
				map.add(frag);
				for (OWLClassExpression sub: subs) {
					ret.append(getClassHierarchyString(sub, onts, prefix+"  ", map));
				}
			}
			else {
				map.add(null);
				map.add(null);
				ret.append("\n       ").append(prefix).append("... [see above entry at line ").append(map.indexOf(frag)+1).append("]\n");
			}
		}
		return ret.toString();
	}
	
	/* (non-Javadoc)
	 * @see casa.ontology.Ontology#isCompatable(casa.ontology.Ontology)
	 */
	@Override
	public boolean isCompatable(Ontology other) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see casa.ontology.Ontology#isCompatableThrow(casa.ontology.Ontology)
	 */
	@Override
	public void isCompatableThrow(Ontology other)
			throws IncompatableTypeHierarchiesException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see casa.ontology.Ontology#extendWith(java.lang.String)
	 */
	@Override
	public Status extendWith(String spec) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see casa.ontology.Ontology#getName()
	 */
	@Override
	public String getName() {
		String ret = getOntologyID().getOntologyIRI().toString();
		return ret;
	}
	
	public void addRelation(String name, String dom, String ran) throws IllegalOperationException {
		OWLObjectProperty op = findEntity(name, OWLObjectProperty.class);
		if (op!=null) {
			addRelation(op, dom, ran);
			return;
		}
		OWLDataProperty dp = findEntity(name, OWLDataProperty.class);
		if (dp!=null) {
			addRelation(dp, dom, ran);
			return;
		}
		//There is not such Property, so make one, depending on the type of ran
		OWLNamedIndividual ind = findIndividualInClosureBySimpleName(ran);
		if (ind!=null) {
			OWLDataFactory factory = manager.getOWLDataFactory();
			op = factory.getOWLObjectProperty(IRI.create(getName()+"#"+name));
			addRelation(op, dom, ran);
			return;
		}
		OWLDatatype dt = findEntity(name, OWLDatatype.class);
		if (dt!=null) {
			OWLDataFactory factory = manager.getOWLDataFactory();
			dp = factory.getOWLDataProperty(IRI.create(getName()+"#"+name));
			addRelation(dp, dom, ran);
			return;
		}
		
		throw new IllegalOperationException("OWLOntology.addRelation(): first parameter '"+name+"' is not an OWLObjectProperty or an OWLDataProperty.");
	}
	
	public void addRelation(OWLObjectProperty prop, String dom, String ran) throws IllegalOperationException {
		OWLNamedIndividual ind = findIndividualInClosureBySimpleName(dom);
		if (ind!=null) {
			addRelation(prop, ind, ran);
			return;
		}
		OWLClass cls = findClassInClosureBySimpleName(dom);
		if (cls!=null) {
			addRelation(prop, cls, ran);
			return;
		}
		throw new IllegalOperationException("OWLOntology.addRelation(): second parameter '"+dom+"' is not an OWLClass or an OWLNamedIndividual.");
	}

	private void addRelation(OWLObjectProperty prop, OWLClass dom, String ran) throws IllegalOperationException {
		OWLNamedIndividual ind = findIndividualInClosureBySimpleName(dom.getIRI().getFragment());
		if (ind==null) {
			// need to add a relation to a CLASS, so make an individual with the same name as a the class, and relate to THAT.
			addIndividual(dom.getIRI().getFragment(), dom);
			ind = findIndividualInClosureBySimpleName(dom.getIRI().getFragment());
		}
		assert ind!=null;
		addRelation(prop, ind, ran);
	}

	private void addRelation(OWLObjectProperty prop, OWLNamedIndividual dom, String ran) throws ParentNodeNotFoundException, DuplicateNodeException, IllegalOperationException {
		OWLNamedIndividual ind = findIndividualInClosureBySimpleName(ran);
		if (ind!=null) {
			addRelation(prop, dom, ind);
			return;
		}
		OWLClass cls = findClassInClosureBySimpleName(ran);
		if (cls!=null) {
			// need to add a relation to a CLASS, so make an individual with the same name as a the class, and relate to THAT.
			addIndividual(cls.getIRI().getFragment(), cls);
			OWLNamedIndividual ind2 = findIndividualInClosureBySimpleName(cls.getIRI().getFragment());
			addRelation(prop, dom, ind2);
			return;
		}
		throw new IllegalOperationException("OWLOntology.addRelation(): second parameter '"+dom+"' is not an OWLClass or an OWLNamedIndividual.");
	}

	private void addRelation(OWLObjectProperty prop, OWLNamedIndividual dom, OWLNamedIndividual ran) throws IllegalOperationException {
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		OWLAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(prop, dom, ran);
		addAxiom(axiom);
		setRelation(prop.getIRI().getFragment());
		flushReasoner();
	}

	public void addRelation(OWLDataProperty prop, String dom, String ran) throws IllegalOperationException {
		throw new IllegalOperationException("OWLOntology.addRelation(OWLDataPropertiy,String,String): not implemented.");
	}
	
	/**
	 * Find (get) or read (load) in a CASAOntology in the following manner:
	 * <ol>
	 * <li> if <em>name</em> doesn't end in ".owl", then append ".owl" to the name.
	 * <li> GET?: Try {@link OWLOntologyManager#getOntology(IRI)}.
	 * <li> LOAD (as file spec)? If <em>name</em> can be interpreted as an existing file, create an IRI from the file name, and try {@link OWLOntologyManager#loadOntology(IRI)}.
	 * <li> LOAD (standard)? Try {@link OWLOntologyManager#loadOntology(IRI)}.
	 * <ol>
	 * @param theName the name of the ontology as either a raw name or as a existing file path.
	 * @return the found ontology
	 */
	public static /*OWL*/Ontology getOntology(String name) throws IllegalArgumentException {
		if (manager==null)
			getOntologyManager();
		assert manager!=null;
		assert manager instanceof CASA_OWLOntologyManagerImpl;
		return ((CASA_OWLOntologyManagerImpl)manager).getOntology(name);
//		org.semanticweb.owlapi.model.OWLOntology ont = null;
//		String theName = name;
//		Throwable e1 = null, e2 = null, e3=null;
//		if (!theName.endsWith(".owl"))
//			theName = theName+".owl";
//		
//		//try GETTING (from memory) the ontology based on the raw name
//		IRI iri1 = IRI.create(theName);
//		try {
//			ont = manager.getOntology(iri1);
//			if (ont!=null) {
//				if (ont instanceof OWLOntology)
//					return (Ontology)ont;
//			}
//		} catch (Throwable e) {
//			e1 = e;
//		}
//
//		//try LOADING the ontology as though it were a file spec.
//		File file = new File(theName);
//		boolean isFile = file.exists() && file.isFile();
//		IRI iri2 = null;
//		if (isFile) {
//			try {
//				iri2 = IRI.create(file.toURI());
//				ont = manager.loadOntology(iri2);
//				if (ont!=null) {
//					if (ont instanceof OWLOntology)
//						return (Ontology)ont;
//				}
//			} catch (Throwable e) {
//				e2 = e;
//			}
//		}
//
//		//try LOADING the ontology in the normal way.  
//		IRI iri3 = IRI.create(theName); 
//		try {
//			ont = manager.loadOntology(iri3);
//			if (ont!=null) {
//				if (ont instanceof OWLOntology)
//					return (Ontology)ont;
//			}
//		} catch (OWLOntologyCreationException e) {
//			e3 = e;
//		} finally {
//			CASAUtil.log("error", "OWLOntology.getOntology("+name+"): failed in manager.getOntology("+iri1+"), "+(e1==null?"":("(Exception: "+e1.toString()+") "))+"continuing...");
//			if (isFile)
//				CASAUtil.log("error", "OWLOntology.getOntology("+name+"): failed in manager.loadOntology("+iri2+"), "+(e2==null?"":("(Exception: "+e2.toString()+") "))+"continuing...");
//			else
//				CASAUtil.log("error", "OWLOntology.getOntology("+name+"): Can't interpret "+theName+" as an existing file path, attempted "+file.getAbsolutePath()+", continuing...");
//			CASAUtil.log("error", "OWLOntology.getOntology("+name+"): failed in manager.loadOntology("+iri3+"), "+(e3==null?"":("(Exception: "+e3.toString()+") "))+", giving up.");
//		}
//
//		return null;
	}
	
	@Override
	public void addSuperOntologies(casa.ontology.Ontology... superOntologies) throws IllegalOperationException {
		for (Ontology o: superOntologies) {
			if (!(o instanceof OWLOntology))
				throw new IllegalOperationException("Can't add ontology "+o.getName()+" of type "+o.getClass().getCanonicalName()+"; must be a subtype of OWLOntology");
			OWLOntology oo = (OWLOntology)o;
			IRI iri = oo.getOntologyID().getOntologyIRI();
			assert iri!=null;
			OWLImportsDeclaration decl = new OWLImportsDeclarationImpl(iri);
			AddImport addImport = new AddImport(this,decl);
			manager.applyChange(addImport);
		}
		flushReasoner();
	}
	
	@Override
	public void addSuperOntologies(String... superOntologyNames) throws IllegalOperationException {
		for (String o: superOntologyNames) {
			Ontology ont = getOntology(o);
			addSuperOntologies(ont);
		}
	}

	
	public static Ontology makeOntology(String name, Ontology... superOntologies) throws IllegalOperationException {
		if (manager==null)
			getOntologyManager();
		OWLOntology ont = null;
		try {
			//try getting the ontology based on the raw name
			try {
				ont = (OWLOntology)manager.createOntology(IRI.create(name));
			} catch (Throwable e1) {
			}
			if (ont!=null) {
				if (ont instanceof OWLOntology)
					if (superOntologies!=null) {
						ont.addSuperOntologies(superOntologies);
					}
				  return (Ontology)ont;
			}
			
		} catch (Throwable e) {
			CASAUtil.log("error", "OWLOntology.makeOntology(\""+name+"\")", e, true);
			return null;
		}
		return null;
	}


  /**
   * Set the name as a string symbol in the Lisp environment.
   * @param name the symbol to be defined.
   * @throws IllegalOperationException if the name was illegal.
   */
  private void setSymbol(String name) throws IllegalOperationException { 
  	String capsName = name.toUpperCase();
  	Symbol sym = org.armedbear.lisp.Lisp.PACKAGE_CL_USER.internAndExport(capsName);
  	sym.setSymbolValue(new SimpleString(name));
  	sym.setDocumentation(Symbol.VARIABLE, new SimpleString("A CASA type or individual symbol"));
//  	CASAUtil.log("OntLispSymbols", "Instantiated Lisp Symbol "+sym.toString()+" as \""+sym.getSymbolValue()+"\"");
  	if (!name.equals(sym.getSymbolValue().toString()))
    	Trace.log("error", "Instantiated Lisp Symbol "+sym.toString()+" as \""+name+"\", but the Lisp symbol value does not agree (\""+sym.getSymbolValue()+"\")");
  }
  
  private void setRelation(final String name) throws IllegalOperationException {
  	final Ontology THIS = this;
    new CasaLispOperator(name,"\"!Test weather items are related in the "+name+" relation\" "
    		+"DOMAIN \"@java.lang.String\" "
    		+"&OPTIONAL RANGE \"@java.lang.String\" "
  			+"&KEY ONTOLOGY \"@java.lang.String\" \"!The ontology use (default is the agent current ontology)\""
    		,TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
    {
    	private String relName = name;
      @Override
			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {
      	try {
        	Ontology ont = null;
        	if (agent!=null) {
        		Ontology o = agent.getOntology();
        		if (o instanceof OWLOntology) {
        			ont = (OWLOntology)o;
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
  	Trace.log("OntLispSymbols", "Intantiated Lisp function Symbol "+name);
  }

  public void commitOntToLisp() {
  	for (OWLClass cls: getClassesInSignature(true)) {
  		try {
				setSymbol(cls.getIRI().getFragment());
			} catch (IllegalOperationException e) {
				Trace.log("error", "Cannot instantiate Lisp string Symbol "+cls.getIRI().getFragment());
			}
  	}
  	for (OWLNamedIndividual ind: this.getIndividualsInSignature(true)) {
  		try {
				setSymbol(ind.getIRI().getFragment());
			} catch (IllegalOperationException e) {
				Trace.log("error", "Cannot instantiate Lisp string Symbol "+ind.getIRI().getFragment());
			}
  	}
  	for (OWLObjectProperty cls: getObjectPropertiesInSignature(true)) {
  		try {
				setRelation(cls.getIRI().getFragment());
			} catch (IllegalOperationException e) {
				Trace.log("error", "Cannot instantiate Lisp function Symbol "+cls.getIRI().getFragment());
			}
  	}
		try {
			setRelation("isa");
		} catch (IllegalOperationException e) {
			Trace.log("error", "Cannot instantiate Lisp function Symbol isa");
		}
		try {
			setRelation("isparent");
		} catch (IllegalOperationException e) {
			Trace.log("error", "Cannot instantiate Lisp function Symbol isparent");
		}
  }
  
  @Override
  public String toString() {
		return toString(null);
  }
  
	public String toString(OWLOntologyFormat outputFormat) { // throws IllegalOperationException {
		return toString(this, outputFormat);
	}

	public static String toStringPlusImports(org.semanticweb.owlapi.model.OWLOntology ont, OWLOntologyFormat outputFormat) { // throws IllegalOperationException {
		StringBuilder b = new StringBuilder(); //(toString(this, outputFormat));
		Set<org.semanticweb.owlapi.model.OWLOntology> imports = ont.getImportsClosure();
		for (org.semanticweb.owlapi.model.OWLOntology imp: imports) {
			b.append('\n').append(toString(imp,outputFormat));
		}
		return b.toString();
	}
	
	@Override
	public void declMaplet(String relationName, String domainName, String rangeName)
			throws IllegalOperationException {
		OWLAxiom axiom = null;
		if ("isa".equalsIgnoreCase(relationName)) {
			OWLClass ran = this.findEntity(rangeName, OWLClass.class);
			if (ran!=null) {
				OWLNamedObject obj = this.findEntity(domainName);
				if (obj instanceof OWLClass) {
					axiom = new OWLSubClassOfAxiomImpl((OWLClass)obj, ran, new TreeSet<OWLAnnotation>());
				}
				else if (obj instanceof OWLNamedIndividual) {
					axiom = new OWLClassAssertionImpl((OWLNamedIndividual)obj, ran, new TreeSet<OWLAnnotation>());
				}
				else
					throw new IllegalOperationException("OWLOntology.declMaplet(): domain "+domainName+" must be either an Individual or a Class.");
			}
			else
				throw new IllegalOperationException("OWLOntology.declMaplet(): range "+rangeName+" must be either a Class.");

		}
		else {
			OWLObjectProperty prop = this.findEntity(relationName, OWLObjectProperty.class);
			if (prop==null) {
				relationName = relationName.toLowerCase();
				prop = this.findEntity(relationName, OWLObjectProperty.class);
			}
			if (prop==null) {
				relationName = relationName.toUpperCase();
				prop = this.findEntity(relationName, OWLObjectProperty.class);
			}
			OWLNamedIndividual dom = this.findEntity(domainName,   OWLNamedIndividual.class);
			OWLNamedIndividual ran = this.findEntity(rangeName,    OWLNamedIndividual.class);
			if (prop!=null && dom!=null && ran!=null) {
				axiom = new OWLObjectPropertyAssertionAxiomImpl(dom, prop, ran, new TreeSet<OWLAnnotation>());
			}
			else if (dom==null) 
				throw new IllegalOperationException("OWLOntology.declMaplet(): domain "+domainName+" not found.");
			else if (ran==null) 
				throw new IllegalOperationException("OWLOntology.declMaplet(): range "+rangeName+" not found.");
			else if (prop==null) 
				throw new IllegalOperationException("OWLOntology.declMaplet(): property "+relationName+" not found.");
		}
		if (axiom != null) {
			addAxiom(axiom);
			if (!isConsistent()) {
				manager.removeAxiom(this, axiom);
				throw new IllegalOperationException("OWLOntology.declMaplet(): adding axiom "+axiom+" would cause the ontology to become inconsistent.");
			}
		}
	}

	@Override
	public void declRelation(final String name, String basedOn, Set<Relation.Property> properties,
			Constraint domConstraint, Constraint ranConstraint, Object... otherParams) throws IllegalOperationException {
	}

	/** 
	 * @param ont The ontology to return as a string.
	 * @param outputFormat The format in which to return the string.  May be null, in which case it will be the manager's format, or (if the manager doesn't have a format) Manchester syntax.
	 * @return The <em>ont</em> ontology (not including its imports) as a String in <em>outputFormat</em> format.
	 */
	public static String toString(org.semanticweb.owlapi.model.OWLOntology ont, OWLOntologyFormat outputFormat) { // throws IllegalOperationException {
		if (outputFormat==null) 
			outputFormat = manager.getOntologyFormat(ont);
		if (outputFormat==null) {
			outputFormat = new ManchesterOWLSyntaxOntologyFormat();
			manager.setOntologyFormat(ont, outputFormat);
		}

		StringDocumentTarget documentTarget = new StringDocumentTarget();

		if(outputFormat.isPrefixOWLOntologyFormat()) {
			((PrefixOWLOntologyFormat)outputFormat).copyPrefixesFrom(outputFormat.asPrefixOWLOntologyFormat());
		}
		try {
			manager.saveOntology(ont, outputFormat, documentTarget);
		} catch (OWLOntologyStorageException e) {
			return e.toString();
		}
		return documentTarget.toString();
	}
	
	public static String[] getResident() {
		Set<org.semanticweb.owlapi.model.OWLOntology> onts = manager.getOntologies();
		if (onts!=null) {
			String[] ret = new String[onts.size()];
			int i=0;
			for (org.semanticweb.owlapi.model.OWLOntology ont: onts) {
				ret[i++] = ont.getOntologyID().toString();
			}
			return ret;
		}
		else
			return new String[]{};
	}

	@Override
	public String getDefaultFileExtension() {
		return DEFAULT_FILE_EXTENSION;
	}

}
