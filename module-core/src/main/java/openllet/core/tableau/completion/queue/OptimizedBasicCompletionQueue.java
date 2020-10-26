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
import openllet.core.boxes.abox.Literal;
import openllet.core.boxes.abox.Node;

/**
 * An optimized basic _queue for individuals that need to have completion rules applied
 */
public final class OptimizedBasicCompletionQueue extends CompletionQueue // The class it set 'final' only to help reading the code.
{

	/**
	 * The queue - array - each entry is an arraylist for a particular rule type
	 */
	private final List<ATermAppl>[] _queue;

	/**
	 * Set to track duplicates for new elements list for queue
	 */
	private final Set<ATermAppl>[] _newQueue;

	//TODO: This will be refactored; however currently there are some unit tests which will not
	//terminate due to the _order in which the completion rules are applied to individuals
	//ont the _queue. An example of this is MiscTests.testIFDP3() - in this example,
	//if the LiteralRule is applied to the _individual "b" first, then an infinite number
	//of non-deterministic choices are created...talk to Evren about this.

	/**
	 * List to hold new elements for the queue
	 */
	private final List<ATermAppl>[] _newQueueList;

	/**
	 * List of current index pointer for each queue
	 */
	private final int _current[];

	/**
	 * List of current _index pointer for each queue
	 */
	private final int _end[];

	/**
	 * List of current index pointer for the stopping point at each queue
	 */
	private final int _cutOff[];

	/**
	 * Flag set for when the kb is restored - in this case we do not want to flush the queue immediatly
	 */
	private boolean _backtracked = false;

	/**
	 * Constructor - create _queue
	 *
	 * @param abox
	 */
	@SuppressWarnings("unchecked")
	public OptimizedBasicCompletionQueue(final ABox abox)
	{
		super(abox);
		final int nSelectors = NodeSelector.numSelectors();
		_queue = new ArrayList[nSelectors];
		_newQueue = new HashSet[nSelectors];
		_newQueueList = new ArrayList[nSelectors];

		_current = new int[nSelectors];
		_cutOff = new int[nSelectors];
		_end = new int[nSelectors];

		for (int i = 0; i < nSelectors; i++)
		{
			_queue[i] = new ArrayList<>();
			_newQueue[i] = new HashSet<>();
			_newQueueList[i] = new ArrayList<>();

			_current[i] = 0;
			_cutOff[i] = 0;
			_end[i] = 0;
		}
	}

	/**
	 * Find the next _individual in a given _queue
	 *
	 * @param type
	 */
	@Override
	protected void findNext(final int type)
	{
		for (; _current[type] < _cutOff[type]; _current[type]++)
		{
			Node node = _abox.getNode(_queue[type].get(_current[type]));

			//because we do not maitain the _queue during restore this _node could be non-existent
			if (node == null)
				continue;

			node = node.getSame();

			if (node != null)
				if ((node instanceof Literal && isAllowLiterals() || node instanceof Individual && !isAllowLiterals()) && !node.isPruned())
					break;
		}
	}

	/**
	 * @return true if there is another element on the queue to process
	 */
	@Override
	public boolean hasNext()
	{
		findNext(_currentType);
		return _current[_currentType] < _cutOff[_currentType];
	}

	/**
	 * Reset the queue to be the current nodes in the abox; Also reset the type index to 0
	 *
	 * @param branch
	 */
	@Override
	public void restore(final int branch)
	{
		for (int i = 0; i < NodeSelector.numSelectors(); i++)
		{
			_queue[i].addAll(_newQueueList[i]);
			_newQueue[i].clear();
			_newQueueList[i].clear();
			_end[i] = _queue[i].size();
			_current[i] = 0;
			_cutOff[i] = _end[i];
		}
		_backtracked = true;
	}

	/**
	 * @return the next element of a _queue of a given type
	 */
	@Override
	public Individual next()
	{
		//get the next _index
		findNext(_currentType);
		Individual ind = (Individual) _abox.getNode(_queue[_currentType].get(_current[_currentType]));
		ind = ind.getSame();
		_current[_currentType]++;
		return ind;
	}

	/**
	 * @return the next element of a _queue of a given type
	 */
	@Override
	public Node nextLiteral()
	{
		//get the next _index
		findNext(_currentType);
		Node node = _abox.getNode(_queue[_currentType].get(_current[_currentType]));
		node = node.getSame();
		_current[_currentType]++;
		return node;
	}

	@Override
	public void add(final QueueElement x, final NodeSelector s)
	{
		final int type = s.ordinal();
		if (!_newQueue[type].contains(x.getNode()))
		{
			_newQueue[type].add(x.getNode());
			_newQueueList[type].add(x.getNode());
		}
	}

	@Override
	public void add(final QueueElement x)
	{
		for (int i = 0; i < NodeSelector.numSelectors(); i++)
			if (!_newQueue[i].contains(x.getNode()))
			{
				_newQueue[i].add(x.getNode());
				_newQueueList[i].add(x.getNode());
			}
	}

	/**
	 * Reset the cutoff for a given type _index
	 *
	 * @param s
	 */
	@Override
	public void reset(final NodeSelector s)
	{
		_currentType = s.ordinal();
		_cutOff[_currentType] = _end[_currentType];
		_current[_currentType] = 0;
	}

	/**
	 * Set _branch pointers to _current pointer. This is done whenever _abox.incrementBranch is called
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
	public OptimizedBasicCompletionQueue copy()
	{
		final OptimizedBasicCompletionQueue copy = new OptimizedBasicCompletionQueue(_abox);

		for (int i = 0; i < NodeSelector.numSelectors(); i++)
		{
			copy._queue[i] = new ArrayList<>(_queue[i]);
			copy._newQueue[i] = new HashSet<>(_newQueue[i]);
			copy._newQueueList[i] = new ArrayList<>(_newQueueList[i]);

			copy._current[i] = _current[i];
			copy._cutOff[i] = _cutOff[i];
			copy._end[i] = _end[i];
		}

		copy._backtracked = _backtracked;

		copy.setAllowLiterals(isAllowLiterals());

		return copy;
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
		if (type > NodeSelector.numSelectors())
			return;
		System.out.println("Queue " + type + ": " + _queue[type]);
	}

	/**
	 * Print method for entire _queue
	 */
	@Override
	public void print()
	{
		for (int i = 0; i < NodeSelector.numSelectors(); i++)
			System.out.println("Queue " + i + ": " + _queue[i]);
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
		for (int i = 0; i < NodeSelector.numSelectors(); i++)
		{
			if (!_backtracked && !_closed)
				_queue[i].clear();
			else
				if (_closed)
					if (!_abox.isClosed())
						_closed = false;

			_queue[i].addAll(_newQueueList[i]);

			_newQueue[i].clear();
			_newQueueList[i].clear();

			_end[i] = _queue[i].size();
		}

		_backtracked = false;
	}

	@Override
	protected void flushQueue(final NodeSelector s)
	{

		final int index = s.ordinal();

		if (index == NodeSelector.UNIVERSAL.ordinal() || !_backtracked)
			_queue[index].clear();

		_queue[index].addAll(_newQueueList[index]);

		_newQueue[index].clear();
		_newQueueList[index].clear();

		_end[index] = _queue[index].size();
	}

	@Override
	public void clearQueue(final NodeSelector s)
	{

		final int index = s.ordinal();

		_queue[index].clear();

		_newQueue[index].clear();
		_newQueueList[index].clear();

		_end[index] = _queue[index].size();
	}

}
