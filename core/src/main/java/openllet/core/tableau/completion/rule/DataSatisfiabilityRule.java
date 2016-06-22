// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.completion.rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import openllet.core.Clash;
import openllet.core.DependencySet;
import openllet.core.Edge;
import openllet.core.Individual;
import openllet.core.Literal;
import openllet.core.Node;
import openllet.core.OpenlletOptions;
import openllet.core.Role;
import openllet.core.datatypes.exceptions.DatatypeReasonerException;
import openllet.core.datatypes.exceptions.InvalidLiteralException;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.tableau.completion.CompletionStrategy;
import openllet.core.tableau.completion.queue.NodeSelector;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public class DataSatisfiabilityRule extends AbstractTableauRule
{

	public DataSatisfiabilityRule(final CompletionStrategy strategy)
	{
		super(strategy, NodeSelector.DATATYPE, BlockingType.NONE);
	}

	@Override
	public void apply(final Individual ind)
	{
		final Set<Literal> nodes = new HashSet<>();
		final LinkedList<Literal> pending = new LinkedList<>();
		final Map<Literal, Set<Literal>> ne = new HashMap<>();
		DependencySet ds = DependencySet.EMPTY;
		boolean nePresent = false;
		for (final Edge e : ind.getOutEdges())
		{
			final Role r = e.getRole();
			if (!r.isDatatypeRole())
				continue;

			ds = ds.union(e.getDepends(), _strategy.getABox().doExplanation());

			final Literal l = (Literal) e.getTo();
			pending.add(l);

			Set<Literal> disj = ne.get(l);

			for (final Role s : r.getDisjointRoles())
				for (final Edge f : ind.getOutEdges().getEdges(s))
				{
					final Literal k = (Literal) f.getTo();
					if (disj == null)
					{
						disj = new HashSet<>();
						ne.put(l, disj);
						nePresent = true;
					}
					disj.add(k);
				}
		}

		while (!pending.isEmpty())
		{
			final Literal l = pending.removeFirst();
			if (!nodes.add(l))
				continue;

			Set<Literal> disj = ne.get(l);

			for (final Node n : l.getDifferents())
				if (n.isLiteral())
				{
					final Literal k = (Literal) n;
					pending.add(k);
					if (disj == null)
					{
						disj = new HashSet<>();
						ne.put(l, disj);
						nePresent = true;
					}
					disj.add(k);
					ds = ds.union(l.getDifferenceDependency(n), _strategy.getABox().doExplanation());
				}
				else
					throw new IllegalStateException();
		}

		/*
		 * This satisfiability check is only needed if an inequality is present
		 * because if no inequalities are present, the check is a repetition of
		 * the satisfiability check performed during Literal.addType
		 * (checkClash)
		 */
		if (nePresent)
			try
			{
				if (!_strategy.getABox().getDatatypeReasoner().isSatisfiable(nodes, ne))
				{
					for (final Node n : nodes)
						for (final DependencySet typeDep : n.getDepends().values())
							ds = ds.union(typeDep, _strategy.getABox().doExplanation());
					/*
					 * TODO: More descriptive clash
					 */
					_strategy.getABox().setClash(Clash.unexplained(ind, ds));
				}
			}
			catch (final InvalidLiteralException e)
			{
				final String msg = "Invalid literal encountered during satisfiability check: " + e.getMessage();
				if (OpenlletOptions.INVALID_LITERAL_AS_INCONSISTENCY)
				{
					_logger.fine(msg);
					for (final Node n : nodes)
						for (final DependencySet typeDep : n.getDepends().values())
							ds = ds.union(typeDep, _strategy.getABox().doExplanation());
					_strategy.getABox().setClash(Clash.invalidLiteral(ind, ds));
				}
				else
				{
					_logger.severe(msg);
					throw new InternalReasonerException(msg, e);
				}
			}
			catch (final DatatypeReasonerException e)
			{
				final String msg = "Unexpected datatype reasoner exception: " + e.getMessage();
				_logger.severe(msg);
				throw new InternalReasonerException(msg, e);
			}
	}
}
