// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules.rete;

import java.util.Iterator;

import openllet.aterm.ATermAppl;
import openllet.core.boxes.abox.ABoxForStrategy;
import openllet.core.boxes.abox.Node;
import openllet.core.rules.model.RuleAtom;
import openllet.core.utils.ATermUtils;

/**
 */
public abstract class AlphaNode extends ReteNode
{
	protected boolean _doExplanation;

	protected final ABoxForStrategy _abox;

	public AlphaNode(final ABoxForStrategy abox)
	{
		_abox = abox;
	}

	public abstract Iterator<WME> getMatches(int argIndex, Node arg);

	public abstract Iterator<WME> getMatches();

	public abstract boolean matches(RuleAtom atom);

	protected Node initNode(final ATermAppl name)
	{
		if (ATermUtils.isLiteral(name))
			return _abox.addLiteral(name);
		else
		{
			_abox.copyOnWrite();
			return _abox.getIndividual(name);
		}
	}

	protected void activate(final WME wme)
	{
		_logger.fine(() -> "Activate alpha " + wme);
		getBetas().forEach(betaNode -> betaNode.activate(wme));
	}

	public void setDoExplanation(final boolean doExplanation)
	{
		_doExplanation = doExplanation;
	}

	@Override
	public void print(final String indent)
	{
		getBetas().stream()//
				.filter(BetaNode::isTop)//
				.forEach(node -> node.print(indent));
	}
}
