// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import openllet.aterm.ATermAppl;
import openllet.atom.OpenError;
import openllet.core.DependencySet;
import openllet.core.OpenlletOptions;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.ABoxForIndividual;
import openllet.core.boxes.abox.Clash;
import openllet.core.boxes.abox.Edge;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.IndividualIterator;
import openllet.core.boxes.abox.Node;
import openllet.core.boxes.rbox.Role;
import openllet.core.expressivity.Expressivity;
import openllet.core.rules.model.Rule;
import openllet.core.rules.model.RuleAtom;
import openllet.core.rules.rete.AlphaNetwork;
import openllet.core.rules.rete.Compiler;
import openllet.core.rules.rete.Interpreter;
import openllet.core.tableau.branch.Branch;
import openllet.core.tableau.branch.RuleBranch;
import openllet.core.tableau.completion.SROIQStrategy;
import openllet.core.tableau.completion.rule.TableauRule;
import openllet.core.utils.Pair;
import openllet.core.utils.Timer;

public class ContinuousRulesStrategy extends SROIQStrategy
{
	private final BindingGeneratorStrategy _bindingStrategy;
	private Interpreter _interpreter;
	private boolean _merging;
	private final Set<PartialBinding> _unsafeRules;
	private final Queue<PartialBinding> _partialBindings;
	private final Map<Pair<Rule, VariableBinding>, Integer> _rulesApplied;
	private final RulesToATermTranslator _atermTranslator;
	private final RuleAtomAsserter _ruleAtomAsserter;
	private final TrivialSatisfactionHelpers _atomTester;

	public ContinuousRulesStrategy(final ABox abox)
	{
		super(abox);
		_bindingStrategy = new BindingGeneratorStrategyImpl(abox);
		_partialBindings = new ConcurrentLinkedQueue<>();
		_unsafeRules = new HashSet<>();
		_rulesApplied = new HashMap<>();
		_atermTranslator = new RulesToATermTranslator();
		_ruleAtomAsserter = new RuleAtomAsserter();
		_atomTester = new TrivialSatisfactionHelpers(abox);
	}

	public void addUnsafeRule(final Rule rule, final Set<ATermAppl> explain)
	{
		_unsafeRules.add(new PartialBinding(rule, new VariableBinding((ABoxForIndividual) _abox), new DependencySet(explain)));
	}

	public void addPartialBinding(final PartialBinding binding)
	{
		_partialBindings.add(binding);
	}

	@Override
	public Edge addEdge(final Individual subj, final Role pred, final Node obj, final DependencySet ds)
	{
		final Edge edge = super.addEdge(subj, pred, obj, ds);

		if (edge != null && !_abox.isClosed() && subj.isRootNominal() && obj.isRootNominal())
			if (_interpreter != null)
				_interpreter._alphaNet.activateEdge(edge);

		return edge;
	}

	@Override
	public void addType(final Node node, final ATermAppl c, final DependencySet ds)
	{
		super.addType(node, c, ds);

		if (!_merging && !_abox.isClosed() && node.isRootNominal() && _interpreter != null && node.isIndividual())
		{
			final Individual ind = (Individual) node;
			_interpreter._alphaNet.activateType(ind, c, ds);
		}
	}

	@Override
	protected boolean mergeIndividuals(final Individual y, final Individual x, final DependencySet ds)
	{
		if (super.mergeIndividuals(y, x, ds))
		{
			if (_interpreter != null)
				_interpreter._alphaNet.activateDifferents(y);
			return true;
		}
		return false;
	}

	@Override
	public boolean setDifferent(final Node y, final Node z, final DependencySet ds)
	{
		if (super.setDifferent(y, z, ds))
		{
			if (_interpreter != null && !_merging && !_abox.isClosed() && y.isRootNominal() && y.isIndividual() && z.isRootNominal() && z.isIndividual())
				_interpreter._alphaNet.activateDifferent((Individual) y, (Individual) z, ds);

			return true;
		}

		return false;
	}

	public void applyRete()
	{
		Optional<Timer> timer;
		if (OpenlletOptions.ALWAYS_REBUILD_RETE)
		{
			timer = _timers.startTimer("rule-rebuildRete");

			_partialBindings.clear();
			_partialBindings.addAll(_unsafeRules);
			_interpreter.reset();
			timer.ifPresent(Timer::stop);
		}

		timer = _timers.startTimer("rule-reteRun");
		_interpreter.run();
		timer.ifPresent(Timer::stop);
	}

	public void applyRuleBindings()
	{
		int total = 0;

		for (final PartialBinding ruleBinding : _partialBindings)
		{
			final Rule rule = ruleBinding.getRule();
			final VariableBinding initial = ruleBinding.getBinding();

			for (final VariableBinding binding : _bindingStrategy.createGenerator(rule, initial))
			{

				final Pair<Rule, VariableBinding> ruleKey = new Pair<>(rule, binding);
				if (!_rulesApplied.containsKey(ruleKey))
				{
					total++;

					if (_logger.isLoggable(Level.FINE))
						_logger.fine("Rule: " + rule + "\nBinding: " + binding + "\ntotal:" + total);

					final int branch = createDisjunctionsFromBinding(binding, rule, ruleBinding.getDependencySet());

					if (branch >= 0)
						_rulesApplied.put(ruleKey, branch);

					if (_abox.isClosed())
						return;
				}
			}

		}
	}

	@Override
	public void complete(final Expressivity expr)
	{
		initialize(_abox.getKB().getExpressivity());

		_merging = false;
		final Optional<Timer> timer = _timers.startTimer("rule-buildReteRules");
		final Compiler compiler = new Compiler(this);
		for (final Entry<Rule, Rule> e : _abox.getKB().getNormalizedRules().entrySet())
		{
			final Rule rule = e.getKey();
			final Rule normalizedRule = e.getValue();

			if (normalizedRule == null)
				continue;

			final Set<ATermAppl> explain = _abox.doExplanation() ? rule.getExplanation(_atermTranslator) : Collections.<ATermAppl> emptySet();

			try
			{
				compiler.compile(normalizedRule, explain);
			}
			catch (final UnsupportedOperationException uoe)
			{
				throw new OpenError("Unsupported rule " + normalizedRule, uoe);
			}
		}
		timer.ifPresent(Timer::stop);

		final AlphaNetwork alphaNet = compiler.getAlphaNet();
		if (_abox.doExplanation())
			alphaNet.setDoExplanation(true);
		_interpreter = new Interpreter(alphaNet);
		_partialBindings.clear();
		_partialBindings.addAll(_unsafeRules);
		_rulesApplied.clear();

		//		t.stop();

		//		t = _timers.startTimer( "rule-compileReteFacts" );
		applyRete();
		//		t.stop();

		while (!_abox.isComplete())
		{
			while (_abox.isChanged() && !_abox.isClosed())
			{
				_completionTimer.ifPresent(Timer::check);

				_abox.setChanged(false);

				if (_logger.isLoggable(Level.FINE))
				{
					_logger.fine("Branch: " + _abox.getBranchIndex()//
							+ ", Depth: " + _abox.getStats()._treeDepth//
							+ ", Size: " + _abox.getNodes().size()//
							+ ", Mem: " + Runtime.getRuntime().freeMemory() / 1000 + "kb");
					_abox.validate();
					// printBlocked();
					_abox.printTree();
					_interpreter._alphaNet.print();
				}

				final IndividualIterator i = _abox.getIndIterator();

				for (final TableauRule tableauRule : _tableauRules)
				{
					final boolean closed = tableauRule.apply(i);
					if (closed)
						break;
				}

				if (_abox.isClosed())
					break;

				if (!_abox.isChanged() && !_partialBindings.isEmpty())
				{
					//					t = _timers.startTimer( "rule-bindings" );
					applyRuleBindings();
					//					t.stop();
					if (_abox.isClosed())
						break;
				}

			}

			if (_abox.isClosed())
			{
				if (_logger.isLoggable(Level.FINE))
					_logger.fine("Clash at Branch (" + _abox.getBranchIndex() + ") " + _abox.getClash());

				if (backtrack())
					_abox.setClash(null);
				else
					_abox.setComplete(true);
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

	private int createDisjunctionsFromBinding(final VariableBinding binding, final Rule rule, final DependencySet dsParam)
	{
		DependencySet ds = dsParam;
		final List<RuleAtom> atoms = new ArrayList<>();

		for (final RuleAtom atom : rule.getBody())
		{
			final DependencySet atomDS = _atomTester.isAtomTrue(atom, binding);
			if (atomDS != null)
				ds = ds.union(atomDS, _abox.doExplanation());
			else
				atoms.add(atom);
		}

		// all the atoms in the body are true
		if (atoms.isEmpty())
		{
			if (rule.getHead().isEmpty())
			{
				if (_logger.isLoggable(Level.FINE))
					_logger.fine("Empty head for rule " + rule);
				_abox.setClash(Clash.unexplained(null, ds));
			}
			else
				for (final RuleAtom atom : rule.getHead())
					_ruleAtomAsserter.assertAtom(atom, binding, ds, false, _abox, this);
			return -1;
		}

		final int bodyAtomCount = atoms.size();

		for (final RuleAtom atom : rule.getHead())
		{
			final DependencySet atomDS = _atomTester.isAtomTrue(atom, binding);
			if (atomDS == null)
				atoms.add(atom);
		}

		// all no head atoms are added to the list they are all true (unless there were no head atoms to begin with) which means there is nothing to be done
		if (atoms.size() == bodyAtomCount && !rule.getHead().isEmpty())
			return -1;
		else
			if (atoms.size() == 1)
			{
				_ruleAtomAsserter.assertAtom(atoms.get(0), binding, ds, true, _abox, this);
				return -1;
			}
			else
			{
				final Branch r;
				synchronized (_abox)
				{
					addBranch(r = new RuleBranch(_abox, this, _ruleAtomAsserter, atoms, binding, bodyAtomCount, ds));
				}
				r.tryNext();
				return r.getBranchIndexInABox();
			}
	}

	@Override
	public void mergeTo(final Node y, final Node z, final DependencySet ds)
	{
		_merging = true;
		super.mergeTo(y, z, ds);
		if (!_abox.isClosed() && _interpreter != null && (y.isRootNominal() || z.isRootNominal()))
		{
			//			if( y.isRootNominal() )
			//				runRules |= interpreter.removeMentions( y.getTerm() );
			//			if( z.isIndividual() )
			//				runRules |= interpreter.rete.processIndividual( (Individual) z );
		}
		_merging = false;
	}

	@Override
	public void restore(final Branch branch)
	{
		super.restore(branch);
		restoreRules(branch);
	}

	@Override
	public void restoreLocal(final Individual ind, final Branch branch)
	{
		super.restoreLocal(ind, branch);
		restoreRules(branch);
	}

	private void restoreRules(final Branch branch)
	{
		@SuppressWarnings("unused")
		int total = 0;
		for (final Iterator<Map.Entry<Pair<Rule, VariableBinding>, Integer>> ruleAppIter = _rulesApplied.entrySet().iterator(); ruleAppIter.hasNext();)
		{
			final Map.Entry<Pair<Rule, VariableBinding>, Integer> ruleBranchEntry = ruleAppIter.next();
			if (ruleBranchEntry.getValue() > branch.getBranchIndexInABox())
			{
				// System.out.println( "Removing " + ruleBranchEntry.getKey() );
				ruleAppIter.remove();
				total++;
			}
		}

		for (final Iterator<PartialBinding> iter = _partialBindings.iterator(); iter.hasNext();)
		{
			final PartialBinding binding = iter.next();
			if (binding.getBranch() > branch.getBranchIndexInABox())
				iter.remove();
		}

		_interpreter.restore(branch.getBranchIndexInABox());
		// rebuildFacts = true;
	}
}
