package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunMax extends AFunOwl
{
	public FunMax(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 3;
	}
}
