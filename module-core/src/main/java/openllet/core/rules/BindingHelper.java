// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules;

import java.util.Collection;
import openllet.core.rules.model.AtomVariable;

/**
 * <p>
 * Title: Binding Helper
 * </p>
 * <p>
 * Description: Binding helper interface.
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

public interface BindingHelper
{

	/**
	 * @param  bound
	 * @return       a set of variables which this binding helper can bind.
	 */
	Collection<? extends AtomVariable> getBindableVars(Collection<AtomVariable> bound);

	/**
	 * @param  bound
	 * @return       a set of variables which must be bound before this helper can generate bindings.
	 */
	Collection<? extends AtomVariable> getPrerequisiteVars(Collection<AtomVariable> bound);

	/**
	 * Set the incoming binding for this helper. This fixes any variables that are already bound by a preceding Binding Helper.
	 *
	 * @param newBinding Binding map. Implementation will copy map if needed.
	 */
	void rebind(VariableBinding newBinding);

	/**
	 * Selects the next binding.
	 *
	 * @return True if a binding was available for this pattern given the initial binding. False otherwise. Will return if the binding is not set.
	 */
	boolean selectNextBinding();

	/**
	 * Set the variables this pattern uses in the given map.
	 *
	 * @param currentBinding
	 */
	void setCurrentBinding(VariableBinding currentBinding);

}
