// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules.rete;

import java.util.Arrays;

import openllet.core.DependencySet;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.Literal;
import openllet.core.rules.builtins.BuiltIn;
import openllet.core.utils.ATermUtils;

/**
 */
public class BetaBuiltinNode extends BetaNode
{
	private final ABox _abox;
	private final String _name;
	private final BuiltIn _builtin;
	private final NodeProvider[] _args;

	public BetaBuiltinNode(final ABox abox, final String name, final BuiltIn builtin, final NodeProvider[] args)
	{
		_abox = abox;
		_name = name;
		_builtin = builtin;
		_args = args;
	}

	@Override
	public void activate(final WME wme)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void activate(final Token token)
	{
		final Literal[] literals = new Literal[_args.length];
		for (int i = 0; i < literals.length; i++)
			literals[i] = _args[i] == null ? null : (Literal) _args[i].getNode(null, token);
		if (_builtin.apply(_abox, literals))
			activateChildren(WME.createBuiltin(literals, DependencySet.INDEPENDENT), token);
	}

	@Override
	public void print(final String indentLvl)
	{
		final String indent = indentLvl + "  ";
		System.out.print(indent);
		System.out.println(this);
		for (final BetaNode node : getBetas())
			node.print(indent);
	}

	@Override
	public String toString()
	{
		return "Builtin " + ATermUtils.toString(ATermUtils.makeTermAppl(_name)) + Arrays.toString(_args);
	}
}
