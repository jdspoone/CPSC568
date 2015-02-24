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

import org.coode.owlapi.functionalrenderer.OWLFunctionalSyntaxOntologyStorer;
import org.coode.owlapi.latex.LatexOntologyStorer;
import org.coode.owlapi.obo.renderer.OBOFlatFileOntologyStorer;
import org.coode.owlapi.owlxml.renderer.OWLXMLOntologyStorer;
import org.coode.owlapi.rdf.rdfxml.RDFXMLOntologyStorer;
import org.coode.owlapi.turtle.TurtleOntologyStorer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.NonMappingOntologyIRIMapper;

import de.uulm.ecs.ai.owlapi.krssrenderer.KRSS2OWLSyntaxOntologyStorer;

import uk.ac.manchester.cs.owl.owlapi.EmptyInMemOWLOntologyFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl;
import uk.ac.manchester.cs.owl.owlapi.ParsableOWLOntologyFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOntologyStorer;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class CASA_OWLManager extends OWLManager {

	/**
   * Creates an OWL ontology manager that is configured with standard parsers,
   * storeres etc.
   *
   * @return The new manager.
   */
  public static OWLOntologyManager createOWLOntologyManager() {
      return createOWLOntologyManager(getOWLDataFactory());
  }

	public static OWLOntologyManager createOWLOntologyManager(OWLDataFactory dataFactory) {
    // Create the ontology manager and add ontology factories, mappers and storers
    OWLOntologyManager ontologyManager = new CASA_OWLOntologyManagerImpl(dataFactory);
    ontologyManager.addOntologyStorer(new RDFXMLOntologyStorer());
    ontologyManager.addOntologyStorer(new OWLXMLOntologyStorer());
    ontologyManager.addOntologyStorer(new OWLFunctionalSyntaxOntologyStorer());
    ontologyManager.addOntologyStorer(new ManchesterOWLSyntaxOntologyStorer());
    ontologyManager.addOntologyStorer(new OBOFlatFileOntologyStorer());
    ontologyManager.addOntologyStorer(new KRSS2OWLSyntaxOntologyStorer());
    ontologyManager.addOntologyStorer(new TurtleOntologyStorer());
    ontologyManager.addOntologyStorer(new LatexOntologyStorer());

    ontologyManager.addIRIMapper(new NonMappingOntologyIRIMapper());

    ontologyManager.addOntologyFactory(new EmptyInMemOWLOntologyFactory());
  //ontologyManager.addOntologyFactory(new ParsableOWLOntologyFactory());
    ontologyManager.addOntologyFactory(new CASA_OWLOntologyFactory());

    return ontologyManager;

//		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager(dataFactory);
//		java.util.Collection<OWLOntologyFactory> set = ontologyManager.getOntologyFactories();
//		for (OWLOntologyFactory o: set) {
//			if (o instanceof ParsableOWLOntologyFactory) {
//				ontologyManager.removeOntologyFactory(o);
//			}
//		}
//    ontologyManager.addOntologyFactory(new CASA_OWLOntologyFactory());
//
//
//    return ontologyManager;
}
	
}
