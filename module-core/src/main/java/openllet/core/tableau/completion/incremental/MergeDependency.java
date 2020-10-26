// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.completion.incremental;

import openllet.aterm.ATermAppl;

/**
 * A dependency for a _node merge
 *
 * @author Christian Halaschek-Wiener
 */
public class MergeDependency implements Dependency
{

	/**
	 * The _individual that _ind is merged to
	 */
	private final ATermAppl _mergedIntoInd;

	/**
	 * The _individual that is merged into _mergedIntoInd
	 */
	private final ATermAppl _ind;

	/**
	 * Constructor
	 *
	 * @param ind
	 * @param mergedIntoInd
	 */
	public MergeDependency(final ATermAppl ind, final ATermAppl mergedIntoInd)
	{
		_mergedIntoInd = mergedIntoInd;
		_ind = ind;
	}

	/**
	 * @return the _individual that is merged into the other
	 */
	public ATermAppl getInd()
	{
		return _ind;
	}

	/**
	 * @return the _individual that has _ind merged into it
	 */
	public ATermAppl getmergedIntoInd()
	{
		return _mergedIntoInd;
	}

	/**
	 * ToString method
	 */
	@Override
	public String toString()
	{
		return "Merge [" + _ind + "]  into [" + _mergedIntoInd + "]";
	}

	/**
	 * Equals method
	 */
	@Override
	public boolean equals(final Object other)
	{
		if (other instanceof MergeDependency)
			return _ind.equals(((MergeDependency) other)._ind) && _mergedIntoInd.equals(((MergeDependency) other)._mergedIntoInd);
		else
			return false;
	}

	/**
	 * Hashcode method TODO: this may not be sufficient
	 */
	@Override
	public int hashCode()
	{
		return _ind.hashCode() + _mergedIntoInd.hashCode();
	}

}
