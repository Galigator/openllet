// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com
//
// ---
// Portions Copyright (c) 2003 Ron Alford, Mike Grove, Bijan Parsia, Evren Sirin
// Alford, Grove, Parsia, Sirin parts of this source code are available under the terms of the MIT License.
//
// The MIT License
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package openllet.core.boxes.abox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.boxes.rbox.Role;
import openllet.core.utils.ATermUtils;

/**
 * @author Evren Sirin
 */
public class EdgeList extends ArrayList<Edge>
{
	private static final long serialVersionUID = -4849551813090525636L;

	public EdgeList()
	{
		super();
	}

	public EdgeList(final int n)
	{
		super(n);
	}

	public EdgeList(final EdgeList edges)
	{
		this(edges.size());
		addEdgeList(edges);
	}

	@Deprecated
	public void addEdgeList(final EdgeList edges)
	{
		super.addAll(edges);
	}

	@Deprecated
	public void addEdge(final Edge e)
	{
		super.add(e);
	}

	/**
	 * Remove an element by replacing it by the last element. Order isn't preserved.
	 *
	 * @param edge to remove
	 * @return true if removing occured
	 */
	public boolean removeEdge(final Edge edge)
	{
		for (int i = 0, l = size(); i < l; i++)
			if (get(i).equals(edge))
			{
				removeEdge(i);
				return true;
			}

		return false;
	}

	protected void removeEdge(final int index)
	{
		final int l = size() - 1;
		if (index == l)
			remove(l);
		else
			set(index, remove(l)); // The last one is remove (no array copy require, then it replace the element to remove)
	}

	public Edge edgeAt(final int i)
	{
		return get(i);
	}

	public EdgeList sort()
	{
		final EdgeList sorted = new EdgeList(this);
		sort((e1, e2) -> e1.getDepends().max() - e2.getDepends().max());
		return sorted;
	}

	private EdgeList findEdges(final Role role, final Individual from, final Node to)
	{
		final EdgeList result = new EdgeList();

		for (final Edge e : this)
			if ((from == null || from.equals(e.getFrom())) && (role == null || e.getRole().isSubRoleOf(role)) && (to == null || to.equals(e.getTo())))
				result.addEdge(e);

		return result;
	}

	public EdgeList getEdgesTo(final Node to)
	{
		return findEdges(null, null, to);
	}

	public EdgeList getEdgesTo(final Role r, final Node to)
	{
		return findEdges(r, null, to);
	}

	public EdgeList getEdgesFrom(final Individual from, final Role r)
	{
		return findEdges(r, from, null);
	}

	public EdgeList getEdges(final Role role)
	{
		final EdgeList result = new EdgeList();

		for (final Edge e : this)
			if (e.getRole().isSubRoleOf(role))
				result.addEdge(e);

		return result;
	}

	public Set<Role> getRoles()
	{
		final Set<Role> result = new HashSet<>();

		for (final Edge e : this)
			result.add(e.getRole());

		return result;
	}

	public Set<Node> getNeighbors(final Node node)
	{
		final Set<Node> result = new HashSet<>();

		for (final Edge e : this)
			result.add(e.getNeighbor(node));

		return result;
	}

	/**
	 * Find the neighbors of a _node that has a certain type. For literals, we collect only the ones with the same language tag.
	 *
	 * @param node The _node whose neighbors are being sought
	 * @param c The concept (or datatype) that each _neighbor should belong to
	 * @return Set of _nodes
	 */
	public Set<Node> getFilteredNeighbors(final Individual node, final ATermAppl c)
	{
		final Set<Node> result = new HashSet<>();

		String lang = null;
		for (final Edge edge : this)
		{
			final Node neighbor = edge.getNeighbor(node);

			if (!ATermUtils.isTop(c) && !neighbor.hasType(c))
				continue;
			else
				if (neighbor instanceof Literal)
				{
					final Literal lit = (Literal) neighbor;
					if (lang == null)
					{
						lang = lit.getLang();
						result.add(neighbor);
					}
					else
						if (lang.equals(lit.getLang()))
							result.add(neighbor);
				}
				else
					result.add(neighbor);
		}

		return result;
	}

	public boolean hasEdgeFrom(final Individual from)
	{
		return hasEdge(from, null, null);
	}

	public boolean hasEdgeTo(final Node to)
	{
		return hasEdge(null, null, to);
	}

	public boolean hasEdge(final Role role)
	{
		return hasEdge(null, role, null);
	}

	/**
	 * Checks if this list contains an edge matching the given subject, predicate and object. A null parameter is treated as a wildcard matching every value and
	 * predicates are matched by considering the subproperty hierarchy, i.e. passing the parameter <code>sup</code> to this function will return
	 * <code>true</code> if an edge with subproperty <code>sub</code> exists.
	 *
	 * @param from
	 * @param role
	 * @param to
	 * @return true if contains an edge that match
	 */
	public boolean hasEdge(final Individual from, final Role role, final Node to)
	{
		for (final Edge e : this)
			if ((from == null || from.equals(e.getFrom())) && (role == null || e.getRole().isSubRoleOf(role)) && (to == null || to.equals(e.getTo())))
				return true;

		return false;
	}

	/**
	 * Similar to {@link #hasEdge(Individual, Role, Node)} but does not consider subproperty hierarchy for matching so only exact predicate matches are
	 * considered.
	 *
	 * @param from
	 * @param role
	 * @param to
	 * @return true if contains an edge that match
	 */
	public boolean hasExactEdge(final Individual from, final Role role, final Node to)
	{
		for (final Edge e : this)
			if ((from == null || from.equals(e.getFrom())) && (role == null || e.getRole().equals(role)) && (to == null || to.equals(e.getTo())))
				return true;

		return false;
	}

	public boolean hasEdge(final Edge e)
	{
		return hasEdge(e.getFrom(), e.getRole(), e.getTo());
	}

	public Edge getExactEdge(final Individual from, final Role role, final Node to)
	{
		for (final Edge e : this)
			if ((from == null || from.equals(e.getFrom())) && (role == null || e.getRole().equals(role)) && (to == null || to.equals(e.getTo())))
				return e;

		return null;
	}

	public DependencySet getDepends(final boolean doExplanation)
	{
		DependencySet ds = DependencySet.INDEPENDENT;

		for (final Edge e : this)
			ds = ds.union(e.getDepends(), doExplanation);

		return ds;
	}

	/**
	 * Resets the edges in this list to only asserted edges.
	 */
	public void reset()
	{
		for (int i = 0; i < size(); i++)
		{
			final Edge e = get(i);

			if (e.getDepends().getBranch() != DependencySet.NO_BRANCH)
				removeEdge(i--);
		}
	}
}
