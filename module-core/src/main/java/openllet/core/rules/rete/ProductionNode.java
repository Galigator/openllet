// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules.rete;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.boxes.abox.ABoxForIndividual;
import openllet.core.boxes.abox.Clash;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.Literal;
import openllet.core.boxes.abox.Node;
import openllet.core.boxes.rbox.Role;
import openllet.core.rules.ContinuousRulesStrategy;
import openllet.core.rules.PartialBinding;
import openllet.core.rules.VariableBinding;
import openllet.core.rules.model.AtomDVariable;
import openllet.core.rules.model.AtomIVariable;
import openllet.core.rules.model.AtomObject;
import openllet.core.rules.model.AtomVariable;
import openllet.core.rules.model.Rule;
import openllet.core.utils.ATermUtils;

/**
 * @author Evren Sirin
 */
public abstract class ProductionNode extends BetaNode
{
	protected final ContinuousRulesStrategy _strategy;
	protected Set<ATermAppl> _explain;
	protected DependencySet _ds;

	public ProductionNode(final ContinuousRulesStrategy strategy, final Set<ATermAppl> explain)
	{
		_strategy = strategy;
		_explain = explain;
	}

	@Override
	public void activate(final WME wme)
	{
		throw new UnsupportedOperationException();
	}

	protected void resetDependencySet(final Token token)
	{
		final boolean doExplanation = _strategy.getABox().doExplanation();
		_ds = token.getDepends(doExplanation);
		if (doExplanation)
			_ds = _ds.union(_explain, doExplanation);
	}

	protected Node getNode(final NodeProvider provider, final Token token)
	{
		Node node = provider.getNode(null, token);
		if (node.isMerged())
		{
			final boolean doExplanation = _strategy.getABox().doExplanation();
			_ds = _ds.union(node.getMergeDependency(true), doExplanation);
			node = node.getSame();
		}
		return node;
	}

	@Override
	public void print(final String indent)
	{
		System.out.print(indent);
		System.out.print("  ");
		System.out.println(this);
	}

	public static class Inconsistency extends ProductionNode
	{
		public Inconsistency(final ContinuousRulesStrategy strategy, final Set<ATermAppl> explain)
		{
			super(strategy, explain);
		}

		@Override
		public void activate(final Token token)
		{
			resetDependencySet(token);
			_strategy.getABox().setClash(Clash.unexplained(null, _ds));
		}

		@Override
		public String toString()
		{
			return "Produce[clash]";
		}
	}

	public static class Type extends ProductionNode
	{
		private final NodeProvider _subject;
		private final ATermAppl _type;

		public Type(final ContinuousRulesStrategy strategy, final Set<ATermAppl> explain, final NodeProvider subject, final ATermAppl type)
		{
			super(strategy, explain);
			_explain = explain;
			_subject = subject;
			_type = type;
		}

		@Override
		public void activate(final Token token)
		{
			resetDependencySet(token);
			final Node s = getNode(_subject, token);
			if (!s.hasType(_type))
				_strategy.addType(s, _type, _ds);
		}

		@Override
		public String toString()
		{
			return "Produce[" + ATermUtils.toString(_type) + "(" + _subject + ")]";
		}
	}

	private static abstract class Binary extends ProductionNode
	{
		protected final NodeProvider _subject;
		protected final NodeProvider _object;

		public Binary(final ContinuousRulesStrategy strategy, final Set<ATermAppl> explain, final NodeProvider subject, final NodeProvider object)
		{
			super(strategy, explain);
			_explain = explain;
			_subject = subject;
			_object = object;
		}
	}

	public static class Edge extends Binary
	{
		private final Role _role;

		public Edge(final ContinuousRulesStrategy strategy, final Set<ATermAppl> explain, final NodeProvider subject, final Role role, final NodeProvider object)
		{
			super(strategy, explain, subject, object);
			_role = role;
		}

		@Override
		public void activate(final Token token)
		{
			resetDependencySet(token);
			final Node s = getNode(_subject, token);
			final Node o = getNode(_object, token);
			final Object edge = _strategy.addEdge((Individual) s, _role, o, _ds);
			_logger.fine(() -> "Produce edge " + token + " -> " + edge);
		}

		@Override
		public String toString()
		{
			return "Produce[" + ATermUtils.toString(_role.getName()) + "(" + _subject + ", " + _object + ")]";
		}
	}

	public static class SameAs extends Binary
	{
		public SameAs(final ContinuousRulesStrategy strategy, final Set<ATermAppl> explain, final NodeProvider subject, final NodeProvider object)
		{
			super(strategy, explain, subject, object);
		}

		@Override
		public void activate(final Token token)
		{
			resetDependencySet(token);
			final Node s = getNode(_subject, token);
			final Node o = getNode(_object, token);
			_strategy.mergeTo(s, o, _ds);
		}

		@Override
		public String toString()
		{
			return "Produce[SameAs(" + _subject + ", " + _object + ")]";
		}
	}

	public static class DiffFrom extends Binary
	{
		public DiffFrom(final ContinuousRulesStrategy strategy, final Set<ATermAppl> explain, final NodeProvider subject, final NodeProvider object)
		{
			super(strategy, explain, subject, object);
		}

		@Override
		public void activate(final Token token)
		{
			resetDependencySet(token);
			final Node s = getNode(_subject, token);
			final Node o = getNode(_object, token);
			_strategy.setDifferent(s, o, _ds);
		}

		@Override
		public String toString()
		{
			return "Produce[DiffFrom(" + _subject + ", " + _object + ")]";
		}
	}

	public static class ProduceBinding extends ProductionNode
	{
		private final Rule _rule;
		private final Map<AtomVariable, NodeProvider> _args;

		public ProduceBinding(final ContinuousRulesStrategy strategy, final Set<ATermAppl> explain, final Rule rule, final Map<AtomVariable, NodeProvider> args)
		{
			super(strategy, explain);
			_rule = rule;
			_args = args;
		}

		@Override
		public void activate(final Token token)
		{
			resetDependencySet(token);
			final VariableBinding binding = new VariableBinding((ABoxForIndividual) _strategy.getABox());
			for (final Entry<AtomVariable, NodeProvider> entry : _args.entrySet())
			{
				final AtomObject arg = entry.getKey();
				final Node node = getNode(_args.get(arg), token);
				if (arg instanceof AtomIVariable)
					binding.set((AtomIVariable) arg, (Individual) node);
				else
					binding.set((AtomDVariable) arg, (Literal) node);
			}
			_logger.fine(() -> "Produce binding " + _rule + " -> " + binding);
			_strategy.addPartialBinding(new PartialBinding(_rule, binding, _ds));
		}

		@Override
		public String toString()
		{
			return "Produce[Binding(" + _args + ")]";
		}
	}
}
