// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.blocking;

import java.util.function.BiPredicate;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermInt;
import openllet.core.boxes.abox.Node;
import openllet.core.boxes.rbox.Role;

/**
 * @author Evren Sirin
 */
public class Block3Max implements BlockingCondition
{
	public static BiPredicate<BlockingContext, ATermAppl> _typeAndRSuccessorN = (ctx, normMax) ->
	{
		final ATermAppl max = (ATermAppl) normMax.getArgument(0);
		final Role role = ctx._blocked.getABox().getRole(max.getArgument(0));
		final ATermAppl c = (ATermAppl) max.getArgument(2);
		final int n = ((ATermInt) max.getArgument(1)).getInt() - 1;

		return ctx._blocked.getParent().hasType(c)//
				&& ctx._blocker.getRSuccessors(role, c).size() < n;
	};

	@Override
	public boolean isBlocked(final BlockingContext cxt)
	{
		for (final ATermAppl normMax : cxt._blocker.getTypes(Node.MAX))
			if (!(Block5Max._maxBlock.test(cxt, normMax)//
					|| _typeAndRSuccessorN.test(cxt, normMax)))
				return false;

		return true;
	}
}
