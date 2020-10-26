// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.model;

import java.util.List;

import openllet.aterm.ATermAppl;

/**
 * <p>
 * Title: Query Result Interface
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
public interface QueryResult extends Iterable<ResultBinding>
{

	/**
	 * Adds a new binding to the query result.
	 *
	 * @param binding to be added
	 */
	void add(final ResultBinding binding);

	/**
	 * Returns result variables.
	 *
	 * @return variables that appear in the result
	 */
	List<ATermAppl> getResultVars();

	boolean isDistinct();

	/**
	 * Tests whether the result is empty or not.
	 *
	 * @return true if the result contains not bindings
	 */
	boolean isEmpty();

	/**
	 * Returns number of bindings in the result.
	 *
	 * @return number of bindings
	 */
	int size();
}
