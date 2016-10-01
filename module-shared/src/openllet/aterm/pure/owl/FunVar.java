package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunVar extends AFunOwl
{
	public FunVar(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 1;
	}
}
