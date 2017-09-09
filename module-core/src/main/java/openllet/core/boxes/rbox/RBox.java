package openllet.core.boxes.rbox;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.atom.OpenError;
import openllet.core.DependencySet;
import openllet.core.FSMBuilder;
import openllet.core.OpenlletOptions;
import openllet.core.PropertyType;
import openllet.core.exceptions.UnsupportedFeatureException;
import openllet.core.taxonomy.Taxonomy;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.SetUtils;
import openllet.shared.tools.Logging;

/**
 * Definition of an rbox.
 *
 * @since 2.6.0
 */
public interface RBox extends Logging
{
	public Taxonomy<ATermAppl> getObjectTaxonomy();

	public void setObjectTaxonomy(Taxonomy<ATermAppl> objectTaxonomy);

	public Taxonomy<ATermAppl> getDataTaxonomy();

	public void setDataTaxonomy(Taxonomy<ATermAppl> dataTaxonomy);

	public Taxonomy<ATermAppl> getAnnotationTaxonomy();

	public void setAnnotationTaxonomy(Taxonomy<ATermAppl> annotationTaxonomy);

	public Map<ATermAppl, Role> getRoles();

	public Set<Role> getReflexiveRoles();

	public Map<Role, Map<ATermAppl, Set<Set<ATermAppl>>>> getDomainAssertions();

	public Map<Role, Map<ATermAppl, Set<Set<ATermAppl>>>> getRangeAssertions();

	public FSMBuilder getFsmBuilder();

	//	/**
	//	 * @return Returns the getRoles().
	//	 */
	//	default Set<ATermAppl> getRoleNames()
	//	{
	//		return getRoles().keySet();
	//	}

	public boolean isObjectTaxonomyPrepared();

	public boolean isDataTaxonomyPrepared();

	public boolean isAnnotationTaxonomyPrepared();

	/**
	 * @param r Name (URI) of the role
	 * @return the role with the given name
	 */
	default Role getRole(final ATerm r)
	{
		return null != r ? getRoles().get(r) : null;
	}

	/**
	 * @param r Name (URI) of the role
	 * @return the role with the given name and throw and exception if it is not found.
	 */
	default Role getDefinedRole(final ATerm r)
	{
		final Role role = getRoles().get(r);

		if (role == null)
			throw new OpenError(r + " is not defined as a property");

		return role;
	}

	default Role addRole(final ATermAppl r)
	{
		Role role = getRole(r);

		if (role == null)
		{
			role = new RoleImpl(r, PropertyType.UNTYPED);
			getRoles().put(r, role);
		}

		return role;
	}

	abstract void propogateDomain(final Role role, final Map<ATermAppl, Set<Set<ATermAppl>>> domains);

	abstract void propogateRange(final Role role, final Map<ATermAppl, Set<Set<ATermAppl>>> ranges);

	abstract void computeImmediateSubRoles(final Role r, final Map<ATerm, DependencySet> subs);

	abstract void computeSubRoles(final Role r, final Set<Role> subRoles, final Set<ATermList> subRoleChains, final Map<ATerm, DependencySet> dependencies, final DependencySet ds);

	/**
	 * Add a non-asserted property range axiom
	 *
	 * @param p The property
	 * @param range
	 * @param explanation
	 * @param a A class expression for the domain
	 * @param clashExplanation A set of {@link ATermAppl}s that explain the range axiom.
	 * @return <code>true</code> if range add was successful, <code>false</code> else
	 * @throws IllegalArgumentException if <code>p</code> is not a defined property.
	 */
	default boolean addRange(final ATerm p, final ATermAppl range, final Set<ATermAppl> explanation)
	{
		final Role r = getRole(p);
		if (r == null)
			throw new IllegalArgumentException(p + " is not defined as a property");

		Map<ATermAppl, Set<Set<ATermAppl>>> ranges = getRangeAssertions().get(r);
		if (ranges == null)
		{
			ranges = new ConcurrentHashMap<>();
			getRangeAssertions().put(r, ranges);
		}

		Set<Set<ATermAppl>> allExplanations = ranges.get(range);
		if (allExplanations == null)
		{
			allExplanations = new HashSet<>();
			ranges.put(range, allExplanations);
		}

		return allExplanations.add(explanation);
	}

	/**
	 * Add an asserted property range axiom
	 *
	 * @param p The property
	 * @param range A class expression for the range
	 * @return <code>true</code> if range add was successful, <code>false</code> else
	 * @throws IllegalArgumentException if <code>p</code> is not a defined property.
	 */
	default boolean addRange(final ATerm p, final ATermAppl range)
	{
		final Set<ATermAppl> ds = Collections.singleton(ATermUtils.makeRange(p, range));

		return addRange(p, range, ds);
	}

	default Role addObjectRole(final ATermAppl r)
	{
		Role role = getRole(r);
		final PropertyType roleType = role == null ? PropertyType.UNTYPED : role.getType();

		switch (roleType)
		{
			case DATATYPE:
				role = null;
				break;
			case OBJECT:
				break;
			default:
				if (role == null)
				{
					role = new RoleImpl(r, PropertyType.OBJECT);
					getRoles().put(r, role);
				}
				else
					role.setType(PropertyType.OBJECT);

				final ATermAppl invR = ATermUtils.makeInv(r);
				final Role invRole = new RoleImpl(invR, PropertyType.OBJECT);
				getRoles().put(invR, invRole);

				role.setInverse(invRole);
				invRole.setInverse(role);

				addSubRole(ATermUtils.BOTTOM_OBJECT_PROPERTY, role.getName(), DependencySet.INDEPENDENT);
				addSubRole(role.getName(), ATermUtils.TOP_OBJECT_PROPERTY, DependencySet.INDEPENDENT);
				addSubRole(ATermUtils.BOTTOM_OBJECT_PROPERTY, role.getName(), DependencySet.INDEPENDENT);
				addSubRole(role.getName(), ATermUtils.TOP_OBJECT_PROPERTY, DependencySet.INDEPENDENT);

				break;
		}

		return role;
	}

	default Role addDatatypeRole(final ATermAppl r)
	{
		Role role = getRole(r);

		if (role == null)
		{
			role = new RoleImpl(r, PropertyType.DATATYPE);
			getRoles().put(r, role);

			addSubRole(ATermUtils.BOTTOM_DATA_PROPERTY, role.getName(), DependencySet.INDEPENDENT);
			addSubRole(role.getName(), ATermUtils.TOP_DATA_PROPERTY, DependencySet.INDEPENDENT);
		}
		else
			switch (role.getType())
			{
				case DATATYPE:
					break;
				case OBJECT:
					role = null;
					break;
				default:
					role.setType(PropertyType.DATATYPE);
					addSubRole(ATermUtils.BOTTOM_DATA_PROPERTY, role.getName(), DependencySet.INDEPENDENT);
					addSubRole(role.getName(), ATermUtils.TOP_DATA_PROPERTY, DependencySet.INDEPENDENT);
					break;
			}

		return role;
	}

	default Role addAnnotationRole(final ATermAppl r)
	{
		Role role = getRole(r);

		if (role == null)
		{
			role = new RoleImpl(r, PropertyType.ANNOTATION);
			getRoles().put(r, role);
		}
		else
			switch (role.getType())
			{
				case ANNOTATION:
					break;
				case OBJECT:
					role = null;
					break;
				default:
					role.setType(PropertyType.ANNOTATION);
					break;
			}

		return role;
	}

	default boolean addSubRole(final ATerm sub, final ATerm sup)
	{
		final DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(ATermUtils.makeSubProp(sub, sup)) : DependencySet.INDEPENDENT;
		return addSubRole(sub, sup, ds);
	}

	default boolean addSubRole(final ATerm sub, final ATerm sup, final DependencySet ds)
	{
		final Role roleSup = getRole(sup);
		final Role roleSub = getRole(sub);

		if (roleSup == null)
			return false;
		else
			if (sub.getType() == ATerm.LIST)
				roleSup.addSubRoleChain((ATermList) sub, ds);
			else
				if (roleSub == null)
					return false;
				else
				{
					roleSup.addSubRole(roleSub, ds);
					roleSub.addSuperRole(roleSup, ds);
				}

		// TODO Need to figure out what to do about about role lists
		// explanationTable.add(ATermUtils.makeSub(sub, sup), ds);
		return true;
	}

	default boolean addEquivalentRole(final ATerm s, final ATerm r)
	{
		final DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(ATermUtils.makeEqProp(s, r)) : DependencySet.INDEPENDENT;
		return addEquivalentRole(r, s, ds);
	}

	default boolean addEquivalentRole(final ATerm s, final ATerm r, final DependencySet ds)
	{
		final Role roleS = getRole(s);
		final Role roleR = getRole(r);

		if (roleS == null || roleR == null)
			return false;

		roleR.addSubRole(roleS, ds);
		roleR.addSuperRole(roleS, ds);
		roleS.addSubRole(roleR, ds);
		roleS.addSuperRole(roleR, ds);

		if (roleR.getInverse() != null)
		{
			roleR.getInverse().addSubRole(roleS.getInverse(), ds);
			roleR.getInverse().addSuperRole(roleS.getInverse(), ds);
			roleS.getInverse().addSubRole(roleR.getInverse(), ds);
			roleS.getInverse().addSuperRole(roleR.getInverse(), ds);
		}

		return true;
	}

	default boolean addDisjointRole(final ATerm s, final ATerm r, final DependencySet ds)
	{
		final Role roleS = getRole(s);
		final Role roleR = getRole(r);

		if (roleS == null || roleR == null)
			return false;

		roleR.addDisjointRole(roleS, ds);
		roleS.addDisjointRole(roleR, ds);

		return true;
	}

	/**
	 * Add a non-asserted property domain axiom
	 *
	 * @param p The property
	 * @param domain A class expression for the domain
	 * @param explanation A set of {@link ATermAppl}s that explain the domain axiom.
	 * @return <code>true</code> if domain add was successful, <code>false</code> else
	 * @throws IllegalArgumentException if <code>p</code> is not a defined property.
	 */
	default boolean addDomain(final ATerm p, final ATermAppl domain, final Set<ATermAppl> explanation)
	{
		final Role r = getRole(p);
		if (r == null)
			throw new IllegalArgumentException(p + " is not defined as a property");

		Map<ATermAppl, Set<Set<ATermAppl>>> domains = getDomainAssertions().get(r);
		if (domains == null)
		{
			domains = new ConcurrentHashMap<>();
			getDomainAssertions().put(r, domains);
		}

		Set<Set<ATermAppl>> allExplanations = domains.get(domain);
		if (allExplanations == null)
		{
			allExplanations = SetUtils.create();
			domains.put(domain, allExplanations);
		}

		return allExplanations.add(explanation);
	}

	/**
	 * Add an asserted property domain axiom
	 *
	 * @param p The property
	 * @param a A class expression for the domain
	 * @return <code>true</code> if domain add was successful, <code>false</code> else
	 * @throws IllegalArgumentException if <code>p</code> is not a defined property.
	 */
	default boolean addDomain(final ATerm p, final ATermAppl a)
	{
		final Set<ATermAppl> explain = Collections.singleton(ATermUtils.makeDomain(p, a));

		return addDomain(p, a, explain);
	}

	default boolean addInverseRole(final ATerm s, final ATerm r, final DependencySet ds)
	{
		final Role roleS = getRole(s);
		final Role roleR = getRole(r);

		if (roleS == null || roleR == null || !roleS.isObjectRole() || !roleR.isObjectRole())
			return false;
		else
			addEquivalentRole(roleS.getInverse().getName(), r, ds);

		return true;
	}

	public Iterator<ATermAppl> getAssertedDomains(final Role r);

	public Iterator<ATermAppl> getAssertedRanges(final Role r);

	//	@Deprecated
	//	default boolean isDomainAsserted(final ATerm p, final ATermAppl domain)
	//	{
	//		final Role r = getRole(p);
	//		if (r == null)
	//			throw new IllegalArgumentException(p + " is not defined as a property");
	//
	//		final Map<ATermAppl, Set<Set<ATermAppl>>> domains = getDomainAssertions().get(r);
	//		if (domains == null)
	//			return false;
	//
	//		final Set<Set<ATermAppl>> allExplanations = domains.get(domain);
	//		if (allExplanations == null)
	//			return false;
	//
	//		final Set<ATermAppl> explanation = Collections.singleton(ATermUtils.makeDomain(p, domain));
	//
	//		return allExplanations.contains(explanation);
	//	}

	//	@Deprecated
	//	default boolean isRangeAsserted(final ATerm p, final ATermAppl range)
	//	{
	//		final Role r = getRole(p);
	//		if (r == null)
	//			throw new IllegalArgumentException(p + " is not defined as a property");
	//
	//		final Map<ATermAppl, Set<Set<ATermAppl>>> ranges = getRangeAssertions().get(r);
	//		if (ranges == null)
	//			return false;
	//
	//		final Set<Set<ATermAppl>> allExplanations = ranges.get(range);
	//		if (allExplanations == null)
	//			return false;
	//
	//		final Set<ATermAppl> explanation = Collections.singleton(ATermUtils.makeRange(p, range));
	//
	//		return allExplanations.contains(explanation);
	//	}

	/**
	 * @param r
	 * @return true if the term is declared as a role
	 */
	default boolean isRole(final ATerm r)
	{
		return getRoles().containsKey(r);
	}

	default void prepare()
	{

		// first pass - compute sub getRoles()
		final Set<Role> complexRoles = SetUtils.create();
		for (final Role role : getRoles().values())
		{
			final Map<ATerm, DependencySet> subExplain = new ConcurrentHashMap<>();
			final Set<Role> subRoles = SetUtils.create();
			final Set<ATermList> subRoleChains = SetUtils.create();

			computeSubRoles(role, subRoles, subRoleChains, subExplain, DependencySet.INDEPENDENT);

			role.setSubRolesAndChains(subRoles, subRoleChains, subExplain);

			for (final Role s : subRoles)
			{
				final DependencySet explainSub = role.getExplainSub(s.getName());
				s.addSuperRole(role, explainSub);
			}

			for (final ATermList chain : subRoleChains)
				if (chain.getLength() != 2 || !chain.getFirst().equals(chain.getLast()) || !subRoles.contains(getRole(chain.getFirst())))
				{
					role.setHasComplexSubRole(true);
					complexRoles.add(role);
					break;
				}
		}

		// iterate over complex getRoles() to build DFAs - needs to be done after
		// all subRoles are propagated above
		for (final Role s : complexRoles)
			getFsmBuilder().build(s);

		// second pass - set super getRoles() and propagate disjoint getRoles() through inverses
		for (final Role role : getRoles().values())
		{
			final Role invR = role.getInverse();
			if (invR != null)
			{
				if (invR.isTransitive() && !role.isTransitive())
					role.setTransitive(true, invR.getExplainTransitive());
				else
					if (role.isTransitive() && !invR.isTransitive())
						invR.setTransitive(true, role.getExplainTransitive());
				if (invR.isFunctional() && !role.isInverseFunctional())
					role.setInverseFunctional(true, invR.getExplainFunctional());
				if (role.isFunctional() && !invR.isInverseFunctional())
					invR.setInverseFunctional(true, role.getExplainFunctional());
				if (invR.isInverseFunctional() && !role.isFunctional())
					role.setFunctional(true, invR.getExplainInverseFunctional());
				if (invR.isAsymmetric() && !role.isAsymmetric())
					role.setAsymmetric(true, invR.getExplainAsymmetric());
				if (role.isAsymmetric() && !invR.isAsymmetric())
					invR.setAsymmetric(true, role.getExplainAsymmetric());
				if (invR.isReflexive() && !role.isReflexive())
					role.setReflexive(true, invR.getExplainReflexive());
				if (role.isReflexive() && !invR.isReflexive())
					invR.setReflexive(true, role.getExplainReflexive());

				for (final Role disjointR : role.getDisjointRoles())
					invR.addDisjointRole(disjointR.getInverse(), role.getExplainDisjointRole(disjointR));
			}

			for (final Role s : role.getSubRoles())
			{
				if (role.isForceSimple())
					s.setForceSimple(true);
				if (!s.isSimple())
					role.setSimple(false);
			}
		}

		// third pass - set transitivity and functionality and propagate disjoint getRoles() through subs
		for (final Role r : getRoles().values())
		{
			if (r.isForceSimple())
			{
				if (!r.isSimple())
					ignoreTransitivity(r);
			}
			else
			{
				boolean isTransitive = r.isTransitive();
				DependencySet transitiveDS = r.getExplainTransitive();
				for (final Role s : r.getSubRoles())
					if (s.isTransitive())
					{
						if (r.isSubRoleOf(s) && r != s)
						{
							isTransitive = true;
							transitiveDS = r.getExplainSub(s.getName()).union(s.getExplainTransitive(), true);
						}
						r.addTransitiveSubRole(s);
					}
				if (isTransitive != r.isTransitive())
					r.setTransitive(isTransitive, transitiveDS);
			}

			if (r.isFunctional())
				r.addFunctionalSuper(r);

			for (final Role s : r.getSuperRoles())
			{
				if (s.equals(r))
					continue;

				final DependencySet supDS = OpenlletOptions.USE_TRACING ? r.getExplainSuper(s.getName()) : DependencySet.INDEPENDENT;

				if (s.isFunctional())
				{
					final DependencySet ds = OpenlletOptions.USE_TRACING ? supDS.union(s.getExplainFunctional(), true) : DependencySet.INDEPENDENT;
					r.setFunctional(true, ds);
					r.addFunctionalSuper(s);
				}
				if (s.isIrreflexive() && !r.isIrreflexive())
				{
					final DependencySet ds = OpenlletOptions.USE_TRACING ? supDS.union(s.getExplainIrreflexive(), true) : DependencySet.INDEPENDENT;
					r.setIrreflexive(true, ds);
				}
				if (s.isAsymmetric() && !r.isAsymmetric())
				{
					final DependencySet ds = OpenlletOptions.USE_TRACING ? supDS.union(s.getExplainAsymmetric(), true) : DependencySet.INDEPENDENT;
					r.setAsymmetric(true, ds);
				}

				// create a duplicate array to avoid ConcurrentModificationException
				for (final Role disjointR : s.getDisjointRoles().toArray(new RoleImpl[0]))
				{
					final DependencySet ds = OpenlletOptions.USE_TRACING ? supDS.union(s.getExplainDisjointRole(disjointR), true) : DependencySet.INDEPENDENT;
					r.addDisjointRole(disjointR, ds);
					disjointR.addDisjointRole(r, ds);
				}
			}

			if (r.isReflexive() && !r.isAnon())
				getReflexiveRoles().add(r);

			getLogger().fine(() -> r.debugString());
		}

		// we will compute the taxonomy when we need it
		setObjectTaxonomy(null);
		setDataTaxonomy(null);
		setAnnotationTaxonomy(null);
	}

	default void propagateDomainRange()
	{
		for (final Role role : getRoles().values())
			role.resetDomainRange();

		for (final Role role : getRoles().values())
		{
			final Role invRole = role.getInverse();
			if (invRole != null)
			{
				final Map<ATermAppl, Set<Set<ATermAppl>>> invDomains = getDomainAssertions().get(invRole);
				final Map<ATermAppl, Set<Set<ATermAppl>>> invRanges = getRangeAssertions().get(invRole);

				propogateDomain(role, invRanges);
				propogateRange(role, invDomains);
			}

			final Map<ATermAppl, Set<Set<ATermAppl>>> domains = getDomainAssertions().get(role);
			final Map<ATermAppl, Set<Set<ATermAppl>>> ranges = getRangeAssertions().get(role);
			propogateDomain(role, domains);
			propogateRange(role, ranges);
		}
	}

	default boolean removeDomain(final ATerm p, final ATermAppl domain)
	{
		if (!OpenlletOptions.USE_TRACING)
			return false;

		final Role r = getRole(p);
		if (r == null)
			return false;

		final Map<ATermAppl, Set<Set<ATermAppl>>> domains = getDomainAssertions().get(r);
		if (domains == null)
			return false;

		final Set<Set<ATermAppl>> allExplanations = domains.get(domain);
		if (allExplanations == null)
			return false;

		final Set<ATermAppl> explanation = Collections.singleton(ATermUtils.makeDomain(p, domain));

		if (!allExplanations.remove(explanation))
			return false;

		if (allExplanations.isEmpty())
			domains.remove(domain);

		return true;
	}

	default boolean removeRange(final ATerm p, final ATermAppl range)
	{
		if (!OpenlletOptions.USE_TRACING)
			return false;

		final Role r = getRole(p);
		if (r == null)
			return false;

		final Map<ATermAppl, Set<Set<ATermAppl>>> ranges = getRangeAssertions().get(r);
		if (ranges == null)
			return false;

		final Set<Set<ATermAppl>> allExplanations = ranges.get(range);
		if (allExplanations == null)
			return false;

		final Set<ATermAppl> explanation = Collections.singleton(ATermUtils.makeRange(p, range));

		if (!allExplanations.remove(explanation))
			return false;

		if (allExplanations.isEmpty())
			ranges.remove(range);

		return true;
	}

	default void ignoreTransitivity(final Role role)
	{
		final Role namedRole = role.isAnon() ? role.getInverse() : role;

		final String msg = "Unsupported axiom: Ignoring transitivity and/or complex subproperty axioms for " + namedRole;

		if (!OpenlletOptions.IGNORE_UNSUPPORTED_AXIOMS)
			throw new UnsupportedFeatureException(msg);

		getLogger().warning(msg);

		role.removeSubRoleChains();
		role.setHasComplexSubRole(false);
		role.setSimple(true);
		role.setFSM(null);

		role.getInverse().removeSubRoleChains();
		role.getInverse().setHasComplexSubRole(false);
		role.getInverse().setSimple(true);
		role.getInverse().setFSM(null);
	}

	/**
	 * For each role in the list finds an inverse role and returns the new list.
	 *
	 * @param roles
	 * @return inverses of the roles
	 */
	default ATermList inverse(final ATermList roles)
	{
		ATermList invList = ATermUtils.EMPTY_LIST;

		for (ATermList list = roles; !list.isEmpty(); list = list.getNext())
		{
			final ATermAppl r = (ATermAppl) list.getFirst();
			final Role role = getRole(r);
			final Role invR = role.getInverse();
			if (invR == null)
				System.err.println("Property " + r + " was supposed to be an ObjectProperty but it is not!");
			else
				invList = invList.insert(invR.getName());
		}

		return invList;
	}

}
