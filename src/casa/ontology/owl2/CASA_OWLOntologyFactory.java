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

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.semanticweb.owlapi.io.OWLOntologyCreationIOException;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParser;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.model.UnloadableImportException;

import uk.ac.manchester.cs.owl.owlapi.ParsableOWLOntologyFactory;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class CASA_OWLOntologyFactory extends ParsableOWLOntologyFactory {

	private static final long serialVersionUID = 380823407617497785L;

	@Override
	public org.semanticweb.owlapi.model.OWLOntology createOWLOntology(OWLOntologyID ontologyID, IRI documentIRI, OWLOntologyCreationHandler handler) throws OWLOntologyCreationException {
		OWLOntology ont = new casa.ontology.owl2.OWLOntology(getOWLOntologyManager(), ontologyID);
		
    handler.ontologyCreated(ont);
    return ont;
	}
	
	/**
	 * This method is overridden here ONLY to correct the error calling super.CreateOWLOntology() rather
	 * than simply createOWLOntology().  See comments with label rck.
   * @see uk.ac.manchester.cs.owl.owlapi.ParsableOWLOntologyFactory#loadOWLOntology(org.semanticweb.owlapi.io.OWLOntologyDocumentSource, org.semanticweb.owlapi.model.OWLOntologyFactory.OWLOntologyCreationHandler, org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration)
	 */
  @Override
  public OWLOntology loadOWLOntology(OWLOntologyDocumentSource documentSource,
          OWLOntologyCreationHandler mediator,
          OWLOntologyLoaderConfiguration configuration)
          throws OWLOntologyCreationException {
      // Attempt to parse the ontology by looping through the parsers. If the
      // ontology is parsed successfully then we break out and return the
      // ontology.
      // I think that this is more reliable than selecting a parser based on a
      // file extension
      // for example (perhaps the parser list could be ordered based on most
      // likely parser, which
      // could be determined by file extension).
      Map<OWLParser, OWLParserException> exceptions = new LinkedHashMap<OWLParser, OWLParserException>();
      // Call the super method to create the ontology - this is needed,
      // because
      // we throw an exception if someone tries to create an ontology directly
      org.semanticweb.owlapi.model.OWLOntology existingOntology = null;
      IRI iri = documentSource.getDocumentIRI();
      if (getOWLOntologyManager().contains(iri)) {
          existingOntology = getOWLOntologyManager().getOntology(iri);
      }
      OWLOntologyID ontologyID = new OWLOntologyID();
//    OWLOntology ont = super.createOWLOntology(ontologyID,                        //corrected (removed "super." rck
//           documentSource.getDocumentIRI(), mediator);                           //
      org.semanticweb.owlapi.model.OWLOntology ont = createOWLOntology(ontologyID, //
          documentSource.getDocumentIRI(), mediator);                              //
      // Now parse the input into the empty ontology that we created
      for (final OWLParser parser : getParsers()) {
          try {
              if (existingOntology == null && !ont.isEmpty()) {
                  // Junk from a previous parse. We should clear the ont
                  getOWLOntologyManager().removeOntology(ont);
//                ont = super.createOWLOntology(ontologyID,                        //corrected (removed "super." rck
//                        documentSource.getDocumentIRI(), mediator);              //
                  ont = createOWLOntology(ontologyID,                        //
                      documentSource.getDocumentIRI(), mediator);                  //
              }
              OWLOntologyFormat format = parser.parse(documentSource, ont,
                      configuration);
              mediator.setOntologyFormat(ont, format);
              return (OWLOntology)ont;
          } catch (IOException e) {
              // No hope of any parsers working?
              // First clean up
              getOWLOntologyManager().removeOntology(ont);
              throw new OWLOntologyCreationIOException(e);
          } catch (UnloadableImportException e) {
              // First clean up
              getOWLOntologyManager().removeOntology(ont);
              throw e;
          } catch (OWLParserException e) {
              // Record this attempts and continue trying to parse.
              exceptions.put(parser, e);
          } catch (RuntimeException e) {
              // Clean up and rethrow
              getOWLOntologyManager().removeOntology(ont);
              throw e;
          }
      }
      if (existingOntology == null) {
          getOWLOntologyManager().removeOntology(ont);
      }
      // We haven't found a parser that could parse the ontology properly.
      // Throw an
      // exception whose message contains the stack traces from all of the
      // parsers
      // that we have tried.
      throw new UnparsableOntologyException(documentSource.getDocumentIRI(), exceptions);
  }

  @Override
  public boolean canCreateFromDocumentIRI(IRI documentIRI) {
      return true;
  }
  
  @Override
	public OWLOntology createOWLOntology(URI ontologyURI, URI physicalURI) {
    return new casa.ontology.owl2.OWLOntology(null, new OWLOntologyID(IRI.create(ontologyURI)));
}


}
