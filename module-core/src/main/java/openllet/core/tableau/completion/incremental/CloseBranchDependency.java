// The MIT License
//
// Copyright (c) 2007 Christian Halaschek-Wiener
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

package openllet.core.tableau.completion.incremental;

import openllet.aterm.ATermAppl;
import openllet.core.tableau.branch.Branch;

/**
 * A dependency for a closed disjunct, merge pair, etc. for a _branch
 *
 * @author Christian Halaschek-Wiener
 */
public class CloseBranchDependency extends BranchDependency
{
	private final int _tryNext;

	private final Branch _closeBranch;

	public CloseBranchDependency(final ATermAppl assertion, final int tryNext, final Branch theBranch)
	{
		super(assertion);
		_tryNext = tryNext;
		_closeBranch = theBranch;
	}

	public ATermAppl getInd()
	{
		return _closeBranch.getNode().getName();
	}

	@Override
	public String toString()
	{
		return "Branch [" + _closeBranch.getNode().getName() + "]  -  [" + _closeBranch.getBranchIndexInABox() + "]";
	}

	@Override
	public boolean equals(final Object other)
	{
		if (other instanceof CloseBranchDependency)
			return getInd().equals(((CloseBranchDependency) other).getInd()) && getBranch() == ((CloseBranchDependency) other).getBranch() && _tryNext == ((CloseBranchDependency) other)._tryNext;
		else
			return false;
	}

	/**
	 * TODO: this hash may not be sufficient
	 */
	@Override
	public int hashCode()
	{
		return getInd().hashCode() + getBranch() + _tryNext;
	}

	public int getBranch()
	{
		return _closeBranch.getBranchIndexInABox();
	}

	public int getTryNext()
	{
		return _tryNext;
	}

	public Branch getCloseBranch()
	{
		return _closeBranch;
	}

}
