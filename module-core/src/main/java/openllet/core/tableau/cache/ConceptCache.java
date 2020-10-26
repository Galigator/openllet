// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.cache;

import java.util.Map;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.utils.Bool;

/**
 * <p>
 * Title: Concept Cache
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
public interface ConceptCache extends Map<ATermAppl, CachedNode>
{
	/**
	 * @return the maximum number of non-primitive concepts allowed in the _cache
	 */
	int getMaxSize();

	/**
	 * Set the maximum number of non-primitive concepts allowed in the cache
	 *
	 * @param maxSize
	 */
	void setMaxSize(int maxSize);

	/**
	 * @param c is the concept
	 * @return the satisfiability status of a concept as a three-value boolean.
	 */
	Bool getSat(ATermAppl c);

	/**
	 * Put an incomplete
	 *
	 * @param c
	 * @param isSatisfiable
	 * @return true if put is success
	 */
	boolean putSat(ATermAppl c, boolean isSatisfiable);

	/**
	 * @param kb
	 * @param node1
	 * @param node2
	 * @return true if mergable
	 */
	Bool isMergable(KnowledgeBase kb, CachedNode node1, CachedNode node2);

	/**
	 * @param kb
	 * @param node1
	 * @param node2
	 * @return true if nominal
	 */
	Bool checkNominalEdges(KnowledgeBase kb, CachedNode node1, CachedNode node2);

	/**
	 * Returns safety checker that tells which concepts are safe to _cache.
	 *
	 * @return safety checker
	 */
	CacheSafety getSafety();
}
