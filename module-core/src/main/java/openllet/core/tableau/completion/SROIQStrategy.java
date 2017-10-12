// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.completion;

import java.util.List;
import java.util.logging.Level;
import openllet.core.OpenlletOptions;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.IndividualIterator;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.expressivity.Expressivity;
import openllet.core.tableau.branch.Branch;
import openllet.core.tableau.completion.rule.TableauRule;

/**
 * https://lat.inf.tu-dresden.de/~baader/Talks/Tableaux2000.pdf
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public class SROIQStrategy extends CompletionStrategy
{
	public SROIQStrategy(final ABox abox)
	{
		super(abox);
	}

	protected boolean backtrack()
	{
		boolean branchFound = false;
		_abox.getStats()._backtracks++;
		while (!branchFound)
		{
			_completionTimer.ifPresent(t -> t.check());

			final int lastBranch = _abox.getClash().getDepends().max();

			if (lastBranch <= 0) // not more branches to try
				return false;
			else
				if (lastBranch > _abox.getBranches().size())
					throw new InternalReasonerException("Backtrack: Trying to backtrack to _branch " + lastBranch + " but has only " + _abox.getBranches().size() + " branches. Clash found: " + _abox.getClash());
				else
					if (OpenlletOptions.USE_INCREMENTAL_DELETION)
					{
						final Branch br = _abox.getBranches().get(lastBranch - 1); // get the last _branch

						// if this is the last _disjunction, merge pair, etc. for the
						// _branch (i.e, br.tryNext == br.tryCount-1) and there are no
						// other branches to test (ie.
						// _abox.getClash().depends.size()==2),
						// then update depedency _index and return false
						if (br.getTryNext() == br.getTryCount() - 1 && _abox.getClash().getDepends().size() == 2)
						{
							_abox.getKB().getDependencyIndex().addCloseBranchDependency(br, _abox.getClash().getDepends());
							return false;
						}
					}

			final List<Branch> branches = _abox.getBranches();
			_abox.getStats()._backjumps += branches.size() - lastBranch;
			// CHW - added for incremental deletion support
			if (OpenlletOptions.USE_TRACING && OpenlletOptions.USE_INCREMENTAL_CONSISTENCY)
			{
				// we must clean up the KB dependecny _index
				final List<Branch> brList = branches.subList(lastBranch, branches.size());
				for (final Branch branch : brList)
					// remove from the dependency _index
					_abox.getKB().getDependencyIndex().removeBranchDependencies(branch);
				brList.clear();
			}
			else
				branches.subList(lastBranch, branches.size()).clear(); // old approach

			final Branch newBranch = branches.get(lastBranch - 1); // get the _branch to try

			_logger.fine(() -> "JUMP: Branch " + lastBranch);

			if (lastBranch != newBranch.getBranchIndexInABox())
				throw new InternalReasonerException("Backtrack: Trying to backtrack to _branch " + lastBranch + " but got " + newBranch.getBranchIndexInABox());

			if (newBranch.getTryNext() < newBranch.getTryCount()) // set the last clash before restore
				newBranch.setLastClash(_abox.getClash().getDepends());

			newBranch.setTryNext(newBranch.getTryNext() + 1); // increment the counter

			if (newBranch.getTryNext() < newBranch.getTryCount()) // no need to restore this _branch if we exhausted possibilities
				restore(newBranch); // undo the changes done after this _branch

			branchFound = newBranch.tryNext(); // try the next possibility

			if (!branchFound)
				_logger.fine(() -> "FAIL: Branch " + lastBranch);
		}

		return branchFound;
	}

	@Override
	public void complete(final Expressivity expr)
	{
		initialize(expr);

		while (!_abox.isComplete())
		{
			while (_abox.isChanged() && !_abox.isClosed())
			{
				_completionTimer.ifPresent(t -> t.check());

				_abox.setChanged(false);

				if (_logger.isLoggable(Level.FINE))
				{
					_logger.fine("Branch: " + _abox.getBranchIndex() + ", Depth: " + _abox.getStats()._treeDepth + ", Size: " + _abox.getNodes().size() + ", Mem: " + Runtime.getRuntime().freeMemory() / 1000 + "kb");
					_abox.validate();
					printBlocked();
					_abox.printTree();
				}

				final IndividualIterator i = OpenlletOptions.USE_COMPLETION_QUEUE ? _abox.getCompletionQueue() : _abox.getIndIterator();

				// flush the _queue
				if (OpenlletOptions.USE_COMPLETION_QUEUE)
					_abox.getCompletionQueue().flushQueue();

				for (final TableauRule tableauRule : _tableauRules)
				{
					final boolean closed = tableauRule.apply(i);
					if (closed)
						break;
				}

				// it could be the case that there was a clash and we had a
				// deletion update that retracted it
				// however there could have been some thing on the _queue that
				// still needed to be refired from backtracking
				// so onle set that the _abox is clash free after we have applied
				// all the rules once
				if (OpenlletOptions.USE_COMPLETION_QUEUE)
					_abox.getCompletionQueue().setClosed(_abox.isClosed());
			}

			if (_abox.isClosed())
			{
				if (_logger.isLoggable(Level.FINE))
					_logger.fine("Clash at Branch (" + _abox.getBranchIndex() + ") " + _abox.getClash());

				if (backtrack())
				{
					_abox.setClash(null);

					if (OpenlletOptions.USE_COMPLETION_QUEUE)
						_abox.getCompletionQueue().setClosed(false);
				}
				else
				{
					_abox.setComplete(true);

					if (OpenlletOptions.USE_COMPLETION_QUEUE) // we need to flush the _queue to add the other elements
						_abox.getCompletionQueue().flushQueue();
				}
			}
			else
				if (OpenlletOptions.SATURATE_TABLEAU)
				{
					Branch unexploredBranch = null;
					for (int i = _abox.getBranches().size() - 1; i >= 0; i--)
					{
						unexploredBranch = _abox.getBranches().get(i);
						unexploredBranch.setTryNext(unexploredBranch.getTryNext() + 1);
						if (unexploredBranch.getTryNext() < unexploredBranch.getTryCount())
						{
							restore(unexploredBranch);
							System.out.println("restoring _branch " + unexploredBranch.getBranchIndexInABox() + " _tryNext = " + unexploredBranch.getTryNext() + " _tryCount = " + unexploredBranch.getTryCount());
							unexploredBranch.tryNext();
							break;
						}
						else
						{
							System.out.println("removing _branch " + unexploredBranch.getBranchIndexInABox());
							_abox.getBranches().remove(i);
							unexploredBranch = null;
						}
					}
					if (unexploredBranch == null)
						_abox.setComplete(true);
				}
				else
					_abox.setComplete(true);
		}

	}

}
