package openllet.core.knowledge;

import static openllet.core.utils.TermFactory.and;
import static openllet.core.utils.TermFactory.some;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.boxes.rbox.Role;
import openllet.core.taxonomy.Taxonomy;
import openllet.core.taxonomy.TaxonomyUtils;
import openllet.core.taxonomy.printer.ClassTreePrinter;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Bool;
import openllet.shared.tools.Logging;

/**
 * @since 2.6.4
 */
public interface ClassesBase extends MessageBase, Logging, Base
{

	@Override
	public default boolean isSubClassOf(final ATermAppl subCls, final ATermAppl supCls)
	{
		if (null == subCls || null == supCls)
			return false;

		ensureConsistency();

		if (!isClass(subCls))
		{
			Base.handleUndefinedEntity(subCls + _isNotAnKnowClass);
			return false;
		}

		if (!isClass(supCls))
		{
			Base.handleUndefinedEntity(supCls + _isNotAnKnowClass);
			return false;
		}

		if (subCls.equals(supCls))
			return true;

		// normalize concepts
		final ATermAppl normalC1 = ATermUtils.normalize(subCls);
		final ATermAppl normalC2 = ATermUtils.normalize(supCls);

		if (isClassified() && !doExplanation())
		{
			final Bool isSubNode = getTaxonomyBuilder().getTaxonomy().isSubNodeOf(normalC1, normalC2);
			if (isSubNode.isKnown())
				return isSubNode.isTrue();
		}

		return getABox().isSubClassOf(normalC1, normalC2);
	}

	/**
	 * @param c1
	 * @param c2
	 * @return true if class c1 is equivalent to class c2.
	 */
	public default boolean isEquivalentClass(final ATermAppl c1, final ATermAppl c2)
	{
		if (null == c1 || null == c2)
			return false;

		ensureConsistency();

		if (!isClass(c1))
		{
			Base.handleUndefinedEntity(c1 + _isNotAnKnowClass);
			return false;
		}

		if (!isClass(c2))
		{
			Base.handleUndefinedEntity(c2 + _isNotAnKnowClass);
			return false;
		}

		if (c1.equals(c2))
			return true;

		// normalize concepts
		final ATermAppl normalC1 = ATermUtils.normalize(c1);
		final ATermAppl normalC2 = ATermUtils.normalize(c2);

		if (!doExplanation())
		{
			Bool isEquivalent = Bool.UNKNOWN;
			if (isClassified())
				isEquivalent = getTaxonomyBuilder().getTaxonomy().isEquivalent(normalC1, normalC2);

			if (isEquivalent.isUnknown())
				isEquivalent = getABox().isKnownSubClassOf(normalC1, normalC2).and(getABox().isKnownSubClassOf(normalC2, normalC1));

			if (isEquivalent.isKnown())
				return isEquivalent.isTrue();
		}

		final ATermAppl notC2 = ATermUtils.negate(normalC2);
		final ATermAppl notC1 = ATermUtils.negate(normalC1);
		final ATermAppl c1NotC2 = ATermUtils.makeAnd(normalC1, notC2);
		final ATermAppl c2NotC1 = ATermUtils.makeAnd(c2, notC1);
		final ATermAppl test = ATermUtils.makeOr(c1NotC2, c2NotC1);

		return !isSatisfiable(test);
	}

	/**
	 * Returns the (named) subclasses of class c. Depending on the second parameter the result will include either all subclasses or only the direct subclasses.
	 * A class d is a direct subclass of c iff
	 * <ol>
	 * <li>d is subclass of c</li>
	 * <li>there is no other class x different from c and d such that x is subclass of c and d is subclass of x</li>
	 * </ol>
	 * The class c itself is not included in the list but all the other classes that are sameAs c are put into the list. Also note that the returned list will
	 * always have at least one element. The list will either include one other concept from the hierarchy or the BOTTOM concept if no other class is subsumed
	 * by c. By definition BOTTOM concept is subclass of every concept.
	 * <p>
	 * *** This function will first classify the whole ontology ***
	 * </p>
	 *
	 * @param c class whose subclasses are returned
	 * @param direct If true return only the direct subclasses, otherwise return all the subclasses
	 * @return A set of sets, where each set in the collection represents an equivalence class. The elements of the inner class are ATermAppl objects.
	 */
	public default Set<Set<ATermAppl>> getSubClasses(final ATermAppl c, final boolean direct)
	{
		if (null == c)
			return Collections.emptySet();

		if (!isClass(c))
		{
			Base.handleUndefinedEntity(c + _isNotAnClass);
			return Collections.emptySet();
		}

		final ATermAppl normalC = ATermUtils.normalize(c);

		classify();

		final Taxonomy<ATermAppl> taxonomy = getTaxonomyBuilder().getTaxonomy();

		if (!taxonomy.contains(normalC))
			getTaxonomyBuilder().classify(normalC);

		final Set<Set<ATermAppl>> subs = new HashSet<>();
		for (final Set<ATermAppl> s : taxonomy.getSubs(normalC, direct))
		{
			final Set<ATermAppl> subEqSet = ATermUtils.primitiveOrBottom(s);
			if (!subEqSet.isEmpty())
				subs.add(subEqSet);
		}

		return subs;
	}

	public default Set<Set<ATermAppl>> getDisjointClasses(final ATermAppl c, final boolean direct)
	{
		if (null == c)
			return Collections.emptySet();

		if (!isClass(c))
		{
			Base.handleUndefinedEntity(c + _isNotAnClass);
			return Collections.emptySet();
		}

		final ATermAppl notC = ATermUtils.normalize(ATermUtils.makeNot(c));

		final Set<ATermAppl> complements = getAllEquivalentClasses(notC);
		if (notC.equals(ATermUtils.BOTTOM))
			complements.add(ATermUtils.BOTTOM);
		if (direct && !complements.isEmpty())
			return Collections.singleton(complements);

		final Set<Set<ATermAppl>> disjoints = getSubClasses(notC, direct);

		if (!complements.isEmpty())
			disjoints.add(complements);

		return disjoints;
	}

	/**
	 * Returns all the classes that are equivalent to class c, including c itself.
	 * <p>
	 * *** This function will first classify the whole ontology ***
	 * </p>
	 *
	 * @param c class whose equivalent classes are found
	 * @return A set of ATerm objects
	 */
	public default Set<ATermAppl> getAllEquivalentClasses(final ATermAppl c)
	{
		if (null == c)
			return Collections.emptySet();

		if (!isClass(c))
		{
			Base.handleUndefinedEntity(c + _isNotAnClass);
			return Collections.emptySet();
		}

		final ATermAppl normalC = ATermUtils.normalize(c);

		classify();

		final Taxonomy<ATermAppl> taxonomy = getTaxonomyBuilder().getTaxonomy();

		if (!taxonomy.contains(normalC))
			getTaxonomyBuilder().classify(normalC);

		return ATermUtils.primitiveOrBottom(taxonomy.getAllEquivalents(normalC));
	}

	/**
	 * Answers the isType question without doing any satisfiability check. It might return <code>Bool.TRUE</code>, <code>Bool.FALSE</code>, or
	 * <code>Bool.UNKNOWN</code>. If <code>Bool.UNKNOWN</code> is returned <code>isType</code> function needs to be called to get the answer.
	 *
	 * @param x
	 * @param c
	 * @return true if the term x is of the know type c (class)
	 */
	public default Bool isKnownType(final ATermAppl x, final ATermAppl c)
	{
		if (null == x || null == c)
			return Bool.FALSE;

		ensureConsistency();

		if (!isIndividual(x))
		{
			Base.handleUndefinedEntity(x + _isNotAnIndividual);
			return Bool.FALSE;
		}
		if (!isClass(c))
		{
			Base.handleUndefinedEntity(c + _isNotAValidClassExpression);
			return Bool.FALSE;
		}

		return getABox().isKnownType(x, ATermUtils.normalize(c));
	}

	public default boolean isType(final ATermAppl x, final ATermAppl c)
	{
		if (null == x || null == c)
			return false;

		ensureConsistency();

		if (!isIndividual(x))
		{
			Base.handleUndefinedEntity(x + _isNotAnIndividual);
			return false;
		}
		if (!isClass(c))
		{
			Base.handleUndefinedEntity(c + _isNotAValidClassExpression);
			return false;
		}

		if (isRealized() && !doExplanation())
		{
			final Taxonomy<ATermAppl> taxonomy = getTaxonomyBuilder().getTaxonomy();

			if (taxonomy == null)
				throw new NullPointerException("Taxonomy is null");

			if (taxonomy.contains(c))
				return TaxonomyUtils.isType(taxonomy, x, c);
		}

		return getABox().isType(x, c);
	}

	public default boolean hasRange(final ATermAppl p, final ATermAppl c)
	{
		if (null == p || null == c)
			return false;

		if (!isClass(c) && !isDatatype(c))
		{
			Base.handleUndefinedEntity(c + _isNotAValidClassExpression);
			return false;
		}
		final ATermAppl allValues = ATermUtils.makeAllValues(p, c);
		return isSubClassOf(ATermUtils.TOP, allValues);
	}

	public default boolean isDisjoint(final ATermAppl c1, final ATermAppl c2)
	{
		if (null == c1 || null == c2)
			return false;

		if (isClass(c1) && isClass(c2))
			return isDisjointClass(c1, c2);
		else
			if (isProperty(c1) && isProperty(c2))
				return isDisjointProperty(c1, c2);
			else
				return false;
	}

	public default boolean isDisjointProperty(final ATermAppl r1, final ATermAppl r2)
	{
		if (null == r1 || null == r2)
			return false;

		final Role role1 = getRole(r1);
		final Role role2 = getRole(r2);

		if (role1 == null)
		{
			Base.handleUndefinedEntity(r1 + _isNotAnKnowProperty);
			return false;
		}

		if (role2 == null)
		{
			Base.handleUndefinedEntity(r2 + _isNotAnKnowProperty);
			return false;
		}

		if (role1.getType() != role2.getType())
			return false;
		else
			if (role1.isBottom() || role2.isBottom())
			{
				if (doExplanation())
					getABox().setExplanation(DependencySet.INDEPENDENT);
				return true;
			}
			else
				if (role1.isTop() || role2.isTop())
					return false;
				else
					if (role1.getSubRoles().contains(role2) || role2.getSubRoles().contains(role1))
						return false;

		if (role1.getDisjointRoles().contains(role2) && !doExplanation())
			return true;

		ensureConsistency();

		ATermAppl anon = ATermUtils.makeAnonNominal(Integer.MAX_VALUE);
		if (role1.isDatatypeRole())
			anon = ATermUtils.makeLiteral(anon);
		final ATermAppl nominal = ATermUtils.makeValue(anon);
		final ATermAppl test = and(some(r1, nominal), some(r2, nominal));

		return !getABox().isSatisfiable(test);
	}

	public default boolean isDisjointClass(final ATermAppl c1, final ATermAppl c2)
	{
		if (null == c1 || null == c2)
			return false;

		final ATermAppl notC2 = ATermUtils.makeNot(c2);

		return isSubClassOf(c1, notC2);
	}

	public default boolean isComplement(final ATermAppl c1, final ATermAppl c2)
	{
		if (null == c1 || null == c2)
			return false;

		final ATermAppl notC2 = ATermUtils.makeNot(c2);

		return isEquivalentClass(c1, notC2);
	}

	@Override
	public default Set<Set<ATermAppl>> getSuperClasses(final ATermAppl cParam, final boolean direct)
	{
		if (null == cParam)
			return Collections.emptySet();

		ATermAppl c = cParam;
		if (!isClass(c))
		{
			Base.handleUndefinedEntity(c + _isNotAnClass);
			return Collections.emptySet();
		}

		c = ATermUtils.normalize(c);

		classify();

		final Taxonomy<ATermAppl> taxonomy = getTaxonomyBuilder().getTaxonomy();

		if (!taxonomy.contains(c))
			getTaxonomyBuilder().classify(c);

		return taxonomy//
				.supers(c, direct)//
				.map(ATermUtils::primitiveOrBottom)//
				.filter(supEqSet -> !supEqSet.isEmpty())//
				.collect(Collectors.toSet());
	}

	/**
	 * Returns all the classes that are equivalent to class c, excluding c itself.
	 * <p>
	 * *** This function will first classify the whole ontology ***
	 * </p>
	 *
	 * @param c class whose equivalent classes are found
	 * @return A set of ATerm objects
	 */
	public default Set<ATermAppl> getEquivalentClasses(final ATermAppl c)
	{
		if (null == c)
			return Collections.emptySet();

		final Set<ATermAppl> result = getAllEquivalentClasses(c);
		result.remove(c);

		return result;
	}

	/**
	 * Print the class hierarchy on the standard output.
	 */
	public default void printClassTree()
	{
		classify();

		new ClassTreePrinter().print(getTaxonomyBuilder().getTaxonomy());
	}
}
