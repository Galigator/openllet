// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.taxonomy;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import openllet.aterm.ATermAppl;
import openllet.atom.OpenError;

/**
 * <p>
 * Title: TaxonomyUtils
 * </p>
 * <p>
 * Description: Utilities for manipulating taxonomy _data structure
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Mike Smith
 */
public class TaxonomyUtils
{
	public enum TaxonomyKey
	{
		INSTANCES_KEY, SUPER_EXPLANATION_KEY
	}

	//	public static final Object INSTANCES_KEY = new Object();
	//	public static final Object SUPER_EXPLANATION_KEY = new Object();

	protected static boolean addSuperExplanation(final Taxonomy<ATermAppl> t, final ATermAppl sub, final ATermAppl sup, final Set<ATermAppl> explanation)
	{
		@SuppressWarnings("unchecked")
		Map<ATermAppl, Set<Set<ATermAppl>>> map = (Map<ATermAppl, Set<Set<ATermAppl>>>) t.getDatum(sub, TaxonomyKey.SUPER_EXPLANATION_KEY);
		Set<Set<ATermAppl>> explanations;
		if (map == null)
		{
			if (t.contains(sub))
			{
				map = new HashMap<>();
				t.putDatum(sub, TaxonomyKey.SUPER_EXPLANATION_KEY, map);
				explanations = null;
			}
			else
				throw new OpenError(sub + " is an unknown class!");
		}
		else
			explanations = map.get(sup);

		if (explanations == null)
		{
			explanations = new HashSet<>();
			map.put(sup, explanations);
		}

		return explanations.add(explanation);
	}

	protected static void clearSuperExplanation(final Taxonomy<ATermAppl> t, final ATermAppl c)
	{
		t.removeDatum(c, TaxonomyKey.SUPER_EXPLANATION_KEY);
	}

	protected static void clearAllInstances(final Taxonomy<?> t)
	{
		for (final TaxonomyNode<?> node : t.getNodes().values())
			node.removeDatum(TaxonomyKey.INSTANCES_KEY);
	}

	/**
	 * @param t is a taxonomy
	 * @param sub classe
	 * @param sup classe
	 * @return if only I know.
	 */
	public static Set<Set<ATermAppl>> getSuperExplanations(final Taxonomy<ATermAppl> t, final ATermAppl sub, final ATermAppl sup)
	{
		@SuppressWarnings("unchecked")
		final Map<ATermAppl, Set<Set<ATermAppl>>> map = (Map<ATermAppl, Set<Set<ATermAppl>>>) t.getDatum(sub, TaxonomyKey.SUPER_EXPLANATION_KEY);
		if (map == null)
			return null;

		final Set<Set<ATermAppl>> explanations = map.get(sup);
		if (explanations == null)
			return null;

		return Collections.unmodifiableSet(explanations);
	}

	/**
	 * Retrieve all instances of a class (based on the _current state of the taxonomy)
	 *
	 * @param t the taxonomy
	 * @param c the class
	 * @return a set of all individuals that are instances of the class
	 */
	public static <T, I> Set<I> getAllInstances(final Taxonomy<T> t, final T c)
	{
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final Iterator<Set<I>> i = (Iterator) t.depthFirstDatumOnly(c, TaxonomyKey.INSTANCES_KEY);
		if (!i.hasNext())
			throw new OpenError(c + " is an unknown class!");

		final Set<I> instances = new HashSet<>();
		do
		{
			final Set<I> current = i.next();
			if (current != null)
				instances.addAll(current);

		} while (i.hasNext());

		return Collections.unmodifiableSet(instances);
		//return Set<OWLNamedIndividual>
		//return Set<ATermAppl>
	}

	/**
	 * Retrieve direct instances of a class (based on _current state of the taxonomy)
	 *
	 * @param t the taxonomy
	 * @param c the class
	 * @return a set of individuals that are instances of {@code c} and not instances of any class {@code d} where {@code subClassOf(d,c)}
	 */
	public static <T, I> Set<I> getDirectInstances(final Taxonomy<T> t, final T c)
	{
		@SuppressWarnings("unchecked")
		final Set<I> instances = (Set<I>) t.getDatum(c, TaxonomyKey.INSTANCES_KEY);
		if (instances == null)
		{
			if (t.contains(c))
				return Collections.emptySet();

			throw new OpenError(c + " is an unknown class!");
		}

		return Collections.unmodifiableSet(instances);
	}

	/**
	 * Get classes of which the _individual is an instance (based on the _current state of the taxonomy)
	 *
	 * @param t the taxonomy
	 * @param ind the _individual
	 * @param directOnly {@code true} if only most specific classes are desired, {@code false} if all classes are desired
	 * @return a set of sets of classes where each inner set is a collection of equivalent classes
	 */
	public static <TClass, TInd> Set<Set<TClass>> getTypes(final Taxonomy<TClass> t, final TInd ind, final boolean directOnly)
	{
		final Set<Set<TClass>> types = new HashSet<>();
		final Iterator<Map.Entry<Set<TClass>, Object>> i = t.datumEquivalentsPair(TaxonomyKey.INSTANCES_KEY);
		while (i.hasNext())
		{
			final Map.Entry<Set<TClass>, Object> pair = i.next();
			@SuppressWarnings("unchecked")
			final Set<TClass> instances = (Set<TClass>) pair.getValue();
			if (instances != null && instances.contains(ind))
			{
				types.add(pair.getKey());
				if (!directOnly)
				{
					final TClass a = pair.getKey().iterator().next();
					types.addAll(t.getSupers(a));
				}
			}
		}
		return Collections.unmodifiableSet(types);
	}

	/**
	 * Determine if an _individual is an instance of a class (based on the _current state of the taxonomy)
	 *
	 * @param t the taxonomy
	 * @param ind the _individual
	 * @param c the class
	 * @return a boolean {@code true} if {@code instanceOf(ind,c)}, {@code false} else
	 */
	public static boolean isType(final Taxonomy<ATermAppl> t, final ATermAppl ind, final ATermAppl c)
	{
		final Iterator<Object> i = t.depthFirstDatumOnly(c, TaxonomyKey.INSTANCES_KEY);
		if (!i.hasNext())
			throw new OpenError(c + " is an unknown class!");

		do
		{
			@SuppressWarnings("unchecked")
			final Set<ATermAppl> instances = (Set<ATermAppl>) i.next();
			if (instances != null && instances.contains(ind))
				return true;

		} while (i.hasNext());

		return false;
	}
}
