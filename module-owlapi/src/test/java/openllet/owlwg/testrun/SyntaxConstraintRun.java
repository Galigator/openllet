package openllet.owlwg.testrun;

import openllet.owlwg.runner.TestRunner;
import openllet.owlwg.testcase.SyntaxConstraint;
import openllet.owlwg.testcase.TestCase;

/**
 * <p>
 * Title: Syntax Constraint Run
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
public class SyntaxConstraintRun extends AbstractRun
{

	final private SyntaxConstraint _constraint;

	public SyntaxConstraintRun(final TestCase<?> testcase, final RunResultType type, final SyntaxConstraint constraint, final TestRunner<?> runner)
	{
		this(testcase, type, constraint, runner, null, null);
	}

	public SyntaxConstraintRun(final TestCase<?> testcase, final RunResultType type, final SyntaxConstraint constraint, final TestRunner<?> runner, final String details)
	{
		this(testcase, type, constraint, runner, details, null);
	}

	public SyntaxConstraintRun(final TestCase<?> testcase, final RunResultType type, final SyntaxConstraint constraint, final TestRunner<?> runner, final String details, final Throwable cause)
	{
		super(testcase, type, RunTestType.SYNTAX_CONSTRAINT, runner, details, cause);
		if (constraint == null) throw new NullPointerException();
		_constraint = constraint;
	}

	@Override
	public void accept(final TestRunResultVisitor visitor)
	{
		visitor.visit(this);
	}

	public SyntaxConstraint getConstraint()
	{
		return _constraint;
	}
}
