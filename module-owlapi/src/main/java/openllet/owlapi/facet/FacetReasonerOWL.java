package openllet.owlapi.facet;

import openllet.owlapi.PelletReasoner;

/**
 * @return a PelletReasoner that reason over this ontology.
 * @since 2.5.1
 */
public interface FacetReasonerOWL
{
	/**
	 * @return a PelletReasoner that reason over a previously registered ontology.
	 * @since 2.5.1
	 */
	public abstract PelletReasoner getReasoner();
}
