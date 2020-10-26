// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.owlapi.explanation;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;
import com.clarkparsia.owlapi.explanation.SatisfiabilityConverter;
import com.clarkparsia.owlapi.explanation.TransactionAwareSingleExpGen;

import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;

/**
 * @author Evren Sirin
 */
public class PelletExplanation
{
	static
	{
		setup();
	}

	/**
	 * Very important initialization step that needs to be called once before a reasoner is created. This function will be called automatically when
	 * GlassBoxExplanation is loaded by the class loader. This function simply calls the {@link GlassBoxExplanation#setup()} function.
	 */
	public static void setup()
	{
		GlassBoxExplanation.setup();
	}

	private final OWLDataFactory _factory;

	private final HSTExplanationGenerator _expGen;

	private final SatisfiabilityConverter _converter;

	public PelletExplanation(final OWLOntology ontology)
	{
		this(ontology, true);
	}

	public PelletExplanation(final OWLOntology ontology, final boolean useGlassBox)
	{
		this(new OpenlletReasonerFactory().createReasoner(ontology), useGlassBox);
	}

	public PelletExplanation(final OpenlletReasoner reasoner)
	{
		this(reasoner, true);
	}

	private PelletExplanation(final OpenlletReasoner reasoner, final boolean useGlassBox)
	{
		// Get the _factory object
		_factory = reasoner.getManager().getOWLDataFactory();

		// Create a single explanation generator
		final TransactionAwareSingleExpGen singleExp = useGlassBox ? new GlassBoxExplanation(reasoner) : new BlackBoxExplanation(reasoner.getRootOntology(), new OpenlletReasonerFactory(), reasoner);

		// Create multiple explanation generator
		_expGen = new HSTExplanationGenerator(singleExp);

		// Create the converter that will translate axioms into class expressions
		_converter = new SatisfiabilityConverter(_factory);
	}

	public Set<OWLAxiom> getEntailmentExplanation(final OWLAxiom axiom)
	{
		final OWLClassExpression unsatClass = _converter.convert(axiom);
		return getUnsatisfiableExplanation(unsatClass);
	}

	public Set<Set<OWLAxiom>> getEntailmentExplanations(final OWLAxiom axiom)
	{
		final OWLClassExpression unsatClass = _converter.convert(axiom);
		return getUnsatisfiableExplanations(unsatClass);
	}

	public Set<Set<OWLAxiom>> getEntailmentExplanations(final OWLAxiom axiom, final int maxExplanations)
	{
		final OWLClassExpression unsatClass = _converter.convert(axiom);
		return getUnsatisfiableExplanations(unsatClass, maxExplanations);
	}

	public Set<OWLAxiom> getInconsistencyExplanation()
	{
		return getUnsatisfiableExplanation(_factory.getOWLThing());
	}

	public Set<Set<OWLAxiom>> getInconsistencyExplanations()
	{
		return getUnsatisfiableExplanations(_factory.getOWLThing());
	}

	public Set<Set<OWLAxiom>> getInconsistencyExplanations(final int maxExplanations)
	{
		return getUnsatisfiableExplanations(_factory.getOWLThing(), maxExplanations);
	}

	public Set<OWLAxiom> getInstanceExplanation(final OWLIndividual ind, final OWLClassExpression cls)
	{
		final OWLClassAssertionAxiom classAssertion = _factory.getOWLClassAssertionAxiom(cls, ind);
		return getEntailmentExplanation(classAssertion);
	}

	public Set<Set<OWLAxiom>> getInstanceExplanations(final OWLIndividual ind, final OWLClassExpression cls)
	{
		final OWLClassAssertionAxiom classAssertion = _factory.getOWLClassAssertionAxiom(cls, ind);
		return getEntailmentExplanations(classAssertion);
	}

	public Set<Set<OWLAxiom>> getInstanceExplanations(final OWLIndividual ind, final OWLClassExpression cls, final int maxExplanations)
	{
		final OWLClassAssertionAxiom classAssertion = _factory.getOWLClassAssertionAxiom(cls, ind);
		return getEntailmentExplanations(classAssertion, maxExplanations);
	}

	public Set<OWLAxiom> getSubClassExplanation(final OWLClassExpression subClass, final OWLClassExpression superClass)
	{
		final OWLSubClassOfAxiom subClassAxiom = _factory.getOWLSubClassOfAxiom(subClass, superClass);
		return getEntailmentExplanation(subClassAxiom);
	}

	public Set<Set<OWLAxiom>> getSubClassExplanations(final OWLClassExpression subClass, final OWLClassExpression superClass)
	{
		final OWLSubClassOfAxiom subClassAxiom = _factory.getOWLSubClassOfAxiom(subClass, superClass);
		return getEntailmentExplanations(subClassAxiom);
	}

	public Set<Set<OWLAxiom>> getSubClassExplanations(final OWLClassExpression subClass, final OWLClassExpression superClass, final int maxExplanations)
	{
		final OWLSubClassOfAxiom subClassAxiom = _factory.getOWLSubClassOfAxiom(subClass, superClass);
		return getEntailmentExplanations(subClassAxiom, maxExplanations);
	}

	/**
	 * Returns a single explanation for an arbitrary class expression, or empty set if the given expression is satisfiable.
	 *
	 * @param unsatClass an unsatisfiabile class expression which is will be explained
	 * @return set of axioms explaining the unsatisfiability of given class expression, or empty set if the given expression is satisfiable.
	 */
	public Set<OWLAxiom> getUnsatisfiableExplanation(final OWLClassExpression unsatClass)
	{
		return _expGen.getExplanation(unsatClass);
	}

	/**
	 * Returns all the explanations for the given unsatisfiable class.
	 *
	 * @param unsatClass The class that is unsatisfiable for which an explanation will be generated.
	 * @return All explanations for the given unsatisfiable class, or an empty set if the concept is satisfiable
	 */
	public Set<Set<OWLAxiom>> getUnsatisfiableExplanations(final OWLClassExpression unsatClass)
	{
		return _expGen.getExplanations(unsatClass);
	}

	/**
	 * Return a specified number of explanations for the given unsatisfiable class. A smaller number of explanations can be returned if there are not as many
	 * explanations for the given concept. The returned set will be empty if the given class is satisfiable,
	 *
	 * @param unsatClass The class that is unsatisfiable for which an explanation will be generated.
	 * @param maxExplanations Maximum number of explanations requested, or 0 to get all the explanations
	 * @return A specified number of explanations for the given unsatisfiable class, or an empty set if the concept is satisfiable
	 */
	public Set<Set<OWLAxiom>> getUnsatisfiableExplanations(final OWLClassExpression unsatClass, final int maxExplanations)
	{
		return _expGen.getExplanations(unsatClass, maxExplanations);
	}
}
