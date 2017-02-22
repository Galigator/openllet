// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.completion.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermInt;
import openllet.aterm.ATermList;
import openllet.core.DependencySet;
import openllet.core.boxes.abox.Clash;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.Node;
import openllet.core.boxes.rbox.Role;
import openllet.core.datatypes.exceptions.DatatypeReasonerException;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.tableau.completion.CompletionStrategy;
import openllet.core.tableau.completion.queue.NodeSelector;

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
public class DataCardinalityRule extends AbstractTableauRule
{
	public DataCardinalityRule(final CompletionStrategy strategy)
	{
		super(strategy, NodeSelector.DATATYPE, BlockingType.NONE);
	}

	@Override
	public void apply(final Individual x)
	{
		final Map<ATermAppl, Collection<ATermAppl>> dataranges = new HashMap<>();
		final Map<ATermAppl, DependencySet> rangeDepends = new HashMap<>();

		for (final ATermAppl allDesc : x.getTypes(Node.ALL)) // Gather all _data properties that appear in universal restrictions on this _node.
		{
			final ATerm rTerm = allDesc.getArgument(0);

			if (rTerm instanceof ATermList) // Skip object property chains
				continue;

			final ATermAppl r = (ATermAppl) rTerm;
			final Role role = _strategy.getABox().getRole(r);

			if (null == role) // FIXME null should not be observe.
				continue;

			if (!role.isDatatypeRole()) // Skip any roles that are not datatype properties
				continue;

			Collection<ATermAppl> existing = dataranges.get(r); // Collect the _data range and its dependency set
			DependencySet ds = x.getDepends(allDesc);
			if (existing == null)
			{
				existing = new ArrayList<>();
				dataranges.put(r, existing);
			}
			else
				ds = ds.union(rangeDepends.get(r), _strategy.getABox().doExplanation());
			existing.add((ATermAppl) allDesc.getArgument(1));
			rangeDepends.put(r, ds);

		}

		for (final ATermAppl minDesc : x.getTypes(Node.MIN)) // Get the ranges of any _data properties that have min cardinality restrictions
		{
			/*
			 * TODO: Verify that minDesc will never have a property chain
			 */
			final ATermAppl r = (ATermAppl) minDesc.getArgument(0);
			final Role role = _strategy.getABox().getRole(r);

			if (!role.isDatatypeRole()) // Skip any roles that are not datatype properties
				continue;

			final Set<ATermAppl> ranges = role.getRanges();
			if (!ranges.isEmpty())
			{
				Collection<ATermAppl> existing = dataranges.get(r);
				DependencySet ds;
				if (existing == null)
				{
					existing = new ArrayList<>();
					dataranges.put(r, existing);
					ds = DependencySet.EMPTY;
				}
				else
					ds = rangeDepends.get(r);

				for (final ATermAppl dataRange : role.getRanges())
				{
					/*
					 * TODO: Verify the dependency set handling here. The old
					 * implementation just used independent (thus could avoid
					 * this loop and call addAll)
					 */
					existing.add(dataRange);
					ds = ds.union(role.getExplainRange(dataRange), _strategy.getABox().doExplanation());
					rangeDepends.put(r, ds);
				}
			}
		}

		for (final ATermAppl minDesc : x.getTypes(Node.MIN)) // For each of the min cardinality restrictions, verify that the _data range is large enough
		{
			final ATermAppl r = (ATermAppl) minDesc.getArgument(0);
			final Role role = _strategy.getABox().getRole(r);

			final Set<ATermAppl> drs = new HashSet<>();
			final Collection<ATermAppl> direct = dataranges.get(r);
			DependencySet ds;
			if (direct != null)
			{
				drs.addAll(direct);
				ds = rangeDepends.get(r);
			}
			else
				ds = DependencySet.EMPTY;

			ds = ds.union(x.getDepends(minDesc), _strategy.getABox().doExplanation());

			for (final Role superRole : role.getSuperRoles())
			{
				final ATermAppl s = superRole.getName();
				final Collection<ATermAppl> inherited = dataranges.get(s);
				if (inherited != null)
				{
					drs.addAll(inherited);
					ds = ds.union(rangeDepends.get(s), _strategy.getABox().doExplanation()).union(role.getExplainSuper(s), _strategy.getABox().doExplanation());
				}
			}

			if (!drs.isEmpty())
			{
				final int n = ((ATermInt) minDesc.getArgument(1)).getInt();
				try
				{
					if (!_strategy.getABox().getDatatypeReasoner().containsAtLeast(n, drs))
					{
						_strategy.getABox().setClash(Clash.minMax(x, ds));
						return;
					}
				}
				catch (final DatatypeReasonerException e)
				{
					throw new InternalReasonerException(e); // TODO Better Error Handling
				}
			}
		}
	}
}
