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

package openllet.core.tableau.branch;

import java.util.logging.Logger;

import openllet.core.DependencySet;
import openllet.core.OpenlletOptions;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.Clash;
import openllet.core.boxes.abox.Node;
import openllet.core.tableau.completion.CompletionStrategy;
import openllet.shared.tools.Log;

/**
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public abstract class Branch implements Comparable<Branch>
{
	public static final Logger _logger = Log.getLogger(Branch.class);

	protected final ABox _abox;

	private final int _branchIndexInABox;
	private final int _anonCount;
	private volatile DependencySet _termDepends;
	private volatile DependencySet _combinedClash;

	protected volatile CompletionStrategy _strategy;
	protected volatile int _tryCount;
	protected volatile int _tryNext;
	protected volatile int _nodeCount;

	protected Branch(final ABox abox, final CompletionStrategy strategy, final DependencySet ds, final int n)
	{
		_abox = abox;
		_strategy = strategy;

		_termDepends = ds;
		_tryCount = n;
		_combinedClash = DependencySet.EMPTY;
		_tryNext = 0;

		_branchIndexInABox = abox.getBranchIndex();
		_anonCount = abox.getAnonCount();
		_nodeCount = abox.size();
	}

	protected Branch(final ABox abox, final int n, final Branch br)
	{
		_abox = abox; // Changing Abox ? seriously ?
		_strategy = br._strategy;

		_termDepends = br._termDepends;
		_tryCount = n; // Changing count.
		_combinedClash = DependencySet.EMPTY;
		_tryNext = br._tryNext;

		_branchIndexInABox = br._branchIndexInABox;
		_anonCount = br._anonCount;
		_nodeCount = br._nodeCount;
	}

	public void setLastClash(final DependencySet ds)
	{
		if (getTryNext() >= 0)
		{
			_combinedClash = _combinedClash.union(ds, _abox.doExplanation());
			if (OpenlletOptions.USE_INCREMENTAL_DELETION)
				//CHW - added for incremental deletions support THIS SHOULD BE MOVED TO SUPER
				_abox.getKB().getDependencyIndex().addCloseBranchDependency(this, ds);
		}
	}

	public DependencySet getCombinedClash()
	{
		return _combinedClash;
	}

	public void setStrategy(final CompletionStrategy strategy)
	{
		_strategy = strategy;
	}

	public boolean tryNext()
	{
		// nothing more to try, update the clash dependency
		if (getTryNext() == getTryCount())
			if (!_abox.isClosed())
				_abox.setClash(Clash.unexplained(getNode(), _termDepends));
			else
				_abox.getClash().setDepends(getCombinedClash());

		// if there is no clash try next possibility
		if (!_abox.isClosed())
			tryBranch();

		// there is a clash so there is no point in trying this
		// _branch again. remove this _branch from clash dependency
		if (_abox.isClosed())
			if (!OpenlletOptions.USE_INCREMENTAL_DELETION)
				_abox.getClash().getDepends().remove(getBranchIndexInABox());

		return !_abox.isClosed();
	}

	public abstract Branch copyTo(ABox abox);

	protected abstract void tryBranch();

	public abstract Node getNode();

	@Override
	public String toString()
	{
		return "{Branch [" + getNode() + "]  n°: " + getBranchIndexInABox() + " tryNext:" + getTryNext() + " tryCount:" + getTryCount() + "}";
	}

	/**
	 * Added for to re-open closed branches. This is needed for incremental reasoning through deletions
	 *
	 * @param branchIndex The shift _index
	 */
	public abstract void shiftTryNext(int branchIndex);

	/**
	 * @param nodeCount the _nodeCount to set
	 */
	public void setNodeCount(final int nodeCount)
	{
		_nodeCount = nodeCount;
	}

	/**
	 * @return the _nodeCount
	 */
	public int getNodeCount()
	{
		return _nodeCount;
	}

	/**
	 * @return the _branch
	 */
	public int getBranchIndexInABox()
	{
		return _branchIndexInABox;
	}

	/**
	 * @return the _anonCount
	 */
	public int getAnonCount()
	{
		return _anonCount;
	}

	/**
	 * @param tryNext the _tryNext to set
	 */
	public void setTryNext(final int tryNext)
	{
		_tryNext = tryNext;
	}

	/**
	 * @return the _tryNext
	 */
	public int getTryNext()
	{
		return _tryNext;
	}

	/**
	 * @param tryCount the _tryCount to set
	 */
	public void setTryCount(final int tryCount)
	{
		_tryCount = tryCount;
	}

	/**
	 * @return the _tryCount
	 */
	public int getTryCount()
	{
		return _tryCount;
	}

	/**
	 * @param termDepends the _termDepends to set
	 */
	public void setTermDepends(final DependencySet termDepends)
	{
		_termDepends = termDepends;
	}

	/**
	 * @return the _termDepends
	 */
	public DependencySet getTermDepends()
	{
		return _termDepends;
	}

	@Override
	public int compareTo(final Branch that)
	{
		return this == that ? 0 : _branchIndexInABox - that._branchIndexInABox;
	}
}
