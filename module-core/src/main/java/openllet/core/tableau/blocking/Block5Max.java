// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.blocking;

import java.util.function.BiPredicate;

import openllet.aterm.ATermAppl;
import openllet.core.boxes.abox.Node;
import openllet.core.boxes.rbox.Role;
import openllet.core.utils.ATermUtils;

/**
 * @author Evren Sirin
 */
public class Block5Max implements BlockingCondition
{
	public static BiPredicate<BlockingContext, ATermAppl> _maxBlock = (ctx, normMax) ->
	{
		final ATermAppl max = (ATermAppl) normMax.getArgument(0);
		final Role role = ctx._blocked.getABox().getRole(max.getArgument(0));
		final ATermAppl c = (ATermAppl) max.getArgument(2);

		return role.isDatatypeRole()//
				|| !ctx.isRSuccessor(role.getInverse())//
				|| ctx._blocked.getParent().hasType(ATermUtils.negate(c));
	};

	@Override
	public boolean isBlocked(final BlockingContext cxt)
	{
		for (final ATermAppl normMax : cxt._blocker.getTypes(Node.MAX))
			if (!_maxBlock.test(cxt, normMax))
				return false;

		return true;
	}
}
