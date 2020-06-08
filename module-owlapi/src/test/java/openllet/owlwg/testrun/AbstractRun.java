package openllet.owlwg.testrun;

import openllet.owlwg.runner.TestRunner;
import openllet.owlwg.testcase.TestCase;

/**
 * <p>
 * Title: Abstract Run
 * </p>
 * <p>
 * Description: Base implementation used by other {@link TestRunResult} implementations
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
public abstract class AbstractRun implements TestRunResult
{

	private final Throwable		_cause;
	private final String		_details;
	private final RunResultType	_resultType;
	private final TestRunner<?>	_runner;
	private final TestCase<?>	_testcase;
	private final RunTestType	_testType;

	public AbstractRun(final TestCase<?> testcase, final RunResultType resultType, final RunTestType testType, final TestRunner<?> runner, final String details, final Throwable cause)
	{
		if (testcase == null) throw new NullPointerException();
		if (resultType == null) throw new NullPointerException();
		if (testType == null) throw new NullPointerException();
		if (runner == null) throw new NullPointerException();

		_testcase = testcase;
		_resultType = resultType;
		_testType = testType;
		_runner = runner;
		_details = details;
		_cause = cause;
	}

	@Override
	public Throwable getCause()
	{
		return _cause;
	}

	@Override
	public String getDetails()
	{
		return _details;
	}

	@Override
	public RunResultType getResultType()
	{
		return _resultType;
	}

	@Override
	public TestCase<?> getTestCase()
	{
		return _testcase;
	}

	@Override
	public TestRunner<?> getTestRunner()
	{
		return _runner;
	}

	@Override
	public RunTestType getTestType()
	{
		return _testType;
	}

}
