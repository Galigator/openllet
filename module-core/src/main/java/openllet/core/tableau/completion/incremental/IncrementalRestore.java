// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.completion.incremental;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.KnowledgeBase;
import openllet.core.OpenlletOptions;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.Edge;
import openllet.core.boxes.abox.EdgeList;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.Node;
import openllet.core.boxes.rbox.Role;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.tableau.branch.Branch;
import openllet.core.tracker.IncrementalChangeTracker;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.intset.IntSet;

/**
 * The incremental restoration is call when predicate have been remove from the ABox and the consistency is re-check.
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public class IncrementalRestore
{
	public static void restoreDependencies(final KnowledgeBase kb)
	{
		new IncrementalRestore(kb).restoreDependencies();
	}

	private final KnowledgeBase _kb;

	private IncrementalRestore(final KnowledgeBase kb)
	{
		_kb = kb;
	}

	private static void phase1(final AddBranchDependency branch, final ABox abox)
	{
		final Collection<ATermAppl> allEffects = //
				OpenlletOptions.TRACK_BRANCH_EFFECTS ? //
						abox.getBranchEffectTracker().getAll(branch.getBranch().getBranchIndexInABox()) : //
						abox.getNodeNames();
		final List<IntSet> updatedList = new ArrayList<>();

		for (final ATermAppl a : allEffects)
		{
			final Node node = abox.getNode(a); // get the actual _node

			for (final Entry<ATermAppl, DependencySet> entry : node.getDepends().entrySet()) // update type dependencies
			{
				DependencySet tDS = entry.getValue(); // get ds for type

				// DependencySet.copy() does not create a new bitset object,
				// so we need to track which bitsets have been
				// updated, so we do not process the same bitset multiple
				// times
				boolean exit = false;
				for (int i = 0; i < updatedList.size(); i++)
					if (updatedList.get(i) == tDS.getDepends())
						exit = true;

				if (exit)
					continue;

				updatedList.add(tDS.getDepends());

				if (tDS.getBranch() > branch.getBranch().getBranchIndexInABox()) // update _branch if necessary
					tDS = tDS.copy(tDS.getBranch() - 1);

				for (int i = branch.getBranch().getBranchIndexInABox(); i <= abox.getBranches().size(); i++)
					if (tDS.contains(i)) // update dependency set
					{
						tDS.remove(i);
						tDS.add(i - 1);
					}

				entry.setValue(tDS);
			}

			for (final Edge edge : node.getInEdges()) // update edge dependencies
			{
				DependencySet tDS = edge.getDepends();

				// DependencySet.copy() does not create a new bitset object,
				// so we need to track which bitsets have been updated,
				// so we do not process the same bitset multiple times
				boolean exit = false;
				for (int i = 0; i < updatedList.size(); i++)
					if (updatedList.get(i) == tDS.getDepends())
						exit = true;

				if (exit)
					continue;

				updatedList.add(tDS.getDepends());

				if (tDS.getBranch() > branch.getBranch().getBranchIndexInABox()) // update _branch if necessary
					tDS = tDS.copy(edge.getDepends().getBranch() - 1);

				for (int i = branch.getBranch().getBranchIndexInABox(); i <= abox.getBranches().size(); i++)
					if (tDS.contains(i)) // update dependency set
					{
						tDS.remove(i);
						tDS.add(i - 1);
					}

				edge.setDepends(tDS);
			}
		}
	}

	private void updateBranchesOfABox(final AddBranchDependency branch, final ABox abox)
	{
		final List<Branch> branches = abox.getBranches();

		// decrease branch id for each branch after the branch we're removing
		// also need to change the dependency set for each label
		for (int i = branch.getBranch().getBranchIndexInABox(); i < branches.size(); i++)
		{
			final Branch br = branches.get(i); // cast for ease

			DependencySet termDepends = br.getTermDepends();

			// update the term depends in the branch
			if (termDepends.getBranch() > branch.getBranch().getBranchIndexInABox())
				termDepends = termDepends.copy(termDepends.getBranch() - 1);

			for (int j = branch.getBranch().getBranchIndexInABox(); j < _kb.getABox().getBranches().size(); j++)
				if (termDepends.contains(j))
				{
					termDepends.remove(j);
					termDepends.add(j - 1);
					break;
				}
			br.setTermDepends(termDepends);
		}

		branches.remove(branch.getBranch()); // remove the actual branch
	}

	/**
	 * Restore a branch add dependency
	 *
	 * @param assertion
	 * @param branch
	 */
	private void restoreBranchAdd(final ATermAppl assertion, final AddBranchDependency branch)
	{
		DependencyIndex._logger.fine(() -> "    Removing _branch add? " + branch.getBranch());

		final DependencySet ds = branch.getBranch().getTermDepends(); // get merge dependency

		ds.removeExplain(assertion); // remove the dependency

		if (ds.getExplain().isEmpty()) // undo merge if empty
		{
			DependencyIndex._logger.fine("           Actually removing _branch!");
			final ABox abox = _kb.getABox();

			phase1(branch, abox); // TODO rename this function when you find the its semantic.

			if (OpenlletOptions.TRACK_BRANCH_EFFECTS)
				abox.getBranchEffectTracker().remove(branch.getBranch().getBranchIndexInABox() + 1);

			updateBranchesOfABox(branch, abox); // Next update abox branches

			abox.setBranchIndex(abox.getBranchIndex() - 1); // set the branch counter
		}
	}

	/**
	 * Restore a clash dependency
	 *
	 * @param assertion
	 * @param clash
	 */
	private void restoreClash(final ATermAppl assertion, final ClashDependency clash)
	{
		DependencyIndex._logger.fine(() -> "    Restoring clash dependency clash: " + clash.getClash());

		clash.getClash().getDepends().removeExplain(assertion); // remove the dependency

		if (clash.getClash().getDepends().getExplain().isEmpty() && clash.getClash().getDepends().isIndependent()) // undo clash if empty and is independent
		{
			DependencyIndex._logger.fine(() -> "           Actually removing clash!");
			_kb.getABox().setClash(null);
		}
	}

	/**
	 * Restore a disjunct, merge pairs, etc. of a _branch that has been closed due to a clash whose dependency set contains an assertion that has been deleted
	 *
	 * @param assertion
	 * @param branch
	 */
	private static void restoreCloseBranch(final ATermAppl assertion, final CloseBranchDependency branch)
	{
		if (branch.getCloseBranch().getTryNext() > -1) // only proceed if _tryNext is larger than 1!
		{
			DependencyIndex._logger.fine(() -> "    Undoing _branch remove - _branch " + branch.getBranch() + "  -  " + branch.getInd() + "   _tryNext: " + branch.getTryNext());
			branch.getCloseBranch().shiftTryNext(branch.getTryNext()); // shift try next for _branch
		}
	}

	/**
	 * Method to remove all stuctures dependent on an _kb.getABox() assertion from the _kb.getABox(). This is used for incremental reasoning under _kb.getABox()
	 * deletions.
	 *
	 * @param ATermAppl assertion The deleted assertion
	 */
	private void restoreDependencies()
	{
		for (final ATermAppl next : _kb.getDeletedAssertions()) // iterate over all removed assertions
		{
			final DependencyEntry entry = _kb.getDependencyIndex().getDependencies(next); // get the dependency entry (from map, so it can be null)

			if (entry != null)
			{
				DependencyIndex._logger.fine(() -> "Restoring dependencies for " + next);
				restoreDependency(next, entry); // restore the entry
			}

			_kb.getDependencyIndex().removeDependencies(next); // remove the entry in the _index for this assertion
		}

	}

	/**
	 * Perform the actual rollback of a dependency entry
	 *
	 * @param assertion
	 * @param entry
	 */
	private void restoreDependency(final ATermAppl assertion, final DependencyEntry entry)
	{
		DependencyIndex._logger.fine(() -> "  Restoring Edge Dependencies:");
		for (final Edge next : entry.getEdges())
			restoreEdge(assertion, next);

		DependencyIndex._logger.fine(() -> "  Restoring Type Dependencies:");
		for (final TypeDependency next : entry.getTypes())
			restoreType(assertion, next);

		DependencyIndex._logger.fine(() -> "  Restoring Merge Dependencies: " + entry.getMerges());
		for (final MergeDependency next : entry.getMerges())
			restoreMerge(assertion, next);

		DependencyIndex._logger.fine(() -> "  Restoring Branch Add Dependencies: " + entry.getBranchAdds());
		for (final AddBranchDependency next : entry.getBranchAdds())
			restoreBranchAdd(assertion, next);

		DependencyIndex._logger.fine(() -> "  Restoring Branch Remove DS Dependencies: " + entry.getBranchAdds());
		for (final CloseBranchDependency next : entry.getCloseBranches())
			restoreCloseBranch(assertion, next);

		DependencyIndex._logger.fine(() -> "  Restoring clash dependency: " + entry.getClash());
		entry.getClash().ifPresent(clash -> restoreClash(assertion, clash));
	}

	/**
	 * Restore an edge - i.e., remove it
	 *
	 * @param assertion
	 * @param edge
	 */
	private void restoreEdge(final ATermAppl assertion, final Edge theEdge)
	{
		if (DependencyIndex._logger.isLoggable(Level.FINE))
			DependencyIndex._logger.fine("    Removing edge? " + theEdge);

		// the edge could have previously been removed so return
		if (theEdge == null)
			return;

		// get the object
		final Individual subj = _kb.getABox().getIndividual(theEdge.getFrom().getName());
		final Node obj = _kb.getABox().getNode(theEdge.getTo().getName());
		final Role role = _kb.getRole(theEdge.getRole().getName());

		// loop over all edges for the subject
		final EdgeList edges = subj.getEdgesTo(obj, role);
		for (int i = 0; i < edges.size(); i++)
		{
			final Edge edge = edges.edgeAt(i);
			if (edge.getRole().equals(role))
			{
				// get dependency set for the edge
				final DependencySet ds = edge.getDepends();

				// clean it
				ds.removeExplain(assertion);

				// remove if the dependency set is empty
				if (ds.getExplain().isEmpty())
				{
					final IncrementalChangeTracker tracker = _kb.getABox().getIncrementalChangeTracker();
					// need to check if the

					subj.removeEdge(edge);
					obj.removeInEdge(edge);

					// update the removed set of edges
					tracker.addDeletedEdge(edge);

					// add to updated individuals
					tracker.addUpdatedIndividual(subj);

					// TODO: Do we need to add literals?
					if (obj instanceof Individual)
						tracker.addUpdatedIndividual((Individual) obj);

					if (DependencyIndex._logger.isLoggable(Level.FINE))
						DependencyIndex._logger.fine("           Actually removed edge!");
				}
				break;
			}
		}
	}

	/**
	 * Restore a merge dependency
	 *
	 * @param assertion
	 * @param merge
	 */
	private void restoreMerge(final ATermAppl assertion, final MergeDependency merge)
	{
		DependencyIndex._logger.fine(() -> "    Removing merge? " + merge.getInd() + " merged to " + merge.getmergedIntoInd());

		final DependencySet ds = _kb.getABox().getNode(merge.getInd()).getMergeDependency(false); // get merge dependency

		ds.removeExplain(assertion); // remove the dependency

		if (ds.getExplain().isEmpty()) // undo merge if empty
		{
			DependencyIndex._logger.fine(() -> "           Actually removing merge!");

			final Node ind = _kb.getABox().getNode(merge.getInd()); // get _nodes
			final Node mergedToInd = _kb.getABox().getNode(merge.getmergedIntoInd());

			if (!ind.isSame(mergedToInd)) // check that they are actually the same - else throw error
				throw new InternalReasonerException(" Restore merge error: " + ind + " not same as " + mergedToInd);

			if (!ind.isPruned())
				throw new InternalReasonerException(" Restore merge error: " + ind + " not pruned");

			ind.unprune(ind.getPruned().getBranch()); // unprune to prune _branch

			ind.undoSetSame(); // undo set same

			// add to updated
			// Note that ind.unprune may add edges, however we do not need to
			// add them to the updated individuals as they will be added when the edge is removed from the _node which
			// this _individual was merged to add to updated
			final IncrementalChangeTracker tracker = _kb.getABox().getIncrementalChangeTracker();

			// because this _node was pruned, we must guarantee that all of its labels have been fired
			tracker.addUnprunedNode(ind);

			if (ind instanceof Individual)
				tracker.addUpdatedIndividual((Individual) ind);

			if (mergedToInd instanceof Individual)
				tracker.addUpdatedIndividual((Individual) mergedToInd);
		}
	}

	/**
	 * Restore a type dependency
	 *
	 * @param assertion
	 * @param type
	 */
	private void restoreType(final ATermAppl assertion, final TypeDependency type)
	{

		final Node node = _kb.getABox().getNode(type.getInd());
		final ATermAppl desc = type.getType();

		if (DependencyIndex._logger.isLoggable(Level.FINE))
			if (node instanceof Individual)
				DependencyIndex._logger.fine("    Removing type? " + desc + " from " + ((Individual) node).debugString());
			else
				DependencyIndex._logger.fine("    Removing type? " + desc + " from " + node);

		// get the dependency set - Note: we must normalize the concept
		final DependencySet ds = node.getDepends(ATermUtils.normalize(desc));

		// return if null - this can happen as currently I have dupilicates in
		// the _index
		if (ds == null || desc == ATermUtils.TOP)
			return;

		// clean it
		ds.removeExplain(assertion);

		// remove if the explanation set is empty
		if (ds.getExplain().isEmpty())
		{
			final IncrementalChangeTracker tracker = _kb.getABox().getIncrementalChangeTracker();

			_kb.getABox().removeType(node.getName(), desc);

			// update the set of removed types
			tracker.addDeletedType(node, type.getType());

			// add to updated individuals
			if (node instanceof Individual)
			{
				final Individual ind = (Individual) node;
				tracker.addUpdatedIndividual(ind);

				// also need to add all edge object to updated individuals -
				// this is needed to fire allValues/domain/range rules etc.
				for (final Edge e : ind.getInEdges())
					tracker.addUpdatedIndividual(e.getFrom());
				for (final Edge e : ind.getOutEdges())
					if (e.getTo() instanceof Individual)
						tracker.addUpdatedIndividual((Individual) e.getTo());
			}

			if (DependencyIndex._logger.isLoggable(Level.FINE))
				DependencyIndex._logger.fine("           Actually removed type!");
		}
	}

}
