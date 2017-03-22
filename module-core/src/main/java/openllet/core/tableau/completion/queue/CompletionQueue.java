// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com
//
// ---
// Portions Copyright (c) 2006 Christian Halaschek-Wiener
// Halaschek-Wiener parts of this source code are available under the terms of the MIT License.
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

package openllet.core.tableau.completion.queue;

import java.util.logging.Logger;
import openllet.aterm.ATermAppl;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.IndividualIterator;
import openllet.core.boxes.abox.Node;
import openllet.shared.tools.Log;

/**
 * <p>
 * Title: Completion Queue
 * </p>
 * <p>
 * Description: A _queue for individuals that need to have completion rules applied
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Christian Halaschek-Wiener
 */
public abstract class CompletionQueue extends IndividualIterator
{

	public final static Logger _logger = Log.getLogger(CompletionQueue.class);

	private boolean _allowLiterals = false;

	protected int _currentType; // Access fom OptimizedBasicCompletionQueue only

	protected boolean _closed = false;

	/**
	 * Constructor - create queue
	 *
	 * @param _abox
	 */
	protected CompletionQueue(final ABox abox)
	{
		super(abox);
	}

	/**
	 * Find the next _individual in a given _queue
	 *
	 * @param type
	 */
	protected abstract void findNext(int type);

	/**
	 * Reset the _queue to be the _current _nodes in the _abox; Also reset the type _index to 0
	 *
	 * @param branch
	 */
	public abstract void restore(int branch);

	/**
	 * Add an element to the _queue
	 *
	 * @param x
	 * @param type
	 */
	public abstract void add(QueueElement x, NodeSelector type);

	/**
	 * Add an element to all queues
	 *
	 * @param x
	 */
	public abstract void add(QueueElement x);

	/**
	 * Reset the _current pointer
	 *
	 * @param type
	 */
	@Override
	public abstract void reset(NodeSelector type);

	/**
	 * Set branch pointers to current pointer. This is done whenever abox.incrementBranch is called
	 *
	 * @param branch
	 */
	public abstract void incrementBranch(int branch);

	/**
	 * @return a copy of the queue
	 */
	public abstract CompletionQueue copy();

	/**
	 * Set the _abox for the _queue
	 *
	 * @param ab
	 */
	public void setABox(final ABox ab)
	{
		_abox = ab;
	}

	/**
	 * Print method for a given _queue type
	 *
	 * @param type
	 */
	public abstract void print(int type);

	/**
	 * Print method for entire _queue
	 */
	public abstract void print();

	/**
	 * Print branch information
	 */
	public static void printBranchInfo()
	{
		return;
	}

	/**
	 * Set flag to allow literals
	 *
	 * @param val
	 */
	public void setAllowLiterals(final boolean val)
	{
		_allowLiterals = val;
	}

	/**
	 * Flush the queue
	 */
	public abstract void flushQueue();

	/**
	 * Flush the queue
	 */
	protected abstract void flushQueue(NodeSelector s);

	/**
	 * Clear the queue
	 *
	 * @param type
	 */
	public abstract void clearQueue(NodeSelector type);

	/**
	 * Get flag to allow literals
	 *
	 * @return
	 */
	protected boolean isAllowLiterals()
	{
		return _allowLiterals;
	}

	/**
	 * @return next literal
	 */
	public abstract Node nextLiteral();

	/**
	 * Get next label
	 *
	 * @return
	 */
	protected static ATermAppl getNextLabel()
	{
		return null;
	}

	public void setClosed(final boolean isClash)
	{
		_closed = isClash;
	}
}
