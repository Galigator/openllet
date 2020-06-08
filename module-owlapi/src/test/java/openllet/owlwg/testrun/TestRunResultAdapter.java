package openllet.owlwg.testrun;

import static openllet.owlwg.testrun.ResultVocabulary.Class.TEST_RUN;
import static openllet.owlwg.testrun.ResultVocabulary.ObjectProperty.SYNTAX_CONSTRAINT;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import openllet.owlwg.testcase.TestVocabulary;
import openllet.owlwg.testrun.ResultVocabulary.AnnotationProperty;
import openllet.owlwg.testrun.ResultVocabulary.ObjectProperty;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * <p>
 * Title: Test Run Result Adapter
 * </p>
 * <p>
 * Description: Convert test run objects to OWLAPI object model
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
public class TestRunResultAdapter
{

	private class RunTypeAdapter implements TestRunResultVisitor
	{

		private List<OWLAxiom> axioms;

		public List<OWLAxiom> process(final TestRunResult result)
		{
			axioms = new ArrayList<>();
			result.accept(this);
			return axioms;
		}

		@Override
		public void visit(final SyntaxConstraintRun result)
		{
			axioms.add(_dataFactory.getOWLClassAssertionAxiom(result.getTestType().getOWLClass(), _currentIndividual));
			axioms.add(_dataFactory.getOWLObjectPropertyAssertionAxiom(SYNTAX_CONSTRAINT.getOWLObjectProperty(), _currentIndividual, result.getConstraint().getOWLIndividual()));
		}

		@Override
		public void visit(final ReasoningRun result)
		{
			axioms.add(_dataFactory.getOWLClassAssertionAxiom(result.getTestType().getOWLClass(), _currentIndividual));
		}

		@Override
		public void visit(final SyntaxTranslationRun result)
		{
			axioms.add(_dataFactory.getOWLClassAssertionAxiom(result.getTestType().getOWLClass(), _currentIndividual));
		}

	}

	private static Integer bnodeid;

	static
	{
		bnodeid = 0;
	}

	private static URI mintBNode()
	{
		bnodeid++;
		return URI.create(String.format("testrunadapter_%d", bnodeid));
	}

	private OWLAnonymousIndividual	_currentIndividual;
	private final OWLDataFactory	_dataFactory;
	private final RunTypeAdapter	_runTypeAdapter;

	public TestRunResultAdapter(final OWLDataFactory dataFactory)
	{
		if (dataFactory == null) throw new NullPointerException();

		_dataFactory = dataFactory;
		_runTypeAdapter = new RunTypeAdapter();
	}

	public Collection<OWLAxiom> asOWLAxioms(final TestRunResult r, final OWLAnonymousIndividual i)
	{
		if (r == null) throw new NullPointerException();

		final List<OWLAxiom> axioms = new ArrayList<>();

		_currentIndividual = i;
		axioms.add(_dataFactory.getOWLClassAssertionAxiom(TEST_RUN.getOWLClass(), _currentIndividual));
		axioms.add(_dataFactory.getOWLClassAssertionAxiom(r.getResultType().getOWLClass(), _currentIndividual));
		axioms.add(_dataFactory.getOWLObjectPropertyAssertionAxiom(ObjectProperty.RUNNER.getOWLObjectProperty(), _currentIndividual, _dataFactory.getOWLNamedIndividual(r.getTestRunner().getIRI())));

		final OWLIndividual testAnonIndividual = _dataFactory.getOWLAnonymousIndividual(mintBNode().toString());

		axioms.add(_dataFactory.getOWLObjectPropertyAssertionAxiom(ObjectProperty.TEST.getOWLObjectProperty(), _currentIndividual, testAnonIndividual));
		axioms.add(_dataFactory.getOWLDataPropertyAssertionAxiom(TestVocabulary.DatatypeProperty.IDENTIFIER.getOWLDataProperty(), testAnonIndividual,
				_dataFactory.getOWLLiteral(r.getTestCase().getIdentifier())));
		final String details = r.getDetails();
		if (details != null && details.length() > 0) axioms
				.add(_dataFactory.getOWLAnnotationAssertionAxiom(_dataFactory.getOWLAnnotationProperty(AnnotationProperty.DETAILS.getAnnotationPropertyIRI()), i, _dataFactory.getOWLLiteral(details)));

		axioms.addAll(_runTypeAdapter.process(r));

		return axioms;
	}
}
