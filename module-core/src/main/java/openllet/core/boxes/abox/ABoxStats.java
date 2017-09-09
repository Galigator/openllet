// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.boxes.abox;

/**
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public class ABoxStats
{
	/**
	 * Total number of ABox consistency checks
	 */
	public volatile long _consistencyCount = 0;

	/**
	 * Total number of satisfiability tests performed
	 */
	public volatile long _satisfiabilityCount = 0;

	public volatile short _treeDepth = 0;

	public volatile int _backjumps = 0;
	public volatile int _backtracks = 0;
	public volatile int _globalRestores = 0;
	public volatile int _localRestores = 0;
	public volatile int _branch = 0;

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(" Branches " + _branch);
		sb.append(" Tree depth: " + _treeDepth);
		sb.append(" Restores " + _globalRestores + " global " + _localRestores + " local");
		sb.append(" Backtracks " + _backtracks);
		sb.append(" Avg backjump " + _backjumps / (double) _backtracks);
		return sb.toString();
	}
}
