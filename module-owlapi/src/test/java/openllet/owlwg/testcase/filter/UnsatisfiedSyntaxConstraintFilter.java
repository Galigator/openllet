package openllet.owlwg.testcase.filter;

import openllet.owlwg.testcase.SyntaxConstraint;
import openllet.owlwg.testcase.TestCase;

/**
 * <p>
 * Title: Unsatisfied Syntax Constraint Filter Condition
 * </p>
 * <p>
 * Description: Filter _condition to match tests for which a particular syntax _constraint is not satisfied.
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
public class UnsatisfiedSyntaxConstraintFilter implements FilterCondition
{

	final private SyntaxConstraint _constraint;

	/**
	 * @param constraint to check
	 * @throws NullPointerException if <code>_constraint == null</code>
	 */
	public UnsatisfiedSyntaxConstraintFilter(final SyntaxConstraint constraint)
	{
		if (constraint == null)
			throw new NullPointerException();

		_constraint = constraint;
	}

	@Override
	public boolean accepts(final TestCase<?> testcase)
	{
		return testcase.getUnsatisfiedConstraints().contains(_constraint);
	}

	@Override
	public String toString()
	{
		return "!" + _constraint.toString();
	}

}
