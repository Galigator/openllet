// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.completion.incremental;

import openllet.aterm.ATermAppl;
import openllet.core.tableau.branch.Branch;

/**
 * Dependency structure for when a branch is added.
 *
 * @author Christian Halaschek-Wiener
 */
public class AddBranchDependency extends BranchDependency
{
	/**
	 * The actual branch
	 */
	private final Branch _branch;

	/**
	 * @param assertion
	 * @param branch
	 */
	public AddBranchDependency(final ATermAppl assertion, final Branch branch)
	{
		super(assertion);
		_branch = branch;
	}

	/**
	 * @return branch that is add
	 */
	public Branch getBranch()
	{
		return _branch;
	}

	@Override
	public String toString()
	{
		return "Branch  - [" + _branch + "]";
	}

	@Override
	public boolean equals(final Object other)
	{
		if (other instanceof AddBranchDependency)
			return _branch.getBranchIndexInABox() == ((AddBranchDependency) other)._branch.getBranchIndexInABox() && _assertion.equals(((AddBranchDependency) other)._assertion);
		else
			return false;
	}

	@Override
	public int hashCode()
	{
		return _branch.getBranchIndexInABox() + _assertion.hashCode();
	}
}
