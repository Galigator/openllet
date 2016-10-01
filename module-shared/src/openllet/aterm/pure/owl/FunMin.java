package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunMin extends AFunOwl
{
	public FunMin(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 3;
	}
}
