package openllet.core.boxes.abox;

import java.util.List;
import java.util.Map;

import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.boxes.rbox.Role;
import openllet.core.tableau.branch.Branch;
import openllet.core.tableau.completion.queue.CompletionQueue;

public interface ABoxForBranch extends ABoxStatus
{
	int getBranchIndex();

	int getAnonCount();

	int size();

	boolean doExplanation();

	void setClash(Clash unexplained);

	Clash getClash();

	KnowledgeBase getKB();

	// End of methods use by the Abstract Branch

	void incrementBranch();

	Map<ATermAppl, int[]> getDisjBranchStats();

	CompletionQueue getCompletionQueue(); // ArrayList<Edge>

	Node getNode(ATerm aTerm);

	List<Branch> getBranches();

	// In atomAsserter
	Individual getIndividual(final ATerm x);

	/**
	 * Convenience function to get the named role.
	 */
	public Role getRole(final ATerm r);
}
