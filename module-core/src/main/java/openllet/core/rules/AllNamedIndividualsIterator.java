// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules;

import java.util.Iterator;
import java.util.NoSuchElementException;
import openllet.core.ABoxImpl;
import openllet.core.Individual;
import openllet.core.Node;

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

	private Individual nextIndividual;
	private final Iterator<Individual> nodeIterator;

	public AllNamedIndividualsIterator(final ABoxImpl abox)
	{
		nodeIterator = abox.getIndIterator();
	}

	@Override
	public boolean hasNext()
	{
		if (nextIndividual != null)
			return true;

		while (nodeIterator.hasNext())
		{
			final Node candidate = nodeIterator.next();
			if ((candidate instanceof Individual) && candidate.isRootNominal())
			{
				nextIndividual = (Individual) candidate;
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
		final Individual result = nextIndividual;
		nextIndividual = null;
		return result;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

}
