// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.completion.rule;

import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.IndividualIterator;

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
public interface TableauRule
{
	boolean apply(final IndividualIterator i);

	/**
	 * Apply transformations rule in the ABox around the given individual
	 *
	 * @param ind is the entry point in the abox.
	 */
	void apply(final Individual ind);
}
