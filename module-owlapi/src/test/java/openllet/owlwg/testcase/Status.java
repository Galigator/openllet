package openllet.owlwg.testcase;

import org.semanticweb.owlapi.model.OWLIndividual;

import openllet.owlwg.testcase.TestVocabulary.Individual;

/**
 * <p>
 * Title: Status
 * </p>
 * <p>
 * Description: See <a href="http://www.w3.org/TR/owl2-test/#Status">OWL 2 Conformance: Status</a>.
 * </p>
 * <p>
 * Copyright: Copyright &copy; 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <a href="http://clarkparsia.com/"/>http://clarkparsia.com/</a>
 * </p>
 *
 * @author Mike Smith &lt;msmith@clarkparsia.com&gt;
 */
public enum Status
{

	APPROVED(Individual.APPROVED), EXTRACREDIT(Individual.EXTRACREDIT), PROPOSED(Individual.PROPOSED), REJECTED(Individual.REJECTED);

	public static Status get(final OWLIndividual i)
	{
		for (final Status s : Status.values())
			if (s.getOWLIndividual().equals(i))
				return s;

		return null;
	}

	private final TestVocabulary.Individual _i;

	private Status(final TestVocabulary.Individual i)
	{
		_i = i;
	}

	public OWLIndividual getOWLIndividual()
	{
		return _i.getOWLIndividual();
	}
}
