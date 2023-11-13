// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.branch;

import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.OpenlletOptions;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.ABoxForBranch;
import openllet.core.boxes.abox.Clash;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.rbox.Role;
import openllet.core.tableau.completion.CompletionStrategy;
import openllet.core.utils.ATermUtils;

/**
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren sirin
 */
public class GuessBranch extends IndividualBranch
{
	private final Role _r;
	private final int _minGuess;
	private final ATermAppl _qualification;

	public GuessBranch(final ABoxForBranch abox, final CompletionStrategy strategy, final Individual x, //
			final Role r, final int minGuess, final int maxGuess, //
			final ATermAppl q, final DependencySet ds)
	{
		super(abox, strategy, x, ds, maxGuess - minGuess + 1);

		_r = r;
		_minGuess = minGuess;
		_qualification = q;
	}

	public GuessBranch(final ABox abox, final GuessBranch gb)
	{
		super(abox, gb, gb._minGuess + gb.getTryCount() /*- 1*/ - gb._minGuess /*+ 1*/);

		_r = gb._r;
		_minGuess = gb._minGuess;
		_qualification = gb._qualification;

		_ind = abox.getIndividual(_ind.getName()); // FIXME : see MaxBranch
	}

	@Override
	public IndividualBranch copyTo(final ABox abox)
	{
		return new GuessBranch(abox, this);
	}

	@Override
	protected void tryBranch()
	{
		_abox.incrementBranch();

		DependencySet ds = getTermDepends();
		for (; getTryNext() < getTryCount(); _tryNext++)
		{
			// start with max possibility and decrement at each try
			final int n = _minGuess + getTryCount() - getTryNext() - 1;

			_logger.fine(() -> "GUES: (" + (getTryNext() + 1) + "/" + getTryCount() + ") at _branch (" + getBranchIndexInABox() + ") to  " + _ind + " -> " + _r + " -> anon" + (n == 1 ? "" : _abox.getAnonCount() + 1 + " - anon") + (_abox.getAnonCount() + n));

			ds = ds.union(new DependencySet(getBranchIndexInABox()), _abox.doExplanation());

			// add the min cardinality restriction just to make early clash detection easier
			_strategy.addType(_ind, ATermUtils.makeMin(_r.getName(), n, _qualification), ds);

			// add the max cardinality for guess
			_strategy.addType(_ind, ATermUtils.makeNormalizedMax(_r.getName(), n, _qualification), ds);

			// create n distinct nominal successors
			final Individual[] y = new Individual[n];
			for (int c1 = 0; c1 < n; c1++)
			{
				y[c1] = _strategy.createFreshIndividual(null, ds);

				_strategy.addEdge(_ind, _r, y[c1], ds);
				y[c1] = y[c1].getSame();
				_strategy.addType(y[c1], _qualification, ds);
				y[c1] = y[c1].getSame();
				for (int c2 = 0; c2 < c1; c2++)
					y[c1].setDifferent(y[c2], ds);
			}

			final boolean earlyClash = _abox.isClosed();
			if (earlyClash)
			{
				_logger.fine(() -> "CLASH: Branch " + getBranchIndexInABox() + " " + _abox.getClash() + "!");

				final DependencySet clashDepends = _abox.getClash().getDepends();

				if (clashDepends.contains(getBranchIndexInABox()))
				{
					// we need a global restore here because the merge operation modified three
					// different _nodes and possibly other global variables
					_strategy.restore(this);

					// global restore sets the _branch number to previous value so we need to
					// increment it again
					_abox.incrementBranch();

					setLastClash(clashDepends);
				}
				else
					return;
			}
			else
				return;
		}

		ds = getCombinedClash();

		//CHW - removed for rollback through deletions
		if (!OpenlletOptions.USE_INCREMENTAL_DELETION)
			ds.remove(getBranchIndexInABox());

		_abox.setClash(Clash.unexplained(_ind, ds));

		return;
	}

	@Override
	public String toString()
	{
		if (getTryNext() < getTryCount())
			return "Branch " + getBranchIndexInABox() + " guess rule on " + _ind + " for role  " + _r;

		return "Branch " + getBranchIndexInABox() + " guess rule on " + _ind + " for role  " + _r + " exhausted merge possibilities";
	}

	/**
	 * Added for to re-open closed branches. This is needed for incremental reasoning through deletions Currently this method does nothing as we cannot support
	 * incremental reasoning when both nominals and inverses are used - this is the only case when the guess rule is needed.
	 *
	 * @param openIndex The shift _index
	 */
	@Override
	public void shiftTryNext(final int openIndex)
	{
		//decrement trynext
		//_tryNext--;
	}
}
