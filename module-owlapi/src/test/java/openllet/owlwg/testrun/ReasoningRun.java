package openllet.owlwg.testrun;

import static openllet.owlwg.testrun.RunTestType.CONSISTENCY;
import static openllet.owlwg.testrun.RunTestType.INCONSISTENCY;
import static openllet.owlwg.testrun.RunTestType.NEGATIVE_ENTAILMENT;
import static openllet.owlwg.testrun.RunTestType.POSITIVE_ENTAILMENT;

import java.util.EnumSet;
import openllet.owlwg.runner.TestRunner;
import openllet.owlwg.testcase.TestCase;

/**
 * <p>
 * Title: Reasoning Run
 * </p>
 * <p>
 * Description:
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
public class ReasoningRun extends AbstractRun
{

	public ReasoningRun(TestCase<?> testcase, RunResultType resultType, RunTestType testType, TestRunner<?> runner)
	{
		this(testcase, resultType, testType, runner, null, null);
	}

	public ReasoningRun(TestCase<?> testcase, RunResultType resultType, RunTestType testType, TestRunner<?> runner, String details)
	{
		this(testcase, resultType, testType, runner, details, null);
	}

	public ReasoningRun(TestCase<?> testcase, RunResultType resultType, RunTestType testType, TestRunner<?> runner, String details, Throwable cause)
	{
		super(testcase, resultType, testType, runner, details, cause);
		if (!EnumSet.of(CONSISTENCY, INCONSISTENCY, NEGATIVE_ENTAILMENT, POSITIVE_ENTAILMENT).contains(testType))
			throw new IllegalArgumentException();
	}

	@Override
	public void accept(TestRunResultVisitor visitor)
	{
		visitor.visit(this);
	}

	@Override
	public String toString()
	{
		final String details = getDetails();
		if (details == null)
			return String.format("Result( %s , %s, %s)", getTestCase(), getResultType(), getTestType());
		else
			return String.format("Result( %s , %s, %s (%s))", getTestCase(), getResultType(), getTestType(), details);
	}
}
