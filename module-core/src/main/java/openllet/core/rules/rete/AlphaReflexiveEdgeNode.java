// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules.rete;

import java.util.Iterator;

import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.Edge;
import openllet.core.boxes.abox.EdgeList;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.Node;
import openllet.core.boxes.rbox.Role;
import openllet.core.rules.model.AtomVariable;
import openllet.core.rules.model.BinaryAtom;
import openllet.core.rules.model.DatavaluedPropertyAtom;
import openllet.core.rules.model.IndividualPropertyAtom;
import openllet.core.rules.model.RuleAtom;
import openllet.core.rules.rete.WME.EdgeDirection;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.iterator.NestedIterator;

/**
 */
public class AlphaReflexiveEdgeNode extends AlphaEdgeNode
{
	public AlphaReflexiveEdgeNode(final ABox abox, final Role role)
	{
		super(abox, role);
	}

	@Override
	public boolean activate(final Edge edge)
	{
		assert edgeMatches(edge) != null;
		if (edge.getFromName().equals(edge.getToName()))
		{
			activate(WME.createEdge(edge));
			return true;
		}
		return false;
	}

	@Override
	public Iterator<WME> getMatches(final int argIndex, final Node arg)
	{
		final EdgeList edges = ((Individual) arg).getRNeighborEdges(_role, arg);

		return toWMEs(edges, EdgeDirection.FORWARD);
	}

	@Override
	public Iterator<WME> getMatches()
	{
		return new NestedIterator<>(_abox.getIndIterator())
		{
			@Override
			public Iterator<WME> getInnerIterator(final Individual ind)
			{
				return toWMEs(ind.getEdgesTo(ind), EdgeDirection.FORWARD);
			}
		};
	}

	@Override
	public boolean matches(final RuleAtom atom)
	{
		return (atom instanceof IndividualPropertyAtom || atom instanceof DatavaluedPropertyAtom) && atom.getPredicate().equals(_role.getName()) && ((BinaryAtom<?, ?, ?>) atom).getArgument1() instanceof AtomVariable && ((BinaryAtom<?, ?, ?>) atom).getArgument2().equals(((BinaryAtom<?, ?, ?>) atom).getArgument1());
	}

	@Override
	public String toString()
	{
		return ATermUtils.toString(_role.getName()) + "(0, 0)";
	}
}
