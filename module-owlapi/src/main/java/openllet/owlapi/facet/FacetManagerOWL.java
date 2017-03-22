package openllet.owlapi.facet;

import openllet.owlapi.OWLGroup;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @return the manager that manage the current ontology.
 * @since 2.5.1
 */
public interface FacetManagerOWL
{
	/**
	 * @return the manager that manage the current ontology.
	 * @since 2.5.1
	 */
	public OWLOntologyManager getManager();

	/**
	 * @return the group of manager that own the getManager() returned manager.
	 * @since 2.6.1
	 */
	public OWLGroup getGroup();
}
