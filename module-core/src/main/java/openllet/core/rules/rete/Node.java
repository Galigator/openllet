// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules.rete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import openllet.aterm.ATermAppl;

/**
 * <p>
 * Title: Node
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 */
public class Node
{

	private final List<BetaNode> _betaNodes = new ArrayList<>();
	public final Index<ATermAppl, Fact> _index = new Index<>();
	public List<ATermAppl> _svars = new ArrayList<>();
	public List<ATermAppl> _vars;

	/**
	 * Add a directly dependent beta node.
	 */
	public void add(final BetaNode beta)
	{
		_betaNodes.add(beta);
	}

	/**
	 * Return any directly dependent beta nodes.
	 */
	public Collection<BetaNode> getBetas()
	{
		return _betaNodes;
	}

	/**
	 * Return the key for indexing.
	 */
	protected List<ATermAppl> getKey()
	{
		List<ATermAppl> key;
		key = Utils.concat(_svars, _vars);
		key = Utils.removeDups(key);

		return key;
	}

	protected int getKeyPosition(final ATermAppl var)
	{
		return getKey().indexOf(var);
	}

	public void reset()
	{
		_index.clear();
	}

}
