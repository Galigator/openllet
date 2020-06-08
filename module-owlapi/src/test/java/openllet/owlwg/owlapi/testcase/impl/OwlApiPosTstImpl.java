package openllet.owlwg.owlapi.testcase.impl;

import openllet.owlwg.testcase.PositiveEntailmentTest;
import openllet.owlwg.testcase.TestCaseVisitor;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * <p>
 * Title: OWLAPI Positive Entailment Test Case
 * </p>
 * <p>
 * Description:
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
public class OwlApiPosTstImpl extends OwlApiETImpl implements PositiveEntailmentTest<OWLOntology>
{

	public OwlApiPosTstImpl(final OWLOntology ontology, final OWLNamedIndividual i)
	{
		super(ontology, i, true);
	}

	@Override
	public void accept(final TestCaseVisitor<OWLOntology> visitor)
	{
		visitor.visit(this);
	}

	@Override
	public String toString()
	{
		return String.format("PositiveEntailmentTest(%s)", getIdentifier());
	}
}
