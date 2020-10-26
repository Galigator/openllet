package openllet.test.transtree;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import junit.framework.JUnit4TestAdapter;

/**
 * @author Blazej Bulka
 */
@RunWith(Suite.class)
@SuiteClasses({ TransTreeTest.class })
public class TransTreeTestSuite
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter(TransTreeTestSuite.class);
	}
}
