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
	public final String IS_INC_CONSISTENT = "isIncConsistent";
	public final String IS_CONSISTENT = "isConsistent";

	/**
	 * @return a copy of this ABox with all the nodes and edges.
	 */
	public ABox copy();

	/**
	 * @param kb from witch the ABox is extracted
	 * @return a copy of this ABox with all the nodes and edges and the given KB.
	 */
	public ABox copy(final KnowledgeBase kb);

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
	public ABox copy(final ATermAppl extraIndividual, final boolean copyIndividuals);

	public void copyOnWrite();

	/**
	 * Clear the pseudo model created for the ABox and concept satisfiability.
	 *
	 * @param clearSatCache If true clear concept satisfiability _cache, if false only clear pseudo model.
	 */
	public void clearCaches(final boolean clearSatCache);

	public Bool getCachedSat(final ATermAppl c);

	public ConceptCache getCache();

	public CachedNode getCached(final ATermAppl c);

	public Bool isKnownSubClassOf(final ATermAppl c1, final ATermAppl c2);

	public boolean isSubClassOf(final ATermAppl c1, final ATermAppl c2);

	public boolean isSatisfiable(final ATermAppl c);

	public boolean isSatisfiable(ATermAppl c, final boolean cacheModel);

	public CandidateSet<ATermAppl> getObviousInstances(final ATermAppl c);

	public CandidateSet<ATermAppl> getObviousInstances(ATermAppl c, final Collection<ATermAppl> individuals);

	public void getObviousTypes(final ATermAppl x, final List<ATermAppl> types, final List<ATermAppl> nonTypes);

	public CandidateSet<ATermAppl> getObviousSubjects(final ATermAppl p, final ATermAppl o);

	public void getSubjects(final ATermAppl p, final ATermAppl o, final CandidateSet<ATermAppl> candidates);

	public void getObviousSubjects(final ATermAppl p, final ATermAppl o, final CandidateSet<ATermAppl> candidates);

	public void getObviousObjects(ATermAppl p, final CandidateSet<ATermAppl> candidates);

	public Bool isKnownType(final ATermAppl x, final ATermAppl c);

	public Bool isKnownType(final ATermAppl x, final ATermAppl c, final Collection<ATermAppl> subs);

	public Bool isKnownType(final Individual pNode, final ATermAppl concept, final Collection<ATermAppl> subs);

	public boolean isSameAs(final ATermAppl ind1, final ATermAppl ind2);

	/**
	 * @param x
	 * @param c
	 * @return true if individual x belongs to type c. This is a logical consequence of the KB if in all possible models x belongs to C. This is checked by
	 *         trying to construct a model where x belongs to not(c).
	 */
	public boolean isType(final ATermAppl x, ATermAppl c);

	/**
	 * @param c
	 * @param inds
	 * @return true if any of the individuals in the given list belongs to type c.
	 */
	public boolean existType(final List<ATermAppl> inds, ATermAppl c);

	public Bool hasObviousPropertyValue(final ATermAppl s, final ATermAppl p, final ATermAppl o);

	public Bool hasObviousDataPropertyValue(final ATermAppl s, final ATermAppl p, final Object value);

	public Bool hasObviousObjectPropertyValue(final ATermAppl s, final ATermAppl p, final ATermAppl o);

	public boolean hasPropertyValue(final ATermAppl s, final ATermAppl p, final ATermAppl o);

	public List<ATermAppl> getDataPropertyValues(final ATermAppl s, final Role role, final ATermAppl datatype);

	public List<ATermAppl> getDataPropertyValues(final ATermAppl s, final Role role, final ATermAppl datatype, final boolean onlyObvious);

	public List<ATermAppl> getObviousDataPropertyValues(final ATermAppl s, final Role prop, final ATermAppl datatype);

	public void getObjectPropertyValues(final ATermAppl s, final Role role, final Set<ATermAppl> knowns, final Set<ATermAppl> unknowns, final boolean getSames);

	public void getSimpleObjectPropertyValues(final Individual subj, final Role role, final Set<ATermAppl> knowns, final Set<ATermAppl> unknowns, final boolean getSames);

	public void getTransitivePropertyValues(final Individual subj, final Role prop, final Set<ATermAppl> knowns, final Set<ATermAppl> unknowns, final boolean getSames, final Map<Individual, Set<Role>> visited, final boolean isIndependent);

	public void getComplexObjectPropertyValues(final Individual subj, final State<Role> st, final TransitionGraph<Role> tg, final Set<ATermAppl> knowns, final Set<ATermAppl> unknowns, final boolean getSames, final HashMap<Individual, Set<State<Role>>> visited, final boolean isIndependent);

	public void getSames(final Individual ind, final Set<ATermAppl> knowns, final Set<ATermAppl> unknowns);

	/**
	 * @return true if this ABox is consistent. Consistent ABox means after applying all the tableau completion rules at least one _branch with no clashes was
	 *         found
	 */
	public boolean isConsistent();

	/**
	 * @return true if this ABox is consistent, using the incremental consistency checking approach
	 */
	boolean isIncConsistent();

	public EdgeList getInEdges(final ATerm x);

	public EdgeList getOutEdges(final ATerm x);

	public Individual getIndividual(final ATerm x);

	public Literal getLiteral(final ATerm x);

	public Node getNode(final ATerm x);

	public void addType(final ATermAppl x, final ATermAppl c);

	public void addType(final ATermAppl x, ATermAppl c, DependencySet ds);

	public Edge addEdge(final ATermAppl p, final ATermAppl s, final ATermAppl o, DependencySet ds);

	/**
	 * Remove the given node from the node map which maps names to nodes. Does not remove the node from the node list or other nodes' edge lists.
	 *
	 * @param x
	 * @return true if the removal occur
	 */
	public boolean removeNode(final ATermAppl x);

	public void removeType(final ATermAppl x, ATermAppl c);

	/**
	 * Add a new literal to the ABox. This function is used only when the literal value does not have a known value, e.g. applyMinRule would create such a
	 * literal.
	 *
	 * @param ds
	 * @return the literal added.
	 */
	public Literal addLiteral(final DependencySet ds);

	/**
	 * Add a new literal to the ABox. Literal will be assigned a fresh unique name.
	 *
	 * @param dataValue A literal ATerm which should be constructed with one of ATermUtils.makeXXXLiteral functions
	 * @return Literal object that has been created
	 */
	public Literal addLiteral(final ATermAppl dataValue);

	public Literal addLiteral(final ATermAppl dataValue, final DependencySet ds);

	public Individual addIndividual(final ATermAppl x, final DependencySet ds);

	public Individual addFreshIndividual(final Individual parent, final DependencySet ds);

	public void addSame(final ATermAppl x, final ATermAppl y);

	public void addDifferent(final ATermAppl x, final ATermAppl y);

	/**
	 * Say that all the term of the list are different from each-other.
	 *
	 * @param list of different element.
	 * @since ever
	 */
	public void addAllDifferent(final ATermList list);

	public boolean isNode(final ATerm x);

	public ATermAppl createUniqueName(final boolean isNominal);

	public Map<ATermAppl, Node> getNodes();

	public List<ATermAppl> getNodeNames();

	/**
	 * @return Returns the datatype reasoner.
	 */
	public DatatypeReasoner getDatatypeReasoner();

	/**
	 * @return Returns the isComplete.
	 */
	public boolean isComplete();

	/**
	 * @param isComplete The isComplete to set.
	 */
	public void setComplete(final boolean isComplete);

	/**
	 * @return true if Abox is closed.
	 */
	public boolean isClosed();

	public Clash getClash();

	public void setClash(final Clash clash);

	/**
	 * @return Returns the _kb.
	 */
	public KnowledgeBase getKB();

	/**
	 * Convenience function to get the named role.
	 *
	 * @param r is the name of the role
	 * @return the named role.
	 */
	public Role getRole(final ATerm r);

	/**
	 * @return the RBox
	 */
	public RBox getRBox();

	/**
	 * @return the TBox
	 */
	public TBox getTBox();

	public List<ATermAppl> getNodeList();

	/**
	 * Return the current branch number. Branches are created when a non-deterministic rule, e.g. disjunction or max rule, is being applied.
	 *
	 * @return Returns the branch.
	 */
	public int getBranchIndex();

	/**
	 * Set the _branch number (should only be called when backjumping is in progress)
	 *
	 * @param branchIndex
	 */
	public void setBranchIndex(final int branchIndex);

	public ABox getSourceABox();

	public void setSourceABox(final ABox sourceABox);

	public boolean isRulesNotApplied();

	public void setRulesNotApplied(final boolean rulesNotApplied);

	/**
	 * Increment the branch number (should only be called when a non-deterministic rule, e.g. disjunction or max rule, is being applied)
	 */
	public void incrementBranch();

	/**
	 * Check if the ABox is ready to be completed.
	 *
	 * @return Returns the initialized.
	 */
	public boolean isInitialized();

	public void setInitialized(final boolean initialized);

	/**
	 * Checks if the clashExplanation is turned on.
	 *
	 * @return Returns the _doExplanation.
	 */
	public boolean doExplanation();

	/**
	 * Enable/disable clashExplanation generation
	 *
	 * @param doExplanation The doExplanation to set.
	 */
	public void setDoExplanation(final boolean doExplanation);

	public void setExplanation(final DependencySet ds);

	public String getExplanation();

	public Set<ATermAppl> getExplanationSet();

	public BranchEffectTracker getBranchEffectTracker();

	/**
	 * @return the branches.
	 */
	public List<Branch> getBranches();

	public IncrementalChangeTracker getIncrementalChangeTracker();

	/**
	 * @return the individuals to which we need to apply the tableau rules
	 */
	public IndividualIterator getIndIterator();

	/**
	 * Validate all the edges in the ABox _nodes. Used to find bugs in the copy and detach/attach functions.
	 */
	public void validate();

	public void validateTypes(final Individual node, final List<ATermAppl> negatedTypes);

	public void validate(final Individual node);

	/**
	 * Print the ABox as a completion tree (child nodes are indented).
	 *
	 * @param stream is where to print
	 */
	public void printTree(PrintStream stream);

	default void printTree()
	{
		printTree(System.err);
	}

	public Clash getLastClash();

	public ABox getLastCompletion();

	public boolean isKeepLastCompletion();

	public void setKeepLastCompletion(final boolean keepLastCompletion);

	/**
	 * @return the number of nodes in the ABox. This number includes both the individuals and the literals.
	 */
	public int size();

	/**
	 * @return true if there are no individuals in the ABox.
	 */
	public boolean isEmpty();

	public void setLastCompletion(final ABox comp);

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

	public CompletionQueue getCompletionQueue();

	/**
	 * Reset the ABox to contain only asserted information. Any ABox assertion added by tableau rules will be removed.
	 */
	public void reset();

	public void resetQueue();

	/**
	 * @param anonCount the anonCount to set
	 * @return the count set.
	 */
	public int setAnonCount(final int anonCount);

	/**
	 * @return the anonCount
	 */
	public int getAnonCount();

	/**
	 * @return the disjBranchStats
	 */
	public Map<ATermAppl, int[]> getDisjBranchStats();

	/**
	 * @param changed the changed to set
	 */
	public void setChanged(final boolean changed);

	/**
	 * @return the changed
	 */
	public boolean isChanged();

	/**
	 * @return the toBeMerged
	 */
	public List<NodeMerge> getToBeMerged();

	public ABoxStats getStats();
}
