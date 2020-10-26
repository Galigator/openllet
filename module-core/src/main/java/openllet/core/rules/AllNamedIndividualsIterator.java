// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules;

import java.util.Iterator;
import java.util.NoSuchElementException;

import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.Node;

/**
 * <p>
 * Title: All Named Individuals Iterator
 * </p>
 * <p>
 * Description: Iterates over all named individuals in the _abox,
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Ron Alford
 */
public class AllNamedIndividualsIterator implements Iterator<Individual>
{
	private Individual _nextIndividual;
	private final Iterator<Individual> _nodeIterator;

	public AllNamedIndividualsIterator(final ABox abox)
	{
		_nodeIterator = abox.getIndIterator();
	}

	@Override
	public boolean hasNext()
	{
		if (_nextIndividual != null)
			return true;

		while (_nodeIterator.hasNext())
		{
			final Node candidate = _nodeIterator.next();
			if (candidate instanceof Individual && candidate.isRootNominal())
			{
				_nextIndividual = (Individual) candidate;
				return true;
			}
		}

		return false;
	}

	@Override
	public Individual next()
	{
		if (!hasNext())
			throw new NoSuchElementException();
		final Individual result = _nextIndividual;
		_nextIndividual = null;
		return result;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

}
