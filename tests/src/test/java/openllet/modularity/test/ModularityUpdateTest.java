// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.modularity.test;

import static openllet.owlapi.OWL.all;
import static openllet.owlapi.OWL.classAssertion;
import static openllet.owlapi.OWL.equivalentClasses;
import static openllet.owlapi.OWL.subClassOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import openllet.core.utils.SetUtils;
import openllet.modularity.IncrementalClassifier;
import openllet.modularity.PelletIncremantalReasonerFactory;
import openllet.owlapi.OWL;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * <p>
 * Description: Test modular classification for correctness against unified classification
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public abstract class ModularityUpdateTest extends AbstractModularityTest
{

	@Test
	public void addNonLocal()
	{
		final OWLAxiom[] axioms = { subClassOf(_A, _B), subClassOf(_C, _D) };
		createOntology(axioms);

		final IncrementalClassifier modular = PelletIncremantalReasonerFactory.getInstance().createReasoner(_ontology);
		modular.classify();

		assertTrue(modular.isEntailed(subClassOf(_A, _B)));
		assertFalse(modular.isEntailed(subClassOf(_B, _C)));
		assertTrue(modular.isEntailed(subClassOf(_C, _D)));

		_ontology.add(Arrays.asList(equivalentClasses(_D, all(_p, _D)), subClassOf(_B, _C)));
		modular.classify();
		assertTrue(modular.isEntailed(subClassOf(_A, _B)));
		assertTrue(modular.isEntailed(subClassOf(_B, _C)));
		assertTrue(modular.isEntailed(subClassOf(_C, _D)));

		_ontology.remove(Arrays.asList(subClassOf(_A, _B)));
		modular.classify();
		assertFalse(modular.isEntailed(subClassOf(_A, _B)));
		assertTrue(modular.isEntailed(subClassOf(_B, _C)));
		assertTrue(modular.isEntailed(subClassOf(_D, _D)));

		modular.dispose();
	}

	@Test
	public void deleteNonLocal()
	{
		final OWLAxiom[] axioms = { subClassOf(_A, _B), subClassOf(_C, _D), equivalentClasses(_D, all(_p, _D)) };
		createOntology(axioms);

		final IncrementalClassifier modular = PelletIncremantalReasonerFactory.getInstance().createReasoner(_ontology);
		modular.classify();

		assertTrue(modular.isEntailed(subClassOf(_A, _B)));
		assertFalse(modular.isEntailed(subClassOf(_B, _C)));
		assertTrue(modular.isEntailed(subClassOf(_C, _D)));

		_ontology.remove(Arrays.asList(equivalentClasses(_D, all(_p, _D))));
		_ontology.add(Arrays.asList(subClassOf(_B, _C)));
		modular.classify();
		assertTrue(modular.isEntailed(subClassOf(_A, _B)));
		assertTrue(modular.isEntailed(subClassOf(_B, _C)));
		assertTrue(modular.isEntailed(subClassOf(_C, _D)));

		_ontology.remove(Arrays.asList(subClassOf(_A, _B)));
		modular.classify();
		assertFalse(modular.isEntailed(subClassOf(_A, _B)));
		assertTrue(modular.isEntailed(subClassOf(_B, _C)));
		assertTrue(modular.isEntailed(subClassOf(_D, _D)));

		modular.dispose();
	}

	@Test
	public void testDeferredClassification()
	{
		final OWLAxiom[] axioms = { subClassOf(_A, _B), subClassOf(_C, _D) };
		createOntology(axioms);

		final IncrementalClassifier modular = PelletIncremantalReasonerFactory.getInstance().createReasoner(_ontology);
		modular.classify();

		assertTrue(modular.isClassified());

		assertEquals(Collections.emptySet(), modular.getTypes(_a, false).entities().collect(Collectors.toSet()));

		assertTrue(modular.isRealized());

		_ontology.add(Arrays.asList(classAssertion(_a, _A)));

		// despite of having added a new fact, the classifier should still be in classified state (the axiom was an A-Box axiom)
		assertTrue(modular.isClassified());
		assertFalse(modular.isRealized());

		assertEquals(SetUtils.create(_A, _B, OWL.Thing), modular.getTypes(_a, false).entities().collect(Collectors.toSet()));
		assertTrue(modular.isEntailed(subClassOf(_A, _B)));
		assertFalse(modular.isEntailed(subClassOf(_A, _C)));

		assertTrue(modular.isRealized());

		// now try to add a T-Box axiom
		_ontology.add(Arrays.asList(subClassOf(_A, _C)));

		// the classifier should no longer be in classified state
		assertFalse(modular.isClassified());
		assertFalse(modular.isRealized());

		// force classification
		modular.classify();

		// check whether the classifier returned to the classified state
		assertTrue(modular.isClassified());

		assertEquals(SetUtils.create(_A, _B, _C, _D, OWL.Thing), modular.getTypes(_a, false).entities().collect(Collectors.toSet()));
		assertTrue(modular.isEntailed(subClassOf(_A, _B)));
		assertTrue(modular.isEntailed(subClassOf(_A, _C)));
	}
}
