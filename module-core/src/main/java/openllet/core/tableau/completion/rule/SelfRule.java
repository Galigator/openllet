// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.completion.rule;

import java.util.ArrayList;
import openllet.aterm.ATermAppl;
import openllet.core.OpenlletOptions;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.Node;
import openllet.core.boxes.rbox.Role;
import openllet.core.tableau.completion.CompletionStrategy;
import openllet.core.tableau.completion.queue.NodeSelector;
import openllet.core.utils.ATermUtils;

/**
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public class SelfRule extends AbstractTableauRule
{

	public SelfRule(final CompletionStrategy strategy)
	{
		super(strategy, NodeSelector.ATOM, BlockingType.NONE);
	}

	@Override
	public final void apply(final Individual node)
	{
		for (final ATermAppl c : new ArrayList<>(node.getTypes(Node.ATOM))) // 'applyAllValues' can change the underlying types of individual; so we make a copy to iterate
		{
			if (!OpenlletOptions.MAINTAIN_COMPLETION_QUEUE && null == node.getDepends(c))
				continue;

			if (ATermUtils.isSelf(c))
			{
				final ATermAppl predicate = (ATermAppl) c.getArgument(0);
				final Role role = _strategy.getABox().getRole(predicate);
				_logger.fine(() -> "SELF: " + node + "\trole:" + role + "\tdepends:" + node.getDepends(c) + "\tRSuccessor:" + node.hasRSuccessor(role, node));
				_strategy.addEdge(node, role, node, node.getDepends(c));

				if (_strategy.getABox().isClosed())
					return;
			}
		}
	}
}
