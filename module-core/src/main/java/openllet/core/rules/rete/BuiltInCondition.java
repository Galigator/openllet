// Copyright (c) 2006 - 2010, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules.rete;

import java.util.Arrays;

import openllet.core.boxes.abox.ABoxForRule;
import openllet.core.boxes.abox.Literal;
import openllet.core.rules.builtins.BuiltIn;
import openllet.core.utils.ATermUtils;

public class BuiltInCondition implements FilterCondition
{
	private final ABoxForRule _abox;
	private final String _name;
	private final BuiltIn _builtin;
	private final NodeProvider[] _args;

	public BuiltInCondition(final ABoxForRule abox, final String name, final BuiltIn builtin, final NodeProvider[] args)
	{
		_abox = abox;
		_name = name;
		_builtin = builtin;
		_args = args;
		for (final NodeProvider arg : args)
			if (arg == null)
				throw new NullPointerException();
	}

	@Override
	public boolean test(final WME wme, final Token token)
	{
		final Literal[] literals = new Literal[_args.length];
		for (int i = 0; i < literals.length; i++)
			literals[i] = (Literal) _args[i].getNode(wme, token);
		return _builtin.apply(_abox, literals);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(_args);
		result = prime * result + _builtin.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof BuiltInCondition))
			return false;
		final BuiltInCondition other = (BuiltInCondition) obj;
		return _builtin.equals(other._builtin) && Arrays.equals(_args, other._args);
	}

	@Override
	public String toString()
	{
		return ATermUtils.toString(ATermUtils.makeTermAppl(_name)) + Arrays.toString(_args);
	}
}
