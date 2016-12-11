package openllet.owlwg.owlapi.runner.impl;

import static openllet.owlwg.testrun.RunResultType.FAILING;
import static openllet.owlwg.testrun.RunResultType.INCOMPLETE;
import static openllet.owlwg.testrun.RunResultType.PASSING;
import static openllet.owlwg.testrun.RunTestType.CONSISTENCY;
import static openllet.owlwg.testrun.RunTestType.INCONSISTENCY;
import static openllet.owlwg.testrun.RunTestType.NEGATIVE_ENTAILMENT;
import static openllet.owlwg.testrun.RunTestType.POSITIVE_ENTAILMENT;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import openllet.atom.OpenError;
import openllet.owlwg.owlapi.testcase.impl.OwlApiCase;
import openllet.owlwg.runner.TestRunner;
import openllet.owlwg.testcase.ConsistencyTest;
import openllet.owlwg.testcase.EntailmentTest;
import openllet.owlwg.testcase.InconsistencyTest;
import openllet.owlwg.testcase.NegativeEntailmentTest;
import openllet.owlwg.testcase.OntologyParseException;
import openllet.owlwg.testcase.PositiveEntailmentTest;
import openllet.owlwg.testcase.PremisedTest;
import openllet.owlwg.testcase.SerializationFormat;
import openllet.owlwg.testcase.TestCase;
import openllet.owlwg.testcase.TestCaseVisitor;
import openllet.owlwg.testrun.ReasoningRun;
import openllet.owlwg.testrun.RunTestType;
import openllet.owlwg.testrun.TestRunResult;
import openllet.shared.tools.Log;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * <p>
 * Title: OWLAPI Abstract Test Runner
 * </p>
 * <p>
 * Description: Base test _runner implementation intended to encapsulate non-interesting bits of the test _runner and make reuse and _runner implementation
 * easier. Handles test type-specific behavior and _timeout enforcement.
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
public abstract class OwlApiAbstractRunner implements TestRunner<OWLOntology>
{

	private static final SerializationFormat[] formatList = new SerializationFormat[] { //
			SerializationFormat.RDFXML, SerializationFormat.FUNCTIONAL, SerializationFormat.OWLXML };

	protected static final Logger _logger = Log.getLogger(OwlApiAbstractRunner.class);

	private final Runner _runner;
	protected long _timeout;

	protected abstract class AbstractTestAsRunnable<T extends TestCase<OWLOntology>> implements TestAsRunnable
	{

		protected TestRunResult _result;
		protected final T _testcase;
		protected Throwable _throwable;
		protected final RunTestType _type;

		public AbstractTestAsRunnable(final T testcase, final RunTestType type)
		{
			this._testcase = testcase;

			if (!EnumSet.of(CONSISTENCY, INCONSISTENCY, NEGATIVE_ENTAILMENT, POSITIVE_ENTAILMENT).contains(type))
				throw new IllegalArgumentException();

			this._type = type;
			_result = null;
			_throwable = null;
		}

		@Override
		public TestRunResult getErrorResult(final Throwable th)
		{
			th.printStackTrace();
			return new ReasoningRun(_testcase, INCOMPLETE, _type, OwlApiAbstractRunner.this, th.getMessage(), th);
		}

		@Override
		public TestRunResult getResult() throws Throwable
		{
			if (_throwable != null)
				throw _throwable;
			if (_result == null)
				throw new IllegalStateException();

			return _result;
		}

		@Override
		public TestRunResult getTimeoutResult()
		{
			return new ReasoningRun(_testcase, INCOMPLETE, _type, OwlApiAbstractRunner.this, String.format("Timeout: %s ms", _timeout));
		}
	}

	private class Runner implements TestCaseVisitor<OWLOntology>
	{

		private volatile TestRunResult[] _results;

		public List<TestRunResult> getResults(final OwlApiCase testcase)
		{
			_results = null;
			testcase.accept(this);
			return Arrays.asList(_results);
		}

		@Override
		public void visit(final ConsistencyTest<OWLOntology> testcase)
		{
			_results = new TestRunResult[1];
			_results[0] = runConsistencyTest(testcase);
			if (null == _results[0])
				throw new OpenError("Result[0] is null");
		}

		@Override
		public void visit(final InconsistencyTest<OWLOntology> testcase)
		{
			_results = new TestRunResult[1];
			_results[0] = runInconsistencyTest(testcase);
			if (null == _results[0])
				throw new OpenError("Result[0] is null");
		}

		@Override
		public void visit(final NegativeEntailmentTest<OWLOntology> testcase)
		{
			_results = new TestRunResult[2];
			_results[0] = runConsistencyTest(testcase);
			if (null == _results[0])
				throw new OpenError("Result[0] is null");
			_results[1] = runEntailmentTest(testcase);
			if (null == _results[1])
				throw new OpenError("Result[1] is null");
		}

		@Override
		public void visit(final PositiveEntailmentTest<OWLOntology> testcase)
		{
			_results = new TestRunResult[2];
			_results[0] = runConsistencyTest(testcase);
			if (null == _results[0])
				throw new OpenError("Result[0] is null");
			_results[1] = runEntailmentTest(testcase);
			if (null == _results[1])
				throw new OpenError("Result[1] is null");
		}
	}

	protected interface TestAsRunnable extends Runnable
	{
		public TestRunResult getErrorResult(Throwable th);

		public TestRunResult getResult() throws Throwable;

		public TestRunResult getTimeoutResult();
	}

	protected class XConsistencyTest extends AbstractTestAsRunnable<PremisedTest<OWLOntology>>
	{

		public XConsistencyTest(final PremisedTest<OWLOntology> testcase, final RunTestType type)
		{
			super(testcase, type);

			if (!EnumSet.of(CONSISTENCY, INCONSISTENCY).contains(type))
				throw new IllegalArgumentException();
		}

		@Override
		public void run()
		{
			SerializationFormat fmt = null;
			for (final SerializationFormat f : formatList)
				if (_testcase.getPremiseFormats().contains(f))
				{
					fmt = f;
					break;
				}
			if (fmt == null)
			{
				_result = new ReasoningRun(_testcase, INCOMPLETE, _type, OwlApiAbstractRunner.this, "No acceptable serialization formats found for premise ontology.");
				return;
			}

			OWLOntology o;
			try
			{
				final long parseStart = System.currentTimeMillis();
				o = _testcase.parsePremiseOntology(fmt);
				final long parseEnd = System.currentTimeMillis();
				System.err.println(_testcase.getIdentifier() + " parse time " + ((parseEnd - parseStart) / 1000));
			}
			catch (final OntologyParseException e)
			{
				_result = new ReasoningRun(_testcase, INCOMPLETE, _type, OwlApiAbstractRunner.this, "Exception parsing premise ontology: " + e.getLocalizedMessage(), e);
				return;
			}

			try
			{
				final boolean consistent = isConsistent(o);
				if (consistent)
					_result = new ReasoningRun(_testcase, CONSISTENCY.equals(_type) ? PASSING : FAILING, _type, OwlApiAbstractRunner.this);
				else
					_result = new ReasoningRun(_testcase, INCONSISTENCY.equals(_type) ? PASSING : FAILING, _type, OwlApiAbstractRunner.this);
			}
			catch (final Throwable th)
			{
				th.printStackTrace();
				_result = new ReasoningRun(_testcase, INCOMPLETE, _type, OwlApiAbstractRunner.this, "Caught throwable: " + th.getLocalizedMessage(), th);
			}
		}

	}

	protected class XEntailmentTest extends AbstractTestAsRunnable<EntailmentTest<OWLOntology>>
	{

		public XEntailmentTest(final EntailmentTest<OWLOntology> testcase, final RunTestType type)
		{
			super(testcase, type);

			if (!EnumSet.of(POSITIVE_ENTAILMENT, NEGATIVE_ENTAILMENT).contains(type))
				throw new IllegalArgumentException();
		}

		@Override
		public void run()
		{
			SerializationFormat pFmt = null, cFmt = null;
			for (final SerializationFormat f : formatList)
				if (_testcase.getPremiseFormats().contains(f))
				{
					pFmt = f;
					break;
				}
			if (pFmt == null)
			{
				_result = new ReasoningRun(_testcase, INCOMPLETE, _type, OwlApiAbstractRunner.this, "No acceptable serialization formats found for premise ontology.");
				return;
			}
			for (final SerializationFormat f : formatList)
				if (_testcase.getConclusionFormats().contains(f))
				{
					cFmt = f;
					break;
				}
			if (cFmt == null)
			{
				_result = new ReasoningRun(_testcase, INCOMPLETE, _type, OwlApiAbstractRunner.this, "No acceptable serialization formats found for conclusion ontology.");
				return;
			}

			OWLOntology premise, conclusion;
			try
			{
				final long parseStart = System.currentTimeMillis();
				premise = _testcase.parsePremiseOntology(pFmt);
				conclusion = _testcase.parseConclusionOntology(cFmt);
				final long parseEnd = System.currentTimeMillis();
				System.err.println(_testcase.getIdentifier() + " parse time " + ((parseEnd - parseStart) / 1000));
			}
			catch (final OntologyParseException e)
			{
				_result = new ReasoningRun(_testcase, INCOMPLETE, _type, OwlApiAbstractRunner.this, "Exception parsing input ontology: " + e.getLocalizedMessage(), e);
				return;
			}

			try
			{
				final boolean entailed = isEntailed(premise, conclusion);
				if (entailed)
					_result = new ReasoningRun(_testcase, POSITIVE_ENTAILMENT.equals(_type) ? PASSING : FAILING, _type, OwlApiAbstractRunner.this);
				else
					_result = new ReasoningRun(_testcase, NEGATIVE_ENTAILMENT.equals(_type) ? PASSING : FAILING, _type, OwlApiAbstractRunner.this);
			}
			catch (final Throwable th)
			{
				System.gc();
				th.printStackTrace();
				_result = new ReasoningRun(_testcase, INCOMPLETE, _type, OwlApiAbstractRunner.this, "Caught throwable: " + th.getLocalizedMessage(), th);
			}
		}
	}

	public OwlApiAbstractRunner()
	{
		_runner = new Runner();
	}

	protected abstract boolean isConsistent(OWLOntology o);

	protected abstract boolean isEntailed(OWLOntology premise, OWLOntology conclusion);

	@SuppressWarnings("deprecation")
	protected TestRunResult run(final TestAsRunnable runnable)
	{
		final Thread t = new Thread(runnable);
		t.start();
		try
		{
			t.join(_timeout);
		}
		catch (final InterruptedException e)
		{
			return runnable.getErrorResult(e);
		}
		if (t.isAlive())
			try
			{
				t.stop();
				return runnable.getTimeoutResult();
			}
			catch (final OutOfMemoryError oome)
			{
				_logger.log(Level.WARNING, "Out of memory allocating timeout response. Retrying.", oome);
				System.gc();
				return runnable.getTimeoutResult();
			}
		else
			try
			{
				return runnable.getResult();
			}
			catch (final Throwable th)
			{
				return runnable.getErrorResult(th);
			}
	}

	@Override
	public Collection<TestRunResult> run(final TestCase<OWLOntology> testcase, final long timeout)
	{
		_timeout = timeout;
		if (testcase instanceof OwlApiCase)
			return _runner.getResults((OwlApiCase) testcase);
		else
			throw new IllegalArgumentException();
	}

	protected TestRunResult runConsistencyTest(final PremisedTest<OWLOntology> testcase)
	{
		return run(new XConsistencyTest(testcase, CONSISTENCY));
	}

	protected TestRunResult runEntailmentTest(final NegativeEntailmentTest<OWLOntology> testcase)
	{
		return run(new XEntailmentTest(testcase, NEGATIVE_ENTAILMENT));
	}

	protected TestRunResult runEntailmentTest(final PositiveEntailmentTest<OWLOntology> testcase)
	{
		return run(new XEntailmentTest(testcase, POSITIVE_ENTAILMENT));
	}

	protected TestRunResult runInconsistencyTest(final InconsistencyTest<OWLOntology> testcase)
	{
		return run(new XConsistencyTest(testcase, INCONSISTENCY));
	}
}
