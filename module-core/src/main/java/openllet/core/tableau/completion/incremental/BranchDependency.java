// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.completion.incremental;

import openllet.aterm.ATermAppl;

/**
 * Abstract class for a branch dependency
 *
 * @author Christian Halaschek-Wiener
 */
public abstract class BranchDependency implements Dependency
{
	/**
	 * The assertion which this branch is indexed on
	 */
	protected final ATermAppl _assertion;

	public BranchDependency(final ATermAppl assertion)
	{
		_assertion = assertion;
	}

	public ATermAppl getAssertion()
	{
		return _assertion;
	}
}
