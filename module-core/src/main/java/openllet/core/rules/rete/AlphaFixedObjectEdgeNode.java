// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules.rete;

import java.util.Iterator;
import openllet.aterm.ATermAppl;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.Edge;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.Node;
import openllet.core.boxes.rbox.Role;
import openllet.core.rules.model.AtomIConstant;
import openllet.core.rules.model.AtomVariable;
import openllet.core.rules.model.BinaryAtom;
import openllet.core.rules.model.DatavaluedPropertyAtom;
import openllet.core.rules.model.IndividualPropertyAtom;
import openllet.core.rules.model.RuleAtom;
import openllet.core.rules.rete.WME.EdgeDirection;
import openllet.core.utils.ATermUtils;

/**
 */
public class AlphaFixedObjectEdgeNode extends AlphaFixedEdgeNode
{
	public AlphaFixedObjectEdgeNode(final ABox abox, final Role role, final ATermAppl object)
	{
		super(abox, role, object);
	}

	@Override
	public boolean activate(final Edge edge)
	{
		final EdgeDirection dir = edgeMatches(edge);
		final Node object = initNode();
		if (dir != null && (dir == EdgeDirection.FORWARD ? edge.getTo() : edge.getFrom()).isSame(object))
		{
			activate(WME.createEdge(edge, dir));
			return true;
		}
		return false;
	}

	@Override
	public Iterator<WME> getMatches(final int argIndex, final Node arg)
	{
		if (argIndex != 0)
			throw new UnsupportedOperationException();

		final Node object = initNode();
		return getMatches((Individual) arg, _role, object);
	}

	@Override
	public Iterator<WME> getMatches()
	{
		final Node object = initNode();
		return toWMEs(object.getInEdges().getEdges(_role), EdgeDirection.FORWARD);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean matches(final RuleAtom atom)
	{
		return ((atom instanceof IndividualPropertyAtom) || (atom instanceof DatavaluedPropertyAtom)) && atom.getPredicate().equals(_role.getName()) && ((BinaryAtom) atom).getArgument1() instanceof AtomVariable && ((BinaryAtom) atom).getArgument2() instanceof AtomIConstant && ((AtomIConstant) ((BinaryAtom) atom).getArgument2()).getValue().equals(_name);
	}

	@Override
	public String toString()
	{
		return ATermUtils.toString(_role.getName()) + "(0, " + ATermUtils.toString(_name) + ")";
	}
}
