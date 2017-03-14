// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC.
// <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms
// of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.branch;

import java.util.List;
import java.util.logging.Level;
import openllet.core.DependencySet;
import openllet.core.OpenlletOptions;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.Clash;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.Node;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.rules.RuleAtomAsserter;
import openllet.core.rules.VariableBinding;
import openllet.core.rules.model.AtomIObject;
import openllet.core.rules.model.BinaryAtom;
import openllet.core.rules.model.RuleAtom;
import openllet.core.rules.model.UnaryAtom;
import openllet.core.tableau.completion.CompletionStrategy;

public class RuleBranch extends Branch
{
	private final RuleAtomAsserter _ruleAtomAsserter;
	private final VariableBinding _binding;
	private final List<RuleAtom> _atoms;
	private final int _bodyAtomCount;
	private final int[] _order;
	private final DependencySet[] _prevDS;

	public RuleBranch(final ABox abox, final CompletionStrategy completion, final RuleAtomAsserter ruleAtomAsserter, //
			final List<RuleAtom> atoms, final VariableBinding binding, final int bodyAtomCount, //
			final DependencySet ds)
	{
		super(abox, completion, ds, atoms.size());

		_ruleAtomAsserter = ruleAtomAsserter;
		_atoms = atoms;
		_bodyAtomCount = bodyAtomCount;
		_binding = binding;

		_prevDS = new DependencySet[atoms.size()];

		_order = new int[atoms.size()];
		for (int i = 0; i < _order.length; i++)
			_order[i] = i;
	}

	public RuleBranch(final RuleBranch rb, final ABox abox)
	{
		super(abox, rb._atoms.size(), rb);

		_ruleAtomAsserter = rb._ruleAtomAsserter;
		_atoms = rb._atoms;
		_binding = rb._binding;
		_bodyAtomCount = rb._bodyAtomCount;

		_prevDS = new DependencySet[rb._prevDS.length];

		System.arraycopy(rb._prevDS, 0, _prevDS, 0, rb._tryNext);
		_order = new int[rb._order.length];
		System.arraycopy(rb._order, 0, _order, 0, rb._order.length);
	}

	@Override
	public Node getNode()
	{
		return null;
	}

	@Deprecated
	@Override
	public RuleBranch copyTo(final ABox abox)
	{
		return new RuleBranch(this, abox);
		//
		//		final RuleBranch b = new RuleBranch(abox, _strategy, _ruleAtomAsserter, _atoms, _binding, _bodyAtomCount, getTermDepends());
		//
		//		b.setAnonCount(getAnonCount());
		//		b.setNodeCount(_nodeCount);
		//		b.setBranchIndexInABox(getBranchIndexInABox());
		//		b.setTryNext(_tryNext);
		//		b._prevDS = new DependencySet[_prevDS.length];
		//		System.arraycopy(_prevDS, 0, b._prevDS, 0, _tryNext);
		//		b._order = new int[_order.length];
		//		System.arraycopy(_order, 0, b._order, 0, _order.length);
		//
		//		return b;
	}

	@Override
	public void setLastClash(final DependencySet ds)
	{
		super.setLastClash(ds);
		if (_tryNext >= 0)
			_prevDS[_tryNext] = ds;
	}

	@Override
	protected void tryBranch()
	{
		_abox.incrementBranch();

		for (; _tryNext < _tryCount; _tryNext++)
		{
			final RuleAtom atom = _atoms.get(_tryNext);

			DependencySet ds = null;
			if (_tryNext == _tryCount - 1 && !OpenlletOptions.SATURATE_TABLEAU)
			{
				ds = getTermDepends();

				for (int m = 0; m < _tryNext; m++)
					ds = ds.union(_prevDS[m], _abox.doExplanation());

				// CHW - added for incremental reasoning and rollback through deletions
				if (OpenlletOptions.USE_INCREMENTAL_DELETION)
					ds.setExplain(getTermDepends().getExplain());
				else
					ds.remove(getBranchIndexInABox());
			}
			else
				// CHW - Changed for tracing purposes
				if (OpenlletOptions.USE_INCREMENTAL_DELETION)
					ds = getTermDepends().union(new DependencySet(getBranchIndexInABox()), _abox.doExplanation());
				else
					ds = new DependencySet(getBranchIndexInABox());

			if (_logger.isLoggable(Level.FINE))
				_logger.fine("RULE: Branch (" + getBranchIndexInABox() + ") try (" + (_tryNext + 1) + "/" + _tryCount + ") " + atom + " " + _binding + " " + _atoms + " " + ds);

			_ruleAtomAsserter.assertAtom(atom, _binding, ds, _tryNext < _bodyAtomCount, _abox, _strategy);

			// if there is a clash
			if (_abox.isClosed())
			{
				final DependencySet clashDepends = _abox.getClash().getDepends();

				if (_logger.isLoggable(Level.FINE))
					_logger.fine("CLASH: Branch " + getBranchIndexInABox() + " " + Clash.unexplained(null, clashDepends) + "!");

				// do not restore if we do not have any more branches to try.
				// after backtrack the correct _branch will restore it anyway. more
				// importantly restore clears the clash info causing exceptions
				if (_tryNext < _tryCount - 1 && clashDepends.contains(getBranchIndexInABox()))
				{
					final AtomIObject obj = (AtomIObject) (atom instanceof UnaryAtom ? ((UnaryAtom<?>) atom).getArgument() : ((BinaryAtom<?, ?, ?>) atom).getArgument1());
					final Individual ind = _binding.get(obj);

					_strategy.restoreLocal(ind, this);

					// global restore sets the _branch number to previous
					// value so we need to
					// increment it again
					_abox.incrementBranch();

					setLastClash(clashDepends);
				}
				else
				{

					_abox.setClash(Clash.unexplained(null, clashDepends.union(ds, _abox.doExplanation())));

					// CHW - added for inc reasoning
					if (OpenlletOptions.USE_INCREMENTAL_DELETION)
						_abox.getKB().getDependencyIndex().addCloseBranchDependency(this, _abox.getClash().getDepends());

					return;
				}
			}
			else
				return;
		}

		// this code is not unreachable. if there are no branches left restore
		// does not call this
		// function, and the loop immediately returns when there are no branches
		// left in this _disjunction. If this exception is thrown it shows a bug in the code.
		throw new InternalReasonerException("This exception should not be thrown!");
	}

	/**
	 * Added for to re-open closed branches. This is needed for incremental reasoning through deletions Currently this method does nothing as we cannot support
	 * incremental reasoning when both rules are used in the KB
	 *
	 * @param openIndex The shift _index
	 */
	@Override
	public void shiftTryNext(final int openIndex)
	{
		//
	}

}
