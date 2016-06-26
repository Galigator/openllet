// Copyright (c) 2006 - 2010, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules;

import openllet.core.DependencySet;
import openllet.core.rules.model.Rule;

public class PartialBinding
{
	private final Rule _rule;
	private final VariableBinding _binding;
	private final DependencySet _ds;

	public PartialBinding(final Rule rule, final VariableBinding binding, final DependencySet ds)
	{
		this._rule = rule;
		this._binding = binding;
		this._ds = ds;
	}

	Rule getRule()
	{
		return _rule;
	}

	VariableBinding getBinding()
	{
		return _binding;
	}

	DependencySet getDependencySet()
	{
		return _ds;
	}

	int getBranch()
	{
		return _ds.max();
	}
}
