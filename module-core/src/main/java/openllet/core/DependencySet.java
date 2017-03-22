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

package openllet.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import openllet.aterm.ATermAppl;
import openllet.core.tableau.completion.incremental.DependencyIndex;
import openllet.core.utils.SetUtils;
import openllet.core.utils.intset.IntSet;
import openllet.core.utils.intset.IntSetFactory;
import openllet.shared.tools.Log;

/**
 * DependencySet for concepts and edges in the ABox for backjumping
 *
 * @author Evren Sirin
 */
public class DependencySet
{
	public static final Logger _logger = Log.getLogger(DependencySet.class);

	public static final int NO_BRANCH = -1;

	/**
	 * An empty dependency set
	 */
	public static final DependencySet EMPTY = new DependencySet();

	/**
	 * Used for assertions that are true by nature, i.e. an _individual always has type owl:Thing
	 */
	public static final DependencySet INDEPENDENT = new DependencySet(0);

	public static final IntSet ZERO = IntSetFactory.create(0);

	/**
	 * A dummy dependency set that is used just to indicate there is a dependency
	 */
	public static final DependencySet DUMMY = new DependencySet(1);

	/**
	 * _index of branches this assertion _depends on
	 */
	private final IntSet _depends;

	/**
	 * _branch number when this assertion was added to ABox
	 */
	private int _branch = NO_BRANCH;

	private volatile Set<ATermAppl> _explain;

	/**
	 * Create an empty set
	 */
	private DependencySet()
	{
		_depends = IntSetFactory.create();
		setExplain(Collections.emptySet());
	}

	/**
	 * Create a dependency set that _depends on a single _branch
	 *
	 * @param branch Branch number
	 */
	public DependencySet(final int branch)
	{
		_depends = IntSetFactory.create(branch);
		setExplain(Collections.emptySet());
	}

	/**
	 * Creates a dependency set with the given IntSet (no separate copy of IntSet is created so if IntSet is modified this DependencySet will be affected).
	 */
	private DependencySet(final int branch, final IntSet depends, final Set<ATermAppl> explain)
	{
		_branch = branch;
		_depends = depends;
		setExplain(explain);
	}

	/**
	 * Creates a dependency set with no dependency and single explanation atom
	 *
	 * @param explainAtom
	 */
	public DependencySet(final ATermAppl explainAtom)
	{
		_depends = DependencySet.ZERO;
		setExplain(SetUtils.singleton(explainAtom));

	}

	/**
	 * Creates a dependency set with no dependency and a set of explanation atoms
	 *
	 * @param explain
	 */
	public DependencySet(final Set<ATermAppl> explain)
	{
		_depends = DependencySet.ZERO;
		setExplain(explain);
	}

	/**
	 * @param newBranch
	 * @return a new DependencySet object with a new _branch number where the IntSet is openllet.shared.hash (changing one will change the other).
	 */
	public DependencySet copy(final int newBranch)
	{
		return new DependencySet(newBranch, _depends, _explain);
	}

	/**
	 * @param b
	 * @return true if <code>b</code> is in this set.
	 */
	public boolean contains(final int b)
	{
		return _depends.contains(b);
	}

	/**
	 * Add the integer value <code>b</code> to this DependencySet.
	 *
	 * @param b
	 */
	public void add(final int b)
	{
		_depends.add(b);
	}

	/**
	 * Remove the integer value <code>b</code> from this DependencySet.
	 *
	 * @param b
	 */
	public void remove(final int b)
	{
		_depends.remove(b);
	}

	/**
	 * @return true if there is no dependency on a non-deterministic _branch
	 */
	public boolean isIndependent()
	{
		return max() <= 0;
	}

	/**
	 * @return the _branch number when the dependency set was created
	 */
	public int getBranch()
	{
		return _branch;
	}

	/**
	 * @return the number of elements in this set.
	 */
	public int size()
	{
		return _depends.size();
	}

	/**
	 * @return the maximum value in this set.
	 */
	public int max()
	{
		return _depends.isEmpty() ? -1 : _depends.max();
	}

	/**
	 * @param ds
	 * @return a new DependencySet and all the elements of <code>this</code> and <code>set</code> .
	 */
	public DependencySet union(final IntSet ds)
	{
		return new DependencySet(_branch, _depends.union(ds), _explain);
	}

	/**
	 * @param ds
	 * @param doExplanation
	 * @return a new DependencySet and all the elements of <code>this</code> and <code>ds</code>.
	 */
	public DependencySet union(final DependencySet ds, final boolean doExplanation)
	{
		final IntSet newDepends = _depends.union(ds._depends);

		final Set<ATermAppl> newExplain = doExplanation ? //
				SetUtils.union(_explain, ds._explain) : //
				Collections.emptySet();

		return new DependencySet(_branch, newDepends, newExplain);
	}

	/**
	 * @param explain
	 * @param doExplanation
	 * @return the union as dependencySet
	 */
	public DependencySet union(final Set<ATermAppl> explain, final boolean doExplanation)
	{
		if (!doExplanation || explain.isEmpty())
			return this;

		return new DependencySet(_branch, _depends.copy(), SetUtils.union(_explain, explain));
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("DS{branch:");
		switch (_branch)
		{
			case NO_BRANCH:
				sb.append("NO_BRANCH(-1)");
				break;

			default:
				sb.append(_branch);
		}
		sb.append(", depends:");
		sb.append(_depends);
		if (_logger.isLoggable(Level.FINE))
		{
			sb.append(", explain:");
			sb.append(_explain);
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Remove explanation sets which contain references to a syntactic assertion
	 *
	 * @param assertion
	 */
	public void removeExplain(final ATermAppl assertion)
	{
		if (getExplain().contains(assertion))
		{
			setExplain(new HashSet<ATermAppl>());
			DependencyIndex._logger.fine("             Explain: removed ");
		}

	}

	public IntSet getDepends()
	{
		return _depends;
	}

	/**
	 * @param explain the explain to set
	 */
	public void setExplain(final Set<ATermAppl> explain)
	{
		_explain = explain;
	}

	/**
	 * @return the set of explanations associated with this DependencySet.
	 */
	public Set<ATermAppl> getExplain()
	{
		return _explain;
	}

	/**
	 * Return a dummy representation of this DependencySet such that <code>this.isIndependent() == this.copyForCache().isIndependent()</code> The returned copy
	 * will not be accurate w.r.t. any other function call, e.g. <code>contains(int)</code> for the copy will return different results for the copy. This
	 * function does not create a new DependencySet object so will not require additional memory. Caching this copy is more appropriate so we don't waste space
	 * for storing the actual dependency set or the explanation which are not used in caches anyway.
	 *
	 * @return the dummy dependency set, the cache
	 */
	public DependencySet cache()
	{
		return isIndependent() ? DependencySet.INDEPENDENT : DependencySet.DUMMY;
	}
}
