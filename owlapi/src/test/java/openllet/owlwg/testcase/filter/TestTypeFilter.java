package openllet.owlwg.testcase.filter;

import openllet.owlwg.testcase.ConsistencyTest;
import openllet.owlwg.testcase.InconsistencyTest;
import openllet.owlwg.testcase.NegativeEntailmentTest;
import openllet.owlwg.testcase.PositiveEntailmentTest;
import openllet.owlwg.testcase.TestCase;

/**
 * <p>
 * Title: Test Type Filter Condition
 * </p>
 * <p>
 * Description: Filter _condition to match tests with a particular type (e.g., consistency, negative entailment).
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
public class TestTypeFilter implements FilterCondition
{

	public static final TestTypeFilter CONSISTENCY, INCONSISTENCY, NEGATIVE_ENTAILMENT, POSITIVE_ENTAILMENT;

	static
	{
		CONSISTENCY = new TestTypeFilter(ConsistencyTest.class);
		INCONSISTENCY = new TestTypeFilter(InconsistencyTest.class);
		NEGATIVE_ENTAILMENT = new TestTypeFilter(NegativeEntailmentTest.class);
		POSITIVE_ENTAILMENT = new TestTypeFilter(PositiveEntailmentTest.class);
	}

	@SuppressWarnings("rawtypes")
	private final Class<? extends TestCase> _cls;

	@SuppressWarnings("rawtypes")
	public TestTypeFilter(Class<? extends TestCase> cls)
	{
		this._cls = cls;
	}

	@Override
	public boolean accepts(final TestCase<?> testcase)
	{
		return _cls.isAssignableFrom(testcase.getClass());
	}

}
