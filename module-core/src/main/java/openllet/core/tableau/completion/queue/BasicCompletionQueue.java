// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Christian Halaschek-Wiener
 */

package openllet.core.tableau.completion.queue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import openllet.aterm.ATermAppl;
import openllet.atom.OpenError;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.Node;

/**
 * A basic _queue for individuals that need to have completion rules applied
 */
public final class BasicCompletionQueue extends CompletionQueue // Class is set to final only to help readability.
{
	/**
	 * The _queue - array - each entry is an arraylist for a particular rule type
	 */
	private final List<ATermAppl> _queue = new ArrayList<>();

	/**
	 * Set to track duplicates for new elements list for queue
	 */
	private final Set<ATermAppl> _newQueue = new HashSet<>();

	//TODO: This will be refactored; however currently there are some unit tests which will not
	//terminate due to the order in which the completion rules are applied to individuals
	//ont the queue. An example of this is MiscTests.testIFDP3() - in this example,
	//if the LiteralRule is applied to the individual "b" first, then an infinite number
	//of non-deterministic choices are created...talk to Evren about this.

	/**
	 * List to hold new elements for the queue
	 */
	private final List<ATermAppl> _newQueueList = new ArrayList<>();

	/**
	 * List of _current index pointer for each queue
	 */
	private int _current = 0;

	/**
	 * List of _current index pointer for each queue
	 */
	private int _end = 0;

	/**
	 * List of _current index pointer for the stopping point at each queue
	 */
	private int _cutOff = 0;

	/**
	 * Flag set for when the kb is restored - in this case we do not want to flush the queue immediatly
	 */
	private boolean _backtracked = false;

	/**
	 * Constructor - create queue
	 *
	 * @param abox
	 */
	public BasicCompletionQueue(final ABox abox)
	{
		super(abox);
	}

	public BasicCompletionQueue(final BasicCompletionQueue that)
	{
		super(that._abox);
		_queue.addAll(that._queue);
		_newQueue.addAll(that._newQueue);
		_newQueueList.addAll(that._newQueueList);

		_current = that._current;
		_cutOff = that._cutOff;
		_backtracked = that._backtracked;
		_end = that._end;
		setAllowLiterals(that.isAllowLiterals());
	}

	/**
	 * Find the next _individual in a given queue
	 *
	 * @param type
	 */
	@Override
	protected void findNext(final int type)
	{
		for (; _current < _cutOff; _current++)
		{
			Node node = _abox.getNode(_queue.get(_current));

			//because we do not maitain the _queue during restore this _node could be non-existent
			if (node == null)
				continue;

			node = node.getSame();

			if ((node.isLiteral() && isAllowLiterals() || node.isIndividual() && !isAllowLiterals()) && !node.isPruned())
				break;
		}
	}

	/**
	 * @return true if there is another element on the queue to process
	 */
	@Override
	public boolean hasNext()
	{
		findNext(-1);
		return _current < _cutOff;
	}

	/**
	 * Restore the queue to be the current nodes in the abox; Also reset the type index to 0
	 *
	 * @param branch
	 */
	@Override
	public void restore(final int branch)
	{
		_queue.addAll(_newQueueList);
		_newQueue.clear();
		_newQueueList.clear();
		_end = _queue.size();
		_current = 0;
		_cutOff = _end;
		_backtracked = true;
	}

	/**
	 * @return the next element of a queue of a given type
	 */
	@Override
	public Individual next()
	{
		//get the next _index
		findNext(-1);
		Individual ind = _abox.getIndividual(_queue.get(_current));
		ind = ind.getSame();
		_current++;
		return ind;

	}

	/**
	 * @return the next element of a queue of a given type
	 */
	@Override
	public Node nextLiteral()
	{
		//get the next _index
		findNext(-1);
		Node node = _abox.getNode(_queue.get(_current));
		node = node.getSame();
		_current++;
		return node;
	}

	@Override
	public void add(final QueueElement x, final NodeSelector s)
	{
		add(x);
	}

	@Override
	public void add(final QueueElement x)
	{
		if (!_newQueue.contains(x.getNode()))
		{
			_newQueue.add(x.getNode());
			_newQueueList.add(x.getNode());
		}
	}

	/**
	 * Reset the cutoff for a given type index
	 *
	 * @param s
	 */
	@Override
	public void reset(final NodeSelector s)
	{
		_cutOff = _end;
		_current = 0;
	}

	/**
	 * Set branch pointers to current pointer. This is done whenever abox.incrementBranch is called
	 *
	 * @param branch
	 */
	@Override
	public void incrementBranch(final int branch)
	{
		return;
	}

	/**
	 * @return a copy of the queue
	 */
	@Override
	public BasicCompletionQueue copy()
	{
		return new BasicCompletionQueue(this);
	}

	/**
	 * Set the abox for the queue
	 *
	 * @param ab
	 */
	@Override
	public void setABox(final ABox ab)
	{
		_abox = ab;
	}

	/**
	 * Print method for a given queue type
	 *
	 * @param type
	 */
	@Override
	public void print(final int type)
	{
		System.out.println("Queue: " + _queue);
	}

	/**
	 * Print method for entire queue
	 */
	@Override
	public void print()
	{
		System.out.println("Queue: " + _queue);
	}

	/**
	 * Remove method for abstract class
	 */
	@Override
	public void remove()
	{
		throw new OpenError("Remove is not supported");
	}

	@Override
	public void flushQueue()
	{
		if (!_backtracked && !_closed)
			_queue.clear();
		else
			if (_closed)
				if (!_abox.isClosed())
					_closed = false;

		_queue.addAll(_newQueueList);

		_newQueue.clear();
		_newQueueList.clear();

		_end = _queue.size();

		_backtracked = false;
	}

	@Override
	protected void flushQueue(final NodeSelector s)
	{
		return;
	}

	@Override
	public void clearQueue(final NodeSelector s)
	{
		return;
	}

}
