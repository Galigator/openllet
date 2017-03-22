package openllet.owlapi;

import java.io.File;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * All methods here are redundant or bugged.s
 *
 * @since 2.5.1
 * @deprecated since 2.6.1
 */
@Deprecated
public class OWLUtils
{
	/**
	 * @param iri an iri that is potentially valid or with a namespace separator.
	 * @return The iri without the part that show the namespace as separate object as the individual name.
	 * @since 2.5.1
	 * @deprecated use IRIUtils.iriModel2iri
	 */
	@Deprecated
	static public String iriModel2iri(final String iri)
	{
		return (!iri.startsWith("{")) ? iri : iri.replaceAll("[\\{\\}]", "");
	}

	@Deprecated
	static public OWLGroup getOwlManagerGroup()
	{
		return OWL._managerGroup;
	}

	@Deprecated
	static public void loadDirectory(final File directory)
	{
		OWL._managerGroup.loadDirectory(directory);
	}

	@Deprecated
	public static String ontology2filename(final OWLOntologyID ontId)
	{
		return OWL._managerGroup.ontology2filename(ontId);
	}

	@Deprecated
	public static String ontology2filename(final OWLOntology ontology)
	{
		return OWL._managerGroup.ontology2filename(ontology);
	}
}
