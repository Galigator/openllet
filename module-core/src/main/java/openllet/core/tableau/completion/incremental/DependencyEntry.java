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
	private final Set<TypeDependency>			_types			= new HashSet<>();

	/**
	 * The set of merges which are dependent
	 */
	private final Set<MergeDependency>			_merges			= new HashSet<>();

	/**
	 * The set of edge which are dependent
	 */
	private final Set<Edge>						_edges			= new HashSet<>();

	/**
	 * The set of branches which are dependent
	 */
	private final Set<AddBranchDependency>		_branchAdds		= new HashSet<>();

	/**
	 * The set of _branch remove ds' which are dependent
	 */
	private final Set<CloseBranchDependency>	_branchCloses	= new HashSet<>();

	/**
	 * Clash dependency
	 */
	private volatile Optional<ClashDependency>	_clash			= Optional.empty();

	/**
	 * Default constructor
	 */
	public DependencyEntry()
	{
		// Nothing to do.
	}

	public DependencyEntry(final DependencyEntry that)
	{
		_types.addAll(that._types); //TODO:may need to perform a deep copy here
		_merges.addAll(that._merges); //TODO:may need to perform a deep copy here

		for (final Edge next : that._edges) //copy edge depenedencies
			_edges.add(new DefaultEdge(next.getRole(), next.getFrom(), next.getTo(), next.getDepends()));

		_branchAdds.addAll(that._branchAdds); //TODO:may need to perform a deep copy here
		_branchCloses.addAll(that._branchCloses); //TODO:may need to perform a deep copy here
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
		_types.add(new TypeDependency(ind, type));
	}

	/**
	 * Add a edge dependency
	 *
	 * @param edge
	 */
	protected void addEdgeDependency(final Edge edge)
	{
		_edges.add(edge);
	}

	/**
	 * Add a edge dependency
	 *
	 * @param ind
	 * @param mergedTo
	 */
	protected void addMergeDependency(final ATermAppl ind, final ATermAppl mergedTo)
	{
		_merges.add(new MergeDependency(ind, mergedTo));
	}

	/**
	 * Add a branch add dependency
	 *
	 * @param branchId
	 * @param branch
	 */
	protected BranchDependency addBranchAddDependency(final ATermAppl assertion, final Branch branch)
	{
		final AddBranchDependency b = new AddBranchDependency(assertion, branch);

		_branchAdds.add(b);
		return b;
	}

	/**
	 * Add a branch remove ds dependency
	 *
	 * @param branchId
	 * @param branch
	 */
	protected BranchDependency addCloseBranchDependency(final ATermAppl assertion, final Branch theBranch)
	{
		final CloseBranchDependency b = new CloseBranchDependency(assertion, theBranch.getTryNext(), theBranch);

		if (_branchCloses.contains(b)) _branchCloses.remove(b);

		_branchCloses.add(b);
		return b;
	}

	/**
	 * Helper method to print all dependencies TODO: this print is not complete
	 */
	public void print()
	{
		System.out.println("  Edge Dependencies:");
		for (final Edge e : _edges)
			System.out.println("    " + e.toString());

		System.out.println("  Type Dependencies:");
		for (final TypeDependency t : _types)
			System.out.println("    " + t.toString());

		System.out.println("  Merge Dependencies:");
		for (final MergeDependency m : _merges)
			System.out.println("    " + m.toString());
	}

	/**
	 * @return the edges
	 */
	public Set<Edge> getEdges()
	{
		return _edges;
	}

	/**
	 * @return the merges
	 */
	public Set<MergeDependency> getMerges()
	{
		return _merges;
	}

	/**
	 * @return the types
	 */
	public Set<TypeDependency> getTypes()
	{
		return _types;
	}

	/**
	 * @return get branches
	 */
	public Set<AddBranchDependency> getBranchAdds()
	{
		return _branchAdds;
	}

	/**
	 * @return the close branches for this entry
	 */
	public Set<CloseBranchDependency> getCloseBranches()
	{
		return _branchCloses;
	}

	/**
	 * @return the clash dependency
	 */
	public Optional<ClashDependency> getClash()
	{
		return _clash;
	}

	/**
	 * Set clash dependency
	 *
	 * @param clash
	 */
	protected void setClash(final ClashDependency clash)
	{
		_clash = Optional.ofNullable(clash);
	}
}
