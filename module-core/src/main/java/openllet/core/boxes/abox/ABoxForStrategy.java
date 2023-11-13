package openllet.core.boxes.abox;

import java.util.Collection;
import java.util.List;

import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.tableau.branch.Branch;
import openllet.core.tableau.cache.CachedNode;
import openllet.core.tableau.cache.ConceptCache;
import openllet.core.tracker.BranchEffectTracker;
import openllet.core.tracker.IncrementalChangeTracker;
import openllet.core.utils.Bool;

public interface ABoxForStrategy extends ABoxForRule, ABoxForIndividual, ABoxForBranch
{
	boolean isInitialized();

	/**
	 * @return the branches.
	 */
	List<Branch> getBranches(boolean unmodifiable);

	@Override
	default List<Branch> getBranches()
	{
		return getBranches(true);
	}

	Collection<? extends NodeMerge> getToBeMerged();

	ABoxStats getStats();

	void setChanged(boolean b);

	void setComplete(boolean b);

	void setInitialized(boolean b);

	Individual addFreshIndividual(Individual parent, DependencySet ds);

	Literal getLiteral(ATerm target);

	/**
	 * @return the individuals to which we need to apply the tableau rules
	 */
	IndividualIterator getIndIterator();

	BranchEffectTracker getBranchEffectTracker(); // ArrayList<Edge>

	void setBranchIndex(int branchIndexInABox);

	void setRulesNotApplied(boolean b);

	IncrementalChangeTracker getIncrementalChangeTracker(); // ArrayList<Edge>

	boolean removeNode(ATermAppl a);

	void printTree();

	void validate();

	// Used only by EmptySRIQStrategy
	ConceptCache getCache();

	Bool getCachedSat(final ATermAppl c);

	CachedNode getCached(final ATermAppl c);
}
