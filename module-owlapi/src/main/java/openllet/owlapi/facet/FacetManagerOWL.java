package openllet.owlapi.facet;

import org.semanticweb.owlapi.model.OWLOntologyManager;

import openllet.owlapi.OWLGroup;

public interface FacetManagerOWL
{
	/**
	 * @return the manager that manage the current ontology.
	 * @since 2.5.1
	 */
	OWLOntologyManager getManager();

	/**
	 * @return the group of manager that own the getManager() returned manager.
	 * @since 2.6.1
	 */
	OWLGroup getGroup();
}
