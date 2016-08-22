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
	public String debugString();

	/**
	 * Add a sub role chain without dependency tracking information
	 * 
	 * @param chain
	 */
	public void addSubRoleChain(final ATermList chain);

	/**
	 * Add a sub role chain with dependency tracking.
	 *
	 * @param chain List of role names of at least length 2.
	 * @param ds
	 */
	public void addSubRoleChain(final ATermList chain, final DependencySet ds);

	public void removeSubRoleChain(final ATermList chain);

	public void removeSubRoleChains();

	/**
	 * r is subrole of this role
	 *
	 * @param r
	 */
	public void addSubRole(final Role r);

	/**
	 * Add sub role with depedency set.
	 *
	 * @param r subrole of this role
	 * @param ds
	 */
	public void addSubRole(final Role r, final DependencySet ds);

	public boolean removeDomain(final ATermAppl a, final DependencySet ds);

	public boolean removeRange(final ATermAppl a, final DependencySet ds);

	void resetDomainRange();

	public void removeSubRole(final Role r);

	/**
	 * r is superrole of this role
	 *
	 * @param r
	 */
	public void addSuperRole(final Role r);

	public void addSuperRole(final Role r, final DependencySet ds);

	public void addDisjointRole(final Role r, final DependencySet ds);

	public boolean addDomain(final ATermAppl a, final DependencySet ds);

	public boolean addRange(final ATermAppl a, final DependencySet ds);

	public boolean isObjectRole();

	public boolean isDatatypeRole();

	/**
	 * check if a role is declared as datatype property
	 */
	public boolean isAnnotationRole();

	public boolean isUntypedRole();

	public Role getInverse();

	public boolean hasNamedInverse();

	public boolean hasComplexSubRole();

	public boolean isFunctional();

	public boolean isInverseFunctional();

	public boolean isSymmetric();

	public boolean isAsymmetric();

	public boolean isTransitive();

	public boolean isReflexive();

	public boolean isIrreflexive();

	public boolean isAnon();

	public ATermAppl getName();

	public Set<ATermAppl> getDomains();

	public Set<ATermAppl> getRanges();

	public Set<Role> getSubRoles();

	public Set<Role> getEquivalentProperties();

	public boolean isEquivalent(final Role r);

	public Set<Role> getProperSubRoles();

	public Set<ATermList> getSubRoleChains();

	public Set<Role> getSuperRoles();

	public Set<Role> getDisjointRoles();

	public DependencySet getExplainDisjointRole(final Role role);

	public PropertyType getType();

	public String getTypeName();

	public boolean isSubRoleOf(final Role r);

	public boolean isSuperRoleOf(final Role r);

	public void setInverse(final Role term);

	public void setFunctional(final boolean b);

	public void setFunctional(final boolean b, final DependencySet ds);

	public void setInverseFunctional(final boolean b);

	public void setInverseFunctional(final boolean b, final DependencySet ds);

	public void setTransitive(final boolean b);

	public void setTransitive(final boolean b, final DependencySet ds);

	public void setReflexive(final boolean b);

	public void setReflexive(final boolean b, final DependencySet ds);

	public void setIrreflexive(final boolean b);

	public void setIrreflexive(final boolean b, final DependencySet ds);

	public void setAsymmetric(final boolean b);

	public void setAsymmetric(final boolean b, final DependencySet ds);

	public void setHasComplexSubRole(final boolean b);

	public void setType(final PropertyType type);

	/**
	 * @param _subRoleChains
	 * @param dependencies map from role names (or lists) to depedencies
	 */
	public void setSubRolesAndChains(final Set<Role> subRoles, final Set<ATermList> subRoleChains, final Map<ATerm, DependencySet> dependencies);

	/**
	 * @param _superRoles The _superRoles to set.
	 * @param dependencies A map from role names (or role lists) to dependency sets.
	 */
	public void setSuperRoles(final Set<Role> superRoles);

	/**
	 * @return Returns the functionalSuper.
	 */
	public Set<Role> getFunctionalSupers();

	/**
	 * @param functionalSuper The functionalSuper to set.
	 */
	public void addFunctionalSuper(final Role r);

	public void setForceSimple(final boolean b);

	public void setSimple(final boolean b);

	public boolean isForceSimple();

	public boolean isSimple();

	/**
	 * @return Returns transitive sub roles.
	 */
	public Set<Role> getTransitiveSubRoles();

	/**
	 * @param r The transtive sub role to add.
	 */
	public void addTransitiveSubRole(final Role r);

	public void setFSM(final TransitionGraph<Role> tg);

	public TransitionGraph<Role> getFSM();

	/* Dependency Retreival */

	public DependencySet getExplainAsymmetric();

	public DependencySet getExplainDomain(final ATermAppl a);

	public DependencySet getExplainFunctional();

	public DependencySet getExplainInverseFunctional();

	public DependencySet getExplainIrreflexive();

	public DependencySet getExplainRange(final ATermAppl a);

	public DependencySet getExplainReflexive();

	public DependencySet getExplainSub(final ATerm r);

	public DependencySet getExplainSubOrInv(final Role r);

	public DependencySet getExplainSuper(final ATerm r);

	public DependencySet getExplainSymmetric();

	public DependencySet getExplainTransitive();

	public boolean isTop();

	public boolean isBottom();

	public boolean isBuiltin();
}
