// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules.builtins;

import java.util.Arrays;
import openllet.aterm.ATermAppl;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.Literal;
import openllet.core.utils.TermFactory;

/**
 * <p>
 * Title: Boolean Operators
 * </p>
 * <p>
 * Description: Implementation for the single SWRL boolean op.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Mike Smith
 */
public class BooleanOperators
{

	private static class BooleanNot implements GeneralFunction
	{

		@Override
		public boolean apply(final ABox abox, final Literal[] args)
		{
			if (args.length != 2) return false;

			if (args[0] == null)
			{
				if (args[1] != null && args[1].getValue() instanceof Boolean)
				{
					args[0] = abox.addLiteral(((Boolean) args[1].getValue()) ? FALSE_TERM : TRUE_TERM);
					return true;
				}
			}
			else if (args[1] == null)
			{
				if (args[0].getValue() instanceof Boolean)
				{
					args[1] = abox.addLiteral(((Boolean) args[0].getValue()) ? FALSE_TERM : TRUE_TERM);
					return true;
				}
			}
			else if (args[0].getValue() instanceof Boolean && args[1].getValue() instanceof Boolean) return !args[0].equals(args[1]);

			return false;
		}

		@Override
		public boolean isApplicable(final boolean[] boundPositions)
		{
			if (boundPositions.length != 2) return false;

			return !Arrays.equals(boundPositions, new boolean[] { false, false });
		}
	}

	public final static GeneralFunction	booleanNot;

	private static final ATermAppl		TRUE_TERM, FALSE_TERM;

	static
	{
		TRUE_TERM = TermFactory.literal(true);
		FALSE_TERM = TermFactory.literal(false);

		booleanNot = new BooleanNot();
	}
}
