// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test.query;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import junit.framework.JUnit4TestAdapter;
import openllet.sparqlowl.parser.test.ParserTest;

/**
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
@RunWith(Suite.class)
@SuiteClasses({ //
		TestBooleanQueries.class, //
		TestSingleSPARQLDLQueries.class, //
		ParserTest.class, //
		TestParameterizedQuery.class, //
		TestGroundBooleanQueryComponents.class, //
		TestNegatedQueries.class, //
		TestUnionQueries.class, //
		TestMiscQueries.class, //
		TestMiscSPARQL.class, //
		SparqlDawgTestSuite.class, //
		TestQuerySubsumption.class//
})
public class QueryTestSuite
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter(QueryTestSuite.class);
	}
}
