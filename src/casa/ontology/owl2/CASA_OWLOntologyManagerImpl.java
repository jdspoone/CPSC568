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

import casa.TransientAgent;
import casa.ontology.Ontology;
import casa.util.Trace;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class CASA_OWLOntologyManagerImpl extends OWLOntologyManagerImpl {

	private static final long serialVersionUID = 1L;

	/**
	 * @param dataFactory
	 */
	public CASA_OWLOntologyManagerImpl(OWLDataFactory dataFactory) {
		super(dataFactory);
		// TODO Auto-generated constructor stub
	}
	
  @Override
	protected OWLOntology loadOntology(IRI ontologyIRI, boolean allowExists, OWLOntologyLoaderConfiguration configuration)
      throws OWLOntologyCreationException {
  	OWLOntology ont = loadOntologyHelper(ontologyIRI, allowExists, configuration);
  	if (ont!=null) {
  		if (ont instanceof casa.ontology.owl2.OWLOntology) {
  		//force the reasoner to instantiate and to commit identifiers to Lisp
  		//((casa.ontology.owl2.OWLOntology)ont).getReasoner(); 
  		((casa.ontology.owl2.OWLOntology)ont).commitOntToLisp();
  		}
			addComment(ont, ontologyIRI, "Loaded from IRI: "+ont.getOWLOntologyManager().getOntologyDocumentIRI(ont));
  	}
  	return ont;
  }
  
  private String DEFAULT_ONT_PATH = "http://casa.cpsc.ucalgary.ca/ontologies/";
  
	private OWLOntology loadOntologyHelper(IRI ontologyIRI, boolean allowExists, OWLOntologyLoaderConfiguration configuration)
      throws OWLOntologyCreationException {
  	
  	if (shouldUseDefault(ontologyIRI)) {
			try {
				return super.loadOntology(ontologyIRI, allowExists, configuration); //super.loadOntology(iri, allowExists, configuration);
			} catch (Exception e) {
				return super.getOntology(ontologyIRI);
			}
  	}

  	String name = getOntologyNameFromIRI(ontologyIRI);
  	if (!name.endsWith(".owl"))
  		name = name+".owl";
  	
  	String path = null;
  	if (ontologyIRI.isAbsolute() && "file".equalsIgnoreCase(ontologyIRI.getScheme())) {
  		path = ontologyIRI.getStart();
  	}

  	if (path==null) {
  		//we want to search for the ontology file using the agent's default places to look.
  		try {
  			path = TransientAgent.findFileResourcePath(name);
  		} catch (Throwable e) {
  			Trace.log("error", "CASA_OWLOntologyManagerImpl(\""+path+"\")", e);
  		}
  	}
		
  	// if we can't find the ontology locally... 
		if (path==null) {
			// if we are using an abstract IRI, we could get the super to look in the CASA home page for the definition
			if (!ontologyIRI.isAbsolute()) {
				path = DEFAULT_ONT_PATH+name;
				IRI iri = IRI.create(path);
				OWLOntology ont =  super.loadOntology(iri, allowExists, configuration);
				if (ont==null)
					Trace.log("ontology", "CASA_OWLOntologyManagerImpl(\""+path+"\"): failed to load original (web) ontology. Returning null.");
				return ont;
			}
			// otherwise we were using a absolute URI, so go with the default behaviour 
			else {
				OWLOntology ont =  super.loadOntology(ontologyIRI, allowExists, configuration); //super.loadOntology(iri, allowExists, configuration);
				if (ont==null)
					Trace.log("ontology", "CASA_OWLOntologyManagerImpl(\""+ontologyIRI+"\"): failed to local ontology. Returning null.");
				return ont;
			}
		}

		try {
			if (path.indexOf('\\')>=0) { // for MSWindows pathnames
				path = path.replace('\\', '/'); 
			}
			//assume that if the path has a ":" in it (it has a protocol), it's already a proper IRI spec (but account for Window's "C:" stuff),
			//otherwise we should assume it's a file path and convert it to a proper URI before making the IRI.
			IRI iri = (path.length()<=2 || path.substring(2).contains(":")) ? IRI.create(path) : IRI.create((new File(path)).toURI());
			assert iri.isAbsolute();
			OWLOntology ont = super.loadOntology(iri, allowExists, configuration); //loadOntologyFromOntologyDocument(IRI.create(path));
			if (ont==null)
				Trace.log("ontology", "CASA_OWLOntologyManagerImpl(\""+path+"\"): failed to load original (web) ontology. Returning null.");
			return ont;
		} catch (Exception e) {
			Trace.log("ontology", "CASA_OWLOntologyManagerImpl(\""+path+"\"): failed to load ontology locally; trying original.", e);  
			OWLOntology ont = super.loadOntology(ontologyIRI, allowExists, configuration); //super.loadOntology(iri, allowExists, configuration);
			if (ont==null)
				Trace.log("ontology", "CASA_OWLOntologyManagerImpl(\""+ontologyIRI+"\"): failed to load original (web) ontology. Returning null." );
			return ont;
		}


  }
	
	/**
	 * From an ontology IRI of the forms
	 * <ul>
	 * <li> [{protocol}:][{host}]/ontologies/{name}[.owl]
	 * <li> {name}[.owl]
	 * </ul>
	 * return {name}.owl.
	 * @param ontologyIRI
	 * @return as specified above
	 */
	private String getOntologyNameFromIRI(IRI ontologyIRI) {
  	//determine the ontology name
  	URI uri = ontologyIRI.toURI();
  	String path = uri.getPath();
  	String name = path.contains("/") ? path.substring(path.lastIndexOf('/')) : path;
  	if (ontologyIRI.isAbsolute() && "http".equalsIgnoreCase(ontologyIRI.getScheme()) && name.startsWith("/ontologies/"))
  		name = path.substring(12);
  	if (!(name.endsWith(".owl") || name.endsWith(".OWL")))
  		name += ".owl";
  	return name;
	}
	
	/**
   * If this is an absolute IRI, but not http: or file: then go with the default behaviour.
	 * @param ontologyIRI
	 * @return true iff this is an absolute, but non-http IRI.
	 */
	private boolean shouldUseDefault(IRI ontologyIRI) {
		boolean absoluteIRI = ontologyIRI.isAbsolute(); 
		if (absoluteIRI) {
			String scheme = ontologyIRI.getScheme();
			boolean http = "http".equalsIgnoreCase(scheme);
			if (!(http /*|| file*/)) {
				return true;
			}

			// if this is an absolute IRI, but not referring to casa.cpsc.ucalgary.ca then go with the default behaviour
			URI uri = ontologyIRI.toURI();
			if (http && !"casa.cpsc.ucalgary.ca".equals(uri.getHost())) 
				return true;
		}

		return false;
	}
  
  private void addComment(OWLOntology ont, IRI iri, String comment) {
    OWLDataFactory df = getOWLDataFactory();
    OWLAnnotation commentAnno = df.getOWLAnnotation(
        df.getRDFSComment(),
        df.getOWLLiteral(comment, "en"));
    OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(iri, commentAnno);
    applyChange(new AddAxiom(ont, ax));
  }

//	/**
//	 * Unlike the super's method, this method will load the ontology if it can't be found.
//	 * @param ontologyIRI the IRI to find or load.
//	 * @return The ontology found or loaded, or null if it could not be found.
//	 * @see uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl#getOntology(org.semanticweb.owlapi.model.IRI)
//	 */
//	@Override
//	public OWLOntology getOntology(IRI ontologyIRI) {
//		OWLOntology ont = super.getOntology(ontologyIRI);
//		if (ont!=null)
//			return ont;
//	
//		// get the ontology by name naively
//		if (shouldUseDefault(ontologyIRI))
//			return super.getOntology(ontologyIRI);
//		
//		IRI iri;
//		if (ontologyIRI.isAbsolute()) {
//			iri = ontologyIRI;
//		}
//		else {
//		  String path = "http://casa.cpsc.ucalgary.ca/ontologies/"+getOntologyNameFromIRI(ontologyIRI);
//		  iri = IRI.create(path);
//		}
//		
//		OWLOntology ret = super.getOntology(iri);
//		if (ret==null) {
//			try {
//				ret = loadOntology(ontologyIRI);
//			} catch (OWLOntologyCreationException e) {
//				CASAUtil.log("ontology5", "CASA_OWLOntologyManagerImp.getOntology(): Can't load ontology \""+ontologyIRI+"\": "+e.getMessage()); //, e, true);
//			}
//		}
//		return ret;
//	}
	
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
	public Ontology getOntology(String name) throws IllegalArgumentException {
		org.semanticweb.owlapi.model.OWLOntology ont = null;
		String theName = name;
		Throwable e1 = null, e3=null;
		if (!theName.endsWith(".owl"))
			theName = theName+".owl";
		
		//try GETTING (from memory) the ontology based on the raw name
		IRI iri1 = IRI.create(theName);
		if (!iri1.isAbsolute()) {
			String frag = iri1.getFragment();
			iri1 = IRI.create(DEFAULT_ONT_PATH, frag==null?theName:frag);
		}
		try {
			ont = getOntology(iri1);
			if (ont!=null) {
				if (ont instanceof OWLOntology)
					return (Ontology)ont;
			}
		} catch (Throwable e) {
			e1 = e;
		}

		//try LOADING the ontology as though it were a file spec.
		try {
			String filePath = TransientAgent.findFileResourcePath(theName);
			if (filePath!=null) filePath = "file://"+filePath;
			IRI iri2 = null;
			if (filePath!=null) {
				try {
					iri2 = IRI.create(filePath);
//					ont = loadOntology(iri2);
					ont = loadOntologyFromOntologyDocument(iri2);
					if (ont!=null) {
						if (ont instanceof OWLOntology)
							return (Ontology)ont;
					}
				} catch (Throwable e) {
					Trace.log("error", "CASA_OWLOntologyManagerImpl: "+(iri2==null?filePath:iri2), e);
				}
			}
		} catch (IOException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		}//.getAbsoluteFile();

		//try LOADING the ontology in the normal way.  
		IRI iri3 = IRI.create(theName); 
		try {
			ont = loadOntology(iri3);
			if (ont!=null) {
				if (ont instanceof OWLOntology)
					return (Ontology)ont;
			}
		} catch (OWLOntologyCreationException e) {
			e3 = e;
		}
		
		StringBuilder b = new StringBuilder();
		b.append("OWLOntology.getOntology("+name+"): failed in manager.getOntology("+iri1+"), "+(e1==null?"":("(Exception: "+e1.toString()+") "))+"continuing...").append('\n');
//		if (isFile)
//			b.append("OWLOntology.getOntology("+name+"): failed in manager.loadOntology("+iri2+"), "+(e2==null?"":("(Exception: "+e2.toString()+") "))+"continuing...").append('\n');
//		else
//			b.append("OWLOntology.getOntology("+name+"): Can't interpret "+theName+" as an existing file path, attempted "+file.getAbsolutePath()+", continuing...").append('\n');
		b.append("OWLOntology.getOntology("+name+"): failed in manager.loadOntology("+iri3+"), "+(e3==null?"":("(Exception: "+e3.toString()+") "))+"giving up.");
		throw new IllegalArgumentException(b.toString());
	}
	


	/* (non-Javadoc)
	 * @see uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl#getOntology(org.semanticweb.owlapi.model.OWLOntologyID)
	 */
	@Override
	public OWLOntology getOntology(OWLOntologyID ontologyID) {
		// TODO Auto-generated method stub
		return super.getOntology(ontologyID);
	}

  
}
