package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunOr extends AFunOwl
{
	public FunOr(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 1;
	}
}
