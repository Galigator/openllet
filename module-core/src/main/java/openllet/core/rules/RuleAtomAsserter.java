// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules;

import static openllet.core.utils.TermFactory.all;
import static openllet.core.utils.TermFactory.not;
import static openllet.core.utils.TermFactory.value;

import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.Node;
import openllet.core.rules.model.BuiltInAtom;
import openllet.core.rules.model.ClassAtom;
import openllet.core.rules.model.DataRangeAtom;
import openllet.core.rules.model.DatavaluedPropertyAtom;
import openllet.core.rules.model.DifferentIndividualsAtom;
import openllet.core.rules.model.IndividualPropertyAtom;
import openllet.core.rules.model.RuleAtom;
import openllet.core.rules.model.RuleAtomVisitor;
import openllet.core.rules.model.SameIndividualAtom;
import openllet.core.tableau.completion.CompletionStrategy;
import openllet.core.utils.ATermUtils;

/**
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public class RuleAtomAsserter implements RuleAtomVisitor
{
	private volatile ABox _abox;
	private volatile CompletionStrategy _strategy;

	private volatile VariableBinding _binding;
	private volatile DependencySet _ds;
	private boolean _negated;

	private boolean _asserted;

	public RuleAtomAsserter()
	{
		// empty
	}

	public boolean assertAtom(final RuleAtom atom, final VariableBinding binding, final DependencySet ds, final boolean negated, final ABox abox, final CompletionStrategy strategy)
	{
		_asserted = true;

		_binding = binding;
		_ds = ds;
		_negated = negated;
		_strategy = strategy;
		_abox = abox;

		atom.accept(this);

		return _asserted;
	}

	@Override
	public void visit(final BuiltInAtom atom)
	{
		_asserted = false;
	}

	@Override
	public void visit(final ClassAtom atom)
	{
		final ATermAppl cls = atom.getPredicate();
		final ATermAppl ind = _binding.get(atom.getArgument()).getName();

		addType(ind, cls);
	}

	private void addType(final ATermAppl ind, final ATermAppl cls)
	{
		DependencySet nodeDS = _ds;
		Individual node = _abox.getIndividual(ind);

		if (node.isMerged())
		{
			nodeDS = node.getMergeDependency(true);
			node = node.getSame();
		}

		_strategy.addType(node, _negated ? ATermUtils.negate(cls) : cls, nodeDS);
	}

	private void addEdge(final ATermAppl p, final ATermAppl s, final ATermAppl o)
	{
		DependencySet edgeDS = _ds;
		Individual node1 = _abox.getIndividual(s);

		if (node1.isMerged())
		{
			edgeDS = node1.getMergeDependency(true);
			node1 = node1.getSame();
		}

		if (_negated)
		{
			final ATermAppl cls = all(p, not(value(o)));
			_strategy.addType(node1, cls, _ds);
		}
		else
		{
			Node node2 = _abox.getNode(o);
			if (node2.isMerged())
			{
				edgeDS = node2.getMergeDependency(true);
				node2 = node2.getSame();
			}
			_strategy.addEdge(node1, _abox.getRole(p), node2, edgeDS);
		}
	}

	@Override
	public void visit(final DataRangeAtom atom)
	{
		_asserted = false;
	}

	@Override
	public void visit(final DatavaluedPropertyAtom atom)
	{
		final ATermAppl p = atom.getPredicate();
		final ATermAppl s = _binding.get(atom.getArgument1()).getName();
		final ATermAppl o = _binding.get(atom.getArgument2()).getName();

		addEdge(p, s, o);
	}

	@Override
	public void visit(final DifferentIndividualsAtom atom)
	{
		final ATermAppl ind1 = _binding.get(atom.getArgument1()).getName();
		final ATermAppl ind2 = _binding.get(atom.getArgument2()).getName();
		final ATermAppl cls = not(value(ind2));

		addType(ind1, cls);
	}

	@Override
	public void visit(final IndividualPropertyAtom atom)
	{
		final ATermAppl p = atom.getPredicate();
		final ATermAppl s = _binding.get(atom.getArgument1()).getName();
		final ATermAppl o = _binding.get(atom.getArgument2()).getName();

		addEdge(p, s, o);
	}

	@Override
	public void visit(final SameIndividualAtom atom)
	{
		final ATermAppl ind1 = _binding.get(atom.getArgument1()).getName();
		final ATermAppl ind2 = _binding.get(atom.getArgument2()).getName();
		final ATermAppl cls = value(ind2);

		addType(ind1, cls);
	}

}
