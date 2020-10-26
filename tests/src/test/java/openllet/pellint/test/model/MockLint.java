package openllet.pellint.test.model;

import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import openllet.pellint.model.Lint;

/**
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Harris Lin
 */
public class MockLint extends Lint
{
	public boolean _applyFixCalled = false;

	public MockLint()
	{
		super(null, null);
	}

	@Override
	public boolean applyFix(final OWLOntologyManager manager) throws OWLOntologyChangeException
	{
		_applyFixCalled = true;
		return true;
	}

}
