package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunDifferent extends AFunOwl
{
	public FunDifferent(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 2;
	}
}
