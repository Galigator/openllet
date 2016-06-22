// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.cache;

import openllet.core.OpenlletOptions;
import openllet.core.expressivity.Expressivity;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public class CacheSafetyFactory
{
	public static CacheSafety createCacheSafety(final Expressivity expr)
	{
		final CacheSafety cacheSafety = expr.hasInverse() ? expr.hasNominal() ? CacheSafetyNeverSafe.getInstance() : OpenlletOptions.USE_INVERSE_CACHING ? new CacheSafetyDynamic(expr) : CacheSafetyNeverSafe.getInstance() : expr.hasNominal() ? CacheSafetyNeverSafe.getInstance() : CacheSafetyAlwaysSafe.getInstance();

		return cacheSafety;
	}
}
