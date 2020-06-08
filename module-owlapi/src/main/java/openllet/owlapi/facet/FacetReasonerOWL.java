package openllet.owlapi.facet;

import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * return a PelletReasoner that reason over this ontology.
 * 
 * @since 2.5.1
 */
public interface FacetReasonerOWL
{
	/**
	 * @return a PelletReasoner that reason over a previously registered ontology.
	 * @since  2.5.1
	 */
	OWLReasoner getReasoner();
}
