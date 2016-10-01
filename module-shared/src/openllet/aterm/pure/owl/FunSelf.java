package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunSelf extends AFunOwl
{
	public FunSelf(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 1;
	}
}
