// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test.owlapi;

import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

import junit.framework.JUnit4TestAdapter;
import openllet.core.OpenlletOptions;
import openllet.core.utils.SetUtils;
import openllet.owlapi.OWL;
import openllet.owlapi.OntologyUtils;
import openllet.owlapi.OpenlletReasonerFactory;
import openllet.test.PelletTestSuite;

/**
 * @author Evren Sirin
 */
public class OWLPrimerTests extends AbstractOWLAPITests
{
	protected static final String NS = "http://example.com/owl/families/";
	protected static final String NS2 = "http://example.org/otherOntologies/families/";

	protected static final OWLNamedIndividual John = OWL.Individual(NS + "John");
	protected static final OWLNamedIndividual Jack = OWL.Individual(NS + "Jack");
	protected static final OWLNamedIndividual Bill = OWL.Individual(NS + "Bill");
	protected static final OWLNamedIndividual Mary = OWL.Individual(NS + "Mary");
	protected static final OWLNamedIndividual MaryBrown = OWL.Individual(NS2 + "MaryBrown");

	protected static final OWLObjectProperty hasParent = OWL.ObjectProperty(NS + "hasParent");
	protected static final OWLObjectProperty hasSpouse = OWL.ObjectProperty(NS + "hasSpouse");
	protected static final OWLObjectProperty hasWife = OWL.ObjectProperty(NS + "hasWife");
	protected static final OWLObjectProperty hasChild = OWL.ObjectProperty(NS + "hasChild");
	protected static final OWLObjectProperty child = OWL.ObjectProperty(NS2 + "child");
	protected static final OWLObjectProperty parentOf = OWL.ObjectProperty(NS2 + "parentOf");

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter(OWLPrimerTests.class);
	}

	@Override
	@Before
	public void setUp()
	{
		_ontology = OntologyUtils.loadOntology(OWLManager.createOWLOntologyManager(), "file:" + PelletTestSuite.base + "modularity/OWL2Primer.owl");
		_reasoner = OpenlletReasonerFactory.getInstance().createReasoner(_ontology);
	}

	protected <T> Set<T> node(@SuppressWarnings("unchecked") final T... inds)
	{
		return SetUtils.create(inds);
	}

	protected Set<OWLObjectPropertyExpression> nodeOP(final OWLObjectPropertyExpression... inds)
	{
		return SetUtils.create(inds);
	}

	@SafeVarargs
	final protected static <E extends OWLObject> void assertEquals(final NodeSet<E> actual, final Set<E>... expected)
	{
		final Set<Set<E>> expectedSet = SetUtils.create(expected);

		final Iterable<Node<E>> it = actual.nodes()::iterator;
		for (final Node<E> node : it)
		{
			final Set<E> entities = node.entities().collect(Collectors.toSet());
			assertTrue("Unexpected value: " + entities + "\tremaing:{" + expectedSet + "}", expectedSet.remove(entities));
		}
		assertTrue("Missing values: " + expectedSet, expectedSet.isEmpty());
	}

	@Test
	public void testJackDifferents()
	{
		assertEquals(_reasoner.getDifferentIndividuals(John), node(Jack), node(Bill), node(Mary, MaryBrown));
	}

	@Test
	public void testHasParentDisjoints()
	{
		// SubObjectPropertyOf(<http://example.com/owl/families/hasWife> <http://example.com/owl/families/hasSpouse>)
		// SymmetricObjectProperty(<http://example.com/owl/families/hasSpouse>)
		// DisjointObjectProperties(<http://example.com/owl/families/hasParent> <http://example.com/owl/families/hasSpouse> )

		// EquivalentClasses(<http://example.com/owl/families/JohnsChildren> ObjectHasValue(<http://example.com/owl/families/hasParent> <http://example.com/owl/families/John>) )
		// InverseObjectProperties(<http://example.com/owl/families/hasParent> <http://example.com/owl/families/hasChild>)
		// SubObjectPropertyOf(<http://example.com/owl/families/hasFather> <http://example.com/owl/families/hasParent>)
		// SubObjectPropertyOf(ObjectPropertyChain( <http://example.com/owl/families/hasParent> <http://example.com/owl/families/hasParent> ) <http://example.com/owl/families/hasGrandparent>)
		// SubClassOf(<http://example.com/owl/families/ChildlessPerson> ObjectIntersectionOf(<http://example.com/owl/families/Person> ObjectComplementOf(ObjectSomeValuesFrom(ObjectInverseOf(<http://example.com/owl/families/hasParent>) owl:Thing))))
		// SubObjectPropertyOf(<http://example.com/owl/families/hasFather> <http://example.com/owl/families/hasParent>)

		// EquivalentObjectProperties(<http://example.com/owl/families/hasChild> <http://example.org/otherOntologies/families/child> )

		assertTrue(_reasoner.isEntailed(OWL.disjointProperties(hasParent, hasSpouse)));
		assertTrue(_reasoner.isEntailed(OWL.disjointProperties(hasParent, hasWife)));
		assertTrue(_reasoner.isEntailed(OWL.disjointProperties(hasParent, child)));
		assertTrue(_reasoner.isEntailed(OWL.disjointProperties(hasParent, hasChild)));
		assertTrue(_reasoner.isEntailed(OWL.disjointProperties(hasParent, OWL.bottomObjectProperty)));

		final NodeSet<OWLObjectPropertyExpression> allDisjoincts = _reasoner.getDisjointObjectProperties(hasParent);
		allDisjoincts.entities().map(OWLObjectPropertyExpression::toString).sorted().forEach(System.out::println);

		// _reasoner.getOntology().axioms().map(OWLAxiom::toString).sorted().forEach(System.out::println);

		if (OpenlletOptions.RETURN_NON_PRIMITIVE_EQUIVALENT_PROPERTIES)
			assertEquals(allDisjoincts, //
					nodeOP(OWL.inverse(hasSpouse), hasSpouse), // hasParent != hasSpouse && hasParent != Inv(hasSpouse) because of 'SymmetricObjectProperty'
					nodeOP(OWL.bottomObjectProperty), // Don't know how this is conclude but it doesn't look bad.
					nodeOP(hasWife), // hasWife != hasParent because  hasParent extends hasWife
					nodeOP(OWL.inverse(hasParent), hasChild, child)// hasParent != Inv(hasParent) by definition &  Inv(hasParent) == hasChild because of explicit axiom. Also by axiom child <-> hasChild.
			// And we don't speak about hasFather because it is a sub property of hasParent.
			);
		else
			assertEquals(allDisjoincts, //
					nodeOP(hasSpouse), //
					nodeOP(OWL.bottomObjectProperty), //
					nodeOP(hasWife), //
					nodeOP(hasChild, child)//
			);
	}
}
