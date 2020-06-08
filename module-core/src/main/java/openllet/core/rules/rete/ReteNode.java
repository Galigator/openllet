// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules.rete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import openllet.shared.tools.Log;

/**
 * @author Evren Sirin
 */
public abstract class ReteNode
{
	public final static Logger		_logger		= Log.getLogger(ReteNode.class);

	private final List<BetaNode>	_children	= new ArrayList<>();

	@SuppressWarnings("unused")
	private boolean					_marked		= false;

	/**
	 * Add a directly dependent _node.
	 *
	 * @param beta
	 */
	public void addChild(final BetaNode beta)
	{
		_children.add(beta);
	}

	/**
	 * @return any directly dependent nodes.
	 */
	public Collection<BetaNode> getBetas()
	{
		return _children;
	}

	/**
	 * Reset any dependent _nodes
	 */
	public void reset()
	{
		for (final BetaNode child : _children)
			child.reset();
	}

	public void restore(final int branch)
	{
		//		if (!_marked) {
		for (final BetaNode child : _children)
			child.restore(branch);
	}

	public void mark()
	{
		setMark(true);
	}

	public void unmark()
	{
		setMark(false);
	}

	private void setMark(final boolean value)
	{
		_marked = value;
		for (final ReteNode child : _children)
			child.setMark(value);
	}

	public void print(final String indent)
	{
		System.out.print(indent);
		System.out.println(this);
	}
}
