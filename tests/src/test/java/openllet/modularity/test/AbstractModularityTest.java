// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.modularity.test;

import static openllet.owlapi.OWL.Class;
import static openllet.owlapi.OWL.Individual;
import static openllet.owlapi.OWL.ObjectProperty;

import openllet.modularity.ModuleExtractor;
import openllet.owlapi.OWL;
import org.junit.After;
import org.junit.Before;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * <p>
 * Title: Tests modularity results for simple hand-made ontologies.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public abstract class AbstractModularityTest
{
	protected volatile OWLOntology			_ontology;
	protected volatile OWLOntologyManager	_manager;
	protected volatile ModuleExtractor		_modExtractor;

	protected final OWLClass				_A	= Class("A");
	protected final OWLClass				_B	= Class("B");
	protected final OWLClass				_C	= Class("C");
	protected final OWLClass				_D	= Class("D");
	protected final OWLClass				_E	= Class("E");
	protected final OWLClass				_F	= Class("F");
	protected final OWLClass				_G	= Class("G");
	protected final OWLClass				_H	= Class("H");

	protected final OWLNamedIndividual		_a	= Individual("a");
	protected final OWLNamedIndividual		_b	= Individual("b");
	protected final OWLNamedIndividual		_c	= Individual("c");
	protected final OWLNamedIndividual		_d	= Individual("d");
	protected final OWLNamedIndividual		_e	= Individual("e");
	protected final OWLNamedIndividual		_f	= Individual("f");
	protected final OWLNamedIndividual		_g	= Individual("g");
	protected final OWLNamedIndividual		_h	= Individual("h");

	protected OWLObjectProperty				_p	= ObjectProperty("p");
	protected OWLObjectProperty				_q	= ObjectProperty("q");

	public abstract ModuleExtractor createModuleExtractor();

	protected void createOntology(final OWLAxiom... axioms)
	{
		_ontology = OWL.Ontology(axioms);
	}

	@Before
	public void before()
	{
		// create an empty module extractor
		_manager = OWLManager.createOWLOntologyManager();
		_modExtractor = createModuleExtractor();
	}

	@After
	public void after()
	{
		_ontology = null;
		_modExtractor = null;
		_manager.clearOntologies();
	}
}
