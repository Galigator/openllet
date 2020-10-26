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

import openllet.aterm.ATermAppl;
import openllet.core.boxes.abox.Node;

/**
 * Structured stored on the completion _queue
 *
 * @author Christian Halaschek-Wiener
 */
public class QueueElement
{

	/**
	 * Label for this element
	 */
	private volatile ATermAppl _label;

	/**
	 * Node for this element
	 */
	private volatile ATermAppl _node;

	/**
	 * Constructor
	 *
	 * @param n The _node
	 */
	public QueueElement(final Node n)
	{
		this(n, null);
	}

	public QueueElement(final Node n, final ATermAppl l)
	{
		this(n.getName(), l);
	}

	/**
	 * Constructor
	 *
	 * @param ATermAppl The _node
	 * @param ATermAppl The label
	 */
	private QueueElement(final ATermAppl n, final ATermAppl l)
	{
		_node = n;

		//This will be set to null only if its called from ABox.createLiteral or Node.setChanged
		//In these cases, the element will be added to the LITERALLIST or DATATYPELIST respectively
		//In both cases it does not matter.
		_label = l;
	}

	/**
	 * To string
	 */
	@Override
	public String toString()
	{
		return _node.getName() + "[" + _label + "]";
	}

	/**
	 * Set label
	 *
	 * @param l The label
	 */
	public void setLabel(final ATermAppl l)
	{
		_label = l;
	}

	/**
	 * Set the _node
	 *
	 * @param n The _node
	 */
	public void setNode(final ATermAppl n)
	{
		_node = n;
	}

	/**
	 * Get the label
	 *
	 * @return ATermAppl The label
	 */
	public ATermAppl getLabel()
	{
		return _label;
	}

	/**
	 * Get the _node
	 *
	 * @return ATermAppl The _node
	 */
	public ATermAppl getNode()
	{
		return _node;
	}
}
