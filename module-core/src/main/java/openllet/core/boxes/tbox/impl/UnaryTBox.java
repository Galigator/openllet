// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.boxes.tbox.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import openllet.aterm.ATermAppl;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.CollectionUtils;
import openllet.core.utils.iterator.IteratorUtils;
import openllet.shared.tools.Log;

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
public class UnaryTBox
{
	public static final Logger						_logger		= Log.getLogger(UnaryTBox.class);

	private final Map<ATermAppl, List<Unfolding>>	_unfoldings	= CollectionUtils.makeIdentityMap();

	public void add(final ATermAppl sub, final ATermAppl sup, final Set<ATermAppl> explanation)
	{
		List<Unfolding> list = _unfoldings.get(sub);
		if (list == null)
		{
			list = new ArrayList<>();
			_unfoldings.put(sub, list);
		}

		_logger.fine(() -> "Add sub: " + ATermUtils.toString(sub) + " < " + ATermUtils.toString(sup));
		list.add(Unfolding.create(ATermUtils.normalize(sup), explanation));
	}

	public Iterator<Unfolding> unfold(final ATermAppl concept)
	{
		final List<Unfolding> unfoldingList = _unfoldings.get(concept);
		return unfoldingList == null ? IteratorUtils.<Unfolding>emptyIterator() : unfoldingList.iterator();
	}

	public void print(final Appendable out) throws IOException
	{
		for (final Entry<ATermAppl, List<Unfolding>> e : _unfoldings.entrySet())
		{
			out.append(ATermUtils.toString(e.getKey()));
			out.append(" < ");
			out.append(e.getValue().toString());
			out.append("\n");
		}
	}
}
