package openllet.core.boxes.rbox;

import java.util.Map;
import java.util.Set;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.core.DependencySet;
import openllet.core.PropertyType;
import openllet.core.utils.fsm.TransitionGraph;

/**
 * Definition of Role.
 *
 * @since 2.6.0
 */
public interface Role
{
	String debugString();

	/**
	 * Add a sub role chain without dependency tracking information
	 *
	 * @param chain
	 */
	void addSubRoleChain(final ATermList chain);

	/**
	 * Add a sub role chain with dependency tracking.
	 *
	 * @param chain List of role names of at least length 2.
	 * @param ds
	 */
	void addSubRoleChain(final ATermList chain, final DependencySet ds);

	void removeSubRoleChain(final ATermList chain);

	void removeSubRoleChains();

	/**
	 * r is subrole of this role
	 *
	 * @param r
	 */
	void addSubRole(final Role r);

	/**
	 * Add sub role with depedency set.
	 *
	 * @param r  subrole of this role
	 * @param ds
	 */
	void addSubRole(final Role r, final DependencySet ds);

	boolean removeDomain(final ATermAppl a, final DependencySet ds);

	boolean removeRange(final ATermAppl a, final DependencySet ds);

	void resetDomainRange();

	void removeSubRole(final Role r);

	/**
	 * r is superrole of this role
	 *
	 * @param r
	 */
	void addSuperRole(final Role r);

	void addSuperRole(final Role r, final DependencySet ds);

	void addDisjointRole(final Role r, final DependencySet ds);

	boolean addDomain(final ATermAppl a, final DependencySet ds);

	boolean addRange(final ATermAppl a, final DependencySet ds);

	boolean isObjectRole();

	boolean isDatatypeRole();

	/**
	 * @return true if a role is declared as datatype property
	 */
	boolean isAnnotationRole();

	boolean isUntypedRole();

	Role getInverse();

	boolean hasNamedInverse();

	boolean hasComplexSubRole();

	boolean isFunctional();

	boolean isInverseFunctional();

	boolean isSymmetric();

	boolean isAsymmetric();

	boolean isTransitive();

	boolean isReflexive();

	boolean isIrreflexive();

	boolean isAnon();

	ATermAppl getName();

	Set<ATermAppl> getDomains();

	Set<ATermAppl> getRanges();

	Set<Role> getSubRoles();

	Set<Role> getEquivalentProperties();

	boolean isEquivalent(final Role r);

	Set<Role> getProperSubRoles();

	Set<ATermList> getSubRoleChains();

	Set<Role> getSuperRoles();

	Set<Role> getDisjointRoles();

	DependencySet getExplainDisjointRole(final Role role);

	PropertyType getType();

	String getTypeName();

	boolean isSubRoleOf(final Role r);

	boolean isSuperRoleOf(final Role r);

	void setInverse(final Role term);

	void setFunctional(final boolean b);

	void setFunctional(final boolean b, final DependencySet ds);

	void setInverseFunctional(final boolean b);

	void setInverseFunctional(final boolean b, final DependencySet ds);

	void setTransitive(final boolean b);

	void setTransitive(final boolean b, final DependencySet ds);

	void setReflexive(final boolean b);

	void setReflexive(final boolean b, final DependencySet ds);

	void setIrreflexive(final boolean b);

	void setIrreflexive(final boolean b, final DependencySet ds);

	void setAsymmetric(final boolean b);

	void setAsymmetric(final boolean b, final DependencySet ds);

	void setHasComplexSubRole(final boolean b);

	void setType(final PropertyType type);

	/**
	 * @param subRoles
	 * @param subRoleChains
	 * @param dependencies  map from role names (or lists) to dependencies A map from role names (or role lists) to dependency sets.
	 */
	void setSubRolesAndChains(final Set<Role> subRoles, final Set<ATermList> subRoleChains, final Map<ATerm, DependencySet> dependencies);

	/**
	 * @param superRoles The _superRoles to set.
	 */
	void setSuperRoles(final Set<Role> superRoles);

	/**
	 * @return Returns the functionalSuper.
	 */
	Set<Role> getFunctionalSupers();

	/**
	 * @param r The functionalSuper to set.
	 */
	void addFunctionalSuper(final Role r);

	void setForceSimple(final boolean b);

	void setSimple(final boolean b);

	boolean isForceSimple();

	boolean isSimple();

	/**
	 * @return Returns transitive sub roles.
	 */
	Set<Role> getTransitiveSubRoles();

	/**
	 * @param r The transtive sub role to add.
	 */
	void addTransitiveSubRole(final Role r);

	void setFSM(final TransitionGraph<Role> tg);

	TransitionGraph<Role> getFSM();

	/* Dependency Retreival */

	DependencySet getExplainAsymmetric();

	DependencySet getExplainDomain(final ATermAppl a);

	DependencySet getExplainFunctional();

	DependencySet getExplainInverseFunctional();

	DependencySet getExplainIrreflexive();

	DependencySet getExplainRange(final ATermAppl a);

	DependencySet getExplainReflexive();

	DependencySet getExplainSub(final ATerm r);

	DependencySet getExplainSubOrInv(final Role r);

	DependencySet getExplainSuper(final ATerm r);

	DependencySet getExplainSymmetric();

	DependencySet getExplainTransitive();

	boolean isTop();

	boolean isBottom();

	boolean isBuiltin();
}
