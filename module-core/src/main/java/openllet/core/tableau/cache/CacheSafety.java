// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.cache;

import openllet.aterm.ATermAppl;
import openllet.core.boxes.abox.Individual;
import openllet.core.expressivity.Expressivity;

/**
 * A class to check if a previsouly cached satisfiability result is safe to reuse for a specific _node in the completion graph.
 *
 * @author Evren Sirin
 */
public interface CacheSafety
{
	/**
	 * Check if this safety checker can be used with the specified expressivity.
	 *
	 * @param expressivity Expressivity of the KB where safety check will be performed
	 * @return <code>true</code> if this safety checker can be used with the specified expressivity
	 */
	boolean canSupport(Expressivity expressivity);

	/**
	 * Returns if a previously cached satisfiability result is safe to reuse for a given concept and node in the completion graph.
	 *
	 * @param c The concept for which we want to reuse previously cached satisfiability result
	 * @param ind The node in the completion graph represented by the concept
	 * @return <code>true</code> if a previously cached satisfiability result is safe to reuse
	 */
	boolean isSafe(ATermAppl c, Individual ind);
}
