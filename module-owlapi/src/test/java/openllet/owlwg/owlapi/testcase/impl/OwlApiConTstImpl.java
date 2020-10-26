package openllet.owlwg.owlapi.testcase.impl;

import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

import openllet.owlwg.testcase.ConsistencyTest;
import openllet.owlwg.testcase.TestCaseVisitor;

/**
 * <p>
 * Title: OWLAPI Consistency Test Case
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
public class OwlApiConTstImpl extends OwlApixCTImpl implements ConsistencyTest<OWLOntology>
{

	public OwlApiConTstImpl(final OWLOntology ontology, final OWLNamedIndividual i)
	{
		super(ontology, i);
	}

	@Override
	public void accept(final TestCaseVisitor<OWLOntology> visitor)
	{
		visitor.visit(this);
	}

	@Override
	public String toString()
	{
		return String.format("ConsistencyTest(%s)", getIdentifier());
	}

}
