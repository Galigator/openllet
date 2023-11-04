package openllet.core.boxes.abox;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.core.DependencySet;
import openllet.core.KnowledgeBase;
import openllet.core.boxes.rbox.RBox;
import openllet.core.boxes.rbox.Role;
import openllet.core.boxes.tbox.TBox;
import openllet.core.datatypes.DatatypeReasoner;
import openllet.core.tableau.branch.Branch;
import openllet.core.tableau.cache.CachedNode;
import openllet.core.tableau.cache.ConceptCache;
import openllet.core.tableau.completion.queue.CompletionQueue;
import openllet.core.tracker.BranchEffectTracker;
import openllet.core.tracker.IncrementalChangeTracker;
import openllet.core.utils.Bool;
import openllet.core.utils.CandidateSet;
import openllet.core.utils.fsm.State;
import openllet.core.utils.fsm.TransitionGraph;
import openllet.shared.tools.Logging;

/**
 * Definition of an abox.
 *
 * @since 2.6.0
 */
public interface ABox extends Logging
{
	String IS_INC_CONSISTENT = "isIncConsistent";
	String IS_CONSISTENT = "isConsistent";

	/**
	 * @return a copy of this ABox with all the nodes and edges.
	 */
	ABox copy();

	/**
	 * @param kb from witch the ABox is extracted
	 * @return a copy of this ABox with all the nodes and edges and the given KB.
	 */
	ABox copy(final KnowledgeBase kb);

	/**
	 * Create a copy of this ABox with one more additional individual. This is <b>NOT</b> equivalent to create a copy and then add the individual. The order of
	 * individuals in the ABox is important to figure out which individuals exist in the original ontology and which ones are created by the tableau algorithm.
	 * This function creates a new ABox such that the individual is supposed to exist in the original ontology. This is very important when satisfiability of a
	 * concept starts with a pesudo model rather than the initial ABox.
	 *
	 * @param extraIndividual Extra _individual to be added to the copy ABox
	 * @param copyIndividuals are the new individual that are supposed to exist in the original ontology.
	 * @return a copy of this ABox
	 */
	ABox copy(final ATermAppl extraIndividual, final boolean copyIndividuals);

	void copyOnWrite();

	/**
	 * Clear the pseudo model created for the ABox and concept satisfiability.
	 *
	 * @param clearSatCache If true clear concept satisfiability _cache, if false only clear pseudo model.
	 */
	void clearCaches(final boolean clearSatCache);

	Bool getCachedSat(final ATermAppl c);

	ConceptCache getCache();

	CachedNode getCached(final ATermAppl c);

	Bool isKnownSubClassOf(final ATermAppl c1, final ATermAppl c2);

	boolean isSubClassOf(final ATermAppl c1, final ATermAppl c2);

	boolean isSatisfiable(final ATermAppl c);

	boolean isSatisfiable(ATermAppl c, final boolean cacheModel);

	CandidateSet<ATermAppl> getObviousInstances(final ATermAppl c);

	CandidateSet<ATermAppl> getObviousInstances(ATermAppl c, final Collection<ATermAppl> individuals);

	void getObviousTypes(final ATermAppl x, final List<ATermAppl> types, final List<ATermAppl> nonTypes);

	CandidateSet<ATermAppl> getObviousSubjects(final ATermAppl p, final ATermAppl o);

	void getSubjects(final ATermAppl p, final ATermAppl o, final CandidateSet<ATermAppl> candidates);

	void getObviousSubjects(final ATermAppl p, final ATermAppl o, final CandidateSet<ATermAppl> candidates);

	void getObviousObjects(ATermAppl p, final CandidateSet<ATermAppl> candidates);

	Bool isKnownType(final ATermAppl x, final ATermAppl c);

	Bool isKnownType(final ATermAppl x, final ATermAppl c, final Collection<ATermAppl> subs);

	Bool isKnownType(final Individual pNode, final ATermAppl concept, final Collection<ATermAppl> subs);

	boolean isSameAs(final ATermAppl ind1, final ATermAppl ind2);

	/**
	 * @param x
	 * @param c
	 * @return true if individual x belongs to type c. This is a logical consequence of the KB if in all possible models x belongs to C. This is checked by
	 *         trying to construct a model where x belongs to not(c).
	 */
	boolean isType(final ATermAppl x, ATermAppl c);

	/**
	 * @param c
	 * @param inds
	 * @return true if any of the individuals in the given list belongs to type c.
	 */
	boolean existType(final List<ATermAppl> inds, ATermAppl c);

	Bool hasObviousPropertyValue(final ATermAppl s, final ATermAppl p, final ATermAppl o);

	Bool hasObviousDataPropertyValue(final ATermAppl s, final ATermAppl p, final Object value);

	Bool hasObviousObjectPropertyValue(final ATermAppl s, final ATermAppl p, final ATermAppl o);

	boolean hasPropertyValue(final ATermAppl s, final ATermAppl p, final ATermAppl o);

	List<ATermAppl> getDataPropertyValues(final ATermAppl s, final Role role, final ATermAppl datatype);

	List<ATermAppl> getDataPropertyValues(final ATermAppl s, final Role role, final ATermAppl datatype, final boolean onlyObvious);

	List<ATermAppl> getObviousDataPropertyValues(final ATermAppl s, final Role prop, final ATermAppl datatype);

	void getObjectPropertyValues(final ATermAppl s, final Role role, final Set<ATermAppl> knowns, final Set<ATermAppl> unknowns, final boolean getSames);

	void getSimpleObjectPropertyValues(final Individual subj, final Role role, final Set<ATermAppl> knowns, final Set<ATermAppl> unknowns, final boolean getSames);

	void getTransitivePropertyValues(final Individual subj, final Role prop, final Set<ATermAppl> knowns, final Set<ATermAppl> unknowns, final boolean getSames, final Map<Individual, Set<Role>> visited, final boolean isIndependent);

	void getComplexObjectPropertyValues(final Individual subj, final State<Role> st, final TransitionGraph<Role> tg, final Set<ATermAppl> knowns, final Set<ATermAppl> unknowns, final boolean getSames, final HashMap<Individual, Set<State<Role>>> visited, final boolean isIndependent);

	void getSames(final Individual ind, final Set<ATermAppl> knowns, final Set<ATermAppl> unknowns);

	/**
	 * @return true if this ABox is consistent. Consistent ABox means after applying all the tableau completion rules at least one _branch with no clashes was
	 *         found
	 */
	boolean isConsistent();

	/**
	 * @return true if this ABox is consistent, using the incremental consistency checking approach
	 */
	boolean isIncConsistent();

	EdgeList getInEdges(final ATerm x);

	EdgeList getOutEdges(final ATerm x);

	Individual getIndividual(final ATerm x);

	Literal getLiteral(final ATerm x);

	Node getNode(final ATerm x);

	void addType(final ATermAppl x, final ATermAppl c);

	void addType(final ATermAppl x, ATermAppl c, DependencySet ds);

	Edge addEdge(final ATermAppl p, final ATermAppl s, final ATermAppl o, DependencySet ds);

	/**
	 * Remove the given node from the node map which maps names to nodes. Does not remove the node from the node list or other nodes' edge lists.
	 *
	 * @param x
	 * @return true if the removal occur
	 */
	boolean removeNode(final ATermAppl x);

	void removeType(final ATermAppl x, ATermAppl c);

	/**
	 * Add a new literal to the ABox. This function is used only when the literal value does not have a known value, e.g. applyMinRule would create such a
	 * literal.
	 *
	 * @param ds
	 * @return the literal added.
	 */
	Literal addLiteral(final DependencySet ds);

	/**
	 * Add a new literal to the ABox. Literal will be assigned a fresh unique name.
	 *
	 * @param dataValue A literal ATerm which should be constructed with one of ATermUtils.makeXXXLiteral functions
	 * @return Literal object that has been created
	 */
	Literal addLiteral(final ATermAppl dataValue);

	Literal addLiteral(final ATermAppl dataValue, final DependencySet ds);

	Individual addIndividual(final ATermAppl x, final DependencySet ds);

	Individual addFreshIndividual(final Individual parent, final DependencySet ds);

	void addSame(final ATermAppl x, final ATermAppl y);

	void addDifferent(final ATermAppl x, final ATermAppl y);

	/**
	 * Say that all the term of the list are different from each-other.
	 *
	 * @param list of different element.
	 * @since ever
	 */
	void addAllDifferent(final ATermList list);

	boolean isNode(final ATerm x);

	ATermAppl createUniqueName(final boolean isNominal);

	Map<ATermAppl, Node> getNodes();

	List<ATermAppl> getNodeNames();

	/**
	 * @return Returns the datatype reasoner.
	 */
	DatatypeReasoner getDatatypeReasoner();

	/**
	 * @return Returns the isComplete.
	 */
	boolean isComplete();

	/**
	 * @param isComplete The isComplete to set.
	 */
	void setComplete(final boolean isComplete);

	/**
	 * @return true if Abox is closed.
	 */
	boolean isClosed();

	Clash getClash();

	void setClash(final Clash clash);

	/**
	 * @return Returns the _kb.
	 */
	KnowledgeBase getKB();

	/**
	 * Convenience function to get the named role.
	 *
	 * @param r is the name of the role
	 * @return the named role.
	 */
	Role getRole(final ATerm r);

	/**
	 * @return the RBox
	 */
	RBox getRBox();

	/**
	 * @return the TBox
	 */
	TBox getTBox();

	List<ATermAppl> getNodeList();

	/**
	 * Return the current branch number. Branches are created when a non-deterministic rule, e.g. disjunction or max rule, is being applied.
	 *
	 * @return Returns the branch.
	 */
	int getBranchIndex();

	/**
	 * Set the _branch number (should only be called when backjumping is in progress)
	 *
	 * @param branchIndex
	 */
	void setBranchIndex(final int branchIndex);

	ABox getSourceABox();

	void setSourceABox(final ABox sourceABox);

	boolean isRulesNotApplied();

	void setRulesNotApplied(final boolean rulesNotApplied);

	/**
	 * Increment the branch number (should only be called when a non-deterministic rule, e.g. disjunction or max rule, is being applied)
	 */
	void incrementBranch();

	/**
	 * Check if the ABox is ready to be completed.
	 *
	 * @return Returns the initialized.
	 */
	boolean isInitialized();

	void setInitialized(final boolean initialized);

	/**
	 * Checks if the clashExplanation is turned on.
	 *
	 * @return Returns the _doExplanation.
	 */
	boolean doExplanation();

	/**
	 * Enable/disable clashExplanation generation
	 *
	 * @param doExplanation The doExplanation to set.
	 */
	void setDoExplanation(final boolean doExplanation);

	void setExplanation(final DependencySet ds);

	String getExplanation();

	Set<ATermAppl> getExplanationSet();

	BranchEffectTracker getBranchEffectTracker();

	/**
	 * @return the branches.
	 */
	List<Branch> getBranches(boolean unmodifiable);

	default List<Branch> getBranches()
	{
		return getBranches(true);
	}

	IncrementalChangeTracker getIncrementalChangeTracker();

	/**
	 * @return the individuals to which we need to apply the tableau rules
	 */
	IndividualIterator getIndIterator();

	/**
	 * Validate all the edges in the ABox _nodes. Used to find bugs in the copy and detach/attach functions.
	 */
	void validate();

	void validateTypes(final Individual node, final List<ATermAppl> negatedTypes);

	void validate(final Individual node);

	/**
	 * Print the ABox as a completion tree (child nodes are indented).
	 *
	 * @param stream is where to print
	 */
	void printTree(PrintStream stream);

	default void printTree()
	{
		printTree(System.err);
	}

	Clash getLastClash();

	ABox getLastCompletion();

	boolean isKeepLastCompletion();

	void setKeepLastCompletion(final boolean keepLastCompletion);

	/**
	 * @return the number of nodes in the ABox. This number includes both the individuals and the literals.
	 */
	int size();

	/**
	 * @return true if there are no individuals in the ABox.
	 */
	boolean isEmpty();

	void setLastCompletion(final ABox comp);

	/**
	 * Set whether changes to the update should be treated as syntactic updates, i.e., if the changes are made on explicit source axioms. This is used for the
	 * completion queue for incremental consistency checking purposes.
	 *
	 * @param val The value
	 */
	void setSyntacticUpdate(final boolean val);

	/**
	 * Set whether changes to the update should be treated as syntactic updates, i.e., if the changes are made on explicit source axioms. This is used for the
	 * completion _queue for incremental consistency checking purposes.
	 *
	 * @return the value
	 */
	boolean isSyntacticUpdate();

	CompletionQueue getCompletionQueue();

	/**
	 * Reset the ABox to contain only asserted information. Any ABox assertion added by tableau rules will be removed.
	 */
	void reset();

	void resetQueue();

	/**
	 * @param anonCount the anonCount to set
	 * @return the count set.
	 */
	int setAnonCount(final int anonCount);

	/**
	 * @return the anonCount
	 */
	int getAnonCount();

	/**
	 * @return the disjBranchStats
	 */
	Map<ATermAppl, int[]> getDisjBranchStats();

	/**
	 * @param changed the changed to set
	 */
	void setChanged(final boolean changed);

	/**
	 * @return the changed
	 */
	boolean isChanged();

	/**
	 * @return the toBeMerged
	 */
	List<NodeMerge> getToBeMerged();

	ABoxStats getStats();
}
