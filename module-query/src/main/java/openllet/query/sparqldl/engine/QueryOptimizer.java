// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.engine;

import java.util.logging.Logger;

import openllet.core.OpenlletOptions;
import openllet.query.sparqldl.model.Query;
import openllet.shared.tools.Log;

/**
 * <p>
 * Title: Optimizer of the query. Provides query atoms for the engine in particular ordering.
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
public class QueryOptimizer
{

	private static final Logger _logger = Log.getLogger(QueryOptimizer.class);

	public QueryPlan getExecutionPlan(final Query query)
	{
		if (OpenlletOptions.SAMPLING_RATIO == 0)
			return new NoReorderingQueryPlan(query);

		if (query.getAtoms().size() > OpenlletOptions.STATIC_REORDERING_LIMIT)
		{
			_logger.fine("Using incremental query plan.");
			return new IncrementalQueryPlan(query);
		}
		else
		{
			_logger.fine("Using full query plan.");
			return new CostBasedQueryPlanNew(query);
		}

	}
}
