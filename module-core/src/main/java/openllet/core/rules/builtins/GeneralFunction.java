// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules.builtins;

import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.Literal;

/**
 * <p>
 * Title: General Function
 * </p>
 * <p>
 * Description: Interface for built-ins that can bind arbitrary argument positions to a new value.
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
public interface GeneralFunction
{
	/**
	 * Apply the function against a set of arguments. Set null values of argument array. Return true if function succeeded, false if it cannot.
	 *
	 * @param abox
	 * @param args
	 * @return the literal that result of the application of the function again the Abox
	 */
	public boolean apply(final ABox abox, final Literal[] args);

	public boolean isApplicable(final boolean[] boundPositions);
}
