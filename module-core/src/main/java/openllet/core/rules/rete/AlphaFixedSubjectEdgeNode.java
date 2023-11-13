// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules.rete;

import java.util.Iterator;

import openllet.aterm.ATermAppl;
import openllet.core.boxes.abox.ABoxForStrategy;
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
public class AlphaFixedSubjectEdgeNode extends AlphaFixedEdgeNode
{
	public AlphaFixedSubjectEdgeNode(final ABoxForStrategy abox, final Role role, final ATermAppl subjectName)
	{
		super(abox, role, subjectName);
	}

	@Override
	public boolean activate(final Edge edge)
	{
		final Individual subject = initNode();
		final EdgeDirection dir = edgeMatches(edge);
		if (dir != null && (dir == EdgeDirection.FORWARD ? edge.getFrom() : edge.getTo()).isSame(subject))
		{
			activate(WME.createEdge(edge, dir));
			return true;
		}
		return false;
	}

	@Override
	public Iterator<WME> getMatches(final int argIndex, final Node arg)
	{
		if (argIndex != 1)
			throw new UnsupportedOperationException();

		final Individual subject = initNode();
		return getMatches(subject, _role, arg);
	}

	@Override
	public Iterator<WME> getMatches()
	{
		final Individual subject = initNode();
		return toWMEs(subject.getOutEdges().getEdges(_role), EdgeDirection.FORWARD);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean matches(final RuleAtom atom)
	{
		return (atom instanceof IndividualPropertyAtom || atom instanceof DatavaluedPropertyAtom) && atom.getPredicate().equals(_role.getName()) && ((BinaryAtom) atom).getArgument1() instanceof AtomIConstant && ((AtomIConstant) ((BinaryAtom) atom).getArgument1()).getValue().equals(_name) && ((BinaryAtom) atom).getArgument2() instanceof AtomVariable;
	}

	@Override
	public String toString()
	{
		return ATermUtils.toString(_role.getName()) + "(" + ATermUtils.toString(_name) + ", 1)";
	}
}
