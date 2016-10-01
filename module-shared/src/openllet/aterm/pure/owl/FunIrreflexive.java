package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunIrreflexive extends AFunOwl
{
	public FunIrreflexive(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 1;
	}
}
