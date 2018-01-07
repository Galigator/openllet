package openllet.core.knowledge;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.core.DependencySet;
import openllet.core.KnowledgeBase.ChangeType;
import openllet.core.OpenlletOptions;
import openllet.core.boxes.abox.Edge;
import openllet.core.boxes.abox.EdgeList;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.Literal;
import openllet.core.boxes.abox.Node;
import openllet.core.boxes.rbox.Role;
import openllet.core.datatypes.exceptions.InvalidLiteralException;
import openllet.core.datatypes.exceptions.UnrecognizedDatatypeException;
import openllet.core.taxonomy.Taxonomy;
import openllet.core.utils.ATermUtils;
import openllet.shared.tools.Logging;

/**
 * Groupment of all methods related to properties.
 *
 * @since 2.6.4
 */
public interface PropertiesBase extends MessageBase, Logging, Base
{
	public Set<ATermAppl> currentIndividuals();

	default void addSubProperty(final ATerm sub, final ATermAppl sup)
	{
		getChanges().add(ChangeType.RBOX_ADD);
		getRBox().addSubRole(sub, sup);

		getLogger().finer(() -> "sub-prop " + sub + " " + sup);
	}

	default void addEquivalentProperty(final ATermAppl p1, final ATermAppl p2)
	{
		getChanges().add(ChangeType.RBOX_ADD);
		getRBox().addEquivalentRole(p1, p2);

		getLogger().finer(() -> "same-prop " + p1 + " " + p2);
	}

	default void addDisjointProperties(final ATermList properties)
	{
		if (null == properties)
			return;

		final DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(ATermUtils.makeDisjointProperties(properties)) : DependencySet.INDEPENDENT;

		for (ATermList l1 = properties; !l1.isEmpty(); l1 = l1.getNext())
		{
			final ATermAppl p1 = (ATermAppl) l1.getFirst();
			for (ATermList l2 = l1.getNext(); !l2.isEmpty(); l2 = l2.getNext())
			{
				final ATermAppl p2 = (ATermAppl) l2.getFirst();
				addDisjointProperty(p1, p2, ds);
			}
		}
		getLogger().finer(() -> "disjoints " + properties);
	}

	default void addDisjointProperty(final ATermAppl p1, final ATermAppl p2)
	{
		final DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(ATermUtils.makeDisjointProperty(p1, p2)) : DependencySet.INDEPENDENT;

		addDisjointProperty(p1, p2, ds);
	}

	default void addDisjointProperty(final ATermAppl p1, final ATermAppl p2, final DependencySet ds)
	{
		getChanges().add(ChangeType.RBOX_ADD);
		getRBox().addDisjointRole(p1, p2, ds);

		getLogger().finer(() -> "dis-prop " + p1 + " " + p2);
	}

	default void addInverseProperty(final ATermAppl p1, final ATermAppl p2)
	{
		if (null == p1 || null == p2)
			return;

		if (OpenlletOptions.IGNORE_INVERSES)
		{
			getLogger().warning("Ignoring inverseOf(" + p1 + " " + p2 + ") axiom due to the IGNORE_INVERSES option");
			return;
		}

		getChanges().add(ChangeType.RBOX_ADD);

		final DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(ATermUtils.makeInvProp(p1, p2)) : DependencySet.INDEPENDENT;

		getRBox().addInverseRole(p1, p2, ds);

		getLogger().finer(() -> "inv-prop " + p1 + " " + p2);
	}

	default void addTransitiveProperty(final ATermAppl p)
	{
		if (null == p)
			return;

		getChanges().add(ChangeType.RBOX_ADD);

		final Role r = getRBox().getDefinedRole(p);

		final DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(ATermUtils.makeTransitive(p)) : DependencySet.INDEPENDENT;

		// r.setTransitive(true);
		r.addSubRoleChain(ATermUtils.makeList(new ATerm[] { p, p }), ds);
		getLogger().finer(() -> "trans-prop " + p);
	}

	default void addSymmetricProperty(final ATermAppl p)
	{
		if (OpenlletOptions.IGNORE_INVERSES)
		{
			getLogger().warning("Ignoring SymmetricProperty(" + p + ") axiom due to the IGNORE_INVERSES option");
			return;
		}

		getChanges().add(ChangeType.RBOX_ADD);

		final DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(ATermUtils.makeSymmetric(p)) : DependencySet.INDEPENDENT;

		getRBox().addInverseRole(p, p, ds);
		getLogger().finer(() -> "sym-prop " + p);
	}

	/**
	 * @param p
	 * @deprecated Use {@link #addAsymmetricProperty(ATermAppl)}
	 */
	@Deprecated
	default void addAntisymmetricProperty(final ATermAppl p)
	{
		addAsymmetricProperty(p);
	}

	default void addAsymmetricProperty(final ATermAppl p)
	{
		if (null == p)
			return;

		getChanges().add(ChangeType.RBOX_ADD);
		final Role r = getRBox().getDefinedRole(p);

		final DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(ATermUtils.makeAsymmetric(p)) : DependencySet.INDEPENDENT;

		r.setAsymmetric(true, ds);
		getLogger().finer(() -> "anti-sym-prop " + p);
	}

	default void addReflexiveProperty(final ATermAppl p)
	{
		if (null == p)
			return;

		getChanges().add(ChangeType.RBOX_ADD);
		final Role r = getRBox().getDefinedRole(p);

		final DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(ATermUtils.makeReflexive(p)) : DependencySet.INDEPENDENT;

		r.setReflexive(true, ds);
		getLogger().finer(() -> "reflexive-prop " + p);
	}

	default void addIrreflexiveProperty(final ATermAppl p)
	{
		getChanges().add(ChangeType.RBOX_ADD);
		final Role r = getRBox().getDefinedRole(p);

		final DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(ATermUtils.makeIrreflexive(p)) : DependencySet.INDEPENDENT;

		r.setIrreflexive(true, ds);
		getLogger().finer(() -> "irreflexive-prop " + p);
	}

	default void addFunctionalProperty(final ATermAppl p)
	{
		getChanges().add(ChangeType.RBOX_ADD);
		final Role r = getRBox().getDefinedRole(p);

		final DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(ATermUtils.makeFunctional(p)) : DependencySet.INDEPENDENT;

		r.setFunctional(true, ds);
		getLogger().finer(() -> "func-prop " + p);
	}

	default void addInverseFunctionalProperty(final ATerm p)
	{
		if (null == p)
			return;

		if (OpenlletOptions.IGNORE_INVERSES)
		{
			getLogger().warning("Ignoring InverseFunctionalProperty(" + p + ") axiom due to the IGNORE_INVERSES option");
			return;
		}

		getChanges().add(ChangeType.RBOX_ADD);
		final Role role = getRBox().getDefinedRole(p);

		final DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(ATermUtils.makeInverseFunctional(p)) : DependencySet.INDEPENDENT;

		role.setInverseFunctional(true, ds);
		getLogger().finer(() -> "inv-func-prop " + p);
	}

	default Set<Set<ATermAppl>> getAllSuperProperties(final ATermAppl prop)
	{
		if (null == prop)
			return Collections.emptySet();

		if (!isProperty(prop))
		{
			Base.handleUndefinedEntity(prop + _isNotAnProperty);
			return Collections.emptySet();
		}

		final Set<Set<ATermAppl>> supers = getSuperProperties(prop);
		supers.add(getAllEquivalentProperties(prop));

		return supers;
	}

	/**
	 * Return the super properties of p. Depending on the second parameter the result will include either all super properties or only the direct super
	 * properties.
	 *
	 * @param prop
	 * @return A set of sets, where each set in the collection represents a set of equivalent properties. The elements of the inner class are Role objects.
	 */
	public default Set<Set<ATermAppl>> getSuperProperties(final ATermAppl prop)
	{
		return getSuperProperties(prop, false);
	}

	/**
	 * Return the super properties of p. Depending on the second parameter the result will include either all super properties or only the direct super
	 * properties.
	 *
	 * @param prop
	 * @param direct If true return only the direct super properties, otherwise return all the super properties
	 * @return A set of sets, where each set in the collection represents a set of equivalent properties. The elements of the inner class are Role objects.
	 */
	default Set<Set<ATermAppl>> getSuperProperties(final ATermAppl prop, final boolean direct)
	{
		if (null == prop)
			return Collections.emptySet();

		if (!isProperty(prop))
		{
			Base.handleUndefinedEntity(prop + _isNotAnProperty);
			return Collections.emptySet();
		}

		final Set<Set<ATermAppl>> supers = new HashSet<>();
		final Taxonomy<ATermAppl> taxonomy = getRoleTaxonomy(prop);
		if (taxonomy != null)
			for (final Set<ATermAppl> s : taxonomy.getSupers(prop, direct))
			{
				final Set<ATermAppl> supEqSet = ATermUtils.primitiveOrBottom(s);
				if (!supEqSet.isEmpty())
					supers.add(supEqSet);
			}

		return supers;
	}

	default Set<Set<ATermAppl>> getAllSubProperties(final ATermAppl prop)
	{
		if (null == prop)
			return Collections.emptySet();

		if (!isProperty(prop))
		{
			Base.handleUndefinedEntity(prop + _isNotAnProperty);
			return Collections.emptySet();
		}

		final Set<Set<ATermAppl>> subs = getSubProperties(prop);
		subs.add(getAllEquivalentProperties(prop));

		return subs;
	}

	default Taxonomy<ATermAppl> getRoleTaxonomy(final ATermAppl r)
	{
		prepare();

		if (isObjectProperty(r))
			return getRBox().getObjectTaxonomy();
		else
			if (isDatatypeProperty(r))
				return getRBox().getDataTaxonomy();
			else
				if (isAnnotationProperty(r))
					return getRBox().getAnnotationTaxonomy();

		return null;
	}

	/**
	 * Return the sub properties of p. Depending on the second parameter the result will include either all subproperties or only the direct subproperties.
	 *
	 * @param prop
	 * @param direct If true return only the direct subproperties, otherwise return all the subproperties
	 * @return A set of sets, where each set in the collection represents a set of equivalent properties. The elements of the inner class are ATermAppl objects.
	 */
	default Set<Set<ATermAppl>> getSubProperties(final ATermAppl prop, final boolean direct)
	{
		if (null == prop)
			return Collections.emptySet();

		if (!isProperty(prop))
		{
			Base.handleUndefinedEntity(prop + _isNotAnProperty);
			return Collections.emptySet();
		}

		final Set<Set<ATermAppl>> subs = new HashSet<>();
		final Taxonomy<ATermAppl> taxonomy = getRoleTaxonomy(prop);
		if (taxonomy != null)
			for (final Set<ATermAppl> s : taxonomy.getSubs(prop, direct))
			{
				final Set<ATermAppl> subEqSet = ATermUtils.primitiveOrBottom(s);
				if (!subEqSet.isEmpty())
					subs.add(subEqSet);
			}
		else
			getLogger().info(() -> "taxonomy is null for " + prop);

		return subs;
	}

	/**
	 * Return all the sub properties of p.
	 *
	 * @param prop
	 * @return A set of sets, where each set in the collection represents a set of equivalent properties. The elements of the inner class are ATermAppl objects.
	 */
	public default Set<Set<ATermAppl>> getSubProperties(final ATermAppl prop)
	{
		return getSubProperties(prop, false);
	}

	default Taxonomy<ATermAppl> getRoleTaxonomy(final boolean objectTaxonomy)
	{
		prepare();

		return objectTaxonomy ? getRBox().getObjectTaxonomy() : getRBox().getDataTaxonomy();
	}

	/**
	 * Return all the properties that are equivalent to p.
	 *
	 * @param prop
	 * @return A set of ATermAppl objects.
	 */
	default Set<ATermAppl> getEquivalentProperties(final ATermAppl prop)
	{
		if (null == prop)
			return Collections.emptySet();

		if (!isProperty(prop))
		{
			Base.handleUndefinedEntity(prop + _isNotAnProperty);
			return Collections.emptySet();
		}

		final Taxonomy<ATermAppl> taxonomy = getRoleTaxonomy(prop);
		if (null == taxonomy)
			return Collections.<ATermAppl> emptySet();

		if (OpenlletOptions.RETURN_NON_PRIMITIVE_EQUIVALENT_PROPERTIES && !ATermUtils.isBuiltinProperty(prop))
			return taxonomy.getEquivalents(prop);

		return ATermUtils.primitiveOrBottom(taxonomy.getEquivalents(prop));
	}

	default Set<ATermAppl> getAllEquivalentProperties(final ATermAppl prop)
	{
		if (null == prop)
			return Collections.emptySet();

		if (!isProperty(prop))
		{
			Base.handleUndefinedEntity(prop + _isNotAnProperty);
			return Collections.emptySet();
		}

		final Taxonomy<ATermAppl> taxonomy = getRoleTaxonomy(prop);
		if (null == taxonomy)
			return Collections.<ATermAppl> emptySet();

		if (OpenlletOptions.RETURN_NON_PRIMITIVE_EQUIVALENT_PROPERTIES && !ATermUtils.isBuiltinProperty(prop))
			return taxonomy.getAllEquivalents(prop);

		return ATermUtils.primitiveOrBottom(taxonomy.getAllEquivalents(prop));
	}

	/**
	 * @param name
	 * @return the named inverse property and all its equivalent properties.
	 */
	default Set<ATermAppl> getInverses(final ATerm name)
	{
		if (null == name)
			return Collections.emptySet();

		final ATermAppl invR = getInverse(name);
		if (invR != null)
		{
			final Set<ATermAppl> inverses = getAllEquivalentProperties(invR);
			return inverses;
		}

		return Collections.emptySet();
	}

	/**
	 * Returns the inverse of given property. This could possibly be an internal property created by the reasoner rather than a named property. In case the
	 * given property has more than one inverse any one of them can be returned.
	 *
	 * @param name Property whose inverse being sought
	 * @return Inverse property or null if given property is not defined or it is not an object property
	 */
	default ATermAppl getInverse(final ATerm name)
	{
		if (null == name)
			return null;

		final Role prop = getRBox().getRole(name);
		if (prop == null)
		{
			Base.handleUndefinedEntity(name + _isNotAnProperty);
			return null;
		}

		final Role invProp = prop.getInverse();

		return invProp != null ? invProp.getName() : null;
	}

	default boolean isSubPropertyOf(final ATermAppl sub, final ATermAppl sup)
	{
		if (null == sub || null == sup)
			return false;

		final Role roleSub = getRBox().getRole(sub);
		final Role roleSup = getRBox().getRole(sup);

		if (roleSub == null)
		{
			Base.handleUndefinedEntity(sub + _isNotAnKnowProperty);
			return false;
		}

		if (roleSup == null)
		{
			Base.handleUndefinedEntity(sup + _isNotAnKnowProperty);
			return false;
		}

		if (roleSub.isSubRoleOf(roleSup))
		{
			if (doExplanation())
				getABox().setExplanation(roleSub.getExplainSuper(sup));
			return true;
		}

		if (roleSub.getType() != roleSup.getType())
			return false;

		ensureConsistency();

		ATermAppl test;
		if (roleSub.isObjectRole())
		{
			final ATermAppl c = ATermUtils.makeTermAppl("_C_");
			final ATermAppl notC = ATermUtils.makeNot(c);
			test = ATermUtils.makeAnd(ATermUtils.makeSomeValues(sub, c), ATermUtils.makeAllValues(sup, notC));
		}
		else
			if (roleSub.isDatatypeRole())
			{
				final ATermAppl anon = ATermUtils.makeLiteral(ATermUtils.makeAnonNominal(Integer.MAX_VALUE));
				test = ATermUtils.makeAnd(ATermUtils.makeHasValue(sub, anon), ATermUtils.makeAllValues(sup, ATermUtils.makeNot(ATermUtils.makeValue(anon))));
			}
			else
				if (roleSub.isAnnotationRole())
					return false; //temporary statement until we incorporate annotation properties to the taxonomy ([t:412])
				else
					throw new IllegalArgumentException();

		return !getABox().isSatisfiable(test);
	}

	default boolean isEquivalentProperty(final ATermAppl p1, final ATermAppl p2)
	{
		if (null == p1 || null == p2)
			return false;

		final Role role1 = getRBox().getRole(p1);
		final Role role2 = getRBox().getRole(p2);

		if (role1 == null)
		{
			Base.handleUndefinedEntity(p1 + _isNotAnKnowProperty);
			return false;
		}

		if (role2 == null)
		{
			Base.handleUndefinedEntity(p2 + _isNotAnKnowProperty);
			return false;
		}

		if (role1.isSubRoleOf(role2) && role2.isSubRoleOf(role1))
		{
			if (doExplanation())
				getABox().setExplanation(role1.getExplainSuper(p2).union(role1.getExplainSub(p2), doExplanation()));
			return true;
		}

		if (role1.isAnnotationRole() || role2.isAnnotationRole())
			return false;

		if (role1.getType() != role2.getType())
			return false;

		ensureConsistency();

		ATermAppl test;
		if (role1.isObjectRole())
		{
			final ATermAppl c = !role1.getRanges().isEmpty() ? role1.getRanges().iterator().next() : !role2.getRanges().isEmpty() ? role2.getRanges().iterator().next() : ATermUtils.makeTermAppl("_C_");
			final ATermAppl notC = ATermUtils.makeNot(c);
			test = ATermUtils.makeOr(ATermUtils.makeAnd(ATermUtils.makeSomeValues(p1, c), ATermUtils.makeAllValues(p2, notC)), ATermUtils.makeAnd(ATermUtils.makeSomeValues(p2, c), ATermUtils.makeAllValues(p1, notC)));
		}
		else
			if (role1.isDatatypeRole())
			{
				final ATermAppl anon = ATermUtils.makeLiteral(ATermUtils.makeAnonNominal(Integer.MAX_VALUE));
				test = ATermUtils.makeOr(ATermUtils.makeAnd(ATermUtils.makeHasValue(p1, anon), ATermUtils.makeAllValues(p2, ATermUtils.makeNot(ATermUtils.makeValue(anon)))), ATermUtils.makeAnd(ATermUtils.makeHasValue(p2, anon), ATermUtils.makeAllValues(p1, ATermUtils.makeNot(ATermUtils.makeValue(anon)))));
			}
			else
				throw new IllegalArgumentException();

		return !getABox().isSatisfiable(test);
	}

	default boolean isInverse(final ATermAppl r1, final ATermAppl r2)
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

		// the following _condition is wrong due to nominals, see OWL test
		// cases SymmetricProperty-002
		// if( !role1.hasNamedInverse() )
		// return false;

		if (!role1.isObjectRole() || !role2.isObjectRole())
			return false;

		if (role1.getInverse().equals(role2))
			return true;

		ensureConsistency();

		final ATermAppl c = ATermUtils.makeTermAppl("_C_");
		final ATermAppl notC = ATermUtils.makeNot(c);

		final ATermAppl test = ATermUtils.makeAnd(c, ATermUtils.makeOr(ATermUtils.makeSomeValues(r1, ATermUtils.makeAllValues(r2, notC)), ATermUtils.makeSomeValues(r2, ATermUtils.makeAllValues(r1, notC))));

		return !getABox().isSatisfiable(test);
	}

	default boolean isTransitiveProperty(final ATermAppl r)
	{
		if (null == r)
			return false;

		final Role role = getRole(r);

		if (role == null)
		{
			Base.handleUndefinedEntity(r + _isNotAnKnowProperty);
			return false;
		}

		if (role.isTransitive())
		{
			if (doExplanation())
				getABox().setExplanation(role.getExplainTransitive());
			return true;
		}
		else
			if (!role.isObjectRole() || role.isFunctional() || role.isInverseFunctional())
				return false;

		ensureConsistency();

		final ATermAppl c = ATermUtils.makeTermAppl("_C_");
		final ATermAppl notC = ATermUtils.makeNot(c);
		final ATermAppl test = ATermUtils.makeAnd(ATermUtils.makeSomeValues(r, ATermUtils.makeSomeValues(r, c)), ATermUtils.makeAllValues(r, notC));

		return !getABox().isSatisfiable(test);
	}

	default boolean isSymmetricProperty(final ATermAppl p)
	{
		return p != null && isInverse(p, p);
	}

	default boolean isFunctionalProperty(final ATermAppl p)
	{
		if (null == p)
			return false;

		final Role role = getRole(p);

		if (role == null)
		{
			Base.handleUndefinedEntity(p + _isNotAnKnowProperty);
			return false;
		}

		if (role.isAnnotationRole())
			return false;

		if (role.isBottom())
		{
			if (doExplanation())
				getABox().setExplanation(DependencySet.INDEPENDENT);
			return true;
		}
		else
			if (role.isFunctional())
			{
				if (doExplanation())
					getABox().setExplanation(role.getExplainFunctional());
				return true;
			}
			else
				if (!role.isSimple())
					return false;

		final ATermAppl min2P = role.isDatatypeRole() ? ATermUtils.makeMin(p, 2, ATermUtils.TOP_LIT) : ATermUtils.makeMin(p, 2, ATermUtils.TOP);
		return !isSatisfiable(min2P);
	}

	default boolean isInverseFunctionalProperty(final ATermAppl p)
	{
		if (null == p)
			return false;

		final Role role = getRole(p);

		if (role == null)
		{
			Base.handleUndefinedEntity(p + _isNotAnKnowProperty);
			return false;
		}

		if (!role.isObjectRole())
			return false;
		else
			if (role.isInverseFunctional() || role.isBottom())
			{
				if (doExplanation())
					getABox().setExplanation(role.getExplainInverseFunctional());
				return true;
			}

		final ATermAppl invP = role.getInverse().getName();
		final ATermAppl max1invP = ATermUtils.makeMax(invP, 1, ATermUtils.TOP);
		return isSubClassOf(ATermUtils.TOP, max1invP);
	}

	default boolean hasDomain(final ATermAppl p, final ATermAppl c)
	{
		if (null == p || null == c)
			return false;

		final Role r = getRBox().getRole(p);
		if (r == null)
		{
			Base.handleUndefinedEntity(p + _isNotAnProperty);
			return false;
		}

		if (!isClass(c))
		{
			Base.handleUndefinedEntity(c + _isNotAValidClassExpression);
			return false;
		}

		final ATermAppl someP = ATermUtils.makeSomeValues(p, ATermUtils.getTop(r));
		return isSubClassOf(someP, c);
	}

	default boolean isReflexiveProperty(final ATermAppl p)
	{
		if (null == p)
			return false;

		final Role role = getRole(p);

		if (role == null)
		{
			Base.handleUndefinedEntity(p + _isNotAnKnowProperty);
			return false;
		}

		if (!role.isObjectRole() || role.isIrreflexive())
			return false;
		else
			if (role.isReflexive())
			{
				if (doExplanation())
					getABox().setExplanation(role.getExplainReflexive());
				return true;
			}

		ensureConsistency();

		final ATermAppl c = ATermUtils.makeTermAppl("_C_");
		final ATermAppl notC = ATermUtils.makeNot(c);
		final ATermAppl test = ATermUtils.makeAnd(c, ATermUtils.makeAllValues(p, notC));

		return !getABox().isSatisfiable(test);
	}

	default boolean isIrreflexiveProperty(final ATermAppl p)
	{
		if (null == p)
			return false;

		final Role role = getRole(p);

		if (role == null)
		{
			Base.handleUndefinedEntity(p + _isNotAnKnowProperty);
			return false;
		}

		if (!role.isObjectRole() || role.isReflexive())
			return false;
		else
			if (role.isIrreflexive())
			{
				if (doExplanation())
					getABox().setExplanation(role.getExplainIrreflexive());
				return true;
			}
			else
				if (role.isAsymmetric())
				{
					if (doExplanation())
						getABox().setExplanation(role.getExplainAsymmetric());
					return true;
				}

		ensureConsistency();

		final ATermAppl test = ATermUtils.makeSelf(p);

		return !getABox().isSatisfiable(test);
	}

	/**
	 * @param p
	 * @return DO NOT USE
	 * @deprecated Use {@link #isAsymmetricProperty(ATermAppl)}
	 */
	@Deprecated
	default boolean isAntisymmetricProperty(final ATermAppl p)
	{
		return isAsymmetricProperty(p);
	}

	default boolean isAsymmetricProperty(final ATermAppl p)
	{
		if (null == p)
			return false;

		final Role role = getRole(p);

		if (role == null)
		{
			Base.handleUndefinedEntity(p + _isNotAnKnowProperty);
			return false;
		}

		if (!role.isObjectRole())
			return false;
		else
			if (role.isAsymmetric())
			{
				if (doExplanation())
					getABox().setExplanation(role.getExplainAsymmetric());
				return true;
			}

		ensureConsistency();

		final ATermAppl o = ATermUtils.makeAnonNominal(Integer.MAX_VALUE);
		final ATermAppl nom = ATermUtils.makeValue(o);
		final ATermAppl test = ATermUtils.makeAnd(nom, ATermUtils.makeSomeValues(p, ATermUtils.makeAnd(ATermUtils.makeNot(nom), ATermUtils.makeSomeValues(p, nom))));

		return !getABox().isSatisfiable(test);
	}

	/**
	 * @return the set of all object properties.
	 */
	default Set<ATermAppl> getObjectProperties()
	{
		final Set<ATermAppl> set = new HashSet<>();
		for (final Role role : getRBox().getRoles().values())
		{
			final ATermAppl p = role.getName();
			if (ATermUtils.isPrimitive(p) && role.isObjectRole())
				set.add(p);
		}
		return set;
	}

	default Set<ATermAppl> getAnnotationProperties()
	{
		final Set<ATermAppl> set = new HashSet<>();
		for (final Role role : getRBox().getRoles().values())
		{
			final ATermAppl p = role.getName();
			if (ATermUtils.isPrimitive(p) && role.isAnnotationRole())
				set.add(p);
		}
		return set;
	}

	default Set<ATermAppl> getTransitiveProperties()
	{
		final Set<ATermAppl> set = new HashSet<>();
		for (final Role role : getRBox().getRoles().values())
		{
			final ATermAppl p = role.getName();
			if (ATermUtils.isPrimitive(p) && role.isTransitive())
				set.add(p);
		}
		set.add(ATermUtils.BOTTOM_OBJECT_PROPERTY);
		return set;
	}

	default Set<ATermAppl> getSymmetricProperties()
	{
		final Set<ATermAppl> set = new HashSet<>();
		for (final Role role : getRBox().getRoles().values())
		{
			final ATermAppl p = role.getName();
			if (ATermUtils.isPrimitive(p) && role.isSymmetric())
				set.add(p);
		}
		return set;
	}

	/**
	 * @return DO NOT USE
	 * @deprecated Use {@link #getAntisymmetricProperties()}
	 */
	@Deprecated
	default Set<ATermAppl> getAntisymmetricProperties()
	{
		return getAsymmetricProperties();
	}

	default Set<ATermAppl> getAsymmetricProperties()
	{
		final Set<ATermAppl> set = new HashSet<>();
		for (final Role role : getRBox().getRoles().values())
		{
			final ATermAppl p = role.getName();
			if (ATermUtils.isPrimitive(p) && role.isAsymmetric())
				set.add(p);
		}
		return set;
	}

	default Set<ATermAppl> getReflexiveProperties()
	{
		final Set<ATermAppl> set = new HashSet<>();
		for (final Role role : getRBox().getRoles().values())
		{
			final ATermAppl p = role.getName();
			if (ATermUtils.isPrimitive(p) && role.isReflexive())
				set.add(p);
		}
		return set;
	}

	default Set<ATermAppl> getIrreflexiveProperties()
	{
		final Set<ATermAppl> set = new HashSet<>();
		for (final Role role : getRBox().getRoles().values())
		{
			final ATermAppl p = role.getName();
			if (ATermUtils.isPrimitive(p) && role.isIrreflexive())
				set.add(p);
		}
		return set;
	}

	default Set<ATermAppl> getFunctionalProperties()
	{
		final Set<ATermAppl> set = new HashSet<>();
		for (final Role role : getRBox().getRoles().values())
		{
			final ATermAppl p = role.getName();
			if (ATermUtils.isPrimitive(p) && role.isFunctional())
				set.add(p);
		}
		set.add(ATermUtils.BOTTOM_DATA_PROPERTY);
		set.add(ATermUtils.BOTTOM_OBJECT_PROPERTY);
		return set;
	}

	default Set<ATermAppl> getInverseFunctionalProperties()
	{
		final Set<ATermAppl> set = getRBox().getRoles().values().stream()//
				.filter(role ->
				{
					final ATermAppl p = role.getName();
					return ATermUtils.isPrimitive(p) && role.isInverseFunctional();
				})//
				.map(Role::getName)//
				.collect(Collectors.toSet());
		set.add(ATermUtils.BOTTOM_OBJECT_PROPERTY);
		return set;
	}

	/**
	 * @return the set of all object properties.
	 */
	default Set<ATermAppl> getDataProperties()
	{
		final Set<ATermAppl> set = new HashSet<>();
		for (final Role role : getRBox().getRoles().values())
		{
			final ATermAppl p = role.getName();
			if (ATermUtils.isPrimitive(p) && role.isDatatypeRole())
				set.add(p);
		}
		return set;
	}

	/**
	 * The results of this function is not guaranteed to be complete. Use {@link #hasRange(ATermAppl, ATermAppl)} to get complete answers.
	 *
	 * @param name
	 * @return the domain restrictions on the property.
	 */
	public default Set<ATermAppl> getRanges(final ATerm name)
	{
		if (null == name)
			return Collections.emptySet();

		ensureConsistency();

		final Set<ATermAppl> set = Collections.emptySet();
		final Role prop = getRBox().getRole(name);
		if (prop == null)
		{
			Base.handleUndefinedEntity(name + _isNotAnProperty);
			return set;
		}

		return ATermUtils.primitiveOrBottom(prop.getRanges());
	}

	/**
	 * The results of this function is not guaranteed to be complete. Use {@link #hasDomain(ATermAppl, ATermAppl)} to get complete answers.
	 *
	 * @param name
	 * @return the domain restrictions on the property.
	 */
	public default Set<ATermAppl> getDomains(final ATermAppl name)
	{
		if (null == name)
			return Collections.emptySet();

		ensureConsistency();

		final Role prop = getRBox().getRole(name);
		if (prop == null)
		{
			Base.handleUndefinedEntity(name + _isNotAnProperty);
			return Collections.emptySet();
		}

		return ATermUtils.primitiveOrBottom(prop.getDomains());
	}

	/**
	 * Return all property values for a given object property and subject value.
	 *
	 * @param r
	 * @param x
	 * @return A list of ATermAppl objects
	 */
	public default Set<ATermAppl> getObjectPropertyValuesSet(final ATermAppl r, final ATermAppl x)
	{
		if (null == r || null == x)
			return Collections.emptySet();

		ensureConsistency();

		final Role role = getRBox().getRole(r);

		if (role == null || !role.isObjectRole())
		{
			Base.handleUndefinedEntity(r + _isNotAnKnowObjectProperty);
			return Collections.emptySet();
		}

		if (!isIndividual(x))
		{
			Base.handleUndefinedEntity(x + _isNotAnKnowIndividual);
			return Collections.emptySet();
		}

		// TODO get rid of unnecessary Set + List creation
		Set<ATermAppl> knowns = new HashSet<>();
		final Set<ATermAppl> unknowns = new HashSet<>();

		if (role.isTop())
		{
			if (!OpenlletOptions.HIDE_TOP_PROPERTY_VALUES)
				knowns = getIndividuals();
		}
		else
			if (!role.isBottom())
				getABox().getObjectPropertyValues(x, role, knowns, unknowns, true);

		if (!unknowns.isEmpty())
		{
			final ATermAppl valueX = ATermUtils.makeHasValue(role.getInverse().getName(), x);
			final ATermAppl c = ATermUtils.normalize(valueX);

			binaryInstanceRetrieval(c, new ArrayList<>(unknowns), knowns);
		}

		return knowns;
	}

	public default List<ATermAppl> getObjectPropertyValues(final ATermAppl r, final ATermAppl x)
	{
		return new ArrayList<>(getObjectPropertyValuesSet(r, x));
	}

	public default Stream<ATermAppl> objectPropertyValues(final ATermAppl r, final ATermAppl x)
	{
		return getObjectPropertyValuesSet(r, x).stream();
	}

	/**
	 * Return all literal values for a given dataproperty that belongs to the specified datatype.
	 *
	 * @param r
	 * @param x
	 * @param datatype
	 * @return List of ATermAppl objects representing literals.
	 */
	public default List<ATermAppl> getDataPropertyValues(final ATermAppl r, final ATermAppl x, final ATermAppl datatype)
	{
		if (null == r || null == x)
			return Collections.emptyList();

		ensureConsistency();

		final Individual ind = getABox().getIndividual(x);
		final Role role = getRBox().getRole(r);

		if (ind == null)
		{
			Base.handleUndefinedEntity(x + _isNotAnIndividual);
			return Collections.emptyList();
		}

		if (role == null || !role.isDatatypeRole())
		{
			Base.handleUndefinedEntity(r + _isNotAnKnowDataProperty);
			return Collections.emptyList();
		}

		if (role.isTop())
		{
			final List<ATermAppl> literals = new ArrayList<>();
			if (!OpenlletOptions.HIDE_TOP_PROPERTY_VALUES)
				for (final Node node : getABox().getNodes().values())
					if (node.isLiteral() && node.getTerm() != null)
						literals.add(node.getTerm());
			return literals;
		}
		else
			if (role.isBottom())
				return Collections.emptyList();
			else
				return getABox().getDataPropertyValues(x, role, datatype);
	}

	/**
	 * List all subjects with the given value for the specified object property.
	 *
	 * @param r
	 * @param o An ATerm object that is the URI of an _individual
	 * @return List of ATermAppl objects.
	 */
	public default List<ATermAppl> getIndividualsWithObjectProperty(final ATermAppl r, final ATermAppl o)
	{
		if (null == r || null == o)
			return Collections.emptyList();

		ensureConsistency();

		if (!isIndividual(o))
		{
			Base.handleUndefinedEntity(o + _isNotAnIndividual);
			return Collections.emptyList();
		}

		final Role role = getRBox().getRole(r);

		final ATermAppl invR = role.getInverse().getName();

		return getObjectPropertyValues(invR, o);
	}

	/**
	 * List all subjects with the given literal value for the specified _data property.
	 *
	 * @param r An ATerm object that contains the literal value in the form literal(lexicalValue, langIdentifier, datatypeURI). Should be created with
	 *        ATermUtils.makeXXXLiteral() functions.
	 * @param litValue
	 * @return List of ATermAppl objects.
	 */
	public default List<ATermAppl> getIndividualsWithDataProperty(final ATermAppl r, final ATermAppl litValue)
	{
		if (null == r || null == litValue)
			return Collections.emptyList();

		if (!ATermUtils.isLiteral(litValue))
			return Collections.emptyList();

		ensureConsistency();

		final List<ATermAppl> knowns = new ArrayList<>();
		final List<ATermAppl> unknowns = new ArrayList<>();

		ATermAppl canonicalLit;
		try
		{
			canonicalLit = getDatatypeReasoner().getCanonicalRepresentation(litValue);
		}
		catch (final InvalidLiteralException e)
		{
			getLogger().warning(format("Invalid literal '%s' passed as input, returning empty set of _individuals: %s", litValue, e.getMessage()));
			return Collections.emptyList();
		}
		catch (final UnrecognizedDatatypeException e)
		{
			getLogger().warning(format("Unrecognized datatype for literal '%s' passed as input, returning empty set of _individuals: %s", litValue, e.getMessage()));
			return Collections.emptyList();
		}
		final Literal literal = getABox().getLiteral(canonicalLit);

		if (literal != null)
		{
			final Role role = getRole(r);
			final EdgeList edges = literal.getInEdges();
			for (final Edge edge : edges)
				if (edge.getRole().isSubRoleOf(role))
				{
					final ATermAppl subj = edge.getFrom().getName();
					if (edge.getDepends().isIndependent())
						knowns.add(subj);
					else
						unknowns.add(subj);
				}

			if (!unknowns.isEmpty())
			{
				final ATermAppl c = ATermUtils.normalize(ATermUtils.makeHasValue(r, litValue));

				binaryInstanceRetrieval(c, unknowns, knowns);
			}
		}

		return knowns;
	}

	public default Set<ATermAppl> getIndividualsWithAnnotation(final ATermAppl p, final ATermAppl o)
	{
		if (null == p || null == o)
			return Collections.emptySet();

		final Set<ATermAppl> ret = new HashSet<>();

		for (final Map.Entry<ATermAppl, Map<ATermAppl, Set<ATermAppl>>> e1 : getAnnotations().entrySet())
		{
			final ATermAppl st = e1.getKey();
			final Map<ATermAppl, Set<ATermAppl>> pidx = e1.getValue();

			for (final Map.Entry<ATermAppl, Set<ATermAppl>> e2 : pidx.entrySet())
			{
				final ATermAppl pt = e2.getKey();
				final Set<ATermAppl> oidx = e2.getValue();

				if (pt.equals(p) && oidx.contains(o))
					ret.add(st);
			}
		}

		return ret;
	}

	/**
	 * List all subjects with a given property and property value.
	 *
	 * @param r
	 * @param x If property is an object property an ATermAppl object that is the URI of the _individual, if the property is a _data property an ATerm object
	 *        that contains the literal value (See {#link #getIndividualsWithDataProperty(ATermAppl, ATermAppl)} for details)
	 * @return List of ATermAppl objects.
	 */
	public default List<ATermAppl> getIndividualsWithProperty(final ATermAppl r, final ATermAppl x)
	{
		final Role role = getRBox().getRole(r);

		if (role == null)
		{
			Base.handleUndefinedEntity(r + _isNotAnKnowProperty);
			return Collections.emptyList();
		}

		if (role.isObjectRole())
			return getIndividualsWithObjectProperty(r, x);
		else
			if (role.isDatatypeRole())
				return getIndividualsWithDataProperty(r, x);
			else
				if (role.isAnnotationRole())
					return Arrays.asList(getIndividualsWithAnnotation(r, x).toArray(new ATermAppl[0]));
				else
					throw new IllegalArgumentException();
	}

	/**
	 * Return all literal values for a given dataproperty that has the specified language identifier.
	 *
	 * @param r
	 * @param x
	 * @param lang
	 * @return List of ATermAppl objects.
	 */
	public default List<ATermAppl> getDataPropertyValues(final ATermAppl r, final ATermAppl x, final String lang)
	{
		if (null == r || null == x)
			return Collections.emptyList();

		final List<ATermAppl> values = getDataPropertyValues(r, x);
		if (lang == null)
			return values;

		final List<ATermAppl> result = new ArrayList<>();
		for (final ATermAppl lit : values)
		{
			final String litLang = ((ATermAppl) lit.getArgument(1)).getName();

			if (litLang.equals(lang))
				result.add(lit);
		}

		return result;
	}

	/**
	 * Return all literal values for a given dataproperty and subject value.
	 *
	 * @param r
	 * @param x
	 * @return List of ATermAppl objects.
	 */
	public default List<ATermAppl> getDataPropertyValues(final ATermAppl r, final ATermAppl x)
	{
		return getDataPropertyValues(r, x, (ATermAppl) null);
	}

	/**
	 * Return all property values for a given property and subject value.
	 *
	 * @param r
	 * @param x
	 * @return List of ATermAppl objects.
	 */
	public default List<ATermAppl> getPropertyValues(final ATermAppl r, final ATermAppl x)
	{
		if (null == r || null == x)
			return Collections.emptyList();

		final Role role = getRBox().getRole(r);

		if (role == null || role.isUntypedRole())
		{
			Base.handleUndefinedEntity(r + _isNotAnKnowProperty);
			return Collections.emptyList();
		}

		if (role.isObjectRole())
			return getObjectPropertyValues(r, x);
		else
			if (role.isDatatypeRole())
				return getDataPropertyValues(r, x);
			else
				if (role.isAnnotationRole())
				{
					final Set<ATermAppl> values = getAnnotations(x, r);
					return values.isEmpty() ? Collections.<ATermAppl> emptyList() : Arrays.asList(values.toArray(new ATermAppl[0]));
				}
				else
					throw new IllegalArgumentException();
	}

	public default Map<ATermAppl, List<ATermAppl>> getPropertyValues(final ATermAppl pred)
	{
		if (null == pred)
			return Collections.emptyMap();

		final Map<ATermAppl, List<ATermAppl>> result = new HashMap<>();

		for (final ATermAppl subj : currentIndividuals())
		{
			final List<ATermAppl> objects = getPropertyValues(pred, subj);
			if (!objects.isEmpty())
				result.put(subj, objects);
		}

		return result;
	}

	/**
	 * @param s subject
	 * @param o object
	 * @return all properties asserted between a subject and object.
	 */
	public default List<ATermAppl> getProperties(final ATermAppl s, final ATermAppl o)
	{
		if (!isIndividual(s))
		{
			Base.handleUndefinedEntity(s + _isNotAnIndividual);
			return Collections.emptyList();
		}

		if (!isIndividual(o) && !ATermUtils.isLiteral(o))
		{
			Base.handleUndefinedEntity(o + _isNotAnIndividual);
			return Collections.emptyList();
		}

		final List<ATermAppl> props = new ArrayList<>();

		final Set<ATermAppl> allProps = ATermUtils.isLiteral(o) ? getDataProperties() : getObjectProperties();
		for (final ATermAppl p : allProps)
			if (getABox().hasPropertyValue(s, p, o))
				props.add(p);

		return props;
	}

	/**
	 * Temporary method until we incorporate annotation properties to the taxonomy ([t:412])
	 *
	 * @param p
	 * @return
	 */
	public default Set<ATermAppl> getSubAnnotationProperties(final ATermAppl p)
	{
		if (null == p)
			return Collections.emptySet();

		final Set<ATermAppl> values = new HashSet<>();

		final List<ATermAppl> temp = new ArrayList<>();
		temp.add(p);
		while (!temp.isEmpty())
		{
			final ATermAppl value = temp.remove(0);
			values.add(value);

			for (final ATermAppl property : getAnnotationProperties())
				if (value != property && isSubPropertyOf(property, value))
					temp.add(property);
		}

		return values;
	}

	public default Set<ATermAppl> getAnnotations(final ATermAppl s, final ATermAppl p)
	{
		if (null == s || null == p)
			return Collections.emptySet();

		final Map<ATermAppl, Set<ATermAppl>> pidx = getAnnotations().get(s);

		if (pidx == null)
			return Collections.emptySet();

		final Set<ATermAppl> values = new HashSet<>();

		for (final ATermAppl subproperty : getSubAnnotationProperties(p))
			if (pidx.get(subproperty) != null)
				for (final ATermAppl value : pidx.get(subproperty))
					values.add(value);

		return values;
	}
}
