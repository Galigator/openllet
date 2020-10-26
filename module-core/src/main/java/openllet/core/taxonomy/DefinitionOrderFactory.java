// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.taxonomy;

import static openllet.core.OpenlletOptions.OrderedClassification.DISABLED;
import static openllet.core.OpenlletOptions.OrderedClassification.ENABLED_LEGACY_ORDERING;

import java.util.Comparator;

import openllet.aterm.ATerm;
import openllet.core.KnowledgeBase;
import openllet.core.OpenlletOptions;
import openllet.core.utils.Comparators;

/**
 * Creates a definition _order based on the configuration options defined in {@link OpenlletOptions}.
 *
 * @author Evren Sirin
 */
public class DefinitionOrderFactory
{
	public static DefinitionOrder createDefinitionOrder(final KnowledgeBase kb)
	{
		final Comparator<ATerm> comparator = OpenlletOptions.ORDERED_CLASSIFICATION != DISABLED ? Comparators.termComparator : null;

		return OpenlletOptions.ORDERED_CLASSIFICATION == ENABLED_LEGACY_ORDERING ? //
				new TaxonomyBasedDefinitionOrder(kb, comparator) : //
				new JGraphBasedDefinitionOrder(kb, comparator);
	}
}
