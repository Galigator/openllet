package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunAnd extends AFunOwl
{

	public FunAnd(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 1;
	}
}
