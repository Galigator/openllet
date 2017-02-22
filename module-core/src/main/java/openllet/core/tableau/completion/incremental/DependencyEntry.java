// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.completion.incremental;

import java.util.HashSet;
import java.util.Optional;
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
	private final Set<TypeDependency> types = new HashSet<>();

	/**
	 * The set of merges which are dependent
	 */
	private final Set<MergeDependency> merges = new HashSet<>();

	/**
	 * The set of edge which are dependent
	 */
	private final Set<Edge> edges = new HashSet<>();

	/**
	 * The set of branches which are dependent
	 */
	private final Set<BranchAddDependency> branchAdds = new HashSet<>();

	/**
	 * The set of _branch remove ds' which are dependent
	 */
	private final Set<CloseBranchDependency> branchCloses = new HashSet<>();

	/**
	 * Clash dependency
	 */
	private volatile Optional<ClashDependency> _clash = Optional.empty();

	/**
	 * Default constructor
	 */
	public DependencyEntry()
	{
		// Nothing to do.
	}

	public DependencyEntry(final DependencyEntry that)
	{
		types.addAll(that.types); //TODO:may need to perform a deep copy here
		merges.addAll(that.merges); //TODO:may need to perform a deep copy here

		for (final Edge next : that.edges) //copy edge depenedencies
			edges.add(new DefaultEdge(next.getRole(), next.getFrom(), next.getTo(), next.getDepends()));

		branchAdds.addAll(that.branchAdds); //TODO:may need to perform a deep copy here
		branchCloses.addAll(that.branchCloses); //TODO:may need to perform a deep copy here
		_clash = that._clash; //TODO:may need to perform a deep copy here
	}

	public DependencyEntry copy()
	{
		return new DependencyEntry(this);
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
	public Optional<ClashDependency> getClash()
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
		_clash = Optional.ofNullable(clash);
	}
}
