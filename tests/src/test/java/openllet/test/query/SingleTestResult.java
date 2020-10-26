// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test.query;

import java.net.URI;

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
public class SingleTestResult
{

	private final URI _uri;

	private final ResultEnum _result;

	private final long _time;

	public SingleTestResult(final URI uri, final ResultEnum result, final long time)
	{
		super();
		_uri = uri;
		_result = result;
		_time = time;
	}

	public URI getUri()
	{
		return _uri;
	}

	public ResultEnum getResult()
	{
		return _result;
	}

	public long getTime()
	{
		return _time;
	}
}
