// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test.query;

import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.Properties;
import openllet.core.OpenlletOptions;
import org.apache.jena.rdf.model.Resource;

/**
 * <p>
 * Title: Engine for processing DAWG test manifests
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Petr Kremen
 */
public class SparqlDawgTestCase
{
	private final Properties		_pelletOptions;

	private final Resource			_resource;

	private final ManifestEngine	_test;

	private final SparqlDawgTester	_tester;

	public SparqlDawgTestCase(final SparqlDawgTester tester, final ManifestEngine test, final Resource resource, final Properties pelletOptions)
	{
		_tester = tester;
		_test = test;
		_resource = resource;
		_pelletOptions = pelletOptions;
	}

	/**
	 * {@inheritDoc}
	 */
	public void runTest()
	{
		final Properties oldOptions = OpenlletOptions.setOptions(_pelletOptions);
		try
		{
			assertTrue(EnumSet.of(ResultEnum.PASS, ResultEnum.SKIP).contains(_test.doSingleTest(_tester, _resource).getResult()));
		}
		finally
		{
			OpenlletOptions.setOptions(oldOptions);
		}
	}
}
