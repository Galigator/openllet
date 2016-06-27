// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import openllet.core.ABoxImpl;
import openllet.core.Individual;
import openllet.core.rules.model.AtomIVariable;
import openllet.core.rules.model.AtomVariable;

/**
 * <p>
 * Title: Object Variable Binding Helper
 * </p>
 * <p>
 * Description: A binding helper that will iterate over all named individuals in the _abox.
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

public class ObjectVariableBindingHelper implements BindingHelper
{

	private final ABoxImpl _abox;
	private Individual _currentIndividual;
	private Iterator<Individual> _individualIterator;
	private final AtomIVariable _var;

	public ObjectVariableBindingHelper(final ABoxImpl abox, final AtomIVariable var)
	{
		this._abox = abox;
		this._var = var;
	}

	@Override
	public Collection<AtomIVariable> getBindableVars(final Collection<AtomVariable> bound)
	{
		return Collections.singleton(_var);
	}

	@Override
	public Collection<AtomIVariable> getPrerequisiteVars(final Collection<AtomVariable> bound)
	{
		return Collections.emptyList();
	}

	@Override
	public void rebind(final VariableBinding newBinding)
	{
		if (newBinding.containsKey(_var))
			_individualIterator = Collections.singleton(newBinding.get(_var)).iterator();
		else
			_individualIterator = new AllNamedIndividualsIterator(_abox);
	}

	@Override
	public boolean selectNextBinding()
	{
		if ((_individualIterator == null) || !_individualIterator.hasNext())
			return false;

		_currentIndividual = _individualIterator.next();

		return true;
	}

	@Override
	public void setCurrentBinding(final VariableBinding currentBinding)
	{
		currentBinding.set(_var, _currentIndividual);
	}

	@Override
	public String toString()
	{
		return "individuals(" + _var + ")";
	}

}
