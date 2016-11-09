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
public class OptimizedBasicCompletionQueue extends CompletionQueue
{

	/**
	 * The _queue - array - each entry is an arraylist for a particular rule type
	 */
	protected List<ATermAppl>[] queue;

	/**
	 * Set to track duplicates for new elements list for _queue
	 */
	protected Set<ATermAppl>[] newQueue;

	//TODO: This will be refactored; however currently there are some unit tests which will not
	//terminate due to the _order in which the completion rules are applied to individuals
	//ont the _queue. An example of this is MiscTests.testIFDP3() - in this example,
	//if the LiteralRule is applied to the _individual "b" first, then an infinite number
	//of non-deterministic choices are created...talk to Evren about this.

	/**
	 * List to hold new elements for the _queue
	 */
	protected List<ATermAppl>[] newQueueList;

	/**
	 * List of _current _index pointer for each _queue
	 */
	protected int current[];

	/**
	 * List of _current _index pointer for each _queue
	 */
	protected int end[];

	/**
	 * List of _current _index pointer for the stopping point at each _queue
	 */
	protected int cutOff[];

	/**
	 * Flag set for when the kb is restored - in this case we do not want to flush the _queue immediatly
	 */
	protected boolean backtracked;

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
		queue = new ArrayList[nSelectors];
		newQueue = new HashSet[nSelectors];
		newQueueList = new ArrayList[nSelectors];

		current = new int[nSelectors];
		cutOff = new int[nSelectors];
		end = new int[nSelectors];

		for (int i = 0; i < nSelectors; i++)
		{
			queue[i] = new ArrayList<>();
			newQueue[i] = new HashSet<>();
			newQueueList[i] = new ArrayList<>();

			current[i] = 0;
			cutOff[i] = 0;
			end[i] = 0;
		}

		backtracked = false;
	}

	/**
	 * Find the next _individual in a given _queue
	 *
	 * @param type
	 */
	@Override
	protected void findNext(final int type)
	{
		for (; current[type] < cutOff[type]; current[type]++)
		{
			Node node = _abox.getNode(queue[type].get(current[type]));

			//because we do not maitain the _queue during restore this _node could be non-existent
			if (node == null)
				continue;

			node = node.getSame();

			if (node != null)
				if ((node instanceof Literal && allowLiterals() || node instanceof Individual && !allowLiterals()) && !node.isPruned())
					break;
		}
	}

	/**
	 * @return true if there is another element on the queue to process
	 */
	@Override
	public boolean hasNext()
	{
		findNext(currentType);
		return current[currentType] < cutOff[currentType];
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
			queue[i].addAll(newQueueList[i]);
			newQueue[i].clear();
			newQueueList[i].clear();
			end[i] = queue[i].size();
			current[i] = 0;
			cutOff[i] = end[i];
		}
		backtracked = true;
	}

	/**
	 * @return the next element of a _queue of a given type
	 */
	@Override
	public Individual next()
	{
		//get the next _index
		findNext(currentType);
		Individual ind = (Individual) _abox.getNode(queue[currentType].get(current[currentType]));
		ind = ind.getSame();
		current[currentType]++;
		return ind;
	}

	/**
	 * @return the next element of a _queue of a given type
	 */
	@Override
	public Node nextLiteral()
	{
		//get the next _index
		findNext(currentType);
		Node node = _abox.getNode(queue[currentType].get(current[currentType]));
		node = node.getSame();
		current[currentType]++;
		return node;
	}

	@Override
	public void add(final QueueElement x, final NodeSelector s)
	{
		final int type = s.ordinal();
		if (!newQueue[type].contains(x.getNode()))
		{
			newQueue[type].add(x.getNode());
			newQueueList[type].add(x.getNode());
		}
	}

	@Override
	public void add(final QueueElement x)
	{
		for (int i = 0; i < NodeSelector.numSelectors(); i++)
			if (!newQueue[i].contains(x.getNode()))
			{
				newQueue[i].add(x.getNode());
				newQueueList[i].add(x.getNode());
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
		currentType = s.ordinal();
		cutOff[currentType] = end[currentType];
		current[currentType] = 0;
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
			copy.queue[i] = new ArrayList<>(queue[i]);
			copy.newQueue[i] = new HashSet<>(newQueue[i]);
			copy.newQueueList[i] = new ArrayList<>(newQueueList[i]);

			copy.current[i] = current[i];
			copy.cutOff[i] = cutOff[i];
			copy.end[i] = end[i];
		}

		copy.backtracked = backtracked;

		copy.setAllowLiterals(allowLiterals());

		//copy _branch effects
		//		for(int i = 0; i < branchEffects.size(); i++){
		//			HashSet<ATermAppl> cp = new HashSet<ATermAppl>();
		//			cp.addAll((Set<ATermAppl>)branchEffects.get(i));
		//			copy.branchEffects.add(cp);
		//		}
		//
		return copy;
	}

	/**
	 * Set the _abox for the _queue
	 *
	 * @param ab
	 */
	@Override
	public void setABox(final ABox ab)
	{
		_abox = ab;
	}

	/**
	 * Print method for a given _queue type
	 *
	 * @param type
	 */
	@Override
	public void print(final int type)
	{
		if (type > NodeSelector.numSelectors())
			return;
		System.out.println("Queue " + type + ": " + queue[type]);
	}

	/**
	 * Print method for entire _queue
	 */
	@Override
	public void print()
	{
		for (int i = 0; i < NodeSelector.numSelectors(); i++)
			System.out.println("Queue " + i + ": " + queue[i]);
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

			if (!backtracked && !closed)
				queue[i].clear();
			else
				if (closed)
					if (!_abox.isClosed())
						closed = false;

			queue[i].addAll(newQueueList[i]);

			newQueue[i].clear();
			newQueueList[i].clear();

			end[i] = queue[i].size();
		}

		backtracked = false;
	}

	@Override
	protected void flushQueue(final NodeSelector s)
	{

		final int index = s.ordinal();

		if (index == NodeSelector.UNIVERSAL.ordinal() || !backtracked)
			queue[index].clear();

		queue[index].addAll(newQueueList[index]);

		newQueue[index].clear();
		newQueueList[index].clear();

		end[index] = queue[index].size();
	}

	@Override
	public void clearQueue(final NodeSelector s)
	{

		final int index = s.ordinal();

		queue[index].clear();

		newQueue[index].clear();
		newQueueList[index].clear();

		end[index] = queue[index].size();
	}

}
