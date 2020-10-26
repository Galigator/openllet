package openllet.profiler.statistical;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import junit.framework.JUnit4TestAdapter;

/**
 * @author Pedro Oliveira <pedro@clarkparsia.com>
 */
@RunWith(Suite.class)
@SuiteClasses({ ReleasePerformanceTest.class })
public class ReleasePerformanceTestSuite
{

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter(ReleasePerformanceTestSuite.class);
	}

	public static void main(final String args[])
	{
		junit.textui.TestRunner.run(suite());
	}

}
