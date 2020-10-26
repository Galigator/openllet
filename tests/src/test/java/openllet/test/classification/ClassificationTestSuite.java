// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test.classification;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import junit.framework.JUnit4TestAdapter;

/**
 * @author Evren Sirin
 */

@RunWith(Suite.class)
@SuiteClasses({ SimpleClassificationTests.class, JenaClassificationTest.class, OWLAPIClassificationTest.class })
public class ClassificationTestSuite
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter(ClassificationTestSuite.class);
	}
}
