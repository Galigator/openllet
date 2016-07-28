// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import openllet.atom.OpenError;
import openllet.owlapi.OWL;
import openllet.owlapi.OntologyUtils;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyIRIMapperImpl;

public class OWLAPIWebOntTester implements WebOntTester
{
	private final OWLOntologyManager _manager;
	private OpenlletReasoner _reasoner;
	private final OWLOntologyIRIMapperImpl _mapper;

	public OWLAPIWebOntTester()
	{
		_manager = OWL._manager;
		_mapper = new OWLOntologyIRIMapperImpl();
	}

	@Override
	public void classify()
	{
		_reasoner.getKB().realize();
	}

	@Override
	public boolean isConsistent()
	{
		return _reasoner.isConsistent();
	}

	@Override
	public void testEntailment(final String entailmentFileURI, final boolean positiveEntailment)
	{
		try
		{
			final OWLOntology ont = _manager.loadOntology(IRI.create(entailmentFileURI));
			ont.logicalAxioms().filter(axiom -> !_reasoner.isEntailed(axiom)).forEach(axiom -> assertFalse("Entailment failed for " + axiom, positiveEntailment));
			assertTrue("All axioms entailed in negative entailment test", positiveEntailment);
		}
		catch (final OWLException e)
		{
			throw new OpenError(e);
		}
	}

	@Override
	public void setInputOntology(final String inputFileURI)
	{
		OntologyUtils.clearOWLOntologyManager();
		OWLOntology ont = null;
		try
		{
			_manager.getIRIMappers().add(_mapper);
			ont = _manager.loadOntology(IRI.create(inputFileURI));
			_reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ont);
		}
		catch (final OWLException e)
		{
			throw new OpenError(e);
		}
		finally
		{
			if (ont != null)
				_manager.removeOntology(ont);
		}
	}

	@Override
	public void setTimeout(final long timeout)
	{
		_reasoner.getKB().setTimeout(timeout);
	}

	@Override
	public void registerURIMapping(final String fromURI, final String toURI)
	{
		_mapper.addMapping(IRI.create(fromURI), IRI.create(toURI));
	}

}
