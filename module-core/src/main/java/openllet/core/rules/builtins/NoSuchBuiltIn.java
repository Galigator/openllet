// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules.builtins;

import java.util.Collection;
import java.util.Collections;

import openllet.core.boxes.abox.ABoxForRule;
import openllet.core.boxes.abox.Literal;
import openllet.core.rules.BindingHelper;
import openllet.core.rules.VariableBinding;
import openllet.core.rules.model.AtomVariable;
import openllet.core.rules.model.BuiltInAtom;

/**
 * <p>
 * Title: No-Such-Built-In Built-In
 * </p>
 * <p>
 * Description: Place holder for any unimplemented built-in.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Ron Alford
 */
public class NoSuchBuiltIn implements BuiltIn
{

	private static class EmptyHelper implements BindingHelper
	{

		@Override
		public Collection<? extends AtomVariable> getBindableVars(final Collection<AtomVariable> bound)
		{
			return Collections.emptySet();
		}

		@Override
		public Collection<? extends AtomVariable> getPrerequisiteVars(final Collection<AtomVariable> bound)
		{
			return Collections.emptySet();
		}

		@Override
		public void rebind(final VariableBinding newBinding)
		{
			// Nothing to do
		}

		@Override
		public boolean selectNextBinding()
		{
			return false;
		}

		@Override
		public void setCurrentBinding(final VariableBinding currentBinding)
		{
			// Nothing to do
		}

	}

	private final BindingHelper empty = new EmptyHelper();

	public static final BuiltIn instance = new NoSuchBuiltIn();

	private NoSuchBuiltIn()
	{
	}

	@Override
	public BindingHelper createHelper(final BuiltInAtom atom)
	{
		BuiltInRegistry._logger.warning("Returning an empty binding helper for unimplemented built-in " + atom);
		return empty;
	}

	@Override
	public boolean apply(final ABoxForRule abox, final Literal[] args)
	{
		return false;
	}
}
