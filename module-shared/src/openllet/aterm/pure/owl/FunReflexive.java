package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunReflexive extends AFunOwl
{
	public FunReflexive(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 1;
	}
}
