// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules.rete;

import java.util.Iterator;
import openllet.core.ABoxImpl;
import openllet.core.DependencySet;
import openllet.core.Individual;
import openllet.core.Node;
import openllet.core.rules.model.DifferentIndividualsAtom;
import openllet.core.rules.model.RuleAtom;
import openllet.core.utils.iterator.MapIterator;
import openllet.core.utils.iterator.NestedIterator;

/**
 */
public class AlphaDiffFromNode extends AlphaNode
{
	public AlphaDiffFromNode(final ABoxImpl abox)
	{
		super(abox);
	}

	public boolean activate(final Individual s, final Individual o, final DependencySet ds)
	{
		activate(WME.createDiffFrom(s, o, ds));
		return true;
	}

	@Override
	public Iterator<WME> getMatches(final int argIndex, final Node arg)
	{
		if (argIndex != 0 && argIndex != 1)
			throw new IndexOutOfBoundsException();

		if (!(arg instanceof Individual))
			throw new IllegalArgumentException();

		return toWMEs(arg);
	}

	private Iterator<WME> toWMEs(final Node arg)
	{
		return new MapIterator<Node, WME>(arg.getDifferents().iterator())
		{
			@Override
			public WME map(final Node node)
			{
				return WME.createDiffFrom((Individual) arg, (Individual) node, arg.getDifferenceDependency(node));
			}
		};
	}

	@Override
	public Iterator<WME> getMatches()
	{
		return new NestedIterator<Individual, WME>(_abox.getIndIterator())
		{
			@Override
			public Iterator<WME> getInnerIterator(final Individual ind)
			{
				return toWMEs(ind);
			}
		};
	}

	@Override
	public boolean matches(final RuleAtom atom)
	{
		return (atom instanceof DifferentIndividualsAtom);
	}

	@Override
	public String toString()
	{
		return "DiffFrom(0, 1)";
	}
}
