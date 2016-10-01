package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunSome extends AFunOwl
{
	public FunSome(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 2;
	}
}
