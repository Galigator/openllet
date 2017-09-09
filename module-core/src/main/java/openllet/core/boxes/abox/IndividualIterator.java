// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.boxes.abox;

import java.util.Iterator;
import java.util.List;
import openllet.aterm.ATermAppl;
import openllet.core.tableau.completion.queue.NodeSelector;

/**
 * An iterator to return nodes in the order they are added. Having a separate iterator instead of using nodes.iterator() allows to change the nodes table
 * without resetting the iteration process.
 *
 * @author Evren Sirin
 */
public class IndividualIterator implements Iterator<Individual>
{
	/**
	 * ABox where the individuals are stored
	 */
	protected volatile ABox _abox; // Can change in tableau.completion.queue.CompletionQueue

	/**
	 * List of node names
	 */
	private final List<ATermAppl> _nodeList;

	/**
	 * Last returned _index
	 */
	private int _index = 0;

	/**
	 * Index where iterator stops (size of list by default)
	 */
	private final int _stop;

	/**
	 * Create an iterator over all the individuals in the ABox
	 *
	 * @param abox
	 */
	public IndividualIterator(final ABox abox)
	{
		_abox = abox;
		_nodeList = abox.getNodeNames();
		_stop = _nodeList.size();

		findNext();
	}

	private void findNext()
	{
		for (; _index < _stop; _index++)
		{
			final Node node = _abox.getNode(_nodeList.get(_index));
			if (!node.isPruned() && node.isIndividual())
				break;
		}
	}

	@Override
	public boolean hasNext()
	{
		findNext();
		return _index < _stop;
	}

	public void reset(@SuppressWarnings("unused") final NodeSelector s) // 's' is used only in the 'tableau.completion.queue.OptimizedBasicCompletionQueue'
	{
		_index = 0;
		findNext();
	}

	@Override
	public Individual next()
	{
		findNext();
		return _abox.getIndividual(_nodeList.get(_index++));
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}
