package openllet.owlapi.facet;

import openllet.owlapi.OWLGroup;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public interface FacetManagerOWL
{
	/**
	 * @return the manager that manage the current ontology.
	 * @since  2.5.1
	 */
	OWLOntologyManager getManager();

	/**
	 * @return the group of manager that own the getManager() returned manager.
	 * @since  2.6.1
	 */
	OWLGroup getGroup();
}
