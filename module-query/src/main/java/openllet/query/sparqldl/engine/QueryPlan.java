// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.engine;

import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.QueryAtom;
import openllet.query.sparqldl.model.ResultBinding;

/**
 * <p>
 * Title: Query Plan abstraction.
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
public abstract class QueryPlan
{
	protected Query _query;

	public QueryPlan(final Query query)
	{
		_query = query;
	}

	public Query getQuery()
	{
		return _query;
	}

	/**
	 * @param binding
	 * @return next atom to be executed w.r. to the current binding.
	 */
	public abstract QueryAtom next(final ResultBinding binding);

	/**
	 * Goes one level back to the last atom.
	 */
	public abstract void back();

	/**
	 * Checks whether there is another atom to execute.
	 *
	 * @return true if there is another atom to execute.
	 */
	public abstract boolean hasNext();

	/**
	 * Resets the query planner.
	 */
	public abstract void reset();
}
