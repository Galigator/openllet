// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.branch;

import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.boxes.abox.ABoxImpl;
import openllet.core.boxes.abox.Node;
import openllet.core.tableau.completion.CompletionStrategy;
import openllet.core.utils.ATermUtils;

public class ChooseBranch extends DisjunctionBranch
{
	public ChooseBranch(final ABoxImpl abox, final CompletionStrategy completion, final Node node, final ATermAppl c, final DependencySet ds)
	{
		super(abox, completion, node, c, ds, new ATermAppl[] { ATermUtils.negate(c), c });
	}

	@Override
	protected String getDebugMsg()
	{
		return "CHOS: Branch (" + getBranch() + ") try (" + (getTryNext() + 1) + "/" + getTryCount() + ") " + _node.getName() + " " + getDisjunct(getTryNext());
	}
}
