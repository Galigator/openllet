package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunEquivalentProperty extends AFunOwl
{
	public FunEquivalentProperty(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 2;
	}
}
