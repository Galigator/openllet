// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test.rules;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import openllet.core.utils.AlphaNumericComparator;
import openllet.core.utils.Comparators;
import openllet.test.PelletTestSuite;
import openllet.test.WebOntTest;
import openllet.test.WebOntTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * <p>
 * Title: SWRL Test Suite
 * </p>
 * <p>
 * Description: Regression tests collected for the rule engine in Pellet.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Ron Alford
 */
@RunWith(Parameterized.class)
public class SWRLTestSuite
{
	public static final String	base	= PelletTestSuite.base + "swrl-test/";

	private static List<File>	IGNORE	= Arrays.asList(new File(base + "equalities/Manifest002.rdf"));

	@Parameters(name = "{0}")
	public static List<Object[]> getParameters()
	{
		final List<Object[]> parameters = new ArrayList<>();

		final WebOntTest test = new WebOntTest();
		test.setAvoidFailTests(true);
		test.setBase("http://owldl.com/ontologies/swrl/tests/", "file:" + base);
		test.setShowStats(WebOntTest.NO_STATS);

		final File testDir = new File(base);
		final File[] dirs = testDir.listFiles();

		Arrays.sort(dirs, Comparators.stringComparator);

		System.out.println(Arrays.toString(dirs));
		for (final File dir : dirs)
		{
			System.out.println(dir.getAbsolutePath());
			if (dir.isFile()) continue;

			final File[] files = dir.listFiles((FileFilter) file -> file.getName().indexOf("Manifest") != -1);
			Arrays.sort(files, AlphaNumericComparator.CASE_INSENSITIVE);

			for (final File file : files)
				if (!IGNORE.contains(file)) parameters.add(new Object[] { new WebOntTestCase(test, file, "swrl-" + dir.getName() + "-" + file.getName()) });
		}

		return parameters;
	}

	private final WebOntTestCase _test;

	public SWRLTestSuite(final WebOntTestCase test)
	{
		_test = test;
	}

	@Test
	public void run() throws IOException
	{
		_test.runTest();
	}
}
