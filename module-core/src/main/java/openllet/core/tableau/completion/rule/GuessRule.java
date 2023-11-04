// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.completion.rule;

import java.util.List;

import openllet.aterm.ATermAppl;
import openllet.aterm.ATermInt;
import openllet.core.DependencySet;
import openllet.core.boxes.abox.Edge;
import openllet.core.boxes.abox.EdgeList;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.Node;
import openllet.core.boxes.rbox.Role;
import openllet.core.tableau.branch.Branch;
import openllet.core.tableau.branch.GuessBranch;
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
public class GuessRule extends AbstractTableauRule
{
	public GuessRule(final CompletionStrategy strategy)
	{
		super(strategy, NodeSelector.GUESS, BlockingType.NONE);
	}

	@Override
	public void apply(final Individual x)
	{
		if (x.isBlockable())
			return;

		final List<ATermAppl> types = x.getTypes(Node.MAX);
		final int size = types.size();
		for (int j = 0; j < size; j++)
		{
			final ATermAppl mc = types.get(j);

			applyGuessingRule(x, mc);

			if (_strategy.getABox().isClosed())
				return;
		}
	}

	private void applyGuessingRule(final Individual x, final ATermAppl mc)
	{
		// max(r, n) is in normalized form not(min(p, n + 1))
		final ATermAppl max = (ATermAppl) mc.getArgument(0);

		final Role r = _strategy.getABox().getRole(max.getArgument(0));
		final int n = ((ATermInt) max.getArgument(1)).getInt() - 1;
		final ATermAppl c = (ATermAppl) max.getArgument(2);

		// obviously if r is a datatype role then there can be no r-predecessor
		// and we cannot apply the rule
		if (r.isDatatypeRole())
			return;

		// FIXME instead of doing the following check set a flag when the edge is added
		// check that x has to have at least one r _neighbor y
		// which is blockable and has successor x
		// (so y is an inv(r) predecessor of x)
		boolean apply = false;
		EdgeList edges = x.getRPredecessorEdges(r.getInverse());
		for (final Edge edge : edges)
		{
			final Individual pred = edge.getFrom();
			if (pred.isBlockable())
			{
				apply = true;
				break;
			}
		}
		if (!apply)
			return;

		if (x.getMaxCard(r) < n)
			return;

		if (x.hasDistinctRNeighborsForMin(r, n, ATermUtils.TOP, true))
			return;

		// if( n == 1 ) {
		// throw new InternalReasonerException(
		// "Functional rule should have been applied " +
		// x + " " + x.isNominal() + " " + edges);
		// }

		int guessMin = x.getMinCard(r, c);
		if (guessMin == 0)
			guessMin = 1;

		// TODO not clear what the correct ds is so be pessimistic and include everything
		DependencySet ds = x.getDepends(mc);
		edges = x.getRNeighborEdges(r);
		for (final Edge edge : edges)
			ds = ds.union(edge.getDepends(), _strategy.getABox().doExplanation());

		final Branch newBranch;
		synchronized (_strategy.getABox())
		{
			_strategy.addBranch(newBranch = new GuessBranch(_strategy.getABox(), _strategy, x, r, guessMin, n, c, ds));
		}

		newBranch.tryNext();
	}
}
