package openllet.protege.plugin.pellet;

import org.protege.editor.owl.model.inference.AbstractProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import openllet.core.OpenlletOptions;

/**
 * @author Evren Sirin
 */
public class PelletReasonerFactory extends AbstractProtegeOWLReasonerInfo
{
	static
	{
		// true = (default) Non DL axioms will be ignored (eg as use of complex
		// roles in cardinality restrictions)
		// false = pellet will throw an exception if non DL axioms are included
		OpenlletOptions.IGNORE_UNSUPPORTED_AXIOMS = false;

		OpenlletOptions.SILENT_UNDEFINED_ENTITY_HANDLING = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OWLReasonerFactory getReasonerFactory()
	{
		return openllet.owlapi.OpenlletReasonerFactory.getInstance();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferingMode getRecommendedBuffering()
	{
		return BufferingMode.BUFFERING;
	}
}
