// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules.rete;

import java.util.Iterator;

import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.boxes.abox.ABoxForStrategy;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.Node;
import openllet.core.rules.model.AtomIConstant;
import openllet.core.rules.model.AtomObject;
import openllet.core.rules.model.AtomVariable;
import openllet.core.rules.model.ClassAtom;
import openllet.core.rules.model.RuleAtom;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.iterator.IteratorUtils;
import openllet.core.utils.iterator.NestedIterator;

public class AlphaTypeNode extends AlphaNode
{
	private final ATermAppl _predicate;
	private final ATermAppl _name;
	private Individual _node;

	public AlphaTypeNode(final ABoxForStrategy abox, final ATermAppl predicate)
	{
		this(abox, predicate, null);
	}

	public AlphaTypeNode(final ABoxForStrategy abox, final ATermAppl predicate, final ATermAppl name)
	{
		super(abox);
		_predicate = predicate;
		_name = name;
	}

	protected Individual initNode()
	{
		if (_node == null)
			_node = (Individual) initNode(_name);
		assert _node != null;
		return _node;
	}

	public boolean activate(final Individual ind, final ATermAppl type, final DependencySet ds)
	{
		assert _predicate.equals(type);
		if (_name != null)
		{
			final Individual node = initNode();
			if (!ind.isSame(node))
				return false;
		}

		activate(WME.createType(ind, type, ds));
		return true;
	}

	@Override
	public Iterator<WME> getMatches(final int argIndex, final Node arg)
	{
		if (_name != null || argIndex != 0)
			throw new IndexOutOfBoundsException();

		if (!(arg instanceof Individual))
			throw new IllegalArgumentException();

		final DependencySet depends = arg.getDepends(_predicate);

		return depends == null ? IteratorUtils.<WME> emptyIterator() : IteratorUtils.<WME> singletonIterator(WME.createType((Individual) arg, _predicate, depends));

	}

	@Override
	public Iterator<WME> getMatches()
	{
		final Iterator<Individual> inds = _name == null ? _abox.getIndIterator() : IteratorUtils.singletonIterator(initNode());
		return new NestedIterator<>(inds)
		{
			@Override
			public Iterator<WME> getInnerIterator(final Individual ind)
			{
				final DependencySet depends = ind.getDepends(_predicate);

				return depends == null ? IteratorUtils.<WME> emptyIterator() : IteratorUtils.<WME> singletonIterator(WME.createType(ind, _predicate, depends));
			}
		};
	}

	@Override
	public boolean matches(final RuleAtom atom)
	{
		return atom instanceof ClassAtom && atom.getPredicate().equals(_predicate) && argMatches((ClassAtom) atom);
	}

	private boolean argMatches(final ClassAtom atom)
	{
		final AtomObject arg = atom.getArgument();
		return _name == null ? arg instanceof AtomVariable : arg instanceof AtomIConstant && ((AtomIConstant) arg).getValue().equals(_name);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + _predicate.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof AlphaTypeNode))
			return false;
		final AlphaTypeNode other = (AlphaTypeNode) obj;
		return _predicate.equals(other._predicate) && (_name == null ? other._name == null : _name.equals(other._name));
	}

	@Override
	public String toString()
	{
		return ATermUtils.toString(_predicate) + "(" + (_name == null ? "0" : _name) + ")";
	}
}
