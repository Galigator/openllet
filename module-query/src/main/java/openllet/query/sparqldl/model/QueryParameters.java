// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import openllet.aterm.ATermAppl;

/**
 * <p>
 * Title: QueryParameter
 * </p>
 * <p>
 * Description: Class for query parameterization
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
public class QueryParameters
{

	private final Map<ATermAppl, ATermAppl> _parameters;

	public QueryParameters()
	{
		_parameters = new HashMap<>();
	}

	public void add(final ATermAppl key, final ATermAppl value)
	{
		_parameters.put(key, value);
	}

	public Set<Map.Entry<ATermAppl, ATermAppl>> entrySet()
	{
		return _parameters.entrySet();
	}

	public boolean cointains(final ATermAppl key)
	{
		return _parameters.containsKey(key);
	}

	public ATermAppl get(final ATermAppl key)
	{
		return _parameters.get(key);
	}

	@Override
	public String toString()
	{
		return _parameters.toString();
	}
}
