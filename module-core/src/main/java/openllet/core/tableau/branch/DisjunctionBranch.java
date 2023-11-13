// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com
//
// ---
// Portions Copyright (c) 2003 Ron Alford, Mike Grove, Bijan Parsia, Evren Sirin
// Alford, Grove, Parsia, Sirin parts of this source code are available under the terms of the MIT License.
//
// The MIT License
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package openllet.core.tableau.branch;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.OpenlletOptions;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.ABoxForBranch;
import openllet.core.boxes.abox.Clash;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.Node;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.tableau.completion.CompletionStrategy;
import openllet.core.utils.ATermUtils;

public class DisjunctionBranch extends Branch
{
	protected final Node _node;
	protected final ATermAppl _disjunction;
	private volatile ATermAppl[] _allDisjonctions;
	protected volatile DependencySet[] _prevDS;
	protected volatile int[] _order;

	public DisjunctionBranch(final ABoxForBranch abox, final CompletionStrategy completion, final Node node, //
			final ATermAppl disjunction, final DependencySet ds, final ATermAppl[] disj)
	{
		super(abox, completion, ds, disj.length);

		_node = node;
		_disjunction = disjunction;
		setDisj(disj);
		_prevDS = new DependencySet[disj.length];
		_order = new int[disj.length];
		for (int i = 0; i < disj.length; i++)
			_order[i] = i;
	}

	public DisjunctionBranch(final DisjunctionBranch dr, final ABox abox)
	{
		super(abox, dr._allDisjonctions.length, dr);

		_node = abox.getNode(dr._node.getName());
		_disjunction = dr._disjunction;
		_allDisjonctions = dr._allDisjonctions;

		_prevDS = new DependencySet[dr._allDisjonctions.length];
		System.arraycopy(dr._prevDS, 0, _prevDS, 0, dr._allDisjonctions.length);

		_order = new int[dr._allDisjonctions.length];
		System.arraycopy(dr._order, 0, _order, 0, dr._allDisjonctions.length);
	}

	@Override
	public Node getNode()
	{
		return _node;
	}

	protected String getDebugMsg()
	{
		return "DISJ: Branch (" + getBranchIndexInABox() + ") try (" + (getTryNext() + 1) + "/" + getTryCount() + ") " + _node + " " + ATermUtils.toString(_allDisjonctions[getTryNext()]) + " " + ATermUtils.toString(_disjunction);
	}

	@Override
	public DisjunctionBranch copyTo(final ABox abox)
	{
		return new DisjunctionBranch(this, abox);
	}

	/**
	 * This function finds preferred disjuncts using different heuristics. 1) A common kind of axiom that exist in a lot of ontologies is in the form A = and(B,
	 * some(p, C)) which is absorbed into an axiom like sub(B, or(A, all(p, not(C))). For these disjunctions, we always prefer picking all(p, C) because it
	 * causes an immediate clash for the instances of A so there is no overhead. For non-instances of A, this builds better pseudo models
	 *
	 * @return
	 */
	private int preferredDisjunct()
	{
		if (_allDisjonctions.length != 2)
			return -1;

		if (ATermUtils.isPrimitive(_allDisjonctions[0]) && ATermUtils.isAllValues(_allDisjonctions[1]) && ATermUtils.isNot((ATermAppl) _allDisjonctions[1].getArgument(1)))
			return 1;

		if (ATermUtils.isPrimitive(_allDisjonctions[1]) && ATermUtils.isAllValues(_allDisjonctions[0]) && ATermUtils.isNot((ATermAppl) _allDisjonctions[0].getArgument(1)))
			return 0;

		return -1;
	}

	@Override
	public void setLastClash(final DependencySet ds)
	{
		super.setLastClash(ds);
		if (getTryNext() >= 0)
			_prevDS[getTryNext()] = ds;
	}

	@Override
	protected void tryBranch()
	{
		_abox.incrementBranch();

		int[] stats = null;
		if (OpenlletOptions.USE_DISJUNCT_SORTING)
		{
			stats = _abox.getDisjBranchStats().get(_disjunction);
			if (stats == null)
			{
				final int preference = preferredDisjunct();
				stats = new int[_allDisjonctions.length];
				for (int i = 0; i < _allDisjonctions.length; i++)
					stats[i] = i != preference ? 0 : Integer.MIN_VALUE;
				_abox.getDisjBranchStats().put(_disjunction, stats);
			}
			if (getTryNext() > 0)
				stats[_order[getTryNext() - 1]]++;

			int minIndex = getTryNext();
			int minValue = stats[getTryNext()];
			for (int i = getTryNext() + 1; i < stats.length; i++)
			{
				final boolean tryEarlier = stats[i] < minValue;

				if (tryEarlier)
				{
					minIndex = i;
					minValue = stats[i];
				}
			}
			if (minIndex != getTryNext())
			{
				final ATermAppl selDisj = _allDisjonctions[minIndex];
				_allDisjonctions[minIndex] = _allDisjonctions[getTryNext()];
				_allDisjonctions[getTryNext()] = selDisj;
				_order[minIndex] = getTryNext();
				_order[getTryNext()] = minIndex;
			}
		}

		final Node node = _node.getSame();

		for (; getTryNext() < getTryCount(); _tryNext++)
		{
			final ATermAppl d = _allDisjonctions[getTryNext()];

			if (OpenlletOptions.USE_SEMANTIC_BRANCHING)
				for (int m = 0; m < getTryNext(); m++)
					_strategy.addType(node, ATermUtils.negate(_allDisjonctions[m]), _prevDS[m]);

			DependencySet ds = null;
			if (getTryNext() == getTryCount() - 1 && !OpenlletOptions.SATURATE_TABLEAU)
			{
				ds = getTermDepends();
				for (int m = 0; m < getTryNext(); m++)
					ds = ds.union(_prevDS[m], _abox.doExplanation());

				//CHW - added for incremental reasoning and rollback through deletions
				if (OpenlletOptions.USE_INCREMENTAL_DELETION)
					ds.setExplain(getTermDepends().getExplain());
				else
					ds.remove(getBranchIndexInABox());
			}
			else
				//CHW - Changed for tracing purposes
				if (OpenlletOptions.USE_INCREMENTAL_DELETION)
					ds = getTermDepends().union(new DependencySet(getBranchIndexInABox()), _abox.doExplanation());
				else
				{
					ds = new DependencySet(getBranchIndexInABox());
					//added for tracing
					final Set<ATermAppl> explain = new HashSet<>();
					explain.addAll(getTermDepends().getExplain());
					ds.setExplain(explain);
				}

			if (_logger.isLoggable(Level.FINE))
				_logger.fine(getDebugMsg());

			final ATermAppl notD = ATermUtils.negate(d);
			DependencySet clashDepends = OpenlletOptions.SATURATE_TABLEAU ? null : node.getDepends(notD);
			if (clashDepends == null)
			{
				_strategy.addType(node, d, ds);
				// we may still find a clash if concept is allValuesFrom
				// and there are some conflicting edges
				if (_abox.isClosed())
					clashDepends = _abox.getClash().getDepends();
			}
			else
				clashDepends = clashDepends.union(ds, _abox.doExplanation());

			// if there is a clash
			if (clashDepends != null)
			{
				if (_logger.isLoggable(Level.FINE))
				{
					final Clash clash = _abox.isClosed() ? _abox.getClash() : Clash.atomic(node, clashDepends, d);
					_logger.fine("CLASH: Branch " + getBranchIndexInABox() + " " + clash + "!" + " " + clashDepends.getExplain());
				}

				if (OpenlletOptions.USE_DISJUNCT_SORTING)
				{
					if (stats == null)
					{
						stats = new int[_allDisjonctions.length];
						for (int i = 0; i < _allDisjonctions.length; i++)
							stats[i] = 0;
						_abox.getDisjBranchStats().put(_disjunction, stats);
					}
					stats[_order[getTryNext()]]++;
				}

				// do not restore if we do not have any more branches to try. after
				// backtrack the correct _branch will restore it anyway. more
				// importantly restore clears the clash info causing exceptions
				if (getTryNext() < getTryCount() - 1 && clashDepends.contains(getBranchIndexInABox()))
				{
					// do not restore if we find the problem without adding the concepts
					if (_abox.isClosed())
						if (node.isLiteral())
						{
							_abox.setClash(null);

							node.restore(getBranchIndexInABox());
						}
						else
						{
							// restoring a single _node is not enough here because one of the disjuncts could be an
							// all(r,C) that changed the r-neighbors
							_strategy.restoreLocal((Individual) node, this);

							// global restore sets the _branch number to previous value so we need to
							// increment it again
							_abox.incrementBranch();
						}

					setLastClash(clashDepends);
				}
				else
				{
					// set the clash only if we are returning from the function
					if (_abox.doExplanation())
					{
						final ATermAppl positive = ATermUtils.isNot(notD) ? d : notD;
						_abox.setClash(Clash.atomic(node, clashDepends.union(ds, _abox.doExplanation()), positive));
					}
					else
						_abox.setClash(Clash.atomic(node, clashDepends.union(ds, _abox.doExplanation())));

					//CHW - added for inc reasoning
					if (OpenlletOptions.USE_INCREMENTAL_DELETION)
						_abox.getKB().getDependencyIndex().addCloseBranchDependency(this, _abox.getClash().getDepends());

					return;
				}
			}
			else
				return;
		}

		// this code is not unreachable. if there are no branches left restore does not call this
		// function, and the loop immediately returns when there are no branches left in this
		// _disjunction. If this exception is thrown it shows a bug in the code.
		throw new InternalReasonerException("This exception should not be thrown!");
	}

	/**
	 * Added for to re-open closed branches. This is needed for incremental reasoning through deletions
	 *
	 * @param openIndex The shift _index
	 */
	@Override
	public void shiftTryNext(final int openIndex)
	{
		//save vals
		//		int ord = _order[openIndex];
		final ATermAppl dis = _allDisjonctions[openIndex];
		//		DependencySet preDS = _prevDS[openIndex];

		//TODO: also need to handle semantic branching
		if (OpenlletOptions.USE_SEMANTIC_BRANCHING)
		{
			//			if(this.ind.getDepends(ATermUtils.makeNot(dis)) != null){
			//				//check if the depedency is the same as preDS - if so, then we know that we added it
			//			}
		}

		//need to shift both _prevDS and next and _order
		//disjfirst
		for (int i = openIndex; i < _allDisjonctions.length - 1; i++)
		{
			_allDisjonctions[i] = _allDisjonctions[i + 1];
			_prevDS[i] = _prevDS[i + 1];
			_order[i] = _order[i];
		}

		//move open label to _end
		_allDisjonctions[_allDisjonctions.length - 1] = dis;
		_prevDS[_allDisjonctions.length - 1] = null;
		_order[_allDisjonctions.length - 1] = _allDisjonctions.length - 1;

		//decrement trynext
		setTryNext(getTryNext() - 1);
	}

	public void printLong()
	{
		for (int i = 0; i < _allDisjonctions.length; i++)
		{
			System.out.println("Disj[" + i + "] " + _allDisjonctions[i]);
			System.out.println("prevDS[" + i + "] " + _prevDS[i]);
			System.out.println("order[" + i + "] " + _order[i]);
		}

		//decrement trynext
		System.out.println("trynext: " + getTryNext());
	}

	/**
	 * @param disj the _disj to set
	 */
	public void setDisj(final ATermAppl[] disj)
	{
		_allDisjonctions = disj;
	}

	/**
	 * @param i
	 * @return the disj
	 */
	public ATermAppl getDisjunct(final int i)
	{
		return _allDisjonctions[i];
	}

}
