// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.explanation.test;

import java.util.Properties;
import junit.framework.JUnit4TestAdapter;
import openllet.core.OpenlletOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * <p>
 * Title: ModularityTestSuite
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Markus Stocker
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({ OWLAPIExplanationTest.class, JenaExplanationTest.class, MiscExplanationTests.class, ExplanationRendererTest.class })
public class ExplanationTestSuite
{

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter(ExplanationTestSuite.class);
	}

	private static Properties savedOptions;

	@BeforeClass
	public static void saveOptions()
	{
		final Properties newOptions = new Properties();
		newOptions.setProperty("USE_TRACING", "true");

		savedOptions = OpenlletOptions.setOptions(newOptions);
	}

	@AfterClass
	public static void restoreOptions()
	{
		OpenlletOptions.setOptions(savedOptions);
	}
}
