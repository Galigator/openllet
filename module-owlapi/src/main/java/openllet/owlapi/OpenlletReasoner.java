package openllet.owlapi;

import java.util.List;
import java.util.Set;
import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.owlapi.facet.FacetFactoryOWL;
import openllet.owlapi.facet.FacetManagerOWL;
import openllet.owlapi.facet.FacetOntologyOWL;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.reasoner.FreshEntitiesException;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.reasoner.TimeOutException;

public interface OpenlletReasoner extends OWLReasoner, OWLOntologyChangeListener, FacetManagerOWL, FacetOntologyOWL, FacetFactoryOWL
{
	/**
	 * Return the underlying Pellet knowledge base.
	 *
	 * @return the underlying Pellet knowledge base
	 */
	public KnowledgeBase getKB();

	/**
	 * Process all the given changes in an incremental fashion. Processing will _stop if a change cannot be handled incrementally and requires a reload. The
	 * reload will not be done as part of processing.
	 *
	 * @param changes the changes to be applied to the reasoner
	 * @return <code>true</code> if all changes have been processed successfully, <code>false</code> otherwise (indicates reasoner will reload the whole
	 *         _ontology next time it needs to do reasoning)
	 */
	public boolean processChanges(final List<? extends OWLOntologyChange> changes);

	public void prepareReasoner() throws ReasonerInterruptedException, TimeOutException;

	public void refresh();

	public ATermAppl term(final OWLObject d);

	public Set<OWLLiteral> getAnnotationPropertyValues(final OWLNamedIndividual ind, final OWLAnnotationProperty pe) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException;
}
