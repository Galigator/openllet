package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunAll extends AFunOwl
{
	public FunAll(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 2;
	}
}
