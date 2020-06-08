// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.blocking;

import openllet.aterm.ATermAppl;
import openllet.aterm.ATermInt;
import openllet.core.boxes.abox.Node;
import openllet.core.boxes.rbox.Role;
import openllet.core.utils.ATermUtils;

/**
 * @author Evren Sirin
 */
public class Block4Min implements BlockingCondition
{
	@Override
	public boolean isBlocked(final BlockingContext cxt)
	{
		for (final ATermAppl min : cxt._blocker.getTypes(Node.MIN))
			if (!block4(cxt, min)) return false;

		for (final ATermAppl normSome : cxt._blocker.getTypes(Node.SOME))
		{
			final ATermAppl some = (ATermAppl) normSome.getArgument(0);
			if (!block4(cxt, some)) return false;
		}

		return true;
	}

	private static boolean block4(final BlockingContext cxt, final ATermAppl term)
	{
		final Role t = cxt._blocked.getABox().getRole(term.getArgument(0));
		final int m;
		final ATermAppl c;

		if (ATermUtils.isMin(term))
		{
			c = (ATermAppl) term.getArgument(2);
			m = ((ATermInt) term.getArgument(1)).getInt();
		}
		else
		{
			c = ATermUtils.negate((ATermAppl) term.getArgument(1));
			m = 1;
		}

		if (t.isDatatypeRole()) return true;

		if (cxt.isRSuccessor(t.getInverse()) && cxt._blocked.getParent().hasType(c)) return true;

		return cxt._blocker.getRSuccessors(t, c).size() >= m;
	}
}
