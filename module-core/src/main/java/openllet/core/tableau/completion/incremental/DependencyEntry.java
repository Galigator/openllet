// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.completion.incremental;

import java.util.HashSet;
import java.util.Set;
import openllet.aterm.ATermAppl;
import openllet.core.boxes.abox.DefaultEdge;
import openllet.core.boxes.abox.Edge;
import openllet.core.tableau.branch.Branch;

/**
 * Structure for containing all dependencies for a given assertion. This is the object stored in the dependency _index
 *
 * @author Christian Halaschek-Wiener
 */
public class DependencyEntry
{

	/**
	 * The set of _node lables which are dependent
	 */
	private Set<TypeDependency> types;

	/**
	 * The set of merges which are dependent
	 */
	private Set<MergeDependency> merges;

	/**
	 * The set of edge which are dependent
	 */
	private final Set<Edge> edges;

	/**
	 * The set of branches which are dependent
	 */
	private Set<BranchAddDependency> branchAdds;

	/**
	 * The set of _branch remove ds' which are dependent
	 */
	private Set<CloseBranchDependency> branchCloses;

	/**
	 * Clash dependency
	 */
	private ClashDependency _clash;

	/**
	 * Default constructor
	 */
	public DependencyEntry()
	{
		types = new HashSet<>();
		edges = new HashSet<>();
		merges = new HashSet<>();
		branchAdds = new HashSet<>();
		branchCloses = new HashSet<>();
		_clash = null;
	}

	public DependencyEntry copy()
	{
		final DependencyEntry newEntry = new DependencyEntry();

		//TODO:may need to perform a deep copy here
		newEntry.types = new HashSet<>(types);

		//TODO:may need to perform a deep copy here
		newEntry.merges = new HashSet<>(merges);

		//copy edge depenedencies
		for (final Edge next : edges)
		{

			//create new edge
			final Edge newEdge = new DefaultEdge(next.getRole(), next.getFrom(), next.getTo(), next.getDepends());

			//add to edge list
			newEntry.edges.add(newEdge);
		}

		//TODO:may need to perform a deep copy here
		newEntry.branchAdds = new HashSet<>(branchAdds);

		//TODO:may need to perform a deep copy here
		newEntry.branchCloses = new HashSet<>(branchCloses);

		//TODO:may need to perform a deep copy here
		newEntry._clash = _clash;

		return newEntry;
	}

	/**
	 * Add a type dependency
	 *
	 * @param ind
	 * @param type
	 */
	protected void addTypeDependency(final ATermAppl ind, final ATermAppl type)
	{
		types.add(new TypeDependency(ind, type));
	}

	/**
	 * Add a edge dependency
	 *
	 * @param edge
	 */
	protected void addEdgeDependency(final Edge edge)
	{
		edges.add(edge);
	}

	/**
	 * Add a edge dependency
	 *
	 * @param ind
	 * @param mergedTo
	 */
	protected void addMergeDependency(final ATermAppl ind, final ATermAppl mergedTo)
	{
		merges.add(new MergeDependency(ind, mergedTo));
	}

	/**
	 * Add a _branch add dependency
	 *
	 * @param branchId
	 * @param _branch
	 */
	protected BranchDependency addBranchAddDependency(final ATermAppl assertion, final int branchId, final Branch branch)
	{
		final BranchAddDependency b = new BranchAddDependency(assertion, branchId, branch);

		branchAdds.add(b);
		return b;
	}

	/**
	 * Add a _branch remove ds dependency
	 *
	 * @param branchId
	 * @param _branch
	 */
	protected BranchDependency addCloseBranchDependency(final ATermAppl assertion, final Branch theBranch)
	{
		final CloseBranchDependency b = new CloseBranchDependency(assertion, theBranch.getTryNext(), theBranch);

		if (branchCloses.contains(b))
			branchCloses.remove(b);

		branchCloses.add(b);
		return b;
	}

	/**
	 * Helper method to print all dependencies TODO: this print is not complete
	 */
	public void print()
	{
		System.out.println("  Edge Dependencies:");
		for (final Edge e : edges)
			System.out.println("    " + e.toString());

		System.out.println("  Type Dependencies:");
		for (final TypeDependency t : types)
			System.out.println("    " + t.toString());

		System.out.println("  Merge Dependencies:");
		for (final MergeDependency m : merges)
			System.out.println("    " + m.toString());
	}

	/**
	 * @return the edges
	 */
	public Set<Edge> getEdges()
	{
		return edges;
	}

	/**
	 * @return the merges
	 */
	public Set<MergeDependency> getMerges()
	{
		return merges;
	}

	/**
	 * @return the types
	 */
	public Set<TypeDependency> getTypes()
	{
		return types;
	}

	/**
	 * @return get branches
	 */
	public Set<BranchAddDependency> getBranchAdds()
	{
		return branchAdds;
	}

	/**
	 * @return the close branches for this entry
	 */
	public Set<CloseBranchDependency> getCloseBranches()
	{
		return branchCloses;
	}

	/**
	 * @return the clash dependency
	 */
	public ClashDependency getClash()
	{
		return _clash;
	}

	/**
	 * Set _clash dependency
	 *
	 * @param _clash
	 */
	protected void setClash(final ClashDependency clash)
	{
		_clash = clash;
	}
}
