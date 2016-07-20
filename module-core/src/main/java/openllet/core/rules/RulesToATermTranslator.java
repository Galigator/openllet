// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules;

import openllet.aterm.ATermAppl;
import openllet.core.rules.model.AtomDConstant;
import openllet.core.rules.model.AtomDObject;
import openllet.core.rules.model.AtomDVariable;
import openllet.core.rules.model.AtomIConstant;
import openllet.core.rules.model.AtomIVariable;
import openllet.core.rules.model.AtomObject;
import openllet.core.rules.model.AtomObjectVisitor;
import openllet.core.rules.model.BuiltInAtom;
import openllet.core.rules.model.ClassAtom;
import openllet.core.rules.model.DataRangeAtom;
import openllet.core.rules.model.DatavaluedPropertyAtom;
import openllet.core.rules.model.DifferentIndividualsAtom;
import openllet.core.rules.model.IndividualPropertyAtom;
import openllet.core.rules.model.Rule;
import openllet.core.rules.model.RuleAtom;
import openllet.core.rules.model.RuleAtomVisitor;
import openllet.core.rules.model.SameIndividualAtom;
import openllet.core.utils.ATermUtils;

/**
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public class RulesToATermTranslator implements RuleAtomVisitor, AtomObjectVisitor
{
	private ATermAppl _term;

	public ATermAppl translate(final Rule rule)
	{
		_term = null;
		visit(rule);
		return _term;
	}

	public ATermAppl translate(final RuleAtom ruleAtom)
	{
		_term = null;
		ruleAtom.accept(this);
		return _term;
	}

	public ATermAppl translate(final AtomObject obj)
	{
		_term = null;
		obj.accept(this);
		return _term;
	}

	public void visit(final Rule rule)
	{
		final ATermAppl[] head = new ATermAppl[rule.getHead().size()];
		final ATermAppl[] body = new ATermAppl[rule.getBody().size()];

		int i = 0;
		for (final RuleAtom atom : rule.getHead())
			head[i++] = translate(atom);

		i = 0;
		for (final RuleAtom atom : rule.getBody())
			body[i++] = translate(atom);

		_term = ATermUtils.makeRule(rule.getName(), head, body);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final BuiltInAtom atom)
	{
		final int arity = atom.getAllArguments().size();
		final ATermAppl[] args = new ATermAppl[arity + 1];
		args[0] = ATermUtils.makeTermAppl(atom.getPredicate());
		int i = 1;
		for (final AtomDObject arg : atom.getAllArguments())
			args[i++] = translate(arg);

		_term = ATermUtils.makeBuiltinAtom(args);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final ClassAtom atom)
	{
		final ATermAppl c = atom.getPredicate();
		final ATermAppl i = translate(atom.getArgument());

		_term = ATermUtils.makeTypeAtom(i, c);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final DataRangeAtom atom)
	{
		final ATermAppl d = atom.getPredicate();
		final ATermAppl l = translate(atom.getArgument());

		_term = ATermUtils.makeTypeAtom(l, d);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final DatavaluedPropertyAtom atom)
	{
		final ATermAppl p = atom.getPredicate();
		final ATermAppl s = translate(atom.getArgument1());
		final ATermAppl o = translate(atom.getArgument2());

		_term = ATermUtils.makePropAtom(p, s, o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final DifferentIndividualsAtom atom)
	{
		final ATermAppl t1 = translate(atom.getArgument1());
		final ATermAppl t2 = translate(atom.getArgument2());

		_term = ATermUtils.makeDifferent(t1, t2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final IndividualPropertyAtom atom)
	{
		final ATermAppl p = atom.getPredicate();
		final ATermAppl s = translate(atom.getArgument1());
		final ATermAppl o = translate(atom.getArgument2());

		_term = ATermUtils.makePropAtom(p, s, o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final SameIndividualAtom atom)
	{
		final ATermAppl t1 = translate(atom.getArgument1());
		final ATermAppl t2 = translate(atom.getArgument2());

		_term = ATermUtils.makeSameAs(t1, t2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final AtomDConstant constant)
	{
		_term = constant.getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final AtomDVariable variable)
	{
		_term = ATermUtils.makeVar(variable.getName());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final AtomIConstant constant)
	{
		_term = constant.getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final AtomIVariable variable)
	{
		_term = ATermUtils.makeVar(variable.getName());
	}

}
