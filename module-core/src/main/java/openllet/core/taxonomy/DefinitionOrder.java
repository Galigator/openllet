// Copyright (c) 2006 - 2010, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.taxonomy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import openllet.aterm.ATermAppl;

/**
 * A class to compute the definition order for concepts and tell if a concept is used in cyclic definition. The definition _order is computed after the TBox
 * preprocessing is applied so the definitions used for computing this order is not always same as asserted definitions. The notion of cyclic definition depends
 * on the expressivity of the ontology. If there are no inverses a definition the concepts used inside restrictions are ignored.
 *
 * @author Evren Sirin
 */
public interface DefinitionOrder extends Iterable<ATermAppl>
{
	/**
	 * Returns an iterator over all the classes in the ontology sorted based on the definition _order.
	 */
	@Override
	Iterator<ATermAppl> iterator();

	/**
	 * Returns if a concept is used in cyclic definitions.
	 *
	 * @param  concept concept to check
	 * @return         <code>true</code> if concept is used in a cyclic definition
	 */
	boolean isCyclic(ATermAppl concept);

	default List<ATermAppl> getList()
	{
		final Iterator<ATermAppl> it = iterator();
		final ArrayList<ATermAppl> result = new ArrayList<>();
		while (it.hasNext())
			result.add(it.next());
		return result;
	}
}
