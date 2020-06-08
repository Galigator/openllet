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

package openllet.core.boxes.tbox.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import openllet.aterm.AFun;
import openllet.aterm.ATermAppl;
import openllet.atom.OpenError;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.CollectionUtils;

/**
 * @author Evren Sirin
 */
public class TermDefinition
{
	private final List<ATermAppl>	_subClassAxioms;
	private final List<ATermAppl>	_eqClassAxioms;
	private Set<ATermAppl>			_dependencies;

	public TermDefinition()
	{
		_subClassAxioms = new ArrayList<>();
		_eqClassAxioms = new ArrayList<>();
		updateDependencies();
	}

	/**
	 * @return the identity(==) set of dependences.
	 */
	public Set<ATermAppl> getDependencies()
	{
		if (_dependencies == null) updateDependencies();
		return _dependencies;
	}

	/**
	 * clear the identity set of dependences.
	 */
	public void clearDependencies()
	{
		_dependencies = null;
	}

	public ATermAppl getName()
	{
		if (!_subClassAxioms.isEmpty()) return (ATermAppl) _subClassAxioms.get(0).getArgument(0);

		if (!_eqClassAxioms.isEmpty()) return (ATermAppl) _eqClassAxioms.get(0).getArgument(0);

		return null;
	}

	public boolean addDef(final ATermAppl appl)
	{
		boolean added = false;

		final AFun fun = appl.getAFun();
		if (fun.equals(ATermUtils.SUBFUN))
			added = _subClassAxioms.contains(appl) ? false : _subClassAxioms.add(appl);
		else if (fun.equals(ATermUtils.EQCLASSFUN))
			added = _eqClassAxioms.contains(appl) ? false : _eqClassAxioms.add(appl);
		else
			throw new OpenError("Cannot add non-definition!");

		if (added) updateDependencies();

		return added;
	}

	public boolean removeDef(final ATermAppl axiom)
	{
		boolean removed;

		final AFun fun = axiom.getAFun();
		if (fun.equals(ATermUtils.SUBFUN))
			removed = _subClassAxioms.remove(axiom);
		else if (fun.equals(ATermUtils.EQCLASSFUN))
			removed = _eqClassAxioms.remove(axiom);
		else
			throw new OpenError("Cannot remove non-definition!");

		updateDependencies();

		return removed;
	}

	public boolean isPrimitive()
	{
		return _eqClassAxioms.isEmpty();
	}

	public boolean isUnique()
	{
		return _eqClassAxioms.isEmpty() || _subClassAxioms.isEmpty() && _eqClassAxioms.size() == 1;
	}

	public boolean isUnique(final ATermAppl axiom)
	{
		return _eqClassAxioms.isEmpty() && (_subClassAxioms.isEmpty() || axiom.getAFun().equals(ATermUtils.SUBFUN));
	}

	public List<ATermAppl> getSubClassAxioms()
	{
		return _subClassAxioms;
	}

	public List<ATermAppl> getEqClassAxioms()
	{
		return _eqClassAxioms;
	}

	@Override
	public String toString()
	{
		return _subClassAxioms + "; " + _eqClassAxioms;
	}

	protected void updateDependencies()
	{
		_dependencies = CollectionUtils.makeIdentitySet();
		for (final ATermAppl sub : getSubClassAxioms())
			ATermUtils.findPrimitives((ATermAppl) sub.getArgument(1), _dependencies);
		for (final ATermAppl eq : getEqClassAxioms())
			ATermUtils.findPrimitives((ATermAppl) eq.getArgument(1), _dependencies);
	}
}
