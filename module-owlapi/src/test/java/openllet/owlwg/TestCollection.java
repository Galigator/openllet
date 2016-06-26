package openllet.owlwg;

import static openllet.owlwg.testcase.TestVocabulary.Class.CONSISTENCY_TEST;
import static openllet.owlwg.testcase.TestVocabulary.Class.INCONSISTENCY_TEST;
import static openllet.owlwg.testcase.TestVocabulary.Class.NEGATIVE_ENTAILMENT_TEST;
import static openllet.owlwg.testcase.TestVocabulary.Class.POSITIVE_ENTAILMENT_TEST;
import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asSet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import openllet.owlwg.testcase.ConsistencyTest;
import openllet.owlwg.testcase.InconsistencyTest;
import openllet.owlwg.testcase.NegativeEntailmentTest;
import openllet.owlwg.testcase.PositiveEntailmentTest;
import openllet.owlwg.testcase.TestCase;
import openllet.owlwg.testcase.TestCaseFactory;
import openllet.owlwg.testcase.filter.FilterCondition;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * <p>
 * Title: Test Collection
 * </p>
 * <p>
 * Description: Converts an ontology containing test case descriptions into an iterable collection of {@link TestCase} objects
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
public class TestCollection<O> implements Iterable<TestCase<O>>
{

	private final Map<OWLIndividual, TestCase<O>> cases;

	public TestCollection(TestCaseFactory<O> factory, OWLOntology o)
	{
		this(factory, o, FilterCondition.ACCEPT_ALL);
	}

	public TestCollection(TestCaseFactory<O> factory, OWLOntology o, FilterCondition filter)
	{
		if (factory == null)
			throw new NullPointerException();

		if (filter == null)
			throw new NullPointerException();

		cases = new HashMap<>();
		Set<OWLClassAssertionAxiom> axioms;

		axioms = asSet(o.classAssertionAxioms(POSITIVE_ENTAILMENT_TEST.getOWLClass()));

		if (axioms != null)
		{
			for (final OWLClassAssertionAxiom ax : axioms)
			{
				final OWLNamedIndividual i = ax.getIndividual().asOWLNamedIndividual();
				final PositiveEntailmentTest<O> t = factory.getPositiveEntailmentTestCase(o, i);
				if (filter.accepts(t))
					cases.put(i, t);
			}
		}

		axioms = asSet(o.classAssertionAxioms(NEGATIVE_ENTAILMENT_TEST.getOWLClass()));
		if (axioms != null)
		{
			for (final OWLClassAssertionAxiom ax : axioms)
			{
				final OWLNamedIndividual i = ax.getIndividual().asOWLNamedIndividual();
				final NegativeEntailmentTest<O> t = factory.getNegativeEntailmentTestCase(o, i);
				if (filter.accepts(t))
					cases.put(i, t);
			}
		}

		axioms = asSet(o.classAssertionAxioms(CONSISTENCY_TEST.getOWLClass()));
		if (axioms != null)
		{
			for (final OWLClassAssertionAxiom ax : axioms)
			{
				final OWLNamedIndividual i = ax.getIndividual().asOWLNamedIndividual();
				/*
				 * Verify the identifier is not already in the map because both
				 * entailment tests may also be marked as consistency tests.
				 */
				if (cases.containsKey(i))
					continue;
				final ConsistencyTest<O> t = factory.getConsistencyTestCase(o, i);
				if (filter.accepts(t) && !cases.containsKey(i))
					cases.put(i, t);
			}
		}

		axioms = asSet(o.classAssertionAxioms(INCONSISTENCY_TEST.getOWLClass()));
		if (axioms != null)
		{
			for (final OWLClassAssertionAxiom ax : axioms)
			{
				final OWLNamedIndividual i = ax.getIndividual().asOWLNamedIndividual();
				final InconsistencyTest<O> t = factory.getInconsistencyTestCase(o, i);
				if (filter.accepts(t))
					cases.put(i, t);
			}
		}
	}

	public LinkedList<TestCase<O>> asList()
	{
		return new LinkedList<>(getTests());
	}

	private Collection<TestCase<O>> getTests()
	{
		return Collections.unmodifiableCollection(cases.values());
	}

	@Override
	public Iterator<TestCase<O>> iterator()
	{
		return getTests().iterator();
	}

	public int size()
	{
		return getTests().size();
	}
}
